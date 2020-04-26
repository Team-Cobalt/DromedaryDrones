package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.Order;
import com.dromedarydrones.location.Point;
import com.dromedarydrones.location.Route;
import com.dromedarydrones.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;

/**
 * Drone utility for tracking drone settings and calculating order delivery times.
 * @author Christian Burns
 */
public class Drone implements XmlSerializable {

    public static final double DEFAULT_MAX_PAYLOAD_WEIGHT =  192.0; // 12 pounds
    public static final double DEFAULT_CRUISING_SPEED = 36.6667;    // 25 miles per hour
    public static final double DEFAULT_FLIGHT_TIME = 20 * 60;       // 20 minutes
    public static final double DEFAULT_TURN_AROUND_TIME = 150;      // 2 minutes 30 seconds
    public static final double DEFAULT_DELIVERY_TIME = 30;          // 30 seconds

    private double maxPayloadWeight;  // max cargo weight at takeoff in ounces
    private double cruisingSpeed;     // average cruising speed in feet per second with full load
    private double flightTime;        // max flight time in seconds with full load at full speed on one battery charge
    private double turnAroundTime;    // turn around time in seconds between flights to reload and recharge
    private double deliveryTime;      // delivery time in seconds to unload order(s) at a delivery point

    /**
     * Default constructor that initializes the default settings.
     * <pre>
     * max payload weight = 192 ounces | 12 pounds
     * cruising speed     = 25 miles per hour
     * flight time        = 20 minutes
     * turn around time   = 2 minutes 30 seconds
     * delivery time      = 30 seconds
     * </pre>
     * @author Christian Burns
     */
    public Drone() {
        maxPayloadWeight = DEFAULT_MAX_PAYLOAD_WEIGHT;
        cruisingSpeed = DEFAULT_CRUISING_SPEED;
        flightTime = DEFAULT_FLIGHT_TIME;
        turnAroundTime = DEFAULT_TURN_AROUND_TIME;
        deliveryTime = DEFAULT_DELIVERY_TIME;
    }

    /**
     * Copy constructor that creates a deep copy of an existing drone instance.
     * @author Christian Burns
     * @param other  the other instance to be cloned
     */
    public Drone(Drone other) {
        this.maxPayloadWeight = other.maxPayloadWeight;
        this.cruisingSpeed = other.cruisingSpeed;
        this.flightTime = other.flightTime;
        this.turnAroundTime = other.turnAroundTime;
        this.deliveryTime = other.deliveryTime;
    }

    /**
     * Initialize drone settings from an XML object.
     * @author Christian Burns
     * @param root  xml element containing drone data
     */
    public Drone(Element root) {

        if (root.hasAttribute("takeoff_capacity"))
            maxPayloadWeight = Double.parseDouble(root.getAttribute("takeoff_capacity"));
        else maxPayloadWeight = DEFAULT_MAX_PAYLOAD_WEIGHT;

        if (root.hasAttribute("cruising_speed"))
            cruisingSpeed = Double.parseDouble(root.getAttribute("cruising_speed"));
        else cruisingSpeed = DEFAULT_CRUISING_SPEED;

        if (root.hasAttribute("flight_time"))
            flightTime = Double.parseDouble(root.getAttribute("flight_time"));
        else flightTime = DEFAULT_FLIGHT_TIME;

        if (root.hasAttribute("recharge_time"))
            turnAroundTime = Double.parseDouble(root.getAttribute("recharge_time"));
        else turnAroundTime = DEFAULT_TURN_AROUND_TIME;

        if (root.hasAttribute("delivery_time"))
            deliveryTime = Double.parseDouble(root.getAttribute("delivery_time"));
        else deliveryTime = DEFAULT_DELIVERY_TIME;

    }

    /**
     * Calculates the delivery times for a set of meals to be delivered.
     * @author Christian Burns
     * @param orders       orders that need to be delivered
     * @param currentTime  current time in the simulation
     * @return             time in seconds it took to deliver all meals
     * @throws RuntimeException  if payload exceeds max takeoff weight or
     *                           if the flight time exceeded the set limit
     */
    public double deliver(List<Order> orders, double currentTime) {

        // ensure the drone can take off
        if (!isSafePayloadCapacity(orders))
            throw new RuntimeException(
                    "Payload exceeds max takeoff weight of " + maxPayloadWeight + " ounces.");

        // ensure the drone can deliver without exceeding flight time
        Route route = ordersToRoute(orders);
        if (!isSafeFlightTime(route))
            throw new RuntimeException(
                    "Drone lost power mid-flight after flying for " + flightTime + " seconds.");

        // deliver orders
        LinkedList<Point> flightPlan = route.getRoute();
        double distance, seconds;
        double duration = 0;
        Point position = null;

        // fly to each waypoint
        for (Point point : flightPlan) {

            // determine time to fly and deliver
            distance = point.distanceFromPoint(position);
            seconds = distance / cruisingSpeed;
            duration += seconds + deliveryTime;
            position = point;

            // set the delivery time to now
            for (Order order : orders) {
                if (order.getDestination().equals(position))
                    order.setTimeDelivered(currentTime + duration);
            }
        }

        // fly back to origin
        if (position != null) {
            distance = position.distanceFromPoint(null);
            seconds = distance / cruisingSpeed;
            duration += seconds;
        }

        return duration;
    }

    /**
     * Determines if the drone is capable of carrying out a route
     * of deliveries without exceeding the max flight time.
     * @author Christian Burns
     * @param route  route to be flown
     * @return       {@code true} if an acceptable flight time
     */
    public boolean isSafeFlightTime(Route route) {

        LinkedList<Point> flightPlan = route.getRoute();
        double distance, seconds;
        double duration = 0;
        Point position = null;

        // fly to each waypoint
        for (Point point : flightPlan) {
            distance = point.distanceFromPoint(position);
            seconds = distance / cruisingSpeed;
            duration += seconds + deliveryTime;
            position = point;
        }

        // fly back to origin
        if (position != null) {
            distance = position.distanceFromPoint(null);
            seconds = distance / cruisingSpeed;
            duration += seconds;
        }

        return duration <= flightTime;
    }

    /**
     * Determines if the drone's max takeoff capacity
     * can accommodate a collection of orders.
     * @author Christian Burns
     * @param orders  orders to use
     * @return        {@code true} if weight is safe
     */
    public boolean isSafePayloadCapacity(List<Order> orders) {
        double weight = orders.stream().mapToDouble(
                order -> order.getMealOrdered().getTotalWeight()).sum();
        return weight <= maxPayloadWeight;
    }

    /**
     * Returns the route for a set of orders.
     * @author Christian Burns
     * @param orders  orders to use
     * @return        ordered route containing unique points
     */
    public Route ordersToRoute(List<Order> orders) {
        LinkedList<Point> waypoints = new LinkedList<>();
        for (Order order : orders) {
            Point waypoint = order.getDestination();
            if (!waypoints.contains(waypoint))
                waypoints.add(waypoint);
        }
        return new Route(waypoints);
    }

    /**
     * Returns the maximum cargo weight at takeoff in ounces.
     * @author Christian Burns
     */
    public double getMaxPayloadWeight() {
        return maxPayloadWeight;
    }

    /**
     * Sets the maximum cargo weight at takeoff.
     * @author Christian Burns
     * @param weight  cargo weight in ounces
     */
    public void setMaxPayloadWeight(double weight) {
        maxPayloadWeight = weight;
    }

    /**
     * Returns the average cruising speed in feet per second with full load.
     * @author Christian Burns
     */
    public double getCruisingSpeed() {
        return cruisingSpeed;
    }

    /**
     * Sets the average cruising speed in feet per second with full load.
     * @author Christian Burns
     * @param speed  speed in feet per second
     */
    public void setCruisingSpeed(double speed) {
        cruisingSpeed = speed;
    }

    /**
     * Returns the max flight time in seconds with full load at full speed on one battery charge.
     * @author Christian Burns
     */
    public double getFlightTime() {
        return flightTime;
    }

    /**
     * Sets the max flight time in seconds with full load at full speed on one battery charge.
     * @author Christian Burns
     * @param time  time in seconds
     */
    public void setFlightTime(double time) {
        flightTime = time;
    }

    /**
     * Returns the turn around time in seconds between flights to reload and recharge.
     * @author Christian Burns
     */
    public double getTurnAroundTime() {
        return turnAroundTime;
    }

    /**
     * Sets the turn around time in seconds between flights to reload and recharge.
     * @author Christian Burns
     * @param time  time in seconds
     */
    public void setTurnAroundTime(double time) {
        turnAroundTime = time;
    }

    /**
     * Returns the delivery time in seconds to unload order(s) at a delivery point.
     * @author Christian Burns
     */
    public double getDeliveryTime() {
        return deliveryTime;
    }

    /**
     * Sets the delivery time in seconds to unload order(s) at a delivery point.
     * @author Christian Burns
     * @param time  time in seconds
     */
    public void setDeliveryTime(double time) {
        deliveryTime = time;
    }

    /**
     * Builds the XML element representing this drone instance.
     * @param doc  XML document
     * @return  XML element
     */
    @Override
    public Element toXml(Document doc) {
        Element root = doc.createElement("drone");
        root.setAttribute("takeoff_capacity", String.valueOf(maxPayloadWeight));
        root.setAttribute("cruising_speed", String.valueOf(cruisingSpeed));
        root.setAttribute("flight_time", String.valueOf(flightTime));
        root.setAttribute("recharge_time", String.valueOf(turnAroundTime));
        root.setAttribute("delivery_time", String.valueOf(deliveryTime));
        return root;
    }
}
