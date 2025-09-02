import React, { useState } from 'react';
import './DeckAnalyzer.css';

const DeckAnalyzer = ({ deck, isOpen, onClose }) => {
  const [query, setQuery] = useState('');
  const [analysis, setAnalysis] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const analyzeDecks = async () => {
    if (!query.trim()) {
      setError('Please enter a question about your deck');
      return;
    }

    setLoading(true);
    setError('');
    setAnalysis('');

    try {
      const response = await fetch('http://localhost:8080/api/analyze', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          deck: deck.cards,
          query: query
        }),
      });

      const data = await response.json();

      if (data.status === 'success') {
        setAnalysis(data.analysis);
      } else {
        setError(data.message || 'Failed to analyze deck');
      }
    } catch (err) {
      setError('Failed to connect to analysis service');
      console.error('Analysis error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Format analysis text with proper styling
  const formatAnalysisText = (text) => {
    if (!text) return null;

    const lines = text.split('\n').filter(line => line.trim());
    const formattedContent = [];
    let currentSection = [];

    lines.forEach((line, index) => {
      const trimmedLine = line.trim();
      
      // Check if it's a section header (starts with **)
      if (trimmedLine.startsWith('**') && trimmedLine.endsWith('**')) {
        // Add previous section if exists
        if (currentSection.length > 0) {
          formattedContent.push(
            <div key={`section-${formattedContent.length}`} className="analysis-section">
              {currentSection}
            </div>
          );
          currentSection = [];
        }
        
        // Add section title
        const title = trimmedLine.replace(/\*\*/g, '');
        formattedContent.push(
          <h4 key={`title-${index}`} className="analysis-section-title">
            {title}
          </h4>
        );
      }
      // Check if it's a bullet point
      else if (trimmedLine.startsWith('- **') || trimmedLine.startsWith('• **')) {
        const content = trimmedLine.replace(/^[•-]\s*/, '').replace(/\*\*/g, '');
        currentSection.push(
          <div key={`item-${index}`} className="analysis-list-item">
            {formatInlineText(content)}
          </div>
        );
      }
      // Regular bullet points
      else if (trimmedLine.startsWith('- ') || trimmedLine.startsWith('• ')) {
        const content = trimmedLine.replace(/^[•-]\s*/, '');
        currentSection.push(
          <div key={`item-${index}`} className="analysis-list-item">
            {formatInlineText(content)}
          </div>
        );
      }
      // Regular paragraphs
      else if (trimmedLine.length > 0) {
        currentSection.push(
          <p key={`para-${index}`}>
            {formatInlineText(trimmedLine)}
          </p>
        );
      }
    });

    // Add final section
    if (currentSection.length > 0) {
      formattedContent.push(
        <div key={`section-final`} className="analysis-section">
          {currentSection}
        </div>
      );
    }

    return formattedContent;
  };

  // Format inline text with bold and emphasis
  const formatInlineText = (text) => {
    if (!text) return '';
    
    // Replace **bold** with styled spans
    return text.split(/(\*\*[^*]+\*\*)/).map((part, index) => {
      if (part.startsWith('**') && part.endsWith('**')) {
        const content = part.replace(/\*\*/g, '');
        return (
          <span key={index} className="analysis-emphasis">
            {content}
          </span>
        );
      }
      return part;
    });
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      analyzeDecks();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="deck-analyzer-overlay">
      <div className="deck-analyzer-modal">
        <div className="analyzer-header">
          <h2>🤖 AI Deck Analysis</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        <div className="analyzer-content">
          <div className="current-deck-summary">
            <h3>Your Current Deck:</h3>
            <div className="deck-cards">
              {deck?.cards?.map((card, index) => (
                <span key={index} className="card-pill">
                  {card.name} ({card.elixirCost})
                </span>
              ))}
            </div>
            <p className="avg-elixir">
              Average Elixir: {deck?.averageElixir?.toFixed(2) || 'N/A'}
            </p>
          </div>

          <div className="query-section">
            <h3>What would you like to know about your deck?</h3>
            <textarea
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Examples:
• How good is this deck in the current meta?
• What are the weaknesses of this deck?
• How should I play this deck against beatdown?
• What cards should I replace to improve win rate?
• Is this deck good for ladder climbing?"
              rows={4}
              className="query-input"
            />
            <button 
              onClick={analyzeDecks}
              disabled={loading || !query.trim()}
              className="analyze-btn"
            >
              {loading ? '🔍 Analyzing...' : '🚀 Analyze Deck'}
            </button>
          </div>

          {error && (
            <div className="error-message">
              ⚠️ {error}
            </div>
          )}

          {analysis && (
            <div className="analysis-result">
              <h3>📊 Analysis Results:</h3>
              <div className="analysis-content">
                {formatAnalysisText(analysis)}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DeckAnalyzer;
