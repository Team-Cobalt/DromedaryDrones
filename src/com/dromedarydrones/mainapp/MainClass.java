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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

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
	private HBox settingLayout; //layout of all icons in setting pages
	private Simulation currentSimulation; //current simulation being run
	private SimulationResults results;
	private final int SECONDS_PER_HOUR = 3600;
	private final int FEET_PER_MILE = 5280;
	private final int OUNCES_PER_POUND = 16;
	private final int SECONDS_PER_MINUTE = 60;
	private static final String PRIMARY_BACKGROUND_COLOR = "-fx-background-color: #e0e0e0;";
	private static final String SECONDARY_BACKGROUND_COLOR = "-fx-background-color: #bdbdbd;";
	private static final String BOLD_FONT_STYLE = "-fx-font-weight: bold;";
	private static final String SIDEBAR_STYLE = "-fx-border-style: hidden solid hidden hidden;" +
			"-fx-border-width: 1.25; -fx-border-color: black; -fx-padding: 0 5 7 0";

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private Future<SimulationResults> futureResults;

	public static void main(String[] args) {

		//loads specified configuration settings
		Configuration configuration = Configuration.getInstance();
		configuration.initialize();

		//launches GUI and simulation
		launch(args);
	}

	/**
	 * COMPLETED AND TESTED!!!!
	 * Runs the simulation asynchronously so as not to block the UI thread.
	 * Once the simulation finishes, the result is retrieved and the results
	 * page is navigated to.
	 * @author Christian Burns
	 * @throws NullPointerException if no simulation configuration exists
	 */
	private void runSimulation() {

		// fetch the simulation to be run and submit it to the executor
		Simulation activeSimulation = Configuration.getInstance().getCurrentSimulation();
		futureResults = executor.submit(activeSimulation);

		// retrieve the simulation results and transition to the results page
		new Thread(() -> {
			try {
				results = futureResults.get();			// waits for the results to be generated
				Platform.runLater(this::resultsPage);	// calls resultsPage() via the UI thread
			} catch (CancellationException ignore) {
				// this will occur when cancelling a running simulation
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * COMPLETED AND TESTED!!!
	 * Cancels the simulation currently running.
	 * @author Christian Burns
	 */
	private void abortSimulation() {
		futureResults.cancel(true);
	}

	/**
	 * Saves the simulation configuration and handles file save dialog if needed.
	 * @author Christian Burns
	 * @throws IOException
	 */
	private void saveSimulation() throws IOException {
		Configuration configuration = Configuration.getInstance();
		File configurationFile = configuration.getLastConfigFile();

		if (configurationFile == null) {
			//saves settings in XML file to local machine
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Settings");
			fileChooser.getExtensionFilters().add(
					new FileChooser.ExtensionFilter("XML", "*.xml")
			);
			configurationFile = fileChooser.showSaveDialog(window);
		}

		if (configurationFile != null) {
			configuration.saveConfigs(configurationFile);
		}
	}

	/**
	 * COMPLETED AND TESTED!!!!
	 * Main screen page where the user can decide what to do (i.e. run simulation, go to settings, etc.)
	 * @author Izzy Patnode
	 */
	@Override
	public void start(Stage primaryStage) {
		window = primaryStage;

		//grabs current simulation for accessing necessary values
		currentSimulation = Configuration.getInstance().getCurrentSimulation();

		//adds camel image to main menu
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

		//adds opening heading to main menu
		title = new Text("Welcome to Dromedary Drones!");
		title.setStyle("-fx-font-family: Serif; -fx-font-size: 50; -fx-fill: #0047ab");
		title.setWrappingWidth(450);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox(20);
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.BASELINE_CENTER);

		//adds buttons to main menu
		VBox buttons = new VBox(10);
		buttons.setPrefWidth(100);

		//button for starting the simulation
		Button buttonStart = new Button("Start Simulation");
		buttonStart.setMinWidth(buttons.getPrefWidth());
		buttonStart.setStyle(primaryButtonStyle());

		//takes user to intermediate page when pressed/starts simulation
		buttonStart.setOnAction(event-> startSimulation());

		//button for editing the simulation
		Button buttonEdit = new Button("Settings");
		buttonEdit.setMinWidth(buttons.getPrefWidth());
		buttonEdit.setStyle(primaryButtonStyle());
		//takes user to general settings page when clicked
		buttonEdit.setOnAction(event -> generalEditPage());

		//button for exiting the gui
		Button buttonExit = new Button("Exit Simulation");
		buttonExit.setMinWidth(buttons.getPrefWidth());
		buttonExit.setStyle(primaryButtonStyle());
		//exits the screen (gui) when clicked
		buttonExit.setOnAction(event-> System.exit(0));

		buttons.getChildren().addAll(buttonStart, buttonEdit, buttonExit);
		buttons.setAlignment(Pos.BOTTOM_CENTER);

		//arranges title and buttons on the screen
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

		//sets starting window to the main menu
		window.setScene(mainMenu);
		window.sizeToScene();
		window.centerOnScreen();
		window.setTitle("Dromedary Drones");
		window.setResizable(false);
		window.show();
	}

	/**
	 * COMPLETED AND TESTED!!!!
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
	 * COMPLETED AND TESTED!!!
	 * An intermediate page that is displayed while the simulation is running so that the user knows it is running
	 * @author Izzy Patnode
	 */
	public void startSimulation() {

		title = new Text("Simulation is Running...");
		title.setStyle("-fx-font-family: Serif; -fx-font-size: 30; -fx-fill: #0047ab");
		title.setWrappingWidth(400);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox(20);
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.TOP_CENTER);

		//adds camel image to display
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
		String cssStyle = PRIMARY_BACKGROUND_COLOR +
				"-fx-font-family: Serif; -fx-font-size: 14; -fx-text-fill: #0047ab;" +
				"-fx-border-width: 1; -fx-border-color: #0047ab";
		cancelButton.setStyle(cssStyle);

		//takes user back to main menu
		cancelButton.setOnAction(event -> {
			abortSimulation();
			window.setScene(mainMenu);
		});

		//adds button to the display
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
	 * COMPLETED AND TESTED!!!
	 * Allows for not writing out the style of each button each time we create a button
	 * @return a string with the style in css of each button
	 * @author Izzy Patnode
	 */
	public String primaryButtonStyle() {
		 return PRIMARY_BACKGROUND_COLOR +
		"-fx-font-family: Serif; -fx-font-size: 12; -fx-text-fill: #0047ab;" +
				"-fx-border-width: 1; -fx-border-color: #0047ab";
	}

	/**
	 * COMPLETED AND TESTED!!!
	 * Allows for not writing out the secondary style of a button each time we create one
	 * @return a string with the secondary style for a button in css
	 * @author Izzy Patnode
	 */
	public String secondaryButtonStyle() {
		return SECONDARY_BACKGROUND_COLOR + "-fx-font-family: Serif; -fx-font-size: 12; -fx-text-fill: #0047ab;" +
				"-fx-border-width: 1; -fx-border-color: #0047ab";
	}

	/**
	 * COMPLETED AND TESTED!!!
	 * Creates title (Simulation Settings) for settings pages
	 * @author Izzy Patnode
	 */
	public void settingTitle() {
		title = new Text("Simulation Settings");
		title.setStyle("-fx-font-family: Serif; -fx-font-size: 30; -fx-fill: #0047ab");
		title.setWrappingWidth(400);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox();
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.CENTER);
	}

	/**
	 * COMPLETED AND TESTED!!!!!
	 * Creates home button icon
	 * @author Izzy Patnode
	 */
	public void homeButton() {
		//icon created by Google
		Image homeIcon = new Image("file:resources/home-button.png");
		ImageView homeView = new ImageView(homeIcon);

		Button homeButton = new Button("", homeView);
		homeButton.setStyle(SECONDARY_BACKGROUND_COLOR);
		homeButton.setOnAction(e-> window.setScene(mainMenu));

		iconLayout = new HBox();
		iconLayout.setAlignment(Pos.TOP_LEFT);
		iconLayout.setPadding(new Insets(0, 0, 0, 15));
		iconLayout.getChildren().add(homeButton);
		iconLayout.setStyle(SECONDARY_BACKGROUND_COLOR);
	}

	/**
	 * COMPLETED AND TESTED!!!!!
	 * Creates menu buttons for settings pages
	 * @author Izzy Patnode
	 */
	public void menuButtons() {
		buttonLayout = new VBox();
		buttonLayout.setPrefWidth(110);
		buttonLayout.setSpacing(5);

		Button generalButton = new Button("General Settings");
		generalButton.setMinWidth(buttonLayout.getPrefWidth());
		generalButton.setStyle(secondaryButtonStyle());
		generalButton.setOnAction(e -> generalEditPage());

		Button foodButton = new Button("Food Settings");
		foodButton.setMinWidth(buttonLayout.getPrefWidth());
		foodButton.setStyle(secondaryButtonStyle());
		foodButton.setOnAction(e -> editFoodPage());

		Button mealButton = new Button("Meal Settings");
		mealButton.setMinWidth(buttonLayout.getPrefWidth());
		mealButton.setStyle(secondaryButtonStyle());
		mealButton.setOnAction(e -> editMealsPage());

		Button droneButton = new Button("Drone Settings");
		droneButton.setMinWidth(buttonLayout.getPrefWidth());
		droneButton.setStyle(secondaryButtonStyle());
		droneButton.setOnAction(e -> editDronePage());

		Button mapButton = new Button("Map Settings");
		mapButton.setMinWidth(buttonLayout.getPrefWidth());
		mapButton.setStyle(secondaryButtonStyle());
		mapButton.setOnAction(e -> editMapPage());

		Button startButton = new Button("Start Simulation");
		startButton.setMinWidth(buttonLayout.getPrefWidth());
		startButton.setStyle(secondaryButtonStyle());
		startButton.setOnAction(e -> startSimulation());

		buttonLayout.getChildren().addAll(generalButton, foodButton, mealButton, droneButton, mapButton, startButton);
		buttonLayout.setAlignment(Pos.CENTER_LEFT);
		buttonLayout.setPadding(new Insets(0, 0, 0, 5));

	}

	/**
	 * COMPLETE!!! TESTED???
	 * Decreases redundancy of code used for importing and exporting settings
	 * @return Vbox containing buttons for importing and exporting buttons
	 * @author Izzy Patnode
	 */
	public VBox importExportSettings() {
		VBox saveLoadButtons = new VBox(5);
		saveLoadButtons.setPrefWidth(100);

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
				try { Configuration.getInstance().saveConfigs(file);
				} catch (IOException exception) { exception.printStackTrace(); }
			}
		});

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
					Configuration.getInstance().setLastConfigFile(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				currentSimulation = Configuration.getInstance().getCurrentSimulation();
			}
		});

		saveLoadButtons.getChildren().addAll(saveButton, loadButton);

		saveLoadButtons.setAlignment(Pos.BOTTOM_LEFT);
		saveLoadButtons.setPadding(new Insets(0, 0, 0, 10));

		return saveLoadButtons;
	}

	/**
	 * COMPLETED AND TESTED!!!!
	 * Creates GUI page for general settings (i.e. stochastic flow)
	 * @author Izzy Patnode and Rachel Franklin
	 */
	public void generalEditPage() {
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);
		leftLayout.setStyle(SECONDARY_BACKGROUND_COLOR + SIDEBAR_STYLE);

		homeButton();

		menuButtons();

		VBox importExportDisplay = importExportSettings();

		leftLayout.getChildren().addAll(iconLayout, buttonLayout, importExportDisplay);

		settingTitle();

		//creates table heading for model
		Text gridHeading = new Text("Order Volume per Hour");
		gridHeading.setFont(Font.font("Serif", 15));
		gridHeading.setFill(Color.BLACK);
		gridHeading.setWrappingWidth(200);
		gridHeading.setTextAlignment(TextAlignment.CENTER);
		gridHeading.setStyle(BOLD_FONT_STYLE);

		//creates gridpane containing the current stochastic flow values
		Text hourOne = new Text("Hour 1: ");
		hourOne.setFont(Font.font("Serif", 15));
		Text hourTwo = new Text("Hour 2: ");
		hourTwo.setFont(Font.font("Serif", 15));
		Text hourThree = new Text("Hour 3: ");
		hourThree.setFont(Font.font("Serif", 15));
		Text hourFour = new Text("Hour 4: ");
		hourFour.setFont(Font.font("Serif", 15));

		//adds current simulation's stochastic flow values to the gridpane
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

		//creates gridpane for stochastic flow values
		GridPane generalSettings = new GridPane();
		generalSettings.setAlignment(Pos.CENTER);
		generalSettings.setVgap(10);
		generalSettings.setMaxSize(300, 300);

		//adds cells to gridpane
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

		VBox centerLayout = new VBox();
		centerLayout.setSpacing(140);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, gridLayout);

		Button editButton = new Button("Save Changes");
		editButton.setStyle(primaryButtonStyle());

		//sets current stochastic flow to edited stochastic flow
		editButton.setOnAction(event -> {
			ArrayList<Integer> stochasticModel = new ArrayList<>();
			try {
				stochasticModel.add(Integer.parseInt(hourOneMeals.getText()));
				stochasticModel.add(Integer.parseInt(hourTwoMeals.getText()));
				stochasticModel.add(Integer.parseInt(hourThreeMeals.getText()));
				stochasticModel.add(Integer.parseInt(hourFourMeals.getText()));

				currentSimulation.addStochasticFlow(stochasticModel);

				Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
				saveAlert.setTitle("Confirm Changes");
				saveAlert.setHeaderText("Changes Saved!");
				saveAlert.showAndWait();
			} //end of try block
			catch(NumberFormatException illegalFormat) {
				Alert errorAlert = new Alert(Alert.AlertType.ERROR);
				errorAlert.setTitle("Invalid Input");
				errorAlert.setHeaderText("Error: Invalid Input");
				errorAlert.setContentText("Integer format required");

				stochasticModel.addAll(List.of(currentHourOne, currentHourTwo, currentHourThree, currentHourFour));

				hourOneMeals.setText(currentHourOne + "");
				hourTwoMeals.setText(currentHourTwo + "");
				hourThreeMeals.setText(currentHourThree + "");
				hourFourMeals.setText(currentHourFour + "");

				errorAlert.showAndWait();
			} //end of catch block
		}); //end of event handler

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
	 * COMPLETED AND TESTED!!!
	 * Creates GUI page for food items settings
	 * @author Izzy Patnode and Rachel Franklin
	 */
	public void editFoodPage() {
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);
		leftLayout.setStyle(SECONDARY_BACKGROUND_COLOR + SIDEBAR_STYLE);

		homeButton();

		menuButtons();

		VBox importExportDisplay = importExportSettings();

		leftLayout.getChildren().addAll(iconLayout, buttonLayout, importExportDisplay);

		settingTitle();

		//create table of food items in simulation
		TableView<FoodItem> foodTable = new TableView<>();
		ObservableList<FoodItem> foodItems = currentSimulation.getFoodItems();
		foodTable.setItems(foodItems);
		foodTable.setEditable(true);
		foodTable.setStyle("-fx-control-inner-background: #bdbdbd; -fx-control-inner-background-alt: #e0e0e0; " +
				"-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: #e0e0e0");

		//Create table headings
		TableColumn<FoodItem, String> itemHeading = new TableColumn<>("Food Item");
		itemHeading.setCellValueFactory(new PropertyValueFactory<>("name"));
		itemHeading.setPrefWidth(100);

		TableColumn<FoodItem, Double> weightHeading = new TableColumn<>("Weight (oz)");
		weightHeading.setCellValueFactory(new PropertyValueFactory<>("weight"));
		weightHeading.setPrefWidth(100);

		//allows user to edit the name of a food item already in the table
		itemHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		itemHeading.setOnEditCommit(event ->
				event.getTableView().getItems().get(event.getTablePosition().
						getRow()).setName(event.getNewValue()));

		//allows user to edit the weight of a food item already in the table
		weightHeading.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter(){
			@Override
			public Double fromString(String value){
				try {
					return super.fromString(value);
				} catch(Exception e){
					return Double.NaN;
				}
			}
		}));
		weightHeading.setOnEditCommit(event -> {
			/* If there is an error make the index 1, this allows us to determine whether to set
			** the cell vale to the new value or the old value */
			int errorIndex = 0;
			Alert invalidInput = new Alert(Alert.AlertType.ERROR);
			double oldValue = event.getOldValue();

			//user must input a double
			if (event.getNewValue().isNaN()){
				invalidInput.setTitle("Invalid Input");
				invalidInput.setHeaderText("Error: Invalid Input");
				invalidInput.setContentText("Input must be an integer or a decimal.");
				errorIndex = 1;
			}
			else {
				double newValue = event.getNewValue();
				double maxPayload = currentSimulation.getDroneSettings().getMaxPayloadWeight();

				//drone capacity cannot exceed 12 lbs, so item weight cannot exceed 12 lbs
				if (newValue > maxPayload) {
					invalidInput.setTitle("Invalid Input");
					invalidInput.setHeaderText("Error: Invalid Input");
					invalidInput.setContentText("Food item weight cannot exceed " + maxPayload + " oz.");
					errorIndex = 1;
				}
				else if (newValue <= 0) {
					invalidInput.setTitle("Invalid Input");
					invalidInput.setHeaderText("Error: Invalid Input");
					invalidInput.setContentText("Food item weight must weigh something.");
					errorIndex = 1;
				}
				else {
					//drone capacity cannot exceed 12 lbs, so any meal cannot exceed 12 lbs
					ArrayList<Meal> mealTypes = currentSimulation.getMealTypes();
					String itemName = event.getTableView().getItems().get(event.getTablePosition().getRow()).getName();
						for (Meal meal : mealTypes) {
							double newWeight = 0;
							if(errorIndex == 0) {
								for (FoodItem food: meal.getFoods()) {
									if (itemName.equals(food.getName())) {
										newWeight += newValue;
									}
									else {
										newWeight += food.getWeight();
									}

									if (newWeight > maxPayload) {
										invalidInput.setTitle("Invalid Input");
										invalidInput.setHeaderText("Error: Invalid Input");
										invalidInput.setContentText("Input food item weight causes a meal weight to " +
												"exceed the drone's maximum payload weight.");
										errorIndex = 1;
									}
								}//foods for loop
							} //checking meals if statement
						}//meals for loop
				} //else statement if newValue <= maxPayload
			} //else statement if maxPayload is a number

			if(errorIndex == 0) {
				//user input valid weight
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setWeight(event.getNewValue());
			}
			else {
				//put old value back
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setWeight(oldValue);
				invalidInput.showAndWait();
			}

			event.getTableView().refresh();
		});//event end

		//adds columns to table
		foodTable.getColumns().setAll(itemHeading, weightHeading);
		foodTable.setPrefWidth(200);
		foodTable.setPrefHeight(300);
		foodTable.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, new BorderWidths(1))));

		//arranges table on screen
		StackPane tableLayout = new StackPane();
		tableLayout.setAlignment(Pos.CENTER_RIGHT);
		tableLayout.setMaxSize(202, 300);
		tableLayout.getChildren().add(foodTable);

		Text newFoodNameLabel = new Text("Food name: ");
		newFoodNameLabel.setFont(Font.font("Serif", 15));
		newFoodNameLabel.setTextAlignment(TextAlignment.CENTER);

		TextField newFoodName = new TextField();
		newFoodName.setMaxWidth(80);

		Text newFoodWeightLabel = new Text("Weight: ");
		newFoodWeightLabel.setFont(Font.font("Serif", 15));
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

		VBox tableFoodLayout = new VBox(10);
		tableFoodLayout.getChildren().addAll(tableLayout, newFoodFields);
		tableFoodLayout.setAlignment(Pos.CENTER);

		VBox centerLayout = new VBox();
		centerLayout.setSpacing(90);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, tableFoodLayout);

		//buttons for adding and deleting table rows
		Button addButton = new Button("Add");
		addButton.setStyle(primaryButtonStyle());
		addButton.setMinWidth(60);

		addButton.setOnAction(event -> {
			Alert errorAlert = new Alert(Alert.AlertType.ERROR);
			errorAlert.setTitle("Invalid Input");
			errorAlert.setHeaderText("Error: Invalid Input");

			try {
				double newWeight = Double.parseDouble(newFoodWeight.getText());
				if (newWeight > currentSimulation.getDroneSettings().getMaxPayloadWeight()){
					errorAlert.setContentText("Food weight cannot exceed drone's maximum payload.");
					errorAlert.showAndWait();
				} else if (newWeight <= 0.0){
					errorAlert.setContentText("Food must weigh something.");
					errorAlert.showAndWait();
				} else if (newFoodName.getText().equals("")){
					errorAlert.setContentText("Food must have a name.");
					errorAlert.showAndWait();
				}
				else{
					currentSimulation.addFoodItem(new FoodItem(newFoodName.getText(), newWeight));
					foodTable.setItems(currentSimulation.getFoodItems());
					foodTable.refresh();
					newFoodName.setText("");
					newFoodWeight.setText("");
				}
			}
			catch(NumberFormatException illegalFormat) {
				errorAlert.setContentText("Number format required");
				errorAlert.showAndWait();
				foodTable.refresh();
			}
		});

		Button deleteButton = new Button("Delete");
		deleteButton.setStyle(primaryButtonStyle());
		deleteButton.setMinWidth(60);
		deleteButton.setOnAction(event -> {
			int deletedRow = foodTable.getSelectionModel().getSelectedIndex();
			if(deletedRow < 0) {
				Alert errorAlert = new Alert(Alert.AlertType.ERROR);
				errorAlert.setTitle("Invalid Deletion");
				errorAlert.setHeaderText("Error: Invalid Deletion");
				errorAlert.setContentText("Food not selected.");
				errorAlert.showAndWait();
			}
			else {
				FoodItem deletedFood = foodTable.getSelectionModel().getSelectedItem();
				foodItems.remove(deletedRow);
				currentSimulation.removeFoodItem(deletedFood);
				foodTable.refresh();
			}
		}); //end of event

		//arranges add and delete buttons relative to each other
		HBox rightLayout = new HBox(10);
		rightLayout.getChildren().addAll(addButton, deleteButton);
		rightLayout.setAlignment(Pos.BOTTOM_RIGHT);
		rightLayout.setPadding(new Insets(0, 50, 122, 0));

		HBox mainLayout = new HBox(20);
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
	 * COMPLETED AND TESTED!!!!
	 * Creates GUI page for meal settings
	 * @author Izzy Patnode and Rachel Franklin
	 */
	public void editMealsPage() {
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);
		leftLayout.setStyle(SECONDARY_BACKGROUND_COLOR + SIDEBAR_STYLE);

		homeButton();

		menuButtons();

		VBox importExportDisplay = importExportSettings();

		leftLayout.getChildren().addAll(iconLayout, buttonLayout, importExportDisplay);

		settingTitle();

		//formats display of meal probabilities
		HashMap<String, Double> probabilities = new HashMap<>();
		for (Meal item : currentSimulation.getMealTypes()){
			probabilities.put(item.getName(), item.getProbability());
		}
		ObservableList<HashMap.Entry<String, Double>> mealProbabilities = FXCollections
				.observableArrayList(probabilities.entrySet());
		//probabilities shown separate from meal contents due to nuances with editing
		TableView<HashMap.Entry<String, Double>> probabilityTable = new TableView<>(mealProbabilities);
		probabilityTable.setMaxSize(205, 300);
		probabilityTable.setEditable(true);
		probabilityTable.setStyle("-fx-control-inner-background: #bdbdbd; -fx-control-inner-background-alt: #e0e0e0;" +
				"-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: #e0e0e0");

		TableColumn<HashMap.Entry<String, Double>, String> itemColumn = new TableColumn<>("Meal Type");
		itemColumn.setCellValueFactory(
				(TableColumn.CellDataFeatures<HashMap.Entry<String, Double>, String> item) ->
						new SimpleStringProperty(item.getValue().getKey()));
		itemColumn.setPrefWidth(100);

		TableColumn<HashMap.Entry<String, Double>, Double> probabilityColumn = new TableColumn<>("Probability");
		probabilityColumn.setCellValueFactory(
				(TableColumn.CellDataFeatures<HashMap.Entry<String, Double>, Double> item) ->
						new ReadOnlyObjectWrapper<>(item.getValue().getValue()));
		probabilityColumn.setPrefWidth(100);
		probabilityColumn.setEditable(true);

		probabilityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter(){
			@Override
			public Double fromString(String value){
				try {
					return super.fromString(value);
				} catch(Exception e){
					return Double.NaN;
				}
			}
		}));

		//we don't want to save the probabilities yet -- just to the table
		probabilityColumn.setOnEditCommit(event -> {
			int errorIndex = 0;
			Alert invalidInput = new Alert(Alert.AlertType.ERROR);
			double oldValue = event.getOldValue();
			double newValue;

			if (event.getNewValue().isNaN()){
				invalidInput.setTitle("Invalid Input");
				invalidInput.setHeaderText("Error: Invalid Input");
				invalidInput.setContentText("Input must be a decimal.");
				errorIndex = 1;
			}
			else{
				newValue = event.getNewValue();
				if (newValue > 1.0){
					invalidInput.setTitle("Invalid Input");
					invalidInput.setHeaderText("Error: Invalid Input");
					invalidInput.setContentText("A probability cannot be greater than 1.");
					errorIndex = 1;
				}
			}

			if (errorIndex == 1){
				event.getTableView().getItems().get(event.getTablePosition().getRow()).setValue(oldValue);
				invalidInput.showAndWait();
			}
			else{
				newValue = event.getNewValue();
				event.getTableView().getItems().get(event.getTablePosition().getRow()).setValue(newValue);
			}

		});

		probabilityTable.getColumns().setAll(itemColumn, probabilityColumn);

		StackPane probabilityLayout = new StackPane();
		probabilityLayout.setAlignment(Pos.TOP_RIGHT);
		probabilityLayout.setMaxSize(202, 300);
		probabilityLayout.getChildren().add(probabilityTable);

		Button saveProbabilityButton = new Button("Save Changes");
		saveProbabilityButton.setStyle(primaryButtonStyle());
		saveProbabilityButton.setOnAction(event -> {
			BigDecimal totalProbability = BigDecimal.ZERO;

			for (HashMap.Entry<String, Double> item : mealProbabilities) {
				totalProbability = totalProbability.add(BigDecimal.valueOf(item.getValue()));
			}

			if (totalProbability.compareTo(BigDecimal.ONE) == 0){
				//update settings with new values
				for (Meal item : currentSimulation.getMealTypes()){
					for (HashMap.Entry<String, Double> tableCell : mealProbabilities){
						if (tableCell.getKey().equals(item.getName())){
							item.setProbability(tableCell.getValue());
						}
					}
				}
			}

			else{
				Alert invalidInput = new Alert(Alert.AlertType.ERROR);
				invalidInput.setTitle("Invalid Input");
				invalidInput.setHeaderText("Error: Invalid Input");
				invalidInput.setContentText("Total probability of meals should equal 1.");
				invalidInput.showAndWait();

				//reset table with old values
				mealProbabilities.clear();
				for (Meal item : currentSimulation.getMealTypes()){
					probabilities.put(item.getName(), item.getProbability());
				}
				mealProbabilities.addAll(probabilities.entrySet());
			}

		});

		HBox saveLayout = new HBox();
		saveLayout.getChildren().add(saveProbabilityButton);
		saveLayout.setAlignment(Pos.CENTER);

		VBox rightLayout = new VBox(5);
		rightLayout.setAlignment(Pos.CENTER_RIGHT);
		rightLayout.setPadding(new Insets(10, 0, 0, 0));
		rightLayout.getChildren().addAll(probabilityLayout, saveLayout);

		//arranges all meals together
		VBox mealsBox = new VBox(10);

		//creates a gridpane for each meal in the simulation
		for(Meal meal : currentSimulation.getMealTypes()) {
			VBox singleMealLayout = new VBox();

			TextField mealName = new TextField(meal.getName());
			mealName.setFont(Font.font("Serif", 15));
			mealName.setMaxWidth(200);
			mealName.setStyle(BOLD_FONT_STYLE);

			//user should be able to edit meal name
			mealName.setOnAction(event -> {
				String newName = mealName.getText();
				meal.setName(newName);
				//when user presses enter
				mealName.setText(newName);
			});

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

			//Map must be an observable to be used in a table
			ObservableList<HashMap.Entry<String, Integer>> itemCounts = FXCollections.
					observableArrayList(numberPerFood.entrySet());
			TableView<HashMap.Entry<String, Integer>> mealTable = new TableView<>(itemCounts);
			//When maxwidth = prefwidth, horizontal scroll bar shows up -- make maxWidth > prefWidth
			mealTable.setMaxSize(205, 300);
			mealTable.setEditable(true);
			mealTable.setStyle("-fx-control-inner-background: #bdbdbd; -fx-control-inner-background-alt: #e0e0e0;" +
					"-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: #e0e0e0");

			//Table holds food items and counts for each meal
			TableColumn<HashMap.Entry<String, Integer>, String> foodColumn = new TableColumn<>("Food Item");
			foodColumn.setCellValueFactory(
					(TableColumn.CellDataFeatures<HashMap.Entry<String, Integer>, String> item) ->
					new SimpleStringProperty(item.getValue().getKey()));
			foodColumn.setPrefWidth(100);
			foodColumn.setEditable(true);

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
					} catch(Exception e){
						return null;
					}
				}
			}));
			countColumn.setOnEditCommit(event ->{
				int errorIndex = 0;
				Alert invalidInput = new Alert(Alert.AlertType.ERROR);
				int oldValue = event.getOldValue();
				String itemName = event.getTableView().getItems().get(event.getTablePosition().getRow()).getKey();

				//user must input an integer
				if (event.getNewValue() == null){
					invalidInput.setTitle("Invalid Input");
					invalidInput.setHeaderText("Error: Invalid Input");
					invalidInput.setContentText("Input must be an integer.");
					errorIndex = 1;
				}
				else {

					double maxPayload = currentSimulation.getDroneSettings().getMaxPayloadWeight();
					int newValue = event.getNewValue();

					//Cannot have negative amounts of an item
					if (newValue < 0){
						invalidInput.setTitle("Invalid Input");
						invalidInput.setHeaderText("Error: Invalid Input");
						invalidInput.setContentText("Input must be non-negative.");
						errorIndex = 1;
					}

					else {
						//new meal weight must not be greater than drone's maximum payload
						double mealWeight;
						double itemWeight = currentSimulation.getFoodItem(itemName).getWeight();
						mealWeight = meal.getTotalWeight() - (oldValue * itemWeight) + (newValue * itemWeight);
						if (mealWeight > maxPayload){
							invalidInput.setTitle("Invalid Input");
							invalidInput.setHeaderText("Error: Invalid Input");
							invalidInput.setContentText("Meal weight must not exceed maximum payload. ("
									+ maxPayload + " oz.)");
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
					//invalid input means value doesn't change
					event.getTableView().getItems().get(event.getTablePosition().getRow()).setValue(oldValue);
					invalidInput.showAndWait();
				}
			});


			mealTable.getColumns().setAll(foodColumn, countColumn);
			mealTable.setPrefWidth(200);
			mealTable.setPrefHeight(250);

			//creates button for deleting meals
			Button deleteButton = new Button("X");
			deleteButton.setStyle(BOLD_FONT_STYLE + "-fx-background-color: #e0e0e0; -fx-font-size: 20; " +
					"fx-font-family: Serif; -fx-border-style: hidden; -fx-border-color: #e0e0e0; -fx-border-width: 1");

			deleteButton.setOnAction(event -> {
				//total probability of meals after meal deletion should equal 1.0
				BigDecimal mealProbability = new BigDecimal(meal.getProbability());
				if (mealProbability.compareTo(BigDecimal.valueOf(0.0)) != 0){
					Alert probabilityAlert = new Alert(Alert.AlertType.ERROR);
					probabilityAlert.setTitle("Error");
					probabilityAlert.setContentText("This meal's probability must be set to 0 before it can be " +
							"deleted. Please refactor the probabilities so that this meal's probability is 0 and " +
							"the rest are equivalent to 1.0.");
					probabilityAlert.showAndWait();
				}

				else{
					String nameToDelete = meal.getName();
					HashMap.Entry<String, Double> mealToDelete = mealProbabilities.get(0);
					for (int i = 0; i < mealProbabilities.size(); i++){
						mealToDelete = mealProbabilities.get(i);
						if (mealToDelete.getKey().equals(nameToDelete)){
							break;
						}
					}

					mealsBox.getChildren().remove(singleMealLayout);
					mealProbabilities.remove(mealToDelete);	//this isn't right - turn to entry <>
					probabilities.remove(mealToDelete);
					probabilityTable.refresh();
					currentSimulation.removeMeal(meal);
				}

			});

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

		Button addButton = new Button("Add Meal");
		addButton.setStyle(primaryButtonStyle());
		addButton.setOnAction(event ->{	//doesn't return to this page
			addMealPage();	//needs fixing
		});

		VBox tableAddLayout = new VBox(5);
		tableAddLayout.getChildren().addAll(mealLayout, addButton);
		tableAddLayout.setAlignment(Pos.CENTER);

		//formats display of menu column and full meal display
		VBox centerLayout = new VBox(80);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, tableAddLayout);

		HBox mainLayout = new HBox();
		mainLayout.getChildren().addAll(centerLayout, rightLayout);

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
	 * COMPLETED AND TESTED!!!!
	 * Creates page for adding a meal
	 * @author Rachel Franklin
	 */
	public void addMealPage(){
		title = new Text("Add Meal");
		title.setStyle("-fx-font-family: Serif; -fx-font-size: 30; -fx-fill: #0047ab");
		title.setWrappingWidth(400);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox();
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.CENTER);

		//user should name the food item
		Text nameText = new Text("Food name: ");
		nameText.setFont(Font.font("Serif", 15));
		TextField nameField = new TextField();
		nameField.setMaxWidth(80);

		HBox nameLayout = new HBox();
		nameLayout.getChildren().addAll(nameText, nameField);
		nameLayout.setAlignment(Pos.CENTER);

		//formats display of meal counts
		HashMap<FoodItem, Integer> foodItems = new HashMap<>();
		for (FoodItem item : currentSimulation.getFoodItems()){
			foodItems.put(item, 0);
		}
		ObservableList<HashMap.Entry<FoodItem, Integer>> foodInMeal = FXCollections
				.observableArrayList(foodItems.entrySet());
		TableView<HashMap.Entry<FoodItem, Integer>> foodTable = new TableView<>(foodInMeal);
		foodTable.setMaxSize(205, 300);
		foodTable.setEditable(true);
		foodTable.setStyle("-fx-control-inner-background: #bdbdbd; -fx-control-inner-background-alt: #e0e0e0;" +
				"-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: #e0e0e0");

		TableColumn<HashMap.Entry<FoodItem, Integer>, String> foodColumn = new TableColumn<>("Food Item");
		foodColumn.setCellValueFactory(
				(TableColumn.CellDataFeatures<HashMap.Entry<FoodItem, Integer>, String> item) ->
						new SimpleStringProperty(item.getValue().getKey().getName()));
		foodColumn.setPrefWidth(100);

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
				} catch(Exception e){
					return null;
				}
			}
		}));
		countColumn.setOnEditCommit(event -> {

			int errorIndex = 0;
			Alert invalidInput = new Alert(Alert.AlertType.ERROR);
			int oldValue = event.getOldValue();

			//user must input an integer
			if (event.getNewValue() == null){
				invalidInput.setTitle("Invalid Input");
				invalidInput.setHeaderText("Error: Invalid Input");
				invalidInput.setContentText("Input must be an integer.");
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

				//drone capacity cannot exceed 12 lbs, so items combined weight cannot exceed 12 lbs
				if (mealWeight > maxPayload) {
					invalidInput.setTitle("Invalid Input");
					invalidInput.setHeaderText("Error: Invalid Input");
					invalidInput.setContentText("Meal weight cannot exceed " + maxPayload + " oz.");
					errorIndex = 1;
				}
				else if (newValue <= 0) {
					invalidInput.setTitle("Invalid Input");
					invalidInput.setHeaderText("Error: Invalid Input");
					invalidInput.setContentText("Negative values or 0 not permitted.");
					errorIndex = 1;
				}
			}

			if(errorIndex == 0) {
				//user input valid weight
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setValue(event.getNewValue());
			}
			else {
				//put old value as
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setValue(oldValue);
				foodTable.refresh();
				invalidInput.showAndWait();
			}

		});//event end

		foodTable.getColumns().setAll(foodColumn, countColumn);
		foodTable.setPrefWidth(200);
		foodTable.setPrefHeight(300);
		foodTable.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, new BorderWidths(1))));

		StackPane tableLayout = new StackPane();
		tableLayout.setAlignment(Pos.TOP_RIGHT);
		tableLayout.setMaxSize(202, 300);
		tableLayout.getChildren().add(foodTable);

		Button save = new Button ("OK");
		save.setStyle(primaryButtonStyle());
		save.setOnAction(event ->{
			int errorIndex = 0;
			Alert invalidInput = new Alert(Alert.AlertType.ERROR);
			invalidInput.setTitle("Invalid Meal Input");

			double mealWeight = 0;
			HashMap.Entry <FoodItem, Integer> currentItem;
			for (int index = 0; index < foodTable.getItems().size(); index++){
				currentItem = foodTable.getItems().get(index);
				mealWeight += currentItem.getValue() * (currentItem.getKey().getWeight());
			}

			//meal must have a name
			if (nameField.getText().equals("")){
				errorIndex = 1;
				invalidInput.setContentText("Meal must be given a name.");
			}
			//meal must have food items
			else if(mealWeight <= 0){
				errorIndex = 1;
				invalidInput.setContentText("Meal must have food items.");
			}

			if (errorIndex == 0){
				//food items to put in meal
				ArrayList <FoodItem> foodToAdd = new ArrayList<>();
				for (int index = 0; index < currentSimulation.getFoodItems().size(); index++){
					int countFood = foodInMeal.get(index).getValue();
					for (int countIndex = 0; countIndex < countFood; countIndex++){
						foodToAdd.add(foodInMeal.get(index).getKey());
					}
				}

				//add new meal to simulation
				currentSimulation.addMealType(new Meal(foodToAdd, nameField.getText(), 0));
				Alert probabilityNotice = new Alert(Alert.AlertType.INFORMATION);
				probabilityNotice.setTitle("Meal Created with Probability of 0");
				probabilityNotice.setHeaderText(null);
				probabilityNotice.setContentText("Your meal has been created with its probability " +
						"set to 0. You can change that through the probability table on the meals " +
						"edit page. Remember, probabilities of all meals combined should equal 1.");
				probabilityNotice.showAndWait();

				editMealsPage();
				return;
			}
			else{
				invalidInput.showAndWait();
			}
		});

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
	 * COMPLETED AND TESTED!!!!!
	 * Creates GUI page for drone settings
	 * @author Izzy Patnode
	 */
	public void editDronePage() {
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);
		leftLayout.setStyle(SECONDARY_BACKGROUND_COLOR + SIDEBAR_STYLE);

		homeButton();
		menuButtons();

		VBox importExportDisplay = importExportSettings();

		leftLayout.getChildren().addAll(iconLayout, buttonLayout, importExportDisplay);

		settingTitle();

		Font font = Font.font("Serif", 15);

		//creates gridpane containing the current stochastic flow values
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

		double currentPayload = currentSimulation.getDroneSettings().getMaxPayloadWeight() / OUNCES_PER_POUND;
		TextField dronePayload = new TextField(String.format("%.2f", currentPayload));
		dronePayload.setMaxWidth(80);

		double currentSpeed = (currentSimulation.getDroneSettings().getCruisingSpeed() * SECONDS_PER_HOUR)
				/ FEET_PER_MILE;
		TextField droneSpeed = new TextField(String.format("%.2f", currentSpeed));
		droneSpeed.setMaxWidth(80);

		double currentFlightTime = currentSimulation.getDroneSettings().getFlightTime() / SECONDS_PER_MINUTE;
		TextField droneFlight = new TextField(String.format("%.2f", currentFlightTime));
		droneFlight.setMaxWidth(80);

		double currentTurnAround = currentSimulation.getDroneSettings().getTurnAroundTime() / SECONDS_PER_MINUTE;
		TextField droneTurnAround = new TextField(String.format("%.2f", currentTurnAround));
		droneTurnAround.setMaxWidth(80);

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

		Button editButton = new Button("Save Changes");
		editButton.setStyle(primaryButtonStyle());

		//sets current drone settings to edited drone settings
		editButton.setOnAction(event -> {
			try {
				currentSimulation.getDroneSettings().setMaxPayloadWeight((Double.parseDouble(dronePayload.getText())
						* OUNCES_PER_POUND));
				currentSimulation.getDroneSettings().setCruisingSpeed((Double.parseDouble(droneSpeed.getText())
						* FEET_PER_MILE) / SECONDS_PER_HOUR);
				currentSimulation.getDroneSettings().setFlightTime(Double.parseDouble(droneFlight.getText())
						* SECONDS_PER_MINUTE);
				currentSimulation.getDroneSettings().setTurnAroundTime(Double.parseDouble(droneFlight.getText())
						* SECONDS_PER_MINUTE);
				currentSimulation.getDroneSettings().setDeliveryTime(Double.parseDouble(droneUnload.getText()));

				Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
				saveAlert.setTitle("Confirm Changes");
				saveAlert.setHeaderText("Changes Saved!");
				saveAlert.showAndWait();
			} //end of try block
			catch(NumberFormatException illegalFormat) {
				Alert errorAlert = new Alert(Alert.AlertType.ERROR);
				errorAlert.setTitle("Invalid Input");
				errorAlert.setHeaderText("Invalid Input!");

				//resets drone settings to what they were before editing
				currentSimulation.getDroneSettings().setMaxPayloadWeight(currentPayload * OUNCES_PER_POUND);
				currentSimulation.getDroneSettings().setCruisingSpeed((currentSpeed * FEET_PER_MILE) / SECONDS_PER_HOUR);
				currentSimulation.getDroneSettings().setFlightTime(currentFlightTime * SECONDS_PER_MINUTE);
				currentSimulation.getDroneSettings().setTurnAroundTime(currentTurnAround * SECONDS_PER_MINUTE);
				currentSimulation.getDroneSettings().setDeliveryTime(currentDelivery);

				dronePayload.setText(String.format("%.2f", currentPayload));
				droneSpeed.setText(String.format("%.2f", currentSpeed));
				droneFlight.setText(String.format("%.2f", currentFlightTime));
				droneTurnAround.setText(String.format("%.2f", currentTurnAround));
				droneUnload.setText(String.format("%.2f", currentDelivery));

				errorAlert.showAndWait();
			} //end of catch block
		}); //end of event handler

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
	 * COMPLETED AND TESTED!!!!!
	 * Creates GUI page for map settings
	 * @author Izzy Patnode
	 */
	public void editMapPage() {
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);
		leftLayout.setStyle(SECONDARY_BACKGROUND_COLOR + SIDEBAR_STYLE);

		homeButton();

		menuButtons();

		VBox importExportDisplay = importExportSettings();

		leftLayout.getChildren().addAll(iconLayout, buttonLayout, importExportDisplay);

		settingTitle();

		//gets list of current map destinations
		ObservableList<Point> mapPoints = currentSimulation.getDeliveryPoints().getPoints();

		//finds maximum and minimum destinations points (in coordinates)
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

		//creates the axes for the map scatter plot
		NumberAxis xAxis = new NumberAxis(lowerXBound - 100, upperXBound + 100, 100);
		xAxis.setLabel("");
		xAxis.setTickMarkVisible(false);
		NumberAxis yAxis = new NumberAxis(lowerYBound - 100, upperYBound + 100, 100);
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
		mapTable.setStyle("-fx-control-inner-background: #bdbdbd; -fx-control-inner-background-alt: #e0e0e0;" +
				"-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: #e0e0e0");

		//creates columns for table
		TableColumn<Point, String> pointHeading = new TableColumn<>("Drop-Off Point");
		pointHeading.setCellValueFactory(new PropertyValueFactory<>("name"));

		//allows user to edit the name of a point already in the table
		pointHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		pointHeading.setOnEditCommit(event -> {
			int selectedRow = event.getTablePosition().getRow();
			event.getTableView().getItems().get(selectedRow).setName(event.getNewValue() + "");
		});

		TableColumn<Point, String> xyHeading = new TableColumn<>("(x,y)");
		xyHeading.setCellValueFactory(new PropertyValueFactory<>("coordinates"));

		//allows user to edit the coordinates of a point already in the table
		xyHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		xyHeading.setOnEditCommit(event -> {
			int selectedRow = event.getTablePosition().getRow();
			String oldCoordinates = event.getOldValue();

			try {
				int oldXValue = event.getTableView().getItems().get(selectedRow).getX();
				int oldYValue = event.getTableView().getItems().get(selectedRow).getY();

				if((oldXValue != 0 || oldYValue != 0)) {
					Drone currentDrone = currentSimulation.getDroneSettings();
					double maxDistanceAllowed = (currentDrone.getFlightTime() * currentDrone.getCruisingSpeed()) / 2;

					event.getTableView().getItems().get(selectedRow).setCoordinates(event.getNewValue() + "");

					Point newPoint = event.getTableView().getSelectionModel().getSelectedItem();

					if(newPoint.distanceFromPoint(newPoint.getOrigin()) <= maxDistanceAllowed) {
						int newXValue = newPoint.getX();
						int newYValue = newPoint.getY();

						XYChart.Data<Number, Number> selectedPoint = mapValues.getData().get(selectedRow);
						selectedPoint.setXValue(newXValue);
						selectedPoint.setYValue(newYValue);

						int currentUpperXBound = (int) xAxis.getUpperBound() - 100;
						int currentLowerXBound = (int) xAxis.getLowerBound() + 100;

						if(newXValue > currentUpperXBound) {
							currentUpperXBound = newXValue;
							xAxis.setUpperBound(currentUpperXBound + 100);
						}
						if(newXValue <= currentLowerXBound) {
							currentLowerXBound = newXValue;
							xAxis.setLowerBound(currentLowerXBound - 100);
						}

						int currentUpperYBound = (int) yAxis.getUpperBound() - 100;
						int currentLowerYBound = (int) yAxis.getLowerBound() + 100;

						if(newYValue > currentUpperYBound) {
							currentUpperYBound = newYValue;
							yAxis.setUpperBound(currentUpperYBound + 100);
						}
						if(newYValue < currentLowerYBound) {
							yAxis.setLowerBound(currentLowerYBound - 100);
						}
					}
					else {
						Alert blockOrigin = new Alert(Alert.AlertType.ERROR);
						blockOrigin.setTitle("Maximum Distance Exceeded");
						blockOrigin.setHeaderText("Error: Maximum Distance Exceeded");
						blockOrigin.setContentText("Coordinates are outside of drone range");
						event.getTableView().getItems().get(selectedRow).setCoordinates(oldCoordinates);
						blockOrigin.showAndWait();
					}

				}
				else {
					Alert blockOrigin = new Alert(Alert.AlertType.ERROR);
					blockOrigin.setTitle("Origin Change Attempted");
					blockOrigin.setHeaderText("Error: Origin Change Attempted");
					blockOrigin.setContentText("Origin must be at (0,0)");
					event.getTableView().getItems().get(selectedRow).setCoordinates(oldCoordinates);
					blockOrigin.showAndWait();
				}

			}
			catch(IllegalArgumentException illegalArgument) {
				Alert invalidInput = new Alert(Alert.AlertType.ERROR);
				invalidInput.setTitle("Invalid Coordinates");
				invalidInput.setHeaderText("Error: Invalid Coordinates");
				invalidInput.setContentText("Input must be a (x,y) integer pair");
				event.getTableView().getItems().get(selectedRow).setCoordinates(oldCoordinates);
				invalidInput.showAndWait();
			}

			event.getTableView().refresh();
		});

		//adds column headings to table
		mapTable.getColumns().setAll(pointHeading, xyHeading);
		mapTable.setPrefWidth(275);
		mapTable.setPrefHeight(300);

		//arranges table
		StackPane tableLayout = new StackPane();
		tableLayout.setMaxSize(275, 300);
		tableLayout.setAlignment(Pos.CENTER);
		tableLayout.getChildren().add(mapTable);

		//buttons for adding and deleting table rows
		Button addButton = new Button("Add");
		addButton.setStyle(primaryButtonStyle());

		addButton.setOnAction(event -> {
			Stage addDialog = new Stage();
			addDialog.setTitle("New Point");
			addDialog.initOwner(window);
			addDialog.initModality(Modality.WINDOW_MODAL);
			addDialog.initStyle(StageStyle.UTILITY);

			GridPane newPointGrid = new GridPane();
			newPointGrid.setHgap(10);
			newPointGrid.setVgap(10);

			TextField pointName = new TextField();
			TextField coordinates = new TextField();

			Text pointTitle = new Text("Name");
			Text coordinateTitle = new Text("Coordinates");

			newPointGrid.add(pointTitle, 0, 0);
			newPointGrid.add(pointName, 1, 0);
			newPointGrid.add(coordinateTitle, 0, 1);
			newPointGrid.add(coordinates, 1, 1);

			Button confirmButton = new Button("Finish");
			confirmButton.setOnAction(event1 -> {
				Point newPoint = currentSimulation.getDeliveryPoints().
						addPoint(pointName.getText(), Double.NaN, Double.NaN);

				try {
					newPoint.setCoordinates(coordinates.getText());

					Drone currentDrone = currentSimulation.getDroneSettings();
					double maxDistanceAllowed = (currentDrone.getFlightTime() * currentDrone.getCruisingSpeed()) / 2;

					if(newPoint.distanceFromPoint(newPoint.getOrigin()) <= maxDistanceAllowed) {
						int newXValue = newPoint.getX();
						int newYValue = newPoint.getY();

						mapValues.getData().add(new XYChart.Data<>(newPoint.getX(), newPoint.getY()));

						int currentUpperXBound = (int) xAxis.getUpperBound() - 100;
						int currentLowerXBound = (int) xAxis.getLowerBound() + 100;

						if(newXValue > currentUpperXBound) {
							currentUpperXBound = newXValue;
							xAxis.setUpperBound(currentUpperXBound + 100);
						}
						if(newXValue <= currentLowerXBound) {
							currentLowerXBound = newXValue;
							xAxis.setLowerBound(currentLowerXBound - 100);
						}

						int currentUpperYBound = (int) yAxis.getUpperBound() - 100;
						int currentLowerYBound = (int) yAxis.getLowerBound() + 100;

						if(newYValue > currentUpperYBound) {
							currentUpperYBound = newYValue;
							yAxis.setUpperBound(currentUpperYBound + 100);
						}
						if(newYValue < currentLowerYBound) {
							yAxis.setLowerBound(currentLowerYBound - 100);
						}

						ObservableList<Point> newMapPoints = currentSimulation.getDeliveryPoints().getPoints();

						mapTable.setItems(newMapPoints);

						for(Node mapPoint : map.lookupAll(".series" + 0)) {
							mapPoint.setStyle("-fx-background-color: #0047ab");
						}

						addDialog.close();
					}
					else {
						Alert invalidCoordinates = new Alert(Alert.AlertType.ERROR);
						invalidCoordinates.setTitle("Maximum Distance Exceeded");
						invalidCoordinates.setHeaderText("Error: Maximum Distance Exceeded");
						invalidCoordinates.setContentText("Coordinates are outside of drone range");
						currentSimulation.getDeliveryPoints().removePoint(newPoint);
						invalidCoordinates.showAndWait();
					}
				}
				catch(IllegalArgumentException illegalArgument) {
					Alert invalidInput = new Alert(Alert.AlertType.ERROR);
					invalidInput.setTitle("Invalid Coordinates");
					invalidInput.setHeaderText("Error: Invalid Coordinates");
					invalidInput.setContentText("Input must be a (x,y) integer pair");
					currentSimulation.getDeliveryPoints().removePoint(newPoint);

					invalidInput.showAndWait();
				}
			});

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
		});

		Button deleteButton = new Button("Delete");
		deleteButton.setStyle(primaryButtonStyle());
		deleteButton.setOnAction(event -> {
			int deletedRow = mapTable.getSelectionModel().getSelectedIndex();
			if(deletedRow < 0) {
				Alert errorAlert = new Alert(Alert.AlertType.ERROR);
				errorAlert.setTitle("Invalid Deletion");
				errorAlert.setHeaderText("Error: Invalid Deletion");
				errorAlert.setContentText("Map point not selected.");
				errorAlert.showAndWait();
			}
			else {
				Point deletedPoint = mapTable.getSelectionModel().getSelectedItem();

				if (deletedPoint.getX() != 0 || deletedPoint.getY() != 0) {
					mapValues.getData().remove(deletedRow);

					int newUpperXBound = mapPoints.get(0).getX();
					int newUpperYBound = mapPoints.get(0).getY();
					int newLowerXBound = mapPoints.get(0).getX();
					int newLowerYBound = mapPoints.get(0).getY();

					for (Point point : mapPoints) {
						if (point.getX() >= newUpperXBound) {
							newUpperXBound = point.getX();
						}
						if (point.getX() <= newLowerXBound) {
							newLowerXBound = point.getX();
						}
						if (point.getY() >= newUpperYBound) {
							newUpperYBound = point.getY();
						}
						if (point.getY() <= newLowerYBound) {
							newLowerYBound = point.getY();
						}

						xAxis.setUpperBound(newUpperXBound + 100);
						xAxis.setLowerBound(newLowerXBound - 100);

						yAxis.setUpperBound(newUpperYBound + 100);
						yAxis.setLowerBound(newLowerYBound - 100);
					}

					mapPoints.remove(deletedRow);
					currentSimulation.getDeliveryPoints().removePoint(deletedPoint);
				} else {
					Alert blockOrigin = new Alert(Alert.AlertType.ERROR);
					blockOrigin.setTitle("Origin Deletion Attempted");
					blockOrigin.setHeaderText("Error: Origin Deletion Attempted");
					blockOrigin.setContentText("Cannot delete origin");
					blockOrigin.showAndWait();
				}
			}
		});

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

		//creates button for loading map
		Button loadButton = new Button("Load Map");
		loadButton.setStyle(primaryButtonStyle());
		//TODO: LOAD IN MAP

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
	 * COMPLETE AND TESTED!!!!!
	 * Displays results from simulation
	 * @author Rachel Franklin
	 */
	public void resultsPage() {
		//creates heading of page
		title = new Text("Simulation Results");
		title.setStyle("-fx-font-family: Serif; -fx-font-size: 30; -fx-fill: #0047ab");
		title.setWrappingWidth(400);
		title.setTextAlignment(TextAlignment.CENTER);

		//aligns title
		titleLayout = new HBox();
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.CENTER);

		//icon created by Google
		Image homeIcon = new Image("file:resources/home-button.png");
		ImageView homeView = new ImageView(homeIcon);

		Button homeButton = new Button("", homeView);
		homeButton.setStyle(PRIMARY_BACKGROUND_COLOR);
		homeButton.setOnAction(e-> window.setScene(mainMenu));

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

		//fifo stats
		Text fifoTitle = new Text("FIFO Delivery");
		fifoTitle.setStyle(BOLD_FONT_STYLE + "-fx-font-family: Serif; -fx-font-size: 18; -fx-fill: #0047ab");
		fifoTitle.setWrappingWidth(300);
		fifoTitle.setTextAlignment(TextAlignment.CENTER);

		double fifoAverageTime = results.getAverageFifoTime();

		Text fifoAverage = new Text(String.format("Average Delivery Time: %.1f minutes",
				fifoAverageTime / SECONDS_PER_MINUTE));
		fifoAverage.setStyle("-fx-font-family: Serif; -fx-font-size: 18; -fx-fill: #0047ab");
		fifoAverage.setWrappingWidth(300);
		fifoAverage.setTextAlignment(TextAlignment.CENTER);

		double fifoWorstTime = results.getWorstFifoTime();

		Text fifoWorst = new Text(String.format("Worst Delivery Time: %.1f minutes",
				fifoWorstTime / SECONDS_PER_MINUTE));
		fifoWorst.setStyle("-fx-font-family: Serif; -fx-font-size: 18; -fx-fill: #0047ab");
		fifoWorst.setWrappingWidth(300);
		fifoWorst.setTextAlignment(TextAlignment.CENTER);

		double fifoExpiredPercent = results.getPercentFifoExpired();
		DecimalFormat df = new DecimalFormat("##.####");
		df.format(fifoExpiredPercent);

		Text fifoExpired = new Text(String.format("Average Expired Orders: %.2f%%",
				(fifoExpiredPercent*100)));
		fifoExpired.setStyle("-fx-font-family: Serif; -fx-font-size: 18; -fx-fill: #0047ab");
		fifoExpired.setWrappingWidth(300);
		fifoExpired.setTextAlignment(TextAlignment.CENTER);

		VBox fifoLayout = new VBox();
		fifoLayout.setSpacing(5);
		fifoLayout.setAlignment(Pos.TOP_CENTER);
		fifoLayout.getChildren().addAll(fifoTitle, fifoAverage, fifoWorst, fifoExpired);

		//knapsack stats
		Text knapsackTitle = new Text("Knapsack Packing Delivery");
		knapsackTitle.setStyle(BOLD_FONT_STYLE + "-fx-font-family: Serif; -fx-font-size: 18; -fx-fill: #0047ab");
		knapsackTitle.setWrappingWidth(300);
		knapsackTitle.setTextAlignment(TextAlignment.CENTER);

		double knapsackAverageTime = results.getAverageKnapsackTime();

		Text knapsackAverage = new Text(String.format("Average Delivery Time: %.1f minutes",
				knapsackAverageTime / SECONDS_PER_MINUTE));
		knapsackAverage.setStyle("-fx-font-family: Serif; -fx-font-size: 18; -fx-fill: #0047ab");
		knapsackAverage.setWrappingWidth(300);
		knapsackAverage.setTextAlignment(TextAlignment.CENTER);

		double knapsackWorstTime = results.getWorstKnapsackTime();

		Text knapsackWorst = new Text(String.format("Worst Delivery Time: %.1f minutes",
				knapsackWorstTime / SECONDS_PER_MINUTE));
		knapsackWorst.setStyle("-fx-font-family: Serif; -fx-font-size: 18; -fx-fill: #0047ab");
		knapsackWorst.setWrappingWidth(300);
		knapsackWorst.setTextAlignment(TextAlignment.CENTER);

		double knapsackExpiredPercent = results.getPercentKnapsackExpired();
		df.format(knapsackExpiredPercent);	//Decimal format initialized earlier

		Text knapsackExpired = new Text(String.format("Average Expired Orders: %.2f%%",
				(knapsackExpiredPercent*100)));
		knapsackExpired.setStyle("-fx-font-family: Serif; -fx-font-size: 18; -fx-fill: #0047ab");
		knapsackExpired.setWrappingWidth(300);
		knapsackExpired.setTextAlignment(TextAlignment.CENTER);

		VBox knapsackLayout = new VBox();
		knapsackLayout.setSpacing(5);
		knapsackLayout.setAlignment(Pos.TOP_CENTER);
		knapsackLayout.getChildren().addAll(knapsackTitle, knapsackAverage, knapsackWorst, knapsackExpired);

		//set up stats layout
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

		final int columns = 26;

		var fifoDurations = new int[columns];
		var knapsackDurations = new int[columns];

		// determine the absolute minimum delivery time of all averages
		int minDuration = (int) results.getWorstFifoTime();

		for (var fifoTime : results.getFifoTimes()) {
			int duration = (int) (fifoTime / SECONDS_PER_MINUTE);
			if (minDuration > duration) minDuration = duration;
		}
		for (var knapsackTime : results.getKnapsackTimes()) {
			int duration = (int) (knapsackTime / SECONDS_PER_MINUTE);
			if (minDuration > duration) minDuration = duration;
		}

		// increment the array values to reflect the distribution
		for (var fifoTime : results.getFifoTimes()) {
			int duration = (int)(Math.floor(fifoTime) / SECONDS_PER_MINUTE);
			int index = (duration - minDuration);
			if (index < columns) fifoDurations[index]++;
			else fifoDurations[columns - 1]++;
		}
		for (var knapsackTime : results.getKnapsackTimes()) {
			int duration = (int)(Math.floor(knapsackTime) / SECONDS_PER_MINUTE);
			int index = (duration - minDuration);
			if (index < columns) knapsackDurations[index]++;
			else knapsackDurations[columns - 1]++;
		}

		var fifoSeries = new XYChart.Series<String, Number>(
				"FIFO", FXCollections.observableArrayList());
		var fifoSeriesData = fifoSeries.getData();

		// add the data values for fifo into the chart
		for (int index = 0; index < columns; index++) {
			int start = minDuration + index;
			int end = start + 1;
			var label = index == columns - 1 ? start + "+" : start + "-" + end;
			var data = new XYChart.Data<String, Number>(label, fifoDurations[index]);
			fifoSeriesData.add(data);
		}

		var knapsackSeries = new XYChart.Series<String, Number>(
				"Knapsack Packing", FXCollections.observableArrayList());
		var knapsackSeriesData = knapsackSeries.getData();

		// add the values for knapsack into the chart
		for (int index = 0; index < columns; index++) {
			int start = minDuration + index;
			int end = start + 1;
			var label = index == columns - 1 ? start + "+" : start + "-" + end;
			var data = new XYChart.Data<String, Number>(label, knapsackDurations[index]);
			knapsackSeriesData.add(data);
		}

		barChart.getData().add(fifoSeries);
		barChart.getData().add(knapsackSeries);

		// color the data columns
		for (var data : fifoSeries.getData())
			data.getNode().setStyle("-fx-bar-fill: #ae000b;");
		for (var data : knapsackSeries.getData())
			data.getNode().setStyle("-fx-bar-fill: #0047ab;");

		barChart.setStyle("-fx-background: #e0e0e0");

		//save results button
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
		});

		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		buttonBox.setPadding(new Insets(0,50, 30, 0));
		buttonBox.getChildren().add(saveButton);

		//combine boxes
		VBox finalLayout = new VBox();
		//top layout, stats, barChart, save button
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
