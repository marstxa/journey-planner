import JourneyPlanner from "./components/JourneyPlanner";
import Navbar from "./components/Navbar";
import "./styles.css"

function App(){
  return (
    <div className="min-h-screen bg-base-200">
      <Navbar/>

      <main className="pt-8">
        <JourneyPlanner/>
      </main>
    </div>
  )
}

export default App;