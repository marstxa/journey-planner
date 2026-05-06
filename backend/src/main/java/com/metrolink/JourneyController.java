package com.metrolink.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import modules.MetrolinkDijkstra;
import modules.MetrolinkGraph;
import modules.RouteState;

@RestController
@RequestMapping("/api/journey")
@CrossOrigin(origins = "http://localhost:5173") // TODO: Allows Vite/React to talk to Java without being blocked by security
public class JourneyController {

    private final MetrolinkGraph graph;

    public JourneyController(MetrolinkGraph graph) {
        this.graph = graph;
    }

    // creates an endpoint at: http://localhost:8080/api/journey/route
    @GetMapping("/route")
    public ResponseEntity<?> getRoute(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam boolean optimisedRoute) {

        RouteState finalState = MetrolinkDijkstra.findShortestRoute(graph, start, end, optimisedRoute);

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

        // JSON
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("totalTime", finalState.actualTime);
        jsonResponse.put("totalChanges", finalState.changes);
        jsonResponse.put("path", pathArray);

        return ResponseEntity.ok(jsonResponse);
    }
}
