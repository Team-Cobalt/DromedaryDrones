package com.dromedarydrones.location;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

public class RouteTest {

    private Route route;
    private LinkedList<Point> points;

    @Before
    public void setUp() {
        //Point origin = new Point("Origin", 0, 0);
        Point one = new Point("One", 1, 1);

        points = new LinkedList<>();
        //points.add(origin);
        points.add(one);
        route = new Route(points);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPointListInConstructor(){
        new Route(null);
    }

    @Test
    public void testCalculateRouteDFS() {
        LinkedList<Point> copy = new LinkedList<>(points);
        assertEquals(route.getRoute(), copy);
    }
}