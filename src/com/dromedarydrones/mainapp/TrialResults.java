package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.Order;

import java.util.Collections;
import java.util.List;

/**
 * Results class to hold information from one specific trial.
 * @author  Christian Burns
 */
public class TrialResults {

    private final List<Order> fifoDeliveries;
    private double averageFifoTime;
    private double worstFifoTime;

    private final List<Order> knapsackDeliveries;
    private double averageKnapsackTime;
    private double worstKnapsackTime;

    /**
     * Default constructor to compile the results of a single simulation trial.
     * @param fifoDeliveries      order results of running the fifo simulation
     * @param knapsackDeliveries  order results of running the knapsack simulation
     */
    public TrialResults(List<Order> fifoDeliveries, List<Order> knapsackDeliveries) {

        averageFifoTime = 0.0;
        averageKnapsackTime = 0.0;
        worstFifoTime = Double.MIN_VALUE;
        worstKnapsackTime = Double.MIN_VALUE;
        this.fifoDeliveries = fifoDeliveries;
        this.knapsackDeliveries = knapsackDeliveries;

        // sort the orders based on their creation times
        Collections.sort(fifoDeliveries);
        Collections.sort(knapsackDeliveries);

        // find worst and average times for Fifo
        for (Order order : fifoDeliveries) {
            averageFifoTime += order.getWaitTime();
            worstFifoTime = Math.max(worstFifoTime, order.getWaitTime());
        }

        // find worst and average times for Knapsack
        for (Order order : knapsackDeliveries) {
            averageKnapsackTime += order.getWaitTime();
            worstKnapsackTime = Math.max(worstKnapsackTime, order.getWaitTime());
        }

        // final averages are the sum / count
        averageFifoTime /= fifoDeliveries.size();
        averageKnapsackTime /= knapsackDeliveries.size();
    }

    /**
     * Returns the fifo orders.
     */
    public List<Order> getFifoDeliveries() {
        return fifoDeliveries;
    }

    /**
     * Returns the knapsack orders.
     */
    public List<Order> getKnapsackDeliveries() {
        return knapsackDeliveries;
    }

    /**
     * Returns the average fifo wait time
     */
    public double getAverageFifoTime() {
        return averageFifoTime;
    }

    /**
     * Returns the worst fifo wait time
     */
    public double getWorstFifoTime() {
        return worstFifoTime;
    }

    /**
     * Returns the average knapsack wait time
     */
    public double getAverageKnapsackTime() {
        return averageKnapsackTime;
    }

    /**
     * Returns the worst knapsack wait time
     */
    public double getWorstKnapsackTime() {
        return worstKnapsackTime;
    }
}
