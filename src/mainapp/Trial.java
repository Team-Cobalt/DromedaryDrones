package mainapp;

import food.Meal;
import food.Order;
import location.DeliveryPoints;
import location.Point;
import location.Route;

import java.util.*;

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
    private double simulationTime;

    private static final int MINUTES = 60;
    private static final double MAX_CARGO_WEIGHT = 192;             // 12 pounds in ounces
    public static final double MAX_SPEED_FEET_PER_SEC = 20 * 1.467; // 20 mph -> 29.3333 feet per second

    /**
     * Constructor for creating a single four-hour shift
     * @author Isabella Patnode
     * @param meals current simulation's list of meals to be used
     * @param model current simulation's stochastic flow model
     * @param points current simulation's delivery points to be used
     */
    public Trial(ArrayList<Meal> meals, ArrayList<Integer> model, DeliveryPoints points) {
        simMeals = new ArrayList<>(meals);
        simFlow = new ArrayList<>(model);
        simPoints = new DeliveryPoints(points);
        rand = new Random();
        fifoDeliveries = new LinkedList<>();
        droneCargo = new LinkedList<>();
        droneDestinations = new LinkedList<>();
        simulationTime = 0.0;
        //calculates generates list of orders based on time they are ordered
        simOrders = generateOrders();
    }

    /**
     * TODO: Add Javadoc and comments to this method
     * TODO: Test method
     * @author Brendan Ortmann
     * @return asdf
     */
    public ArrayList<Order> runKnapsackDeliveries(){
        double cargoWeight = 0.0, currentMealWeight;
        ArrayList<Order> knapsackResults = new ArrayList<>(); // Is this needed?
        ArrayList<Order> knapsackDeliveries = new ArrayList<>();
        ArrayList<Order> skippedOrders = new ArrayList<>();
        simulationTime = 0.0;

        for(Order o : simOrders)
            knapsackDeliveries.add(new Order(o));

        knapsackDeliveries.sort((a,b)-> Double.compare(b.getMealOrdered().getTotalWeight(),
                a.getMealOrdered().getTotalWeight())); // Sort the meals in descending order based on weight

        while(!knapsackDeliveries.isEmpty()){
            simulationTime = Math.round(simulationTime);

            Collections.sort(skippedOrders); // Sort skipped orders by time so that older orders get added first
            for(Order s : skippedOrders){ // Query skipped orders first
                if(s.getTimeOrdered() > simulationTime) continue;
                currentMealWeight = s.getMealOrdered().getTotalWeight();
                if(currentMealWeight + cargoWeight > MAX_CARGO_WEIGHT) continue; // If max weight exceeded, ignore
                droneCargo.add(s);
                cargoWeight += currentMealWeight;
                skippedOrders.remove(s);
            }

            for(Order o : knapsackDeliveries){ // Query current set of orders to add to drone
                if(o.getTimeOrdered() > simulationTime) continue;
                currentMealWeight = o.getMealOrdered().getTotalWeight();
                if(currentMealWeight + cargoWeight > MAX_CARGO_WEIGHT){
                    skippedOrders.add(o);
                    knapsackDeliveries.remove(o);
                    continue;
                }
                droneCargo.add(o);
                cargoWeight += currentMealWeight;
                knapsackDeliveries.remove(o);
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
                simulationTime += 3.0;  // three minutes to load the drone
                makeDeliveries(droneCargo, droneDestinations);

                // mark the delivery time for each order
                droneCargo.forEach(order -> order.setTimeDelivered((int) simulationTime));
                //.addAll(droneCargo);
                droneCargo.clear();

                cargoWeight = 0.0;
            } else {
                simulationTime++;
            }

        }

        return knapsackDeliveries;
    }

    /**
     * @author Isabella Patnode
     * Method that uses FIFO to deliver the orders
     * @return  list of orders used in the trial that contain their creation
     *          and delivery times relative to the start of the trial
     */
    public ArrayList<Order> runFifoDeliveries() {
        double cargoWeight = 0.0; //weight of cargo already on drone
        double currentMealWeight; //weight of the current meal
        int index; //loop variable

        ArrayList<Order> fifoDeliveryResults = new ArrayList<>();
        simulationTime = 0.0;

        //adds all orders to fifo queue
        for(Order o : simOrders)
            fifoDeliveries.add(new Order(o));

        //runs delivery routes while there are orders to be delivered
        while(!fifoDeliveries.isEmpty()) {
            simulationTime = Math.round(simulationTime);
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
                for(index = 0; index < droneCargo.size(); index++) {
                    if(!droneDestinations.contains(droneCargo.get(index).getDestination())) {
                        droneDestinations.add(droneCargo.get(index).getDestination());
                    }
                }

                //calculates the most optimal route given the drone's list of destinations
                droneRoute = new Route(droneDestinations);
                droneDestinations = droneRoute.getRoute();

                //delivers orders to specified destinations using calculated route
                simulationTime += 3.0;  // three minutes to load the drone
                makeDeliveries(droneCargo, droneDestinations);

                // mark the delivery time for each order
                droneCargo.forEach(order -> order.setTimeDelivered((int) simulationTime));
                fifoDeliveryResults.addAll(droneCargo);
                droneCargo.clear();

                cargoWeight = 0.0;
            } else {
                simulationTime++;
            }
        }

        return fifoDeliveryResults;
    }

    /** NEED TO TEST!!!!!!!!!!
     * Method that has the drone make deliveries to order destinations
     * @author Isabella Patnode and Christian Burns
     * @param droneOrders the orders currently loaded on the drone
     * @param route the route the drone is to take
     */
    public void makeDeliveries(LinkedList<Order> droneOrders, LinkedList<Point> route) {
        Point currentLocation = null;
        double seconds;
        double minutes;

        // fly to each unique point on the route
        while (!route.isEmpty()) {
            Point nextPoint = route.removeFirst();
            if (currentLocation == null) {
                seconds = nextPoint.distanceFromPoint(null) / MAX_SPEED_FEET_PER_SEC;
                minutes = seconds / 60;             // minutes to fly to the destination
                simulationTime += minutes + 0.5;    // flight time + 30 seconds for delivery
            } else if (!currentLocation.equals(nextPoint)) {
                seconds = nextPoint.distanceFromPoint(currentLocation) / MAX_SPEED_FEET_PER_SEC;
                minutes = seconds / 60;             // minutes to fly to the destination
                simulationTime += minutes + 0.5;    // flight time + 30 seconds for delivery
            }
            //System.out.println(String.format("[%.1f elapsed] Flying to %s", simulationTime, nextPoint.getName()));
            // TODO: here is where we will set delivery time when determined by drop off
            //       droneOrders::getOrder().setDeliveryTime(simulationTime);
            currentLocation = nextPoint;
        }

        // fly back to the origin
        if (currentLocation != null) {
            seconds = currentLocation.distanceFromPoint(null) / MAX_SPEED_FEET_PER_SEC;
            minutes = seconds / 60;
            simulationTime += minutes;
            //System.out.println(String.format("[%.1f elapsed] Flying back to origin.\n", simulationTime));
        }
    }

    /**
     * Randomly generates a list of order times and creates a list of orders based on the times
     * @author Isabella Patnode
     * @return the list of orders and their order times
     */
    public ArrayList<Order> generateOrders() {
        int timeOfOrder;
        int mealsPerHour;
        int index;
        ArrayList<Order> orders = new ArrayList<>();

        //generates a list of random order times according to given stochastic flow
        for(index = 0; index < simFlow.size(); index++) {
            //number of meals to be generated in specific hour
            mealsPerHour = simFlow.get(index);

            //generates each order time for all orders in each hour slot
            for(int mealNum = 0; mealNum < mealsPerHour; mealNum++) {
                //calculates time of order using given hour (i.e. first hour, second hour, etc.)
                timeOfOrder = (rand.nextInt(MINUTES) + 1) + (MINUTES * index);
                orders.add(new Order(getRandomMeal(), timeOfOrder, simPoints.getRandomPoint()));
            }
        }

        //sorts list of order times in increasing order
        Collections.sort(orders);
        return orders;
    }

    /**
     * Method for randomly generating the meal to be ordered based on meal's probability
     * @author Rachel Franklin
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
