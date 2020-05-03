package com.dromedarydrones.food;

import com.dromedarydrones.location.Point;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderTests {

    Order order;

    @Before
    public void setUp(){
        order = new Order(new Meal(), 0, new Point("", 0,0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullMealInConstructor(){
        new Order(null, 0, new Point("", 0,0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDestinationInConstructor(){
        new Order(new Meal(), 0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeTimeInConstructor(){
        new Order(new Meal(), -1, null);
    }

    @Test // Do I need to test individual member variables??
    public void testCopyConstructor(){
        assertEquals("Copy constructor did not copy correctly.",
                new Order(order), order);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeTimeInSetter(){
        order.setTimeDelivered(-1);
    }
}
