package com.dromedarydrones.food;

import com.dromedarydrones.location.Point;

/**
 * Container for tracking an individual order within a Trial.
 * @author  Christian Burns
 */
public class Order implements Comparable<Order> {

	private Meal mealOrdered;
	private Point destination;
	private double timeOrdered;
	private double timeDelivered;

	/**
	 * Default constructor to create a new instance of the Order class.
	 * @param mealOrdered  meal ordered
	 * @param timeOrdered  time in seconds relative to the start of the sim the order was placed
	 * @param destination  drop off location where the meal was ordered
	 * @throws IllegalArgumentException if mealOrdered or destination is null
	 */
	public Order(Meal mealOrdered, double timeOrdered, Point destination) throws IllegalArgumentException {
		if(mealOrdered == null)
			throw new IllegalArgumentException("Ordered meal cannot be null.");
		if(destination == null)
			throw new IllegalArgumentException("Order destination cannot be null.");

		this.mealOrdered = mealOrdered;
		this.destination = destination;
		this.timeOrdered = timeOrdered;
		this.timeDelivered = -1;
	}

	/**
	 * Copy constructor to clone an existing instance of the Order class.
	 * @param other  other order instance to copy
	 */
	public Order(Order other) {
		this.mealOrdered = other.mealOrdered;
		this.timeOrdered = other.timeOrdered;
		this.timeDelivered = other.timeDelivered;
		this.destination = other.destination;
	}

	/**
	 * Returns the time in seconds relative to the start
	 * of the simulation of when the order was created.
	 */
	public double getTimeOrdered() {
		return timeOrdered;
	}

	/**
	 * Returns the time in seconds relative to the start
	 * of the simulation of when the order was delivered.
	 */
	public double getTimeDelivered() {
		return timeDelivered;
	}

	/**
	 * Sets the time in seconds relative to the start
	 * of the simulation of when the order was delivered.
	 * @param time  time in seconds relative to start of sim
	 */
	public void setTimeDelivered(double time) throws IllegalArgumentException {
		if(time < 0)
			throw new IllegalArgumentException("Delivery time cannot be negative.");

		timeDelivered = time;
	}

	public double getWaitTime() {
		return timeDelivered - timeOrdered;
	}

	/**
	 * Returns the meal that was ordered.
	 */
	public Meal getMealOrdered() {
		return mealOrdered;
	}

	/**
	 * Returns the total weight of the order.
	 * @author Christian Burns
	 */
	public double getTotalWeight() {
		return mealOrdered.getTotalWeight();
	}

	/**
	 * Returns the location of where the meal was ordered.
	 */
	public Point getDestination(){
		return destination;
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 *
	 * @param other the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                              from being compared to this object.
	 */
	@Override
	public int compareTo(Order other) {
		return Double.compare(timeOrdered, other.timeOrdered);
	}

	@Override
	public String toString() {
		return "Order{" +
				"mealOrdered=" + mealOrdered.getName() +
				", destination=" + destination.getName() +
				", timeOrdered=" + timeOrdered +
				" seconds, timeDelivered=" + timeDelivered +
				" seconds}";
	}
}
