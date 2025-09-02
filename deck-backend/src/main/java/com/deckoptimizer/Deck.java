package com.deckoptimizer;

import java.util.List;

public class Deck {
    private List<Card> cards;
    private double averageElixir;

    public Deck(List<Card> cards, double averageElixir) {
        this.cards = cards;
        this.averageElixir = averageElixir;
    }

    public List<Card> getCards() {
        return cards;
    }

    public double getAverageElixir() {
        return averageElixir;
    }
}
