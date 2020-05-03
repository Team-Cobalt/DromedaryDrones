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
        Order orderCopy = new Order(order);

        assertEquals(orderCopy.getMealOrdered(), order.getMealOrdered());
        assertEquals(orderCopy.getDestination(), order.getDestination());
        assertEquals(orderCopy.getTimeOrdered(), order.getTimeOrdered(), 0);
        assertEquals(orderCopy.getTimeDelivered(), order.getTimeDelivered(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeTimeInSetter(){
        order.setTimeDelivered(-1);
    }
}
