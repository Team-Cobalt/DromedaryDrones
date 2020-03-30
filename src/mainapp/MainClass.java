package mainapp;

import java.io.File;
import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

	@Override
	public void start(Stage primaryStage) throws Exception {
		window = primaryStage;
		
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
		Image simImg = new Image("DrCameltine.jpg");
		
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
	 */
	public void settingTitle() {
		//adds title to general settings page
		title = new Text("Simulation Settings");
		title.setFont(Font.font("Serif", 30));
		title.setFill(Color.BLACK);
		title.setWrappingWidth(400);
		title.setTextAlignment(TextAlignment.CENTER);

		titleLayout = new HBox(5);
		titleLayout.getChildren().add(title);
		titleLayout.setAlignment(Pos.BASELINE_CENTER);
	}

	/**
	 * Creates home button icon
	 */
	public void homeBtn() {
		Image homeIcon = new Image("HomeButton.jpg");

		ImageView homeView = new ImageView(homeIcon);

		Button homeBtn = new Button("", homeView);
		homeBtn.setStyle("-fx-background-color: WHITE");
		homeBtn.setOnAction(e-> window.setScene(mainMenu));

		iconLayout = new HBox(5);
		iconLayout.setAlignment(Pos.TOP_LEFT);
		iconLayout.getChildren().add(homeBtn);
		iconLayout.setStyle("-fx-background-color: WHITE");
	}

	/**
	 * Creates menu buttons for settings pages
	 */
	public void menuBtns() {
		btnLayout = new VBox(15);
		btnLayout.setPrefWidth(100);

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

	}

	/**
	 * Creates GUI page for general settings (i.e. stochastic flow)
	 */
	public void genEditPage() {
		//creates heading of page
		settingTitle();

		//sets up home button icon
		homeBtn();

		//sets up menu buttons
		menuBtns();

		Text hourOne = new Text("Hour 1: ");
		Text hourTwo = new Text("Hour 2: ");
		Text hourThree = new Text("Hour 3: ");
		Text hourFour = new Text("Hour 4: ");

		TextField hrOneMeals = new TextField();
		TextField hrTwoMeals = new TextField();
		TextField hrThreeMeals = new TextField();
		TextField hrFourMeals = new TextField();

		//TODO: complete general simulation GUI page

		GridPane genSettings = new GridPane();
		genSettings.setAlignment(Pos.CENTER);

		genSettings.add(hourOne, 0, 0);
		genSettings.add(hrOneMeals, 1, 0);
		genSettings.add(hourTwo, 0, 1);
		genSettings.add(hrTwoMeals, 1, 1);
		genSettings.add(hourThree, 0, 2);
		genSettings.add(hrThreeMeals, 1, 2);
		genSettings.add(hourFour, 0, 3);
		genSettings.add(hrFourMeals, 1, 3);


		//arranges all elements of the page on the screen
		settingLayout = new VBox();
		settingLayout.setStyle("-fx-background-color: WHITE");

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

		//sets up menu buttons
		menuBtns();

		//TODO: complete food items settings GUI page

		//arranges all elements of the page on the screen
		settingLayout = new VBox(35);
		settingLayout.getChildren().addAll(iconLayout, titleLayout, btnLayout);
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

		//sets up menu buttons
		menuBtns();

		//TODO: complete meals settings GUI page

		//arranges all elements of the page on the screen
		settingLayout = new VBox(35);
		settingLayout.getChildren().addAll(iconLayout, titleLayout, btnLayout);
		settingLayout.setStyle("-fx-background-color: WHITE");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		mealEditPg = new Scene(root, 800, 600);

		//sets screen to display page
		window.setScene(mealEditPg);
	}

	/**
	 * Creates GUI page for map settings
	 */
	public void editMapPage() {
		//creates heading of page
		settingTitle();

		//sets up home button icon
		homeBtn();

		//sets up menu buttons
		menuBtns();

		//TODO: complete map GUI page

		//arranges all elements of the page on the screen
		settingLayout = new VBox(35);
		settingLayout.getChildren().addAll(iconLayout, titleLayout, btnLayout);
		settingLayout.setStyle("-fx-background-color: WHITE");

		root = new StackPane();
		root.getChildren().add(settingLayout);

		mapEditPg = new Scene(root, 800, 600);

		//sets screen to display page
		window.setScene(mapEditPg);
	}

}
