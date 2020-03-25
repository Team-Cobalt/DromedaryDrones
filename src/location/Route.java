package location;

import java.util.LinkedList;

/**
* Class containing the "route" the drone will take as a queue of Points. Also tracks distance traveled.
* @author Brendan Ortmann
*/
public class Route {
    LinkedList<Point> points;
    double distanceTraveled;
    int currPoint;

    public Route(){
        points = new LinkedList<>();
        distanceTraveled = 0;
        currPoint = 0;
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
        if(currPoint >= points.size())
            return null;

        Point p = points.get(currPoint);
        currPoint++;
        return p;
    }

    // TODO: greedy or recursive backtracking/DFS for traveling salesman?
}
