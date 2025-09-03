# Clash Royale Deck Optimizer ⚔️  

A **full-stack web application** that helps Clash Royale players optimize their deck compositions using **AI-powered analysis**.  
Built with a **React frontend** and **Spring Boot backend**, featuring **OpenAI GPT-3.5-turbo integration** for intelligent deck strategy recommendations.  

---

## 🚀 Key Features  

- 🎮 **Interactive Deck Builder** – Visual card selection with drag-and-drop interface  
- 🤖 **AI Deck Analysis** – Personalized strategy feedback powered by OpenAI GPT-3.5-turbo  
- 📊 **Meta Integration** – Stay ahead with current meta trends and synergy analysis  
- ⚡ **Real-time Validation** – Instant deck composition warnings and smart suggestions  
- 🎯 **Custom Queries** – Ask AI specific questions about your deck strategy  

---

## 🛠 Tech Stack  

**Frontend:** React 18, CSS3, Responsive Design  
**Backend:** Java 17, Spring Boot 3.5.4, Maven  
**Database:** H2 (development), JPA/Hibernate  
**AI Integration:** OpenAI GPT-3.5-turbo API  
**Planned External API:** Clash Royale Official API  

---

## ✨ Features  

- Generate **random competitive decks**  
- Build **custom 8-card decks** with validation  
- AI-powered **analysis of strengths, weaknesses, and matchups**  
- **Meta-aware** strategy recommendations  
- Clean, **gaming-themed UI** with card visuals  

---

## 🎯 Who’s This For?  

Perfect for **Clash Royale players** who want to:  
- Improve their **deck-building skills**  
- Get **personalized AI insights**  
- Climb the ladder with **data-driven strategies**  

---

## 🚀 Getting Started

### Prerequisites
- Node.js 14+
- Java 17+
- Maven 3.6+
- OpenAI API Key

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/clash-royale-deck-optimizer.git
   cd clash-royale-deck-optimizer
   ```

2. **Backend Setup**
   ```bash
   cd deck-backend
   
   # Set environment variable
   export OPENAI_API_KEY="your-openai-api-key-here"
   
   # Run the backend
   mvn spring-boot:run
   ```

3. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm start
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080

## 📝 Usage

1. **Build a Deck**: Select 8 cards using the interactive card picker
2. **Generate Random Deck**: Use the random deck generator for quick builds
3. **AI Analysis**: Ask questions about your deck strategy and get personalized feedback
4. **Optimize**: Follow AI recommendations to improve your deck composition

## 🔧 Configuration

### Environment Variables
- `OPENAI_API_KEY`: Your OpenAI API key for GPT-3.5-turbo
- `CLASHROYALE_API_KEY`: (Optional) Clash Royale Official API key for enhanced meta data

### API Endpoints
- `GET /api/random-deck` - Generate random competitive deck
- `POST /api/analyze` - Analyze deck with AI feedback

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
