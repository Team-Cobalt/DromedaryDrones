package com.dromedarydrones.mainapp;

import com.dromedarydrones.food.FoodItem;
import com.dromedarydrones.food.Meal;
import com.dromedarydrones.location.Point;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that runs the simulation
 * @author Isabella Patnode and Rachel Franklin
 */
public class MainClass extends Application {
	private Stage window; //used for creating gui
	private Scene mainMenu; //main menu page
	private StackPane root;
	private Text title; //title of page
	private HBox titleLayout; //layout regarding title of page
	private HBox iconLayout; //layout of home icon
	private VBox buttonLayout; //layout of setting's menu buttons
	private VBox settingLayout; //layout of all icons in setting pages
	private Simulation currentSimulation; //current simulation being run
	private SimulationResults results;
	
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
	 * @author Isabella Patnode
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
		buttonStart.setOnAction(e-> startSimulation());
		
		//button for editing the simulation
		Button buttonEdit = new Button("Settings");
		buttonEdit.setMinWidth(buttons.getPrefWidth());
		buttonEdit.setStyle(buttonStyle());
		//takes user to general settings page when clicked
		buttonEdit.setOnAction(e -> generalEditPage());
		
		//button for exiting the gui
		Button buttonExit = new Button("Exit Simulation");
		buttonExit.setMinWidth(buttons.getPrefWidth());
		buttonExit.setStyle(buttonStyle());
		//exits the screen (gui) when clicked
		buttonExit.setOnAction(e-> System.exit(0));
		
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
	 * @author Isabella Patnode
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
		cancelButton.setOnAction(e -> window.setScene(mainMenu));

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
	 * @author Isabella Patnode
	 */
	public String buttonStyle() {
		 return "-fx-background-color: #e0e0e0; " +
		"-fx-font-family: Serif; -fx-font-size: 12; -fx-text-fill: #0047ab;" +
				"-fx-border-width: 1; -fx-border-color: #0047ab";
	}

	/**
	 * Creates title (Simulation Settings) for settings pages
	 * @author Isabella Patnode
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
	 * @author Isabella Patnode
	 */
	public void homeButton() {

		//icon taken from Google
		Image homeIcon = new Image("file:resources/home-button.png");
		ImageView homeView = new ImageView(homeIcon);

		Button homeButton = new Button("", homeView);
		homeButton.setStyle("-fx-background-color: #e0e0e0");
		homeButton.setOnAction(e-> window.setScene(mainMenu));

		iconLayout = new HBox();
		iconLayout.setAlignment(Pos.TOP_LEFT);
		iconLayout.getChildren().add(homeButton);
		iconLayout.setStyle("-fx-background-color: #e0e0e0");
	}

	/**
	 * Creates menu buttons for settings pages
	 * @author Isabella Patnode
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
	 * Method for arranging title and home icon of settings
	 * @return layout of title and home icon
	 * @author Isabella Patnode
	 */
	public HBox settingsTopLayout() {
		//creates heading of the page
		settingTitle();

		//allows user to return to main page
		homeButton();

		//configures display of home button and page title
		HBox layout = new HBox();
		layout.setSpacing(141);
		layout.setAlignment(Pos.TOP_LEFT);
		layout.getChildren().addAll(iconLayout, titleLayout);

		return layout;
	}

	/**
	 * Creates GUI page for general settings (i.e. stochastic flow)
	 * @author Isabella Patnode
	 */
	public void generalEditPage() {
		HBox topLayout = settingsTopLayout();

		//sets up menu buttons
		menuButtons();

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

		TextField hourOneMeals = new TextField(currentModel.get(0).toString());
		hourOneMeals.setMaxWidth(80);
		TextField hourTwoMeals = new TextField(currentModel.get(1).toString());
		hourTwoMeals.setMaxWidth(80);
		TextField hourThreeMeals = new TextField(currentModel.get(2).toString());
		hourThreeMeals.setMaxWidth(80);
		TextField hourFourMeals = new TextField(currentModel.get(3).toString());
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

		//configures display of menu buttons and gridpane
		HBox centerLayout = new HBox();
		centerLayout.setSpacing(220);
		centerLayout.setAlignment(Pos.CENTER_LEFT);
		centerLayout.getChildren().addAll(buttonLayout, gridLayout);

		//arranges btns for loading and saving model
		VBox saveLoadButtons = new VBox();
		saveLoadButtons.setPrefWidth(100);
		saveLoadButtons.setSpacing(10);
		saveLoadButtons.setAlignment(Pos.BOTTOM_RIGHT);
		saveLoadButtons.setPadding(new Insets(0, 80, 100, 0));

		//adds buttons for loading and saving model
		Button saveButton = new Button("Save Changes");
		saveButton.setMinWidth(saveLoadButtons.getPrefWidth());
		saveButton.setStyle(buttonStyle());

		//opens file explorer and saves current settings
		saveButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Changes");
			fileChooser.getExtensionFilters().add(
					new FileChooser.ExtensionFilter("XML", "*.xml")
			);
			File file = fileChooser.showSaveDialog(window);
			if (file != null) {
				try { Configuration.getInstance().saveConfigs(file);
				} catch (IOException exception) { exception.printStackTrace(); }
			}
		});

		Button loadButton = new Button("Load Model");
		loadButton.setMinWidth(saveLoadButtons.getPrefWidth());
		loadButton.setStyle(buttonStyle());

		//opens settings and loads model from user location
		loadButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Load Settings");
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

		saveLoadButtons.getChildren().addAll(loadButton, saveButton);

		//arranges top layout (home btn and title) and center layout (menu btn and gridpane)
		settingLayout = new VBox();
		settingLayout.setSpacing(103);
		settingLayout.setPadding(new Insets(10, 0 , 0, 0));
		settingLayout.setStyle("-fx-background-color: #e0e0e0");
		settingLayout.getChildren().addAll(topLayout, centerLayout, saveLoadButtons);

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene generalEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(generalEditPage);
	}

	/**
	 * Creates GUI page for food items settings
	 * @author Rachel Franklin and Isabella Patnode
	 */
	public void editFoodPage() {
		HBox topLayout = settingsTopLayout();

		//sets up menu buttons
		menuButtons();

		//create table of food items in simulation
		TableView<FoodItem> foodTable = new TableView<>();
		ObservableList<FoodItem> foodItems = currentSimulation.getFoodItems();
		foodTable.setItems(foodItems);

		//Create table headings
		TableColumn<FoodItem, String> itemHeading = new TableColumn<>("Food Item");
		itemHeading.setCellValueFactory(new PropertyValueFactory<>("name"));
		itemHeading.setPrefWidth(100);

		TableColumn<FoodItem, String> weightHeading = new TableColumn<>("Weight (oz)");
		weightHeading.setCellValueFactory(new PropertyValueFactory<>("weight"));
		weightHeading.setPrefWidth(100);

		//adds columns to table
		foodTable.getColumns().setAll(itemHeading, weightHeading);
		foodTable.setPrefWidth(200);
		foodTable.setPrefHeight(300);
		foodTable.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, new BorderWidths(1))));

		//arranges table on screen
		StackPane tableLayout = new StackPane();
		tableLayout.setAlignment(Pos.CENTER);
		tableLayout.setMaxSize(300, 300);
		tableLayout.getChildren().add(foodTable);

		//arranges menu buttons and table in the center (vertically)
		HBox centerLayout = new HBox();
		centerLayout.setSpacing(220);
		centerLayout.setAlignment(Pos.CENTER_LEFT);
		centerLayout.getChildren().addAll(buttonLayout, tableLayout);

		//buttons for adding and deleting table rows
		Button addButton = new Button("Add");
		addButton.setStyle(buttonStyle());

		Button deleteButton = new Button("Delete");
		deleteButton.setStyle(buttonStyle());

		//arranges add and delete buttons relative to each other
		HBox editButtons = new HBox(10);
		editButtons.setAlignment(Pos.TOP_CENTER);
		editButtons.getChildren().addAll(addButton, deleteButton);
		editButtons.setPadding(new Insets(0, 20, 0, 0));

		VBox tableButtonLayout = new VBox(10);
		tableButtonLayout.getChildren().addAll(centerLayout, editButtons);

		//arranges buttons for loading and saving model
		VBox saveLoadButtons = new VBox();
		saveLoadButtons.setPrefWidth(100);
		saveLoadButtons.setSpacing(10);
		saveLoadButtons.setAlignment(Pos.BOTTOM_RIGHT);
		saveLoadButtons.setPadding(new Insets(0, 80, 0, 0));

		//creates button for loading food items
		Button loadButton = new Button("Load Foods");
		loadButton.setMinWidth(saveLoadButtons.getPrefWidth());
		loadButton.setStyle(buttonStyle());

		//adds button for saving food items
		Button saveButton = new Button("Save Changes");
		saveButton.setMinWidth(saveLoadButtons.getPrefWidth());
		saveButton.setStyle(buttonStyle());

		saveLoadButtons.getChildren().addAll(loadButton, saveButton);

		VBox displayLayout = new VBox(10);
		displayLayout.getChildren().addAll(tableButtonLayout, saveLoadButtons);

		//arranges all elements of the page on the screen
		settingLayout = new VBox(30);
		settingLayout.getChildren().addAll(topLayout, displayLayout);
		settingLayout.setStyle("-fx-background-color: #e0e0e0");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		 Scene foodEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(foodEditPage);
	}

	/**
	 * Creates GUI page for meal settings
	 * @author Isabella Patnode
	 */
	public void editMealsPage() {
		HBox topLayout = settingsTopLayout();

		//sets up menu buttons
		menuButtons();

		//sets padding of menu buttons to match rest of settings pages
		buttonLayout.setAlignment(Pos.TOP_LEFT);
		buttonLayout.setPadding(new Insets(68, 0, 0, 5));

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
			titleFormat.setPadding(new Insets(8, 0, 0, 0));

			//used to store each food item in the meal and how many of it there is
			HashMap<String, Integer> numberPerFood = new HashMap<>();

			//formats each food item and the probability of it
			GridPane mealFoods = new GridPane();
			mealFoods.setAlignment(Pos.CENTER_RIGHT);
			mealFoods.setVgap(1);
			mealFoods.setHgap(5);
			mealFoods.setMaxSize(200, 200);

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

			int index = 0;

			/*adds each food item and their corresponding count in meals (i.e. 2 hamburger, 0 fries)
			**to the grid*/
			for(FoodItem food: currentSimulation.getFoodItems()) {
				String currentFood = food.getName();
				Text foodName = new Text(currentFood + ":");
				foodName.setFont(Font.font("Serif", 15));
				mealFoods.add(foodName, 0, index);

				//gets # of the specific food item in the meal
				if(numberPerFood.containsKey(currentFood)) {
					TextField foodCount = new TextField(numberPerFood.get(currentFood).toString());
					foodCount.setMaxWidth(80);
					mealFoods.add(foodCount, 1, index);
				}
				else {
					TextField foodCount = new TextField("0");
					foodCount.setMaxWidth(80);
					mealFoods.add(foodCount, 1, index);
				}

				index++;
			}

			//creates and formats probability of meal display
			Text probabilityName = new Text("Probability:");
			probabilityName.setFont(Font.font("Serif", 15));
			mealFoods.add(probabilityName, 0, index);
			TextField probabilityValue = new TextField(meal.getProbability() + "");
			probabilityValue.setMaxWidth(80);
			mealFoods.add(probabilityValue, 1, index);

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
			singleMealLayout.getChildren().addAll(topFormat, mealFoods);

			//adds meal to layout of all meals
			mealsBox.getChildren().add(singleMealLayout);
		}

		//allows for user to scroll through meals
		ScrollPane mealLayout = new ScrollPane();
		mealLayout.setContent(mealsBox);
		mealLayout.setPrefSize(250, 400);
		mealLayout.setStyle("-fx-background: WHITE");
		mealLayout.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, new BorderWidths(1))));


		//formats display of buttons for changing meals
		VBox changeButtons = new VBox();
		changeButtons.setPrefWidth(100);
		changeButtons.setSpacing(10);
		changeButtons.setAlignment(Pos.BOTTOM_RIGHT);

		//adds buttons for adding, loading, and saving meals
		Button addButton = new Button("Add Meal");
		addButton.setMinWidth(changeButtons.getPrefWidth());
		addButton.setStyle(buttonStyle());

		Button loadButton = new Button("Load Meals");
		loadButton.setMinWidth(changeButtons.getPrefWidth());
		loadButton.setStyle(buttonStyle());

		Button saveButton = new Button("Save Changes");
		saveButton.setMinWidth(changeButtons.getPrefWidth());
		saveButton.setStyle(buttonStyle());

		changeButtons.getChildren().addAll(addButton, loadButton, saveButton);

		//formats display of meal layout and corresponding buttons
		HBox mealButtonLayout = new HBox(100);
		mealButtonLayout.getChildren().addAll(mealLayout, changeButtons);

		//formats display of menu column and full meal display
		HBox centerLayout = new HBox(200);
		centerLayout.getChildren().addAll(buttonLayout, mealButtonLayout);

		//arranges all elements of the page on the screen
		settingLayout = new VBox(35);
		settingLayout.getChildren().addAll(topLayout, centerLayout);
		settingLayout.setStyle("-fx-background-color: #e0e0e0");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene mealEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(mealEditPage);
	}

	/**
	 * Creates GUI page for drone settings
	 * @author Isabella Patnode
	 */
	public void editDronePage() {
		HBox topLayout = settingsTopLayout();

		//sets up menu buttons
		menuButtons();

		//TODO: PUT IN DRONE SETTINGS!!!

		//arranges buttons for loading and saving map and table of points
		VBox saveLoadButtons = new VBox();
		saveLoadButtons.setPrefWidth(100);
		saveLoadButtons.setSpacing(10);
		saveLoadButtons.setAlignment(Pos.BOTTOM_RIGHT);
		saveLoadButtons.setPadding(new Insets(0, 80, 0, 0));

		//creates button for loading map
		Button loadButton = new Button("Load Drone");
		loadButton.setMinWidth(saveLoadButtons.getPrefWidth());
		loadButton.setStyle(buttonStyle());


		//adds buttons for loading and saving model
		Button saveButton = new Button("Save Changes");
		saveButton.setMinWidth(saveLoadButtons.getPrefWidth());
		saveButton.setStyle(buttonStyle());

		saveLoadButtons.getChildren().addAll(loadButton, saveButton);

		//arranges map elements with load and save buttons
		VBox mainLayout = new VBox();
		mainLayout.getChildren().addAll(buttonLayout, saveLoadButtons);
		mainLayout.setSpacing(50);

		//arranges all elements of the page on the screen
		settingLayout = new VBox(30);
		settingLayout.getChildren().addAll(topLayout, mainLayout);
		settingLayout.setStyle("-fx-background-color: #e0e0e0");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene droneEditPage = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(droneEditPage);
	}

	/**
	 * Creates GUI page for map settings
	 * @author Isabella Patnode
	 */
	public void editMapPage() {
		HBox topLayout = settingsTopLayout();

		//sets up menu buttons
		menuButtons();

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
		plotLayout.setAlignment(Pos.CENTER);

		//creates table of current simulation's points
		TableView<Point> mapTable = new TableView<>();

		//adds cell values to table
		mapTable.setItems(mapPoints);
		mapTable.setEditable(true);

		//creates columns for table
		TableColumn<Point, String> pointHeading = new TableColumn<>("Drop-Off Point");
		pointHeading.setCellValueFactory(new PropertyValueFactory<>("name"));

		//TODO: UPDATE MAP WITH NEW LOCATION OF POINT

		//allows user to edit the name of a point already in the table
		pointHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		pointHeading.setOnEditCommit((EventHandler<CellEditEvent<Point, String>>) event ->
				((Point) event.getTableView().getItems().get(event.getTablePosition().
						getRow())).setName(event.getNewValue() + ""));

		TableColumn<Point, String> xyHeading = new TableColumn<>("(x,y)");
		xyHeading.setCellValueFactory(new PropertyValueFactory<>("coordinates"));

		//TODO: UPDATE MAP WITH NEW LOCATION OF POINT

		//allows user to edit the coordinates of a point already in the table
		xyHeading.setCellFactory(TextFieldTableCell.forTableColumn());
		xyHeading.setOnEditCommit((EventHandler<CellEditEvent<Point, String>>) event ->
				((Point) event.getTableView().getItems().get(event.getTablePosition().
						getRow())).setCoordinates(event.getNewValue() + ""));

		//adds column headings to table
		mapTable.getColumns().setAll(pointHeading, xyHeading);
		mapTable.setPrefWidth(275);
		mapTable.setPrefHeight(300);
		mapTable.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, new BorderWidths(1))));

		//arranges table
		StackPane tableLayout = new StackPane();
		tableLayout.setMaxSize(300, 300);
		tableLayout.setAlignment(Pos.CENTER);
		tableLayout.getChildren().add(mapTable);

		//arranges map and table of points with consideration to each other
		HBox mapLayout = new HBox();
		mapLayout.setSpacing(40);
		mapLayout.setAlignment(Pos.CENTER_RIGHT);
		mapLayout.getChildren().addAll(plotLayout, tableLayout);

		//arranges map and menu buttons in the center vertically
		HBox centerLayout = new HBox();
		centerLayout.setSpacing(160);
		centerLayout.setAlignment(Pos.CENTER_LEFT);
		centerLayout.getChildren().addAll(buttonLayout, mapLayout);

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

		//arranges map, table, and table buttons together
		VBox buttonMap = new VBox();
		buttonMap.getChildren().addAll(centerLayout, addDeleteButtons);
		buttonMap.setSpacing(10);

		//arranges buttons for loading and saving map and table of points
		VBox saveLoadButtons = new VBox();
		saveLoadButtons.setPrefWidth(100);
		saveLoadButtons.setSpacing(10);
		saveLoadButtons.setAlignment(Pos.BOTTOM_RIGHT);
		saveLoadButtons.setPadding(new Insets(0, 80, 0, 0));

		//creates button for loading map
		Button loadButton = new Button("Load Map");
		loadButton.setMinWidth(saveLoadButtons.getPrefWidth());
		loadButton.setStyle(buttonStyle());


		//adds buttons for loading and saving model
		Button saveButton = new Button("Save Changes");
		saveButton.setMinWidth(saveLoadButtons.getPrefWidth());
		saveButton.setStyle(buttonStyle());

		saveLoadButtons.getChildren().addAll(loadButton, saveButton);

		//arranges map elements with load and save buttons
		VBox mainLayout = new VBox();
		mainLayout.getChildren().addAll(buttonMap, saveLoadButtons);
		mainLayout.setSpacing(50);

		//arranges all elements of the page on the screen
		settingLayout = new VBox(30);
		settingLayout.getChildren().addAll(topLayout, mainLayout);
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

		double fifoAverageTime = results.getAverageFifoTime();	//change once you can access the results

		Text fifoAverage = new Text(String.format("Average Delivery Time: %.1f minutes", fifoAverageTime / 60));
		fifoAverage.setFont(Font.font("Serif", 18));
		fifoAverage.setFill(Color.BLACK);
		fifoAverage.setWrappingWidth(300);
		fifoAverage.setTextAlignment(TextAlignment.CENTER);

		double fifoWorstTime = results.getWorstFifoTime();	//change once you can access the results

		Text fifoWorst = new Text(String.format("Worst Delivery Time: %.1f minutes", fifoWorstTime / 60));
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

		double knapsackAverageTime = results.getAverageKnapsackTime();	//change once you can access the results

		Text knapsackAverage = new Text(String.format("Average Delivery Time: %.1f minutes", knapsackAverageTime / 60));
		knapsackAverage.setFont(Font.font("Serif", 18));
		knapsackAverage.setFill(Color.BLACK);
		knapsackAverage.setWrappingWidth(300);
		knapsackAverage.setTextAlignment(TextAlignment.CENTER);

		double knapsackWorstTime = results.getWorstKnapsackTime();	//change once you can access the results

		Text knapsackWorst = new Text(String.format("Worst Delivery Time: %.1f minutes", knapsackWorstTime / 60));
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
            int time = (int)(Math.floor(fifoTimes.get(index)) / 60);    //get the floor of the order delivery time
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
			int time = (int)(Math.floor(knapsackTimes.get(index)) / 60);    //get the floor of the order delivery time
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
