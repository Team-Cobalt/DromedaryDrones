package mainapp;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

import food.Meal;
import location.DeliveryPoints;
import food.Order;

public class Trial {
    private ArrayList<Meal> simMeals; //list of current sim's meals
    private ArrayList<Integer> simFlow; //current sim's stochastic flow
    private ArrayList<Order> simOrders; //list of orders generated during shift
    private DeliveryPoints simPoints; //list of current sim's delivery points
    private Random rand;
    private final int MINUTES = 60;

    /**
     * Constructor for creating a single four-hour shift
     * @param meals current simulation's list of meals to be used
     * @param model current simulation's stochastic flow model
     * @param points current simulation's delivery points to be used
     */
    public Trial(ArrayList<Meal> meals, ArrayList<Integer> model, DeliveryPoints points) {
        simMeals = new ArrayList<>(meals);
        simFlow = new ArrayList<>(model);
        simOrders = new ArrayList<>();
        simPoints = new DeliveryPoints(points);
        rand = new Random();
    }

    /**
     * Randomly generates the list of orders for the shift
     */
    public void generateOrders() {
        int timeOfOrder;
        int mealsPerHour;
        int index;
        ArrayList<Integer> orderTimes = new ArrayList<>();

        //generates a list of random order times according to given stochastic flow
        for(index = 0; index < simFlow.size(); index++) {
            //number of meals to be generated in specific hour
            mealsPerHour = simFlow.get(index);

            //generates each order time for all orders in each hour slot
            for(int mealNum = 0; mealNum < mealsPerHour; mealNum++) {
                //calculates time of order using given hour (i.e. first hour, second hour, etc.)
                timeOfOrder = (rand.nextInt(MINUTES) + 1) + (MINUTES * index);
                orderTimes.add(timeOfOrder);
            }
        }

        //sorts list of order times in increasing order
        Collections.sort(orderTimes);

        //creates order for each specific order time
        for(Integer orderTime : orderTimes) {
            Order newOrder = new Order(getRandomMeal(), orderTime, simPoints.getRandomPoint());
            simOrders.add(newOrder);
        }
    }

    /**
     * Method for randomly generating the meal to be ordered based on meal's probability
     * @return the specific meal that is ordered
     */
    public Meal getRandomMeal() {
        //TODO: write the code for randomly generating the meal
        return simMeals.get(0);
    }
}
