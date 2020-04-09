package mainapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import location.DeliveryPoints;
import food.FoodItem;
import food.Meal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xml.XmlFactory;
import xml.XmlSerializable;

/**
 * A standalone configuration of a simulation containing
 * meals, food items, and delivery points.
 * @author  Christian Burns and Isabella Patnode
 */
public class Simulation implements XmlSerializable {

	private String simulationName;          // name of the simulation
    private ArrayList<Integer> stocFlow;   //stochastic flow for simulation
    private ArrayList<FoodItem> foodItems;  // all known food items
    private ArrayList<Meal> mealTypes;      // all known meals
    private DeliveryPoints deliveryPoints;  // all known delivery points

    /**
     * Creates a new simulation configuration with the specified name.
     * @author Christian Burns
     * @param name  name of the new configuration
     */
    public Simulation(String name) {
        simulationName = name;
        stocFlow = new ArrayList<>();
        foodItems = new ArrayList<>();
        mealTypes = new ArrayList<>();
        deliveryPoints = new DeliveryPoints();
    }

    /**
     * Copy constructor to duplicate an existing simulation. All data
     * is deep copied so modifying this new configuration will not
     * change the data within the copied configuration.
     * @author Christian Burns
     * @param other  existing configuration to clone
     */
    public Simulation(Simulation other) {
        this.simulationName = other.simulationName;

        //copies stochastic flow of existing simulation
        this.stocFlow = new ArrayList<>();
        this.stocFlow.addAll(other.stocFlow);

        //copies known foods from existing simulation
        this.foodItems = new ArrayList<>();
        for (FoodItem food : other.foodItems) {
            this.foodItems.add(new FoodItem(food));
        }

        //copies known meals from existing simulation
        this.mealTypes = new ArrayList<>();
        for (Meal meal : other.mealTypes) {
            ArrayList<FoodItem> foods = new ArrayList<>();
            for (FoodItem food : meal.getFoods()) {
                foods.add(getFoodItem(food.getName()));
            }

            Meal newType = new Meal(foods, meal.getName(), meal.getProbability());
            mealTypes.add(newType);
        }

        //copies known delivery points from existing simulation
        this.deliveryPoints = new DeliveryPoints(other.deliveryPoints);
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
        stocFlow = new ArrayList<>();

        NodeList stochasticNodeList = root.getElementsByTagName("stochastic");
        NodeList foodItemNodeList = root.getElementsByTagName("fooditems");
        NodeList mealTypeNodeList = root.getElementsByTagName("mealtypes");
        NodeList deliveryPointNodeList = root.getElementsByTagName("deliverypoints");

        // load stochastic values
        if (stochasticNodeList.getLength() > 0) {
            Element stochasticRoot = (Element) stochasticNodeList.item(0);
            int hourIndex = 0;
            NodeList stochasticHourNodes = stochasticRoot.getElementsByTagName("hour" + hourIndex);
            while (stochasticHourNodes.getLength() > 0) {
                Element stochasticHour = (Element) stochasticHourNodes.item(0);
                stocFlow.add(Integer.parseInt(stochasticHour.getAttribute("orders")));
                stochasticHourNodes = stochasticRoot.getElementsByTagName(String.format("hour%d", ++hourIndex));
            }
        } else {
            System.err.println(String.format("simulation \"%s\" missing the \"stochastic\" element", simulationName));
        }

        // load food items
        if (foodItemNodeList.getLength() > 0) {
            Element foodItemRoot = (Element) foodItemNodeList.item(0);
            NodeList foodChildren = foodItemRoot.getElementsByTagName("fooditem");
            for (int i = 0; i < foodChildren.getLength(); i++)
                foodItems.add(new FoodItem((Element) foodChildren.item(i)));
        } else {
            System.err.println(String.format("simulation \"%s\" missing the \"fooditems\" element", simulationName));
        }

        // load meal items
        if (mealTypeNodeList.getLength() > 0) {
            Element mealTypeRoot = (Element) mealTypeNodeList.item(0);
            NodeList mealChildren = mealTypeRoot.getElementsByTagName("meal");
            for (int i = 0; i < mealChildren.getLength(); i++) {
                Element mealChild = (Element) mealChildren.item(i);
                String mealName = mealChild.getAttribute("name");
                double mealProb = Double.parseDouble(mealChild.getAttribute("probability"));
                ArrayList<FoodItem> mealFoodItems = new ArrayList<>();

                // load foods within the meal
                NodeList mealFoods = mealChild.getChildNodes();
                for (int f = 0; f < mealFoods.getLength(); f++) {
                    if (mealFoods.item(f).getNodeType() == Node.ELEMENT_NODE) {
                        Element mealFood = (Element) mealFoods.item(f);
                        String foodName = mealFood.getTagName();
                        int amount = Integer.parseInt(mealFood.getTextContent());
                        FoodItem food = foodItems.stream()
                                .filter(fi -> XmlFactory.toXmlTag(fi.getName()).equals(foodName))
                                .findFirst().orElse(null);
                        if (food != null) {
                            for (int a = 0; a < amount; a++)
                                mealFoodItems.add(food);
                        }
                    }
                }
                mealTypes.add(new Meal(mealFoodItems, mealName, mealProb));
            }
        } else {
            System.err.println(String.format("simulation \"%s\" missing the \"mealtypes\" element", simulationName));
        }

        // load delivery points
        if (deliveryPointNodeList.getLength() > 0) {
            Element deliveryPointRoot = (Element) deliveryPointNodeList.item(0);
            deliveryPoints = new DeliveryPoints(deliveryPointRoot);
        } else {
            deliveryPoints = new DeliveryPoints();
            System.err.println(String.format("simulation \"%s\" missing the \"deliverypoints\" element", simulationName));
        }
    }

    /**
     * Runs the simulation and returns the results.
     */
    public SimulationResults run() {
        ArrayList<TrialResults> trialResults = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Trial trial = new Trial(mealTypes, stocFlow, deliveryPoints);
            trialResults.add(trial.run());
        }
        return new SimulationResults(trialResults);
    }

    /**
     * Returns the name of the simulation state.
     * @author Christian Burns
     */
    public String getName() {
        return simulationName;
    }

    /**
     * Changes the name of the simulation state to the one specified.
     * @author  Christian Burns
     * @param name  new name to use
     */
    public void setName(String name) {
        simulationName = name;
    }

    /**
     * Adds specified food item to the list of known foods
     * @author Christian Burns
     * @param food  food item to be added
     */
    public void addFoodItem(FoodItem food) {
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
    public void addMealType(Meal meal) {
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
     * @author Isabella Patnode
     * @param numMeals the number of meals per hour for each hour
     * @throws IllegalArgumentException  if number of hours per shift is not 4
     */
    public void addStochasticFlow(List<Integer> numMeals) {
        //throws exception if number of hours per shift is not 4
        if(numMeals.size() != 4) {
            throw new IllegalArgumentException("Number of hours per shift must be 4");
        }

        //copies over number of meals per hour
        this.stocFlow = new ArrayList<>(numMeals);
    }

    /**
     * Method to get the simulation's stochastic flow
     * @author Isabella Patnode
     * @return the simulation's stochastic flow model
     */
    public ArrayList<Integer> getStochasticFlow() {
        return stocFlow;
    }

    /**Method to get list of simulation's delivery points
     * @author Isabella Patnode
     * @return the simulation's list of delivery points
     */
    public DeliveryPoints getDeliveryPoints() {
        return deliveryPoints;
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
     * Removes the specified food item from the simulation.
     * @author Christian Burns
     * @param food  food item to be removed
     * @return  {@code true} if the item was removed.
     *          {@code false} if the item did not exist.
     * @see Simulation#getFoodItem(String name)
     */
    public boolean removeFoodItem(FoodItem food) {
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
        Element stocElement = doc.createElement("stochastic");
        for (int i = 0; i < stocFlow.size(); i++) {
            Element hr = doc.createElement("hour"+i);
            hr.setAttribute("orders", String.valueOf(stocFlow.get(i)));
            stocElement.appendChild(hr);
        }
        Element foods = doc.createElement("fooditems");
        for (FoodItem food : foodItems) foods.appendChild(food.toXml(doc));
        Element meals = doc.createElement("mealtypes");
        for (Meal meal : mealTypes) meals.appendChild(meal.toXml(doc));
        root.appendChild(stocElement);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Simulation that = (Simulation) o;
        return simulationName.equals(that.simulationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simulationName);
    }
}
