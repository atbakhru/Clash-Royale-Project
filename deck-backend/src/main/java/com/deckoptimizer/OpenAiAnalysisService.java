package com.deckoptimizer;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

@Service
public class OpenAiAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiAnalysisService.class);
    
    private final OpenAiService openAiService;
    
    public OpenAiAnalysisService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
    }
    
    /**
     * Analyze a deck and provide strategic feedback based on user query and RoyaleAPI data
     */
    public String analyzeDeck(List<Card> deck, String userQuery, String royaleApiData) {
        try {
            String systemPrompt = createSystemPrompt();
            String userPrompt = createUserPrompt(deck, userQuery, royaleApiData);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt));
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .maxTokens(800)
                .temperature(0.7)
                .build();
            
            String response = openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();
            
            logger.info("Generated deck analysis for user query: {}", userQuery);
            return response;
            
        } catch (Exception e) {
            logger.error("Error generating deck analysis: {}", e.getMessage());
            return "Sorry, I couldn't analyze your deck at the moment. Please try again later.";
        }
    }
    
    private String createSystemPrompt() {
        return """
            You are an expert Clash Royale strategist and deck analyst. Your role is to provide detailed, 
            actionable feedback on player decks based on:
            
            1. Current meta trends from RoyaleAPI data
            2. Card synergies and interactions
            3. Elixir efficiency and cycle potential
            4. Win condition viability
            5. Defensive capabilities
            6. Counter-play strategies
            
            Provide concise but comprehensive analysis. Focus on:
            - Strengths and weaknesses of the deck
            - Specific card interactions and synergies
            - Meta relevance and matchup considerations
            - Concrete improvement suggestions
            - Strategic gameplay tips
            
            Keep responses under 600 words and use bullet points for clarity.
            """;
    }
    
    private String createUserPrompt(List<Card> deck, String userQuery, String royaleApiData) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("**User's Deck:**\n");
        for (Card card : deck) {
            prompt.append("- ").append(card.getName())
                .append(" (").append(card.getElixirCost()).append(" elixir, ")
                .append(card.getRole()).append(", ")
                .append(card.getType()).append(")\n");
        }
        
        double avgElixir = deck.stream()
            .mapToInt(Card::getElixirCost)
            .average()
            .orElse(0.0);
        prompt.append("\n**Average Elixir Cost:** ").append(String.format("%.2f", avgElixir)).append("\n\n");
        
        prompt.append("**User's Question/Request:** ").append(userQuery).append("\n\n");
        
        if (royaleApiData != null && !royaleApiData.isEmpty()) {
            prompt.append("**Current Meta Data from RoyaleAPI:**\n")
                .append(royaleApiData).append("\n\n");
        }
        
        prompt.append("Please analyze this deck and provide strategic feedback based on the user's specific question.");
        
        return prompt.toString();
    }
}
