package modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetrolinkDijkstraTest {

    // Test topology:
    //   A --red(3min)-- B --blue(3min)-- C   (needs a line change, 6min raw + 2min penalty = 8min)
    //   A --green(10min)-- D --green(10min)-- C   (no change needed, 20min)
    // E is left isolated (no connections) to test the "no route" case.
    private MetrolinkGraph graph;

    @BeforeEach
    void setUp() {
        graph = new MetrolinkGraph();
        graph.addConnection("A", "B", "red", 3.0);
        graph.addConnection("B", "C", "blue", 3.0);
        graph.addConnection("A", "D", "green", 10.0);
        graph.addConnection("D", "C", "green", 10.0);
    }

    @Test
    void fastestRoutePicksLowerTimeEvenWithALineChange() {
        RouteState result = MetrolinkDijkstra.findShortestRoute(graph, "A", "C", true, RouteConstraints.none());

        assertEquals(8.0, result.actualTime, 0.0001);
        assertEquals(1, result.changes);
    }

    @Test
    void fewestChangesRoutePicksZeroChangesEvenWhenSlower() {
        RouteState result = MetrolinkDijkstra.findShortestRoute(graph, "A", "C", false, RouteConstraints.none());

        assertEquals(0, result.changes);
        assertEquals(20.0, result.actualTime, 0.0001);
    }

    @Test
    void closingAnIntermediateStationForcesTheAlternateRoute() {
        Set<String> closed = Set.of("b");
        RouteConstraints constraints = new RouteConstraints(closed, Map.of());

        RouteState result = MetrolinkDijkstra.findShortestRoute(graph, "A", "C", true, constraints);

        // With B closed, the only remaining path is via D
        assertEquals(20.0, result.actualTime, 0.0001);
        assertEquals(0, result.changes);
    }

    @Test
    void closingTheStartStationReturnsNull() {
        RouteConstraints constraints = new RouteConstraints(Set.of("a"), Map.of());
        assertNull(MetrolinkDijkstra.findShortestRoute(graph, "A", "C", true, constraints));
    }

    @Test
    void closingTheEndStationReturnsNull() {
        RouteConstraints constraints = new RouteConstraints(Set.of("c"), Map.of());
        assertNull(MetrolinkDijkstra.findShortestRoute(graph, "A", "C", true, constraints));
    }

    @Test
    void delayOnAConnectionMakesTheAlternateRouteFaster() {
        Map<String, Double> delays = new HashMap<>();
        delays.put("a-b", 50.0);
        delays.put("b-a", 50.0);
        RouteConstraints constraints = new RouteConstraints(new HashSet<>(), delays);

        RouteState result = MetrolinkDijkstra.findShortestRoute(graph, "A", "C", true, constraints);

        // A-B now costs 50min instead of 3, so the via-D route (20min) wins
        assertEquals(20.0, result.actualTime, 0.0001);
    }

    @Test
    void returnsNullWhenNoRouteExists() {
        graph.addConnection("E", "E", "isolated", 0.0); // registers E with no real connections elsewhere
        RouteState result = MetrolinkDijkstra.findShortestRoute(graph, "A", "E", true, RouteConstraints.none());
        assertNull(result);
    }

    @Test
    void startEqualsEndReturnsATrivialZeroCostRoute() {
        RouteState result = MetrolinkDijkstra.findShortestRoute(graph, "A", "A", true, RouteConstraints.none());

        assertEquals(0.0, result.actualTime, 0.0001);
        assertEquals(0, result.changes);
    }
}