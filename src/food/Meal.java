package food;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xml.XmlFactory;
import xml.XmlSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class pertaining to the creation of meals
 * @author Isabella Patnode
 *
 */
public class Meal implements XmlSerializable {
	private ArrayList<FoodItem> foods; //list of foods in the meal
	private String name; //name of the meal
	private double probability; //probability a customer orders the meal
	private double totalWeight; //the weight of the meal
	private final double DRONE_WEIGHT = 192; //max cargo weight in ounces
	
	/**
	 * Default constructor for Meal class
	 */
	public Meal() {
		foods = new ArrayList<>();
		name = "";
		probability = 0.0;
		totalWeight = 0.0;
	}
	
	/**
	 * Copy constructor for Meal class
	 * @param mealFoods //list of foods that make up the meal
	 * @param name //name of the meal
	 * @param probability //probability a customer orders the meal
	 * @throws IllegalArgumentException if meal weight exceeds drone's cargo weight limit
	 */
	public Meal(List<FoodItem> mealFoods, String name, double probability) {
		double mealWeight = 0.0;
		
		for(FoodItem food: mealFoods) {
			mealWeight += food.getWeight();
		}
		
		if(mealWeight <= DRONE_WEIGHT) {
			//MAKE SURE THIS COPYING DOESN'T RUIN ANYTHING
			foods = new ArrayList<>(mealFoods);
			this.name = name;
			this.probability = probability;
			totalWeight = mealWeight;
		}
		else {
			throw new IllegalArgumentException(name + " exceeds maximum cargo weight");
		}
	}
	
	/**
	 * Method that updates the name of the meal
	 * @param name the name of the meal
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Method that updates the probability of the meal
	 * @param prob new probability of the meal
	 */
	public void setProbability(double prob) {
		this.probability = prob;
	}
	
	/**
	 * Method to get the name of the meal
	 * @return the name of the meal
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Method to get the probability of the meal
	 * @return the probability of the meal
	 */
	public double getProbability() {
		return probability;
	}
	
	/**
	 * Method to get the list of foods in the meal
	 * @return a list of foods in the meal
	 */
	public ArrayList<FoodItem> getFoods() {
		return foods;
	}
	
	/**
	 * Method to get the total weight of the meal
	 * @return the total weight of the meal
	 */
	public double getTotalWeight() {
		return totalWeight;
	}
	
	/**
	 * Method that adds a food item to the meal
	 * @param food the food item to be added to the meal
	 * @throws IllegalArgumentException if food causes weight to exceed 12 pounds
	 */
	public void addItem(FoodItem food) {
		
		if(totalWeight + food.getWeight() <= DRONE_WEIGHT) {
			foods.add(food);
		}
		else { //THROW ERROR
			throw new IllegalArgumentException("Unable to add " + food + " due to weight restrictions");
		}
	
	}
	
	/**
	 * Method that removes a food item from the meal
	 * @param food the food item to be removed from the meal
	 */
	public void removeItem(FoodItem food) {
		if(!foods.isEmpty() && foods.contains(food)) {
			totalWeight -= food.getWeight();
			foods.remove(food);
		}
		else {
			System.out.println("Food not contained in meal");
		}
	}

	@Override
	public Element toXml(Document doc) {
		Element root = doc.createElement("meal");
		root.setAttribute("name", name);
		root.setAttribute("probability", String.valueOf(probability));
		HashMap<FoodItem, Integer> foodQuantities = new HashMap<>();
		for (FoodItem food : foods) {
			foodQuantities.putIfAbsent(food, 0);
			foodQuantities.put(food, foodQuantities.get(food) + 1);
		}
		for (Map.Entry<FoodItem, Integer> entry : foodQuantities.entrySet()) {
			Element foodElem = doc.createElement(XmlFactory.toXmlTag(entry.getKey().getName()));
			foodElem.appendChild(doc.createTextNode(entry.getValue().toString()));
			root.appendChild(foodElem);
		}
		return root;
	}
}
