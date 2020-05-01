package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.FoodItem;
import com.dromedarydrones.food.Meal;
import com.dromedarydrones.food.Order;
import com.dromedarydrones.xml.XmlFactory;
import com.dromedarydrones.xml.XmlSerializable;
import com.dromedarydrones.xml.XmlSerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    // TODO this will need to be switched to an external directory if this project becomes a jar
    /** location of the file that tracks the location of the last used safe file */
    public static final String LOCATIONS_PATH = "locations.txt";

    /* ensures the configuration instance exists as a singleton */
    private static final Configuration INSTANCE = new Configuration();
    /** Returns an instance of the {@link Configuration} class. */
    public static Configuration getInstance() { return INSTANCE; }

	private Simulation currentSimulation; // the loaded simulation state

    /**
     * Default constructor for internal use.
     * @see Configuration#getInstance()
     */
	private Configuration() {
		currentSimulation = null;
	}

    public void initialize() {
        initialize(getLastConfigFile());
    }

    /**
     * Loads the simulation config from the specified file if
     * possible. Otherwise uses the default configuration.
     * @param file  XML save file containing the configuration
     * @author      Christian Burns
     */
    public void initialize(File file) {
        currentSimulation = getConfigFromFile(file);
        if (currentSimulation == null)
            currentSimulation = getDefaultConfig();
    }

    /** returns the simulation configuration currently loaded. */
    public Simulation getCurrentSimulation() {
        return currentSimulation;
    }

    /**
     * Builds a simulation instance from the contents of an XML file.
     *
     * @param file  save file to retrieve the configuration from
     * @return      new instance of the configuration or null
     * @author      Christian Burns
     */
    public static Simulation getConfigFromFile(File file) {
        if (file != null && file.exists()) {
            try {
                // read all XML out of the save file
                StringBuilder sb = new StringBuilder();
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNextLine())
                        sb.append(scanner.nextLine().trim());
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace(); // this should never occur
                    return null;
                }

                // convert the text into an XML Document and
                // build the sim instance from the root element
                Document doc = XmlFactory.fromXmlString(sb.toString());
                return new Simulation(doc.getDocumentElement());
            } catch (XmlSerializationException xmle) {
                xmle.printStackTrace();
            }
        }
        return null;
    }

    /** Returns the default simulation configuration of Grove City College */
    public static Simulation getDefaultConfig() {
        //creates default simulation with all default food items and meal types
        Simulation simulation = new Simulation("Grove City College");

        // sets default #orders per hour for each of the four hours
        simulation.addStochasticFlow(List.of(38, 45, 60, 30));

        // create default food items
        FoodItem burger = new FoodItem("Burger", 6);
        FoodItem fries = new FoodItem("Fries", 4);
        FoodItem drink = new FoodItem("Drink", 14);

        // add default food items to the simulation
        simulation.addFoodItems(burger, fries, drink);

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
        simulation.addMealTypes(basicCombo, deluxeCombo, basicNoDrink, deluxeNoDrink, singleFries);

        return simulation;
    }

    /**
     * Returns the File object of the last used configuration.
     * @author Christian Burns
     */
    public File getLastConfigFile() {
        File locationFile = new File(LOCATIONS_PATH);
        File configurationFile;
        String path = "";
        try {
            // ensure the locations file exists before reading from it
            if (locationFile.exists() || locationFile.createNewFile()) {
                // start reading from the locations file
                try (Scanner reader = new Scanner(locationFile)) {
                    // put the first line with text into path
                    while (reader.hasNextLine() && path.isEmpty())
                        path = reader.nextLine().strip();
                }
            }
        } catch (IOException ioe) {
            return null;
        }
        // return the file if it exists otherwise return null
        if (path.isEmpty()) return null;

        configurationFile = new File(path);
        return configurationFile.exists() ? configurationFile : null;
    }

    /**
     * Specifies which configuration file will be loaded in
     * the next time the program starts.
     * @author Christian Burns
     */
    public void setLastConfigFile(File configFile) throws IOException {
        File locationFile = new File(LOCATIONS_PATH);
        if (locationFile.exists() || locationFile.createNewFile()) {
            try (PrintWriter writer = new PrintWriter(locationFile)) {
                writer.println(configFile.getCanonicalPath());
            }
        }
    }

    /**
     * Saves the simulation results to a CSV file.
     * @param results  instance of the simulation results
     * @param file     file to save the results to
     */
	public void saveResults(SimulationResults results, File file) {
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
                    List<Order> fifoOrders = trial.getFifoDeliveries();
                    List<Order> knapsackOrders = trial.getKnapsackDeliveries();
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
            }
        } catch (IOException ioException) {
	        ioException.printStackTrace();
        }
    }

    /**
     * Saves all simulation states to the specified file.
     * @param saveFile  save file to save the data to
     * @throws FileNotFoundException  if the save file did not exist
     */
	public void saveConfigs(File saveFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(saveFile)) {
            String xmlSaveData = XmlFactory.toXmlString(this);
            pw.println(xmlSaveData);
            setLastConfigFile(saveFile);
        } catch (XmlSerializationException xmlException) {
            xmlException.printStackTrace();
        }
    }

    @Override
    public Element toXml(Document doc) {
        return currentSimulation.toXml(doc);
    }
}
