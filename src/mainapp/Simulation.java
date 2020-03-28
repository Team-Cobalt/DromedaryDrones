package mainapp;

import java.util.ArrayList;
import java.util.Objects;

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
 * @author  Christian Burns
 */
public class Simulation implements XmlSerializable {

	private String simulationName;          // name of the simulation
    private ArrayList<FoodItem> foodItems;  // all known food items
    private ArrayList<Meal> mealTypes;      // all known meals
    private DeliveryPoints deliveryPoints;  // all known delivery points

    /**
     * Creates a new simulation configuration with the specified name.
     * @param name  name of the new configuration
     */
    public Simulation(String name) {
        simulationName = name;
        foodItems = new ArrayList<>();
        mealTypes = new ArrayList<>();
        deliveryPoints = new DeliveryPoints();
    }

    /**
     * Copy constructor to duplicate an existing simulation. All data
     * is deep copied so modifying this new configuration will not
     * change the data within the copied configuration.
     * @param other  existing configuration to clone
     */
    public Simulation(Simulation other) {
        this.simulationName = other.simulationName;
        this.foodItems = new ArrayList<>();
        this.mealTypes = new ArrayList<>();
        for (FoodItem food : other.foodItems)
            this.foodItems.add(new FoodItem(food));
        this.mealTypes = new ArrayList<>();
        for (Meal meal : other.mealTypes) {
            ArrayList<FoodItem> foods = new ArrayList<>();
            for (FoodItem food : meal.getFoods())
                foods.add(getFoodItem(food.getName()));
            Meal newType = new Meal(foods, meal.getName(), meal.getProbability());
            mealTypes.add(newType);
        }
        this.deliveryPoints = new DeliveryPoints(other.deliveryPoints);
    }

    /**
     * Load Simulation from an XML object.
     * @param root  element containing simulation data
     */
    public Simulation(Element root) {
        simulationName = root.getAttribute("name");
        foodItems = new ArrayList<>();
        mealTypes = new ArrayList<>();

        // load food items
        Element foods = (Element) root.getElementsByTagName("fooditems").item(0);
        NodeList foodChildren = foods.getElementsByTagName("fooditem");
        for (int i = 0; i < foodChildren.getLength(); i++)
            foodItems.add(new FoodItem((Element) foodChildren.item(i)));

        // load meal items
        Element meals = (Element) root.getElementsByTagName("mealtypes").item(0);
        NodeList mealChildren = meals.getElementsByTagName("meal");
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

        // load delivery points
        Element points = (Element) root.getElementsByTagName("deliverypoints").item(0);
        deliveryPoints = new DeliveryPoints(points);
    }

    /**
     * Returns the name of the simulation state.
     */
    public String getName() {
        return simulationName;
    }

    /**
     * Changes the name of the simulation state to the one specified.
     * @param name  new name to use
     */
    public void setName(String name) {
        simulationName = name;
    }

    /**
     * Adds specified food item to ArrayList of known foods
     * @param food the food item to be added
     */
    public void addFoodItem(FoodItem food) {
        if (!foodItems.contains(food))
            foodItems.add(food);
    }

    /**
     * Adds specified meal to ArrayList of known meals
     * @param meal the meal to be added
     */
    public void addMealType(Meal meal) {
        if (!mealTypes.contains(meal))
            mealTypes.add(meal);
    }

    /**
     * Creates a brand new food item with the specified name and weight.
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
     * @param name  name of the food
     * @return  the food item or null if not found
     */
    public FoodItem getFoodItem(String name) {
        return foodItems.stream().filter(food -> food.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Creates a brand new meal type with the specified probability.
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
        Element foods = doc.createElement("fooditems");
        for (FoodItem food : foodItems) foods.appendChild(food.toXml(doc));
        Element meals = doc.createElement("mealtypes");
        for (Meal meal : mealTypes) meals.appendChild(meal.toXml(doc));
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
