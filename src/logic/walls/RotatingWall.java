package logic.walls;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import logic.Constants;
import ui.GameCanvas;
import ui.SceneManager;
import util.Complex;
import util.SquareBound;
import util.Util;

public class RotatingWall extends Wall {
	private double spinRate;
	private double curAngle;
	private static final double SCORE_MULT = 1.5;

	public RotatingWall(int size, double spinRate, double wallDensity, double colorDensity, double zOffset) {
		super(size, wallDensity, colorDensity, zOffset);
		this.spinRate = spinRate;
	}
	
	@Override
	public void move(double deltaZ) {
		curAngle = Util.pMod(curAngle + spinRate * deltaZ, Math.PI * 2);
		super.move(deltaZ);
	}
	
	@Override
	protected int getCellIndexFromPoint(double x, double y) {
		if(x < 0 || x > 1 || y < 0 || y > 1) return -1; // border
		return Util.clamp((int)(y * size), 0, size - 1) * size +
				Util.clamp((int)(x * size), 0, size - 1);
	}
	
	@Override
	protected boolean isFacingCellSolid(double playerX, double playerY) {
		Complex t = new Complex(playerX - 0.5, playerY - 0.5);
		t = Complex.fromPolar(t.abs(), t.arg() - curAngle);
		return super.isFacingCellSolid(t.re + 0.5, t.im + 0.5);
	}

	@Override
	public WallCell getCollidedCell(double deltaZ, double oldX, double oldY, double newX, double newY) {
		if(zPos > 1 && zPos - deltaZ < 1) {
			double cX = oldX + (newX - oldX) * (zPos - 1) / deltaZ;
			double cY = oldY + (newY - oldY) * (zPos - 1) / deltaZ;
			double dA = spinRate * (zPos - 1);
			curAngle += dA;
			if(isFacingCellSolid(cX, cY)) {
				curAngle -= dA;
				return WallCell.SOLID;
			}
			Complex t = new Complex(cX - 0.5, cY - 0.5);
			t = Complex.fromPolar(t.abs(), t.arg() - curAngle);
			curAngle -= dA;
			return super.getCollidedCell(t.re + 0.5, t.im + 0.5);
		}
		return null;
	}

	@Override
	public void render(GraphicsContext gc) {
		gc.save();
		Complex center = Util.wallCoordToCanvasCoord(0.5, 0.5, zPos);
		gc.translate(center.re, center.im);
		gc.rotate(Math.toDegrees(curAngle));
		double wallSize = new SquareBound(SceneManager.width, SceneManager.height).ratioToSize(2.0 / zPos);
		double borderSize = wallSize * Math.sqrt(2);
		double cellSize = wallSize / size;
		double opacity = Util.clamp((Constants.WALL_START_Z - zPos) / Constants.WALL_DISTANCE_BETWEEN, 0, 1); // simulates distance fog
		Color t = WallCell.FILL_COLORS.get(WallCell.SOLID);
		gc.setFill(Color.hsb(0, redSat, redSat * (1 - t.getBrightness()) + t.getBrightness(), t.getOpacity()));
		gc.fillArc(-borderSize / 2, -borderSize / 2, borderSize, borderSize,  -45, 90, ArcType.CHORD);
		gc.fillArc(-borderSize / 2, -borderSize / 2, borderSize, borderSize,   45, 90, ArcType.CHORD);
		gc.fillArc(-borderSize / 2, -borderSize / 2, borderSize, borderSize,  135, 90, ArcType.CHORD);
		gc.fillArc(-borderSize / 2, -borderSize / 2, borderSize, borderSize, -135, 90, ArcType.CHORD);
		for(int i = 0; i < size * size; i++) {
			WallCell cell = cells[i];
			double cX = -wallSize / 2 + cellSize * (i % size);
			double cY = -wallSize / 2 + cellSize * (i / size);
			if(cell != WallCell.BLANK) { // too lazy to do a proper "is the cell outside the canvas" check
				Color srcCol = WallCell.FILL_COLORS.get(cell);
				Color modCol = cell == WallCell.SOLID && zPos < Constants.WALL_DISTANCE_BETWEEN ?
					// Solid wall gets redder if it's going to hit the player
					Color.hsb(0, redSat, redSat * (1 - srcCol.getBrightness()) + srcCol.getBrightness(), srcCol.getOpacity()) :
					Color.color(srcCol.getRed(), srcCol.getGreen(), srcCol.getBlue(), opacity * srcCol.getOpacity());
				gc.setFill(modCol);
				gc.fillRect(cX, cY, cellSize, cellSize);
			}
		}
		gc.rotate(-Math.toDegrees(curAngle)); // rotate back
		gc.setFill(((GameCanvas) gc.getCanvas()).getBackgroundColor());
		// cover out-of-playfield parts
		gc.fillRect(-wallSize / 2, -borderSize / 2 - 1, wallSize, (borderSize - wallSize) / 2 + 1);
		gc.fillRect(-borderSize / 2 - 1, -wallSize / 2, (borderSize - wallSize) / 2 + 1, wallSize);
		gc.fillRect( wallSize / 2 + 1,   -wallSize / 2, (borderSize - wallSize) / 2 + 1, wallSize);
		gc.fillRect(-wallSize / 2,    wallSize / 2 + 1, wallSize, (borderSize - wallSize) / 2 + 1);
		gc.restore();
	}
	
	@Override
	public double getScoreMult() {
		return SCORE_MULT;
	}
}
