package ui;

import input.MouseState;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import logic.GameState;

public abstract class SceneManagedCanvas extends Canvas {
	protected AnimationTimer thread;
	protected MouseState mouse;
	protected static Pane refPane;
	protected boolean resizeOccured;
	private long lastFrameTime;
	
	public SceneManagedCanvas(double width, double height) {
		super(width, height);
		mouse = new MouseState(this);
		Resource.regenFonts();
	}
	
	public static void setRefPane(Pane pane) {
		refPane = pane;
	}
	
	public void startAnimation() {
		lastFrameTime = System.nanoTime();
		thread = new AnimationTimer() {
			@Override
			public void handle(long cur) {
				fitToWindow();
				doAnimation(cur - lastFrameTime);
				mouse.clearClicked();
				lastFrameTime = cur;
			}
		};
		thread.start();
	}
	
	public void stopAnimation() {
		thread.stop();
		GameState.stopGameStateUpdate();
	}
	
	public abstract void doAnimation(long deltaT);
	
	public void fitToWindow() {
		if(refPane != null) {
			if(SceneManager.width == refPane.getWidth() && SceneManager.height == refPane.getHeight())
				resizeOccured = false;
			else {
				SceneManager.width = refPane.getWidth();
				SceneManager.height = refPane.getHeight();
				this.setWidth(SceneManager.width);
				this.setHeight(SceneManager.height);
				resizeOccured = true;
				Resource.regenFonts();
			}
		}
	}
}
