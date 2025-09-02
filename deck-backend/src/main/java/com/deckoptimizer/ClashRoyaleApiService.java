package com.deckoptimizer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ClashRoyaleApiService {
    private static final Logger logger = LoggerFactory.getLogger(ClashRoyaleApiService.class);
    
    private final WebClient webClient;
    
    @Value("${clashroyale.api.key}")
    private String apiKey;
    
    private final String baseUrl = "https://api.clashroyale.com/v1";
    
    public ClashRoyaleApiService() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }
    
    /**
     * Get all available cards from Clash Royale Official API
     */
    public Mono<JsonNode> getCards() {
        return webClient.get()
            .uri(baseUrl + "/cards?limit=300")
            .header("Authorization", "Bearer " + apiKey)
            .header("Accept", "application/json")
            .retrieve()
            .bodyToMono(JsonNode.class)
            .doOnNext(response -> logger.info("Received cards data from Clash Royale API"))
            .doOnError(error -> logger.error("Error fetching cards: {}", error.getMessage()))
            .onErrorReturn(createFallbackCardsData());
    }
    
    /**
     * Get current season information (contains meta insights)
     */
    public Mono<JsonNode> getCurrentSeason() {
        return webClient.get()
            .uri(baseUrl + "/locations/global/seasons/current")
            .header("Authorization", "Bearer " + apiKey)
            .header("Accept", "application/json")
            .retrieve()
            .bodyToMono(JsonNode.class)
            .doOnNext(response -> logger.info("Received current season data"))
            .doOnError(error -> logger.error("Error fetching season data: {}", error.getMessage()))
            .onErrorReturn(createFallbackSeasonData());
    }
    
    /**
     * Get global tournament rankings (for meta analysis)
     */
    public Mono<JsonNode> getGlobalTournaments() {
        return webClient.get()
            .uri(baseUrl + "/globaltournaments")
            .header("Authorization", "Bearer " + apiKey)
            .header("Accept", "application/json")
            .retrieve()
            .bodyToMono(JsonNode.class)
            .doOnNext(response -> logger.info("Received global tournaments data"))
            .doOnError(error -> logger.error("Error fetching tournaments: {}", error.getMessage()))
            .onErrorReturn(createFallbackTournamentData());
    }
    
    /**
     * Create simplified meta analysis data for AI using real API data
     */
    public String createMetaAnalysisData() {
        try {
            // Try to get real data from API
            JsonNode cardsResponse = getCards().block();
            
            if (cardsResponse != null && cardsResponse.has("items")) {
                StringBuilder metaInfo = new StringBuilder();
                metaInfo.append("Current Meta Information from Clash Royale API:\n\n");
                
                JsonNode items = cardsResponse.get("items");
                metaInfo.append("Total available cards: ").append(items.size()).append("\n\n");
                
                // Analyze cards by elixir cost
                int[] elixirDistribution = new int[11]; // 0-10 elixir
                
                for (JsonNode card : items) {
                    if (card.has("elixirCost")) {
                        int cost = card.get("elixirCost").asInt();
                        elixirDistribution[cost]++;
                    }
                }
                
                metaInfo.append("Card Distribution by Elixir Cost:\n");
                for (int i = 1; i <= 10; i++) {
                    if (elixirDistribution[i] > 0) {
                        metaInfo.append("- ").append(i).append(" elixir: ").append(elixirDistribution[i]).append(" cards\n");
                    }
                }
                
                metaInfo.append("\nMeta Insights:\n");
                metaInfo.append("- Balanced elixir curve is crucial for competitive play\n");
                metaInfo.append("- Most successful decks have 2.6-4.2 average elixir cost\n");
                metaInfo.append("- Spell cards are essential for clearing swarms and finishing towers\n");
                metaInfo.append("- Win conditions should be supported by defensive and cycle cards\n");
                
                // Add current meta trends
                metaInfo.append("\nCurrent Meta Trends (September 2025):\n");
                metaInfo.append("- Fast cycle decks remain popular (Hog 2.6, Miner cycle)\n");
                metaInfo.append("- Beatdown with heavy tanks is strong in certain metas\n");
                metaInfo.append("- Bridge spam and dual-lane pressure strategies\n");
                metaInfo.append("- Control decks focusing on defensive value\n");
                
                return metaInfo.toString();
            } else {
                return getFallbackMetaData();
            }
            
        } catch (Exception e) {
            logger.warn("Could not fetch live meta data, using fallback: {}", e.getMessage());
            return getFallbackMetaData();
        }
    }
    
    /**
     * Get detailed card information by name from API
     */
    public JsonNode getCardByName(String cardName) {
        try {
            JsonNode cardsResponse = getCards().block();
            
            if (cardsResponse != null && cardsResponse.has("items")) {
                JsonNode items = cardsResponse.get("items");
                
                for (JsonNode card : items) {
                    if (card.has("name") && 
                        card.get("name").asText().equalsIgnoreCase(cardName)) {
                        return card;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not fetch card data for {}: {}", cardName, e.getMessage());
        }
        return null;
    }
    
    /**
     * Get card statistics for deck analysis
     */
    public String getCardStats(String cardName) {
        JsonNode card = getCardByName(cardName);
        if (card != null) {
            StringBuilder stats = new StringBuilder();
            stats.append(card.get("name").asText()).append(": ");
            stats.append(card.get("elixirCost").asInt()).append(" elixir, ");
            stats.append(card.get("rarity").asText()).append(" rarity");
            return stats.toString();
        }
        return cardName + ": Stats unavailable";
    }

    private String getFallbackMetaData() {
        return """
            Current Meta Information (September 2025):
            
            Popular Archetypes:
            - Hog Cycle: Fast cycle with cheap cards (2.6-3.0 avg elixir)
            - Beatdown: Heavy tanks with support (4.0+ avg elixir)
            - Control: Defensive focus with counter-push potential
            - Bridge Spam: Quick pressure with dual lane potential
            
            Strong Cards in Meta:
            - Win Conditions: Hog Rider, Giant, Balloon, Miner, Goblin Barrel
            - Spells: Fireball, The Log, Arrows (essential for clearing swarms)
            - Defense: Tesla, Cannon, Inferno Tower (anti-tank)
            - Support: Musketeer, Archers, Ice Spirit (versatile)
            
            Key Strategies:
            - Elixir efficiency is crucial
            - Having spell coverage is essential
            - Balance between offense and defense
            - Cycle speed affects playstyle significantly
            """;
    }
    
    private JsonNode createFallbackCardsData() {
        // Return a simple fallback structure
        return null; // We'll handle this in the service
    }
    
    private JsonNode createFallbackSeasonData() {
        return null;
    }
    
    private JsonNode createFallbackTournamentData() {
        return null;
    }
}
