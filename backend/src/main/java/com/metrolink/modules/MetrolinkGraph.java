package modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Metrolink network's topology: which stations connect to which, on
 * which line, and how long each leg takes.
 *
 * This is populated once at startup by ReadMap and never mutated again —
 * it holds no per-request state (closed stations, delays). That used to
 * live here, but a Spring-managed instance of this class is shared across
 * every concurrent request, so mutating it per-request was a race
 * condition. Per-request state now lives in RouteConstraints instead,
 * built fresh for each call and passed into MetrolinkDijkstra directly.
 */
public class MetrolinkGraph {

    public static class Connection {
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
}