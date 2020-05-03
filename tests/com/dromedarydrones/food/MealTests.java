package com.dromedarydrones.food;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Brendan Ortmann
 */
public class MealTests {

    private Meal meal;

    @Before
    public void setUp() {
        meal = new Meal();
    }

    @Test
    public void testDefaultConstructor(){
        assertTrue("Incorrect default food list.", meal.getFoods().isEmpty());
        assertTrue("Incorrect default name.", meal.getName().isBlank());
        assertEquals("Incorrect default probability.", 0.0, meal.getProbability(), 0.0);
        assertEquals("Incorrect default weight.", 0.0, meal.getTotalWeight(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullFoodListInConstructor(){
        new Meal(null, "", 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNameInConstructor(){
        new Meal(new ArrayList<>(), null, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeProbabilityInConstructor(){
        new Meal(new ArrayList<>(), "", -0.9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeProbabilityInSetter(){
        meal.setProbability(-1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNameInSetter(){
        meal.setName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAddFood(){
        meal.addItem(null);
    }

}
