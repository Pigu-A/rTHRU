package logic;

public class Constants {
	// Game
	public static final double MVMT_SPEED_MULT = 2e-9;
	public static final double DIFFICULTY_INCREASE_RATE = 1e-11;
	public static final double HITBOX_RADIUS = 0.025;
	
	public static final double WALL_START_Z = 100;
	public static final double WALL_DISTANCE_BETWEEN = WALL_START_Z / 5.0;
	public static final double WALL_ADVANCE_RATE = 2e-8;
	public static final double WALL_BEGIN_MIN_SIZE = 4;
	public static final double WALL_BEGIN_WALL_DENSITY = 1.0/3.0;
	public static final double WALL_MAX_COLOR_DENSITY = 0.25;
	public static final double WALL_SPECIAL_WALL_RATE = 1.0/3.0;
	public static final double WALL_MIN_MOVING_SPEED = 0.002;
	public static final double WALL_MAX_MOVING_SPEED = 0.02;
	public static final double WALL_MIN_ROTATING_SPEED = 0.005;
	public static final double WALL_MAX_ROTATING_SPEED = 0.05;
	
	public static final double SCORE_CELL_BASE = 125;
	public static final int SCORE_COLOR_BONUS = 250;
	
	// UI
	public static final double DEFAULT_WIDTH = 1024;
	public static final double DEFAULT_HEIGHT = 768;
	public static final double MIN_WIDTH = 320;
	public static final double MIN_HEIGHT = 240;
	
	public static final double MVMT_INPUT_SIZE = 2.0/3.0;
	public static final double MVMT_INDICATOR_SIZE = 0.4/768;
	
	public static final double FONT_SCORE_SIZE = 48.0/768;
	public static final double FONT_UI_SIZE = 36.0/768;
	public static final double FONT_TITLE_SIZE = 0.25;
}
