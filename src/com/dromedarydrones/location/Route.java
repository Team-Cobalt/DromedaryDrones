package com.dromedarydrones.location;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
* Class containing the "route" the drone will take as a queue of {@link Point}s.
* @author Brendan Ortmann
*/
public class Route {
    private LinkedList<Point> route;

    public Route(LinkedList<Point> points) throws IllegalArgumentException {
        if(points == null)
            throw new IllegalArgumentException("List of points cannot be null.");

        route = new LinkedList<>(points);
        calculateRouteDFS(route, 0, 0, getTotalDistance(route));
        // Calculate the optimal route using DFS w/pruning
    }

    /**
     * Function that calculates the minimum weight Hamiltonian cycle for the given list of {@link Point}s
     * using recursive backtracking and alpha-beta pruning.
     * Adapted from <a href="https://www.win.tue.nl/~kbuchin/teaching/2IL15/backtracking.pdf">this paper</a>.
     * @author Brendan Ortmann
     * @param currentRoute the list of {@code Point}s
     * @param index the current index being looked at in the current cycle construction
     * @param distanceSoFar the distance so far in the current cycle construction
     * @param bestDistance the smallest distance we've seen in any cycle so far
     * @return {@code LinkedList} of {@code Point}s which gives the shortest possible distance when traversed in order
     */
    private LinkedList<Point> calculateRouteDFS(LinkedList<Point> currentRoute, int index, double distanceSoFar,
                                               double bestDistance) {
        int size = currentRoute.size();

        if(size <= 1) // If only one point to deliver to, it's automatically the most efficient route
            return currentRoute;

        if(index == size) // Base case: if we've looked at every point in the route, update bestDistance
            bestDistance = Math.min(bestDistance, distanceSoFar + currentRoute.get(size-1)
                    .distanceFromPoint(null));

        // Iterate through trying different arrangements
        for(int otherIndex = index + 1; otherIndex < size; otherIndex++) {
            swapPoints(index + 1, otherIndex, currentRoute); // Swap points
            double newLength = distanceSoFar + currentRoute.get(index).distanceFromPoint(currentRoute.get(index + 1));
            if(newLength >= bestDistance) // Prune routes that we know are worse than one we already have
                continue;
            bestDistance = Math.min(bestDistance, getTotalDistance(calculateRouteDFS(currentRoute, index + 1,
                    newLength, bestDistance)));
            swapPoints(otherIndex, index + 1, currentRoute); // Reverse the swap
        }

        return currentRoute;
    }

    // Simulated Annealing from Baeldung
    // TODO: IGNORE FOR NOW
    private LinkedList<Point> calculateRouteSA() {
        double startingTemperature = 100; // Starting temperature
        int numberIterations = 10000; // Number of iterations before stopping
        double bestDistance = getTotalDistance(route);
        Random random = new Random();

        for(int index = 0; index < numberIterations; index++) {
            if(startingTemperature < 0.1)
                break;

            int pointAIndex = random.nextInt(route.size()), pointBIndex = random.nextInt(route.size());
            swapPoints(pointAIndex, pointBIndex, route); // Swap two random indices in route

            double distance = getTotalDistance(route);
            if(distance < bestDistance)
                bestDistance = distance;
            else if(Math.exp((bestDistance - distance) / startingTemperature) < Math.random())
                // SA allows for "bad" trades under the above criterion: if false, reverse the swap
                swapPoints(pointBIndex, pointAIndex, route);

            // "Cooling" function to lower temperature iteratively
            startingTemperature = (startingTemperature / Math.log(numberIterations));
        }

        return route;
    }

    /**
     * Swaps two Points in the given collection.
     * @param pointA the index of the first {@link Point}
     * @param pointB the index of the second {@code Point}
     * @param points the collection in which {@code Point}s are swapped
     */
    private void swapPoints(int pointA, int pointB, LinkedList<Point> points) {
        Collections.swap(points, pointA, pointB);
    }

    /**
     * Function that returns the total distance when traversing the given list of points in order. Since the list does
     * not include the starting {@link Point}, {@code null} is used to calculate the distance
     * between the given {@code Point} and the starting
     * {@code Point} at the beginning of the path and the end.
     * @param points the list of {@code Point}s for which we are calculating the distance
     * @return the total distance when traversing the {@code Point}s in order
     */
    private double getTotalDistance(LinkedList<Point> points) {
        double distance = 0;
        Point previousPoint = null;
        for (Point point : points) {
            distance += point.distanceFromPoint(previousPoint);
            previousPoint = point;
        }

        //add distance from last point to the start
        distance += points.get(points.size() - 1).distanceFromPoint(null);

        return distance;
    }

    /**
     * Getter for route member variable.
     * @return {@code route} member variable
     */
    public LinkedList<Point> getRoute(){
        return route;
    }

}
