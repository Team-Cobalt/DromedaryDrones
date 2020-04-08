package mainapp;

import food.FoodItem;
import food.Meal;
import food.Order;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import xml.XmlFactory;
import xml.XmlSerializationException;
import xml.XmlSerializable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Master configuration class to track of each separate saved
 * simulation state/configuration. Controls saving and loading
 * configuration save data and swapping between saved simulations.
 * @author  Christian Burns
 */
public class Configuration implements XmlSerializable {

    /* ensures only one instance of the configuration class exists */
    private static final Configuration INSTANCE = new Configuration();
    /** Returns an instance of the {@link Configuration} class. */
    public static Configuration getInstance() { return INSTANCE; }

	private Simulation currentSim; // the current simulation state
	private ArrayList<Simulation> simulations; // all saved simulation states

    /**
     * Default constructor for internal use.
     * @see Configuration#getInstance()
     */
	private Configuration() {
		currentSim = null;
		simulations = new ArrayList<>();
	}

    /**
     * Retrieves the simulation state by its name.
     * @param name  name of the simulation state
     * @return  the simulation state or null if not found
     */
    public Simulation getSimulation(String name) {
        for (Simulation sim : simulations)
            if (sim.getName().equals(name))
                return sim;
        return null;
    }

    /**
     * Returns the simulation state currently being used.
     */
    public Simulation getCurrentSimulation() {
        return currentSim;
    }

    /**
     * Changes the simulation state currently in use.
     * @param simulation  new current simulation
     */
    public void setCurrentSimulation(Simulation simulation) {
        if (!simulations.contains(simulation)) simulations.add(simulation);
        currentSim = simulation;
    }

    /**
     * Adds a new simulation to be saved and loaded.
     * @param simulation  new simulation, not null
     */
    public void addSimulation(Simulation simulation) {
        if (!simulations.contains(simulation))
            simulations.add(simulation);
        else throw new IllegalArgumentException(
                "simulation " + simulation.getName() + " already exists");
    }

    /**
     * Deletes the specified simulation from the save data.
     * @param simulation  simulation to be deleted
     * @return  {@code true} if the simulation state was removed.
     *          {@code false} if the simulation didn't exist or was
     *          currently the active simulation.
     */
    public boolean removeSimulation(Simulation simulation) {
        if (currentSim.equals(simulation)) return false;
        return simulations.remove(simulation);
    }

    /**
     * Loads in all simulations from the specified save file.
     * @param saveFile  save file containing simulation configuration data
     */
	public void initialize(File saveFile) throws FileNotFoundException {
	    simulations.clear();

	    if (saveFile == null || !saveFile.exists()) {

	        //creates default simulation with all default food items and meal types
            Simulation newSim = new Simulation("Grove City College");

            //sets up default hour specifications based on default stochastic flow
            ArrayList<Integer> ordersPerHour = new ArrayList<>();
            ordersPerHour.add(15);
            ordersPerHour.add(17);
            ordersPerHour.add(22);
            ordersPerHour.add(15);

            //sets up stochastic flow in new simulation
            newSim.addStochasticFlow(ordersPerHour);

            //creates default food items with default weights
            FoodItem burger = new FoodItem("Burger", 6);
            FoodItem fries = new FoodItem("Fries", 4);
            FoodItem drink = new FoodItem("Drink", 14);

            //sets up known food items in simulation
            newSim.addFoodItem(burger);
            newSim.addFoodItem(fries);
            newSim.addFoodItem(drink);

            //creates default basic combo meal type
            ArrayList<FoodItem> basic = new ArrayList<>();
            basic.add(burger);
            basic.add(fries);
            basic.add(drink);
            Meal basicCombo = new Meal(basic, "Basic Combo", 0.55);

            //adds meal to known meals in simulation
            newSim.addMealType(basicCombo);

            //creates default deluxe combo meal
            ArrayList<FoodItem> deluxe = new ArrayList<>();
            deluxe.add(burger);
            deluxe.add(burger);
            deluxe.add(fries);
            deluxe.add(drink);
            Meal deluxeCombo= new Meal(deluxe, "Deluxe Combo", 0.10);

            //adds meal to known meals in simulation
            newSim.addMealType(deluxeCombo);

            //creates default basic combo w/o drink meal
            ArrayList<FoodItem> basicNoDrk = new ArrayList<>();
            basicNoDrk.add(burger);
            basicNoDrk.add(fries);
            Meal basicNoDrink = new Meal(basicNoDrk, "Basic No Drink", 0.2);

            //adds meal to known meals in simulation
            newSim.addMealType(basicNoDrink);

            //creates default deluxe combo w/o drink meal
            ArrayList<FoodItem> deluxeNoDrk = new ArrayList<>();
            deluxeNoDrk.add(burger);
            deluxeNoDrk.add(burger);
            deluxeNoDrk.add(fries);
            Meal deluxeNoDrink = new Meal(deluxeNoDrk, "Deluxe No Drink", 0.15);

            //adds meal to known meals in simulation
            newSim.addMealType(deluxeNoDrink);

            //sets the default simulation as the current simulation to run
            currentSim = newSim;
            simulations.add(newSim);

        } else {
	        // read all XML out of the save file
	        StringBuilder sb = new StringBuilder();
	        try (Scanner scnr = new Scanner(saveFile)) {
	            while (scnr.hasNextLine())
	                sb.append(scnr.nextLine().trim());
            }

	        // convert the text into an XML Document
	        String xmlString = sb.toString();
	        Document doc = XmlFactory.fromXmlString(xmlString);
	        Element root = doc.getDocumentElement();

	        String currentName = root.getAttribute("current");
            NodeList children = root.getElementsByTagName("simulation");

            // build the simulations from the XML Document data
            for (int i = 0; i < children.getLength(); i++) {
                Element child = (Element) children.item(i);
                Simulation sim = new Simulation(child);
                simulations.add(sim);
                if (sim.getName().equals(currentName))
                    currentSim = sim;
            }
        }
	}

    /**
     * Saves the simulation results to a CSV file.
     * @param results  instance of the simulation results
     * @param file     file to save the results to
     * @return  {@code true} if it was able to be saved
     */
	public boolean saveResults(SimulationResults results, File file) {
	    try {
            if (file.exists() || file.createNewFile()) {
                // collect all the results
                StringBuilder builder = new StringBuilder();

                // format the headers
                String title = "\n,Fifo,Knapsack\n";
                String average = String.format("Average (sec),%.2f,%.2f\n",
                        results.getAverageFifoTime(), results.getAverageKnapsackTime());
                String worst = String.format("Worst (sec),%.2f,%.2f\n",
                        results.getWorstFifoTime(), results.getWorstKnapsackTime());
                String header = ",,Fifo,,,,,Knapsack\n";
                String columns = ",meal,ordered,delivered,wait (sec),,meal,ordered,delivered,wait (sec)\n";

                // add the headers
                builder.append(title).append(average).append(worst)
                        .append("\n\n").append(header).append(columns);

                // print the results of each trial
                int trialNum, orderNum, minSize, fifoSize, knapSize;
                Order fifoOrder, knapOrder;
                String trialInfo, fifoName, knapName;
                double fifoTimeOrdered, fifoTimeDelivered, fifoWaitTime;
                double knapTimeOrdered, knapTimeDelivered, knapWaitTime;
                int numTrials = results.getTrialResults().size();
                ArrayList<TrialResults> trialResults = results.getTrialResults();

                // for each of the 50 trials...
                for (trialNum = 0; trialNum < numTrials; trialNum++) {

                    TrialResults trial = trialResults.get(trialNum);
                    ArrayList<Order> fifoOrders = trial.getFifoDeliveries();
                    ArrayList<Order> knapOrders = trial.getKnapsackDeliveries();
                    fifoSize = fifoOrders.size();
                    knapSize = knapOrders.size();
                    minSize = Math.min(fifoOrders.size(), knapOrders.size());

                    // for each order shared between the two sets of orders
                    for (orderNum = 0; orderNum < minSize; orderNum++) {
                        fifoOrder = fifoOrders.get(orderNum);
                        knapOrder = knapOrders.get(orderNum);

                        trialInfo = orderNum == 0 ? "trial " + (trialNum + 1) : "";
                        fifoName = fifoOrder.getMealOrdered().getName();
                        fifoTimeOrdered = fifoOrder.getTimeOrdered();
                        fifoTimeDelivered = fifoOrder.getTimeDelivered();
                        fifoWaitTime = fifoOrder.getWaitTime();

                        knapName = knapOrder.getMealOrdered().getName();
                        knapTimeOrdered = knapOrder.getTimeOrdered();
                        knapTimeDelivered = knapOrder.getTimeDelivered();
                        knapWaitTime = knapOrder.getWaitTime();

                        String row = String.format("%s,%s,%.2f,%.2f,%.2f,,%s,%.2f,%.2f,%.2f\n", trialInfo,
                                fifoName, fifoTimeOrdered, fifoTimeDelivered, fifoWaitTime,
                                knapName, knapTimeOrdered, knapTimeDelivered, knapWaitTime);

                        builder.append(row);
                    }

                    // for each order that exists in fifo but not knapsack
                    for (; orderNum < fifoSize; orderNum++) {
                        fifoOrder = fifoOrders.get(orderNum);

                        trialInfo = orderNum == 0 ? "trial " + (trialNum + 1) : "";
                        fifoName = fifoOrder.getMealOrdered().getName();
                        fifoTimeOrdered = fifoOrder.getTimeOrdered();
                        fifoTimeDelivered = fifoOrder.getTimeDelivered();
                        fifoWaitTime = fifoOrder.getWaitTime();

                        String row = String.format("%s,%s,%.2f,%.2f,%.2f\n", trialInfo,
                                fifoName, fifoTimeOrdered, fifoTimeDelivered, fifoWaitTime);

                        builder.append(row);
                    }

                    // for each order that exists in knapsack but not fifo
                    for (; orderNum < knapSize; orderNum++) {
                        knapOrder = knapOrders.get(trialNum);

                        trialInfo = orderNum == 0 ? "trial " + (trialNum + 1) : "";
                        knapName = knapOrder.getMealOrdered().getName();
                        knapTimeOrdered = knapOrder.getTimeOrdered();
                        knapTimeDelivered = knapOrder.getTimeDelivered();
                        knapWaitTime = knapOrder.getWaitTime();

                        String row = String.format("%s,,,,,,%s,%.2f,%.2f,%.2f\n", trialInfo,
                                knapName, knapTimeOrdered, knapTimeDelivered, knapWaitTime);

                        builder.append(row);
                    }

                    builder.append("\n");
                }

                // write the data to the file
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println(builder.toString());
                }
                return false;
            }
        } catch (IOException ioe) {
	        ioe.printStackTrace();
        }
	    return false;
    }

    /**
     * Saves all simulation states to the specified file.
     * @param saveFile  save file to save the data to
     * @return  {@code true} if all data was successfully saved
     *          {@code false} if the data was unable to be parsed or saved
     * @throws FileNotFoundException  if the save file did not exist
     */
	public boolean saveConfigs(File saveFile) throws FileNotFoundException {
        try (PrintWriter pw = new PrintWriter(saveFile)) {
            String xmlSaveData = XmlFactory.toXmlString(this);
            pw.println(xmlSaveData);
            return true;
        } catch (XmlSerializationException xse) {
            xse.printStackTrace();
            return false;
        }
    }

    @Override
    public Element toXml(Document doc) {
        Element root = doc.createElement("simulations");
        root.setAttribute("current", currentSim.getName());
        for (Simulation sim : simulations) root.appendChild(sim.toXml(doc));
        return root;
    }
}
