package mainapp;

import java.util.ArrayList;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import location.DeliveryPoints;
import location.Point;
import food.FoodItem;
import food.Meal;

public class Simulation {
	private String simulationName;              // name of the simulation
    private ArrayList<FoodItem> foodItems;      // all known food items
    private ArrayList<Meal> mealTypes;    // all known meals
    private DeliveryPoints deliveryPoints;      // all known delivery points

    public Simulation(String name) {
        simulationName = name;
        foodItems = new ArrayList<>();
        mealTypes = new ArrayList<>();
        deliveryPoints = new DeliveryPoints();
    }
    
    public String getName() {
        return simulationName;
    }
    
    public Element toXml(Document document) {
        // create simulation root element
        Element simulation = document.createElement("simulation");
        Attr simId = document.createAttribute("name");
        simId.setValue(simulationName);
        simulation.setAttributeNode(simId);

        // create simulation child elements
        Element foodItemsElement = document.createElement("fooditems");
        simulation.appendChild(foodItemsElement);
        Element orderTypesElement = document.createElement("ordertypes");
        simulation.appendChild(orderTypesElement);
        Element deliveryPointsElement = document.createElement("deliverypoints");
        simulation.appendChild(deliveryPointsElement);

        for (FoodItem item : foodItems) {
            foodItemsElement.appendChild(item.toXml(document));
        }

        for (Meal type : mealTypes) {
           // orderTypesElement.appendChild(type.toXml(document));
        }

        Attr originPoint = document.createAttribute("origin");
        //originPoint.setValue(deliveryPoints.getOriginName());
        deliveryPointsElement.setAttributeNode(originPoint);
        for (Point pt : deliveryPoints) {
            //deliveryPointsElement.appendChild(pt.toXml(document));
        }

        return simulation;
    }
}
