package modules;

import java.util.Scanner;
import java.util.Set;

public class GetJourney {

    public static void start(MetrolinkGraph graph, Set<String> validStations) {

        Scanner scanner = new Scanner(System.in);

        // CLI Welcome Screen
        System.out.println("                 _-====-__-======-__-========-_____-============-__");
        System.out.println("               _(   WELCOME TO THE MANCHESTER METROLINK PLANNER   _)");
        System.out.println("            OO(                                                   )_");
        System.out.println("           0  (_                                                   _)");
        System.out.println("         o0     (_                                                _)");
        System.out.println("        o         '=-___-===-_____-========-___________-===-dwb-='");
        System.out.println("      .o                                _________");
        System.out.println("     . ______          ______________  |         |      _____");
        System.out.println("   _()_||__|| ________ |            |  |_________|   __||___||__");
        System.out.println("  (BNSF 1995| |      | |            | __Y______00_| |_         _|");
        System.out.println(" /-OO----OO\"\"=\"OO--OO\"=\"OO--------OO\"=\"OO-------OO\"=\"OO-------OO\"=P");
        System.out.println("#####################################################################");
        System.out.println();

        // Get user input
        String startStation = getValidStation(scanner, "Enter your starting station: ", validStations);
        String endStation = getValidStation(scanner, "Enter your destination station: ", validStations);
        boolean optimisedRoute = getOptimisedInput(scanner);

        closeStations(scanner, graph, validStations);
        addDelays(scanner, graph, validStations);

        // Output for testing
        System.out.println("\nPlanning route...");
        RouteState finalState = MetrolinkDijkstra.findShortestRoute(graph, startStation, endStation, optimisedRoute);
        RouteFormatter.printRoute(finalState, optimisedRoute);

        scanner.close();
    }

    private static String getValidStation(Scanner scanner, String prompt, Set<String> validStations) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            // Check if input exists in the list of valid stations
            // Used for loop to make it case-insensitive for better ux
            for (String station : validStations) {
                if (station.equalsIgnoreCase(input)) {
                    return station; // Return station capitalised
                }
            }

            // If station is invalid
            System.out.println("[Error] '" + input + "' is not a recognised Metrolink station. Please try again.\n");
        }
    }

    // handles user preference for choosing the optimisedRoute method 
    private static boolean getOptimisedInput(Scanner scanner) {
        while (true) {
            System.out.println("\nHow would you like to travel?");
            System.out.println(" 1. Shortest Time");
            System.out.println(" 2. Fewest Changes");
            System.out.print("Please choose 1 or 2: ");

            String input = scanner.nextLine().trim();

            if (input.equals("1")) {
                return true;
            } else if (input.equals("2")) {
                return false;
            } else {
                System.out.println("[Error] Invalid choice. \nPlease eneter 1 or 2.");
            }
        }
    }

    // ignores capital sensitive cases
    private static String validateString(String input, Set<String> validStations) {
        for (String station : validStations) {
            if (station.equalsIgnoreCase(input)) {
                return station;
            }
        }
        return null;
    }

    private static void closeStations(Scanner scanner, MetrolinkGraph graph, Set<String> validStations) {
        System.out.println("\n*** Station Closures ***");
        System.out.println("Are there any stations closed today?");

        while (true) {
            System.out.println("Enter closed station name (press Enter to skip)");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) { // allows user to skip preference if not needed
                break;
            }

            String validStation = validateString(input, validStations);

            if (validStation != null) {
                graph.closeStation(validStation);
                System.out.println("[System]" + validStation + " is now CLOSED.");
            } else {
                System.out.println("[Error] Unkown station.");
            }
        }
    }

    private static void addDelays(Scanner scanner, MetrolinkGraph graph, Set<String> validStations) {
        System.out.println("\n*** Line Delays ***");
        System.out.println("Are there any delays between two stations?");

        while (true) {
            System.out.println("\nEnter Start Station for delay: (press Enter to skip)"); // get start
            String startInput = scanner.nextLine().trim();

            if (startInput.isEmpty()) { // allows user to skip preference if not needed
                break;
            }

            String startStation = validateString(startInput, validStations);

            if (startStation == null) {
                System.out.println("[Error] Unkown station.");
                continue;
            }

            System.out.println("\nEnter End Station for delay: "); // get end 
            String endInput = scanner.nextLine().trim();
            String endStation = validateString(endInput, validStations);

            if (endStation == null) {
                System.out.println("[Error] Unkown station.");
                continue;
            }

            // Allow user to enter a specified amount of time for delays it must be a double
            System.out.println("\nEnter the NEW total travel time (in mins) between " + startInput + " and " + endStation);
            try {
                double newTime = Double.parseDouble(scanner.nextLine().trim());
                graph.addDelay(startStation, endStation, newTime);

                System.out.println("[System] Delay logged. New time is " + newTime + " mins.");
            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid number. Delay not added.");
            }
        }
    }

}
