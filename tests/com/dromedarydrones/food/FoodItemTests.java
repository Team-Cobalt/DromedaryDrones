package com.dromedarydrones.food;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Brendan Ortmann
 */
public class FoodItemTests {

    /**
     *
     */
    @Test
    public void checkDefaultConstructor(){
        FoodItem food = new FoodItem();
        Assert.assertEquals("Incorrect default name.", "", food.getName());
        Assert.assertEquals("Incorrect default weight.", 0.0, food.getWeight(), 0);
    }

    @Test
    public void checkParamConstructor(){
        String name = "Large Pizza";
        double weight = 96.0;
        FoodItem food = new FoodItem(name, weight);

        Assert.assertEquals("Incorrect parameterized name.", name, food.getName());
        Assert.assertEquals("Incorrect parameterized weight.", weight, food.getWeight(), 0);
    }



}
