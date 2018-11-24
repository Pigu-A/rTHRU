package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import ui.CanvasID;
import ui.SceneManager;


public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		SceneManager.init(primaryStage);
		SceneManager.setCanvas(CanvasID.TITLE);
		primaryStage.setTitle("->thru");
		if(SceneManager.isOpen) primaryStage.show();
	}
	
	@Override
	public void stop() {
		SceneManager.stopAnimation();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
