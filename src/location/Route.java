package location;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
* Class containing the "route" the drone will take as a queue of Points.
* @author Brendan Ortmann
*/

public class Route {
    private LinkedList<Point> route;

    public Route(LinkedList<Point> points){ // Needs error checking
        route = new LinkedList<>(points);
    }

    // Simulated Annealing from Baeldung
    public LinkedList<Point> getRoute() {
        double t = 100; // Starting temperature
        int numIterations = 10000; // Number of iterations before stopping
        double bestDistance = getDistance();
        Random r = new Random();

        for(int i = 0; i < numIterations; i++){
            if(t < 0.1)
                break;

            int p = r.nextInt(route.size()), q = r.nextInt(route.size());
            swapPoints(p, q); // Swap two random indices in route

            double distance = getDistance();
            if(distance < bestDistance)
                bestDistance = distance;
            else if(Math.exp((bestDistance - distance) / t) < Math.random())
                swapPoints(q, p); // SA allows for "bad" trades under the above criterion: if false, reverse the swap

            t = (t / Math.log(numIterations)); // "Cooling" function to lower temperature iteratively
        }

        return route;
    }

    public void swapPoints(int x, int y){
        Collections.swap(route, x, y);
    }

    public double getDistance(){
        double d = 0;
        Point previousPoint = null;
        for (Point point : route) {
            d += point.distanceFromPoint(previousPoint);
            previousPoint = point;
        }
        return d;
    }
}
