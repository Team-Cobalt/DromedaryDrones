package location;

import java.util.Iterator;
import java.util.LinkedList;

/**
* Class containing the "route" the drone will take as a queue of Points. Also tracks distance traveled.
* @author Brendan Ortmann
*/

//Taking in LL<Orders> and constructing LL<Points>: route
public class Route {
    private DeliveryPoints points;
    private double distanceTraveled;
    private int currPoint;
    private Iterator<Point> pointIterator;
    private LinkedList<Point> route;

    public Route(DeliveryPoints points){
        this.points = points;
        pointIterator = points.iterator();
        distanceTraveled = 0;
        currPoint = 0;
        route = new LinkedList<>();
    }

    /**
     * Returns the distance traveled along the route so far.
     * @return the double distanceTraveled
     */
    public double getDistanceTraveled(){
        return distanceTraveled;
    }

    /**
     * Returns the next destination on the Route or null if there are none remaining.
     * @return the next {@link Point} in the queue
     */
    public Point getNextPoint(){
        if(!pointIterator.hasNext())
            return null;

        Point p = pointIterator.next();
        currPoint++;
        return p;
    }

    // TODO: greedy or recursive backtracking/DFS for traveling salesman?

    public LinkedList<Point> getRoute() {

        // Iterate through Orders to get Destinations
        // Construct route based on destinations

        return route;
    }
}
