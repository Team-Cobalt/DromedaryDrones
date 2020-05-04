package com.dromedarydrones.food;

import com.dromedarydrones.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * Class pertaining to the creation of Food items
 * @author Izzy Patnode and Christian Burns
 *
 */
public class FoodItem implements XmlSerializable {

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
	 * Constructor for FoodItem class
	 * @param name the name of the food item
	 * @param weight the weight of the food item
	 */
	public FoodItem(String name, double weight) throws IllegalArgumentException {
		if(name == null)
			throw new IllegalArgumentException("Name cannot be null.");

		if(weight <= 0.0)
			throw new IllegalArgumentException("Weight cannot be less than or equal to 0.");

		this.name = name;
		this.weight = weight;
	}

	/**
	 * Copy constructor to create a deep copy of
	 * an existing food item
	 * @param other  other food item to copy
	 */
	public FoodItem(FoodItem other) {
		this.name = other.name;
		this.weight = other.weight;
	}

	public FoodItem(Element root) {
		name = root.getAttribute("name");
		weight = Double.parseDouble(root.getAttribute("weight"));
	}
	
	/**
	 * Updates the name of the current food item
	 * @param name the new name of the food item
	 */
	public void setName(String name) throws IllegalArgumentException {
		if(name == null)
			throw new IllegalArgumentException("Name cannot be null.");

		this.name = name;
	}
	
	/**
	 * Updates the weight of the current food item
	 * @param weight the new weight of the food item
	 */
	public void setWeight(double weight) throws IllegalArgumentException {
		if(weight <= 0.0)
			throw new IllegalArgumentException("Weight cannot be less than or equal to 0.");

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

	@Override
	public Element toXml(Document document) {
		Element root = document.createElement("fooditem");
		root.setAttribute("name", name);
		root.setAttribute("weight", String.valueOf(weight));
		return root;
	}

	@Override
	public String toString() {
		return name.toLowerCase();
	}

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        FoodItem foodItem = (FoodItem) other;
        return name.equals(foodItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
	
}
