package modules;

import java.util.Map;
import java.util.Set;

/**
 * Closures and delays for a single journey lookup, kept separate from
 * MetrolinkGraph itself. The CLI is single-threaded so there's no race
 * condition here the way there was in the backend's shared Spring bean —
 * but keeping this split means MetrolinkGraph, MetrolinkDijkstra, and this
 * class behave identically whether they're driven by the CLI or the REST
 * API, which matters since both currently maintain their own copy of this
 * code.
 */
public class RouteConstraints {

    private final Set<String> closedStations;
    private final Map<String, Double> delays;

    public RouteConstraints(Set<String> closedStations, Map<String, Double> delays) {
        this.closedStations = closedStations;
        this.delays = delays;
    }

    public static RouteConstraints none() {
        return new RouteConstraints(Set.of(), Map.of());
    }

    public boolean isClosed(String station) {
        return station != null && closedStations.contains(normalize(station));
    }

    public double getActualTime(String from, String to, double normalTime) {
        if (from == null || to == null) {
            return normalTime;
        }
        String key = normalize(from) + "-" + normalize(to);
        return delays.getOrDefault(key, normalTime);
    }

    public static String normalize(String station) {
        return station.trim().toLowerCase();
    }
}