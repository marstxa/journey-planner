import { useEffect } from "react";
import JourneyPlanner from "./components/JourneyPlanner";
import Navbar from "./components/Navbar";
import "./styles.css";
import AOS from "aos";
import "aos/dist/aos.css";

function App() {
    useEffect(() => {
        // Initialise AOS (scroll animations)
        AOS.init({
            duration: 500,
            once: true,
            easing: "ease-in-out",
        });
    }, []);

    return (
        <div className="min-h-screen bg-base-200">
            <Navbar />

            <main className="pt-8">
                <JourneyPlanner />
            </main>
        </div>
    );
}

export default App;
