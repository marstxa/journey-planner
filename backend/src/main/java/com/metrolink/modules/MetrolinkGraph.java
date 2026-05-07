package modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetrolinkGraph {
    // core data structure

    public static class Connection {

        // constants
        public final String destination;
        public final String line;
        public final double time;

        public Connection(String destination, String line, double time) {
            this.destination = destination;
            this.line = line;
            this.time = time;
        }
    }

    // Graph that maps a station name to a list of connections
    private final Map<String, List<Connection>> network = new HashMap<>();

    // list of custom closed stations and custom delays
    private final Set<String> closedStations = new HashSet<>();
    private final Map<String, Double> customDelays = new HashMap<>();

    public void addConnection(String from, String to, String line, double time) {
        network.putIfAbsent(from, new ArrayList<>());
        network.putIfAbsent(to, new ArrayList<>());

        // Add two way connection
        network.get(from).add(new Connection(to, line, time));
        network.get(to).add(new Connection(from, line, time));
    }

    public List<Connection> getConnections(String station) {
        return network.getOrDefault(station, new ArrayList<>());
    }

    // method to close stations
    public void closeStation(String station) {
        if (station != null && !station.isEmpty()) {
            closedStations.add(station.trim().toLowerCase());
        }
    }

    // Method to open a station after the search is done
    public void openStation(String station) {
        if (station != null && !station.isEmpty()) {
            closedStations.remove(station.trim().toLowerCase());
        }
    }

    // method to add custom delays
    public void addDelay(String from, String to, double newTime) {
        if (from != null && to != null) {
            String stationA = from.trim().toLowerCase();
            String stationB = to.trim().toLowerCase();

            customDelays.put(stationA + "-" + stationB, newTime);
            customDelays.put(stationB + "-" + stationA, newTime);
        }
    }

    // Method to remove a delay after the search is done
    public void removeDelay(String from, String to) {
        if (from != null && to != null) {
            String stationA = from.trim().toLowerCase();
            String stationB = to.trim().toLowerCase();
            customDelays.remove(stationA + "-" + stationB);
            customDelays.remove(stationB + "-" + stationA);
        }
    }

    // return true if station is closed
    public boolean isClosed(String station) {
        if (station == null) {
            return false;
        }
        return closedStations.contains(station.trim().toLowerCase());
    }

    public double getActualTime(String from, String to, double normalTime) {
        if (from == null || to == null) {
            return normalTime;
        }

        String key = from.trim().toLowerCase() + "-" + to.trim().toLowerCase();
        double extraDelay = customDelays.getOrDefault(key, 0.0);
        return normalTime + extraDelay;
    }
}
