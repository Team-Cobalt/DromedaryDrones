package mainapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import food.FoodItem;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import location.Point;

/**
 * Class that runs the simulation
 * @author Isabella Patnode, Rachel Franklin, Brendan Ortmann, and Christian Burns
 *
 *SAVING SIMULATION RESULTS WILL HAVE TO OCCUR AFTER SIMULATION IS RUN????? Yes
 */
public class MainClass extends Application {
	private Stage window; //used for creating gui
	private Scene mainMenu; //main menu page
	private Scene simPage; //running sim page
	private Scene genEditPg; //general settings page
	private Scene foodEditPg; //food item settings page
	private Scene mealEditPg; //meal settings page
	private Scene mapEditPg; //map settings page
	private StackPane root;
	private Text title; //title of page
	private HBox titleLayout; //layout regarding title of page
	private HBox iconLayout; //layout of home icon
	private VBox btnLayout; //layout of setting's menu btns
	private VBox settingLayout; //layout of all icons in setting pages
	private Simulation currentSim;
	
	
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
		//takes user to intermediate page when pressed/starts simulation
		btnStart.setOnAction(e-> startSim());
		
		//button for editing the simulation
		Button btnEdit = new Button("Settings");
		btnEdit.setMinWidth(buttons.getPrefWidth());
		//takes user to general settings page when clicked
		btnEdit.setOnAction(e -> genEditPage());
		
		//button for exiting the gui
		Button btnExit = new Button("Exit Simulation");
		btnExit.setMinWidth(buttons.getPrefWidth());
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
		
		mainMenu = new Scene(root, 800, 600);
		
		//sets starting window to the main menu
		window.setScene(mainMenu);
		window.sizeToScene();
		window.centerOnScreen();
		window.setTitle("Dromedary Drones");
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
		
		//TODO: Set camel to rotate!
		
		//button that allows user to cancel the sim
		Button cancelBtn = new Button("Cancel Simulation");
		cancelBtn.setStyle("-fx-font-size: 14");
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
		
		simPage = new Scene(root, 800, 600);
		
		//sets screen to display page
		window.setScene(simPage);
		
		//TODO: run simulation??
		
		//TODO: change window to results page
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
		genBtn.setOnAction(e -> genEditPage());

		Button foodBtn = new Button("Food Items");
		foodBtn.setMinWidth(btnLayout.getPrefWidth());
		foodBtn.setOnAction(e -> editFoodPage());

		Button mealBtn = new Button("Meals");
		mealBtn.setMinWidth(btnLayout.getPrefWidth());
		mealBtn.setOnAction(e -> editMealsPage());

		Button mapBtn = new Button("Map");
		mapBtn.setMinWidth(btnLayout.getPrefWidth());
		mapBtn.setOnAction(e -> editMapPage());

		Button startBtn = new Button("Start Simulation");
		startBtn.setMinWidth(btnLayout.getPrefWidth());
		startBtn.setOnAction(e -> startSim());

		btnLayout.getChildren().addAll(genBtn, foodBtn, mealBtn, mapBtn, startBtn);
		btnLayout.setAlignment(Pos.CENTER_LEFT);
		btnLayout.setStyle("-fx-background-color: WHITE");
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
		topLayout.setSpacing(125);
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
		Text hourTwo = new Text("Hour 2: ");
		Text hourThree = new Text("Hour 3: ");
		Text hourFour = new Text("Hour 4: ");

		//adds current simulation's stochastic flow values to the gridpane
		ArrayList<Integer> currentModel = new ArrayList<>(currentSim.getStochasticFlow());

		TextField hrOneMeals = new TextField(currentModel.get(0).toString());
		TextField hrTwoMeals = new TextField(currentModel.get(1).toString());
		TextField hrThreeMeals = new TextField(currentModel.get(2).toString());
		TextField hrFourMeals = new TextField(currentModel.get(3).toString());

		//creates gridpane for stochastic flow values
		GridPane genSettings = new GridPane();
		genSettings.setAlignment(Pos.CENTER);
		genSettings.setVgap(5);
		genSettings.setMinSize(50, 100);

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
		centerLayout.setSpacing(200);
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

		Button loadBtn = new Button("Load Model");
		loadBtn.setMinWidth(svLdBtns.getPrefWidth());

		svLdBtns.getChildren().addAll(loadBtn, saveBtn);

		//arranges top layout (home btn and title) and center layout (menu btn and gridpane)
		settingLayout = new VBox();
		settingLayout.setSpacing(105);
		settingLayout.setStyle("-fx-background-color: WHITE");
		settingLayout.getChildren().addAll(topLayout, centerLayout, svLdBtns);

		root = new StackPane();
		root.getChildren().add(settingLayout);

		genEditPg = new Scene(root, 800, 600);

		//sets screen to display page
		window.setScene(genEditPg);
	}

	/**
	 * Creates GUI page for food items settings
	 */
	public void editFoodPage() {
		//creates heading of page
		settingTitle();

		//sets up home button icon
		homeBtn();

		//configures display of home button and page title
		HBox topLayout = new HBox();
		topLayout.setSpacing(125);
		topLayout.setAlignment(Pos.TOP_LEFT);
		topLayout.getChildren().addAll(iconLayout, titleLayout);

		//sets up menu buttons
		menuBtns();

		//TODO: complete food items settings GUI page

		//create table of food items in simulation
		TableView foodTable = new TableView();
		ObservableList<FoodItem> foodItems = (ObservableList<FoodItem>) currentSim.getFoodItems();
		foodTable.setItems(foodItems);

		//Create table headings
		TableColumn itemHeading = new TableColumn("Food Item");
		itemHeading.setCellValueFactory(new PropertyValueFactory<Point, String>("name"));
		TableColumn weightHeading = new TableColumn("Weight (oz)");
		weightHeading.setCellValueFactory(new PropertyValueFactory<Point, String>("weight"));

		foodTable.getColumns().setAll(itemHeading, weightHeading);
		foodTable.setPrefWidth(275);
		foodTable.setPrefHeight(250);

		//TODO: make add and delete buttons for table

		HBox centerLayout = new HBox();
		centerLayout.setSpacing(40);
		centerLayout.setAlignment(Pos.CENTER);	//it won't freaking go in the middle-middle
		centerLayout.getChildren().addAll(btnLayout, foodTable);

		//arranges btns for loading and saving model
		VBox svLdBtns = new VBox();
		svLdBtns.setPrefWidth(100);
		svLdBtns.setSpacing(10);
		svLdBtns.setAlignment(Pos.BOTTOM_RIGHT);
		svLdBtns.setPadding(new Insets(0, 10, 10, 0));

		//adds buttons for loading and saving model
		Button saveBtn = new Button("Save Changes");
		saveBtn.setMinWidth(svLdBtns.getPrefWidth());

		Button loadBtn = new Button("Load Model");
		loadBtn.setMinWidth(svLdBtns.getPrefWidth());

		svLdBtns.getChildren().addAll(loadBtn, saveBtn);

		//arranges all elements of the page on the screen
		settingLayout = new VBox(35);
		settingLayout.getChildren().addAll(topLayout, btnLayout, svLdBtns, centerLayout);
		settingLayout.setStyle("-fx-background-color: WHITE");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		foodEditPg = new Scene(root, 800, 600);

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
		topLayout.setSpacing(125);
		topLayout.setAlignment(Pos.TOP_LEFT);
		topLayout.getChildren().addAll(iconLayout, titleLayout);

		//sets up menu buttons
		menuBtns();

		//TODO: complete meals settings GUI page


		//arranges all elements of the page on the screen
		settingLayout = new VBox(35);
		settingLayout.getChildren().addAll(topLayout, btnLayout);
		settingLayout.setStyle("-fx-background-color: WHITE");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		mealEditPg = new Scene(root, 800, 600);

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
		topLayout.setSpacing(125);
		topLayout.setAlignment(Pos.TOP_LEFT);
		topLayout.getChildren().addAll(iconLayout, titleLayout);

		//sets up menu buttons
		menuBtns();

		ObservableList<Point> mapPoints = currentSim.getDeliveryPoints().getPoints();

		//creates the axes for the map scatter plot
		NumberAxis xAxis = new NumberAxis(-2000, 1000, 100);
		xAxis.setLabel("");
		xAxis.setTickMarkVisible(false);
		NumberAxis yAxis = new NumberAxis(-2000, 2000, 100);
		yAxis.setLabel("");
		yAxis.setTickMarkVisible(false);

		ScatterChart map = new ScatterChart(xAxis, yAxis);
		map.setHorizontalGridLinesVisible(false);
		map.setVerticalGridLinesVisible(false);
		map.setLegendVisible(false);

		XYChart.Series mapValues = new XYChart.Series();

		for(int index = 0; index < mapPoints.size(); index++) {
			mapValues.getData().add(new XYChart.Data(mapPoints.get(index).getX(), mapPoints.get(index).getY()));
		}

		map.getData().add(mapValues);

		StackPane mapLayout = new StackPane();
		mapLayout.setMaxSize(300, 250);
		mapLayout.getChildren().add(map);

		//creates table of current simulation's points
		TableView mapTable = new TableView();

		mapTable.setItems(mapPoints);

		TableColumn pointHeading = new TableColumn("Drop-Off Point");
		pointHeading.setCellValueFactory(new PropertyValueFactory<Point, String>("name"));
		TableColumn xyHeading = new TableColumn("(x,y)");
		xyHeading.setCellValueFactory(new PropertyValueFactory<Point, String>("coordinates"));


		mapTable.getColumns().setAll(pointHeading, xyHeading);
		mapTable.setPrefWidth(275);
		mapTable.setPrefHeight(250);

		//TODO: make add and delete buttons for table

		HBox centerLayout = new HBox();
		centerLayout.setSpacing(40);
		centerLayout.setAlignment(Pos.CENTER_LEFT);
		centerLayout.getChildren().addAll(btnLayout, mapLayout, mapTable);

		//arranges btns for loading and saving model
		VBox svLdBtns = new VBox();
		svLdBtns.setPrefWidth(100);
		svLdBtns.setSpacing(10);
		svLdBtns.setAlignment(Pos.BOTTOM_RIGHT);
		svLdBtns.setPadding(new Insets(0, 10, 10, 0));

		//adds buttons for loading and saving model
		Button saveBtn = new Button("Save Changes");
		saveBtn.setMinWidth(svLdBtns.getPrefWidth());

		Button loadBtn = new Button("Load Model");
		loadBtn.setMinWidth(svLdBtns.getPrefWidth());

		svLdBtns.getChildren().addAll(loadBtn, saveBtn);

		//arranges all elements of the page on the screen
		settingLayout = new VBox(52);
		settingLayout.getChildren().addAll(topLayout, centerLayout, svLdBtns);
		settingLayout.setStyle("-fx-background-color: WHITE");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		mapEditPg = new Scene(root, 800, 600);

		//sets screen to display page
		window.setScene(mapEditPg);
	}

}
