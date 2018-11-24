package logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import input.MouseState;
import logic.walls.MovingWall;
import logic.walls.RotatingWall;
import logic.walls.StaticWall;
import logic.walls.Wall;
import logic.walls.WallCell;
import ui.Resource;
import ui.SceneManager;
import util.Complex;
import util.Random;
import util.SquareBound;
import util.Util;

public class GameState {
	public static Random wallGenRng;
	public static ArrayDeque<Wall> walls;
	public static boolean stateBusy;
	public static boolean stateUpdateRequested;
	public static Thread stateUpdate;
	public static double playerX;
	public static double playerY;
	public static double playerOldX;
	public static double playerOldY;
	public static MouseState mouse;
	public static double mouseX;
	public static double mouseY;
	public static Complex computedMovement;
	public static double difficulty;
	public static double speed;
	private static boolean stateUpdateEnabled;
	private static long lastStateUpdate;
	private static double newWallCounter;
	private static double curWallSize;
	private static double curWallDensity;
	private static double curColorDensity;
	private static double curSpecialWallRate;
	
	public static ArrayList<WallCell> history = new ArrayList<>();
	public static int score;
	public static int wallsPassed;
	public static boolean solidWallCollided;
	
	public static void init(String levelSeed) {
		wallGenRng = new Random(levelSeed);
		walls = new ArrayDeque<>();
		playerX = 0.5;
		playerY = 0.5;
		mouseX = SceneManager.width / 2;
		mouseY = SceneManager.height / 2;
		computedMovement = new Complex(0, 0);
		difficulty = 0;
		newWallCounter = Constants.WALL_DISTANCE_BETWEEN;
		curWallSize = Constants.WALL_BEGIN_MIN_SIZE;
		curWallDensity = Constants.WALL_BEGIN_WALL_DENSITY;
		curColorDensity = 0;
		addWall(0);
		history.clear();
		score = 0;
		wallsPassed = 0;
		solidWallCollided = false;
		
		stateBusy = false;
		stateUpdateRequested = false;
		stateUpdateEnabled = true;
		stateUpdate = new Thread(new Runnable() {
			@Override
			public void run() {
				lastStateUpdate = System.nanoTime();
				while(stateUpdateEnabled) {
					try {
						if(stateUpdateRequested) {
							long now = System.nanoTime();
							doGameStateUpdate(now - lastStateUpdate);
							lastStateUpdate = now;
							stateUpdateRequested = false;
						}
						Thread.sleep(1);
					} catch (Exception e) {
						e.printStackTrace();
						stateBusy = false;
					}
				}
			}
		}, "Game State");
		stateUpdate.start();
	}
	
	public static void refreshLastStateUpdate() {
		lastStateUpdate = System.nanoTime();
	}
	
	private static void doGameStateUpdate(long deltaT) throws InterruptedException {
		double sp = Math.max(speed, 1);
		// Mouse and player positions
		double mX = mouse.getX();
		double mY = mouse.getY();
		SquareBound sq = new SquareBound(SceneManager.width, SceneManager.height);
		Complex mvmt = new Complex((mX - SceneManager.width / 2) / sq.size() / Constants.MVMT_INPUT_SIZE * 2,
				(mY - SceneManager.height / 2) / sq.size() / Constants.MVMT_INPUT_SIZE * 2);
		mvmt = Complex.fromPolar(Math.min(mvmt.abs(), 1), mvmt.arg()); // clamp the speed ratio to 1
		//System.out.println(sp);
		double pX = Util.clamp(playerX + mvmt.re * deltaT * sp * Constants.MVMT_SPEED_MULT, 0, 1);
		double pY = Util.clamp(playerY + mvmt.im * deltaT * sp * Constants.MVMT_SPEED_MULT, 0, 1);
		double dZ = deltaT * speed * Constants.WALL_ADVANCE_RATE;
		
		// the thread now really want to access state variables that are shared with animation thread
		while(stateBusy) Thread.sleep(1);
		stateBusy = true;
		mouseX = mX;
		mouseY = mY;
		playerX = pX;
		playerY = pY;
		computedMovement = mvmt;
		difficulty += deltaT * Constants.DIFFICULTY_INCREASE_RATE;
		speed = sp;
		curWallSize = Constants.WALL_BEGIN_MIN_SIZE + Math.min(difficulty, 1.5) * 2 / 1.5; // 4-6 -> 6-8 @ 1.5
		curWallDensity = Constants.WALL_BEGIN_WALL_DENSITY + Math.min(difficulty, 1.5) * (0.5 - Constants.WALL_BEGIN_WALL_DENSITY) / 1.5; // 1/3 -> 0.5 @ 1.5
		curColorDensity = Math.min(difficulty, 1.5) * Constants.WALL_MAX_COLOR_DENSITY / 1.5; // 0 -> 0.25 @ 1.5
		curSpecialWallRate = Constants.WALL_SPECIAL_WALL_RATE * (Math.min(difficulty, 1.5) - 0.5); // -1/6 -> 1/3 @ 1.5
		newWallCounter -= dZ;
		if(newWallCounter < 0) {
			addWall(newWallCounter);
			newWallCounter += Constants.WALL_DISTANCE_BETWEEN;
		}
		Iterator<Wall> it = walls.iterator();
		if(it.hasNext()) {
			Wall first = it.next();
			WallCell coll = first.getCollidedCell(dZ, playerOldX, playerOldY, playerX, playerY);
			if(coll != null) {
				switch(coll) {
				case BLANK:
					wallsPassed++;
					history.add(coll);
					Resource.sndBlankPassed.play();
					break;
				case SOLID:
					solidWallCollided = true;
					computedMovement = new Complex(0, 0);
					dZ = first.getzPos() - 1;
					Resource.sndCollided.play();
					break;
				case RED:
				case BLUE:
				case CYAN:
				case GREEN:
				case PURPLE:
					wallsPassed++;
					score += Constants.SCORE_COLOR_BONUS * Math.max(difficulty, 1);
					history.add(coll);
					Resource.sndColorPassed.play();
					break;
				}
			}
			first.move(dZ);
			double fZ = first.getzPos();
			if(fZ < 0) walls.poll();
			while(it.hasNext()) it.next().move(dZ);
			if(solidWallCollided) first.forceRedWall();
		}
		playerOldX = pX;
		playerOldY = pY;
		stateBusy = false;
	}
	
	public static void stopGameStateUpdate() {
		stateUpdateEnabled = false;
	}
	
	public static void setMouseState(MouseState m) {
		mouse = m;
	}
	
	private static void addWall(double zOffset) {
		int size = (int) Math.floor(wallGenRng.nextRange(curWallSize, curWallSize + 2));
		double wallType = wallGenRng.nextDouble();
		if(wallType > curSpecialWallRate) walls.add(new StaticWall(size, curWallDensity, curColorDensity, zOffset));
		else if(wallType > curSpecialWallRate / 2) {
			Complex t = Complex.fromPolar(wallGenRng.nextRange(Constants.WALL_MIN_MOVING_SPEED, Constants.WALL_MAX_MOVING_SPEED),
					wallGenRng.nextRange(-Math.PI, Math.PI));
			walls.add(new MovingWall(size - 1, t.re, t.im, curWallDensity, curColorDensity, zOffset));
		} else walls.add(new RotatingWall(size - 1,
				wallGenRng.nextRange(Constants.WALL_MIN_ROTATING_SPEED, Constants.WALL_MAX_ROTATING_SPEED),
				curWallDensity, curColorDensity, zOffset));
	}

	public static void increaseScore(int sc) {
		score = Math.max(score + sc, 0);
	}
}
