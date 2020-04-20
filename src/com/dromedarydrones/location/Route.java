package com.dromedarydrones.location;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
* Class containing the "route" the drone will take as a queue of {@link Point}s.
* @author Brendan Ortmann
*/

// TODO: Error checking in all functions

public class Route {
    private LinkedList<Point> route;

    public Route(LinkedList<Point> points){
        route = new LinkedList<>(points);
        route = calculateRouteDFS(route, 0, 0, getDistance(route));
        // Calculate the optimal route using DFS w/pruning
    }

    /**
     * Function that calculates the minimum weight Hamiltonian cycle for the given list of {@link Point}s using recursive
     * backtracking and alpha-beta pruning. Adapted from <a href="https://www.win.tue.nl/~kbuchin/teaching/2IL15/backtracking.pdf">this paper</a>.
     * @author Brendan Ortmann
     * @param currentRoute the list of {@code Point}s
     * @param index the current index being looked at in the current cycle construction
     * @param distanceSoFar the distance so far in the current cycle construction
     * @param bestDistance the smallest distance we've seen in any cycle so far
     * @return {@code LinkedList} of {@code Point}s which gives the shortest possible distance when traversed in order
     */
    public LinkedList<Point> calculateRouteDFS(LinkedList<Point> currentRoute, int index, double distanceSoFar,
                                               double bestDistance){
        int n = currentRoute.size();

        if(n == 1) // If only one point to deliver to, it's automatically the most efficient route
            return currentRoute;

        if(index == n) // Base case: if we've looked at every point in the route, update bestDistance
            bestDistance = Math.min(bestDistance, distanceSoFar + currentRoute.get(n-1).distanceFromPoint(null));

        for(int i = index + 1; i < n; i++){ // Iterate through trying different arrangements
            swapPoints(index+1, i, currentRoute); // Swap points
            double newLength = distanceSoFar + currentRoute.get(index).distanceFromPoint(currentRoute.get(index+1));
            if(newLength >= bestDistance) // Prune routes that we know are worse than one we already have
                continue;
            bestDistance = Math.min(bestDistance, getDistance(calculateRouteDFS(currentRoute, index + 1,
                    newLength, bestDistance)));
            swapPoints(i, index+1, currentRoute); // Reverse the swap
        }

        return currentRoute;
    }

    // Simulated Annealing from Baeldung
    // TODO: IGNORE FOR NOW
    public LinkedList<Point> calculateRouteSA() {
        double t = 100; // Starting temperature
        int numIterations = 10000; // Number of iterations before stopping
        double bestDistance = getDistance(route);
        Random r = new Random();

        for(int i = 0; i < numIterations; i++){
            if(t < 0.1)
                break;

            int p = r.nextInt(route.size()), q = r.nextInt(route.size());
            swapPoints(p, q, route); // Swap two random indices in route

            double distance = getDistance(route);
            if(distance < bestDistance)
                bestDistance = distance;
            else if(Math.exp((bestDistance - distance) / t) < Math.random())
                swapPoints(q, p, route); // SA allows for "bad" trades under the above criterion: if false, reverse the swap

            t = (t / Math.log(numIterations)); // "Cooling" function to lower temperature iteratively
        }

        return route;
    }

    /**
     * Swaps two Points in the given collection.
     * @param x the index of the first {@link Point}
     * @param y the index of the second {@code Point}
     * @param points the collection in which {@code Point}s are swapped
     */
    public void swapPoints(int x, int y, LinkedList<Point> points){
        Collections.swap(points, x, y);
    }

    /**
     * Function that returns the total distance when traversing the given list of points in order. Since the list does
     * not include the starting {@link Point}, {@code null} is used to calculate the distance between the given {@code Point} and the starting
     * {@code Point} at the beginning of the path and the end.
     * @param points the list of {@code Point}s for which we are calculating the distance
     * @return the total distance when traversing the {@code Point}s in order
     */
    public double getDistance(LinkedList<Point> points){
        double d = 0;
        Point previousPoint = null;
        for (Point point : points) {
            d += point.distanceFromPoint(previousPoint);
            previousPoint = point;
        }

        d += points.get(points.size()-1).distanceFromPoint(null); // Add distance from last point to the start

        return d;
    }

    /**
     * Getter for route member variable.
     * @return {@code route} member variable
     */
    public LinkedList<Point> getRoute(){
        return route;
    }
}
