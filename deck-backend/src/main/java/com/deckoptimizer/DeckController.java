package com.deckoptimizer;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
@CrossOrigin("http://localhost:3000")
public class DeckController {
    private final DeckService service;
    private final ClashRoyaleApiService clashRoyaleApiService;
    private final OpenAiAnalysisService openAiAnalysisService;

    public DeckController(DeckService service, ClashRoyaleApiService clashRoyaleApiService, OpenAiAnalysisService openAiAnalysisService) {
        this.service = service;
        this.clashRoyaleApiService = clashRoyaleApiService;
        this.openAiAnalysisService = openAiAnalysisService;
    }

    @GetMapping("/generate")
    public ResponseEntity<Deck> getDeck() {
        Deck deck = service.generateBalancedDeck();
        // Add cache control headers to prevent image caching issues
        return ResponseEntity
            .ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .body(deck);
    }

    @PostMapping("/generate/custom")
    public ResponseEntity<DeckResponse> getCustomDeck(@RequestBody Map<String, List<Card>> request) {
        List<Card> selectedCards = request.get("selectedCards");
        DeckResponse response = service.generateCustomDeck(selectedCards);
        return ResponseEntity
            .ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .body(response);
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeDeck(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deckData = (List<Map<String, Object>>) request.get("deck");
            String userQuery = (String) request.get("query");
            
            // Convert JSON deck data to Card objects
            List<Card> deck = new ArrayList<>();
            for (Map<String, Object> cardData : deckData) {
                Card card = new Card();
                card.setName((String) cardData.get("name"));
                card.setElixirCost(((Number) cardData.get("elixirCost")).intValue());
                card.setType((String) cardData.get("type"));
                card.setRole((String) cardData.get("role"));
                card.setImageUrl((String) cardData.get("imageUrl"));
                deck.add(card);
            }
            
            // Get meta data from Clash Royale Official API
            String metaData = clashRoyaleApiService.createMetaAnalysisData();
            
            // Generate AI analysis
            String analysis = openAiAnalysisService.analyzeDeck(deck, userQuery, metaData);
            
            Map<String, String> response = Map.of(
                "analysis", analysis,
                "status", "success"
            );
            
            return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(response);
                
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                "analysis", "Sorry, I couldn't analyze your deck. Please try again.",
                "status", "error",
                "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
