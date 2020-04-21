package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.FoodItem;
import com.dromedarydrones.food.Meal;
import com.dromedarydrones.food.Order;
import com.dromedarydrones.xml.XmlFactory;
import com.dromedarydrones.xml.XmlSerializable;
import com.dromedarydrones.xml.XmlSerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

    public static final String LOCATIONS_PATH = "locations.txt";

    /* ensures only one instance of the configuration class exists */
    private static final Configuration INSTANCE = new Configuration();
    /** Returns an instance of the {@link Configuration} class. */
    public static Configuration getInstance() { return INSTANCE; }

	private Simulation currentSimulation; // the current simulation state
	private ArrayList<Simulation> simulations; // all saved simulation states

    /**
     * Default constructor for internal use.
     * @see Configuration#getInstance()
     */
	private Configuration() {
		currentSimulation = null;
		simulations = new ArrayList<>();
	}

    /**
     * Retrieves the simulation state by its name.
     * @param name  name of the simulation state
     * @return  the simulation state or null if not found
     */
    public Simulation getSimulation(String name) {
        for (Simulation simulation : simulations)
            if (simulation.getName().equals(name))
                return simulation;
        return null;
    }

    /**
     * Returns the simulation state currently being used.
     */
    public Simulation getCurrentSimulation() {
        return currentSimulation;
    }

    /**
     * Changes the simulation state currently in use.
     * @param simulation  new current simulation
     */
    public void setCurrentSimulation(Simulation simulation) {
        if (!simulations.contains(simulation)) simulations.add(simulation);
        currentSimulation = simulation;
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
        if (currentSimulation.equals(simulation)) return false;
        return simulations.remove(simulation);
    }

    public void initialize() throws IOException {
        File saveFile = getLastConfigFile();    // fetch the last config file
        initialize(saveFile);
    }

    /**
     * Loads in all simulations from the save file.
     */
	public void initialize(File saveFile) throws IOException {
	    simulations.clear();        // flush any old settings before loading in the new ones

	    if (saveFile == null) {     // no config file was found
	        loadDefault();
        } else {
	        try {
                // read all XML out of the save file
                StringBuilder sb = new StringBuilder();
                try (Scanner scanner = new Scanner(saveFile)) {
                    while (scanner.hasNextLine())
                        sb.append(scanner.nextLine().trim());
                }

                // convert the text into an XML Document
                String xmlString = sb.toString();
                Document doc = XmlFactory.fromXmlString(xmlString);
                Element root = doc.getDocumentElement();

                String currentName = root.getAttribute("current");
                NodeList children = root.getElementsByTagName("simulation");

                // build the simulations from the XML Document data
                for (int index = 0; index < children.getLength(); index++) {
                    Element child = (Element) children.item(index);
                    Simulation simulation = new Simulation(child);
                    simulations.add(simulation);
                    if (simulation.getName().equals(currentName))
                        currentSimulation = simulation;
                }
            } catch (XmlSerializationException xmlException) {
	            xmlException.printStackTrace();
	            loadDefault();
            }
        }
	}

	private void loadDefault() {
        //creates default simulation with all default food items and meal types
        Simulation newSimulation = new Simulation("Grove City College");

        // sets default #orders per hour for each of the four hours
        newSimulation.addStochasticFlow(List.of(38, 45, 60, 30));

        // create default food items
        FoodItem burger = new FoodItem("Burger", 6);
        FoodItem fries = new FoodItem("Fries", 4);
        FoodItem drink = new FoodItem("Drink", 14);

        // add default food items to the simulation
        newSimulation.addFoodItems(burger, fries, drink);

        //creates default basic combo meal type
        List<FoodItem> basic = List.of(burger, fries, drink);
        Meal basicCombo = new Meal(basic, "Basic Combo", 0.50);

        //creates default deluxe combo meal
        List<FoodItem> deluxe = List.of(burger, burger, fries, drink);
        Meal deluxeCombo= new Meal(deluxe, "Deluxe Combo", 0.20);

        //creates default basic combo w/o drink meal
        List<FoodItem> noDrinkBasic = List.of(burger, fries);
        Meal basicNoDrink = new Meal(noDrinkBasic, "Basic with No Drink", 0.15);

        //creates default deluxe combo w/o drink meal
        List<FoodItem> noDrinkDeluxe = List.of(burger, burger, fries);
        Meal deluxeNoDrink = new Meal(noDrinkDeluxe, "Deluxe with No Drink", 0.10);

        //creates default one fries meal
        List<FoodItem> oneFries = List.of(fries);
        Meal singleFries = new Meal(oneFries, "Single Fries", 0.05);

        // add default meal types to the simulation
        newSimulation.addMealTypes(basicCombo, deluxeCombo, basicNoDrink, deluxeNoDrink, singleFries);

        //sets the default simulation as the current simulation to run
        currentSimulation = newSimulation;
        simulations.add(newSimulation);
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
                int trialNumber, orderNumber, minSize, fifoSize, knapsackSize;
                Order fifoOrder, knapsackOrder;
                String trialInfo, fifoName, knapsackName;
                double fifoTimeOrdered, fifoTimeDelivered, fifoWaitTime;
                double knapsackTimeOrdered, knapsackTimeDelivered, knapsackWaitTime;
                int numberTrials = results.getTrialResults().size();
                ArrayList<TrialResults> trialResults = results.getTrialResults();

                // for each of the 50 trials...
                for (trialNumber = 0; trialNumber < numberTrials; trialNumber++) {

                    TrialResults trial = trialResults.get(trialNumber);
                    ArrayList<Order> fifoOrders = trial.getFifoDeliveries();
                    ArrayList<Order> knapsackOrders = trial.getKnapsackDeliveries();
                    fifoSize = fifoOrders.size();
                    knapsackSize = knapsackOrders.size();
                    minSize = Math.min(fifoOrders.size(), knapsackOrders.size());

                    // for each order shared between the two sets of orders
                    for (orderNumber = 0; orderNumber < minSize; orderNumber++) {
                        fifoOrder = fifoOrders.get(orderNumber);
                        knapsackOrder = knapsackOrders.get(orderNumber);

                        trialInfo = orderNumber == 0 ? "trial " + (trialNumber + 1) : "";
                        fifoName = fifoOrder.getMealOrdered().getName();
                        fifoTimeOrdered = fifoOrder.getTimeOrdered();
                        fifoTimeDelivered = fifoOrder.getTimeDelivered();
                        fifoWaitTime = fifoOrder.getWaitTime();

                        knapsackName = knapsackOrder.getMealOrdered().getName();
                        knapsackTimeOrdered = knapsackOrder.getTimeOrdered();
                        knapsackTimeDelivered = knapsackOrder.getTimeDelivered();
                        knapsackWaitTime = knapsackOrder.getWaitTime();

                        String row = String.format("%s,%s,%.2f,%.2f,%.2f,,%s,%.2f,%.2f,%.2f\n", trialInfo,
                                fifoName, fifoTimeOrdered, fifoTimeDelivered, fifoWaitTime,
                                knapsackName, knapsackTimeOrdered, knapsackTimeDelivered, knapsackWaitTime);

                        builder.append(row);
                    }

                    // for each order that exists in fifo but not knapsack
                    for (; orderNumber < fifoSize; orderNumber++) {
                        fifoOrder = fifoOrders.get(orderNumber);

                        trialInfo = orderNumber == 0 ? "trial " + (trialNumber + 1) : "";
                        fifoName = fifoOrder.getMealOrdered().getName();
                        fifoTimeOrdered = fifoOrder.getTimeOrdered();
                        fifoTimeDelivered = fifoOrder.getTimeDelivered();
                        fifoWaitTime = fifoOrder.getWaitTime();

                        String row = String.format("%s,%s,%.2f,%.2f,%.2f\n", trialInfo,
                                fifoName, fifoTimeOrdered, fifoTimeDelivered, fifoWaitTime);

                        builder.append(row);
                    }

                    // for each order that exists in knapsack but not fifo
                    for (; orderNumber < knapsackSize; orderNumber++) {
                        knapsackOrder = knapsackOrders.get(trialNumber);

                        trialInfo = orderNumber == 0 ? "trial " + (trialNumber + 1) : "";
                        knapsackName = knapsackOrder.getMealOrdered().getName();
                        knapsackTimeOrdered = knapsackOrder.getTimeOrdered();
                        knapsackTimeDelivered = knapsackOrder.getTimeDelivered();
                        knapsackWaitTime = knapsackOrder.getWaitTime();

                        String row = String.format("%s,,,,,,%s,%.2f,%.2f,%.2f\n", trialInfo,
                                knapsackName, knapsackTimeOrdered, knapsackTimeDelivered, knapsackWaitTime);

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
        } catch (IOException ioException) {
	        ioException.printStackTrace();
        }
	    return false;
    }

    /**
     * Returns the File object of the last used configuration.
     * @throws IOException  if the program doesn't have permissions
     *                      to create new files
     */
    public File getLastConfigFile() throws IOException {
	    File locationFile = new File(LOCATIONS_PATH);
	    File configurationFile;
	    String path = "";
	    // ensure the locations file exists before reading from it
	    if (locationFile.exists() || locationFile.createNewFile()) {
	        // start reading from the locations file
	        try (Scanner reader = new Scanner(locationFile)) {
	            // put the first line with text into path
                while (reader.hasNextLine() && path.isEmpty())
                    path = reader.nextLine().strip();
            }
        }
	    // return the file if it exists otherwise return null
	    if (path.isEmpty()) return null;

	    configurationFile = new File(path);
	    return configurationFile.exists() ? configurationFile : null;
    }

    public void setLastConfigFile(File configFile) throws IOException {
        File locationFile = new File(LOCATIONS_PATH);
        String path = configFile.getCanonicalPath();
        if (locationFile.exists() || locationFile.createNewFile()) {
            try (PrintWriter writer = new PrintWriter(locationFile)) {
                writer.println(path);
            }
        }
    }

    /**
     * Saves all simulation states to the specified file.
     * @param saveFile  save file to save the data to
     * @return  {@code true} if all data was successfully saved
     *          {@code false} if the data was unable to be parsed or saved
     * @throws FileNotFoundException  if the save file did not exist
     */
	public boolean saveConfigs(File saveFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(saveFile)) {
            String xmlSaveData = XmlFactory.toXmlString(this);
            pw.println(xmlSaveData);
            setLastConfigFile(saveFile);
            return true;
        } catch (XmlSerializationException xmlException) {
            xmlException.printStackTrace();
            return false;
        }
    }

    @Override
    public Element toXml(Document doc) {
        Element root = doc.createElement("simulations");
        root.setAttribute("current", currentSimulation.getName());

        for (Simulation simulation : simulations) root.appendChild(simulation.toXml(doc));

        return root;
    }
}
