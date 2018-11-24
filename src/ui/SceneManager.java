package ui;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.Constants;
import logic.GameState;

public class SceneManager {
	private static Stage curStage;
	private static VBox curPane;
	private static SceneManagedCanvas curCanvas;
	public static double width = 1024;
	public static double height = 768;
	public static boolean isOpen;
	public static String gameSeed = "";
	
	public static void init(Stage stage) {
		isOpen = true;
		curStage = stage;
		curStage.setMinWidth(Constants.MIN_WIDTH);
		curStage.setMinHeight(Constants.MIN_HEIGHT);
		curPane = new VBox(1);
		SceneManagedCanvas.setRefPane(curPane);
	}
	
	public static void setCanvas(CanvasID id) {
		stopAnimation();
		switch(id) {
		case TITLE:
			curCanvas = new TitleCanvas(width, height);
			break;
		case GAME:
			GameState.init(gameSeed);
			curCanvas = new GameCanvas(width, height);
			GameState.setMouseState(curCanvas.mouse);
			break;
		case RESULT:
			curCanvas = new ResultCanvas(width, height);
			break;
		}
		curPane.getChildren().clear();
		curPane.getChildren().add(curCanvas);
		if(curStage.getScene() == null) curStage.setScene(new Scene(curPane, width, height));
		curCanvas.startAnimation();
	}
	
	public static void stopAnimation() {
		if(curCanvas != null) curCanvas.stopAnimation();
	}
	
	public static void closeProgram() {
		isOpen = false;
		stopAnimation();
		curStage.close();
	}
}


