package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.FoodItem;
import com.dromedarydrones.food.Meal;
import com.dromedarydrones.location.DeliveryPoints;
import com.dromedarydrones.xml.XmlFactory;
import com.dromedarydrones.xml.XmlSerializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * A standalone configuration of a simulation containing
 * meals, food items, delivery points, and drone settings.
 * @author  Christian Burns and Izzy Patnode
 */
public class Simulation implements XmlSerializable, Callable<SimulationResults> {

	private String simulationName;          // name of the simulation
    private ArrayList<Integer> stochasticFlow;   //stochastic flow for simulation
    private Drone droneSettings;
    private ArrayList<FoodItem> foodItems;  // all known food items
    private ArrayList<Meal> mealTypes;      // all known meals
    private DeliveryPoints deliveryPoints;  // all known delivery points
    private static final int NUMBER_OF_TRIALS = 50;
    private static final int NUMBER_OF_SHIFTS = 4;

    /**
     * Creates a new simulation configuration with the specified name.
     * @author Christian Burns
     * @param name  name of the new configuration
     */
    public Simulation(String name) throws IllegalArgumentException {
        if(name == null)
            throw new IllegalArgumentException("Name cannot be null.");

        simulationName = name;
        stochasticFlow = new ArrayList<>();
        foodItems = new ArrayList<>();
        mealTypes = new ArrayList<>();
        deliveryPoints = new DeliveryPoints();
        droneSettings = new Drone();
    }

    /**
     * Load Simulation from an XML object.
     * @author  Christian Burns
     * @param root  element containing simulation data
     */
    public Simulation(Element root) {
        simulationName = root.getAttribute("name");
        foodItems = new ArrayList<>();
        mealTypes = new ArrayList<>();
        stochasticFlow = new ArrayList<>();
        int index; //loop variable

        NodeList stochasticNodeList = root.getElementsByTagName("stochastic");
        NodeList foodItemNodeList = root.getElementsByTagName("fooditems");
        NodeList mealTypeNodeList = root.getElementsByTagName("mealtypes");
        NodeList deliveryPointNodeList = root.getElementsByTagName("deliverypoints");
        NodeList droneSettingsNodeList = root.getElementsByTagName("drone");

        // load stochastic values
        if (stochasticNodeList.getLength() > 0) {
            Element stochasticRoot = (Element) stochasticNodeList.item(0);
            int hourIndex = 0;
            NodeList stochasticHourNodes = stochasticRoot.getElementsByTagName("hour" + hourIndex);
            while (stochasticHourNodes.getLength() > 0) {
                Element stochasticHour = (Element) stochasticHourNodes.item(0);
                stochasticFlow.add(Integer.parseInt(stochasticHour.getAttribute("orders")));
                stochasticHourNodes = stochasticRoot.getElementsByTagName(String.format("hour%d", ++hourIndex));
            }
        }
        else {
            System.err.println(String.format("simulation \"%s\" missing the \"stochastic\" element", simulationName));
        }

        // load drone settings
        if (droneSettingsNodeList.getLength() > 0) {
            Element droneSettingsRoot = (Element) droneSettingsNodeList.item(0);
            droneSettings = new Drone(droneSettingsRoot);
        }
        else {
            droneSettings = new Drone();
            System.err.println(String.format("simulation \"%s\" missing the \"drone\" element", simulationName));
        }

        // load food items
        if (foodItemNodeList.getLength() > 0) {
            Element foodItemRoot = (Element) foodItemNodeList.item(0);
            NodeList foodChildren = foodItemRoot.getElementsByTagName("fooditem");
            for (index = 0; index < foodChildren.getLength(); index++)
                foodItems.add(new FoodItem((Element) foodChildren.item(index)));
        }
        else {
            System.err.println(String.format("simulation \"%s\" missing the \"fooditems\" element", simulationName));
        }

        // load meal items
        if (mealTypeNodeList.getLength() > 0) {
            Element mealTypeRoot = (Element) mealTypeNodeList.item(0);
            NodeList mealChildren = mealTypeRoot.getElementsByTagName("meal");
            for (index = 0; index < mealChildren.getLength(); index++) {
                Element mealChild = (Element) mealChildren.item(index);
                String mealName = mealChild.getAttribute("name");
                double mealProb = Double.parseDouble(mealChild.getAttribute("probability"));
                ArrayList<FoodItem> mealFoodItems = new ArrayList<>();

                // load foods within the meal
                NodeList mealFoods = mealChild.getChildNodes();
                for (int foodIndex = 0; foodIndex < mealFoods.getLength(); foodIndex++) {
                    if (mealFoods.item(foodIndex).getNodeType() == Node.ELEMENT_NODE) {
                        Element mealFood = (Element) mealFoods.item(foodIndex);
                        String foodName = mealFood.getTagName();
                        int amount = Integer.parseInt(mealFood.getTextContent());
                        FoodItem food = foodItems.stream()
                                .filter(fi -> XmlFactory.toXmlTag(fi.getName()).equals(foodName))
                                .findFirst().orElse(null);
                        if (food != null) {
                            for (int counter = 0; counter < amount; counter++)
                                mealFoodItems.add(food);
                        }
                    }
                }
                mealTypes.add(new Meal(mealFoodItems, mealName, mealProb));
            }
        }
        else {
            System.err.println(String.format("simulation \"%s\" missing the \"mealtypes\" element", simulationName));
        }

        // load delivery points
        if (deliveryPointNodeList.getLength() > 0) {
            Element deliveryPointRoot = (Element) deliveryPointNodeList.item(0);
            deliveryPoints = new DeliveryPoints(deliveryPointRoot);
        }
        else {
            deliveryPoints = new DeliveryPoints();
            System.err.println(String.format("simulation \"%s\" missing the " +
                    "\"deliverypoints\" element", simulationName));
        }
    }

    /**
     * Runs the simulation and returns the result.
     * @author Christian Burns
     */
    @Override
    public SimulationResults call() {
        // create and load an executor service
        ExecutorService service = Executors.newFixedThreadPool(3);
        List<Callable<TrialResults>> tasks = new ArrayList<>();
        for (int index = 0; index < NUMBER_OF_TRIALS; index++)
            tasks.add(() -> new Trial(this).run());

        ArrayList<TrialResults> results = new ArrayList<>();

        try {
            // collect all the results
            List<Future<TrialResults>> futures = service.invokeAll(tasks);
            for (Future<TrialResults> result : futures) {
                try {
                    results.add(result.get());
                }
                catch (ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        }
        catch (CancellationException | InterruptedException ignore) {
        }
        finally {
            service.shutdown();
        }

        return new SimulationResults(results);
    }

    /**
     * Returns the drone settings.
     * @author Christian Burns
     */
    public Drone getDroneSettings() {
        return droneSettings;
    }

    /**
     * Adds specified food item to the list of known foods
     * @author Christian Burns
     * @param food  food item to be added
     */
    public void addFoodItem(FoodItem food) throws IllegalArgumentException {
        if(food == null)
            throw new IllegalArgumentException("Food item cannot be null.");

        if (!foodItems.contains(food))
            foodItems.add(food);
    }

    /**
     * Adds specified food items to the list of known foods
     * @author Christian Burns
     * @param foods  list of food items
     */
    public void addFoodItems(FoodItem... foods) {
        for (FoodItem food : foods) addFoodItem(food);
    }

    /**
     * Adds specified meal to ArrayList of known meals
     * @author Christian Burns
     * @param meal  the meal to be added
     */
    public void addMealType(Meal meal) throws IllegalArgumentException {
        if(meal == null)
            throw new IllegalArgumentException("Meal cannot be null.");

        if (!mealTypes.contains(meal))
            mealTypes.add(meal);
    }

    /**
     * Adds specified meal types to the list of known meals
     * @author Christian Burns
     * @param meals  list of meal types
     */
    public void addMealTypes(Meal... meals) {
        for (Meal meal : meals) addMealType(meal);
    }

    /**
     * Returns a list of all known meals
     * @author  Christian Burns
     */
    public ArrayList<Meal> getMealTypes() {
        return new ArrayList<>(mealTypes);
    }

    /**
     * Makes specified stochastic flow the model for current simulation
     * @author Izzy Patnode
     * @param numberMeals the number of meals per hour for each hour
     * @throws IllegalArgumentException  if number of hours per shift is not 4
     */
    public void addStochasticFlow(List<Integer> numberMeals) throws IllegalArgumentException {
        if(numberMeals == null)
            throw new IllegalArgumentException("numberMeals cannot be null.");

        //throws exception if number of hours per shift is not 4
        if(numberMeals.size() != NUMBER_OF_SHIFTS)
            throw new IllegalArgumentException("Number of hours per shift must be 4");

        //copies over number of meals per hour
        this.stochasticFlow = new ArrayList<>(numberMeals);
    }

    /**
     * Method to get the simulation's stochastic flow
     * @author Izzy Patnode
     * @return the simulation's stochastic flow model
     */
    public ArrayList<Integer> getStochasticFlow() {
        return stochasticFlow;
    }


    /**Method to get list of simulation's delivery points
     * @author Izzy Patnode
     * @return the simulation's list of delivery points
     */
    public DeliveryPoints getDeliveryPoints() {
        return deliveryPoints;
    }

    /**
     * Specifies a new collection of delivery points to use.
     * @author Christian Burns
     */
    public void setDeliveryPoints(DeliveryPoints other) throws IllegalArgumentException {
        if(other == null)
            throw new IllegalArgumentException("DeliveryPoints cannot be null.");

        deliveryPoints = new DeliveryPoints(other);
    }

    /**Method to get list of simulation's food items
     * @author Rachel Franklin
     * @return the simulation's list of food items available for javafx
     */
    public ObservableList<FoodItem> getFoodItems() {
        return FXCollections.observableList(foodItems);
    }

    /**
     * Creates a brand new food item with the specified name and weight.
     * @author Christian Burns
     * @param name    name of the new food
     * @param weight  weight of the food in ounces
     * @return        the newly created food item
     * @throws IllegalArgumentException  if the food already exists
     */
      public FoodItem createFoodItem(String name, double weight) {
          FoodItem food = new FoodItem(name, weight);
          if (foodItems.contains(food))
              throw new IllegalArgumentException("food item " + name + " already exists");
          foodItems.add(food);
          return food;
      }

    /**
     * DO WE NEED THIS RETURN????
     * Removes the specified food item from the simulation.
     * @author Christian Burns
     * @param food  food item to be removed
     * @return  {@code true} if the item was removed.
     *          {@code false} if the item did not exist.
     * @see Simulation#getFoodItem(String name)
     */
    public boolean removeFoodItem(FoodItem food) {
        for(Meal meal: mealTypes) {
            while(meal.getFoods().contains(food)) {
                meal.removeItem(food);
            }
        }
        return foodItems.remove(food);
    }

    /**
     * Retrieves an existing food item by name.
     * @author Christian Burns
     * @param name  name of the food
     * @return  the food item or null if not found
     */
    public FoodItem getFoodItem(String name) {
        return foodItems.stream().filter(food -> food.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * DO WE NEED THIS????
     * Creates a brand new meal type with the specified probability.
     * @author Christian Burns
     * @param probability  probability of occurrence
     * @return  the newly created meal
     * @throws IllegalArgumentException  if the meal type already exists
     */
    public Meal createMeal(ArrayList<FoodItem> mealFoods, String name, double probability) {
        Meal type = new Meal(mealFoods, name, probability);
        if(mealTypes.contains(type)) {
            throw new IllegalArgumentException(("Meal type " + name + " already exists"));
        }

        mealTypes.add(type);
        return type;
    }

    /**
     * Removes the specified meal type from the simulation.
     * @author Christian Burns
     * @param meal  meal to be removed
     * @return  {@code true} if the meal was removed.
     *          {@code false} if the meal did not exist.
     */
    public boolean removeMeal(Meal meal) {
        return mealTypes.remove(meal);
    }

    @Override
    public Element toXml(Document doc) {
        Element root = doc.createElement("simulation");
        root.setAttribute("name", simulationName);

        Element stochasticElement = doc.createElement("stochastic");
        for (int index = 0; index < stochasticFlow.size(); index++) {
            Element hour = doc.createElement("hour" + index);
            hour.setAttribute("orders", String.valueOf(stochasticFlow.get(index)));
            stochasticElement.appendChild(hour);
        }

        Element foods = doc.createElement("fooditems");
        for (FoodItem food : foodItems) foods.appendChild(food.toXml(doc));

        Element meals = doc.createElement("mealtypes");
        for (Meal meal : mealTypes) meals.appendChild(meal.toXml(doc));

        root.appendChild(stochasticElement);
        root.appendChild(droneSettings.toXml(doc));
        root.appendChild(foods);
        root.appendChild(meals);
        root.appendChild(deliveryPoints.toXml(doc));
        return root;
    }

    @Override
    public String toString() {
        return simulationName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Simulation that = (Simulation) other;
        return simulationName.equals(that.simulationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simulationName);
    }
}
