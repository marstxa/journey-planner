package modules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

public class ReadMap {
    // Reads the Metrolink CSV format and populates the Dijkstra planner

    public static void loadMapData(String filePath, MetrolinkGraph graph, Set<String> extractedStations) {
        String line;
        String currentLineColor = "idk";

        // Using try to ensure file is closed
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            // read and discard first header row NOTE: From, To, Time (mins)
            br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",", -1);

                if (columns.length >= 3 && columns[1].trim().isEmpty() && columns[2].trim().isEmpty()) {
                    currentLineColor = columns[0].trim(); // update state to a new line color
                } else if (columns.length >= 3) {
                    try {
                        String formStation = columns[0].trim();
                        String toStation = columns[1].trim();
                        double travelTime = Double.parseDouble(columns[2].trim());

                        // add connection to graph
                        graph.addConnection(formStation, toStation, currentLineColor, travelTime);

                        // add to set
                        extractedStations.add(formStation);
                        extractedStations.add(toStation);
                    } catch (NumberFormatException e) {
                        // if it fails to parse the time (please don't)
                        System.err.println("Warning: Could not parse time for row: " + line);
                    }
                }
            }

            // Successful data read
            System.err.println("Map data loaded succesfully");
        } catch (IOException e) {
            System.err.println("Could not read the file " + filePath);
            System.err.println("Make sure the file exists in the correct directory it should be in /utils per default");
        }
    }

    public static void loadWalkData(String filePath, MetrolinkGraph graph) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine(); // read header row

            if (headerLine == null) {
                return;
            }

            String[] headerStations = headerLine.split(",", -1); // array holds all our destination stations
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] columns = line.split(",", -1);
                String startStation = columns[0].trim(); // the very first row is our start station

                // loop through the rest of columns
                for (int i = 1; i < columns.length; i++) {
                    String timeStr = columns[i].trim(); // grab walking time

                    if (!timeStr.isEmpty()) {
                        try {
                            // convert text
                            double walkingTime = Double.parseDouble(timeStr);

                            // loop index to look back up at the header array
                            String endStation = headerStations[i].trim();

                            // add the connnection to our graph
                            // now the program knows what "line" is walking and not an actual train line
                            graph.addConnection(startStation, endStation, "walking", walkingTime);
                        } catch (NumberFormatException e) {
                            // ignore empty cells
                        }
                    }
                }
            }
            System.out.println("Walking data loaded succesfully");
        } catch (IOException e) {
            System.err.println("Could not read the walking file " + filePath);
        }
    }
}
