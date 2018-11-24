package input;

import java.util.EnumSet;

import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import ui.SceneManager;

public class MouseState {
	private double x, y;
	private EnumSet<MouseButton> clicked, held;

	public MouseState(Node node) {
		x = SceneManager.width / 2;
		y = SceneManager.height / 2;
		clicked = EnumSet.noneOf(MouseButton.class);
		held = EnumSet.noneOf(MouseButton.class);
		node.setOnMouseMoved((MouseEvent me) -> {
			x = me.getX();
			y = me.getY();
		});
		node.setOnMouseDragged((MouseEvent me) -> {
			x = me.getX();
			y = me.getY();
		});
		node.setOnMousePressed((MouseEvent me) -> {
			held.add(me.getButton());
		});
		node.setOnMouseReleased((MouseEvent me) -> {
			MouseButton t = me.getButton();
			if(held.contains(t)) {
				held.remove(t);
				clicked.add(t);
			}
		});
	}
	
	public void clearClicked() {
		clicked.clear();
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public EnumSet<MouseButton> getClicked() {
		return clicked;
	}

	public EnumSet<MouseButton> getHeld() {
		return held;
	}
}
