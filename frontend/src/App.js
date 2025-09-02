import { useState, useEffect } from "react";
import axios from "axios";
import "./App.css";
import cardsData from './data/cards_full.json';
import CardPicker from './components/CardPicker';
import DeckAnalyzer from './components/DeckAnalyzer';

// Placeholder image to use when card image is not found
const PLACEHOLDER_IMAGE = "/images/cards/placeholder.png";

function CardDisplay({ card, key }) {
  const [imageError, setImageError] = useState(false);
  const [imageLoaded, setImageLoaded] = useState(false);

  // Reset image states when card changes
  useEffect(() => {
    setImageError(false);
    setImageLoaded(false);
  }, [card.name]);

  // Add console log to debug rarity
  console.log('Card:', card.name, 'Rarity:', card.rarity);

  const getRarityClass = (card) => {
    // Force a default rarity if none exists
    const rarity = (card.rarity || 'Common').toLowerCase();
    console.log('Card:', card.name, 'Rarity:', rarity); // Debug log
    return rarity;
  };

  const cardClass = `card ${getRarityClass(card)}`;
  console.log('Applied class:', cardClass); // Debug log

  // Format filename from card name
  const getImagePath = (cardName) => {
    // Handle special cases
    if (cardName === "Mini P.E.K.K.A") {
      return "/images/cards/mini_pekka.png";
    }
    if (cardName === "P.E.K.K.A") {
      return "/images/cards/pekka.png";
    }
    
    return `/images/cards/${cardName.toLowerCase().replace(/[ -]/g, '_')}.png`;
  };

  // Add this function to format the role text
  const formatRole = (role) => {
    return role.replace(/([A-Z])/g, " $1").trim();
  };
  
  return (
    <div 
      className="card"
      data-rarity={(card.rarity || 'common').toLowerCase()}
      style={{ borderWidth: '3px' }} // Ensure border is visible
    >
      <div className="elixir-cost">{card.elixirCost}</div>
      <div className="card-image-container">
        <img
          // Change this line to use getImagePath instead of card.imageUrl
          src={imageError ? PLACEHOLDER_IMAGE : getImagePath(card.name)}
          onError={(e) => {
            console.error(`Failed to load image for ${card.name}`);
            setImageError(true);
          }}
          onLoad={() => {
            setImageLoaded(true);
            setImageError(false);
          }}
          alt={card.name}
          style={{ 
            width: "100%", 
            height: "100%", 
            objectFit: "contain"
          }}
        />
      </div>
      <h3 className="card-title">{card.name}</h3>
      <div className="card-meta">
        <span className="role-badge">{formatRole(card.role)}</span>
        <span className="type-badge">{card.type}</span>
      </div>
    </div>
  );
}

// Add this function before the App component
function DeckBreakdown({ cards }) {
  const roleCount = cards.reduce((acc, card) => {
    const role = card.role || 'Unknown';
    acc[role] = (acc[role] || 0) + 1;
    return acc;
  }, {});

  return (
    <div className="deck-breakdown">
      <h3>Role Breakdown</h3>
      <div className="role-counts">
        {Object.entries(roleCount).map(([role, count]) => (
          <div key={role} className="role-count">
            <span className="role-label">{role.replace(/([A-Z])/g, ' $1').trim()}</span>
            <span className="role-number">{count}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function App() {
  const [deck, setDeck] = useState(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");
  const [selectedCards, setSelectedCards] = useState([]);
  const [warnings, setWarnings] = useState([]); // Add this line
  const [mode, setMode] = useState('random');
  const [isPickerOpen, setIsPickerOpen] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [isAnalyzerOpen, setIsAnalyzerOpen] = useState(false);

  // Debug effect to track deck state changes
  useEffect(() => {
    if (deck) {
      console.log('Deck state updated:', {
        cardCount: deck.cards ? deck.cards.length : 'no cards property',
        cards: deck.cards ? deck.cards.map(c => c.name) : 'no cards',
        averageElixir: deck.averageElixir
      });
    } else {
      console.log('Deck state cleared');
    }
  }, [deck]);

  // Debug effect to track warnings state changes
  useEffect(() => {
    console.log('Warnings state updated:', warnings);
  }, [warnings]);

  const generateRandomDeck = async () => {
    if (loading) return; // Prevent multiple rapid clicks
    
    try {
      setLoading(true);
      setDeck(null); // Clear current deck to show loading state
      setWarnings([]); // Clear previous warnings
      setErr(""); // Clear any previous errors
      
      const response = await fetch('http://localhost:8080/api/generate');
      const data = await response.json();
      
      // Wait a bit before setting the new deck to allow for proper state reset
      setTimeout(() => {
        console.log('Received deck data:', data);
        console.log('Card names:', data.cards.map(c => c.name));
        console.log('Setting deck with', data.cards.length, 'cards');
        
        // Ensure we have exactly 8 cards
        if (data.cards.length !== 8) {
          console.error(`Expected 8 cards but got ${data.cards.length}`);
        }
        
        // Check for duplicates in frontend and remove them if any
        const uniqueCards = [];
        const seenNames = new Set();
        
        for (const card of data.cards) {
          if (!seenNames.has(card.name)) {
            seenNames.add(card.name);
            uniqueCards.push(card);
          } else {
            console.warn('Duplicate card found and removed:', card.name);
          }
        }
        
        if (uniqueCards.length !== data.cards.length) {
          console.warn(`Removed ${data.cards.length - uniqueCards.length} duplicate cards`);
          data.cards = uniqueCards; // Replace with deduplicated array
        }
        
        setDeck(data);
        setErr("");
        setWarnings([]); // Ensure warnings are cleared for random decks
        
        // Additional delay to allow images to load
        setTimeout(() => {
          setLoading(false);
        }, 1000);
      }, 200); // Increased delay to match custom deck
      
    } catch (error) {
      setErr("Failed to generate deck. Please try again.");
      console.error(error);
      setLoading(false);
    }
  };

  const generateCustomDeck = async () => {
    if (loading) return; // Prevent multiple rapid clicks
    
    try {
      setLoading(true);
      setDeck(null); // Clear current deck to show loading state
      setWarnings([]); // Clear previous warnings
      setErr(""); // Clear any previous errors
      
      const response = await fetch('http://localhost:8080/api/generate/custom', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ selectedCards }),
      });
      const data = await response.json();
      
      // Wait a bit before setting the new deck to allow for proper state reset
      setTimeout(() => {
        console.log('Received custom deck data:', data);
        
        // Validate the response structure
        if (!data || !data.deck || !Array.isArray(data.deck)) {
          throw new Error('Invalid response structure from server');
        }
        
        console.log('Raw deck from server has', data.deck.length, 'cards');
        
        // Check for duplicates in custom deck and remove them if any
        const uniqueCards = [];
        const seenNames = new Set();
        
        for (const card of data.deck) {
          if (!seenNames.has(card.name)) {
            seenNames.add(card.name);
            uniqueCards.push(card);
          } else {
            console.warn('Duplicate card found and removed from custom deck:', card.name);
          }
        }
        
        if (uniqueCards.length !== data.deck.length) {
          console.warn(`Removed ${data.deck.length - uniqueCards.length} duplicate cards from custom deck`);
        }
        
        // Ensure we have exactly 8 cards
        if (uniqueCards.length !== 8) {
          console.error(`Expected 8 cards but got ${uniqueCards.length}`);
        }
        
        // Create a deck object with the same structure as random deck
        const deckObject = {
          cards: uniqueCards,
          averageElixir: data.averageElixir || 0
        };
        console.log('Setting deck with', uniqueCards.length, 'cards');
        
        // Set deck and warnings in the correct order
        setDeck(deckObject);
        if (data.warnings && data.warnings.length > 0) {
          console.log('Setting warnings:', data.warnings);
          setWarnings(data.warnings);
        } else {
          setWarnings([]);
        }
        
        // Additional delay to allow images to load
        setTimeout(() => {
          setLoading(false);
        }, 1000);
      }, 200); // Increased delay to ensure proper state reset
      
    } catch (error) {
      setErr("Failed to generate custom deck. Please try again.");
      console.error(error);
      setLoading(false);
    }
  };

  const handleCardSelect = (card) => {
    if (selectedCards.length < 8) {
      setSelectedCards([...selectedCards, card]);
    }
    if (selectedCards.length === 7) {
      setIsPickerOpen(false);
    }
  };

  const generateDeck = async () => {
    if (isGenerating) return; // Prevent multiple rapid clicks
    
    setIsGenerating(true);
    
    try {
      const response = await fetch('http://localhost:8080/api/deck/generate');
      const data = await response.json();
      setDeck(data.cards);
      
      // Small delay to allow images to load
      setTimeout(() => {
        setIsGenerating(false);
      }, 500);
    } catch (error) {
      console.error('Error generating deck:', error);
      setIsGenerating(false);
    }
  };

  return (
    <div className="page">
      <header className="header">
        <h1>Clash Royale Deck Optimizer</h1>
        <p className="sub">Choose your deck generation method</p>
        <div className="generation-modes">
          <button 
            className={`mode-btn ${mode === 'random' ? 'active' : ''}`}
            onClick={() => setMode('random')}
          >
            Random Deck
          </button>
          <button 
            className={`mode-btn ${mode === 'custom' ? 'active' : ''}`}
            onClick={() => setMode('custom')}
          >
            Custom Deck
          </button>
        </div>
        {mode === 'random' ? (
          <button 
            className="btn generate-btn" 
            onClick={generateRandomDeck}
            disabled={loading}
          >
            {loading ? "Generating..." : "Generate Random Deck"}
          </button>
        ) : (
          <div className="custom-deck-controls">
            <div className="selected-cards">
              {selectedCards.map((card, index) => (
                <div key={index} className="selected-card-pill">
                  {card.name}
                  <button 
                    className="remove-card"
                    onClick={() => setSelectedCards(cards => 
                      cards.filter(c => c.name !== card.name)
                    )}
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
            <button 
              className="btn add-card-btn"
              onClick={() => setIsPickerOpen(true)}
              disabled={selectedCards.length >= 8}
            >
              Add Card
            </button>
            <button 
              className="btn generate-btn"
              onClick={() => generateCustomDeck(selectedCards)}
              disabled={selectedCards.length === 0}
            >
              Complete My Deck
            </button>
          </div>
        )}
        {err && <div className="error">{err}</div>}
        {warnings.length > 0 && (
          <div className="deck-warnings">
            <h3>Deck Warnings:</h3>
            <ul>
              {warnings.map((warning, index) => (
                <li key={index}>{warning}</li>
              ))}
            </ul>
          </div>
        )}
      </header>

      <CardPicker
        isOpen={isPickerOpen}
        onClose={() => setIsPickerOpen(false)}
        onCardSelect={handleCardSelect}
        selectedCards={selectedCards}
      />

      {loading && (
        <section className="deck">
          <h2>Generating Deck...</h2>
          <div className="loading-spinner">🗘 Loading...</div>
        </section>
      )}

      {deck && deck.cards && !loading && (
        <section className="deck">
          <h2>Your Deck</h2>
          <ul className="card-list">
            {deck.cards.map((c, i) => (
              <li key={`${c.name}-${i}`}>
                <CardDisplay card={c} />
              </li>
            ))}
          </ul>
          <div className="deck-stats">
            <div className="avg">
              <div className="avg-content">
                <span className="avg-label">Average Elixir</span>
                <span className="avg-value">
                  {deck.averageElixir ? deck.averageElixir.toFixed(2) : "—"}
                </span>
              </div>
            </div>
            <DeckBreakdown cards={deck.cards} />
          </div>
          <div className="deck-actions">
            <button 
              className="btn analyze-deck-btn"
              onClick={() => setIsAnalyzerOpen(true)}
            >
              🤖 Analyze with AI
            </button>
          </div>
        </section>
      )}

      <DeckAnalyzer 
        deck={deck}
        isOpen={isAnalyzerOpen}
        onClose={() => setIsAnalyzerOpen(false)}
      />
    </div>
  );
}
