package com.deckoptimizer;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class DeckService {
    private static final Logger logger = LoggerFactory.getLogger(DeckService.class);
    private final CardLoader cardLoader;

    public DeckService(CardLoader cardLoader) {
        this.cardLoader = cardLoader;
        // Add debug logging
        List<Card> allCards = cardLoader.getCards();
        logger.info("Loaded cards:");
        allCards.forEach(card -> logger.info("Card: {}, Image: {}", card.getName(), card.getImageUrl()));
        
        // Specifically check for X-Bow
        Optional<Card> xbow = allCards.stream()
            .filter(c -> "X-Bow".equals(c.getName()))
            .findFirst();
        if (xbow.isPresent()) {
            logger.info("X-Bow found: {}", xbow.get().getImageUrl());
        } else {
            logger.warn("X-Bow not found in loaded cards!");
        }
    }

    public Deck generateBalancedDeck() {
        List<Card> allCards = cardLoader.getCards();
        if (allCards.isEmpty()) {
            throw new RuntimeException("No cards available");
        }

        for (int attempt = 0; attempt < 1000; attempt++) {
            LinkedHashSet<Card> deck = new LinkedHashSet<>();  // Using LinkedHashSet to maintain order and prevent duplicates
            
            // 1. Add win condition (1-2)
            List<Card> winConditions = new ArrayList<>(allCards.stream()
                .filter(c -> "WinCondition".equals(c.getRole()))
                .collect(Collectors.toList()));
            if (!winConditions.isEmpty()) {
                Collections.shuffle(winConditions);
                deck.add(winConditions.get(0));
                if (Math.random() < 0.25 && winConditions.size() > 1) {
                    deck.add(winConditions.get(1));
                }
            }

            // 2. Add spells (1-2)
            List<Card> spells = new ArrayList<>(allCards.stream()
                .filter(c -> !deck.contains(c))
                .filter(c -> c.getRole().contains("Spell"))
                .collect(Collectors.toList()));
            if (!spells.isEmpty()) {
                Collections.shuffle(spells);
                deck.add(spells.get(0));
                if (Math.random() < 0.7 && spells.size() > 1) {
                    deck.add(spells.get(1));
                }
            }

            // Fill remaining slots with non-duplicate cards
            List<Card> remainingCards = new ArrayList<>(allCards.stream()
                .filter(c -> !deck.contains(c))
                .filter(c -> !"WinCondition".equals(c.getRole()))
                .collect(Collectors.toList()));

            while (deck.size() < 8 && !remainingCards.isEmpty()) {
                Collections.shuffle(remainingCards);
                Card nextCard = remainingCards.get(0);
                if (deck.add(nextCard)) {  // Will only add if not already present
                    remainingCards.remove(0);
                }
            }

            if (deck.size() == 8) {
                List<Card> finalDeck = new ArrayList<>(deck);
                
                // Debug: Check for duplicates
                Set<String> cardNames = new HashSet<>();
                List<String> duplicates = new ArrayList<>();
                for (Card card : finalDeck) {
                    if (!cardNames.add(card.getName())) {
                        duplicates.add(card.getName());
                    }
                }
                if (!duplicates.isEmpty()) {
                    logger.warn("Duplicates found in deck: {}", duplicates);
                    continue; // Try again if duplicates found
                }
                
                logger.info("Generated deck: {}", finalDeck.stream().map(Card::getName).collect(Collectors.toList()));
                
                double avgElixir = finalDeck.stream()
                    .mapToInt(Card::getElixirCost)
                    .average()
                    .orElse(0.0);
                return new Deck(finalDeck, avgElixir);
            }
        }
        throw new RuntimeException("Could not generate a valid deck");
    }

    private boolean isElixirSuitable(Card card, double currentAvg, int deckSize) {
        if (deckSize < 4) return true; // Allow any card for first few slots
        if (currentAvg < 3.0) return true; // Allow any card if average is too low
        if (currentAvg > 4.0) return card.getElixirCost() <= 4; // Prefer cheaper cards if average is high
        return card.getElixirCost() <= 6; // Generally allow cards up to 6 elixir
    }

    private Deck generateSimpleDeck(List<Card> allCards) {
        Collections.shuffle(allCards);
        List<Card> deck = allCards.subList(0, Math.min(8, allCards.size()));
        double avg = deck.stream()
            .mapToInt(Card::getElixirCost)
            .average()
            .orElse(0.0);
        return new Deck(deck, avg);
    }

    // Update custom deck generation to use similar logic
    public DeckResponse generateCustomDeck(List<Card> selectedCards) {
        if (selectedCards == null || selectedCards.isEmpty()) {
            return new DeckResponse(generateBalancedDeck().getCards(), 0.0, List.of());
        }

        List<Card> allCards = cardLoader.getCards();
        List<Card> availableCards = allCards.stream()
            .filter(c -> !selectedCards.contains(c))
            .collect(Collectors.toList());

        List<Card> deck = new ArrayList<>(selectedCards);

        while (deck.size() < 8 && !availableCards.isEmpty()) {
            Collections.shuffle(availableCards);
            double currentAvg = deck.stream()
                .mapToInt(Card::getElixirCost)
                .average()
                .orElse(0.0);

            Card nextCard = availableCards.stream()
                .filter(c -> isElixirSuitable(c, currentAvg, deck.size()))
                .findFirst()
                .orElse(availableCards.get(0));

            deck.add(nextCard);
            availableCards.remove(nextCard);
        }

        double finalAvg = deck.stream()
            .mapToInt(Card::getElixirCost)
            .average()
            .orElse(0.0);

        // Validate the complete deck, not just the selected cards
        List<String> warnings = validateDeck(deck);

        return new DeckResponse(deck, finalAvg, warnings);
    }

    private List<String> validateDeck(List<Card> cards) {
        List<String> warnings = new ArrayList<>();
        
        // Count roles
        Map<String, Long> roleCounts = cards.stream()
            .collect(Collectors.groupingBy(Card::getRole, Collectors.counting()));
        
        // Check win conditions
        long winConditions = roleCounts.getOrDefault("WinCondition", 0L);
        if (winConditions == 0) {
            warnings.add("Deck has no win condition");
        } else if (winConditions > 2) {
            warnings.add("Too many win conditions (max 2 recommended)");
        }

        // Check spells
        long spells = cards.stream()
            .filter(c -> c.getRole().contains("Spell"))
            .count();
        if (spells == 0) {
            warnings.add("Deck has no spells");
        } else if (spells > 3) {
            warnings.add("Too many spells (max 3 recommended)");
        }

        // Check buildings
        long buildings = cards.stream()
            .filter(c -> "Building".equals(c.getType()))
            .count();
        if (buildings > 2) {
            warnings.add("Too many buildings (max 2 recommended)");
        }

        // Check elixir cost
        double avgElixir = cards.stream()
            .mapToInt(Card::getElixirCost)
            .average()
            .orElse(0.0);
        if (avgElixir > 4.5) {
            warnings.add("Deck is too expensive (average elixir > 4.5)");
        } else if (avgElixir < 3.0) {
            warnings.add("Deck might be too cheap (average elixir < 3.0)");
        }

        // Check cycle cards
        long cycleCards = cards.stream()
            .filter(c -> c.getElixirCost() <= 2)
            .count();
        if (cycleCards > 4) {
            warnings.add("Too many cheap cards (max 4 cards costing 2 or less recommended)");
        }

        return warnings;
    }
}
