package mainapp;

import java.util.*;

import food.Meal;
import location.DeliveryPoints;
import food.Order;
import location.Point;
import location.Route;

public class Trial {
    private ArrayList<Meal> simMeals; //list of current sim's meals
    private ArrayList<Integer> simFlow; //current sim's stochastic flow
    private ArrayList<Order> simOrders; //list of orders generated during shift
    private DeliveryPoints simPoints; //list of current sim's delivery points
    private Queue<Order> fifoDeliveries; //queue of orders for FIFO
    private Random rand; //used for random number generation
    private LinkedList<Order> droneCargo; //list of orders on drone
    private Route droneRoute;
    private LinkedList<Point> droneDestinations; //list of destinations for drone's route

    private final int MINUTES = 60;
    private final double MAX_CARGO_WEIGHT = 12;

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
        droneDestinations = new LinkedList<>();
    }

    public void runFifoDeliveries() {
        double cargoWeight = 0.0; //weight of cargo already on drone
        double currentMealWeight; //weight of the current meal
        int index; //loop variable

        //calculates generates list of orders based on time they are ordered
        simOrders = generateOrders();

        //adds all orders to fifo queue
        for(index = 0; index < simOrders.size(); index++) {
            fifoDeliveries.add(simOrders.get(index));
        }

        //runs delivery routes while there are orders to be delivered
        while(!fifoDeliveries.isEmpty()) {
            currentMealWeight = fifoDeliveries.peek().getMealOrdered().getTotalWeight();

            //add order to drone if the drone can take it
            if(cargoWeight + currentMealWeight <= MAX_CARGO_WEIGHT) {
                droneCargo.add(fifoDeliveries.remove());
                cargoWeight += currentMealWeight;
            }
            //send drone to make deliveries if drone is full
            else {
                //creates list of destinations the drone will need to visit
                for(index = 0; index < droneCargo.size(); index++) {
                    droneDestinations.add(droneCargo.get(index).getDestination());
                }

                //calculates the most optimal route given the drone's list of destinations
                droneRoute = new Route(droneDestinations);
                droneDestinations = droneRoute.getRoute();

                //delivers orders to specified destinations using calculated route
                makeDeliveries(droneCargo, droneDestinations);

                //TODO: TIMING
                //60,000ms in 1 minute
            }
        }
    }

    public void makeDeliveries(LinkedList<Order> droneOrders, LinkedList<Point> route) {
        //TODO: write make deliveries method that takes in a list of orders
    }

    /**
     * Randomly generates the list of orders for the shift
     */
    public ArrayList<Order> generateOrders() {
        int timeOfOrder;
        int mealsPerHour;
        int index;
        ArrayList<Integer> orderTimes = new ArrayList<>();
        ArrayList<Order> orders = new ArrayList<>();

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
            orders.add(newOrder);
        }

        return orders;
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
