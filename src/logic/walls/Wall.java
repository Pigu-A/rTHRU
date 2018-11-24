package logic.walls;

import javafx.scene.canvas.GraphicsContext;
import logic.Constants;
import logic.GameState;
import util.Util;

public abstract class Wall {
	protected double zPos;
	protected int size;
	protected WallCell[] cells;
	protected double redSat; // animated indicator if the player is going to collide with the wall
	private double redSatState;
	private static final double SCORE_MULT = 1;
	
	public Wall(int size, double wallDensity, double colorDensity, double zOffset) {
		this.size = size;
		zPos = Constants.WALL_START_Z + zOffset;
		cells = new WallCell[size * size];
		for(int i = 0; i < size * size; i++) {
			if(GameState.wallGenRng.nextDouble() < wallDensity) cells[i] = WallCell.SOLID;
			else if(GameState.wallGenRng.nextDouble() < colorDensity)
				cells[i] = WallCell.COLORS[GameState.wallGenRng.nextInt(WallCell.COLORS.length)];
			else cells[i] = WallCell.BLANK;
		}
	}
	
	public void move(double deltaZ) {
		zPos -= deltaZ;
		if(zPos < Constants.WALL_DISTANCE_BETWEEN) {
			if(isFacingCellSolid(GameState.playerX, GameState.playerY)) redSatState += deltaZ * 5e-9 / Constants.WALL_ADVANCE_RATE;
			else redSatState -= deltaZ * 1e-8 / Constants.WALL_ADVANCE_RATE;
			redSatState = Util.clamp(redSatState, 0, 1);
			redSat = Util.clamp((Constants.WALL_DISTANCE_BETWEEN - zPos) / (Constants.WALL_DISTANCE_BETWEEN - 1), 0, 1) * redSatState;
		}
	}
	
	public void setCell(int index, WallCell value) {
		cells[index] = value;
	}
	
	public WallCell getCell(int index) {
		return cells[index];
	}
	
	public void forceRedWall() {
		redSatState = 1;
		redSat = 1;
	}
	
	protected WallCell getWallFromPoint(double x, double y) {
		// for RotatingWall, cell index of -1 is a border
		int i = getCellIndexFromPoint(x, y);
		return i >= 0 ? cells[i] : WallCell.SOLID;
	}
	
	/**
	 * Determines if a solid wall will overlap with player's hitbox when the wall's z position is 1.
	 * @param playerX Player's X position in wall coordinate [0,1]
	 * @param playerY Player's Y position in wall coordinate [0,1]
	 */
	protected boolean isFacingCellSolid(double playerX, double playerY) {
		if(getWallFromPoint(playerX - Constants.HITBOX_RADIUS, playerY) == WallCell.SOLID) return true;
		if(getWallFromPoint(playerX, playerY - Constants.HITBOX_RADIUS) == WallCell.SOLID) return true;
		if(getWallFromPoint(playerX + Constants.HITBOX_RADIUS, playerY) == WallCell.SOLID) return true;
		if(getWallFromPoint(playerX, playerY + Constants.HITBOX_RADIUS) == WallCell.SOLID) return true;
		int cellIndex = getCellIndexFromPoint(playerX, playerY);
		double cellTopLeftX = (double)(cellIndex % size) / size;
		double cellTopLeftY = Math.floor(cellIndex / size) / size;
		if(cellTopLeftX > 0 && cellTopLeftY > 0 &&
				Util.distance2D(cellTopLeftX, cellTopLeftY, playerX, playerY) < Constants.HITBOX_RADIUS
				&& cells[cellIndex - size - 1] == WallCell.SOLID) return true;
		if(cellTopLeftX < (double)(size - 1)/size && cellTopLeftY > 0 &&
				Util.distance2D(cellTopLeftX + 1.0 / size, cellTopLeftY, playerX, playerY) < Constants.HITBOX_RADIUS
				&& cells[cellIndex - size + 1] == WallCell.SOLID) return true;
		if(cellTopLeftX > 0 && cellTopLeftY < (double)(size - 1)/size &&
				Util.distance2D(cellTopLeftX, cellTopLeftY + 1.0 / size, playerX, playerY) < Constants.HITBOX_RADIUS
				&& cells[cellIndex + size - 1] == WallCell.SOLID) return true;
		if(cellTopLeftX < (double)(size - 1)/size && cellTopLeftY < (double)(size - 1)/size &&
				Util.distance2D(cellTopLeftX + 1.0 / size, cellTopLeftY + 1.0 / size, playerX, playerY) < Constants.HITBOX_RADIUS
				&& cells[cellIndex + size + 1] == WallCell.SOLID) return true;
		return false;
	}

	public WallCell getCollidedCell(double deltaZ, double oldX, double oldY, double newX, double newY) {
		// if the wall doesn't move past the player's z position of 1, it's guaranteed not to collide
		if(zPos > 1 && zPos - deltaZ < 1) {
			double cX = oldX + (newX - oldX) * (zPos - 1) / deltaZ;
			double cY = oldY + (newY - oldY) * (zPos - 1) / deltaZ;
			// do solid wall check first, if it is, then rip
			if(isFacingCellSolid(cX, cY)) return WallCell.SOLID;
			return getCollidedCell(cX, cY); // continue on another method with transformed position
		}
		return null; // no collision
	}
	
	protected WallCell getCollidedCell(double cX, double cY) {
		int cellIndex = getCellIndexFromPoint(cX, cY);
		int iX = cellIndex % size;
		int iY = cellIndex / size;
		increaseScore(iX, iY);
		if(getWallFromPoint(cX, cY) != WallCell.BLANK) return popCell(iX, iY);
		// in case the hitbox collide with the surrounding solid / colored cells
		// order of precedence is left, top, right, bottom, top-left, top-right, bottom-left, bottom-right
		if(cX > Constants.HITBOX_RADIUS && getWallFromPoint(cX - Constants.HITBOX_RADIUS, cY) != WallCell.BLANK)
			return popCell(iX - 1, iY);
		if(cY > Constants.HITBOX_RADIUS && getWallFromPoint(cX, cY - Constants.HITBOX_RADIUS) != WallCell.BLANK)
			return popCell(iX, iY - 1);
		if(cX + Constants.HITBOX_RADIUS < 1 && getWallFromPoint(cX + Constants.HITBOX_RADIUS, cY) != WallCell.BLANK)
			return popCell(iX + 1, iY);
		if(cY + Constants.HITBOX_RADIUS < 1 && getWallFromPoint(cX, cY + Constants.HITBOX_RADIUS) != WallCell.BLANK)
			return popCell(iX, iY + 1);
		double cellTopLeftX = (double)(cellIndex % size) / size;
		double cellTopLeftY = Math.floor(cellIndex / size) / size;
		if(cellTopLeftX > 0 && cellTopLeftY > 0 &&
				Util.distance2D(cellTopLeftX, cellTopLeftY, cX, cY) < Constants.HITBOX_RADIUS
				&& cells[cellIndex - size - 1] != WallCell.BLANK) return popCell(iX - 1, iY - 1);
		if(cellTopLeftX < (double)(size - 1)/size && cellTopLeftY > 0 &&
				Util.distance2D(cellTopLeftX + 1.0 / size, cellTopLeftY, cX, cY) < Constants.HITBOX_RADIUS
				&& cells[cellIndex - size + 1] != WallCell.BLANK) return popCell(iX + 1, iY - 1);
		if(cellTopLeftX > 0 && cellTopLeftY < (double)(size - 1)/size &&
				Util.distance2D(cellTopLeftX, cellTopLeftY + 1.0 / size, cX, cY) < Constants.HITBOX_RADIUS
				&& cells[cellIndex + size - 1] != WallCell.BLANK) return popCell(iX - 1, iY + 1);
		if(cellTopLeftX < (double)(size - 1)/size && cellTopLeftY < (double)(size - 1)/size &&
				Util.distance2D(cellTopLeftX + 1.0 / size, cellTopLeftY + 1.0 / size, cX, cY) < Constants.HITBOX_RADIUS
				&& cells[cellIndex + size + 1] != WallCell.BLANK) return popCell(iX + 1, iY + 1);
		return WallCell.BLANK;
	}
	
	protected WallCell popCell(int x, int y) {
		WallCell cell = cells[y * size + x];
		cells[y * size + x] = WallCell.BLANK;
		return cell;
	}
	
	/**
	 * Calculate and increase the score get from passing through the wall based on how large the hole is around the cell.
	 * The formula is SCORE_CELL_BASE * SCORE_MULT * size * max(difficulty, 1) / sqrt(hole size)
	 * @param x Cell's x index to get the score
	 * @param y Cell's y index to get the score
	 */
	protected void increaseScore(int x, int y) {
		double t = 1; // center should really not be a solid wall at this point
		if(x > 0 && cells[y * size + x - 1] != WallCell.SOLID) t += 1;
		if(y > 0 && cells[(y - 1) * size + x] != WallCell.SOLID) t += 1;
		if(x < size - 1 && cells[y * size + x + 1] != WallCell.SOLID) t += 1;
		if(y < size - 1 && cells[(y + 1) * size + x] != WallCell.SOLID) t += 1;
		if(x > 0 && y > 0 && cells[(y - 1) * size + x - 1] != WallCell.SOLID) t += 0.25;
		if(x < size - 1 && y > 0 && cells[(y - 1) * size + x + 1] != WallCell.SOLID) t += 0.25;
		if(x > 0 && y < size - 1 && cells[(y + 1) * size + x - 1] != WallCell.SOLID) t += 0.25;
		if(x < size - 1 && y < size - 1 && cells[(y + 1) * size + x + 1] != WallCell.SOLID) t += 0.25;
		int sc = (int)(Constants.SCORE_CELL_BASE * getScoreMult() * size * Math.max(GameState.difficulty, 1) / Math.sqrt(t));
		GameState.increaseScore(sc);
	}
	
	protected abstract int getCellIndexFromPoint(double x, double y);
	public abstract void render(GraphicsContext gc);

	public double getzPos() {
		return zPos;
	}
	
	public double getScoreMult() {
		return SCORE_MULT;
	}
}
