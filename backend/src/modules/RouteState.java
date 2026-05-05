package modules;

public class RouteState implements Comparable<RouteState> {

    String station;
    String line; // line used to arrive to this station
    double totalCost; // Renamed since time changes based on the method selected - time taken to get here so far
    double actualTime; // The actual time taken for the final output
    RouteState previous; // To reconstruct path backward if needed
    int changes; // Number of line changes made

    public RouteState(String station, String line, double totalCost, double actualTime, int changes, RouteState previous) {
        this.station = station;
        this.line = line;
        this.totalCost = totalCost;
        this.actualTime = actualTime;
        this.changes = changes;
        this.previous = previous;
    }

    @Override // for safety
    public int compareTo(RouteState other) {
        int costCompare = Double.compare(this.totalCost, other.totalCost);

        // tie breaker, if we have two routes with the same cost
        if (costCompare == 0) {
            return Double.compare(this.actualTime, other.actualTime);
        }

        return costCompare;
    }
}
