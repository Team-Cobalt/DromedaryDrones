package mainapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import food.FoodItem;
import food.Meal;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import location.Point;

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
	private VBox btnLayout; //layout of setting's menu btns
	private VBox settingLayout; //layout of all icons in setting pages
	private Simulation currentSim; //current simulation being run
	private SimulationResults results;
	
	public static void main(String[] args) {
		
		//grabs simulation settings to be loaded
		File loadedFile = new File("simulations.xml");
		
		//loads specified configuration settings
		Configuration config = Configuration.getInstance();

		try {
			// try loading the file
			config.initialize(loadedFile);
		} catch (FileNotFoundException fnf) {
			try {
				// try loading the default GCC data
				config.initialize(null);
			} catch (Exception e) {
				// something went very wrong
				e.printStackTrace();
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
		currentSim = Configuration.getInstance().getCurrentSimulation();

		//adds camel image to main menu
		Image image = new Image("Camel.jpg");
		
		ImageView imgView = new javafx.scene.image.ImageView(image);
		
		imgView.setX(50);
		imgView.setY(50);
		
		imgView.setFitHeight(175);
		imgView.setFitWidth(175);
		
		imgView.setPreserveRatio(true);
		
		VBox picture = new VBox(15);
		picture.getChildren().add(imgView);
		picture.setAlignment(Pos.TOP_CENTER);
		
		//adds opening heading to main menu
		title = new Text("Welcome to Dromedary Drones!");
		title.setFont(Font.font("Serif", 50));
		title.setFill(Color.BLACK);
		title.setWrappingWidth(450);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox(20);
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.BASELINE_CENTER);
		
		//adds buttons to main menu
		VBox buttons = new VBox(10);
		buttons.setPrefWidth(100);
			
		//button for starting the simulation
		Button btnStart = new Button("Start Simulation");
		btnStart.setMinWidth(buttons.getPrefWidth());
		btnStart.setStyle("-fx-background-color: WHITE");
		btnStart.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		//takes user to intermediate page when pressed/starts simulation
		btnStart.setOnAction(e-> startSim());
		
		//button for editing the simulation
		Button btnEdit = new Button("Settings");
		btnEdit.setMinWidth(buttons.getPrefWidth());
		btnEdit.setStyle("-fx-background-color: WHITE");
		btnEdit.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		//takes user to general settings page when clicked
		btnEdit.setOnAction(e -> genEditPage());
		
		//button for exiting the gui
		Button btnExit = new Button("Exit Simulation");
		btnExit.setMinWidth(buttons.getPrefWidth());
		btnExit.setStyle("-fx-background-color: WHITE");
		btnExit.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		//exits the screen (gui) when clicked
		btnExit.setOnAction(e-> System.exit(0));
		
		buttons.getChildren().addAll(btnStart, btnEdit, btnExit);
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
		menuLayout.setStyle("-fx-background-color: WHITE");
		
		
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
	 * Method for creating page that is displayed while the simulation is running
	 * @author Isabella Patnode
	 */
	public void startSim() {
		
		//Adds necessary text to the display
		title = new Text("Simulation is Running...");
		title.setFont(Font.font("Serif", 30));
		title.setFill(Color.BLACK);
		title.setWrappingWidth(400);
		title.setTextAlignment(TextAlignment.CENTER);
		
		titleLayout = new HBox(20);
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.TOP_CENTER);
		
		//Adds camel image to the display
		Image simImg = new Image("Camel.jpg");
		
		ImageView view = new ImageView(simImg);
		
		view.setX(50);
		view.setY(50);
		
		view.setFitHeight(300);
		view.setFitWidth(300);
		
		view.setPreserveRatio(true);
		
		VBox simPic = new VBox(20);
		simPic.getChildren().add(view);
		simPic.setAlignment(Pos.CENTER);

		//button that allows user to cancel the sim
		Button cancelBtn = new Button("Cancel Simulation");
		cancelBtn.setStyle("-fx-background-color: WHITE; -fx-font-size: 14");
		cancelBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		//takes user back to main menu
		cancelBtn.setOnAction(e-> window.setScene(mainMenu));
		
		//adds button to the display
		HBox simBtns = new HBox(20);
		simBtns.getChildren().add(cancelBtn);
		simBtns.setAlignment(Pos.BOTTOM_CENTER);
		
		//arranges all elements of the page on the screen
		VBox simLayout = new VBox(35);
		simLayout.getChildren().addAll(titleLayout, simPic, simBtns);
		simLayout.setAlignment(Pos.CENTER);
		simLayout.setStyle("-fx-background-color: WHITE");
		
		root = new StackPane();
		root.getChildren().add(simLayout);
		
		Scene simPage = new Scene(root, 900, 600);
		
		//sets screen to display page
		window.setScene(simPage);
		
		//TODO: run simulation??
		results = currentSim.run();
		//run simulation method found in Simulation class
		//have observable list of avg times from each trial so they can be graphed

		//takes simulation to results page
		resultsPage();
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
	public void homeBtn() {
		Image homeIcon = new Image("HomeButton.jpg");

		ImageView homeView = new ImageView(homeIcon);

		Button homeBtn = new Button("", homeView);
		homeBtn.setStyle("-fx-background-color: WHITE");
		homeBtn.setOnAction(e-> window.setScene(mainMenu));

		iconLayout = new HBox();
		iconLayout.setAlignment(Pos.TOP_LEFT);
		iconLayout.getChildren().add(homeBtn);
		iconLayout.setStyle("-fx-background-color: WHITE");
	}

	/**
	 * Creates menu buttons for settings pages
	 * @author Isabella Patnode
	 */
	public void menuBtns() {
		btnLayout = new VBox();
		btnLayout.setPrefWidth(110);
		btnLayout.setSpacing(5);

		Button genBtn = new Button("General");
		genBtn.setMinWidth(btnLayout.getPrefWidth());
		genBtn.setStyle("-fx-background-color: WHITE");
		genBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		genBtn.setOnAction(e -> genEditPage());

		Button foodBtn = new Button("Food Items");
		foodBtn.setMinWidth(btnLayout.getPrefWidth());
		foodBtn.setStyle("-fx-background-color: WHITE");
		foodBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		foodBtn.setOnAction(e -> editFoodPage());

		Button mealBtn = new Button("Meals");
		mealBtn.setMinWidth(btnLayout.getPrefWidth());
		mealBtn.setStyle("-fx-background-color: WHITE");
		mealBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		mealBtn.setOnAction(e -> editMealsPage());

		Button mapBtn = new Button("Map");
		mapBtn.setMinWidth(btnLayout.getPrefWidth());
		mapBtn.setStyle("-fx-background-color: WHITE");
		mapBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		mapBtn.setOnAction(e -> editMapPage());

		Button startBtn = new Button("Start Simulation");
		startBtn.setMinWidth(btnLayout.getPrefWidth());
		startBtn.setStyle("-fx-background-color: WHITE");
		startBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		startBtn.setOnAction(e -> startSim());

		btnLayout.getChildren().addAll(genBtn, foodBtn, mealBtn, mapBtn, startBtn);
		btnLayout.setAlignment(Pos.CENTER_LEFT);
		btnLayout.setPadding(new Insets(0, 0, 0, 5));

	}

	/**
	 * Creates GUI page for general settings (i.e. stochastic flow)
	 * @author Isabella Patnode
	 */
	public void genEditPage() {
		//creates heading of page
		settingTitle();

		//sets up home button icon
		homeBtn();

		//configures display of home button and page title
		HBox topLayout = new HBox();
		topLayout.setSpacing(141);
		topLayout.setAlignment(Pos.TOP_LEFT);
		topLayout.getChildren().addAll(iconLayout, titleLayout);

		//sets up menu buttons
		menuBtns();

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
		ArrayList<Integer> currentModel = new ArrayList<>(currentSim.getStochasticFlow());

		TextField hrOneMeals = new TextField(currentModel.get(0).toString());
		hrOneMeals.setMaxWidth(80);
		TextField hrTwoMeals = new TextField(currentModel.get(1).toString());
		hrTwoMeals.setMaxWidth(80);
		TextField hrThreeMeals = new TextField(currentModel.get(2).toString());
		hrThreeMeals.setMaxWidth(80);
		TextField hrFourMeals = new TextField(currentModel.get(3).toString());
		hrFourMeals.setMaxWidth(80);

		//creates gridpane for stochastic flow values
		GridPane genSettings = new GridPane();
		genSettings.setAlignment(Pos.CENTER);
		genSettings.setVgap(10);
		genSettings.setMaxSize(300, 300);

		//adds cells to gridpane
		genSettings.add(hourOne, 0, 0);
		genSettings.add(hrOneMeals, 1, 0);
		genSettings.add(hourTwo, 0, 1);
		genSettings.add(hrTwoMeals, 1, 1);
		genSettings.add(hourThree, 0, 2);
		genSettings.add(hrThreeMeals, 1, 2);
		genSettings.add(hourFour, 0, 3);
		genSettings.add(hrFourMeals, 1, 3);

		VBox gridLayout = new VBox();
		gridLayout.setSpacing(5);
		gridLayout.setAlignment(Pos.CENTER);
		gridLayout.getChildren().addAll(gridHeading, genSettings);

		//configures display of menu buttons and gridpane
		HBox centerLayout = new HBox();
		centerLayout.setSpacing(220);
		centerLayout.setAlignment(Pos.CENTER_LEFT);
		centerLayout.getChildren().addAll(btnLayout, gridLayout);

		//arranges btns for loading and saving model
		VBox svLdBtns = new VBox();
		svLdBtns.setPrefWidth(100);
		svLdBtns.setSpacing(10);
		svLdBtns.setAlignment(Pos.BOTTOM_RIGHT);
		svLdBtns.setPadding(new Insets(0, 80, 100, 0));

		//adds buttons for loading and saving model
		Button saveBtn = new Button("Save Changes");
		saveBtn.setMinWidth(svLdBtns.getPrefWidth());
		saveBtn.setStyle("-fx-background-color: WHITE");
		saveBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		Button loadBtn = new Button("Load Model");
		loadBtn.setMinWidth(svLdBtns.getPrefWidth());
		loadBtn.setStyle("-fx-background-color: WHITE");
		loadBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		svLdBtns.getChildren().addAll(loadBtn, saveBtn);

		//arranges top layout (home btn and title) and center layout (menu btn and gridpane)
		settingLayout = new VBox();
		settingLayout.setSpacing(105);
		settingLayout.setPadding(new Insets(10, 0 , 0, 0));
		settingLayout.setStyle("-fx-background-color: WHITE");
		settingLayout.getChildren().addAll(topLayout, centerLayout, svLdBtns);

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene genEditPg = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(genEditPg);
	}

	/**
	 * Creates GUI page for food items settings
	 * @author Rachel Franklin and Isabella Patnode
	 */
	public void editFoodPage() {
		//creates heading of page
		settingTitle();

		//sets up home button icon
		homeBtn();

		//configures display of home button and page title
		HBox topLayout = new HBox();
		topLayout.setSpacing(141);
		topLayout.setAlignment(Pos.TOP_LEFT);
		topLayout.getChildren().addAll(iconLayout, titleLayout);

		//sets up menu buttons
		menuBtns();

		//create table of food items in simulation
		TableView<FoodItem> foodTable = new TableView<>();
		ObservableList<FoodItem> foodItems = currentSim.getFoodItems();
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
		//TODO: change background of table?????
		foodTable.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		//arranges table on screen
		StackPane tableLayout = new StackPane();
		tableLayout.setAlignment(Pos.CENTER);
		tableLayout.setMaxSize(300, 300);
		tableLayout.getChildren().add(foodTable);

		//arranges menu buttons and table in the center (vertically)
		HBox centerLayout = new HBox();
		centerLayout.setSpacing(220);
		centerLayout.setAlignment(Pos.CENTER_LEFT);
		centerLayout.getChildren().addAll(btnLayout, tableLayout);

		//buttons for adding and deleting table rows
		Button addBtn = new Button("Add");
		addBtn.setStyle("-fx-background-color: WHITE");
		addBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		Button delBtn = new Button("Delete");
		delBtn.setStyle("-fx-background-color: WHITE");
		delBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		//arranges add and delete buttons relative to each other
		HBox editBtns = new HBox(10);
		editBtns.setAlignment(Pos.TOP_CENTER);
		editBtns.getChildren().addAll(addBtn, delBtn);
		editBtns.setPadding(new Insets(0, 20, 0, 0));

		//arranges btns for loading and saving model
		VBox svLdBtns = new VBox();
		svLdBtns.setPrefWidth(100);
		svLdBtns.setSpacing(10);
		svLdBtns.setAlignment(Pos.BOTTOM_RIGHT);
		svLdBtns.setPadding(new Insets(0, 80, 0, 0));

		//adds buttons for loading and saving food items
		Button saveBtn = new Button("Save Changes");
		saveBtn.setMinWidth(svLdBtns.getPrefWidth());
		saveBtn.setStyle("-fx-background-color: WHITE");
		saveBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		Button loadBtn = new Button("Load Foods");
		loadBtn.setMinWidth(svLdBtns.getPrefWidth());
		loadBtn.setStyle("-fx-background-color: WHITE");
		loadBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		svLdBtns.getChildren().addAll(loadBtn, saveBtn);

		VBox tbBtnLayout = new VBox(10);
		tbBtnLayout.getChildren().addAll(centerLayout, editBtns);

		VBox displayLayout = new VBox(10);
		displayLayout.getChildren().addAll(tbBtnLayout, svLdBtns);

		//arranges all elements of the page on the screen
		settingLayout = new VBox(30);
		settingLayout.getChildren().addAll(topLayout, displayLayout);
		settingLayout.setStyle("-fx-background-color: WHITE");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		 Scene foodEditPg = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(foodEditPg);
	}

	/**
	 * Creates GUI page for meal settings
	 */
	public void editMealsPage() {
		//creates heading of page
		settingTitle();

		//sets up home button icon
		homeBtn();

		//configures display of home button and page title
		HBox topLayout = new HBox();
		topLayout.setSpacing(141);
		topLayout.setAlignment(Pos.TOP_LEFT);
		topLayout.getChildren().addAll(iconLayout, titleLayout);

		//sets up menu buttons
		menuBtns();

		//sets padding of menut buttons to match rest of settings pages
		btnLayout.setAlignment(Pos.TOP_LEFT);
		btnLayout.setPadding(new Insets(68, 0, 0, 5));

		//arranges all meals together
		VBox mealsBox = new VBox(10);

		//creates a gridpane for each meal in the simulation
		for(Meal meal : currentSim.getMealTypes()) {
			//arranges meal with its components
			VBox singleMealLayout = new VBox();

			//sets up name of meal
			Text mealName = new Text(meal.getName());
			mealName.setFont(Font.font("Serif", 15));
			mealName.setFill(Color.BLACK);
			mealName.setWrappingWidth(200);
			mealName.setTextAlignment(TextAlignment.JUSTIFY);
			mealName.setStyle("-fx-font-weight: bold");

			//used to store each food item in the meal and how many of it there is
			HashMap<String, Integer> numPerFood = new HashMap<>();

			//formats each food item and the probability of it
			GridPane mealFoods = new GridPane();
			mealFoods.setAlignment(Pos.CENTER_RIGHT);
			mealFoods.setVgap(1);
			mealFoods.setHgap(5);
			mealFoods.setMaxSize(200, 200);

			//counts the number of each food item in the meal (i.e. 2 burgers, 1, fries, etc.)
			for(FoodItem mealItem: meal.getFoods()) {
				String foodName = mealItem.getName();

				if(numPerFood.containsKey(foodName)) {
					numPerFood.put(foodName, numPerFood.get(foodName) + 1);
				}
				else {
					numPerFood.put(foodName, 1);
				}
			}

			int index = 0;

			/*adds each food item and their corresponding count in meals (i.e. 2 hamburger, 0 fries)
			**to the grid*/
			for(FoodItem food: currentSim.getFoodItems()) {
				String currentFood = food.getName();
				Text foodName = new Text(currentFood + ":");
				foodName.setFont(Font.font("Serif", 15));
				mealFoods.add(foodName, 0, index);

				//TODO: SET UP DELETE BUTTON

				//gets # of the specific food item in the meal
				if(numPerFood.containsKey(currentFood)) {
					TextField foodCount = new TextField(numPerFood.get(currentFood).toString());
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
			Text probName = new Text("Probability:");
			probName.setFont(Font.font("Serif", 15));
			mealFoods.add(probName, 0, index);
			TextField probValue = new TextField(meal.getProbability() + "");
			probValue.setMaxWidth(80);
			mealFoods.add(probValue, 1, index);

			//formats meal components
			singleMealLayout.setSpacing(5);
			singleMealLayout.setAlignment(Pos.CENTER);
			singleMealLayout.getChildren().addAll(mealName, mealFoods);

			//adds meal to layout of all meals
			mealsBox.getChildren().add(singleMealLayout);
		}

		//allows for user to scroll through meals
		ScrollPane mealLayout = new ScrollPane();
		mealLayout.setContent(mealsBox);
		mealLayout.setPrefSize(250, 400);
		mealLayout.setStyle("-fx-background: WHITE");
		mealLayout.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		//formats display of buttons for changing meals
		VBox changeBtns = new VBox();
		changeBtns.setPrefWidth(100);
		changeBtns.setSpacing(10);
		changeBtns.setAlignment(Pos.BOTTOM_RIGHT);

		//adds buttons for adding, loading, and saving meals
		Button addBtn = new Button("Add Meal");
		addBtn.setMinWidth(changeBtns.getPrefWidth());
		addBtn.setStyle("-fx-background-color: WHITE");
		addBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		Button loadBtn = new Button("Load Meals");
		loadBtn.setMinWidth(changeBtns.getPrefWidth());
		loadBtn.setStyle("-fx-background-color: WHITE");
		loadBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		Button saveBtn = new Button("Save Changes");
		saveBtn.setMinWidth(changeBtns.getPrefWidth());
		saveBtn.setStyle("-fx-background-color: WHITE");
		saveBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		changeBtns.getChildren().addAll(addBtn, loadBtn, saveBtn);

		//formats display of meal layout and corresponding buttons
		HBox mealBtnLayout = new HBox(100);
		mealBtnLayout.getChildren().addAll(mealLayout, changeBtns);

		//formats display of menu column and full meal display
		HBox centerLayout = new HBox(200);
		centerLayout.getChildren().addAll(btnLayout, mealBtnLayout);

		//arranges all elements of the page on the screen
		settingLayout = new VBox(35);
		settingLayout.getChildren().addAll(topLayout, centerLayout);
		settingLayout.setStyle("-fx-background-color: WHITE");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene mealEditPg = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(mealEditPg);
	}

	/**
	 * Creates GUI page for map settings
	 * @author Isabella Patnode
	 */
	public void editMapPage() {
		//creates heading of page
		settingTitle();

		//sets up home button icon
		homeBtn();

		//configures display of home button and page title
		HBox topLayout = new HBox();
		topLayout.setSpacing(141);
		topLayout.setAlignment(Pos.TOP_LEFT);
		topLayout.getChildren().addAll(iconLayout, titleLayout);

		//sets up menu buttons
		menuBtns();

		//gets list of current map destinations
		ObservableList<Point> mapPoints = currentSim.getDeliveryPoints().getPoints();

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
		//TODO: Look into this
		ScatterChart<Number, Number> map = new ScatterChart<>(xAxis, yAxis);
		map.setHorizontalGridLinesVisible(false);
		map.setVerticalGridLinesVisible(false);
		map.setLegendVisible(false);
		map.setStyle("-fx-background: WHITE");


		//creates points from destination coordinates for scatter plot
		XYChart.Series<Number, Number> mapValues = new XYChart.Series<>();

		for(Point dest : mapPoints) {
			mapValues.getData().add(new XYChart.Data<>(dest.getX(), dest.getY()));
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

		//creates columns for table
		TableColumn<Point, String> pointHeading = new TableColumn<>("Drop-Off Point");
		pointHeading.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<Point, String> xyHeading = new TableColumn<>("(x,y)");
		xyHeading.setCellValueFactory(new PropertyValueFactory<>("coordinates"));

		//adds column headings to table
		mapTable.getColumns().setAll(pointHeading, xyHeading);
		mapTable.setPrefWidth(275);
		mapTable.setPrefHeight(300);
		mapTable.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

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
		centerLayout.getChildren().addAll(btnLayout, mapLayout);

		//buttons for adding and deleting table rows
		Button addBtn = new Button("Add");
		addBtn.setStyle("-fx-background-color: WHITE");
		addBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		Button delBtn = new Button("Delete");
		delBtn.setStyle("-fx-background-color: WHITE");
		delBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		HBox addDelBtns = new HBox(10);
		addDelBtns.setAlignment(Pos.CENTER_RIGHT);
		addDelBtns.setPadding(new Insets(0, 80, 0, 0));
		addDelBtns.getChildren().addAll(addBtn, delBtn);

		//arranges map, table, and table buttons together
		VBox btnMap = new VBox();
		btnMap.getChildren().addAll(centerLayout, addDelBtns);
		btnMap.setSpacing(10);

		//arranges btns for loading and saving map
		VBox svLdBtns = new VBox();
		svLdBtns.setPrefWidth(100);
		svLdBtns.setSpacing(10);
		svLdBtns.setAlignment(Pos.BOTTOM_RIGHT);
		svLdBtns.setPadding(new Insets(0, 80, 0, 0));

		//adds buttons for loading and saving model
		Button saveBtn = new Button("Save Changes");
		saveBtn.setMinWidth(svLdBtns.getPrefWidth());
		saveBtn.setStyle("-fx-background-color: WHITE");
		saveBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		Button loadBtn = new Button("Load Map");
		loadBtn.setMinWidth(svLdBtns.getPrefWidth());
		loadBtn.setStyle("-fx-background-color: WHITE");
		loadBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		svLdBtns.getChildren().addAll(loadBtn, saveBtn);

		//arranges load/save buttons with map elements
		VBox mainLayout = new VBox();
		mainLayout.getChildren().addAll(btnMap, svLdBtns);
		mainLayout.setSpacing(50);

		//arranges all elements of the page on the screen
		settingLayout = new VBox(30);
		settingLayout.getChildren().addAll(topLayout, mainLayout);
		settingLayout.setStyle("-fx-background-color: WHITE");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		Scene mapEditPg = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(mapEditPg);
	}

	/**
	 * Displays results from simulation
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
		homeBtn();

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

		double fifoAvgTime = results.getAverageFifoTime();	//change once you can access the results

		Text fifoAvg = new Text(String.format("Average Delivery Time: %.1f minutes", fifoAvgTime/60));
		fifoAvg.setFont(Font.font("Serif", 18));
		fifoAvg.setFill(Color.BLACK);
		fifoAvg.setWrappingWidth(300);
		fifoAvg.setTextAlignment(TextAlignment.CENTER);

		double fifoWorstTime = results.getWorstFifoTime();	//change once you can access the results

		Text fifoWorst = new Text(String.format("Worst Delivery Time: %.1f minutes", fifoWorstTime/60));
		fifoWorst.setFont(Font.font("Serif", 18));
		fifoWorst.setFill(Color.BLACK);
		fifoWorst.setWrappingWidth(300);
		fifoWorst.setTextAlignment(TextAlignment.CENTER);

		VBox fifoLayout = new VBox();
		fifoLayout.setSpacing(5);
		fifoLayout.setAlignment(Pos.TOP_CENTER);
		fifoLayout.getChildren().addAll(fifoTitle, fifoAvg, fifoWorst);

		//knapsack stats
		Text knapsackTitle = new Text("Knapsack Packing Delivery");
		knapsackTitle.setFont(Font.font("Serif", FontWeight.BOLD, 18));
		knapsackTitle.setFill(Color.BLACK);
		knapsackTitle.setWrappingWidth(300);
		knapsackTitle.setTextAlignment(TextAlignment.CENTER);

		double knapAvgTime = results.getAverageKnapsackTime();	//change once you can access the results

		Text knapAvg = new Text(String.format("Average Delivery Time: %.1f minutes", knapAvgTime/60));
		knapAvg.setFont(Font.font("Serif", 18));
		knapAvg.setFill(Color.BLACK);
		knapAvg.setWrappingWidth(300);
		knapAvg.setTextAlignment(TextAlignment.CENTER);

		double knapWorstTime = results.getWorstKnapsackTime();	//change once you can access the results

		Text knapWorst = new Text(String.format("Worst Delivery Time: %.1f minutes", knapWorstTime/60));
		knapWorst.setFont(Font.font("Serif", 18));
		knapWorst.setFill(Color.BLACK);
		knapWorst.setWrappingWidth(300);
		knapWorst.setTextAlignment(TextAlignment.CENTER);

		VBox knapsackLayout = new VBox();
		fifoLayout.setSpacing(5);
		fifoLayout.setAlignment(Pos.TOP_CENTER);
		fifoLayout.getChildren().addAll(knapsackTitle, knapAvg, knapWorst);

		//set up stats layout
		HBox statsLayout = new HBox();
		statsLayout.setSpacing(40);
		statsLayout.setAlignment(Pos.CENTER);
		statsLayout.getChildren().addAll(fifoLayout, knapsackLayout);

		//sets up BarChart (histogram)
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
		barChart.setCategoryGap(0.2);
		barChart.setBarGap(0.2);
		xAxis.setLabel("Delivery Time (add units!)");
		yAxis.setLabel("Number of Orders");

		//sets up fifo and knapsack packing series of data
		XYChart.Series<String, Number> fifoSeries = new XYChart.Series<>();
		XYChart.Series<String, Number> knapsackSeries = new XYChart.Series<>();
		fifoSeries.setName("FIFO");
		knapsackSeries.setName("Knapsack Packing");

		//add data from lists
		//fifoSeries.getData().add(new XYChart.Data<>("1-2", 5));
		//knapsackSeries.getData().add(new XYChart.Data<>("1-2", 7));

		//add series data to bar chart
		barChart.getData().add(fifoSeries);
		barChart.getData().add(knapsackSeries);

		//save results button
		Button saveBtn = new Button("Save Results");
		saveBtn.setMinWidth(100);
		saveBtn.setStyle("-fx-background-color: WHITE");
		saveBtn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		saveBtn.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Results");
			fileChooser.getExtensionFilters().add(
					new FileChooser.ExtensionFilter("CSV", "*.csv")
			);
			File file = fileChooser.showSaveDialog(window);
			if (file != null)
			    Configuration.getInstance().saveResults(results, file);
		});

		//combine boxes
		VBox vBox = new VBox();
		vBox.getChildren().addAll(topLayout, statsLayout, barChart, saveBtn);//top layout, stats, barChart, save button

		StackPane root = new StackPane();
		root.getChildren().add(vBox);

		Scene resultsPg = new Scene(root, 900, 600);

		//sets screen to display page
		window.setScene(resultsPg);
	}
}
