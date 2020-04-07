package mainapp;

import food.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Results class containing the overview of all simulation trial results.
 * @author  Christian Burns
 */
public class SimulationResults {

    private ArrayList<TrialResults> trialResults;

    private ArrayList<Entry> fifoTimes;
    private double averageFifoTime;
    private int worstFifoTime;

    private ArrayList<Entry> knapsackTimes;
    private double averageKnapsackTime;
    private int worstKnapsackTime;

    /**
     * Default constructor for compiling together
     * all the results from each simulation trial.
     * @param trialResults  list of all trial results
     */
    public SimulationResults(ArrayList<TrialResults> trialResults) {

        averageFifoTime = 0;
        averageKnapsackTime = 0;
        fifoTimes = new ArrayList<>();
        knapsackTimes = new ArrayList<>();
        worstFifoTime = Integer.MIN_VALUE;
        worstKnapsackTime = Integer.MIN_VALUE;
        HashMap<Integer, Entry> _fifoTimes = new HashMap<>();
        HashMap<Integer, Entry> _knapsackTimes = new HashMap<>();
        this.trialResults = trialResults;

        for (TrialResults result : trialResults) {
            averageFifoTime += result.getAverageFifoTime();
            averageKnapsackTime += result.getAverageKnapsackTime();
            worstFifoTime = Math.max(worstFifoTime, result.getWorstFifoTime());
            worstKnapsackTime = Math.max(worstKnapsackTime, result.getWorstKnapsackTime());

            ArrayList<Entry> _trialFifoTimes = result.getFifoTimes();
            ArrayList<Entry> _trialKnapsackTimes = result.getKnapsackTimes();

            for (Entry entry : _trialFifoTimes) {
                _fifoTimes.putIfAbsent(entry.elapsedTime, new Entry(0, 0));
                Entry _entry = _fifoTimes.get(entry.elapsedTime);
                _entry.elapsedTime += entry.deliveryCount;
                _entry.deliveryCount++;
            }

            for (Entry entry : _trialKnapsackTimes) {
                _knapsackTimes.putIfAbsent(entry.elapsedTime, new Entry(0, 0));
                Entry _entry = _knapsackTimes.get(entry.elapsedTime);
                _entry.elapsedTime += entry.deliveryCount;
                _entry.deliveryCount++;
            }
        }

        averageFifoTime /= trialResults.size();
        averageKnapsackTime /= trialResults.size();

        _fifoTimes.forEach((key, value) -> fifoTimes.add(new Entry(key, (int)Math.ceil(value.elapsedTime / (double)value.deliveryCount))));
        _knapsackTimes.forEach((key, value) -> knapsackTimes.add(new Entry(key, (int)Math.ceil(value.elapsedTime / (double)value.deliveryCount))));
        Collections.sort(fifoTimes);
        Collections.sort(knapsackTimes);
    }

    /**
     * Returns the list of all trial results from the simulation
     */
    public ArrayList<TrialResults> getTrialResults() {
        return trialResults;
    }

    /**
     * Returns an observable list of the sorted fifo
     * times representing the bel curve distribution.
     */
    public ObservableList<Entry> getFifoTimes() {
        return FXCollections.observableList(fifoTimes);
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
    public int getWorstFifoTime() {
        return worstFifoTime;
    }

    /**
     * Returns an observable list of the sorted knapsack
     * times representing the bel curve distribution.
     */
    public ObservableList<Entry> getKnapsackTimes() {
        return FXCollections.observableList(knapsackTimes);
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
    public int getWorstKnapsackTime() {
        return worstKnapsackTime;
    }

}
