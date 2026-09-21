package modules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class GetJourney {

    public static void start(MetrolinkGraph graph, Set<String> validStations) {
        Scanner scanner = new Scanner(System.in);

        printWelcomeBanner();

        String startStation;
        String endStation;
        while (true) {
            startStation = getValidStation(scanner, "Enter your starting station: ", validStations);
            endStation = getValidStation(scanner, "Enter your destination station: ", validStations);

            if (!startStation.equalsIgnoreCase(endStation)) {
                break;
            }
            System.out.println("[Error] Start and destination stations can't be the same. Please try again.\n");
        }

        boolean optimisedRoute = getOptimisedInput(scanner);

        Set<String> closedStations = readClosedStations(scanner, validStations);
        Map<String, Double> delays = readDelays(scanner, validStations);
        RouteConstraints constraints = new RouteConstraints(closedStations, delays);

        System.out.println("\nPlanning route...");
        RouteState finalState = MetrolinkDijkstra.findShortestRoute(graph, startStation, endStation, optimisedRoute, constraints);
        RouteFormatter.printRoute(finalState, optimisedRoute);

        scanner.close();
    }

    private static void printWelcomeBanner() {
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
    }

    private static String getValidStation(Scanner scanner, String prompt, Set<String> validStations) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            String match = validateString(input, validStations);
            if (match != null) {
                return match;
            }

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
                System.out.println("[Error] Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    // case-insensitive lookup, returns the canonical-cased station name
    private static String validateString(String input, Set<String> validStations) {
        for (String station : validStations) {
            if (station.equalsIgnoreCase(input)) {
                return station;
            }
        }
        return null;
    }

    private static Set<String> readClosedStations(Scanner scanner, Set<String> validStations) {
        Set<String> closedStations = new HashSet<>();

        System.out.println("\n*** Station Closures ***");
        System.out.println("Are there any stations closed today?");

        while (true) {
            System.out.println("Enter closed station name (press Enter to skip)");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                break;
            }

            String validStation = validateString(input, validStations);
            if (validStation != null) {
                closedStations.add(RouteConstraints.normalize(validStation));
                System.out.println("[System] " + validStation + " is now CLOSED.");
            } else {
                System.out.println("[Error] Unknown station.");
            }
        }

        return closedStations;
    }

    private static Map<String, Double> readDelays(Scanner scanner, Set<String> validStations) {
        Map<String, Double> delays = new HashMap<>();

        System.out.println("\n*** Line Delays ***");
        System.out.println("Are there any delays between two stations?");

        while (true) {
            System.out.println("\nEnter Start Station for delay: (press Enter to skip)");
            String startInput = scanner.nextLine().trim();

            if (startInput.isEmpty()) {
                break;
            }

            String startStation = validateString(startInput, validStations);
            if (startStation == null) {
                System.out.println("[Error] Unknown station.");
                continue;
            }

            System.out.println("\nEnter End Station for delay: ");
            String endInput = scanner.nextLine().trim();
            String endStation = validateString(endInput, validStations);

            if (endStation == null) {
                System.out.println("[Error] Unknown station.");
                continue;
            }

            System.out.println("\nEnter the NEW total travel time (in mins) between " + startStation + " and " + endStation);
            try {
                double newTime = Double.parseDouble(scanner.nextLine().trim());
                String from = RouteConstraints.normalize(startStation);
                String to = RouteConstraints.normalize(endStation);
                delays.put(from + "-" + to, newTime);
                delays.put(to + "-" + from, newTime);

                System.out.println("[System] Delay logged. New time is " + newTime + " mins.");
            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid number. Delay not added.");
            }
        }

        return delays;
    }
}