
import java.util.HashSet;
import java.util.Set;
import modules.GetJourney;
import modules.MetrolinkGraph;
import modules.ReadMap;

public class Main {

    public static void main(String[] args) {
        // Initialise data structures
        MetrolinkGraph graph = new MetrolinkGraph();
        Set<String> validStations = new HashSet<>(); // hold valid stations in a set

        // Load all data from CSVs 
        String csvPath = "src/utils/Metrolink_times_linecolour(in).csv";
        ReadMap.loadMapData(csvPath, graph, validStations); // load data and get a set of valid stations

        String walkPath = "src/utils/walktimes(in).csv";
        ReadMap.loadWalkData(walkPath, graph);

        if (validStations.isEmpty()) {
            System.err.println("[Error] No stations were loaded. Please check the CSV file");
            return;
        }

        // Call interface
        GetJourney.start(graph, validStations);
    }
}
