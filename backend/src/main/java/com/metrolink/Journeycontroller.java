package com.metrolink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import modules.MetrolinkDijkstra;
import modules.MetrolinkGraph;
import modules.RouteConstraints;
import modules.RouteState;

@RestController
@RequestMapping("/api/journey")
@CrossOrigin(origins = "http://localhost:5173") // allows the Vite dev server to call this API
public class JourneyController {

    private final MetrolinkGraph graph;

    public JourneyController(MetrolinkGraph graph) {
        this.graph = graph;
    }

    // GET /api/journey/route
    @GetMapping("/route")
    public ResponseEntity<?> getRoute(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam boolean optimisedRoute,
            @RequestParam(required = false, defaultValue = "") String station,
            @RequestParam(required = false, defaultValue = "") String delayFrom,
            @RequestParam(required = false, defaultValue = "") String delayTo,
            @RequestParam(required = false, defaultValue = "0") int delayTime) {

        if (start.equalsIgnoreCase(end)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Start and destination stations can't be the same."));
        }

        // Build this request's constraints locally rather than mutating the
        // shared graph bean — see RouteConstraints for why.
        Set<String> closedStations = new HashSet<>();
        if (!station.isEmpty()) {
            closedStations.add(RouteConstraints.normalize(station));
        }

        Map<String, Double> delays = new HashMap<>();
        if (!delayFrom.isEmpty() && !delayTo.isEmpty() && delayTime > 0) {
            String from = RouteConstraints.normalize(delayFrom);
            String to = RouteConstraints.normalize(delayTo);
            delays.put(from + "-" + to, (double) delayTime);
            delays.put(to + "-" + from, (double) delayTime);
        }

        RouteConstraints constraints = new RouteConstraints(closedStations, delays);

        if (constraints.isClosed(start)) {
            return ResponseEntity.badRequest().body(Map.of("error", start + " is currently closed."));
        }
        if (constraints.isClosed(end)) {
            return ResponseEntity.badRequest().body(Map.of("error", end + " is currently closed."));
        }

        RouteState finalState = MetrolinkDijkstra.findShortestRoute(graph, start, end, optimisedRoute, constraints);

        if (finalState == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No route could be found."));
        }

        List<Map<String, Object>> pathArray = new ArrayList<>();
        RouteState curr = finalState;
        while (curr != null) {
            Map<String, Object> step = new HashMap<>();
            step.put("station", curr.station);
            step.put("line", curr.line == null ? "start" : curr.line);
            step.put("actualTime", curr.actualTime);
            pathArray.add(step);
            curr = curr.previous;
        }
        Collections.reverse(pathArray);

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("totalTime", finalState.actualTime);
        jsonResponse.put("totalChanges", finalState.changes);
        jsonResponse.put("path", pathArray);

        return ResponseEntity.ok(jsonResponse);
    }
}