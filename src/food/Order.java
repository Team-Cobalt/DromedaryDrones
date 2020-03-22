package food;

import java.util.ArrayList;

public class Order {
	private ArrayList<Meal> orderList;
	//private DeliveryPoint destination;
	long timeOrdered;
	long timeDelivered;
	
	public Order() {
		orderList = new ArrayList<Meal>();
		//timeOrdered equal current time
	}
	

}
