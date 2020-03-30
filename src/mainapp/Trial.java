package mainapp;

import java.util.ArrayList;
import food.Meal;
import location.DeliveryPoints;
import food.Order;

public class Trial {
    private ArrayList<Meal> simMeals;
    private ArrayList<Integer> simFlow;
    private ArrayList<Order> simOrders;
    private DeliveryPoints simDelPoints;

    public Trial() {
        simMeals = new ArrayList<>();
        simFlow = new ArrayList<>();
        simOrders = new ArrayList<>();
        simDelPoints = new DeliveryPoints();
    }
}
