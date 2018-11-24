package logic.walls;

import java.util.HashMap;

import javafx.scene.paint.Color;

public enum WallCell {
	SOLID, BLANK, RED, GREEN, BLUE, CYAN, PURPLE;
	
	public static final WallCell[] COLORS = {RED, GREEN, BLUE, CYAN, PURPLE};
	public static final HashMap<WallCell,Color> FILL_COLORS = new HashMap<>();
	static {
		FILL_COLORS.put(SOLID , Color.gray(0.5, 0.9)     );
		FILL_COLORS.put(BLANK , Color.gray(1.0, 0.3)     ); // although it's invisible in-game, its color is used in result screen
		FILL_COLORS.put(RED   , Color.hsb(  0, 1, 1, 0.3));
		FILL_COLORS.put(GREEN , Color.hsb(120, 1, 1, 0.3));
		FILL_COLORS.put(BLUE  , Color.hsb(240, 1, 1, 0.3));
		FILL_COLORS.put(CYAN  , Color.hsb(180, 1, 1, 0.3));
		FILL_COLORS.put(PURPLE, Color.hsb(270, 1, 1, 0.3));
	}
}
