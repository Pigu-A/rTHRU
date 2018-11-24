package ui;

import java.util.Comparator;
import java.util.TreeSet;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import logic.GameState;
import logic.walls.WallCell;
import util.ScoreAnimator;
import util.Util;

public class ResultCanvas extends SceneManagedCanvas {
	private ScoreAnimator score;
	private int curPart;
	private long animCounter;
	private long soundCooldown;
	private TreeSet<HistoryLine> historyRenderList;
	private double screenSize;
	private int curHistoryIndex;
	private int historySize;
	private double historyDrawingRate;
	private boolean retryClicked;

	public ResultCanvas(double width, double height) {
		super(width, height);
		score = new ScoreAnimator(0, 12);
		score.setTarget(GameState.score);
		curPart = 0;
		animCounter = 0;
		soundCooldown = -1;
		Comparator<HistoryLine> cmp = Comparator.comparing(HistoryLine::getXPos);
		historyRenderList = new TreeSet<>(cmp);
		curHistoryIndex = 0;
		historySize = GameState.history.size();
		historyDrawingRate = 3e9 / Math.max(historySize, 30);
	}
	
	private class HistoryLine {
		private double xPos;
		private Color fillColor;
		private Color lineColor;
		
		public HistoryLine(double xPos, WallCell cell) {
			this.xPos = xPos;
			Color c = WallCell.FILL_COLORS.get(cell);
			this.fillColor = c;
			this.lineColor = Color.hsb(c.getHue(), c.getSaturation() / 2, c.getBrightness());
		}
		
		public void render(GraphicsContext gc) {
			double tX = (xPos - 0.5) * 0.8 * SceneManager.width;
			gc.setFill(fillColor);
			gc.setStroke(lineColor);
			gc.beginPath();
			gc.lineTo(tX - 0.02 * screenSize, -0.425 * screenSize);
			gc.lineTo(tX + 0.02 * screenSize, -0.415 * screenSize);
			gc.lineTo(tX + 0.02 * screenSize, -0.335 * screenSize);
			gc.lineTo(tX - 0.02 * screenSize, -0.325 * screenSize);
			gc.closePath();
			gc.fill();
			gc.strokeLine(tX - 0.02 * screenSize, -0.425 * screenSize,
					tX - 0.02 * screenSize, -0.325 * screenSize);
		}
		
		private double getXPos() {
			return xPos;
		}
	}

	@Override
	public void doAnimation(long deltaT) {
		drawResult(deltaT);
		GraphicsContext gc = getGraphicsContext2D();
		gc.save();
		switch(curPart) {
		case 0: // fade in
			gc.setFill(Color.gray(1.0, 1 - animCounter / 2e9));
			gc.fillRect(0, 0, SceneManager.width, SceneManager.height);
			animCounter += deltaT;
			if(animCounter > 2000000000) {
				animCounter = 0;
				curPart = 1;
			}
			break;
		case 1: // passed walls
			score.animate(deltaT * 4e-9);
			while(curHistoryIndex < historySize && curHistoryIndex * historyDrawingRate < animCounter) {
				historyRenderList.add(new HistoryLine(curHistoryIndex / (historySize - 1.0),
						GameState.history.get(curHistoryIndex)));
				curHistoryIndex++;
				if(soundCooldown < 0) {
					Resource.sndBlankPassed.play();
					soundCooldown = 33333333;
				}
			}
			animCounter += deltaT;
			soundCooldown -= deltaT;
			if(animCounter > 3000000000L) {
				score.setCurrent(GameState.score);
				animCounter = 0;
				curPart = 2;
			}
			break;
		case 2: // wait for mouse click
			double mX = (mouse.getX() - SceneManager.width / 2) / screenSize;
			double mY = (mouse.getY() - SceneManager.height / 2) / screenSize;
			if(Util.isPointInsideRect(mX, mY, -0.25, 0.21, 0.1, 0.05)) { // retry button
				if(mouse.getClicked().contains(MouseButton.PRIMARY)) {
					retryClicked = true;
					curPart = 3;
					setCursor(Cursor.DEFAULT);
					Resource.sndTransition.play();
				}
				else setCursor(Cursor.HAND);
			}
			else if(Util.isPointInsideRect(mX, mY, 0.08, 0.21, 0.24, 0.05)) { // back to title button
				if(mouse.getClicked().contains(MouseButton.PRIMARY)) {
					retryClicked = false;
					curPart = 3;
					setCursor(Cursor.DEFAULT);
					Resource.sndTransition.play();
				}
				else setCursor(Cursor.HAND);
			}
			else setCursor(Cursor.DEFAULT);
			break;
		case 3: // fade out
			gc.setFill(Color.gray(1.0, animCounter / 2e9));
			gc.fillRect(0, 0, SceneManager.width, SceneManager.height);
			animCounter += deltaT;
			if(animCounter > 2000000000) {
				if(retryClicked) SceneManager.setCanvas(CanvasID.GAME);
				else SceneManager.setCanvas(CanvasID.TITLE);
			}
			break;
		}
		gc.restore();
	}

	private void drawResult(long deltaT) {
		// left click to skip all animations
		if(mouse.getClicked().contains(MouseButton.PRIMARY) && curPart < 2) {
			while(curHistoryIndex < historySize) {
				historyRenderList.add(new HistoryLine(curHistoryIndex / (historySize - 1.0),
						GameState.history.get(curHistoryIndex)));
				curHistoryIndex++;
			}
			score.setCurrent(GameState.score);
			animCounter = 0;
			curPart = 2;
		}
		
		GraphicsContext gc = getGraphicsContext2D();
		double sW = SceneManager.width;
		double sH = SceneManager.height;
		screenSize = Math.min(sW, sH);
		
		gc.save();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, sW, sH);
		
		gc.translate(sW / 2, sH / 2);
		gc.setLineWidth(screenSize * 2.0/768.0);
		for(HistoryLine i : historyRenderList) i.render(gc);
		gc.setFont(Resource.fntScore);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFill(Color.WHITE);
		gc.fillText(String.format("%07d", score.getCurrent()), 0, -0.2 * screenSize);
		gc.fillText(String.format("%04d", curHistoryIndex), 0, 0);
		gc.setFont(Resource.fntUi);
		gc.fillText("total score", 0, -0.15 * screenSize);
		gc.fillText("walls passed", 0, 0.05 * screenSize);
		gc.fillText("retry", -0.2 * screenSize, 0.25 * screenSize);
		gc.fillText("back to title", 0.2 * screenSize, 0.25 * screenSize);
		gc.restore();
	}

}
