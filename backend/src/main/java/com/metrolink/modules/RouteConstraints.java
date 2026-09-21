package modules;

import java.util.Map;
import java.util.Set;

/**
 * Per-request routing constraints: which stations are closed and what
 * custom delays apply, for a single findShortestRoute() call.
 *
 * This exists so a request can express "treat Victoria as closed" without
 * mutating the shared MetrolinkGraph bean. Previously, closures and delays
 * were written directly onto the graph and then removed again once the
 * route was found; since the graph is a single Spring-managed instance
 * shared across every request, two requests in flight at the same time
 * could interleave their mutations — one request's cleanup could remove a
 * closure another request still depended on. Building this object fresh
 * per request instead means there's no shared mutable state to race on.
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