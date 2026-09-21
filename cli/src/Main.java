import java.util.HashSet;
import java.util.Set;

import modules.GetJourney;
import modules.MetrolinkGraph;
import modules.ReadMap;

public class Main {

    public static void main(String[] args) {
        MetrolinkGraph graph = new MetrolinkGraph();
        Set<String> validStations = new HashSet<>();

        String csvPath = "src/utils/Metrolink_times_linecolour(in).csv";
        ReadMap.loadMapData(csvPath, graph, validStations);

        String walkPath = "src/utils/walktimes(in).csv";
        ReadMap.loadWalkData(walkPath, graph);

        if (validStations.isEmpty()) {
            System.err.println("[Error] No stations were loaded. Please check the CSV file");
            return;
        }

        GetJourney.start(graph, validStations);
    }
}