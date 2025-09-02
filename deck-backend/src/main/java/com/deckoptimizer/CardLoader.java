package com.deckoptimizer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class CardLoader {
    private List<Card> cards;

    @PostConstruct
    public void loadCards() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/cards.json")) {
            if (is == null) {
                throw new IllegalStateException("cards.json not found on classpath (src/main/resources)");
            }
            cards = mapper.readValue(is, new TypeReference<List<Card>>() {});
        }
        
        // If you want to add cards manually as well, you can do it here
        // ...add other cards similarly
    }

    public List<Card> getCards() {
        return cards;
    }
}
