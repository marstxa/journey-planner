package modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteFormatter {

    public static void printRoute(RouteState endState, boolean optimisedRoute) {
        if (endState == null) {
            System.out.println("\nNo route could be found");
            return;
        }

        if (optimisedRoute) {
            System.out.println("\n*** Minimal Time Route ***");
        } else {
            System.out.println("\n*** Route with Fewest Changes ***");
        }

// Reconstruct the path backwards
        List<RouteState> path = new ArrayList<>();
        RouteState curr = endState;

        while (curr != null) {
            path.add(curr);
            curr = curr.previous;
        }
        Collections.reverse(path); // Start -> End

        String currentLine = path.get(1).line; // The line we take from the start station

        if (currentLine.equals("walking")) { // now we know if the user is walking
            System.out.println(path.get(0).station + " walking");
        } else {
            System.out.println(path.get(0).station + " on " + currentLine + " line");
        }

        for (int i = 1; i < path.size(); i++) {
            RouteState step = path.get(i);

            if (!step.line.equals(currentLine)) {
                if (step.line.equals("walking")) {
                    System.out.println("** Change to Walking ***");
                    System.out.println(path.get(i - 1).station + " walking");
                } else if (currentLine.equals("walking")) {
                    System.out.println("** Board " + step.line + " line ***");
                    System.out.println(path.get(i - 1).station + " on " + step.line + " line");
                } else {
                    System.out.println("** Change Line to " + step.line + " line ***");
                    System.out.println(path.get(i - 1).station + " on " + step.line + " line");
                }
                currentLine = step.line;
            }

            if (step.line.equals("walking")) {
                System.out.println(step.station + " walking");
            } else {
                System.out.println(step.station + " on " + step.line + " line");
            }
        }

        System.out.println("Overall Journey Time (mins) =" + endState.actualTime);
        System.out.println("Total changes: " + endState.changes);
    }
}
