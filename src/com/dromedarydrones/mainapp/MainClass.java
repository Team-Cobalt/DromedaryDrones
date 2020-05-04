package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.FoodItem;
import com.dromedarydrones.food.Meal;
import com.dromedarydrones.location.Point;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;
import java.text.DecimalFormat;


/**
 * Class that runs the simulation
 * @author Izzy Patnode, Rachel Franklin, and Christian Burns
 *
 *			COLOR PALETTE
 *	-----------------------------
 * Main Background: 	e0e0e0 (Light Grey)
 * Sidebar Background: 	bdbdbd (Grey)
 * Button Text:			0047ab (Cobalt Blue)
 * Button Outline:		0047ab (Cobalt Blue)
 * GUI Text:			0047ab (Cobalt Blue)
 * Bar Chart:			ae000b (Dark Red) and 0047ab (Cobalt Blue)
 * Map Points:			0047ab (Cobalt Blue)
 *
 */
public class MainClass extends Application {

	private Stage window; //used for creating gui
	private Scene mainMenu; //main menu page
	private StackPane root;
	private Text title; //title of page
	private HBox titleLayout; //layout regarding title of page
	private HBox iconLayout; //layout of home icon
	private VBox buttonLayout; //layout of setting's menu buttons
	private HBox settingLayout; //layout of all elements in setting pages
	private Simulation currentSimulation; //current simulation being run
	private SimulationResults results;	//results of simulation

	private static final int SECONDS_PER_HOUR = 3600;
	private static final int FEET_PER_MILE = 5280;
	private static final int OUNCES_PER_POUND = 16;
	private static final int SECONDS_PER_MINUTE = 60;

	private static final String PRIMARY_BACKGROUND_COLOR = "-fx-background-color: #e0e0e0;";
	private static final String SECONDARY_BACKGROUND_COLOR = "-fx-background-color: #bdbdbd;";
	private static final String BOLD_FONT_STYLE = "-fx-font-weight: bold;";
	private static final String FONT_TYPE = "-fx-font-family: Helvetica;";

	//css string regarding style of line separating sidebar from main page
	private static final String SIDEBAR_STYLE = "-fx-border-style: hidden solid hidden hidden;" +
			"-fx-border-width: 1.25; -fx-border-color: black; -fx-padding: 0 5 7 0";

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private Future<SimulationResults> futureResults;

	public static void main(String[] args) {

		//configuration are the settings which are loaded in
		Configuration configuration = Configuration.getInstance();
		configuration.initialize();

		launch(args);
	}

	/**
	 * Runs the simulation asynchronously so as not to block the UI thread.
	 * Once the simulation finishes, the result is retrieved and the results
	 * page is navigated to.
	 * @author Christian Burns
	 * @throws NullPointerException if no simulation configuration exists
	 */
	private void runSimulation() {

		//simulation to be run is submitted to the executor so it can be run asynchronously with the UI thread
		Simulation activeSimulation = Configuration.getInstance().getCurrentSimulation();
		futureResults = executor.submit(activeSimulation);

		// retrieve the simulation results and transition to the results page
		new Thread(() -> {
			try {
				results = futureResults.get();			// waits for the results to be generated
				Platform.runLater(this::resultsPage);	// calls resultsPage() via the UI thread
			} catch (CancellationException ignore) {
				// this will occur when cancelling a running simulation
			} catch (InterruptedException | ExecutionException exception) {
				exception.printStackTrace();
			}
		}).start();
	}

	/**
	 * Cancels the simulation currently running.
	 * @author Christian Burns
	 */
	private void abortSimulation() {
		futureResults.cancel(true);
	}

	/**
	 * Main screen page where the user can decide what to do (i.e. run simulation, go to settings, etc.)
	 * @author Izzy Patnode
	 */
	@Override
	public void start(Stage primaryStage) {
		window = primaryStage;

		//need to grab the current simulation so we can access the necessary settings values
		currentSimulation = Configuration.getInstance().getCurrentSimulation();

		//allows us to access camel png and add it to the page
		Image image = new Image("file:resources/CuteCamel.png");

		ImageView imageView = new ImageView(image);

		imageView.setX(50);
		imageView.setY(50);

		imageView.setFitHeight(175);
		imageView.setFitWidth(175);

		imageView.setPreserveRatio(true);

		VBox picture = new VBox(15);
		picture.getChildren().add(imageView);
		picture.setAlignment(Pos.TOP_CENTER);

		//front page title is created to welcome users
		title = new Text("Welcome to Dromedary Drones!");
		title.setStyle(FONT_TYPE+ "-fx-font-size: 55; -fx-fill: #0047ab");
		title.setWrappingWidth(500);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox(20);
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.BASELINE_CENTER);

		//buttons for user to user the simulation are placed into VBox for a readable layout
		VBox buttons = new VBox(10);
		buttons.setPrefWidth(100);

		//allows user to run the simulation
		Button buttonStart = new Button("Start Simulation");
		buttonStart.setMinWidth(buttons.getPrefWidth());
		buttonStart.setStyle(primaryButtonStyle());

		//takes user to intermediate page when pressed/starts simulation
		buttonStart.setOnAction(event-> startSimulation());

		//allows user to access settings for viewing and editing
		Button buttonEdit = new Button("Settings");
		buttonEdit.setMinWidth(buttons.getPrefWidth());
		buttonEdit.setStyle(primaryButtonStyle());
		//user is taken to the first setting page (general settings)
		buttonEdit.setOnAction(event -> generalEditPage());

		//allows user to exit simulation from main page
		Button buttonExit = new Button("Exit Simulation");
		buttonExit.setMinWidth(buttons.getPrefWidth());
		buttonExit.setStyle(primaryButtonStyle());
		buttonExit.setOnAction(event-> System.exit(0));

		buttons.getChildren().addAll(buttonStart, buttonEdit, buttonExit);
		buttons.setAlignment(Pos.BOTTOM_CENTER);

		//gives title and button a good layout with respect to each other
		VBox firstLayout = new VBox(30);
		firstLayout.getChildren().addAll(titleLayout, buttons);
		firstLayout.setAlignment(Pos.CENTER);

		//arranges all elements of the main menu on the screen
		VBox menuLayout = new VBox(30);
		menuLayout.getChildren().addAll(picture, firstLayout);
		menuLayout.setSpacing(10);
		menuLayout.setAlignment(Pos.CENTER);
		menuLayout.setStyle(PRIMARY_BACKGROUND_COLOR);


		root = new StackPane();
		root.getChildren().add(menuLayout);

		mainMenu = new Scene(root, 900, 600);

		//necessary to set window and create all the window specifics in the default javafx start method
		window.setScene(mainMenu);
		window.sizeToScene();
		window.centerOnScreen();
		window.setTitle("Dromedary Drones");
		window.setResizable(false);
		window.show();
	}

	/**
	 * Called when the program is told to shutdown.
	 * Shuts down the executor service.
	 * @author Christian Burns
	 */
	@Override
	public void stop() throws Exception {
		super.stop();
		try { executor.shutdownNow();
		} catch (Exception ignore) {}
	}

	/**
	 * An intermediate page that is displayed while the simulation is running so that the user knows it is running
	 * @author Izzy Patnode
	 */
	public void startSimulation() {
		title = new Text("Simulation is Running...");
		title.setStyle(FONT_TYPE + "-fx-font-size: 35; -fx-fill: #0047ab");
		title.setWrappingWidth(450);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox(20);
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.TOP_CENTER);

		//allows us to add camel png to display
		Image simulationImage = new Image("file:resources/CuteCamel.png");

		ImageView picture = new ImageView(simulationImage);
		picture.setX(50);
		picture.setY(50);

		picture.setFitHeight(300);
		picture.setFitWidth(300);

		picture.setPreserveRatio(true);

		VBox simulationPicture = new VBox(20);
		simulationPicture.getChildren().add(picture);
		simulationPicture.setAlignment(Pos.CENTER);

		//allows the user to cancel the simulation
		Button cancelButton = new Button("Cancel Simulation");
		String cssStyle = PRIMARY_BACKGROUND_COLOR + FONT_TYPE + "-fx-font-size: 14; -fx-text-fill: #0047ab;" +
				"-fx-border-width: 1; -fx-border-color: #0047ab";
		cancelButton.setStyle(cssStyle);

		//takes user back to main menu
		cancelButton.setOnAction(event -> {
			abortSimulation();
			window.setScene(mainMenu);
		});

		//need to put button in its own display so the layout of the page stays nice
		HBox simulationButton = new HBox(20);
		simulationButton.getChildren().add(cancelButton);
		simulationButton.setAlignment(Pos.BOTTOM_CENTER);

		//arranges all elements of the page on the screen
		VBox simulationLayout = new VBox(35);
		simulationLayout.getChildren().addAll(titleLayout, simulationPicture, simulationButton);
		simulationLayout.setAlignment(Pos.CENTER);
		simulationLayout.setStyle(PRIMARY_BACKGROUND_COLOR);

		root = new StackPane();
		root.getChildren().add(simulationLayout);

		Scene simulationPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(simulationPage);

		runSimulation();
	}

	/**
	 * Allows for not writing out the style of each button each time we create a button
	 * @return a string with the style in css of each button
	 * @author Izzy Patnode
	 */
	public String primaryButtonStyle() {
		 return PRIMARY_BACKGROUND_COLOR + FONT_TYPE + " -fx-font-size: 12; -fx-text-fill: #0047ab;" +
				"-fx-border-width: 1; -fx-border-color: #0047ab";
	}

	/**
	 * Allows for not writing out the secondary style of a button each time we create one
	 * @return a string with the secondary style for a button in css
	 * @author Izzy Patnode
	 */
	public String secondaryButtonStyle() {
		return SECONDARY_BACKGROUND_COLOR + FONT_TYPE + "-fx-font-size: 12; -fx-text-fill: #0047ab;" +
				"-fx-border-width: 1; -fx-border-color: #0047ab";
	}

	/**
	 * Allows for not writing out the style of a table for each table
	 * @return a string with the style of a table in css
	 * @author Izzy Patnode
	 */
	public String tableStyle() {
		return "-fx-control-inner-background: #bdbdbd; -fx-control-inner-background-alt: #e0e0e0; " +
				"-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: #e0e0e0";
	}

	/**
	 * Allows for not having to recreate the title (Simulation Settings) for every settings page
	 * @author Izzy Patnode
	 */
	public void settingTitle() {
		title = new Text("Simulation Settings");
		title.setStyle(FONT_TYPE + "-fx-font-size: 35; -fx-fill: #0047ab");
		title.setWrappingWidth(450);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox();
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.CENTER);
	}

	/**
	 * Allows for not having to recreate the home button for every settings page
	 * @author Izzy Patnode
	 */
	public void homeButton() {
		//icon created by Google
		Image homeIcon = new Image("file:resources/home-button.png");
		ImageView homeView = new ImageView(homeIcon);

		Button homeButton = new Button("", homeView);
		homeButton.setStyle(SECONDARY_BACKGROUND_COLOR);
		homeButton.setOnAction(event -> window.setScene(mainMenu));

		iconLayout = new HBox();
		iconLayout.setAlignment(Pos.TOP_LEFT);
		iconLayout.setPadding(new Insets(0, 0, 0, 15));
		iconLayout.getChildren().add(homeButton);
		iconLayout.setStyle(SECONDARY_BACKGROUND_COLOR);
	}

	/**
	 * Allows for not having to recreate the menu buttons for every settings page
	 * @author Izzy Patnode
	 */
	public void menuButtons() {
		buttonLayout = new VBox();
		buttonLayout.setPrefWidth(110);
		buttonLayout.setSpacing(5);

		Button generalButton = new Button("General Settings");
		generalButton.setMinWidth(buttonLayout.getPrefWidth());
		generalButton.setStyle(secondaryButtonStyle());
		generalButton.setOnAction(event -> generalEditPage());

		Button foodButton = new Button("Food Settings");
		foodButton.setMinWidth(buttonLayout.getPrefWidth());
		foodButton.setStyle(secondaryButtonStyle());
		foodButton.setOnAction(event -> editFoodPage());

		Button mealButton = new Button("Meal Settings");
		mealButton.setMinWidth(buttonLayout.getPrefWidth());
		mealButton.setStyle(secondaryButtonStyle());
		mealButton.setOnAction(event -> editMealsPage());

		Button droneButton = new Button("Drone Settings");
		droneButton.setMinWidth(buttonLayout.getPrefWidth());
		droneButton.setStyle(secondaryButtonStyle());
		droneButton.setOnAction(event -> editDronePage());

		Button mapButton = new Button("Map Settings");
		mapButton.setMinWidth(buttonLayout.getPrefWidth());
		mapButton.setStyle(secondaryButtonStyle());
		mapButton.setOnAction(event -> editMapPage());

		Button startButton = new Button("Start Simulation");
		startButton.setMinWidth(buttonLayout.getPrefWidth());
		startButton.setStyle(secondaryButtonStyle());
		startButton.setOnAction(event -> startSimulation());

		buttonLayout.getChildren().addAll(generalButton, foodButton, mealButton, droneButton, mapButton, startButton);
		buttonLayout.setAlignment(Pos.CENTER_LEFT);
		buttonLayout.setPadding(new Insets(0, 0, 0, 5));
	}

	/**
	 * Decreases redundancy of code used for importing and exporting settings
	 * @return Vbox containing buttons for importing and exporting buttons
	 * @author Izzy Patnode
	 */
	public VBox importExportSettings(String sceneName) {
		VBox saveLoadButtons = new VBox(5);
		saveLoadButtons.setPrefWidth(110);

		//adds buttons for loading and saving model
		Button saveButton = new Button("Export Settings");
		saveButton.setMinWidth(saveLoadButtons.getPrefWidth());
		saveButton.setStyle(secondaryButtonStyle());

		//saves current settings to simulation and opens file explorer to save to xml file
		saveButton.setOnAction(event -> {
			//saves settings in XML file to local machine
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Export Settings");
			fileChooser.getExtensionFilters().add(
					new FileChooser.ExtensionFilter("XML", "*.xml")
			);
			File file = fileChooser.showSaveDialog(window);
			if (file != null) {
				try { Configuration.getInstance().saveConfigurations(file);
				} catch (IOException exception) { exception.printStackTrace(); }
			}
		}); //end of saving settings event

		Button loadButton = new Button("Import Settings");
		loadButton.setMinWidth(saveLoadButtons.getPrefWidth());
		loadButton.setStyle(secondaryButtonStyle());

		//opens settings and loads model from user location
		loadButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Import Settings");
			fileChooser.getExtensionFilters().add(
					new FileChooser.ExtensionFilter("XML", "*.xml")
			);
			File file = fileChooser.showOpenDialog(window);
			if (file != null) {
				Configuration.getInstance().initialize(file);
				try {
					Configuration.getInstance().setLastConfigurationFile(file);
				} catch (IOException exception) {
					exception.printStackTrace();
				}
				currentSimulation = Configuration.getInstance().getCurrentSimulation();

				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("The settings have been imported.");
				alert.showAndWait();

				switch(sceneName) {
					case "General":
						generalEditPage();
						break;
					case "Food":
						editFoodPage();
						break;
					case "Meal":
						editMealsPage();
						break;
					case "Drone":
						editDronePage();
						break;
					case "Map":
						editMapPage();
						break;
					default:
						break;
				}
			}
		}); //end of loading settings event

		//allows user to reload in the default settings at any given time
		Button defaultButton = new Button("Default Settings");
		defaultButton.setMinWidth(saveLoadButtons.getPrefWidth());
		defaultButton.setStyle(secondaryButtonStyle());

		defaultButton.setOnAction(click -> {
			var configuration = Configuration.getInstance();
			configuration.initialize(null);
			currentSimulation = configuration.getCurrentSimulation();

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("The default settings have been loaded in.");
			alert.showAndWait();

			switch(sceneName) {
				case "General":
					generalEditPage();
					break;
				case "Food":
					editFoodPage();
					break;
				case "Meal":
					editMealsPage();
					break;
				case "Drone":
					editDronePage();
					break;
				case "Map":
					editMapPage();
					break;
				default:
					break;
			}
		}); //end of loading default settings event

		saveLoadButtons.getChildren().addAll(saveButton, loadButton, defaultButton);

		saveLoadButtons.setAlignment(Pos.BOTTOM_LEFT);
		saveLoadButtons.setPadding(new Insets(0, 0, 0, 10));

		return saveLoadButtons;
	}

	/**
	 * Reduces redundancy of creating layout for sidebar
	 * @param currentSceneName the name of the current scene so we can refresh the page when importing settings
	 * @return the layout of the sidebar
	 * @author Izzy Patnode
	 */
	private VBox sideBarLayout(String currentSceneName) {
		VBox sideBar = new VBox();
		sideBar.setSpacing(110);
		sideBar.setStyle(SECONDARY_BACKGROUND_COLOR + SIDEBAR_STYLE);

		homeButton();

		menuButtons();

		VBox importExportDisplay = importExportSettings(currentSceneName);

		sideBar.getChildren().addAll(iconLayout, buttonLayout, importExportDisplay);

		return sideBar;
	}

	/**
	 * Reduces redundancy in creating error alerts
	 * @param errorName the title and header of the error alert
	 * @param errorMessage the message for the error alert
	 * @return an alert with the given title, header, and message
	 * @author Izzy Patnode
	 */
	private Alert setErrorAlert(String errorName, String errorMessage) {
		//alert to let users know why their values are not saving
		Alert errorAlert = new Alert(Alert.AlertType.ERROR);
		errorAlert.setTitle(errorName);
		errorAlert.setHeaderText("Error: " + errorName);

		errorAlert.setContentText(errorMessage);

		return errorAlert;
	}

	/**
	 * Creates GUI page for general settings (i.e. stochastic flow)
	 * @author Izzy Patnode and Rachel Franklin
	 */
	public void generalEditPage() {
		VBox leftLayout = sideBarLayout("General");

		settingTitle();

		//table heading (outside of gridpane) for user to understand what is on the page
		Text gridHeading = new Text("Order Volume per Hour");
		gridHeading.setStyle(BOLD_FONT_STYLE + FONT_TYPE + "-fx-font-size: 15; -fx-text-fill: black");
		gridHeading.setWrappingWidth(200);
		gridHeading.setTextAlignment(TextAlignment.CENTER);

		//creates gridpane containing the current stochastic flow values
		GridPane generalSettings = new GridPane();
		generalSettings.setAlignment(Pos.CENTER);
		generalSettings.setVgap(10);
		generalSettings.setMaxSize(300, 300);

		Font helvetica = Font.font("Helvetica", 15);
		Text hourOne = new Text("Hour 1: ");
		hourOne.setFont(helvetica);

		Text hourTwo = new Text("Hour 2: ");
		hourTwo.setFont(helvetica);

		Text hourThree = new Text("Hour 3: ");
		hourThree.setFont(helvetica);

		Text hourFour = new Text("Hour 4: ");
		hourFour.setFont(helvetica);

		//grabs current stochastic flow so that it can be added to the grid pane
		ArrayList<Integer> currentModel = new ArrayList<>(currentSimulation.getStochasticFlow());

		int currentHourOne = currentModel.get(0);
		TextField hourOneMeals = new TextField(currentHourOne + "");
		hourOneMeals.setMaxWidth(80);
		int currentHourTwo = currentModel.get(1);
		TextField hourTwoMeals = new TextField(currentHourTwo + "");
		hourTwoMeals.setMaxWidth(80);
		int currentHourThree = currentModel.get(2);
		TextField hourThreeMeals = new TextField(currentHourThree + "");
		hourThreeMeals.setMaxWidth(80);
		int currentHourFour = currentModel.get(3);
		TextField hourFourMeals = new TextField(currentHourFour + "");
		hourFourMeals.setMaxWidth(80);

		//cells are added to the gridpane which allows for a simple and readable format
		generalSettings.add(hourOne, 0, 0);
		generalSettings.add(hourOneMeals, 1, 0);
		generalSettings.add(hourTwo, 0, 1);
		generalSettings.add(hourTwoMeals, 1, 1);
		generalSettings.add(hourThree, 0, 2);
		generalSettings.add(hourThreeMeals, 1, 2);
		generalSettings.add(hourFour, 0, 3);
		generalSettings.add(hourFourMeals, 1, 3);

		VBox gridLayout = new VBox();
		gridLayout.setSpacing(5);
		gridLayout.setAlignment(Pos.CENTER);
		gridLayout.getChildren().addAll(gridHeading, generalSettings);

		//arranges the title and grid with respect to each other so they are centered on the page
		VBox centerLayout = new VBox();
		centerLayout.setSpacing(140);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, gridLayout);

		//allows user to edit the current stochastic model
		Button editButton = new Button("Save Changes");
		editButton.setStyle(primaryButtonStyle());

		editButton.setOnAction(event -> {
			ArrayList<Integer> stochasticModel = new ArrayList<>();

			Alert errorAlert;
			String errorTitle = "Invalid Input";
			String errorText;

			try {
				int newHourOneMeals = Integer.parseInt(hourOneMeals.getText());
				int newHourTwoMeals = Integer.parseInt(hourTwoMeals.getText());
				int newHourThreeMeals = Integer.parseInt(hourThreeMeals.getText());
				int newHourFourMeals = Integer.parseInt(hourFourMeals.getText());

				//alerts user that their inputs are not positive values or zero
				if(newHourOneMeals < 0 || newHourTwoMeals < 0 || newHourThreeMeals < 0 || newHourFourMeals < 0) {
					errorText = "Stochastic model cannot have negative number of meals.";
					errorAlert = setErrorAlert(errorTitle, errorText);

					//resets the gridpane values and stochastic model so that incorrect values are not used
					stochasticModel.addAll(List.of(currentHourOne, currentHourTwo, currentHourThree, currentHourFour));

					hourOneMeals.setText(currentHourOne + "");
					hourTwoMeals.setText(currentHourTwo + "");
					hourThreeMeals.setText(currentHourThree + "");
					hourFourMeals.setText(currentHourFour + "");

					errorAlert.showAndWait();
				}
				else {

					stochasticModel.addAll(List.of(newHourOneMeals, newHourTwoMeals,
							newHourThreeMeals, newHourFourMeals));

					currentSimulation.addStochasticFlow(stochasticModel);

					/* alerts the user that their edit to the stochastic model has been successful
					 ** so they know that the values have been updated */
					Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
					saveAlert.setTitle("Confirm Changes");
					saveAlert.setHeaderText("Changes Saved!");
					saveAlert.showAndWait();
				}

			} //end of try block
			catch(NumberFormatException illegalFormat) {
				//alerts the user that their input is not an integer
				errorText = "Integer format required.";
				errorAlert = setErrorAlert(errorTitle, errorText);

				//resets the gridpane values and stochastic model so that incorrect values are not used
				stochasticModel.addAll(List.of(currentHourOne, currentHourTwo, currentHourThree, currentHourFour));

				hourOneMeals.setText(currentHourOne + "");
				hourTwoMeals.setText(currentHourTwo + "");
				hourThreeMeals.setText(currentHourThree + "");
				hourFourMeals.setText(currentHourFour + "");

				errorAlert.showAndWait();
			} //end of catch block
		}); //end of editing stochastic flow event

		VBox rightLayout = new VBox();
		rightLayout.setAlignment(Pos.BOTTOM_LEFT);
		rightLayout.setPadding(new Insets(0, 0, 200, 0));
		rightLayout.getChildren().add(editButton);

		HBox mainLayout = new HBox();
		mainLayout.getChildren().addAll(centerLayout, rightLayout);

		settingLayout = new HBox(130);
		settingLayout.getChildren().addAll(leftLayout, mainLayout);
		settingLayout.setStyle(PRIMARY_BACKGROUND_COLOR);

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene generalEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(generalEditPage);
	}

	/**
	 * Creates GUI page for food items settings
	 * @author Izzy Patnode and Rachel Franklin
	 */
	public void editFoodPage() {
		VBox leftLayout = sideBarLayout("Food");

		settingTitle();

		//arranges food items in a table format for a simple and readable layout
		TableView<FoodItem> foodTable = new TableView<>();
		ObservableList<FoodItem> foodItems = currentSimulation.getFoodItems();
		foodTable.setItems(foodItems);
		foodTable.setEditable(true);
		foodTable.setStyle(tableStyle());

		//Creates table headings for the table so the user understands what each column represents
		TableColumn<FoodItem, String> itemHeading = new TableColumn<>("Food Item");
		itemHeading.setCellValueFactory(new PropertyValueFactory<>("name"));
		itemHeading.setPrefWidth(100);

		TableColumn<FoodItem, Double> weightHeading = new TableColumn<>("Weight (oz)");
		weightHeading.setCellValueFactory(new PropertyValueFactory<>("weight"));
		weightHeading.setPrefWidth(100);

		//allows user to edit the name of a food item already in the table
		itemHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		itemHeading.setOnEditCommit(event -> {
			String oldName = event.getOldValue();
			String newName = event.getNewValue();

			//alerts user if they put in a name that is null (not allowed)
			if(newName.equals("")) {
				String errorName = "Invalid Input";
				String errorMessage = "Food name cannot be null.";
				Alert errorAlert = setErrorAlert(errorName, errorMessage);
				event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(oldName);
				errorAlert.showAndWait();
			}
			else {
				event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(newName);
			}

			event.getTableView().refresh();
		}); //end of editing food name event

		//allows user to edit the weight of a food item already in the table
		weightHeading.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter(){
			@Override
			public Double fromString(String value){
				try {
					return super.fromString(value);
				} catch(Exception exception){
					return Double.NaN;
				}
			}
		}));
		weightHeading.setOnEditCommit(event -> {
			/* If there is an error make the index 1, this allows us to determine whether to set
			** the cell value to the new value or the old value */
			int errorIndex = 0;
			Alert invalidInput;
			String errorTitle = "Invalid input";
			String errorMessage = "";
			double oldValue = event.getOldValue();

			//notifies the user if the value they entered is not a numerical value (integer or decimal)
			if (event.getNewValue().isNaN()){
				errorMessage = "Input must be an integer or a decimal.";
				errorIndex = 1;
			}
			else {
				double newValue = event.getNewValue();
				double maxPayload = currentSimulation.getDroneSettings().getMaxPayloadWeight();

				/* notifies the user if the new weight of the selected food item exceeds
				** the maximum payload of the drone */
				if (newValue > maxPayload) {
					errorMessage = "Food item weight cannot exceed" + maxPayload + " oz.";
					errorIndex = 1;
				}
				/* notifies the user if the new weight of the selected food item is less than or equal to zero
				** (cannot have an item that weighs a negative value or does not weight anything at all */
				else if (newValue <= 0) {
					errorMessage = "Food item weight cannot be less than or equal to zero.";
					errorIndex = 1;
				}
				/* since the drone capacity cannot exceed a given number of pounds, the weight of each order cannot
				** exceed that given number. So we must check to make sure meals with the the specified food and
				** new weight do not exceed the maximum payload */
				else {
					ArrayList<Meal> mealTypes = currentSimulation.getMealTypes();
					String itemName = event.getTableView().getItems().get(event.getTablePosition().getRow()).getName();
					for (Meal meal : mealTypes) {
						double newWeight = 0;
						if(errorIndex == 0) {
							/* need to add up the weights of the given meal's food items to check that the
							** meal's weight is less than the maximum payload */
							for (FoodItem food: meal.getFoods()) {
								if (itemName.equals(food.getName())) {
									newWeight += newValue;
								}
								else {
									newWeight += food.getWeight();
								}

								/* alerts user if the new weight for the given food item causes a meal
								** to exceed the drone's maximum payload */
								if (newWeight > maxPayload) {
									errorMessage = "Inputted weight causes a meal to exceed the " +
											"drone's maximum cargo capacity.";
									errorIndex = 1;
								}
							}//end of for loop for adding meal's food's weights together
						} //end of if statement for when meals (so far) do not exceed the drone's maximum payload
					}//end of for loop for checking all meals
				} //end of else statement for when new weight is greater than zero and less than maximum drone payload
			} //end of else statement for if the new weight is a number

			//finally sets the new food weight if the weight meets the necessary criteria
			if(errorIndex == 0) {
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setWeight(event.getNewValue());
			}
			//resets the weight of the food item if the new weight does not meet the necessary criteria
			else {
				invalidInput = setErrorAlert(errorTitle, errorMessage);
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setWeight(oldValue);

				invalidInput.showAndWait();
			}

			event.getTableView().refresh();
		});//end of editing food item weight event

		//adds columns to table
		foodTable.getColumns().setAll(itemHeading, weightHeading);
		foodTable.setPrefWidth(200);
		foodTable.setPrefHeight(300);

		//allows table to be easily arranged on the screen
		StackPane tableLayout = new StackPane();
		tableLayout.setAlignment(Pos.CENTER_RIGHT);
		tableLayout.setMaxSize(202, 300);
		tableLayout.getChildren().add(foodTable);

		Font textFont = new Font("Helvetica", 15);
		//necessary elements that allow the user to add a food item to the current list of food items
		Text newFoodNameLabel = new Text("Food name: ");
		newFoodNameLabel.setFont(textFont);
		newFoodNameLabel.setTextAlignment(TextAlignment.CENTER);

		TextField newFoodName = new TextField();
		newFoodName.setMaxWidth(80);

		Text newFoodWeightLabel = new Text("Weight: ");
		newFoodWeightLabel.setFont(textFont);
		newFoodWeightLabel.setTextAlignment(TextAlignment.CENTER);

		TextField newFoodWeight = new TextField();
		newFoodWeight.setMaxWidth(80);

		HBox newNameField = new HBox();
		newNameField.getChildren().addAll(newFoodNameLabel, newFoodName);

		HBox newWeightField = new HBox();
		newWeightField.getChildren().addAll(newFoodWeightLabel, newFoodWeight);

		HBox newFoodFields = new HBox(7);
		newFoodFields.getChildren().addAll(newNameField, newWeightField);
		newFoodFields.setAlignment(Pos.CENTER);
		newFoodFields.setPadding(new Insets(0, 20, 0, 0));

		//arranges table and fields for creating new food items together to make the page easily readable
		VBox tableFoodLayout = new VBox(10);
		tableFoodLayout.getChildren().addAll(tableLayout, newFoodFields);
		tableFoodLayout.setAlignment(Pos.CENTER);

		VBox centerLayout = new VBox();
		centerLayout.setSpacing(90);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, tableFoodLayout);

		//allows user to add a food item to the current simulation
		Button addButton = new Button("Add");
		addButton.setStyle(primaryButtonStyle());
		addButton.setMinWidth(60);

		addButton.setOnAction(event -> {
			Alert errorAlert;
			String errorTitle = "Invalid Input";
			String errorMessage;

			//attempts to create food item with given user input
			try {
				double newWeight = Double.parseDouble(newFoodWeight.getText());

				//alerts user if the new food's weight is greater than drone's maximum payload
				if (newWeight > currentSimulation.getDroneSettings().getMaxPayloadWeight()) {
					errorMessage = "Food weight cannot exceed drone's maximum payload.";
					errorAlert = setErrorAlert(errorTitle, errorMessage);
					errorAlert.showAndWait();
				}
				//alerts user if the new food's weight is less than or equal to zero
				else if (newWeight <= 0.0) {
					errorMessage = "Food weight cannot be less than or equal to zero.";
					errorAlert = setErrorAlert(errorTitle, errorMessage);
					errorAlert.showAndWait();
				}
				//alerts the user if the new food does not have a name
				else if (newFoodName.getText().equals("")) {
					errorMessage = "Food name cannot be null.";
					errorAlert = setErrorAlert(errorTitle, errorMessage);
					errorAlert.showAndWait();
				}
				//adds new food item to current simulation if it meets all of the necessary criteria
				else{
					currentSimulation.addFoodItem(new FoodItem(newFoodName.getText(), newWeight));
					foodTable.setItems(currentSimulation.getFoodItems());
					foodTable.refresh();
					newFoodName.setText("");
					newFoodWeight.setText("");
				}
			}
			//alerts user if the value inputted is not a number (for weight)
			catch(NumberFormatException illegalFormat) {
				errorMessage = "Number format required.";
				errorAlert = setErrorAlert(errorTitle, errorMessage);
				errorAlert.showAndWait();
				foodTable.refresh();
			}
		}); //end of add food item event

		//allows the user to delete a food item from the current simulation
		Button deleteButton = new Button("Delete");
		deleteButton.setStyle(primaryButtonStyle());
		deleteButton.setMinWidth(60);
		deleteButton.setOnAction(event -> {
			int deletedRow = foodTable.getSelectionModel().getSelectedIndex();
			//alerts user if they attempt to delete a food item without selecting the food item first
			if(deletedRow < 0) {
				String errorTitle = "Invalid Deletion";
				String errorMessage = "Food item not selected.";
				Alert errorAlert = setErrorAlert(errorTitle, errorMessage);
				errorAlert.showAndWait();
			}
			//deletes food items from current simulation (and all meals where the food item exists)
			else {
				FoodItem deletedFood = foodTable.getSelectionModel().getSelectedItem();
				foodItems.remove(deletedRow);
				currentSimulation.removeFoodItem(deletedFood);
				foodTable.refresh();
			}
		}); //end of delete food item event

		//arranges add and delete buttons relative to each other
		HBox rightLayout = new HBox(10);
		rightLayout.getChildren().addAll(addButton, deleteButton);
		rightLayout.setAlignment(Pos.BOTTOM_RIGHT);
		rightLayout.setPadding(new Insets(0, 50, 107, 0));

		HBox mainLayout = new HBox(10);
		mainLayout.getChildren().addAll(centerLayout, rightLayout);

		//arranges all elements of the page on the screen
		settingLayout = new HBox(130);
		settingLayout.getChildren().addAll(leftLayout, mainLayout);
		settingLayout.setStyle(PRIMARY_BACKGROUND_COLOR);

		root = new StackPane();
		root.getChildren().add(settingLayout);

		 Scene foodEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(foodEditPage);
	}

	/**
	 * Creates GUI page for meal settings
	 * @author Izzy Patnode and Rachel Franklin
	 */
	public void editMealsPage() {
		VBox leftLayout = sideBarLayout("Meal");

		settingTitle();

		int mealIndex = 0;

		Text probabilityTitle = new Text("Meal Probabilities");
		probabilityTitle.setTextAlignment(TextAlignment.CENTER);
		probabilityTitle.setStyle(FONT_TYPE + BOLD_FONT_STYLE + "-fx-font-size: 15; -fx-text-fill: black");

		HBox probabilityTitleLayout = new HBox();
		probabilityTitleLayout.getChildren().add(probabilityTitle);
		probabilityTitleLayout.setAlignment(Pos.CENTER);

		GridPane mealProbabilities = new GridPane();
		mealProbabilities.setAlignment(Pos.CENTER);
		mealProbabilities.setHgap(5);

		//adds meal's probabilities to the gridpane
		for(Meal meal : currentSimulation.getMealTypes()) {
			Text mealName = new Text(meal.getName());
			mealName.setStyle(FONT_TYPE + "-fx-font-size: 12; -fx-text-fill: black");
			TextField mealProbability = new TextField(meal.getProbability() + "");
			mealProbability.setMaxWidth(50);
			mealProbabilities.add(mealName, 0, mealIndex);
			mealProbabilities.add(mealProbability, 1, mealIndex);
			mealIndex++;
		}

		ScrollPane probabilityPane = new ScrollPane();
		probabilityPane.setContent(mealProbabilities);
		probabilityPane.setMinWidth(185);
		probabilityPane.setMinHeight(200);
		probabilityPane.setMaxHeight(200);
		probabilityPane.setStyle("-fx-background: #e0e0e0; -fx-border-style: solid; " +
				"-fx-border-width: 1; -fx-border-color: black");


		//allows user to save their changes to the meal probabilities
		Button saveProbabilityButton = new Button("Save Changes");
		saveProbabilityButton.setStyle(primaryButtonStyle());

		saveProbabilityButton.setOnAction(event -> {
			BigDecimal totalProbability = BigDecimal.ZERO;
			ArrayList<Meal> currentMealTypes = currentSimulation.getMealTypes();
			ArrayList<Double> oldProbabilities = new ArrayList<>();
			int errorIndex = 0;

			Alert invalidInput;
			String errorTitle = "Invalid Input";
			String errorMessage = "";

			//allows for access to the old probability values if the new values are invalid
			for(Meal currentMeal : currentMealTypes) {
				oldProbabilities.add(currentMeal.getProbability());
			}

			try {
				//finds all the nodes that contain the probabilities (textfields) and adds those probabilities up
				for (Node node : mealProbabilities.getChildren()) {
					if (mealProbabilities.getColumnIndex(node) == 1) {
						TextField mealProbability = (TextField) node;
						double currentProbability = Double.parseDouble(mealProbability.getText());

						//alerts user if probability value is negative or greater than one
						if (currentProbability < 0 || currentProbability > 1) {
							errorMessage = "Probabilities must be between 0 and 1 (inclusive).";
							errorIndex = 1;
						} else {
							totalProbability = totalProbability.add(BigDecimal.valueOf(currentProbability));
						}
					}
				}
				if (errorIndex == 0) {
					//check to make sure all probabilities add up to one (using BigDecimal so we can get exactly one)
					if (totalProbability.compareTo(BigDecimal.ONE) == 0) {
						Text currentMealName = new Text("");
						for (Node currentMeal : mealProbabilities.getChildren()) {
							TextField currentMealProbability;

							//finds the name of the meal (names of meals are text in the first column)
							if (mealProbabilities.getColumnIndex(currentMeal) == 0) {
								currentMealName = (Text) currentMeal;
							}
							//finds the probability of the correspond meal (in second column)
							if(mealProbabilities.getColumnIndex(currentMeal) == 1) {
								currentMealProbability = (TextField) currentMeal;
								for (Meal meal : currentMealTypes) {
									/* since the node for the name comes before the node of the probability,
									** we know that the name of the previous node corresponds to the
									** current probability*/
									if (meal.getName().equals(currentMealName.getText())) {
										meal.setProbability(Double.parseDouble(currentMealProbability.getText()));
									}
								}
							}
						} //end of nodes for loop
					} //end of if statement where total probability is equal to one
					else {
						//alerts user if probabilities of all meals do not add up to one
						errorMessage = "Total probability must equal 1.";
						errorIndex = 1;
					}
				} //end of if statement where inputted probabilities are between 0 and 1 (inclusive)
			} //end of try block
			catch(IllegalArgumentException exception) {
				//alerts user if probability is not a number
				errorMessage = "Inputted probabilities must be a number";
				errorIndex = 1;
			}

			/* once errorIndex is equal to one, checking values stops so we can initialize the alert,
			** reset the probabilities, and show the alert here rather than after every error */
			if(errorIndex == 1) {
				invalidInput = setErrorAlert(errorTitle, errorMessage);

				int currentIndex = 0;

				/* nodes go in order of meals (based on meal arrays) so we don't have to find
				** the specific meal to reset the probabilities */
				for(Node node: mealProbabilities.getChildren()) {
					if(mealProbabilities.getColumnIndex(node) == 1) {
						TextField currentProbability = (TextField) node;
						currentProbability.setText(oldProbabilities.get(currentIndex) + "");
						currentIndex++;
					}
				}
				invalidInput.showAndWait();
			} //end of if statement where error dialog is shown
			else {
				//alerts user the changes have been save so they know the probabilities have been updated
				Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
				saveAlert.setTitle("Confirm Changes");
				saveAlert.setHeaderText("Changes Saved!");
				saveAlert.showAndWait();
			}
		}); //end of editing meal probabilities event


		VBox probabilityLayout = new VBox(5);
		probabilityLayout.getChildren().addAll(probabilityTitleLayout, probabilityPane, saveProbabilityButton);
		probabilityLayout.setAlignment(Pos.CENTER);

		//arranges all meals together
		VBox mealsBox = new VBox(10);

		//creates a gridpane for each meal in the simulation
		for(Meal meal : currentSimulation.getMealTypes()) {
			//allows for each meal to be arranged with respect to its own elements
			VBox singleMealLayout = new VBox();

			TextField mealName = new TextField(meal.getName());
			mealName.setFont(Font.font("Helvetica", 15));
			mealName.setMaxWidth(200);
			mealName.setStyle(BOLD_FONT_STYLE);

			//user should be able to edit meal name
			mealName.setOnAction(event -> {
				String newName = mealName.getText();
				int errorIndex = 0;

				Alert errorAlert;
				String errorTitle = "Invalid Input";
				String errorMessage = "";

				if(newName.equals("")) {
					errorMessage = "Meal must have a name.";
					errorIndex = 1;
				}
				else {
					for(Meal currentMeal: currentSimulation.getMealTypes()) {
						if(currentMeal.getName().equals(newName) && (currentMeal.getFoods().equals(meal.getFoods())
								|| currentMeal.getProbability() != meal.getProbability())) {
							errorMessage = "Meal with name already exists.";
							errorIndex = 1;
							break;
						}
					}

					if(errorIndex == 0) {
						//resets the node in the gridpane of the meal name that is being changed
						for(Node currentMeal: mealProbabilities.getChildren()) {
							//all meal names reside in the first column (column zero)
							if(mealProbabilities.getColumnIndex(currentMeal) == 0) {
								Text name = (Text) currentMeal;
								if(meal.getName().equals(name.getText())) {
									name.setText(newName);
								}
							}
						}

						meal.setName(newName);

						Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
						saveAlert.setTitle("Confirm Changes");
						saveAlert.setHeaderText("Changes Saved!");
						saveAlert.showAndWait();
					} //end of if statement where name does not already exist
				} //end of else statement where name is not null

				if(errorIndex == 1) {
					errorAlert = setErrorAlert(errorTitle, errorMessage);
					mealName.setText(meal.getName());
					errorAlert.showAndWait();
				}
			}); //end of event handling meal name change

			HBox titleFormat = new HBox();
			titleFormat.getChildren().add(mealName);
			titleFormat.setPadding(new Insets(8, 0, 0, 5));

			//used to store each food item in the meal and how many of it there is
			HashMap<String, Integer> numberPerFood = new HashMap<>();

			//counts the number of each food item in the meal (i.e. 2 burgers, 1, fries, etc.)
			for (FoodItem mealItem : meal.getFoods()) {
				String name = mealItem.getName();
				numberPerFood.put(name, numberPerFood.getOrDefault(name, 0) + 1);
			}

			//we want to show items with a 0 that aren't in the meal too
			for (FoodItem foodItem : currentSimulation.getFoodItems()){
				String name = foodItem.getName();
				numberPerFood.put(name, numberPerFood.getOrDefault(name, 0));
			}

			//map must be an observable to be used in a table
			ObservableList<HashMap.Entry<String, Integer>> itemCounts = FXCollections.
					observableArrayList(numberPerFood.entrySet());

			TableView<HashMap.Entry<String, Integer>> mealTable = new TableView<>(itemCounts);
			//when maxwidth = prefwidth, horizontal scroll bar shows up -- make maxWidth > prefWidth
			mealTable.setMaxSize(205, 300);
			mealTable.setEditable(true);
			mealTable.setStyle(tableStyle());

			//table holds food items and counts for each meal
			TableColumn<HashMap.Entry<String, Integer>, String> foodColumn = new TableColumn<>("Food Item");
			foodColumn.setCellValueFactory(
					(TableColumn.CellDataFeatures<HashMap.Entry<String, Integer>, String> item) ->
					new SimpleStringProperty(item.getValue().getKey()));
			foodColumn.setPrefWidth(100);
			foodColumn.setEditable(true);

			// creates the table column displaying food quantities within a meal
			TableColumn<ObservableMap.Entry<String, Integer>, Integer> countColumn =
					new TableColumn<>("Number in Meal");
			countColumn.setCellValueFactory(
					(TableColumn.CellDataFeatures<HashMap.Entry<String, Integer>, Integer> item) ->
							new ReadOnlyObjectWrapper<>(item.getValue().getValue())); //Integers don't like to play nice
			countColumn.setPrefWidth(100);

			//user should be able to change how many of an item is in a meal
			countColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter(){
				@Override
				public Integer fromString(String value){
					try {
						return super.fromString(value);
					} catch(Exception exception){
						return null;
					}
				}
			}));
			countColumn.setOnEditCommit(event ->{
				int errorIndex = 0;
				Alert invalidInput;
				String alertTitle = "Invalid Input";
				String alertMessage = "";
				int oldValue = event.getOldValue();
				String itemName = event.getTableView().getItems().get(event.getTablePosition().getRow()).getKey();

				//user must input an integer
				if (event.getNewValue() == null){
					alertMessage = "Input must be an integer.";
					errorIndex = 1;
				}
				else {
					double maxPayload = currentSimulation.getDroneSettings().getMaxPayloadWeight();
					int newValue = event.getNewValue();

					//Cannot have negative amounts of an item
					if (newValue < 0){
						alertMessage = "Input must be non-negative.";
						errorIndex = 1;
					}

					else {
						//new meal weight must not be greater than drone's maximum payload
						double mealWeight;
						double itemWeight = currentSimulation.getFoodItem(itemName).getWeight();
						mealWeight = meal.getTotalWeight() - (oldValue * itemWeight) + (newValue * itemWeight);
						if (mealWeight > maxPayload){
							alertMessage = "Meal weight must not exceed maximum payload. (" + maxPayload + " oz.)";
							errorIndex = 1;
						}
					}
				}

				if(errorIndex == 0) {
					//user input valid weight
					int newValue = event.getNewValue();
					event.getTableView().getItems().get(event.getTablePosition().getRow()).setValue(newValue);

					if (newValue < oldValue){
						int difference = oldValue - newValue;
						int numberRemoved = 0;
						for (FoodItem item : meal.getFoods()){
							if (item.getName().equals(itemName)){
								meal.removeItem(item);
								numberRemoved++;
								if (numberRemoved == difference){
									break;
								}
							}
						}
					}

					else if (newValue > oldValue){
						int difference = newValue - oldValue;
						for (int index = 0; index < difference; index++){
							FoodItem item = new FoodItem(currentSimulation.getFoodItem(itemName));
							meal.addItem(item);
						}
					}
					//if the value is the same, don't do anything
				}
				else {
					invalidInput = setErrorAlert(alertTitle, alertMessage);
					//invalid input means value doesn't change
					event.getTableView().getItems().get(event.getTablePosition().getRow()).setValue(oldValue);
					invalidInput.showAndWait();
				}
			});

			//adds necessary columns to meal's table
			mealTable.getColumns().setAll(foodColumn, countColumn);
			mealTable.setPrefWidth(200);
			mealTable.setPrefHeight(250);

			//creates button for deleting meals
			Button deleteButton = new Button("X");
			deleteButton.setStyle(BOLD_FONT_STYLE + "-fx-background-color: #e0e0e0; -fx-font-size: 20; " +
					"fx-font-family: Helvetica; -fx-border-style: hidden; -fx-border-color: #e0e0e0; " +
					"-fx-border-width: 1");

			deleteButton.setOnAction(event -> {
				//total probability of meals after meal deletion should equal 1.0
				BigDecimal mealProbability = BigDecimal.valueOf(meal.getProbability());
				/* alerts user that they must set the probability of the deleted meal to zero and refactor the
				** other meal's probabilities to equal 1 */
				if (mealProbability.compareTo(BigDecimal.valueOf(0.0)) != 0){
					String alertMessage = "This meal's probability must be set to 0 before it can be " +
							"deleted. Please refactor the probabilities so that this meal's probability is 0 and " +
							"the rest are equivalent to 1.0.";
					Alert probabilityAlert = setErrorAlert("Meal not ready to delete", alertMessage);
					probabilityAlert.showAndWait();
				}
				else{
					String nameToDelete = meal.getName();

					mealsBox.getChildren().remove(singleMealLayout);
					currentSimulation.removeMeal(meal);

					Text deletedMealName = new Text("");
					Node nameNodeToDelete = new Node() {};
					/* since there is no way to remove an entire row at once in a gridpane, we need to find both
					** nodes that are in the row to be deleted and delete them together */
					for(Node deletedMeal : mealProbabilities.getChildren()) {
						if (mealProbabilities.getColumnIndex(deletedMeal) == 0) {
							nameNodeToDelete = deletedMeal;
							deletedMealName = (Text) deletedMeal;
						}
						if (mealProbabilities.getColumnIndex(deletedMeal) == 1) {
							if (nameToDelete.equals(deletedMealName.getText())) {
								mealProbabilities.getChildren().remove(nameNodeToDelete);
								mealProbabilities.getChildren().remove(deletedMeal);
								/* we need to break after the specified meal has been removed from the gridpane so the
								** program does not try to keep removing nodes */
								break;
							}
						}
					}
				} //end of else statement (when user has set up probabilities for meal to be deleted)
			}); //end of deleting meal event

			HBox deleteLayout = new HBox();
			deleteLayout.getChildren().add(deleteButton);
			deleteLayout.setAlignment(Pos.CENTER);

			//formats delete button with meal heading
			HBox topFormat = new HBox();
			topFormat.getChildren().addAll(titleFormat, deleteLayout);

			//formats meal components
			singleMealLayout.setSpacing(5);
			singleMealLayout.setAlignment(Pos.CENTER);
			singleMealLayout.getChildren().addAll(topFormat, mealTable);

			//adds meal to layout of all meals
			mealsBox.getChildren().add(singleMealLayout);
		} //end of for loop for creating layouts of each meal

		//allows for user to scroll through meals
		ScrollPane mealLayout = new ScrollPane();
		mealLayout.setContent(mealsBox);
		mealLayout.setMaxSize(260, 320);
		mealLayout.setStyle("-fx-background: #e0e0e0; -fx-border-style: solid; " +
				"-fx-border-width: 1; -fx-border-color: black");

		//allows user to add a meal to the current simulation's list of meals
		Button addButton = new Button("Add Meal");
		addButton.setStyle(primaryButtonStyle());
		addButton.setOnAction(event -> addMealPage());

		VBox tableAddLayout = new VBox(5);
		tableAddLayout.getChildren().addAll(mealLayout, addButton);
		tableAddLayout.setAlignment(Pos.CENTER);

		//formats display of menu column and full meal display
		VBox centerLayout = new VBox(80);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, tableAddLayout);

		HBox mainLayout = new HBox();
		mainLayout.getChildren().addAll(centerLayout, probabilityLayout);

		//arranges all elements of the page on the screen
		settingLayout = new HBox(130);
		settingLayout.getChildren().addAll(leftLayout, mainLayout);
		settingLayout.setStyle(PRIMARY_BACKGROUND_COLOR);

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene mealEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(mealEditPage);
	}

	/**
	 * Creates page for adding a meal
	 * @author Rachel Franklin
	 */
	public void addMealPage(){
		title = new Text("Add Meal");
		title.setStyle(FONT_TYPE + "-fx-font-size: 35; -fx-fill: #0047ab");
		title.setWrappingWidth(450);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox();
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.CENTER);

		//user should name the food item
		Text nameText = new Text("Food name: ");
		nameText.setFont(Font.font("Helvetica", 15));
		TextField nameField = new TextField();
		nameField.setMaxWidth(80);

		HBox nameLayout = new HBox();
		nameLayout.getChildren().addAll(nameText, nameField);
		nameLayout.setAlignment(Pos.CENTER);

		//formats display of meal counts
		HashMap<FoodItem, Integer> foodItems = new HashMap<>();

		for (FoodItem item : currentSimulation.getFoodItems()) {
			foodItems.put(item, 0);
		}

		ObservableList<HashMap.Entry<FoodItem, Integer>> foodInMeal = FXCollections
				.observableArrayList(foodItems.entrySet());

		TableView<HashMap.Entry<FoodItem, Integer>> foodTable = new TableView<>(foodInMeal);
		foodTable.setMaxSize(205, 300);
		foodTable.setEditable(true);
		foodTable.setStyle(tableStyle());

		// creates the table column showing the name of the food item
		TableColumn<HashMap.Entry<FoodItem, Integer>, String> foodColumn = new TableColumn<>("Food Item");
		foodColumn.setCellValueFactory(
				(TableColumn.CellDataFeatures<HashMap.Entry<FoodItem, Integer>, String> item) ->
						new SimpleStringProperty(item.getValue().getKey().getName()));
		foodColumn.setPrefWidth(100);

		// creates the table column showing the quantity of a food item within a meal
		TableColumn<HashMap.Entry<FoodItem, Integer>, Integer> countColumn = new TableColumn<>("Count in Meal");
		countColumn.setCellValueFactory(
				(TableColumn.CellDataFeatures<HashMap.Entry<FoodItem, Integer>, Integer> item) ->
						new ReadOnlyObjectWrapper<>(item.getValue().getValue()));
		countColumn.setPrefWidth(100);
		countColumn.setEditable(true);

		//user determines count of food item in meal
		countColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter(){
			@Override
			public Integer fromString(String value){
				try {
					return super.fromString(value);
				} catch(Exception exception){
					return null;
				}
			}
		}));
		countColumn.setOnEditCommit(event -> {
			int errorIndex = 0;
			Alert invalidInput;
			String errorTitle = "Invalid Input";
			String errorMessage = "";
			int oldValue = event.getOldValue();

			//alerts user if they do not put input an integer
			if (event.getNewValue() == null){
				errorMessage = "Input must be an integer.";
				errorIndex = 1;
			}
			//if given an integer value, proceed
			else {
				int newValue = event.getNewValue();
				double maxPayload = currentSimulation.getDroneSettings().getMaxPayloadWeight();
				double itemWeight = event.getRowValue().getKey().getWeight();

				double mealWeight = 0;
				HashMap.Entry <FoodItem, Integer> currentItem;
				for (int index = 0; index < event.getTableView().getItems().size(); index++){
					currentItem = event.getTableView().getItems().get(index);
					if (currentItem.equals(event.getRowValue())){	//make sure new value is accounted for
						mealWeight += newValue * (itemWeight);
					}
					else{
						mealWeight += currentItem.getValue() * (currentItem.getKey().getWeight());
					}
				}

				//alerts user if current meal's weight exceeds drone's maximum capacity
				if (mealWeight > maxPayload) {
					errorMessage = "Meal weight cannot exceed " + maxPayload + " oz.";
					errorIndex = 1;
				}
				//alerts user if the number of the specified food item is a negative number
				else if (newValue <= 0) {
					errorMessage = "Negative values or 0 not permitted";
					errorIndex = 1;
				}
			}

			//sets number of specified food item if the inputted value passes the necessary criteria
			if(errorIndex == 0) {
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setValue(event.getNewValue());
			}
			//reset table if new value does not meet the necessary criteria
			else {
				invalidInput = setErrorAlert(errorTitle, errorMessage);
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setValue(oldValue);
				foodTable.refresh();
				invalidInput.showAndWait();
			}

		});//end of event for adding number of food items to meal

		foodTable.getColumns().setAll(foodColumn, countColumn);
		foodTable.setPrefWidth(200);
		foodTable.setPrefHeight(300);

		StackPane tableLayout = new StackPane();
		tableLayout.setAlignment(Pos.TOP_RIGHT);
		tableLayout.setMaxSize(202, 300);
		tableLayout.getChildren().add(foodTable);

		Button save = new Button ("OK");
		save.setStyle(primaryButtonStyle());
		save.setOnAction(event ->{
			int errorIndex = 0;
			Alert invalidInput;
			String errorTitle = "Invalid Meal Input";
			String errorMessage = "";

			double mealWeight = 0;
			HashMap.Entry <FoodItem, Integer> currentItem;
			for (int index = 0; index < foodTable.getItems().size(); index++){
				currentItem = foodTable.getItems().get(index);
				mealWeight += currentItem.getValue() * (currentItem.getKey().getWeight());
			}

			String newMealName = nameField.getText();

			//alerts user if meal does not have a name
			if (newMealName.equals("")) {
				errorIndex = 1;
				errorMessage = "Meal must be given a name.";
			}
			//alerts user if meal does not contain any food items
			else if(mealWeight <= 0){
				errorIndex = 1;
				errorMessage = "Meal must have food items.";
			}
			else {
				for(Meal meal: currentSimulation.getMealTypes()) {
					if(newMealName.equals(meal.getName())) {
						errorIndex = 1;
						errorMessage = "Meal with name already exists.";
						break;
					}
				}
			}

			if (errorIndex == 0){
				//adds specified number of food items to meal
				ArrayList <FoodItem> foodToAdd = new ArrayList<>();
				for (int index = 0; index < currentSimulation.getFoodItems().size(); index++){
					int countFood = foodInMeal.get(index).getValue();
					for (int countIndex = 0; countIndex < countFood; countIndex++){
						foodToAdd.add(foodInMeal.get(index).getKey());
					}
				}

				//add new meal to simulation
				currentSimulation.addMealType(new Meal(foodToAdd, newMealName, 0));

				//alerts user that probability of new meal is zero
				Alert probabilityNotice = new Alert(Alert.AlertType.INFORMATION);
				probabilityNotice.setTitle("Meal Created with Probability of 0");
				probabilityNotice.setHeaderText(null);
				probabilityNotice.setContentText("Your meal has been created with its probability " +
						"set to 0. You can change that through the probability table on the meals " +
						"edit page. Remember, probabilities of all meals combined should equal 1.");
				probabilityNotice.showAndWait();

				editMealsPage();
			} //end of if statement for when meal values are acceptable
			else{
				invalidInput = setErrorAlert(errorTitle, errorMessage);
				invalidInput.showAndWait();
			}
		}); //end of event for adding meal to table

		VBox tableSaveLayout = new VBox(10);
		tableSaveLayout.getChildren().addAll(tableLayout, save);
		tableSaveLayout.setAlignment(Pos.CENTER);

		VBox mainLayout = new VBox(20);
		mainLayout.getChildren().addAll(nameLayout, tableSaveLayout);
		mainLayout.setAlignment(Pos.CENTER);

		VBox centerLayout = new VBox(80);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, mainLayout);

		settingLayout = new HBox();
		settingLayout.getChildren().addAll(centerLayout);
		settingLayout.setStyle(PRIMARY_BACKGROUND_COLOR);

		root = new StackPane();
		root.getChildren().addAll(settingLayout, centerLayout);

		Scene addMealPage = new Scene (root, 900, 600);
		window.setScene(addMealPage);
	}

	/**
	 * Creates GUI page for drone settings
	 * @author Izzy Patnode
	 */
	public void editDronePage() {
		VBox leftLayout = sideBarLayout("Drone");

		settingTitle();

		Font font = Font.font("Helvetica", 15);

		//creates gridpane containing the current drone settings
		Text maxPayload = new Text("Max Cargo Weight (lbs): ");
		maxPayload.setFont(font);

		Text speed = new Text("Average Cruising Speed (mph): ");
		speed.setFont(font);

		Text maxFlight = new Text("Max Flight Time (minutes): ");
		maxFlight.setFont(font);

		Text turnAround = new Text("Turn-around Time (minutes): ");
		turnAround.setFont(font);

		Text unloadTime = new Text("Unloading Delay (seconds): ");
		unloadTime.setFont(font);

		// fetches and formats the maximum payload capacity of the drone
		double currentPayload = currentSimulation.getDroneSettings().getMaxPayloadWeight() / OUNCES_PER_POUND;
		TextField dronePayload = new TextField(String.format("%.2f", currentPayload));
		dronePayload.setMaxWidth(80);

		// fetches and formats the maximum cruising speed the drone can fly at
		double currentSpeed = (currentSimulation.getDroneSettings().getCruisingSpeed() * SECONDS_PER_HOUR)
				/ FEET_PER_MILE;
		TextField droneSpeed = new TextField(String.format("%.2f", currentSpeed));
		droneSpeed.setMaxWidth(80);

		// fetches and formats the maximum duration of time the drone can fly
		double currentFlightTime = currentSimulation.getDroneSettings().getFlightTime() / SECONDS_PER_MINUTE;
		TextField droneFlight = new TextField(String.format("%.2f", currentFlightTime));
		droneFlight.setMaxWidth(80);

		// fetches and formats the duration of time the drone takes to recharge/reload
		double currentTurnAround = currentSimulation.getDroneSettings().getTurnAroundTime() / SECONDS_PER_MINUTE;
		TextField droneTurnAround = new TextField(String.format("%.2f", currentTurnAround));
		droneTurnAround.setMaxWidth(80);

		// fetches and formats the duration of time the drone takes to deliver at a destination
		double currentDelivery = currentSimulation.getDroneSettings().getDeliveryTime();
		TextField droneUnload = new TextField(String.format("%.2f", currentDelivery));
		droneUnload.setMaxWidth(80);

		//creates gridpane for drone settings
		GridPane droneSettings = new GridPane();
		droneSettings.setAlignment(Pos.CENTER);
		droneSettings.setVgap(15);
		droneSettings.setMaxSize(300, 300);

		//adds cells to gridpane
		droneSettings.add(maxPayload, 0, 0);
		droneSettings.add(dronePayload, 1, 0);

		droneSettings.add(speed, 0, 1);
		droneSettings.add(droneSpeed, 1, 1);

		droneSettings.add(maxFlight, 0, 2);
		droneSettings.add(droneFlight, 1, 2);

		droneSettings.add(turnAround, 0, 3);
		droneSettings.add(droneTurnAround, 1, 3);

		droneSettings.add(unloadTime, 0, 4);
		droneSettings.add(droneUnload, 1, 4);

		VBox centerLayout = new VBox();
		centerLayout.setSpacing(140);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, droneSettings);

		//allows user to edit current drone settings
		Button editButton = new Button("Save Changes");
		editButton.setStyle(primaryButtonStyle());

		editButton.setOnAction(event -> {
			Alert errorAlert;
			String errorTitle = "Invalid Input";
			String errorMessage = "";

			//attempts to change current drone settings to new drone settings
			Drone drone = currentSimulation.getDroneSettings();
			try {
				double newDronePayload = Double.parseDouble(dronePayload.getText())  * OUNCES_PER_POUND;
				double newCruisingSpeed = (Double.parseDouble(droneSpeed.getText()) * FEET_PER_MILE) / SECONDS_PER_HOUR;
				double newFlightTime = Double.parseDouble(droneFlight.getText())  * SECONDS_PER_MINUTE;
				double newTurnAround = Double.parseDouble(droneTurnAround.getText())  * SECONDS_PER_MINUTE;
				double newDeliveryTime = Double.parseDouble(droneUnload.getText());

				int errorIndex = 0;

				if(newDronePayload <= 0 || newCruisingSpeed <= 0 || newFlightTime <= 0 ||
						newTurnAround <= 0 || newDeliveryTime <= 0) {
					errorIndex = 1;
					errorMessage = "Input must be greater than zero.";
				} //end of if statement where input is less than or equal to zero
				else {
					drone.setTurnAroundTime(newTurnAround);

					double maxAllowedDistance = ((newFlightTime - newDeliveryTime) / 2)  * 0.95 * newCruisingSpeed;

					for(Point point: currentSimulation.getDeliveryPoints().getPoints()) {
						if(maxAllowedDistance < point.distanceFromPoint(null)) {
							errorMessage = "New drone settings do not allow for all points to be reached.";
							errorIndex = 1;
						}
					}

					if(errorIndex == 0) {
						drone.setDeliveryTime(newDeliveryTime);
						drone.setCruisingSpeed(newCruisingSpeed);
						drone.setFlightTime(newFlightTime);

						for(Meal meal: currentSimulation.getMealTypes()) {
							if(errorIndex == 0) {
								double mealWeight = 0.0;

								for (FoodItem food : meal.getFoods()) {
									mealWeight += food.getWeight();
								}

								if (mealWeight > newDronePayload) {
									errorIndex = 1;
									errorMessage = "Existing meal weights exceed new drone payload.";
								}
							}
						} //end of if statement where all but the new drone payload meet respective criteria
					} //end of if statement where drone can reach all points

					if(errorIndex == 0) {
						drone.setMaxPayloadWeight(newDronePayload);
					}

				} //end of else statement where inputs are valid numbers

				if(errorIndex == 1) {
					//resets drone settings to what they were before editing
					drone.setMaxPayloadWeight(currentPayload * OUNCES_PER_POUND);
					drone.setCruisingSpeed((currentSpeed * FEET_PER_MILE) / SECONDS_PER_HOUR);
					drone.setFlightTime(currentFlightTime * SECONDS_PER_MINUTE);
					drone.setTurnAroundTime(currentTurnAround * SECONDS_PER_MINUTE);
					drone.setDeliveryTime(currentDelivery);

					dronePayload.setText(String.format("%.2f", currentPayload));
					droneSpeed.setText(String.format("%.2f", currentSpeed));
					droneFlight.setText(String.format("%.2f", currentFlightTime));
					droneTurnAround.setText(String.format("%.2f", currentTurnAround));
					droneUnload.setText(String.format("%.2f", currentDelivery));

					errorAlert = setErrorAlert(errorTitle, errorMessage);
					errorAlert.showAndWait();
				} //end of if statement where one or more inputs do not meet respective requirements
				else {
					//gives the user confirmation that their changes have been applied to the drone settings
					Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
					saveAlert.setTitle("Confirm Changes");
					saveAlert.setHeaderText("Changes Saved!");
					saveAlert.showAndWait();
				}
			} //end of try block
			catch(NumberFormatException illegalFormat) {
				//alerts user if the changed value(s) are not acceptable
				errorMessage = "Input must be integers or decimals.";
				errorAlert = setErrorAlert(errorTitle, errorMessage);

				//resets drone settings to what they were before editing
				drone.setMaxPayloadWeight(currentPayload * OUNCES_PER_POUND);
				drone.setCruisingSpeed(currentSpeed * FEET_PER_MILE / SECONDS_PER_HOUR);
				drone.setFlightTime(currentFlightTime * SECONDS_PER_MINUTE);
				drone.setTurnAroundTime(currentTurnAround * SECONDS_PER_MINUTE);
				drone.setDeliveryTime(currentDelivery);

				dronePayload.setText(String.format("%.2f", currentPayload));
				droneSpeed.setText(String.format("%.2f", currentSpeed));
				droneFlight.setText(String.format("%.2f", currentFlightTime));
				droneTurnAround.setText(String.format("%.2f", currentTurnAround));
				droneUnload.setText(String.format("%.2f", currentDelivery));

				errorAlert.showAndWait();
			} //end of catch block
		}); //end of event for editing drone settings

		VBox rightLayout = new VBox();
		rightLayout.setAlignment(Pos.BOTTOM_LEFT);
		rightLayout.setPadding(new Insets(0, 0, 150, 0));
		rightLayout.getChildren().add(editButton);

		HBox mainLayout = new HBox();
		mainLayout.getChildren().addAll(centerLayout, rightLayout);

		//arranges all elements of the page on the screen
		settingLayout = new HBox(130);
		settingLayout.getChildren().addAll(leftLayout, mainLayout);
		settingLayout.setStyle(PRIMARY_BACKGROUND_COLOR);

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene droneEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(droneEditPage);
	}

	/**
	 * Determines the bounds of the x and y axis, used when creating the table and when shrinking the bounds is
	 * necessary, gives the map a good range so all points are visible
	 * @param mapPoints a list of points to determine the bounds of the map's axes from
	 * @return the x-axis and y-axis bounds
	 * @author Izzy Patnode
	 */
	public ArrayList<Integer> setAxes(ObservableList<Point> mapPoints) {
		ArrayList<Integer> axesBounds = new ArrayList<>();

		int upperYBound = mapPoints.get(0).getY();
		int upperXBound = mapPoints.get(0).getX();
		int lowerYBound = mapPoints.get(0).getY();
		int lowerXBound = mapPoints.get(0).getX();

		for(Point point : mapPoints) {
			int currentY = point.getY();
			int currentX = point.getX();

			if (currentY >= upperYBound) {
				upperYBound = currentY;
			}
			if(currentY <= lowerYBound) {
				lowerYBound = currentY;
			}
			if(currentX >= upperXBound) {
				upperXBound = currentX;
			}
			if(currentX <= lowerXBound) {
				lowerXBound = currentX;
			}
		}

		axesBounds.addAll(List.of(lowerXBound, upperXBound, lowerYBound, upperYBound));

		return axesBounds;
	}

	/**
	 * Changes bounds of map if the new coordinate resides outside of the map's current scope
	 * @param newXValue the updated x coordinate
	 * @param newYValue the updated y coordinate
	 * @param xAxis used to set x axes
	 * @param yAxis used to set y axes
	 * @author Izzy Patnode
	 */
	public void setNewBounds(int newXValue, int newYValue, NumberAxis xAxis, NumberAxis yAxis) {
		int currentUpperXBound = (int) xAxis.getUpperBound() - 100;
		int currentLowerXBound = (int) xAxis.getLowerBound() + 100;

		if(newXValue >= currentUpperXBound) {
			xAxis.setUpperBound(newXValue + 100);
		}
		if(newXValue <= currentLowerXBound) {
			xAxis.setLowerBound(newXValue - 100);
		}

		int currentUpperYBound = (int) yAxis.getUpperBound() - 100;
		int currentLowerYBound = (int) yAxis.getLowerBound() + 100;

		if(newYValue >= currentUpperYBound) {
			yAxis.setUpperBound(newYValue + 100);
		}
		if(newYValue <= currentLowerYBound) {
			yAxis.setLowerBound(newYValue - 100);
		}
	}

	/**
	 * Creates GUI page for map settings
	 * @author Izzy Patnode
	 */
	public void editMapPage() {
		VBox leftLayout = sideBarLayout("Map");

		settingTitle();

		//gets list of current map destinations
		ObservableList<Point> mapPoints = currentSimulation.getDeliveryPoints().getPoints();

		//finds maximum and minimum x and y values to make axes of map have a good range
		ArrayList<Integer> bounds = setAxes(mapPoints);

		//creates the axes for the map scatter plot using the
		NumberAxis xAxis = new NumberAxis(bounds.get(0) - 100,
				bounds.get(1) + 100, 100);
		xAxis.setLabel("");
		xAxis.setTickMarkVisible(false);
		NumberAxis yAxis = new NumberAxis(bounds.get(2) - 100,
				bounds.get(3) + 100, 100);
		yAxis.setLabel("");
		yAxis.setTickMarkVisible(false);

		//creates scatter plot for map
		ScatterChart<Number, Number> map = new ScatterChart<>(xAxis, yAxis);
		map.setHorizontalGridLinesVisible(false);
		map.setVerticalGridLinesVisible(false);
		map.setLegendVisible(false);
		map.setStyle("-fx-background: #e0e0e0");

		//creates points from destination coordinates for scatter plot
		XYChart.Series<Number, Number> mapValues = new XYChart.Series<>();

		for(Point destination : mapPoints) {
			mapValues.getData().add(new XYChart.Data<>(destination.getX(), destination.getY()));
		}

		//adds points to scatter plot
		map.getData().add(mapValues);

		for(Node mapPoint : map.lookupAll(".series" + 0)) {
			mapPoint.setStyle("-fx-background-color: #0047ab");
		}

		//arranges map
		StackPane plotLayout = new StackPane();
		plotLayout.setMaxSize(300, 300);
		plotLayout.getChildren().add(map);
		plotLayout.setAlignment(Pos.CENTER_RIGHT);

		//creates table of current simulation's points
		TableView<Point> mapTable = new TableView<>();

		//adds cell values to table
		mapTable.setItems(mapPoints);
		mapTable.setEditable(true);
		mapTable.setStyle(tableStyle());

		//creates columns for table
		TableColumn<Point, String> pointHeading = new TableColumn<>("Drop-Off Point");
		pointHeading.setCellValueFactory(new PropertyValueFactory<>("name"));

		//allows user to edit the name of a point already in the table
		pointHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		pointHeading.setOnEditCommit(event -> {
			int selectedRow = event.getTablePosition().getRow();
			String oldName = event.getOldValue();
			String newName = event.getNewValue();
			Point updatedPoint = event.getTableView().getItems().get(selectedRow);

			if(newName.equals("")) {
				Alert invalidName = setErrorAlert("Invalid Name", "Point name cannot be null.");
				updatedPoint.setName(oldName);
				invalidName.showAndWait();
			}
			else {
				int errorIndex = 0;
				//alerts user if a point with the inputted name exists
				for(Point point: mapPoints) {
					if(newName.equals(point.getName()) && (updatedPoint.getX() != point.getX()
							|| updatedPoint.getY() != updatedPoint.getY())) {
						errorIndex = 1;
						break;
					}
				}

				if(errorIndex == 0) {
					updatedPoint.setName(newName);
				}
				else {
					Alert invalidName = setErrorAlert("Invalid Name",
							"Point with inputted name already exists.");
					updatedPoint.setName(oldName);
					invalidName.showAndWait();
				}
			}

			event.getTableView().refresh();
		});//end of changing point name event

		TableColumn<Point, String> xyHeading = new TableColumn<>("(x,y)");
		xyHeading.setCellValueFactory(new PropertyValueFactory<>("coordinates"));

		//allows user to edit the coordinates of a point already in the table
		xyHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		xyHeading.setOnEditCommit(event -> {
			int selectedRow = event.getTablePosition().getRow();
			String oldCoordinates = event.getOldValue();

			Alert invalidInput;
			String alertTitle = "";
			String alertMessage = "";

			int errorIndex = 0;

			//attempts to update specified point's coordinates to given values
			try {
				int oldXValue = event.getTableView().getItems().get(selectedRow).getX();
				int oldYValue = event.getTableView().getItems().get(selectedRow).getY();

				//checks that the point does not reside at the origin (0,0) since the origin must exist at all times
				if((oldXValue != 0 || oldYValue != 0)) {
					Drone currentDrone = currentSimulation.getDroneSettings();
					double maxDistanceAllowed = ((currentDrone.getFlightTime() - currentDrone.getDeliveryTime())
							/ 2) * currentDrone.getCruisingSpeed() * 0.95;

					event.getTableView().getItems().get(selectedRow).setCoordinates(event.getNewValue() + "");

					Point newPoint = event.getTableView().getSelectionModel().getSelectedItem();

					//checks that the new coordinates are in range of the drone (the drone can fly that distance)
					if(newPoint.distanceFromPoint(null) <= maxDistanceAllowed) {
						int newXValue = newPoint.getX();
						int newYValue = newPoint.getY();

						//checks that a point does not already have the given coordinates
						for(Point point: mapPoints) {
							if(!point.getName().equals(newPoint.getName()) && newXValue == point.getX()
									&& newYValue == point.getY()) {
								alertTitle = "Invalid Input";
								alertMessage = "Point with inputted coordinates already exists.";
								errorIndex = 1;
								break;
							}
						}

						if(errorIndex == 0) {
							//updates map so the point is now located at its new coordinates
							XYChart.Data<Number, Number> selectedPoint = mapValues.getData().get(selectedRow);
							selectedPoint.setXValue(newXValue);
							selectedPoint.setYValue(newYValue);

							if((Math.abs(newXValue) > Math.abs(oldXValue)) ||
									(Math.abs(newYValue) > Math.abs(oldYValue))){
								setNewBounds(newXValue, newYValue, xAxis, yAxis);
							}
							else {
								ArrayList<Integer> newBounds = setAxes(mapPoints);

								xAxis.setLowerBound(newBounds.get(0) - 100);
								xAxis.setUpperBound(newBounds.get(1) + 100);

								yAxis.setLowerBound(newBounds.get(2) - 100);
								yAxis.setUpperBound(newBounds.get(3) + 100);
							}
						}
					} //end of if statement where new coordinates are legal
					else {
						//alerts user that the new coordinates are not in range of the drone
						alertTitle = "Maximum Distance Exceeded";
						alertMessage = "Coordinates are outside the maximum distance the drone can fly";
						errorIndex = 1;
					}

				} //end of if statement where coordinates being changed are not the origin
				else {
					//alerts user that they are attempting to change the coordinates of the origin (not allowed)
					alertTitle = "Origin Change Attempted";
					alertMessage = "Origin must be at (0,0) and cannot be changed";
					errorIndex = 1;
				}

			} //end of try block
			catch(IllegalArgumentException illegalArgument) {
				//alerts the user that the new coordinates are not valid (incorrect syntax, not integers, etc.)
				alertTitle = "Invalid Coordinates";
				alertMessage = "Input must be a (x,y) integer pair";
				errorIndex = 1;
			} //end of catch block

			if(errorIndex == 1) {
				invalidInput = setErrorAlert(alertTitle, alertMessage);
				event.getTableView().getItems().get(selectedRow).setCoordinates(oldCoordinates);
				invalidInput.showAndWait();
			}

			event.getTableView().refresh();

		}); //end of event for editing point coordinates

		//adds column headings to table
		mapTable.getColumns().setAll(pointHeading, xyHeading);
		mapTable.setPrefWidth(275);
		mapTable.setPrefHeight(300);

		//arranges table
		StackPane tableLayout = new StackPane();
		tableLayout.setMaxSize(275, 300);
		tableLayout.setAlignment(Pos.CENTER);
		tableLayout.getChildren().add(mapTable);

		//allows user to add new points
		Button addButton = new Button("Add");
		addButton.setStyle(primaryButtonStyle());

		addButton.setOnAction(event -> {
			//creates a popup window so that the current map page does not become overcrowded
			Stage addDialog = new Stage();
			addDialog.setTitle("New Point");
			addDialog.initOwner(window);
			addDialog.initModality(Modality.WINDOW_MODAL);
			addDialog.initStyle(StageStyle.UTILITY);

			//arranges textfields and labels for point information in a gridpane for simple layout
			GridPane newPointGrid = new GridPane();
			newPointGrid.setHgap(10);
			newPointGrid.setVgap(10);

			Text pointTitle = new Text("Name");
			Text coordinateTitle = new Text("Coordinates");

			TextField pointName = new TextField();
			pointName.setPromptText("Enter Name");
			TextField coordinates = new TextField();
			coordinates.setPromptText("Enter coordinates: (x,y)");

			newPointGrid.add(pointTitle, 0, 0);
			newPointGrid.add(pointName, 1, 0);
			newPointGrid.add(coordinateTitle, 0, 1);
			newPointGrid.add(coordinates, 1, 1);

			//adds new point to simulation
			Button confirmButton = new Button("Finish");
			confirmButton.setOnAction(event1 -> {
				Alert errorAlert;
				String alertTitle = "";
				String alertMessage = "";

				if(pointName.getText().equals("")) {
					alertTitle = "Unspecified Point Name";
					alertMessage = "Point name cannot be null.";
					errorAlert = setErrorAlert(alertTitle, alertMessage);
					errorAlert.showAndWait();
				}
				else {
					//creates a new point with name and empty coordinates (since we have to parse coordinate string)
					Point newPoint = currentSimulation.getDeliveryPoints().
							addPoint(pointName.getText(), 0, 0);
					int errorIndex = 0;

					//attempts to set new point's coordinates
					try {
						newPoint.setCoordinates(coordinates.getText());

						if (newPoint.getX() == 0 && newPoint.getY() == 0) {
							//alerts user that the inputted coordinates exceed the distance the drone can travel
							alertTitle = "Illegal Coordinates";
							alertMessage = "Attempting to set new point at origin (0,0) which already exists.";
							errorIndex = 1;
						} //end of if statement where user attempt to put a point at (0,0)
						else {
							Drone currentDrone = currentSimulation.getDroneSettings();
							double maxDistanceAllowed = (currentDrone.getFlightTime() *
									currentDrone.getCruisingSpeed()) / 2;

							//checks that the distance to the point from the origin is within the drone's range
							if (newPoint.distanceFromPoint(null) <= maxDistanceAllowed) {
								int newXValue = newPoint.getX();
								int newYValue = newPoint.getY();

								for(Point point: mapPoints) {
									if(!newPoint.getName().equals(point.getName()) &&newXValue == point.getX()
											&& newYValue == point.getY()) {
										errorIndex = 1;
										alertTitle = "Invalid Input";
										alertMessage = "Point already exists.";
										break;
									}
								}
								if(errorIndex == 0) {
									//adds new point to the map so the new point is visible
									mapValues.getData().add(new XYChart.Data<>(newPoint.getX(), newPoint.getY()));

									setNewBounds(newXValue, newYValue, xAxis, yAxis);

									mapTable.setItems(currentSimulation.getDeliveryPoints().getPoints());

									for (Node mapPoint : map.lookupAll(".series" + 0)) {
										mapPoint.setStyle("-fx-background-color: #0047ab");
									}

									//closes dialog and takes user back to map settings page
									addDialog.close();
								}
							} //end of statement where inputted coordinates are valid and can be added to simulation
							else {
								//alerts user that the inputted coordinates exceed the distance the drone can travel
								alertTitle = "Maximum Distance Exceeded";
								alertMessage = "Coordinates are outside the range of the drone.";
								errorAlert = setErrorAlert(alertTitle, alertMessage);
								currentSimulation.getDeliveryPoints().removePoint(newPoint);
								errorAlert.showAndWait();
							}
						} //end of else statement where point being added is not at (0,0)
					} //end of try block
					catch (IllegalArgumentException illegalArgument) {
						//alerts user that the inputted coordinates are invalid (illegal syntax, not integers, etc.)
						alertTitle = "Invalid Coordinates";
						alertMessage = "Input must be a (x,y) integer pair.";
						errorIndex = 1;
					} //end of cancel block

					//reduces having to initialize the alert for every error
					if(errorIndex == 1) {
						errorAlert = setErrorAlert(alertTitle, alertMessage);
						currentSimulation.getDeliveryPoints().removePoint(newPoint);
						errorAlert.showAndWait();
					}
				}//end of else statement where point name is valid
			}); //end of confirmation button event (adding new point to current simulation)

			//allows user to cancel (exit the popup) if they no longer want to add a new point
			Button cancelButton = new Button("Cancel");
			cancelButton.setCancelButton(true);
			cancelButton.setOnAction(event12 -> addDialog.close());

			HBox dialogButtons = new HBox(10);
			dialogButtons.getChildren().addAll(confirmButton, cancelButton);
			dialogButtons.setAlignment(Pos.BOTTOM_CENTER);

			VBox dialogLayout = new VBox(10);
			dialogLayout.getChildren().addAll(newPointGrid, dialogButtons);

			Scene dialogScene = new Scene(dialogLayout);
			addDialog.setScene(dialogScene);
			addDialog.show();
		}); //end of event for adding delivery points to the current simulation (creating popup window)

		//allows user to delete delivery points from the current simulation
		Button deleteButton = new Button("Delete");
		deleteButton.setStyle(primaryButtonStyle());
		deleteButton.setOnAction(event -> {
			int deletedRow = mapTable.getSelectionModel().getSelectedIndex();

			Alert errorAlert;
			String alertTitle;
			String alertMessage;

			//alerts user if they attempt to delete a point without selecting the point first
			if(deletedRow < 0) {
				alertTitle = "Invalid Deletion";
				alertMessage = "Map point not selected.";
				errorAlert = setErrorAlert(alertTitle, alertMessage);
				errorAlert.showAndWait();
			}
			else {
				Point deletedPoint = mapTable.getSelectionModel().getSelectedItem();

				//checks to see if the point the user wants to delete is the origin (not allowed)
				if (deletedPoint.getX() != 0 || deletedPoint.getY() != 0) {
					//removes point from map
					mapValues.getData().remove(deletedRow);

					currentSimulation.getDeliveryPoints().removePoint(deletedPoint);
					mapPoints.remove(deletedPoint);

					ArrayList<Integer> newBounds = setAxes(mapPoints);

					xAxis.setLowerBound(newBounds.get(0) - 100);
					xAxis.setUpperBound(newBounds.get(1) + 100);

					yAxis.setLowerBound(newBounds.get(2) - 100);
					yAxis.setUpperBound(newBounds.get(3) + 100);

					mapTable.refresh();

				}
				else {
					//alerts user if they attempt to delete the origin
					alertTitle = "Origin Deletion Attempted";
					alertMessage = "Origin point cannot be deleted.";
					errorAlert = setErrorAlert(alertTitle, alertMessage);
					errorAlert.showAndWait();
				}
			} //end of else statement where user selects a valid row
		}); //end of event for deleting delivery points

		HBox addDeleteButtons = new HBox(10);
		addDeleteButtons.setAlignment(Pos.CENTER_RIGHT);
		addDeleteButtons.setPadding(new Insets(0, 80, 0, 0));
		addDeleteButtons.getChildren().addAll(addButton, deleteButton);

		VBox fullTableDisplay = new VBox(5);
		fullTableDisplay.setAlignment(Pos.CENTER_RIGHT);
		fullTableDisplay.getChildren().addAll(tableLayout, addDeleteButtons);

		HBox mapDisplay = new HBox(50);
		mapDisplay.getChildren().addAll(plotLayout, fullTableDisplay);
		mapDisplay.setAlignment(Pos.CENTER_LEFT);
		mapDisplay.setPadding(new Insets(0, 0, 0, 10));

		titleLayout.setAlignment(Pos.CENTER_LEFT);

		VBox centerLayout = new VBox();
		centerLayout.setSpacing(70);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, mapDisplay);

		//allows user to load in a map from an existing simulation save file
		Button loadButton = new Button("Load Map");
		loadButton.setStyle(primaryButtonStyle());
		loadButton.setOnAction(click -> {
			// try and fetch the configuration file from the user
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Import Settings");
			fileChooser.getExtensionFilters().add(
					new FileChooser.ExtensionFilter("XML", "*.xml"));
			File file = fileChooser.showOpenDialog(window);

			// confirm that the user wishes to overwrite the current point settings
			if (file != null) {
				var newSimulation = Configuration.getConfigurationFromFile(file);
				if (newSimulation != null) {
					var points = newSimulation.getDeliveryPoints();

					// alert box to ask for confirmation before overwriting all delivery points
					Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
					confirmation.setTitle("Import Confirmation");
					confirmation.setHeaderText("The following delivery points will overwrite the existing ones.\n" +
							"Are you sure you wish to proceed?");

					// create a formatted table to display new delivery points
					var importPointTable = new TableView<Point>();
					importPointTable.setItems(points.getPoints());
					importPointTable.setStyle(tableStyle());

					// table column to list the names of the delivery locations
					var importPointName = new TableColumn<Point, String>("Drop-Off Point");
					importPointName.setCellValueFactory(new PropertyValueFactory<>("name"));

					// table column to list the coordinates of the locations
					var importPointCoordinates = new TableColumn<Point, String>("(X,Y) Coordinates");
					importPointCoordinates.setCellValueFactory(new PropertyValueFactory<>("coordinates"));

					// add the columns to the table and the table to the alert box
					importPointTable.getColumns().add(importPointName);
					importPointTable.getColumns().add(importPointCoordinates);
					confirmation.getDialogPane().setContent(importPointTable);

					var answer = confirmation.showAndWait();
					if (answer.isPresent()) {
						if (answer.get().getText().equals("OK")) {
							currentSimulation.setDeliveryPoints(points);
							editMapPage();
						}
					}
				} else {
					// inform that something failed when trying to parse the save file
					Alert corruptFile = setErrorAlert("Invalid Save File",
							"No simulation settings were found in" + file.getName());
					corruptFile.showAndWait();
				}
			}
		});

		HBox loadDisplay = new HBox();
		loadDisplay.getChildren().add(loadButton);
		loadDisplay.setAlignment(Pos.CENTER_RIGHT);
		loadDisplay.setPadding(new Insets(50,95,0,0));

		VBox mainLayout = new VBox();
		mainLayout.getChildren().addAll(centerLayout, loadDisplay);

		//arranges all elements of the page on the screen
		settingLayout = new HBox(130);
		settingLayout.getChildren().addAll(leftLayout, mainLayout);
		settingLayout.setStyle(PRIMARY_BACKGROUND_COLOR);

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene mapEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(mapEditPage);
	}

	/**
	 * Displays results from simulation
	 * @author Rachel Franklin
	 */
	public void resultsPage() {
		//creates heading of page
		title = new Text("Simulation Results");
		title.setStyle(FONT_TYPE + "-fx-font-size: 35; -fx-fill: #0047ab");
		title.setWrappingWidth(450);
		title.setTextAlignment(TextAlignment.CENTER);

		//aligns title
		titleLayout = new HBox();
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.CENTER);

		//icon created by Google, allows us to display the icon png on the page
		Image homeIcon = new Image("file:resources/home-button.png");
		ImageView homeView = new ImageView(homeIcon);

		Button homeButton = new Button("", homeView);
		homeButton.setStyle(PRIMARY_BACKGROUND_COLOR);
		homeButton.setOnAction(event -> window.setScene(mainMenu));

		iconLayout = new HBox();
		iconLayout.setAlignment(Pos.TOP_LEFT);
		iconLayout.setPadding(new Insets(0, 0, 0, 15));
		iconLayout.getChildren().add(homeButton);
		iconLayout.setStyle(PRIMARY_BACKGROUND_COLOR);

		//configures display of home button and page title
		HBox topLayout = new HBox();
		topLayout.setSpacing(141);
		topLayout.setAlignment(Pos.TOP_LEFT);
		topLayout.getChildren().addAll(iconLayout, titleLayout);

		String resultsTextStyle = BOLD_FONT_STYLE + FONT_TYPE + "-fx-font-size: 18; -fx-fill: #0047ab";

		//sets up statistics for FIFO
		Text fifoTitle = new Text("FIFO Delivery");
		fifoTitle.setStyle(resultsTextStyle);
		fifoTitle.setWrappingWidth(300);
		fifoTitle.setTextAlignment(TextAlignment.CENTER);

		double fifoAverageTime = results.getAverageFifoTime();

		Text fifoAverage = new Text(String.format("Average Delivery Time: %.1f minutes",
				fifoAverageTime / SECONDS_PER_MINUTE));
		fifoAverage.setStyle(resultsTextStyle);
		fifoAverage.setWrappingWidth(300);
		fifoAverage.setTextAlignment(TextAlignment.CENTER);

		double fifoWorstTime = results.getWorstFifoTime();

		Text fifoWorst = new Text(String.format("Worst Delivery Time: %.1f minutes",
				fifoWorstTime / SECONDS_PER_MINUTE));
		fifoWorst.setStyle(resultsTextStyle);
		fifoWorst.setWrappingWidth(300);
		fifoWorst.setTextAlignment(TextAlignment.CENTER);

		double fifoExpiredPercent = results.getPercentFifoExpired();
		DecimalFormat decimalFormat = new DecimalFormat("##.####");
		decimalFormat.format(fifoExpiredPercent);

		Text fifoExpired = new Text(String.format("Average Expired Orders: %.2f%%",
				(fifoExpiredPercent*100)));
		fifoExpired.setStyle(resultsTextStyle);
		fifoExpired.setWrappingWidth(300);
		fifoExpired.setTextAlignment(TextAlignment.CENTER);

		VBox fifoLayout = new VBox();
		fifoLayout.setSpacing(5);
		fifoLayout.setAlignment(Pos.TOP_CENTER);
		fifoLayout.getChildren().addAll(fifoTitle, fifoAverage, fifoWorst, fifoExpired);

		//sets up statistics for knapsack packing
		Text knapsackTitle = new Text("Knapsack Packing Delivery");
		knapsackTitle.setStyle(resultsTextStyle);
		knapsackTitle.setWrappingWidth(300);
		knapsackTitle.setTextAlignment(TextAlignment.CENTER);

		double knapsackAverageTime = results.getAverageKnapsackTime();

		Text knapsackAverage = new Text(String.format("Average Delivery Time: %.1f minutes",
				knapsackAverageTime / SECONDS_PER_MINUTE));
		knapsackAverage.setStyle(resultsTextStyle);
		knapsackAverage.setWrappingWidth(300);
		knapsackAverage.setTextAlignment(TextAlignment.CENTER);

		double knapsackWorstTime = results.getWorstKnapsackTime();

		Text knapsackWorst = new Text(String.format("Worst Delivery Time: %.1f minutes",
				knapsackWorstTime / SECONDS_PER_MINUTE));
		knapsackWorst.setStyle(resultsTextStyle);
		knapsackWorst.setWrappingWidth(300);
		knapsackWorst.setTextAlignment(TextAlignment.CENTER);

		double knapsackExpiredPercent = results.getPercentKnapsackExpired();
		decimalFormat.format(knapsackExpiredPercent);	//Decimal format initialized earlier

		Text knapsackExpired = new Text(String.format("Average Expired Orders: %.2f%%",
				(knapsackExpiredPercent*100)));
		knapsackExpired.setStyle(resultsTextStyle);
		knapsackExpired.setWrappingWidth(300);
		knapsackExpired.setTextAlignment(TextAlignment.CENTER);

		VBox knapsackLayout = new VBox();
		knapsackLayout.setSpacing(5);
		knapsackLayout.setAlignment(Pos.TOP_CENTER);
		knapsackLayout.getChildren().addAll(knapsackTitle, knapsackAverage, knapsackWorst, knapsackExpired);

		//sets up layout of statistics
		HBox statsLayout = new HBox();
		statsLayout.setAlignment(Pos.CENTER);
		statsLayout.getChildren().addAll(fifoLayout, knapsackLayout);

		//sets up BarChart (histogram)
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
		barChart.setCategoryGap(1.5);
		barChart.setBarGap(0.4);
		xAxis.setLabel("Delivery Time (in minutes)");
		yAxis.setLabel("Number of Orders");

		final int columns = 26;						// number of columns to display
		var fifoDurations = new int[columns];		// number of fifo deliveries for each of delivery time
		var knapsackDurations = new int[columns];	// number of knapsack deliveries for each delivery time

		// absolute shortest delivery time of all trial averages
		int minDuration = (int) results.getWorstFifoTime();

		// make minDuration reflect the smallest fifo delivery time
		for (var fifoTime : results.getFifoTimes()) {
			int duration = (int) (fifoTime / SECONDS_PER_MINUTE);
			if (minDuration > duration) minDuration = duration;
		}

		// make minDuration reflect the smallest knapsack delivery time if shorter than the current value
		for (var knapsackTime : results.getKnapsackTimes()) {
			int duration = (int) (knapsackTime / SECONDS_PER_MINUTE);
			if (minDuration > duration) minDuration = duration;
		}

		// count the number of fifo occurrences for each duration of delivery time
		for (var fifoTime : results.getFifoTimes()) {
			int duration = (int)(Math.floor(fifoTime) / SECONDS_PER_MINUTE);
			int index = (duration - minDuration);
			if (index < columns) fifoDurations[index]++;
			else fifoDurations[columns - 1]++;
		}

		// count the number of knapsack occurrences for each duration of delivery time
		for (var knapsackTime : results.getKnapsackTimes()) {
			int duration = (int)(Math.floor(knapsackTime) / SECONDS_PER_MINUTE);
			int index = (duration - minDuration);
			if (index < columns) knapsackDurations[index]++;
			else knapsackDurations[columns - 1]++;
		}

		// create the data series for fifo durations
		var fifoSeries = new XYChart.Series<String, Number>(
				"FIFO", FXCollections.observableArrayList());
		var fifoSeriesData = fifoSeries.getData();

		//adds the data values for fifo into the series
		for (int index = 0; index < columns; index++) {
			int start = minDuration + index;
			int end = start + 1;
			var label = index == columns - 1 ? start + "+" : start + "-" + end;
			var data = new XYChart.Data<String, Number>(label, fifoDurations[index]);
			fifoSeriesData.add(data);
		}

		// create the data series for knapsack durations
		var knapsackSeries = new XYChart.Series<String, Number>(
				"Knapsack Packing", FXCollections.observableArrayList());
		var knapsackSeriesData = knapsackSeries.getData();

		//adds the values for knapsack into the series
		for (int index = 0; index < columns; index++) {
			int start = minDuration + index;
			int end = start + 1;
			var label = index == columns - 1 ? start + "+" : start + "-" + end;
			var data = new XYChart.Data<String, Number>(label, knapsackDurations[index]);
			knapsackSeriesData.add(data);
		}

		// add the series to the chart
		barChart.getData().add(fifoSeries);
		barChart.getData().add(knapsackSeries);

		// color the data columns to distinguish them from each other
		for (var data : fifoSeries.getData())
			data.getNode().setStyle("-fx-bar-fill: #ae000b;");
		for (var data : knapsackSeries.getData())
			data.getNode().setStyle("-fx-bar-fill: #0047ab;");

		barChart.setStyle("-fx-background: #e0e0e0");

		//allows user to save the full results
		Button saveButton = new Button("Save Results");
		saveButton.setStyle(primaryButtonStyle());

		saveButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Results");
			fileChooser.getExtensionFilters().add(
					new FileChooser.ExtensionFilter("CSV", "*.csv")
			);
			File file = fileChooser.showSaveDialog(window);
			if (file != null)
			    Configuration.getInstance().saveResults(results, file);
		}); //end of save results event

		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		buttonBox.setPadding(new Insets(0,50, 30, 0));
		buttonBox.getChildren().add(saveButton);

		//combine separate layouts into one
		VBox finalLayout = new VBox();
		finalLayout.getChildren().addAll(topLayout, statsLayout, barChart, buttonBox);
		finalLayout.setStyle(PRIMARY_BACKGROUND_COLOR);

		StackPane root = new StackPane();
		root.getChildren().add(finalLayout);

		Scene resultsPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(resultsPage);

		//change legend colors to reflect bar colors
		//note - this must be done after scene is set
		for (Node node : barChart.lookupAll(".bar-legend-symbol.default-color0")) {
			node.setStyle("-fx-background-color: #ae000b;");
		}
		for (Node node : barChart.lookupAll(".bar-legend-symbol.default-color1")) {
			node.setStyle("-fx-background-color: #0047ab;");
		}
	}
}