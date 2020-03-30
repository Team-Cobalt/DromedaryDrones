package food;

import java.util.ArrayList;
import location.Point;

public class Order {
	private Meal mealOrdered;
	int timeOrdered;
	Point destination;

	
	public Order(Meal mealOrdered, int timeOrdered, Point destination) {
		this.mealOrdered = mealOrdered;
		this.timeOrdered = timeOrdered;
		this.destination = destination;
	}
	

}
