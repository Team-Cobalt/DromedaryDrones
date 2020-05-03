package com.dromedarydrones.location;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointTest {

    private Point point;

    @Before
    public void setUp(){
        point = new Point("", 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNameInConstructor(){
        new Point(null, 0,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNameInSetter(){
        point.setName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCoordinatesWeird(){
        point.setCoordinates("((((1     ,1((()  -909   ) ( 1000000");
        assertEquals(1, point.getX());
        assertEquals(1, point.getY());
    }

    @Test(expected = NullPointerException.class)
    public void testNullCoordinate(){
        point.setCoordinates(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingleCoordinate(){
        point.setCoordinates("1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTwoCoordinatesNoComma(){
        point.setCoordinates("2   2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonNumberCoordinates(){
        point.setCoordinates("Are you threatening me, Master Jedi?");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommasFirst(){
        point.setCoordinates(",,,,,,1,2");
    }

}