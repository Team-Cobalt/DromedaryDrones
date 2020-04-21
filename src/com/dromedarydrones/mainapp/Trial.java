package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.Meal;
import com.dromedarydrones.food.Order;
import com.dromedarydrones.location.DeliveryPoints;
import com.dromedarydrones.location.Point;
import com.dromedarydrones.location.Route;

import java.util.*;

/**
 * @author  Isabella Patnode, Christian Burns,
 *          Brendan Ortmann, and Rachel Franklin
 */
public class Trial {
    private ArrayList<Meal> simulationMeals; //list of current sim's meals
    private ArrayList<Integer> simulationFlow; //current sim's stochastic flow
    private ArrayList<Order> simulationOrders; //list of orders generated during shift
    private DeliveryPoints simulationPoints; //list of current sim's delivery points
    private Queue<Order> fifoDeliveries; //queue of orders for FIFO
    private Random numberGenerator; //used for random number generation
    private LinkedList<Order> droneCargo; //list of orders on drone
    private Route droneRoute;
    private LinkedList<Point> droneDestinations; //list of destinations for drone's route
    private double simulationTime;

    private static final int SECONDS_PER_HOUR = 3600;   // 60 seconds * 60 minutes
    private static final int SECONDS_TO_DELIVER = 30;   // 30 seconds
    private static final int SECONDS_TO_RECHARGE = 180; // 3 minutes
    private static final double MAX_CARGO_WEIGHT = 192; // 12 pounds in ounces
    public static final double MAX_SPEED_FEET_PER_SEC = 20 * 1.467; // 20 mph -> 29.3333 feet per second

    /**
     * Constructor for creating a single four-hour shift
     * @author Isabella Patnode
     * @param meals current simulation's list of meals to be used
     * @param model current simulation's stochastic flow model
     * @param points current simulation's delivery points to be used
     */
    public Trial(ArrayList<Meal> meals, ArrayList<Integer> model, DeliveryPoints points) {
        simulationMeals = new ArrayList<>(meals);
        simulationFlow = new ArrayList<>(model);
        simulationPoints = new DeliveryPoints(points);
        numberGenerator = new Random();
        fifoDeliveries = new LinkedList<>();
        droneCargo = new LinkedList<>();
        droneDestinations = new LinkedList<>();
        simulationTime = 0.0;
        //calculates generates list of orders based on time they are ordered
        simulationOrders = generateOrders();
    }

    /**
     * Runs the simulation for one trial and returns the result.
     */
    public TrialResults run() {
        return new TrialResults(runFifoDeliveries(), runKnapsackDeliveries());
    }

    /**
     * TODO: Add Javadoc and comments to this method
     * TODO: Test method
     * @author Brendan Ortmann
     * @return asdf
     */
    public ArrayList<Order> runKnapsackDeliveries(){
        double cargoWeight = 0.0, currentMealWeight;
        ArrayList<Order> knapsackDeliveries = new ArrayList<>();
        ArrayList<Order> skippedOrders = new ArrayList<>();
        ArrayList<Order> knapsackDeliveryResults = new ArrayList<>();
        simulationTime = 0.0;

        for(Order order : simulationOrders)
            knapsackDeliveries.add(new Order(order));

        knapsackDeliveries.sort(Comparator.comparingDouble(a -> a.getMealOrdered().getTotalWeight())); // Sort the meals in descending order based on weight

        while(!knapsackDeliveries.isEmpty()){

            Collections.sort(skippedOrders); // Sort skipped orders by time so that older orders get added first
            for(int index = 0; index < skippedOrders.size(); index++){ // Query skipped orders first
                Order skippedOrder = skippedOrders.get(index);
                if(skippedOrder.getTimeOrdered() > simulationTime) continue;
                currentMealWeight = skippedOrder.getMealOrdered().getTotalWeight();
                if(currentMealWeight + cargoWeight > MAX_CARGO_WEIGHT) continue; // If max weight exceeded, ignore
                droneCargo.add(skippedOrder);
                cargoWeight += currentMealWeight;
                skippedOrders.remove(skippedOrder);
            }

            for(int index = 0; index < knapsackDeliveries.size(); index++){ // Query current set of orders to add to drone
                Order order = knapsackDeliveries.get(index);
                if(order.getTimeOrdered() > simulationTime) continue;
                currentMealWeight = order.getMealOrdered().getTotalWeight();
                if(currentMealWeight + cargoWeight > MAX_CARGO_WEIGHT){
                    skippedOrders.add(order);
                    knapsackDeliveries.remove(order);
                    continue;
                }
                droneCargo.add(order);
                cargoWeight += currentMealWeight;
                knapsackDeliveries.remove(order);
            }

            if (!droneCargo.isEmpty()) {
                //creates list of destinations the drone will need to visit
                for (Order cargo : droneCargo) {
                    if (!droneDestinations.contains(cargo.getDestination())) {
                        droneDestinations.add(cargo.getDestination());
                    }
                }

                //calculates the most optimal route given the drone's list of destinations
                droneRoute = new Route(droneDestinations);
                droneDestinations = droneRoute.getRoute();

                //delivers orders to specified destinations using calculated route
                simulationTime += SECONDS_TO_RECHARGE;  // three minutes to load the drone
                makeDeliveries(droneDestinations);

                // mark the delivery time for each order
                droneCargo.forEach(order -> order.setTimeDelivered(simulationTime));
                knapsackDeliveryResults.addAll(droneCargo);
                droneCargo.clear();

                cargoWeight = 0.0;
            } else {
                simulationTime++;
            }
        }

        return knapsackDeliveryResults;
    }

    /**
     * Method that uses FIFO to deliver the orders
     * @author Isabella Patnode and Christian Burns
     * @return  list of orders used in the trial that contain their creation
     *          and delivery times relative to the start of the trial
     */
    public ArrayList<Order> runFifoDeliveries() {
        double cargoWeight = 0.0; //weight of cargo already on drone
        double currentMealWeight; //weight of the current meal

        ArrayList<Order> fifoDeliveryResults = new ArrayList<>();
        simulationTime = 0.0;

        //adds all orders to fifo queue
        for(Order order : simulationOrders)
            fifoDeliveries.add(new Order(order));

        //runs delivery routes while there are orders to be delivered
        while(!fifoDeliveries.isEmpty()) {
            // load up drone with meals ordered in the past that don't exceed payload capacity
            while (true) {
                Order nextOrder = fifoDeliveries.peek();
                if (nextOrder == null || nextOrder.getTimeOrdered() > simulationTime) break;
                currentMealWeight = nextOrder.getMealOrdered().getTotalWeight();
                if (currentMealWeight + cargoWeight > MAX_CARGO_WEIGHT) break;
                droneCargo.add(fifoDeliveries.remove());
                cargoWeight += currentMealWeight;
            }

            //send drone to make deliveries if drone is full
            if (!droneCargo.isEmpty()) {
                //creates list of destinations the drone will need to visit
                for (Order cargo : droneCargo) {
                    if (!droneDestinations.contains(cargo.getDestination())) {
                        droneDestinations.add(cargo.getDestination());
                    }
                }

                //calculates the most optimal route given the drone's list of destinations
                droneRoute = new Route(droneDestinations);
                droneDestinations = droneRoute.getRoute();

                //delivers orders to specified destinations using calculated route
                simulationTime += SECONDS_TO_RECHARGE;  // three minutes to load the drone
                makeDeliveries(droneDestinations);

                // mark the delivery time for each order
                droneCargo.forEach(order -> order.setTimeDelivered(simulationTime));
                fifoDeliveryResults.addAll(droneCargo);
                droneCargo.clear();

                // reset the weight since everything was delivered
                cargoWeight = 0.0;
            } else {
                // increment the time by one second because no deliveries were available
                simulationTime++;
            }
        }

        return fifoDeliveryResults;
    }

    /**
     * Method that has the drone make deliveries to order destinations
     * @author  Isabella Patnode and Christian Burns
     * @param route  the route the drone is to take
     */
    public void makeDeliveries(LinkedList<Point> route) {
        Point currentLocation = null;
        double seconds;

        // fly to each unique point on the route
        while (!route.isEmpty()) {
            Point nextPoint = route.removeFirst();
            if (currentLocation == null) {
                seconds = nextPoint.distanceFromPoint(null) / MAX_SPEED_FEET_PER_SEC;
                simulationTime += seconds + SECONDS_TO_DELIVER; // flight time + 30 seconds for delivery
            } else if (!currentLocation.equals(nextPoint)) {
                seconds = nextPoint.distanceFromPoint(currentLocation) / MAX_SPEED_FEET_PER_SEC;
                simulationTime += seconds + SECONDS_TO_DELIVER; // flight time + 30 seconds for delivery
            }
            //System.out.println(String.format("[%.1f elapsed] Flying to %s", simulationTime, nextPoint.getName()));
            // TODO: here is where we will set delivery time when determined by drop off
            //       droneOrders::getOrder().setDeliveryTime(simulationTime);
            currentLocation = nextPoint;
        }

        // fly back to the origin
        if (currentLocation != null) {
            // add the number of seconds it takes to fly back to the origin
            simulationTime += currentLocation.distanceFromPoint(null) / MAX_SPEED_FEET_PER_SEC;
            //System.out.println(String.format("[%.1f elapsed] Flying back to origin.\n", simulationTime));
        }
    }

    /**
     * Generates a list of random orders to be used for this trial.
     * @author  Isabella Patnode and Christian Burns
     * @return  list of orders with their creation times relative
     *          to the start of the simulation in seconds.
     */
    private ArrayList<Order> generateOrders() {
        ArrayList<Order> orders = new ArrayList<>();
        int hour, mealsPerHour, mealNum, creationTime;
        int hourCount = simulationFlow.size();

        // generates a list of random order times according to the given stochastic flow
        for (hour = 0; hour < hourCount; hour++) {
            // number of meals to be generated in specific hour
            mealsPerHour = simulationFlow.get(hour);

            // generates each order time for all orders in each hour slot
            for (mealNum = 0; mealNum < mealsPerHour; mealNum++) {
                // calculates time of order using given hour (i.e. first hour, second hour, etc.)
                creationTime = (numberGenerator.nextInt(SECONDS_PER_HOUR) + 1) + (SECONDS_PER_HOUR * hour);
                orders.add(new Order(getRandomMeal(), creationTime, simulationPoints.getRandomPoint()));
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

        double [] mealProbabilities = new double[simulationMeals.size()];    //array of meal probabilities
        double upperBound;  //upper bound of possible ranges for random double
        int index; //index for loops and identifying randomly selected meal

        for (index = 0; index < simulationMeals.size(); index++){  //get probabilities
            mealProbabilities[index] = simulationMeals.get(index).getProbability();
        }

        double meal = numberGenerator.nextDouble();    //get decimal between 0.0 and 1.0

        upperBound = 0.0;
        for (index = 0; index < mealProbabilities.length; index++){
            upperBound += mealProbabilities[index];  //set upperBound to previous value + probability
            if (meal < upperBound){     //index i is the randomly selected meal
                break;
            }
        }

        return simulationMeals.get(index);
    }
}
