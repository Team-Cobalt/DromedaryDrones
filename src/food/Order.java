package food;

import location.Point;

public class Order implements Comparable<Order> {

	private Meal mealOrdered;
	private int timeOrdered;
	private int timeDelivered;
	private Point destination;

	public Order(Meal mealOrdered, int timeOrdered, Point destination) {
		this.mealOrdered = mealOrdered;
		this.timeOrdered = timeOrdered;
		this.timeDelivered = -1;
		this.destination = destination;
	}

	/**
	 * Copy constructor
	 * @param other  other order to copy
	 */
	public Order(Order other) {
		this.mealOrdered = other.mealOrdered;
		this.timeOrdered = other.timeOrdered;
		this.timeDelivered = other.timeDelivered;
		this.destination = other.destination;
	}

	public int getTimeOrdered() {
		return timeOrdered;
	}

	/**
	 * Returns the time in minutes relative to the start
	 * of the simulation of when the order was delivered.
	 */
	public int getTimeDelivered() {
		return timeDelivered;
	}

	/**
	 * Sets the time in minutes relative to the start
	 * of the simulation of when the order was delivered.
	 * @param time  relative time time in minutes
	 */
	public void setTimeDelivered(int time) {
		timeDelivered = time;
	}

	public Meal getMealOrdered() {
		return mealOrdered;
	}

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
		return Integer.compare(timeOrdered, other.timeOrdered);
	}
}
