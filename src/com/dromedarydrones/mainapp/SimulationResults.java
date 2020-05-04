package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.Order;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Results class containing the overview of all simulation trial results.
 * @author  Christian Burns
 */
public class SimulationResults {

    private final ArrayList<TrialResults> trialResults;

    private double averageFifoExpired;
    private double averageFifoTime;
    private double worstFifoTime;

    private double averageKnapsackExpired;
    private double averageKnapsackTime;
    private double worstKnapsackTime;

    /**
     * Default constructor for compiling together
     * all the results from each simulation trial.
     * @param trialResults  list of all trial results
     */
    public SimulationResults(ArrayList<TrialResults> trialResults) {

        averageFifoTime = 0.0;
        averageFifoExpired = 0.0;
        worstFifoTime = Double.MIN_VALUE;

        averageKnapsackTime = 0.0;
        averageKnapsackExpired = 0.0;
        worstKnapsackTime = Double.MIN_VALUE;

        this.trialResults = trialResults;
        int trialCount = trialResults.size();

        for (TrialResults result : trialResults) {
            averageFifoTime += result.getAverageFifoTime();
            averageFifoExpired += result.numExpiredFifoOrders();
            worstFifoTime = Math.max(worstFifoTime, result.getWorstFifoTime());

            averageKnapsackTime += result.getAverageKnapsackTime();
            averageKnapsackExpired += result.numExpiredKnapsackOrders();
            worstKnapsackTime = Math.max(worstKnapsackTime, result.getWorstKnapsackTime());
        }

        averageFifoTime /= trialCount;
        averageFifoExpired /= trialCount;
        averageKnapsackTime /= trialCount;
    }

    /**
     * Returns the list of all trial results from the simulation
     */
    public ArrayList<TrialResults> getTrialResults() {
        return trialResults;
    }

    /**
     * Returns an observable list of the fifo delivery wait times in seconds.
     */
    public ArrayList<Double> getFifoTimes() {
        ArrayList<Double> waitTimes = new ArrayList<>();
        trialResults.stream().flatMapToDouble(trial ->
                trial.getFifoDeliveries().stream().mapToDouble(Order::getWaitTime))
                .forEach(waitTimes::add);
        Collections.sort(waitTimes);
        return waitTimes;
    }

    /**
     * Returns the overall average fifo wait time.
     */
    public double getAverageFifoTime() {
        return averageFifoTime;
    }

    /**
     * Returns the average percent of fifo orders that had to
     * be remade due to their delivery times exceeding 2 hours.
     * <br>
     * A return value of 0.15 represents 15%
     * @author Christian Burns
     */
    public double getPercentFifoExpired() {
        if (trialResults.size() > 0 && trialResults.get(0).getFifoDeliveries().size() > 0)
            return averageFifoExpired / trialResults.get(0).getFifoDeliveries().size();
        return 0;
    }

    /**
     * Returns the absolute worst fifo wait time.
     */
    public double getWorstFifoTime() {
        return worstFifoTime;
    }

    /**
     * Returns an observable list of the fifo delivery wait times in seconds.
     */
    public ArrayList<Double> getKnapsackTimes() {
        ArrayList<Double> waitTimes = new ArrayList<>();
        trialResults.stream().flatMapToDouble(trial ->
                trial.getKnapsackDeliveries().stream().mapToDouble(Order::getWaitTime))
                .forEach(waitTimes::add);
        Collections.sort(waitTimes);
        return waitTimes;
    }

    /**
     * Returns the overall average knapsack wait time.
     */
    public double getAverageKnapsackTime() {
        return averageKnapsackTime;
    }

    /**
     * Returns the average percent of knapsack orders that had to
     * be remade due to their delivery times exceeding 2 hours.
     * <br>
     * A return value of 0.15 represents 15%
     * @author Christian Burns
     */
    public double getPercentKnapsackExpired() {
        if (trialResults.size() > 0 && trialResults.get(0).getKnapsackDeliveries().size() > 0)
            return averageKnapsackExpired / trialResults.get(0).getKnapsackDeliveries().size();
        return 0;
    }

    /**
     * Returns the overall worst knapsack wait time.
     */
    public double getWorstKnapsackTime() {
        return worstKnapsackTime;
    }

}
