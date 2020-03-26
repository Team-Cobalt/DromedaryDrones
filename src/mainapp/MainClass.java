package mainapp;

import java.io.File;
import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
		Text simName = new Text("Welcome to Dromedary Drones!");
		simName.setFont(Font.font("Serif", 50));
		simName.setFill(Color.BLACK);
		simName.setWrappingWidth(450);
		simName.setTextAlignment(TextAlignment.CENTER);
		
		HBox simTitle = new HBox(20);
		simTitle.getChildren().add(simName);
		simTitle.setAlignment(Pos.BASELINE_CENTER);
		
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
		firstLayout.getChildren().addAll(simTitle, buttons);
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
		Text simText = new Text("Simulation is Running...");
		simText.setFont(Font.font("Serif", 30));
		simText.setFill(Color.BLACK);
		simText.setWrappingWidth(400);
		simText.setTextAlignment(TextAlignment.CENTER);
		
		HBox text = new HBox(20);
		text.getChildren().add(simText);
		text.setAlignment(Pos.TOP_CENTER);
		
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
		simLayout.getChildren().addAll(text, simPic, simBtns);
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
	
	public void genEditPage() {
		//TODO: complete general simulation gui page
	}
	
	public void editFoodPage() {
		//TODO: complete food items gui page
	}
	
	public void editMealsPage() {
		//TODO: complete meals gui page
	}
	
	public void editMapPage() {
		//TODO: complete map gui page
	}
	
}
