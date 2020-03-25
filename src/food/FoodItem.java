package food;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * Class pertaining to the creation of Food items
 * @author Isabella Patnode
 *
 */
public class FoodItem {
	private String name; //name of the food item
	private double weight; //weight of food item (in oz.)
	
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
	
	public Element toXml(Document document) {
        Element foodItemElement = document.createElement("fooditem");
        Attr nameAttr = document.createAttribute("name");
        Attr weightAttr = document.createAttribute("weight");
        nameAttr.setValue(name);
        weightAttr.setValue(String.valueOf(weight));
        foodItemElement.setAttributeNode(nameAttr);
        foodItemElement.setAttributeNode(weightAttr);
        return foodItemElement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return name.equals(foodItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
	
}
