import React, { useState } from 'react';

function CardDisplay({ card }) {
  const [imageError, setImageError] = useState(false);

  return (
    <div className="card" data-rarity={card.rarity.toLowerCase()}>
      <div className="elixir-cost">{card.elixirCost}</div>
      <div className="card-image-container">
        <img
          src={imageError ? "/images/cards/placeholder.png" : card.imageUrl}
          onError={(e) => {
            console.log(`Failed to load image for ${card.name}`);
            setImageError(true);
          }}
          alt={card.name}
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

function formatRole(role) {
  return role.replace(/([A-Z])/g, ' $1').trim();
}

export default CardDisplay;