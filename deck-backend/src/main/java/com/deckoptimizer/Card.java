package com.deckoptimizer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    private String name;
    private String type;
    private int elixirCost;
    private String role;
    private String rarity;
    private String imageUrl;

    // Default constructor required by JPA
    public Card() {
    }

    // Constructor with all fields
    public Card(String name, String type, int elixirCost, String role, String rarity, String imageUrl) {
        this.name = name;
        this.type = type;
        this.elixirCost = elixirCost;
        this.role = role;
        this.rarity = rarity;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getElixirCost() { return elixirCost; }
    public void setElixirCost(int elixirCost) { this.elixirCost = elixirCost; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
