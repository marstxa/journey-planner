package modules;

public class RouteState implements Comparable<RouteState> {

    public final String station;
    public final String line; // line used to arrive to this station
    public final double totalCost; // cost used for comparison (meaning depends on optimisedRoute)
    public final double actualTime; // actual cumulative travel time, for display
    public final RouteState previous; // to reconstruct the path backward
    public final int changes; // number of line changes made so far

    public RouteState(String station, String line, double totalCost, double actualTime, int changes, RouteState previous) {
        this.station = station;
        this.line = line;
        this.totalCost = totalCost;
        this.actualTime = actualTime;
        this.changes = changes;
        this.previous = previous;
    }

    @Override
    public int compareTo(RouteState other) {
        int costCompare = Double.compare(this.totalCost, other.totalCost);
        if (costCompare != 0) {
            return costCompare;
        }
        // tie-break on actual time when two routes have equal cost
        // (e.g. two routes with the same number of changes)
        return Double.compare(this.actualTime, other.actualTime);
    }
}