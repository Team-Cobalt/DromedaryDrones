package mainapp;

import java.util.*;

import food.Meal;
import location.DeliveryPoints;
import food.Order;

public class Trial {
    private ArrayList<Meal> simMeals; //list of current sim's meals
    private ArrayList<Integer> simFlow; //current sim's stochastic flow
    private ArrayList<Order> simOrders; //list of orders generated during shift
    private DeliveryPoints simPoints; //list of current sim's delivery points
    private Queue<Order> fifoDeliveries;
    private Random rand;
    private final int MINUTES = 60;
    private final double MAX_CARGO_WEIGHT = 12;
    private LinkedList<Order> droneCargo;

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
        fifoDeliveries = new LinkedList<>();
        droneCargo = new LinkedList<>();
    }

    public void runFifoDeliveries() {
        double cargoWeight = 0.0;
        double currentMealWeight;

        //TODO: Need to get the timing

        while(cargoWeight < MAX_CARGO_WEIGHT && !fifoDeliveries.isEmpty()) {
            currentMealWeight = fifoDeliveries.peek().getMealOrdered().getTotalWeight();

            //add order to drone if the drone can take it
            if(cargoWeight + currentMealWeight <= 12) {
                droneCargo.add(fifoDeliveries.remove());
                cargoWeight += currentMealWeight;
            }
            //send drone to make deliveries if drone is full
            else {
                //TODO: run route deliveries

                //keep making deliveries while there are orders
                runFifoDeliveries();
            }
        }
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

        double [] mealProbs = new double[simMeals.size()];    //array of meal probabilities
        double upperBound;  //upper bound of possible ranges for random double
        int i; //index for loops and identifying randomly selected meal

        for (i = 0; i < simMeals.size(); i++){  //get probabilities
            mealProbs[i] = simMeals.get(i).getProbability();
        }

        double meal = rand.nextDouble();    //get decimal between 0.0 and 1.0

        upperBound = 0.0;
        for (i = 0; i < mealProbs.length; i++){
            upperBound += mealProbs[i];  //set upperBound to previous value + probability
            if (meal < upperBound){     //index i is the randomly selected meal
                break;
            }
        }

        return simMeals.get(i);
    }
}
