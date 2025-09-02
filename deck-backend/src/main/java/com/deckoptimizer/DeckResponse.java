package com.deckoptimizer;

import java.util.List;

public class DeckResponse {
    private final List<Card> deck;
    private final double averageElixir;
    private final List<String> warnings;

    public DeckResponse(List<Card> deck, double averageElixir, List<String> warnings) {
        this.deck = deck;
        this.averageElixir = averageElixir;
        this.warnings = warnings;
    }

    // Add getters
    public List<Card> getDeck() {
        return deck;
    }

    public double getAverageElixir() {
        return averageElixir;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}