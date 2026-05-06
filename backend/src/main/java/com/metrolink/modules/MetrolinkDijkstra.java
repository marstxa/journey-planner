package modules;

import java.util.*;

public class MetrolinkDijkstra {

    // Pathfinding algorithm
    // Since i have to allow the users to decide wether they want the fastest route or the one with least changes
    // Added a new parameter for fewest changes
    public static RouteState findShortestRoute(MetrolinkGraph graph, String startStation, String endStation, boolean optimisedRoute) {

        // Dont start if the start or end is closed
        if (graph.isClosed(startStation)) {
            System.out.println("\n[Error] The start station is currently closed");
            return null;
        }

        if (graph.isClosed(endStation)) {
            System.out.println("\n [Error] THe end station is currently closed");
            return null;
        }

        PriorityQueue<RouteState> queue = new PriorityQueue<>();

        // Tracks minimum time to reach a station + line
        // allows the algorithm to visit a station twice if its on a different line or we have to backtrack
        Map<String, Double> minCost = new HashMap<>();

        // Start with - time and 0 line
        queue.add(new RouteState(startStation, null, 0.0, 0.0, 0, null));

        RouteState finalState = null;

        while (!queue.isEmpty()) {
            RouteState current = queue.poll();

            // Break condition = we found the fastest route
            if (current.station.equals(endStation)) {
                return current;
            }

            // Check neigbouring stations
            for (MetrolinkGraph.Connection conn : graph.getConnections(current.station)) {
                if (graph.isClosed(conn.destination)) {
                    continue; // changed to skip closed stations
                }

                double travelTime = graph.getActualTime(current.station, conn.destination, conn.time); // get actual time 
                int newChanges = current.changes;

                // add penalty for change, (TEST)
                if (current.line != null && !current.line.equals(conn.line)) {
                    travelTime += 2.0; //TODO: Change 
                    newChanges++;
                }

                double newActualTime = current.actualTime + travelTime;
                double newTotalCost;

                // Calculate cost to determine if its optimised or not
                if (optimisedRoute) {
                    newTotalCost = newActualTime;
                } else {
                    // break ties happening if two routes have 0 changes
                    newTotalCost = newChanges;
                }

                String stateKey = conn.destination + "_" + conn.line;

                // if this is the fastest way we found for this station-line
                if (newTotalCost < minCost.getOrDefault(stateKey, Double.MAX_VALUE)) {
                    minCost.put(stateKey, newTotalCost);
                    queue.add(new RouteState(conn.destination, conn.line, newTotalCost, newActualTime, newChanges, current));
                }
            }
        }

        return null;
    }
}
