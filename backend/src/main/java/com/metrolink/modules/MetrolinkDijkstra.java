package modules;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class MetrolinkDijkstra {

    // The per-line-change time penalty added when a route switches lines.
    // Encourages the algorithm to prefer routes with fewer changes when
    // times are otherwise close, without a full second cost function.
    private static final double LINE_CHANGE_PENALTY_MINUTES = 2.0;

    // Since users can choose between the fastest route or the one with the
    // fewest changes, optimisedRoute selects which cost function to use.
    public static RouteState findShortestRoute(
            MetrolinkGraph graph,
            String startStation,
            String endStation,
            boolean optimisedRoute,
            RouteConstraints constraints) {

        if (constraints.isClosed(startStation)) {
            return null;
        }
        if (constraints.isClosed(endStation)) {
            return null;
        }

        PriorityQueue<RouteState> queue = new PriorityQueue<>();

        // Tracks the minimum cost to reach a station on a given line, so the
        // algorithm can revisit a station if arriving via a different line
        // (or after backtracking) turns out cheaper.
        Map<String, Double> minCost = new HashMap<>();

        queue.add(new RouteState(startStation, null, 0.0, 0.0, 0, null));

        while (!queue.isEmpty()) {
            RouteState current = queue.poll();

            if (current.station.equals(endStation)) {
                return current;
            }

            for (MetrolinkGraph.Connection conn : graph.getConnections(current.station)) {
                if (constraints.isClosed(conn.destination)) {
                    continue;
                }

                double travelTime = constraints.getActualTime(current.station, conn.destination, conn.time);
                int newChanges = current.changes;

                boolean isLineChange = current.line != null && !current.line.equals(conn.line);
                if (isLineChange) {
                    travelTime += LINE_CHANGE_PENALTY_MINUTES;
                    newChanges++;
                }

                double newActualTime = current.actualTime + travelTime;
                double newTotalCost = optimisedRoute ? newActualTime : newChanges;

                String stateKey = conn.destination + "_" + conn.line;

                // Only enqueue this station-line pair again if we've found a
                // cheaper way to reach it than any previous route did.
                if (newTotalCost < minCost.getOrDefault(stateKey, Double.MAX_VALUE)) {
                    minCost.put(stateKey, newTotalCost);
                    queue.add(new RouteState(conn.destination, conn.line, newTotalCost, newActualTime, newChanges, current));
                }
            }
        }

        return null;
    }
}