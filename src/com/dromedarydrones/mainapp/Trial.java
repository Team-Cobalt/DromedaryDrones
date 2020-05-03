package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.Meal;
import com.dromedarydrones.food.Order;
import com.dromedarydrones.location.DeliveryPoints;

import java.util.*;

/**
 * @author  Izzy Patnode, Christian Burns, Brendan Ortmann, and Rachel Franklin
 */
public class Trial {

    private final Drone drone;                      // drone settings
    private final ArrayList<Meal> mealTypes;        // all available types of meals
    private final DeliveryPoints deliveryPoints;    // all available destinations
    private final ArrayList<Integer> ordersPerHour; // number of orders to deliver each hour

    private final Queue<Order> fifoOrderQueue;      // fifo order queue
    private final Queue<Order> knapsackOrderQueue;      // knapsack order queue
    private final List<Order> fifoResults;          // order queue for the fifo algorithm
    private final List<Order> knapsackResults;          // order queue for the knapsack algorithm
    private final Random random;                    // random number generator

    private static final int SECONDS_PER_HOUR = 3600;   // 60 seconds * 60 minutes

    /**
     * Constructor for creating a single four-hour shift
     * @author Izzy Patnode, Christian Burns
     * @param simulationConfiguration  simulation configuration to use
     */
    public Trial(Simulation simulationConfiguration) {

        drone = simulationConfiguration.getDroneSettings();
        mealTypes = simulationConfiguration.getMealTypes();
        ordersPerHour = simulationConfiguration.getStochasticFlow();
        deliveryPoints = new DeliveryPoints(simulationConfiguration.getDeliveryPoints());

        fifoOrderQueue = new LinkedList<>();
        knapsackOrderQueue = new LinkedList<>();
        fifoResults = new ArrayList<>();
        knapsackResults = new ArrayList<>();
        random = new Random();

        // clone the orders into the order queues
        for (Order order : generateOrders()) {
            fifoOrderQueue.add(new Order(order));
            knapsackOrderQueue.add(new Order(order));
        }
    }

    /**
     * Runs the simulation for one trial and returns the result.
     * @author Christian Burns
     */
    public TrialResults run() {
        runFifoDeliveries();
        runKnapsackDeliveries();
        return new TrialResults(fifoResults, knapsackResults);
    }

    /**
     * Generates delivery times based on a knapsack packing algorithm.
     * @author Brendan Ortmann, Christian Burns
     */
    public void runKnapsackDeliveries() {

        double simulationTime = 0;  // reset the simulation time to zero
        double cargoWeight = 0;     // weight of cargo already on drone
        double currentMealWeight;   // weight of the current meal
        boolean safe;

        //List<Order> knapsackDeliveryResults = new ArrayList<>();
        List<Order> availableOrders = new ArrayList<>();
        List<Order> dronePayload = new ArrayList<>();
        List<Order> skippedOrders = new ArrayList<>();

        // loop while orders are still being placed or there's a backlog of orders to be delivered
        while(!knapsackOrderQueue.isEmpty() || !skippedOrders.isEmpty()) {

            // obtain all the newly available orders
            while (true) {
                Order nextOrder = knapsackOrderQueue.peek();
                if (nextOrder == null || nextOrder.getTimeOrdered() > simulationTime) break;
                availableOrders.add(knapsackOrderQueue.remove());
            }

            // sort available orders by weight in descending order
            availableOrders.sort(Comparator.comparingDouble(Order::getTotalWeight));

            // sort skipped orders by order time in increasing order
            Collections.sort(skippedOrders);

            // load up drone with orders we skipped last time
            ListIterator<Order> skippedOrderList = skippedOrders.listIterator();
            while (skippedOrderList.hasNext()) {
                Order order = skippedOrderList.next();
                currentMealWeight = order.getTotalWeight();

                safe = safeToAdd(dronePayload, order);

                if (cargoWeight + currentMealWeight <= drone.getMaxPayloadWeight() && safe) {
                    cargoWeight += currentMealWeight;
                    dronePayload.add(order);
                    skippedOrderList.remove();
                }
            }

            // load up drone with newly available orders
            for (Order order : availableOrders) {
                currentMealWeight = order.getTotalWeight();

                safe = safeToAdd(dronePayload, order);

                if (cargoWeight + currentMealWeight > drone.getMaxPayloadWeight() || !safe) {
                    skippedOrders.add(order);
                } else {
                    cargoWeight += currentMealWeight;
                    dronePayload.add(order);
                }
            }

            // all available orders have been processed
            availableOrders.clear();

            // deliver the ordered meals
            if (!dronePayload.isEmpty()) {
                simulationTime += drone.getTurnAroundTime();
                simulationTime += drone.deliver(dronePayload, simulationTime);
                knapsackResults.addAll(dronePayload);
                dronePayload.clear();
                cargoWeight = 0;
            }
            else {
                simulationTime++;
            }
        }
    }

    /**
     * Method that generates delivery times based on a first-in-first-out packing algorithm.
     * @author Izzy Patnode and Christian Burns
     */
    public void runFifoDeliveries() {

        double simulationTime = 0;  // reset the simulation time to zero
        double cargoWeight = 0;     // weight of cargo already on drone
        double currentMealWeight;   // weight of the current meal

        //List<Order> fifoDeliveryResults = new ArrayList<>();
        List<Order> dronePayload = new ArrayList<>();

        //runs delivery routes while there are orders to be delivered
        while (!fifoOrderQueue.isEmpty()) {

            // load up drone with meals ordered in the past that don't exceed payload capacity
            while (true) {
                Order nextOrder = fifoOrderQueue.peek();
                if (nextOrder == null || nextOrder.getTimeOrdered() > simulationTime) break;
                currentMealWeight = nextOrder.getTotalWeight();
                if (currentMealWeight + cargoWeight > drone.getMaxPayloadWeight()) break;

                // would adding the order exceed max flight time?
                if (!safeToAdd(dronePayload, nextOrder)) break;

                dronePayload.add(fifoOrderQueue.remove());
                cargoWeight += currentMealWeight;
            }

            // deliver the ordered meals
            if (!dronePayload.isEmpty()) {
                simulationTime += drone.getTurnAroundTime();
                simulationTime += drone.deliver(dronePayload, simulationTime);
                fifoResults.addAll(dronePayload);
                dronePayload.clear();
                cargoWeight = 0;
            }
            else {
                simulationTime++;
            }
        }
    }

    private boolean safeToAdd(List<Order> dronePayload, Order nextOrder) {
        List<Order> newPayload = new ArrayList<>(dronePayload);
        newPayload.add(nextOrder);
        return drone.isEstimatedSafeFlightTime(newPayload);
    }

    /**
     * Generates a list of random orders to be used for this trial.
     * @author  Izzy Patnode and Christian Burns
     * @return  list of orders with their creation times relative
     *          to the start of the simulation in seconds.
     */
    private List<Order> generateOrders() {

        ArrayList<Order> orders = new ArrayList<>();
        int hour, mealsPerHour, mealNum, creationTime;
        int hourCount = ordersPerHour.size();

        // generates a list of random order times according to the given stochastic flow
        for (hour = 0; hour < hourCount; hour++) {
            // number of meals to be generated in specific hour
            mealsPerHour = ordersPerHour.get(hour);

            // generates each order time for all orders in each hour slot
            for (mealNum = 0; mealNum < mealsPerHour; mealNum++) {
                // calculates time of order using given hour (i.e. first hour, second hour, etc.)
                creationTime = (random.nextInt(SECONDS_PER_HOUR) + 1) + (SECONDS_PER_HOUR * hour);
                orders.add(new Order(getRandomMeal(), creationTime, deliveryPoints.getRandomPoint()));
            }
        }

        // sorts list of order times in increasing order relative to their creation times
        Collections.sort(orders);
        return orders;
    }

    /**
     * Method for randomly generating the meal to be ordered based on meal's probability
     * @author Rachel Franklin
     * @return the specific meal that is ordered
     */
    public Meal getRandomMeal() {

        int mealCount = mealTypes.size();
        double [] mealProbabilities = new double[mealCount];    //array of meal probabilities
        double upperBound;  //upper bound of possible ranges for random double
        int index; //index for loops and identifying randomly selected meal

        for (index = 0; index < mealCount; index++){  //get probabilities
            mealProbabilities[index] = mealTypes.get(index).getProbability();
        }

        double meal = random.nextDouble();    //get decimal between 0.0 and 1.0

        upperBound = 0.0;
        for (index = 0; index < mealProbabilities.length; index++){
            upperBound += mealProbabilities[index];  //set upperBound to previous value + probability
            if (meal < upperBound){     //index i is the randomly selected meal
                break;
            }
        }

        return mealTypes.get(index);
    }
}
