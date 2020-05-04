package com.dromedarydrones.mainapp;

import org.junit.Before;
import org.junit.Test;

public class SimulationTest {

    private Simulation simulation;

    @Before
    public void setUp() {
        simulation = new Simulation("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNameInConstructor(){
        new Simulation((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullFoodItem() {
        simulation.addFoodItem(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMealType() {
        simulation.addMealType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullStochasticFlow() {
        simulation.addStochasticFlow(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDeliveryPoints() {
        simulation.setDeliveryPoints(null);
    }

}