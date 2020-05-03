package com.dromedarydrones.location;

import org.junit.Before;
import org.junit.Test;

public class DeliveryPointsTest {
    private DeliveryPoints deliveryPoints;

    @Before
    public void setUp(){
        deliveryPoints = new DeliveryPoints();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullObjectInCopyConstructor(){
        DeliveryPoints dp = null;
        new DeliveryPoints(dp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPointName(){
        deliveryPoints.addPoint(null,0,0);
    }

}