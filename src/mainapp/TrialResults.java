package mainapp;

import food.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Results class to hold information from one specific trial.
 * @author  Christian Burns
 */
public class TrialResults {

    private ArrayList<Order> fifoDeliveries;
    private ArrayList<Entry> fifoTimes;
    private double averageFifoTime;
    private int worstFifoTime;

    private ArrayList<Order> knapsackDeliveries;
    private ArrayList<Entry> knapsackTimes;
    private double averageKnapsackTime;
    private int worstKnapsackTime;

    /**
     * Default constructor to compile the results of a single simulation trial.
     * @param fifoDeliveries   order results of running the fifo simulation
     * @param knapsackDeliveries  order results of running the knapsack simulation
     */
    public TrialResults(ArrayList<Order> fifoDeliveries, ArrayList<Order> knapsackDeliveries) {

        averageFifoTime = 0;
        averageKnapsackTime = 0;
        fifoTimes = new ArrayList<>();
        knapsackTimes = new ArrayList<>();
        worstFifoTime = Integer.MIN_VALUE;
        worstKnapsackTime = Integer.MIN_VALUE;
        this.fifoDeliveries = fifoDeliveries;
        this.knapsackDeliveries = knapsackDeliveries;
        HashMap<Integer, Integer> _fifoTimes = new HashMap<>();
        HashMap<Integer, Integer> _knapsackTimes = new HashMap<>();

        Collections.sort(fifoDeliveries);
        Collections.sort(knapsackDeliveries);

        for (Order order : fifoDeliveries) {
            _fifoTimes.put(order.getWaitTime(), _fifoTimes.getOrDefault(order.getWaitTime(), 0) + 1);
            averageFifoTime += order.getWaitTime();
            worstFifoTime = Math.max(worstFifoTime, order.getTimeOrdered());
        }
        averageFifoTime /= fifoDeliveries.size();

        for (Order order : knapsackDeliveries) {
            _knapsackTimes.put(order.getWaitTime(), _knapsackTimes.getOrDefault(order.getWaitTime(), 0) + 1);
            averageKnapsackTime += order.getWaitTime();
            worstKnapsackTime = Math.max(worstKnapsackTime, order.getTimeOrdered());
        }
        averageKnapsackTime /= knapsackDeliveries.size();

        _fifoTimes.forEach((key, value) -> fifoTimes.add(new Entry(key, value)));
        _knapsackTimes.forEach((key, value) -> knapsackTimes.add(new Entry(key, value)));
    }

    /**
     * Returns the fifo orders.
     */
    public ArrayList<Order> getFifoDeliveries() {
        return fifoDeliveries;
    }

    /**
     * Returns the knapsack orders.
     */
    public ArrayList<Order> getKnapsackDeliveries() {
        return knapsackDeliveries;
    }

    /**
     * Returns a list of wait times and the number of orders with the wait times for fifo
     */
    public ArrayList<Entry> getFifoTimes() {
        return fifoTimes;
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
    public int getWorstFifoTime() {
        return worstFifoTime;
    }

    /**
     * Returns a list of wait times and the number of orders with the wait times for knapsack
     */
    public ArrayList<Entry> getKnapsackTimes() {
        return knapsackTimes;
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
    public int getWorstKnapsackTime() {
        return worstKnapsackTime;
    }
}
