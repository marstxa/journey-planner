package modules;

import java.util.*;

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
        closedStations.add(station);
    }

    // method to add custom delays
    public void addDelay(String from, String to, double newTime) {
        customDelays.put(from + "-" + to, newTime);
        customDelays.put(to + "-" + from, newTime);
    }

    // return true if station is closed
    public boolean isClosed(String station) {
        return closedStations.contains(station);
    }

    public double getActualTime(String from, String to, double normalTime) {
        return customDelays.getOrDefault(from + "-" + to, normalTime);
    }
}
