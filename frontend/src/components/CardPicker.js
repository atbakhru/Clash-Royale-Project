import React, { useState } from 'react';
import cardsData from '../data/cards_full.json';

function CardPicker({ onCardSelect, selectedCards, isOpen, onClose }) {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterRole, setFilterRole] = useState('all');

  const roles = [
    'All', 'WinCondition', 'Support', 'Spell', 'Defense', 'Building'
  ];

  const filteredCards = cardsData
    .filter(card => card.name.toLowerCase().includes(searchTerm.toLowerCase()))
    .filter(card => filterRole === 'all' || card.role === filterRole)
    .filter(card => !selectedCards.find(c => c.name === card.name));

  if (!isOpen) return null;

  return (
    <div className="card-picker-overlay">
      <div className="card-picker">
        <div className="picker-header">
          <h2>Select Cards</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        
        <div className="picker-filters">
          <input
            type="text"
            placeholder="Search cards..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          
          <div className="role-filters">
            {roles.map(role => (
              <button
                key={role}
                className={`role-filter ${filterRole === role.toLowerCase() ? 'active' : ''}`}
                onClick={() => setFilterRole(role.toLowerCase())}
              >
                {role}
              </button>
            ))}
          </div>
        </div>

        <div className="cards-grid">
          {filteredCards.map(card => (
            <div
              key={card.name}
              className="picker-card"
              onClick={() => {
                if (selectedCards.length < 8) {
                  onCardSelect(card);
                }
              }}
            >
              <img 
                src={`/images/cards/${card.name.toLowerCase().replace(/ /g, '_')}.png`}
                alt={card.name} 
              />
              <span className="card-name">{card.name}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default CardPicker;