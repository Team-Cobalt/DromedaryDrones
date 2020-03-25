package mainapp;

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
 */
public class Simulation extends Application {
	private Stage window;
	private Scene mainMenu;
	private Scene simPage;
	private Scene editSim;
	private StackPane root;
	
	
	public static void main(String[] args) {
		launch(args); 
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		window = primaryStage;
	
		Image image = new Image("Camel.jpg");
		
		ImageView imgView = new ImageView(image);
		
		imgView.setX(50);
		imgView.setY(50);
		
		imgView.setFitHeight(175);
		imgView.setFitWidth(175);
		
		imgView.setPreserveRatio(true);
		
		VBox picture = new VBox(15);
		picture.getChildren().add(imgView);
		picture.setAlignment(Pos.TOP_CENTER);
		
		
		Text simName = new Text("Welcome to Dromedary Drones!");
		simName.setFont(Font.font("Serif", 50));
		simName.setFill(Color.BLACK);
		simName.setWrappingWidth(450);
		simName.setTextAlignment(TextAlignment.CENTER);
		
		HBox simTitle = new HBox(20);
		simTitle.getChildren().add(simName);
		simTitle.setAlignment(Pos.BASELINE_CENTER);
		
		VBox buttons = new VBox(10);
		buttons.setPrefWidth(100);
				
		Button btnStart = new Button("Start Simulation");
		btnStart.setMinWidth(buttons.getPrefWidth());
		btnStart.setOnAction(e-> startSim());
		
		Button btnEdit = new Button("Settings");
		btnEdit.setMinWidth(buttons.getPrefWidth());
		btnEdit.setOnAction(e -> editSim());
		
		Button btnExit = new Button("Exit Simulation");
		btnExit.setMinWidth(buttons.getPrefWidth());
		btnExit.setOnAction(e-> System.exit(0));
		
		buttons.getChildren().addAll(btnStart, btnEdit, btnExit);
		buttons.setAlignment(Pos.BOTTOM_CENTER);
		
		VBox firstLayout = new VBox(30);
		firstLayout.getChildren().addAll(simTitle, buttons);
		firstLayout.setAlignment(Pos.CENTER);
		
		VBox menuLayout = new VBox(30);
		menuLayout.getChildren().addAll(picture, firstLayout);
		menuLayout.setSpacing(10);
		menuLayout.setAlignment(Pos.CENTER);
		menuLayout.setStyle("-fx-background-color: WHITE");
		
		
		root = new StackPane();
		root.getChildren().add(menuLayout);
		
		mainMenu = new Scene(root, 800, 600);
		
		window.setScene(mainMenu);
		window.sizeToScene();
		window.centerOnScreen();
		window.setTitle("Dromedary Drones");
		window.show();

	}
	
	public void startSim() {
		Text simText = new Text("Simulation is Running...");
		simText.setFont(Font.font("Serif", 30));
		simText.setFill(Color.BLACK);
		simText.setWrappingWidth(400);
		simText.setTextAlignment(TextAlignment.CENTER);
		
		HBox text = new HBox(20);
		text.getChildren().add(simText);
		text.setAlignment(Pos.TOP_CENTER);
		
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
		
		Button cancelBtn = new Button("Cancel Simulation");
		cancelBtn.setStyle("-fx-font-size: 14");
		cancelBtn.setOnAction(e-> window.setScene(mainMenu));
		
		HBox simBtns = new HBox(20);
		simBtns.getChildren().add(cancelBtn);
		simBtns.setAlignment(Pos.BOTTOM_CENTER);
		
		VBox simLayout = new VBox(40);
		simLayout.getChildren().addAll(text, simPic, simBtns);
		simLayout.setAlignment(Pos.CENTER);
		
		root = new StackPane();
		root.getChildren().add(simLayout);
		
		simPage = new Scene(root, 800, 600);
		
		window.setScene(simPage);
		
		runSimulation();
	}
	
	public void editSim() {
		
	}
	
	public void runSimulation() {
		for (int i = 0; i < 50; i++) {
			//run trial
		}
	}
	
}
