package com.dromedarydrones.food;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Brendan Ortmann
 *
 * Throughout, throw IllegalArgumentException where an object *could* take on a value we don't want it to.
 * Otherwise, throw NullPointerException when an object cannot be null as limited by the compiler.
 */
public class FoodItemTests {

    private FoodItem food, pizza;

    @Before
    public void setUp() {
        food = new FoodItem();
        pizza = new FoodItem("Large Pizza", 96.0);
    }

    @Test
    public void testDefaultConstructor(){
        assertEquals("Incorrect default name.", "", food.getName());
        assertEquals("Incorrect default weight.", 0.0, food.getWeight(), 0);
    }

    @Test
    public void testCopyConstructor(){
        assertEquals("Copy constructor failed.", new FoodItem(food), food);
    }

    @Test
    public void testParamConstructor(){
        String name = "Large Pizza";
        double weight = 96.0;

        assertEquals("Incorrect parameterized name.", name, pizza.getName());
        assertEquals("Incorrect parameterized weight.", weight, pizza.getWeight(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNameInConstructor(){
        new FoodItem(null, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeWeightInConstructor(){
        new FoodItem("", -9.6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNameInSetter(){
        food.setName(null);
    }

    @Test
    public void testXmlConstructor(){
        // How to test?
    }

    @Test
    public void testEquals(){
        FoodItem copy = new FoodItem("Large Pizza", 96.0);
        FoodItem badCopy = new FoodItem("SWEEWSEWES", 9.0);

        assertEquals("Incorrect comparison with true copy.", copy, pizza);
        assertNotEquals("Incorrect comparison with different object.", badCopy, pizza);
    }

}
