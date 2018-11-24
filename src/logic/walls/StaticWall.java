package logic.walls;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import logic.Constants;
import ui.SceneManager;
import util.Complex;
import util.SquareBound;
import util.Util;

public class StaticWall extends Wall {
	
	public StaticWall(int size, double wallDensity, double colorDensity, double zOffset) {
		super(size, wallDensity, colorDensity, zOffset);
	}

	@Override
	protected int getCellIndexFromPoint(double x, double y) {
		return Util.clamp((int)(y * size), 0, size - 1) * size +
				Util.clamp((int)(x * size), 0, size - 1);
	}

	@Override
	public void render(GraphicsContext gc) {
		gc.save();
		Complex corner = Util.wallCoordToCanvasCoord(0, 0, zPos);
		double cellSize = new SquareBound(SceneManager.width, SceneManager.height).ratioToSize(2.0 / size / zPos);
		double sW = SceneManager.width / 2;
		double sH = SceneManager.height / 2;
		double opacity = Util.clamp((Constants.WALL_START_Z - zPos) / Constants.WALL_DISTANCE_BETWEEN, 0, 1); // simulates distance fog
		for(int i = 0; i < size * size; i++) {
			WallCell cell = cells[i];
			double cX = corner.re + cellSize * (i % size);
			double cY = corner.im + cellSize * (i / size);
			if(cell != WallCell.BLANK && (cX + cellSize) > -sW && cX < sW && (cY + cellSize) > -sH && cY < sH) {
				Color srcCol = WallCell.FILL_COLORS.get(cell);
				Color modCol = cell == WallCell.SOLID && zPos < Constants.WALL_DISTANCE_BETWEEN ?
					// Solid wall gets redder if it's going to hit the player
					Color.hsb(0, redSat, redSat * (1 - srcCol.getBrightness()) + srcCol.getBrightness(), srcCol.getOpacity()) :
					Color.color(srcCol.getRed(), srcCol.getGreen(), srcCol.getBlue(), opacity * srcCol.getOpacity());
				gc.setFill(modCol);
				gc.fillRect(cX, cY, cellSize, cellSize);
			}
		}
		gc.restore();
	}
}
