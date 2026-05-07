# Metrolink Journey Planner (Web & CLI)

## Core Features
* **Custom Dijkstra Algorithm:** Calculates the fastest route and the route with the fewest line changes.
* **Dynamic Network Disruptions:** Intelligently recalculates routes around closed stations or delayed tracks.
* **Dual Interfaces:** Fully functional CLI for terminal use, and a modern React web interface.
* **Dual-Fetch Analytics (Web):** Instantly compares "Fastest Time" vs "Fewest Changes" routes using `Promise.all`.
* **Interactive Data Visualizations (Web):** Custom Chart.js Line and Bar graphs for journey analysis.

## Tech Stack
* **Core Algorithm:** Java 17+, Graph Theory (Dijkstra)
* **Backend API:** Spring Boot, RESTful Architecture
* **Frontend UI:** React (TypeScript), Vite, Tailwind CSS, DaisyUI, Chart.js, Axios

## How to run
git clone https://github.com/marstxa/uni-journey-planner

### CLI (run inside /cli folder)
javac -d bin src/Main.java src/modules/*.java
java -cp bin Main

### BACKEND (run inside /backend folder)
./mvnw clean spring-boot:run

### FRONTEND (run inside /backend folder, must have npm and nodejs installed)
npm install
npm run dev
