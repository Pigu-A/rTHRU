package util;

import logic.GameState;
import ui.SceneManager;

public class Util {
	
	public static int clamp(int number, int min, int max) {
		return Math.min(Math.max(number, min), max);
	}
	
	public static double clamp(double number, double min, double max) {
		return Math.min(Math.max(number, min), max);
	}
	
	public static Complex wallCoordToCanvasCoord(double x, double y, double z) {
		// assuming (0, 0) is the center
		double s = Math.min(SceneManager.width, SceneManager.height);
		double tx = ((x - GameState.playerX) * 2 / z) * s;
		double ty = ((y - GameState.playerY) * 2 / z) * s;
		return new Complex(tx, ty);
	}
	
	public static double distance2D(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}
	
	/**
	 * Do a % b that always result in a positive number (Euclidean division remainder)
	 */
	public static double pMod(double a, double b) {
		return a - Math.abs(b) * Math.floor(a / Math.abs(b));
	}
	
	public static boolean isPointInsideRect(double pointX, double pointY,
			double rectX, double rectY, double rectW, double rectH) {
		return pointX > rectX && pointY > rectY && pointX < rectX + rectW && pointY < rectY + rectH;
	}
}
