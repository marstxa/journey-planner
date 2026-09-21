package modules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

public class ReadMap {
    // Reads the Metrolink CSV format and populates the Dijkstra planner

    public static void loadMapData(String filePath, MetrolinkGraph graph, Set<String> extractedStations) {
        String line;
        String currentLineColor = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // discard header row: From, To, Time (mins)

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",", -1);

                if (columns.length >= 3 && columns[1].trim().isEmpty() && columns[2].trim().isEmpty()) {
                    currentLineColor = columns[0].trim(); // this row is a new line's header
                } else if (columns.length >= 3) {
                    if (currentLineColor == null) {
                        System.err.println("Warning: connection row appeared before any line header, skipping: " + line);
                        continue;
                    }
                    try {
                        String fromStation = columns[0].trim();
                        String toStation = columns[1].trim();
                        double travelTime = Double.parseDouble(columns[2].trim());

                        graph.addConnection(fromStation, toStation, currentLineColor, travelTime);

                        extractedStations.add(fromStation);
                        extractedStations.add(toStation);
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: could not parse travel time for row: " + line);
                    }
                }
            }

            System.err.println("Map data loaded successfully");
        } catch (IOException e) {
            System.err.println("Could not read the file " + filePath);
            System.err.println("Make sure the file exists in the correct directory; it should be in /utils by default");
        }
    }

    public static void loadWalkData(String filePath, MetrolinkGraph graph) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                return;
            }

            String[] headerStations = headerLine.split(",", -1); // destination stations, by column
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] columns = line.split(",", -1);
                String startStation = columns[0].trim(); // first column is the start station for this row

                for (int i = 1; i < columns.length; i++) {
                    String timeStr = columns[i].trim();
                    if (timeStr.isEmpty()) {
                        continue; // no walking time recorded between these two stations
                    }

                    try {
                        double walkingTime = Double.parseDouble(timeStr);
                        String endStation = headerStations[i].trim();
                        // "walking" is used as the line name so the rest of the app can
                        // tell a walked leg apart from a leg taken by train
                        graph.addConnection(startStation, endStation, "walking", walkingTime);
                    } catch (NumberFormatException e) {
                        // not a number: ignore the cell
                    }
                }
            }
            System.out.println("Walking data loaded successfully");
        } catch (IOException e) {
            System.err.println("Could not read the walking file " + filePath);
        }
    }
}