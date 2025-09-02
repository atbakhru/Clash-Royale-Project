import React, { useState, useEffect } from 'react';

function DeckDisplay() {
    const [deck, setDeck] = useState(null);
    const [key, setKey] = useState(0);

    const handleGenerateNewDeck = async () => {
        setDeck(null);
        setKey(prevKey => prevKey + 1);
        
        try {
            const response = await fetch('http://localhost:8080/api/deck/generate');
            const newDeck = await response.json();
            console.log('Generated deck:', newDeck); // Debug logging
            setDeck(newDeck);
        } catch (error) {
            console.error('Error generating deck:', error);
        }
    };

    const handleImageError = (card) => {
        console.error(`Failed to load image for ${card.name}:`, card.imageUrl);
        // Try alternative path
        const altPath = card.imageUrl.replace('/images/', '/public/images/');
        console.log(`Trying alternative path: ${altPath}`);
    };

    const handleImageLoad = (card) => {
        console.log(`Successfully loaded image for ${card.name}:`, card.imageUrl);
    };

    return (
        <div>
            <button onClick={handleGenerateNewDeck}>Generate New Deck</button>
            <div className="deck-container">
                {deck && deck.map(card => (
                    <div key={card.name} className="card">
                        <img 
                            src={process.env.PUBLIC_URL + card.imageUrl} 
                            alt={card.name}
                            className="card-image"
                            onError={() => handleImageError(card)}
                            onLoad={() => handleImageLoad(card)}
                        />
                    </div>
                ))}
            </div>
        </div>
    );
}

export default DeckDisplay;