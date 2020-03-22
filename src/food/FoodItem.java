package food;

/**
 * Class pertaining to the creation of Food items
 * @author Isabella Patnode
 *
 */
public class FoodItem {
	private String name; //name of the food item
	private double weight; //weight of food item
	
	/**
	 * Default constructor for FoodItem class
	 */
	public FoodItem() {
		name = "";
		weight = 0.0;
	}
	
	/**
	 * Copy constructor for FoodItem class
	 * @param name the name of the food item
	 * @param weight the weight of the food item
	 */
	public FoodItem(String name, double weight) {
		this.name = name;
		this.weight = weight;
	}
	
	/**
	 * Updates the name of the current food item
	 * @param name the new name of the food item
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Updates the weight of the current food item
	 * @param weight the new weight of the food item
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	/**
	 * Method to get the name of a food item
	 * @return the name of the current food item
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Method to get the weight of a food item
	 * @return the weight of the current food item
	 */
	public double getWeight() {
		return weight;
	}
	
}
