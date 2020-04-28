package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.FoodItem;
import com.dromedarydrones.food.Meal;
import com.dromedarydrones.location.Point;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that runs the simulation
 * @author Izzy Patnode and Rachel Franklin
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
	
	public static void main(String[] args) {
		
		//loads specified configuration settings
		Configuration configuration = Configuration.getInstance();

		try {
			// try loading the file
			configuration.initialize();
		} catch (IOException ioException) {
			try {
				// try loading the default GCC data
				configuration.initialize();
			} catch (Exception exception) {
				// something went very wrong
				exception.printStackTrace();
			}
		}

		//launches GUI and simulation
		launch(args); 
	}

	/**
	 * @author Izzy Patnode
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		window = primaryStage;

		//grabs current simulation for accessing necessary values
		currentSimulation = Configuration.getInstance().getCurrentSimulation();

		//adds camel image to main menu
		Image image = new Image("file:resources/CuteCamel.png");
		
		ImageView imageView = new javafx.scene.image.ImageView(image);
		
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
		buttonStart.setStyle(buttonStyle());

		//takes user to intermediate page when pressed/starts simulation
		buttonStart.setOnAction(event-> startSimulation());

		//creates icon for settings
		//Image settingsIcon = new Image("file:resources/SettingsIcon.png");

		//button for editing the simulation
		Button buttonEdit = new Button("Settings");
		buttonEdit.setMinWidth(buttons.getPrefWidth());
		buttonEdit.setStyle(buttonStyle());
		//takes user to general settings page when clicked
		buttonEdit.setOnAction(event -> generalEditPage());
		
		//button for exiting the gui
		Button buttonExit = new Button("Exit Simulation");
		buttonExit.setMinWidth(buttons.getPrefWidth());
		buttonExit.setStyle(buttonStyle());
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
		menuLayout.setStyle("-fx-background-color: #e0e0e0");
		
		
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
	 * Method for running the simulation
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
		String cssStyle = "-fx-background-color: #e0e0e0; " +
				"-fx-font-family: Serif; -fx-font-size: 14; -fx-text-fill: #0047ab;" +
				"-fx-border-width: 1; -fx-border-color: #0047ab";
		cancelButton.setStyle(cssStyle);

		//takes user back to main menu
		cancelButton.setOnAction(event -> window.setScene(mainMenu));

		//adds button to the display
		HBox simulationButton = new HBox(20);
		simulationButton.getChildren().add(cancelButton);
		simulationButton.setAlignment(Pos.BOTTOM_CENTER);

		//arranges all elements of the page on the screen
		VBox simulationLayout = new VBox(35);
		simulationLayout.getChildren().addAll(titleLayout, simulationPicture, simulationButton);
		simulationLayout.setAlignment(Pos.CENTER);
		simulationLayout.setStyle("-fx-background-color: #e0e0e0");

		root = new StackPane();
		root.getChildren().add(simulationLayout);

		Scene simulationPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(simulationPage);

		//window.show();

		//TODO: GET PAGE TO SHOW

		//results = currentSimulation.run();

		//resultsPage();
	}

	/**
	 * Allows for not writing out the style of each button each time we create a button
	 * @return a string with the style in css of each button
	 * @author Izzy Patnode
	 */
	public String buttonStyle() {
		 return "-fx-background-color: #e0e0e0; " +
		"-fx-font-family: Serif; -fx-font-size: 12; -fx-text-fill: #0047ab;" +
				"-fx-border-width: 1; -fx-border-color: #0047ab";
	}

	/**
	 * Creates title (Simulation Settings) for settings pages
	 * @author Izzy Patnode
	 */
	public void settingTitle() {
		//adds title to general settings page
		title = new Text("Simulation Settings");
		title.setFont(Font.font("Serif", 30));
		title.setFill(Color.BLACK);
		title.setWrappingWidth(400);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox();
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.CENTER);
	}

	/**
	 * Creates home button icon
	 * @author Izzy Patnode
	 */
	public void homeButton() {

		//icon created by Google
		Image homeIcon = new Image("file:resources/home-button.png");
		ImageView homeView = new ImageView(homeIcon);

		Button homeButton = new Button("", homeView);
		homeButton.setStyle("-fx-background-color: #e0e0e0");
		homeButton.setOnAction(e-> window.setScene(mainMenu));

		iconLayout = new HBox();
		iconLayout.setAlignment(Pos.TOP_LEFT);
		iconLayout.setPadding(new Insets(0, 0, 0, 15));
		iconLayout.getChildren().add(homeButton);
		iconLayout.setStyle("-fx-background-color: #e0e0e0");
	}

	/**
	 * Creates menu buttons for settings pages
	 * @author Izzy Patnode
	 */
	public void menuButtons() {
		buttonLayout = new VBox();
		buttonLayout.setPrefWidth(110);
		buttonLayout.setSpacing(5);

		Button generalButton = new Button("General Settings");
		generalButton.setMinWidth(buttonLayout.getPrefWidth());
		generalButton.setStyle(buttonStyle());
		generalButton.setOnAction(e -> generalEditPage());

		Button foodButton = new Button("Food Settings");
		foodButton.setMinWidth(buttonLayout.getPrefWidth());
		foodButton.setStyle(buttonStyle());
		foodButton.setOnAction(e -> editFoodPage());

		Button mealButton = new Button("Meal Settings");
		mealButton.setMinWidth(buttonLayout.getPrefWidth());
		mealButton.setStyle(buttonStyle());
		mealButton.setOnAction(e -> editMealsPage());

		Button droneButton = new Button("Drone Settings");
		droneButton.setMinWidth(buttonLayout.getPrefWidth());
		droneButton.setStyle(buttonStyle());
		droneButton.setOnAction(e -> editDronePage());

		Button mapButton = new Button("Map Settings");
		mapButton.setMinWidth(buttonLayout.getPrefWidth());
		mapButton.setStyle(buttonStyle());
		mapButton.setOnAction(e -> editMapPage());

		Button startButton = new Button("Start Simulation");
		startButton.setMinWidth(buttonLayout.getPrefWidth());
		startButton.setStyle(buttonStyle());
		startButton.setOnAction(e -> startSimulation());

		buttonLayout.getChildren().addAll(generalButton, foodButton, mealButton, droneButton, mapButton, startButton);
		buttonLayout.setAlignment(Pos.CENTER_LEFT);
		buttonLayout.setPadding(new Insets(0, 0, 0, 5));

	}

	/**
	 * Decreases redundancy of code used for importing and exporting settings
	 * @return Vbox containing buttons for importing and exporting buttons
	 * @author Izzy Patnode
	 */
	public VBox importExportSettings() {
		VBox saveLoadButtons = new VBox(5);
		saveLoadButtons.setPrefWidth(100);

		//adds buttons for loading and saving model
		Button saveButton = new Button("Import Settings");
		saveButton.setMinWidth(saveLoadButtons.getPrefWidth());
		saveButton.setStyle(buttonStyle());

		//saves current settings to simulation and opens file explorer to save to xml file
		saveButton.setOnAction(event -> {
			//saves settings in XML file to local machine
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Import Settings");
			fileChooser.getExtensionFilters().add(
					new FileChooser.ExtensionFilter("XML", "*.xml")
			);
			File file = fileChooser.showSaveDialog(window);
			if (file != null) {
				try { Configuration.getInstance().saveConfigs(file);
				} catch (IOException exception) { exception.printStackTrace(); }
			}
		});

		Button loadButton = new Button("Export Settings");
		loadButton.setMinWidth(saveLoadButtons.getPrefWidth());
		loadButton.setStyle(buttonStyle());

		//opens settings and loads model from user location
		loadButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Export Settings");
			fileChooser.getExtensionFilters().add(
					new FileChooser.ExtensionFilter("XML", "*.xml")
			);
			File file = fileChooser.showOpenDialog(window);
			if (file != null) {
				try {
					Configuration.getInstance().initialize(file);
					currentSimulation = Configuration.getInstance().getCurrentSimulation();
				} catch (IOException exception) { exception.printStackTrace(); }
			}
		});

		saveLoadButtons.getChildren().addAll(saveButton, loadButton);

		saveLoadButtons.setAlignment(Pos.BOTTOM_LEFT);
		saveLoadButtons.setPadding(new Insets(0, 0, 0, 10));

		return saveLoadButtons;
	}

	/**
	 * Creates GUI page for general settings (i.e. stochastic flow)
	 * @author Izzy Patnode and Rachel Franklin
	 */
	public void generalEditPage() {
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);

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
		gridHeading.setStyle("-fx-font-weight: bold");

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
		editButton.setStyle(buttonStyle());

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
				errorAlert.setHeaderText("Invalid Input!");
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
		settingLayout.setStyle("-fx-background-color: #e0e0e0");
		settingLayout.getChildren().addAll(leftLayout, mainLayout);

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
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);

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
				invalidInput.setContentText("Input must be an integer or a decimal.");
				errorIndex = 1;
			}
			else {
				double newValue = event.getNewValue();
				double maxPayload = currentSimulation.getDroneSettings().getMaxPayloadWeight();

				//drone capacity cannot exceed 12 lbs, so item weight cannot exceed 12 lbs
				if (newValue > maxPayload) {
					invalidInput.setTitle("Invalid Input");
					invalidInput.setContentText("Food item weight cannot exceed " + maxPayload + " oz.");
					errorIndex = 1;
				}
				else if (newValue <= 0) {
					invalidInput.setTitle("Invalid Input");
					invalidInput.setContentText("Food item weight must weigh something.");
					errorIndex = 1;
				}
				else {
					//drone capacity cannot exceed 12 lbs, so any meal cannot exceed 12 lbs
					ArrayList<Meal> mealTypes = currentSimulation.getMealTypes();
					String itemName = event.getTableView().getItems().get(event.getTablePosition().getRow()).getName();
					if(errorIndex == 0) {
						for (int meal = 0; meal < mealTypes.size(); meal++) {
							for (int food = 0; food < mealTypes.get(meal).getFoods().size(); food++) {
								FoodItem currItem = mealTypes.get(meal).getFoods().get(food);
								if (itemName.equals(currItem.getName())) {
									double newWeight = currItem.getWeight() - event.getOldValue() + newValue;
									if (newWeight > maxPayload) {
										invalidInput.setTitle("Invalid Input");
										invalidInput.setContentText("Input food item weight causes a meal weight to exceed the drone's " +
												"maximum payload weight.");
										errorIndex = 1;
									}
								}
							}//foods for loop
						}//meals for loop
					} //checking meals if statement
				} //else statement if newValue <= maxPayload
			} //else statement if maxPayload is a number

			if(errorIndex == 0) {
				//user input valid weight
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setWeight(event.getNewValue());
			}
			else {
				//put old value as
				event.getTableView().getItems().get(event.getTablePosition().getRow()).
						setWeight(oldValue);
				invalidInput.showAndWait();
			}
			//TODO: WHY DOES IT SHOW THE NEW VALUE UNTIL YOU CLICK ON IT AGAIN?????
		});//event end

		//adds columns to table
		foodTable.getColumns().setAll(itemHeading, weightHeading);
		foodTable.setPrefWidth(200);
		foodTable.setPrefHeight(300);
		foodTable.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, new BorderWidths(1))));

		//arranges table on screen
		StackPane tableLayout = new StackPane();
		tableLayout.setAlignment(Pos.TOP_RIGHT);
		tableLayout.setMaxSize(202, 300);
		tableLayout.getChildren().add(foodTable);

		//buttons for adding and deleting table rows
		Button addButton = new Button("Add");
		addButton.setStyle(buttonStyle());

		Button deleteButton = new Button("Delete");
		deleteButton.setStyle(buttonStyle());

		//arranges add and delete buttons relative to each other
		HBox editButtons = new HBox(10);
		editButtons.getChildren().addAll(addButton, deleteButton);
		editButtons.setPadding(new Insets(0, 0, 0, 60));

		VBox tableButtonLayout = new VBox(10);
		tableButtonLayout.getChildren().addAll(tableLayout, editButtons);
		tableButtonLayout.setPadding(new Insets(0,0,0,100));

		VBox centerLayout = new VBox();
		centerLayout.setSpacing(90);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, tableButtonLayout);

		//arranges all elements of the page on the screen
		settingLayout = new HBox(130);
		settingLayout.getChildren().addAll(leftLayout, centerLayout);
		settingLayout.setStyle("-fx-background-color: #e0e0e0");

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
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);

		homeButton();

		menuButtons();

		VBox importExportDisplay = importExportSettings();

		leftLayout.getChildren().addAll(iconLayout, buttonLayout, importExportDisplay);

		settingTitle();

		//arranges all meals together
		VBox mealsBox = new VBox(10);

		//creates a gridpane for each meal in the simulation
		for(Meal meal : currentSimulation.getMealTypes()) {
			//arranges meal with its components
			VBox singleMealLayout = new VBox();

			//sets up name of meal
			Text mealName = new Text(meal.getName());
			mealName.setFont(Font.font("Serif", 15));
			mealName.setFill(Color.BLACK);
			mealName.setWrappingWidth(200);
			mealName.setTextAlignment(TextAlignment.JUSTIFY);
			mealName.setStyle("-fx-font-weight: bold");

			HBox titleFormat = new HBox();
			titleFormat.getChildren().add(mealName);
			titleFormat.setPadding(new Insets(8, 0, 0, 5));

			//used to store each food item in the meal and how many of it there is
			HashMap<String, Integer> numberPerFood = new HashMap<>();
			//counts the number of each food item in the meal (i.e. 2 burgers, 1, fries, etc.)
			for(FoodItem mealItem: meal.getFoods()) {
				String name = mealItem.getName();

				if(numberPerFood.containsKey(name)) {
					numberPerFood.put(name, numberPerFood.get(name) + 1);
				}
				else {
					numberPerFood.put(name, 1);
				}
			}
			//we want to show items with a 0 that aren't in the meal too
			for (FoodItem foodItem: currentSimulation.getFoodItems()){
				String name = foodItem.getName();

				if(numberPerFood.containsKey(name)) {
					numberPerFood.put(name, numberPerFood.get(name) + 0);
				}
				else{
					numberPerFood.put(name, 0);
				}
			}

			//Map must be an observable to be used in a table
			ObservableList<HashMap.Entry<String, Integer>> itemCounts = FXCollections.
					observableArrayList(numberPerFood.entrySet());
			TableView<HashMap.Entry<String, Integer>> mealTable = new TableView(itemCounts);
			//When maxwidth = prefwidth, horizontal scroll bar shows up -- make maxWidth > prefWidth
			mealTable.setMaxSize(205, 300);
			mealTable.setEditable(true);

			//Table holds food items and counts for each meal
			TableColumn<HashMap.Entry<String, Integer>, String> foodColumn = new TableColumn("Food Item");
			foodColumn.setCellValueFactory(
					(TableColumn.CellDataFeatures<HashMap.Entry<String, Integer>, String> item) ->
					new SimpleStringProperty(item.getValue().getKey()));
			foodColumn.setPrefWidth(100);
			foodColumn.setEditable(true);

			TableColumn<ObservableMap.Entry<String, Integer>, Integer> countColumn = new TableColumn<>("Number in Meal");
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

				//user must input a double
				if (event.getNewValue() == null){
					invalidInput.setTitle("Invalid Input");
					invalidInput.setContentText("Input must be an integer.");
					errorIndex = 1;
				}
				else {

					double maxPayload = currentSimulation.getDroneSettings().getMaxPayloadWeight();
					int newValue = event.getNewValue();

					//Cannot have negative amounts of an item
					if (newValue < 0){
						invalidInput.setTitle("Invalid Input");
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
						for (FoodItem item : meal.getFoods()){
							meal.removeItem(item);
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
			mealTable.setPrefHeight(300);
			mealTable.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
					CornerRadii.EMPTY, new BorderWidths(1))));

			//probability must add up to 100% -- show user to verify
			Text probability = new Text("Probability: " + meal.getProbability());
			probability.setFont(Font.font("Serif", 15));
			probability.setFill(Color.BLACK);
			probability.setWrappingWidth(200);
			probability.setTextAlignment(TextAlignment.JUSTIFY);
			probability.setStyle("-fx-font-weight: bold");

			HBox probabilityFormat = new HBox();
			probabilityFormat.getChildren().add(probability);
			titleFormat.setPadding(new Insets(8, 0, 0, 5));


			//creates button for deleting meals
			Button deleteButton = new Button("X");
			deleteButton.setStyle("-fx-background-color: WHITE; -fx-font-weight: bold; -fx-font-size: 15; " +
					"fx-font-family: Serif");
			deleteButton.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID,
					CornerRadii.EMPTY, new BorderWidths(0))));

			//formats delete button with meal heading
			HBox topFormat = new HBox();
			topFormat.getChildren().addAll(titleFormat, deleteButton);

			//formats meal components
			singleMealLayout.setSpacing(5);
			singleMealLayout.setAlignment(Pos.CENTER);
			singleMealLayout.getChildren().addAll(topFormat, mealTable, probabilityFormat);

			//adds meal to layout of all meals
			mealsBox.getChildren().add(singleMealLayout);
		}

		//allows for user to scroll through meals
		ScrollPane mealLayout = new ScrollPane();
		mealLayout.setContent(mealsBox);
		mealLayout.setMaxSize(260, 400);
		mealLayout.setStyle("-fx-background: WHITE");
		mealLayout.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, new BorderWidths(1))));

		//formats display of menu column and full meal display
		VBox centerLayout = new VBox(80);
		centerLayout.setAlignment(Pos.TOP_CENTER);
		centerLayout.setPadding(new Insets(20,0,0,0));
		centerLayout.getChildren().addAll(titleLayout, mealLayout);

		Button addButton = new Button("Add Meal");
		addButton.setStyle(buttonStyle());

		VBox rightLayout = new VBox();
		rightLayout.setAlignment(Pos.BOTTOM_LEFT);
		rightLayout.setPadding(new Insets(0, 0, 70, 50));
		rightLayout.getChildren().add(addButton);

		HBox mainLayout = new HBox();
		mainLayout.getChildren().addAll(centerLayout, rightLayout);

		//arranges all elements of the page on the screen
		settingLayout = new HBox(130);
		settingLayout.getChildren().addAll(leftLayout, mainLayout);
		settingLayout.setStyle("-fx-background-color: #e0e0e0");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene mealEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(mealEditPage);
	}

	/**
	 * Creates GUI page for drone settings
	 * @author Izzy Patnode
	 */
	public void editDronePage() {
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);

		homeButton();

		menuButtons();

		VBox importExportDisplay = importExportSettings();

		leftLayout.getChildren().addAll(iconLayout, buttonLayout, importExportDisplay);

		settingTitle();

		Drone currentDrone = currentSimulation.getDroneSettings();

		//creates gridpane containing the current stochastic flow values
		Text maxPayload = new Text("Max Cargo Weight (lbs): ");
		maxPayload.setFont(Font.font("Serif", 15));

		Text speed = new Text("Average Cruising Speed (mph): ");
		speed.setFont(Font.font("Serif", 15));

		Text maxFlight = new Text("Max Flight Time (minutes): ");
		maxFlight.setFont(Font.font("Serif", 15));

		Text turnAround = new Text("Turn-around Time (minutes): ");
		turnAround.setFont(Font.font("Serif", 15));

		Text unloadTime = new Text("Unloading Delay (seconds): ");
		unloadTime.setFont(Font.font("Serif", 15));

		double currentPayload = currentDrone.getMaxPayloadWeight() / OUNCES_PER_POUND;
		TextField dronePayload = new TextField(String.format("%.2f", currentPayload));
		dronePayload.setMaxWidth(80);

		double currentSpeed = (currentDrone.getCruisingSpeed() * SECONDS_PER_HOUR) / FEET_PER_MILE;
		TextField droneSpeed = new TextField(String.format("%.2f", currentSpeed));
		droneSpeed.setMaxWidth(80);

		double currentFlightTime = currentDrone.getFlightTime() / SECONDS_PER_MINUTE;
		TextField droneFlight = new TextField(String.format("%.2f", currentFlightTime));
		droneFlight.setMaxWidth(80);

		double currentTurnAround = currentDrone.getTurnAroundTime() / SECONDS_PER_MINUTE;
		TextField droneTurnAround = new TextField(String.format("%.2f", currentTurnAround));
		droneTurnAround.setMaxWidth(80);

		double currentDelivery = currentDrone.getDeliveryTime();
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
		editButton.setStyle(buttonStyle());

		//sets current drone settings to edited drone settings
		editButton.setOnAction(event -> {
			try {
				currentDrone.setMaxPayloadWeight((Double.parseDouble(dronePayload.getText()) * OUNCES_PER_POUND));
				currentDrone.setCruisingSpeed((Double.parseDouble(droneSpeed.getText()) * FEET_PER_MILE) / SECONDS_PER_HOUR);
				currentDrone.setFlightTime(Double.parseDouble(droneFlight.getText()) * SECONDS_PER_MINUTE);
				currentDrone.setTurnAroundTime(Double.parseDouble(droneFlight.getText()) * SECONDS_PER_MINUTE);
				currentDrone.setDeliveryTime(Double.parseDouble(droneUnload.getText()));

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
				currentDrone.setMaxPayloadWeight(currentPayload * OUNCES_PER_POUND);
				currentDrone.setCruisingSpeed((currentSpeed * FEET_PER_MILE) / SECONDS_PER_HOUR);
				currentDrone.setFlightTime(currentFlightTime * SECONDS_PER_MINUTE);
				currentDrone.setTurnAroundTime(currentTurnAround * SECONDS_PER_MINUTE);
				currentDrone.setDeliveryTime(currentDelivery);

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
		settingLayout.setStyle("-fx-background-color: #e0e0e0");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene droneEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(droneEditPage);
	}

	/**
	 * Creates GUI page for map settings
	 * @author Izzy Patnode
	 */
	public void editMapPage() {
		VBox leftLayout = new VBox();
		leftLayout.setSpacing(110);

		homeButton();

		menuButtons();

		VBox importExportDisplay = importExportSettings();

		leftLayout.getChildren().addAll(iconLayout, buttonLayout, importExportDisplay);

		settingTitle();

		//gets list of current map destinations
		ObservableList<Point> mapPoints = currentSimulation.getDeliveryPoints().getPoints();

		//finds maximum and minimum destinations points (in coordinates)
		int maxY = mapPoints.get(0).getY();
		int maxX = mapPoints.get(0).getX();
		int minY = mapPoints.get(0).getY();
		int minX = mapPoints.get(0).getX();

		for(Point point : mapPoints) {
			int currentY = point.getY();
			int currentX = point.getX();

			if (currentY >= maxY) {
				maxY = currentY;
			}
			if(currentY <= minY) {
				minY = currentY;
			}
			if(currentX >= maxX) {
				maxX = currentX;
			}
			if(currentX <= minX) {
				minX = currentX;
			}
		}

		//creates the axes for the map scatter plot
		NumberAxis xAxis = new NumberAxis(minX - 100, maxX + 100, 100);
		xAxis.setLabel("");
		xAxis.setTickMarkVisible(false);
		NumberAxis yAxis = new NumberAxis(minY - 100, maxY + 100, 100);
		yAxis.setLabel("");
		yAxis.setTickMarkVisible(false);

		//creates scatter plot for map
		ScatterChart<Number, Number> map = new ScatterChart<>(xAxis, yAxis);
		map.setHorizontalGridLinesVisible(false);
		map.setVerticalGridLinesVisible(false);
		map.setLegendVisible(false);
		map.setStyle("-fx-background: WHITE");

		//creates points from destination coordinates for scatter plot
		XYChart.Series<Number, Number> mapValues = new XYChart.Series<>();

		for(Point destination : mapPoints) {
			mapValues.getData().add(new XYChart.Data<>(destination.getX(), destination.getY()));
		}
		//adds points to scatter plot
		map.getData().add(mapValues);

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

		//creates columns for table
		TableColumn<Point, String> pointHeading = new TableColumn<>("Drop-Off Point");
		pointHeading.setCellValueFactory(new PropertyValueFactory<>("name"));

		//allows user to edit the name of a point already in the table
		pointHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		pointHeading.setOnEditCommit(event ->
				event.getTableView().getItems().get(event.getTablePosition().
						getRow()).setName(event.getNewValue() + ""));

		TableColumn<Point, String> xyHeading = new TableColumn<>("(x,y)");
		xyHeading.setCellValueFactory(new PropertyValueFactory<>("coordinates"));

		//TODO: UPDATE MAP WITH NEW LOCATION OF POINT

		//allows user to edit the coordinates of a point already in the table
		xyHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		xyHeading.setOnEditCommit(event -> {
				try {
					event.getTableView().getItems().get(event.getTablePosition().getRow()).
							setCoordinates(event.getNewValue() + "");
				}
				catch(IllegalArgumentException illegalArgument) {
					Alert invalidInput = new Alert(Alert.AlertType.ERROR);
					invalidInput.setTitle("Invalid Coordinate");
					invalidInput.setContentText("Input must be a (x,y) integer pair");
					event.getTableView().getItems().get(event.getTablePosition().getRow()).
							setCoordinates((event.getOldValue()));
					invalidInput.showAndWait();
				}
		});

		//TODO: FIND A WAY TO GET THE OLD VALUE TO SHOW UP INSTANTLY???

		//adds column headings to table
		mapTable.getColumns().setAll(pointHeading, xyHeading);
		mapTable.setPrefWidth(275);
		mapTable.setPrefHeight(300);
		mapTable.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, new BorderWidths(1))));

		//arranges table
		StackPane tableLayout = new StackPane();
		tableLayout.setMaxSize(275, 300);
		tableLayout.setAlignment(Pos.CENTER);
		tableLayout.getChildren().add(mapTable);

		//buttons for adding and deleting table rows
		Button addButton = new Button("Add");
		addButton.setStyle(buttonStyle());
		//addButton.setOnAction(new AddButtonListener());

		Button deleteButton = new Button("Delete");
		deleteButton.setStyle(buttonStyle());
		//deleteButton.setOnAction(new DeleteButtonListener());

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
		loadButton.setStyle(buttonStyle());
		//TODO: LOAD IN MAP??? ALSO WANT TO SAVE MAP???

		HBox loadDisplay = new HBox();
		loadDisplay.getChildren().add(loadButton);
		loadDisplay.setAlignment(Pos.CENTER_RIGHT);
		loadDisplay.setPadding(new Insets(50,95,0,0));

		VBox mainLayout = new VBox();
		mainLayout.getChildren().addAll(centerLayout, loadDisplay);

		//arranges all elements of the page on the screen
		settingLayout = new HBox(130);
		settingLayout.getChildren().addAll(leftLayout, mainLayout);
		settingLayout.setStyle("-fx-background-color: #e0e0e0");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene mapEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(mapEditPage);

		//changing the color of the points on the map must be done after the scene is set
		for(Node mapPoint : map.lookupAll(".series" + 0)) {
			mapPoint.setStyle("-fx-background-color: blue");
		}
	}

	/**
	 * Displays results from simulation
	 * @author Rachel Franklin
	 */
	public void resultsPage() {

		//creates heading of page
		title = new Text("Simulation Results");
		title.setFont(Font.font("Serif", 30));
		title.setFill(Color.BLACK);
		title.setWrappingWidth(400);
		title.setTextAlignment(TextAlignment.CENTER);

		//aligns title
		titleLayout = new HBox();
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.CENTER);

		//sets up home button icon
		homeButton();

		//configures display of home button and page title
		HBox topLayout = new HBox();
		topLayout.setSpacing(141);
		topLayout.setAlignment(Pos.TOP_LEFT);
		topLayout.getChildren().addAll(iconLayout, titleLayout);

		//fifo stats
		Text fifoTitle = new Text("FIFO Delivery");
		fifoTitle.setFont(Font.font("Serif", FontWeight.BOLD, 18));
		fifoTitle.setFill(Color.BLACK);
		fifoTitle.setWrappingWidth(300);
		fifoTitle.setTextAlignment(TextAlignment.CENTER);

		double fifoAverageTime = results.getAverageFifoTime();

		Text fifoAverage = new Text(String.format("Average Delivery Time: %.1f minutes",
				fifoAverageTime / SECONDS_PER_MINUTE));
		fifoAverage.setFont(Font.font("Serif", 18));
		fifoAverage.setFill(Color.BLACK);
		fifoAverage.setWrappingWidth(300);
		fifoAverage.setTextAlignment(TextAlignment.CENTER);

		double fifoWorstTime = results.getWorstFifoTime();

		Text fifoWorst = new Text(String.format("Worst Delivery Time: %.1f minutes",
				fifoWorstTime / SECONDS_PER_MINUTE));
		fifoWorst.setFont(Font.font("Serif", 18));
		fifoWorst.setFill(Color.BLACK);
		fifoWorst.setWrappingWidth(300);
		fifoWorst.setTextAlignment(TextAlignment.CENTER);

		VBox fifoLayout = new VBox();
		fifoLayout.setSpacing(5);
		fifoLayout.setAlignment(Pos.TOP_CENTER);
		fifoLayout.getChildren().addAll(fifoTitle, fifoAverage, fifoWorst);

		//knapsack stats
		Text knapsackTitle = new Text("Knapsack Packing Delivery");
		knapsackTitle.setFont(Font.font("Serif", FontWeight.BOLD, 18));
		knapsackTitle.setFill(Color.BLACK);
		knapsackTitle.setWrappingWidth(300);
		knapsackTitle.setTextAlignment(TextAlignment.CENTER);

		double knapsackAverageTime = results.getAverageKnapsackTime();

		Text knapsackAverage = new Text(String.format("Average Delivery Time: %.1f minutes",
				knapsackAverageTime / SECONDS_PER_MINUTE));
		knapsackAverage.setFont(Font.font("Serif", 18));
		knapsackAverage.setFill(Color.BLACK);
		knapsackAverage.setWrappingWidth(300);
		knapsackAverage.setTextAlignment(TextAlignment.CENTER);

		double knapsackWorstTime = results.getWorstKnapsackTime();

		Text knapsackWorst = new Text(String.format("Worst Delivery Time: %.1f minutes",
				knapsackWorstTime / SECONDS_PER_MINUTE));
		knapsackWorst.setFont(Font.font("Serif", 18));
		knapsackWorst.setFill(Color.BLACK);
		knapsackWorst.setWrappingWidth(300);
		knapsackWorst.setTextAlignment(TextAlignment.CENTER);

		VBox knapsackLayout = new VBox();
		knapsackLayout.setSpacing(5);
		knapsackLayout.setAlignment(Pos.TOP_CENTER);
		knapsackLayout.getChildren().addAll(knapsackTitle, knapsackAverage, knapsackWorst);

		//set up stats layout
		HBox statsLayout = new HBox();
		statsLayout.setAlignment(Pos.CENTER);
		statsLayout.getChildren().addAll(fifoLayout, knapsackLayout);

		//sets up BarChart (histogram)
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
		barChart.setCategoryGap(0.4);
		barChart.setBarGap(0.4);
		xAxis.setLabel("Delivery Time (in minutes)");
		yAxis.setLabel("Number of Orders");

		//sets up fifo and knapsack packing series of data
		XYChart.Series<String, Number> fifoSeries = new XYChart.Series<>();
		XYChart.Series<String, Number> knapsackSeries = new XYChart.Series<>();
		fifoSeries.setName("FIFO");
		knapsackSeries.setName("Knapsack Packing");

        int [] fifoCount = new int [26];    //keeps track of how many orders occur per each time range
		ArrayList<Double> fifoTimes = new ArrayList<>(results.getFifoTimes());

        //count number of orders per time slot
        for (int index = 0; index < fifoTimes.size(); index++){
			//get the floor of the order delivery time
            int time = (int)(Math.floor(fifoTimes.get(index)) / SECONDS_PER_MINUTE);
            if (time < 25){
            	fifoCount[time]++;  //increment orders in this time slot
			}
            else{
            	fifoCount[25]++;
			}
        }

        //add data from fifo times
        for (int index = 0; index < fifoCount.length - 1; index++){
            fifoSeries.getData().add(new XYChart.Data<>(index + "-" + (index + 1), fifoCount[index]));
        }
        fifoSeries.getData().add(new XYChart.Data<>("25+", fifoCount[25]));

        int [] knapsackCount = new int [26];	//keeps track of how many orders occur per each time range
        ArrayList<Double> knapsackTimes = new ArrayList<>(results.getKnapsackTimes());

        //count number of orders per time slot
		for (int index = 0; index < knapsackTimes.size(); index++){
			//get the floor of the order delivery time
			int time = (int)(Math.floor(knapsackTimes.get(index)) / SECONDS_PER_MINUTE);
			if (time < 25) {
				knapsackCount[time]++;  //increment orders in this time slot
			}
			else{
				knapsackCount[25]++;
			}
		}

		//add data from knapsack times
		for (int index = 0; index < knapsackCount.length - 1; index++){
			knapsackSeries.getData().add(new XYChart.Data<>(index + "-" + (index + 1), knapsackCount[index]));
		}
		knapsackSeries.getData().add(new XYChart.Data<>("25+", knapsackCount[25]));

		//add series data to bar chart
		barChart.getData().add(fifoSeries);
		barChart.getData().add(knapsackSeries);

		//change bar colors on chart
		for (XYChart.Data data: fifoSeries.getData()){
			data.getNode().setStyle("-fx-bar-fill: red;");
		}
		for (XYChart.Data data: knapsackSeries.getData()){
			data.getNode().setStyle("-fx-bar-fill: blue;");
		}

		barChart.setStyle("-fx-background: WHITE");

		//save results button
		Button saveButton = new Button("Save Results");
		saveButton.setStyle(buttonStyle());

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
		finalLayout.setStyle("-fx-background-color: #e0e0e0");

		StackPane root = new StackPane();
		root.getChildren().add(finalLayout);

		Scene resultsPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(resultsPage);

		//change legend colors to reflect bar colors
		//note - this must be done after scene is set
		for (Node node : barChart.lookupAll(".bar-legend-symbol.default-color0")) {
			node.setStyle("-fx-background-color: red;");
		}
		for (Node node : barChart.lookupAll(".bar-legend-symbol.default-color1")) {
			node.setStyle("-fx-background-color: blue;");
		}
	}
}
