package logic.walls;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import logic.Constants;
import ui.SceneManager;
import util.Complex;
import util.SquareBound;
import util.Util;

public class MovingWall extends Wall {
	private double xSpeed;
	private double ySpeed;
	private double curXOffset;
	private double curYOffset;
	private static final double SCORE_MULT = 1.5;

	public MovingWall(int size, double xSpeed, double ySpeed, double wallDensity, double colorDensity, double zOffset) {
		super(size, wallDensity, colorDensity, zOffset);
		this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
		curXOffset = 0;
		curYOffset = 0;
	}
	
	@Override
	public void move(double deltaZ) {
		curXOffset = Util.pMod(curXOffset + xSpeed * deltaZ, 1);
		curYOffset = Util.pMod(curYOffset + ySpeed * deltaZ, 1);
		super.move(deltaZ);
	}

	@Override
	protected int getCellIndexFromPoint(double x, double y) {
		return (int)(Util.pMod(y, 1) * size) * size + (int)(Util.pMod(x, 1) * size);
	}
	
	@Override
	protected boolean isFacingCellSolid(double playerX, double playerY) {
		double tX = Util.pMod(playerX - curXOffset, 1);
		double tY = Util.pMod(playerY - curYOffset, 1);
		if(getWallFromPoint(tX - Constants.HITBOX_RADIUS, tY) == WallCell.SOLID) return true;
		if(getWallFromPoint(tX, tY - Constants.HITBOX_RADIUS) == WallCell.SOLID) return true;
		if(getWallFromPoint(tX + Constants.HITBOX_RADIUS, tY) == WallCell.SOLID) return true;
		if(getWallFromPoint(tX, tY + Constants.HITBOX_RADIUS) == WallCell.SOLID) return true;
		int iX = (int)(Util.pMod(tX, 1) * size);
		int iY = (int)(Util.pMod(tY, 1) * size);
		double cellTopLeftX = (double) iX / size;
		double cellTopLeftY = (double) iY / size;
		if(Util.distance2D(cellTopLeftX, cellTopLeftY, tX, tY) < Constants.HITBOX_RADIUS
				&& cells[Math.floorMod(iY - 1, size) * size + Math.floorMod(iX - 1, size)] == WallCell.SOLID) return true;
		if(Util.distance2D(cellTopLeftX + 1.0 / size, cellTopLeftY, tX, tY) < Constants.HITBOX_RADIUS
				&& cells[Math.floorMod(iY - 1, size) * size + Math.floorMod(iX + 1, size)] == WallCell.SOLID) return true;
		if(Util.distance2D(cellTopLeftX, cellTopLeftY + 1.0 / size, tX, tY) < Constants.HITBOX_RADIUS
				&& cells[Math.floorMod(iY + 1, size) * size + Math.floorMod(iX - 1, size)] == WallCell.SOLID) return true;
		if(Util.distance2D(cellTopLeftX + 1.0 / size, cellTopLeftY + 1.0 / size, tX, tY) < Constants.HITBOX_RADIUS
				&& cells[Math.floorMod(iY + 1, size) * size + Math.floorMod(iX + 1, size)] == WallCell.SOLID) return true;
		return false;
	}

	@Override
	public WallCell getCollidedCell(double deltaZ, double oldX, double oldY, double newX, double newY) {
		if(zPos > 1 && zPos - deltaZ < 1) {
			double cX = oldX + (newX - oldX) * (zPos - 1) / deltaZ;
			double cY = oldY + (newY - oldY) * (zPos - 1) / deltaZ;
			double dX = xSpeed * (zPos - 1);
			double dY = ySpeed * (zPos - 1);
			if(isFacingCellSolid(cX - dX, cY - dY)) return WallCell.SOLID;
			cX = Util.pMod(cX - curXOffset - dX, 1);
			cY = Util.pMod(cY - curYOffset - dY, 1);
			int iX = (int)(Util.pMod(cX, 1) * size);
			int iY = (int)(Util.pMod(cY, 1) * size);
			if(getWallFromPoint(cX, cY) != WallCell.BLANK) return popCell(iX, iY);
			if(getWallFromPoint(cX - Constants.HITBOX_RADIUS, cY) != WallCell.BLANK) return popCell(Math.floorMod(iX - 1, size), iY);
			if(getWallFromPoint(cX, cY - Constants.HITBOX_RADIUS) != WallCell.BLANK) return popCell(iX, Math.floorMod(iY - 1, size));
			if(getWallFromPoint(cX + Constants.HITBOX_RADIUS, cY) != WallCell.BLANK) return popCell(Math.floorMod(iX + 1, size), iY);
			if(getWallFromPoint(cX, cY + Constants.HITBOX_RADIUS) != WallCell.BLANK) return popCell(iX, Math.floorMod(iY + 1, size));
			double cellTopLeftX = (double) iX / size;
			double cellTopLeftY = (double) iY / size;
			if(Util.distance2D(cellTopLeftX, cellTopLeftY, cX, cY) < Constants.HITBOX_RADIUS
					&& cells[Math.floorMod(iY - 1, size) * size + Math.floorMod(iX - 1, size)] != WallCell.BLANK)
				return popCell(Math.floorMod(iX - 1, size), Math.floorMod(iY - 1, size));
			if(Util.distance2D(cellTopLeftX + 1.0 / size, cellTopLeftY, cX, cY) < Constants.HITBOX_RADIUS
					&& cells[Math.floorMod(iY - 1, size) * size + Math.floorMod(iX + 1, size)] != WallCell.BLANK)
				return popCell(Math.floorMod(iX + 1, size), Math.floorMod(iY - 1, size));
			if(Util.distance2D(cellTopLeftX, cellTopLeftY + 1.0 / size, cX, cY) < Constants.HITBOX_RADIUS
					&& cells[Math.floorMod(iY + 1, size) * size + Math.floorMod(iX - 1, size)] != WallCell.BLANK)
				return popCell(Math.floorMod(iX - 1, size), Math.floorMod(iY + 1, size));
			if(Util.distance2D(cellTopLeftX + 1.0 / size, cellTopLeftY + 1.0 / size, cX, cY) < Constants.HITBOX_RADIUS
					&& cells[Math.floorMod(iY + 1, size) * size + Math.floorMod(iX + 1, size)] != WallCell.BLANK)
				return popCell(Math.floorMod(iX + 1, size), Math.floorMod(iY + 1, size));
			return WallCell.BLANK;
		}
		return null;
	}

	@Override
	public void render(GraphicsContext gc) {
		gc.save();
		Complex corner = Util.wallCoordToCanvasCoord(0, 0, zPos);
		double cellSize = new SquareBound(SceneManager.width, SceneManager.height).ratioToSize(2.0 / zPos / size);
		double opacity = Util.clamp((Constants.WALL_START_Z - zPos) / Constants.WALL_DISTANCE_BETWEEN, 0, 1); // simulates distance fog
		for(int i = 0; i < size * size; i++) {
			WallCell cell = cells[i];
			double iX = (i + curXOffset * size) % size;
			double iY = (Math.floor(i / size) + curYOffset * size) % size;
			double cX = corner.re + cellSize * iX;
			double cY = corner.im + cellSize * iY;
			if(cell != WallCell.BLANK) {
				if(iX + 1 < size && iY + 1 < size) setColorAndFillRect(gc, cell, opacity, cX, cY, cellSize, cellSize);
				else {
					setColorAndFillRect(gc, cell, opacity, cX, cY, Math.min(size - iX, 1) * cellSize, Math.min(size - iY, 1) * cellSize);
					if(iX + 1 > size && iY + 1 > size) {
						setColorAndFillRect(gc, cell, opacity, corner.re, corner.im, (iX + 1 - size) * cellSize, (iY + 1 - size) * cellSize);
						setColorAndFillRect(gc, cell, opacity, corner.re, cY, (iX + 1 - size) * cellSize, Math.min(size - iY, 1) * cellSize);
						setColorAndFillRect(gc, cell, opacity, cX, corner.im, Math.min(size - iX, 1) * cellSize, (iY + 1 - size) * cellSize);
					} else if(iX + 1 > size) setColorAndFillRect(gc, cell, opacity, corner.re, cY, (iX + 1 - size) * cellSize, cellSize);
					else setColorAndFillRect(gc, cell, opacity, cX, corner.im, cellSize, (iY + 1 - size) * cellSize);
				}
			}
		}
		gc.restore();
	}
	
	private void setColorAndFillRect(GraphicsContext gc, WallCell cell, double opacity, double x, double y, double w, double h) {
		double sW = SceneManager.width / 2;
		double sH = SceneManager.height / 2;
		if((x + w) > -sW && x < sW && (y + h) > -sH && y < sH) {
			Color srcCol = WallCell.FILL_COLORS.get(cell);
			Color modCol = cell == WallCell.SOLID && zPos < Constants.WALL_DISTANCE_BETWEEN ?
				// Solid wall gets redder if it's going to hit the player
				Color.hsb(0, redSat, redSat * (1 - srcCol.getBrightness()) + srcCol.getBrightness(), srcCol.getOpacity()) :
				Color.color(srcCol.getRed(), srcCol.getGreen(), srcCol.getBlue(), opacity * srcCol.getOpacity());
			gc.setFill(modCol);
			gc.fillRect(x, y, w, h);
		}
	}
	
	@Override
	public double getScoreMult() {
		return SCORE_MULT;
	}
}
