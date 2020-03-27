package mainapp;

import food.FoodItem;
import food.Meal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import xml.XmlFactory;
import xml.XmlSerializationException;
import xml.XmlSerializable;

import java.io.File;
import java.io.FileNotFoundException;
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

            Simulation newSim = new Simulation("Grove City College");
            FoodItem burger = new FoodItem("Burger", 6);
            FoodItem fries = new FoodItem("Fries", 4);
            FoodItem drink = new FoodItem("Drink", 14);
            newSim.addFoodItem(burger);
            newSim.addFoodItem(fries);
            newSim.addFoodItem(drink);
            newSim.addMealType(new Meal(
                    new ArrayList<>(List.of(burger, fries, drink)),
                    "meal1", 0.55));
            newSim.addMealType(new Meal(
                    new ArrayList<>(List.of(burger, burger, fries, drink)),
                    "meal2", 0.1));
            newSim.addMealType(new Meal(
                    new ArrayList<>(List.of(burger, fries)),
                    "meal3", 0.2));
            newSim.addMealType(new Meal(
                    new ArrayList<>(List.of(burger, burger, fries)),
                    "meal4", 0.15));
            currentSim = newSim;
            simulations.add(newSim);

        } else {

	        StringBuilder sb = new StringBuilder();
	        try (Scanner scnr = new Scanner(saveFile)) {
	            while (scnr.hasNextLine())
	                sb.append(scnr.nextLine().trim());
            }
	        String xmlString = sb.toString();
	        Document doc = XmlFactory.fromXmlString(xmlString);
	        Element root = doc.getDocumentElement();

	        String currentName = root.getAttribute("current");

            NodeList children = root.getElementsByTagName("simulation");
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
     * Saves all simulation states to the specified file.
     * @param saveFile  save file to save the data to
     * @return  {@code true} if all data was successfully saved
     *          {@code false} if the data was unable to be parsed or saved
     * @throws FileNotFoundException  if the save file did not exist
     */
	public boolean save(File saveFile) throws FileNotFoundException {
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
