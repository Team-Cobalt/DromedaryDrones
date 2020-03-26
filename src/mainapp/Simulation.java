package mainapp;

import java.util.ArrayList;
import java.util.Objects;

import location.DeliveryPoints;
import food.FoodItem;
import food.Meal;
import xml.annotations.XmlAttribute;
import xml.annotations.XmlElement;
import xml.annotations.XmlElementList;
import xml.annotations.XmlSerializable;

/**
 * A standalone configuration of a simulation containing
 * meals, food items, and delivery points.
 * @author  Christian Burns
 */
@XmlSerializable
public class Simulation {

    @XmlAttribute(name="name")
	private String simulationName;          // name of the simulation

    @XmlElementList
    private ArrayList<FoodItem> foodItems;  // all known food items

    @XmlElementList
    private ArrayList<Meal> mealTypes;      // all known meals

    @XmlElement
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

    public void addFoodItem(FoodItem food) {
        if (!foodItems.contains(food))
            foodItems.add(food);
    }

    public void addMealType(Meal meal) {
        if (!mealTypes.contains(meal))
            mealTypes.add(meal);
    }

//    /**
//     * Creates a brand new food item with the specified name and weight.
//     * @param name    name of the new food
//     * @param weight  weight of the food in ounces
//     * @return        the newly created food item
//     * @throws IllegalArgumentException  if the food already exists
//     */
//    public FoodItem createFoodItem(String name, int weight) {
//        FoodItem food = new FoodItem(name, weight);
//        if (foodItems.contains(food))
//            throw new IllegalArgumentException(
//                    "food item " + name + " already exists");
//        foodItems.add(food);
//        return food;
//    }

//    /**
//     * Removes the specified food item from the simulation.
//     * @param food  food item to be removed
//     * @return  {@code true} if the item was removed.
//     *          {@code false} if the item did not exist.
//     * @see Simulation#getFoodItem(String name)
//     */
//    public boolean removeFoodItem(FoodItem food) {
//        return foodItems.remove(food);
//    }

    /**
     * Retrieves an existing food item by name.
     * @param name  name of the food
     * @return      the food item or null if not found
     */
    public FoodItem getFoodItem(String name) {
        return foodItems.stream().filter(food -> food.getName().equals(name)).findFirst().orElse(null);
    }

//    /**
//     * Creates a brand new meal type with the specified probability.
//     * @param probability  probability of occurrence
//     * @return  the newly created meal
//     */
//    public Meal createMeal(double probability) {
//        Meal type = new Meal(probability);
//        mealTypes.add(type);
//        return type;
//    }

//    /**
//     * Removes the specified meal type from the simulation.
//     * @param meal  meal to be removed
//     * @return  {@code true} if the meal was removed.
//     *          {@code false} if the meal did not exist.
//     */
//    public boolean removeMeal(Meal meal) {
//        return mealTypes.remove(meal);
//    }

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
