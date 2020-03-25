package mainapp;

import java.io.FileInputStream;

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
		
		//Image image = new Image(new FileInputStream("C:\\Users\\PatnodeIA17\\Pictures\\Camel.png"));
		Image image = new Image("https://cdn.hswstatic.com/gif/how-to-draw-animals-117.jpg");
		
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
		btnExit.setOnAction(e->System.exit(0));
		
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
	
	}
	
	public void editSim() {
		
	}
	
}
