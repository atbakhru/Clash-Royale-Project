package com.yourpackage.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

public class CardTest {
    
    @Test
    public void testCardsJsonLoading() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Card> cards = Arrays.asList(mapper.readValue(
                getClass().getResourceAsStream("/cards.json"), 
                Card[].class));
            assertNotNull(cards);
            assertFalse(cards.isEmpty());
        } catch (IOException e) {
            fail("Failed to parse cards.json: " + e.getMessage());
        }
    }
}