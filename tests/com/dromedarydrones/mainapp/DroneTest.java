package com.dromedarydrones.mainapp;

import org.junit.Before;
import org.junit.Test;

public class DroneTest {

    private Drone drone;

    @Before
    public void setUp() {
        drone = new Drone();
    }

    @Test(expected = NullPointerException.class)
    public void testNullDroneInCopyConstructor(){
        Drone d = null;
        new Drone(d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullOrderList() {
        drone.deliver(null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRouteIsSafe() {
        drone.isSafeFlightTime(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullOrderListIsSafeTime() {
        drone.isEstimatedSafeFlightTime(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullOrdersIsSafePayload() {
        drone.isSafePayloadCapacity(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullOrdersToRoute() {
        drone.ordersToRoute(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMaxPayload() {
        drone.setMaxPayloadWeight(-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCruiseSpeed() {
        drone.setCruisingSpeed(-100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeFlightTime() {
        drone.setFlightTime(-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeTurnaroundTime() {
        drone.setTurnAroundTime(-100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDeliveryTime() {
        drone.setDeliveryTime(-10);
    }
}