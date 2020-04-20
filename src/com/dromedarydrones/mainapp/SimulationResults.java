package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.Order;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Results class containing the overview of all simulation trial results.
 * @author  Christian Burns
 */
public class SimulationResults {

    private ArrayList<TrialResults> trialResults;

    private double averageFifoTime;
    private double worstFifoTime;

    private double averageKnapsackTime;
    private double worstKnapsackTime;

    /**
     * Default constructor for compiling together
     * all the results from each simulation trial.
     * @param trialResults  list of all trial results
     */
    public SimulationResults(ArrayList<TrialResults> trialResults) {

        averageFifoTime = 0.0;
        averageKnapsackTime = 0.0;
        worstFifoTime = Double.MIN_VALUE;
        worstKnapsackTime = Double.MIN_VALUE;
        this.trialResults = trialResults;

        for (TrialResults result : trialResults) {
            averageFifoTime += result.getAverageFifoTime();
            averageKnapsackTime += result.getAverageKnapsackTime();
            worstFifoTime = Math.max(worstFifoTime, result.getWorstFifoTime());
            worstKnapsackTime = Math.max(worstKnapsackTime, result.getWorstKnapsackTime());
        }

        averageFifoTime /= trialResults.size();
        averageKnapsackTime /= trialResults.size();
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
     * Returns the overall worst knapsack wait time.
     */
    public double getWorstKnapsackTime() {
        return worstKnapsackTime;
    }

}
