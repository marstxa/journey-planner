// Entry point for REST API
package com.metrolink;

import java.util.HashSet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import modules.MetrolinkGraph;
import modules.ReadMap;

@SpringBootApplication
public class MetrolinkApp {

    public static void main(String[] args) {
        SpringApplication.run(MetrolinkApp.class, args);
    }

    // Run on startup and keep it in memory
    @Bean
    public MetrolinkGraph metrolinkGraph() {
        MetrolinkGraph graph = new MetrolinkGraph();

        ReadMap.loadMapData("src/main/resources/utils/Metrolink_times_linecolour(in).csv", graph, new HashSet<>());
        ReadMap.loadWalkData("src/main/resources/utils/walktimes(in).csv", graph);

        System.out.println("Metrolink Graph loaded into memory successfully");
        return graph;
    }
}
