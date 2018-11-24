package ui;

import java.util.Iterator;

import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.text.TextAlignment;
import logic.Constants;
import logic.GameState;
import logic.walls.Wall;
import util.Complex;
import util.ScoreAnimator;
import util.SquareBound;
import util.Util;

public class GameCanvas extends SceneManagedCanvas {
	private double hue = 0;
	private double borderFlash = 0;
	private double lastCursorX;
	private double lastCursorY;
	private int refScore = 0;
	private ScoreAnimator displayScore;
	private long fadeCounter = 0;
	private int curGamePart;
	
	// used in part 2 animation
	private long shakeCounter;
	private double baseX;
	private double baseY;
	private double destX;
	private double destY;

	public GameCanvas(double width, double height) {
		super(width, height);
		setCursor(Cursor.NONE);
		lastCursorX = Double.NaN;
		lastCursorY = Double.NaN;
		destX = Double.NaN;
		destY = Double.NaN;
		curGamePart = 0;
		displayScore = new ScoreAnimator(0, 10);
	}
	
	public Color getBackgroundColor() {
		return Color.hsb(hue, 0.1, 1);
	}
	
	private void drawDepthGuide(GraphicsContext gc, int x, int y) {
		Complex dg = new Complex(GameState.playerX - x, GameState.playerY - y);
		Complex dc = Util.wallCoordToCanvasCoord(x, y, 1);
		if(dg.abs() > 0) {
			double mb = new Complex(SceneManager.width, SceneManager.height).abs();
			dg = Complex.fromPolar(mb, dg.arg() + Math.PI);
			gc.setStroke(RadialGradient.valueOf(String.format("center 0px 0px, radius %fpx, transparent %f%%, black 100%%",
					dc.abs() / 2, 200 / Constants.WALL_START_Z)));
			gc.strokeLine(0, 0, dg.re, dg.im);
		}
	}

	@Override
	public void doAnimation(long deltaT) {
		try {
			drawGame(deltaT);
			GraphicsContext gc = getGraphicsContext2D();
			gc.save();
			switch(curGamePart) {
			case 0: // fade in
				gc.setFill(Color.gray(1.0, 1 - fadeCounter / 2e9));
				gc.fillRect(0, 0, SceneManager.width, SceneManager.height);
				fadeCounter += deltaT;
				if(fadeCounter > 2000000000) {
					// don't let 2 seconds of fade add up to deltaT in the next frame
					GameState.refreshLastStateUpdate();
					fadeCounter = 0;
					curGamePart = 1;
				}
				break;
			case 1: // main game
				GameState.stateUpdateRequested = true;
				break;
			case 2: // solid wall collided (rip)
				if(shakeCounter >= 25000000) {
					if(!Double.isNaN(destX)) GameState.playerX = destX;
					if(!Double.isNaN(destY)) GameState.playerY = destY;
					Complex shake = Complex.fromPolar(Math.max((1e9 - fadeCounter) / 2e10, 0), Math.random() * Math.PI * 2);
					destX = baseX + shake.re;
					destY = baseY + shake.im;
					shakeCounter = shakeCounter % 25000000;
				}
				GameState.playerX += (destX - GameState.playerX) * deltaT / 2.5e7;
				GameState.playerY += (destY - GameState.playerY) * deltaT / 2.5e7;
				fadeCounter += deltaT;
				shakeCounter += deltaT;
				if(fadeCounter > 2000000000L) {
					fadeCounter = 0;
					curGamePart = 3;
				}
				break;
			case 3: // fade out
				gc.setFill(Color.gray(1.0, fadeCounter / 2e9));
				gc.fillRect(0, 0, SceneManager.width, SceneManager.height);
				fadeCounter += deltaT;
				if(fadeCounter > 2000000000) SceneManager.setCanvas(CanvasID.RESULT);
				break;
			}
			gc.restore();
		} catch (Exception e) {
			e.printStackTrace();
			GameState.stateBusy = false;
		}
	}
	
	private void drawGame(long deltaT) {
		// Wait until game state finishes updating variables
		try {
			while(GameState.stateBusy) Thread.sleep(1);
			GameState.stateBusy = true;
		} catch (InterruptedException e) {}
		
		// If the solid wall is collided, change the part to 2
		if(GameState.solidWallCollided && curGamePart == 1) {
			GameState.stopGameStateUpdate();
			baseX = GameState.playerX;
			baseY = GameState.playerY;
			shakeCounter = 25000000;
			curGamePart = 2;
		}
		
		// Screen size shorthands
		double sW = SceneManager.width;
		double sH = SceneManager.height;

		// Mouse positions
		double mX = GameState.mouseX;
		double mY = GameState.mouseY;
		double mdX = Double.isNaN(lastCursorX) ? 0 : mX - lastCursorX;
		double mdY = Double.isNaN(lastCursorY) ? 0 : mY - lastCursorY;
		SquareBound sq = new SquareBound(sW, sH);
		Complex mvmt = GameState.computedMovement;
		double mvmtSize = mvmt.abs();
		
		// Drawing
		GraphicsContext gc = getGraphicsContext2D();
		gc.save();
		
		// Background
		gc.setFill(getBackgroundColor());
		gc.fillRect(0, 0, sW, sH);
		hue = (hue + deltaT * 360 / 3e9) % 360;
		
		// Move (0, 0) to the center
		gc.translate(sW / 2, sH / 2);
		
		// Depth guides
		gc.setLineWidth(sq.ratioToSize(1.5/768.0));
		drawDepthGuide(gc, 0, 0);
		drawDepthGuide(gc, 1, 0);
		drawDepthGuide(gc, 1, 1);
		drawDepthGuide(gc, 0, 1);
		
		// Walls
		Iterator<Wall> it = GameState.walls.descendingIterator();
		Wall first = null;
		if(it.hasNext()) {
			first = it.next();
			if(first.getzPos() >= 1.0) first.render(gc);
			while(it.hasNext()) it.next().render(gc);
		}
		
		// Movement border
		gc.setFill(Color.rgb(255, 255, 0, Math.sin(borderFlash) * 0.1 + 0.2));
		borderFlash = (borderFlash + deltaT * Math.PI / 5e8) % (Math.PI * 2);
		Complex tlc = Util.wallCoordToCanvasCoord(0, 0, 1);
		Complex brc = Util.wallCoordToCanvasCoord(1, 1, 1);
		double tlcX = tlc.re;
		double tlcY = tlc.im;
		double brcX = brc.re;
		double brcY = brc.im;
		if(tlcX > -sW / 2) // top-left + left
			gc.fillRect(-sW / 2, -sH / 2, tlcX - -sW / 2, Math.min(sH / 2 + brcY, sH));
		if(tlcY > -sH / 2) // top + top-right
			gc.fillRect(Math.max(tlcX, -sW / 2), -sH / 2, Math.min(sW / 2 - tlcX, sW), tlcY - -sH / 2);
		if(brcX < sW / 2) // right + bottom-right
			gc.fillRect(brcX, Math.max(tlcY, -sH / 2), sW / 2 - brcX, sH);
		if(brcY < sH / 2) // bottom-left + bottom
			gc.fillRect(-sW / 2, brcY, Math.min(sW / 2 + brcX, sW), sH / 2 - brcY);
		
		// Player
		gc.setFill(Color.gray(0, 0.25));
		double hb = sq.ratioToSize(Constants.HITBOX_RADIUS * 4);
		gc.fillOval(-hb / 2, -hb / 2, hb, hb);
		
		// Wall behind (if any)
		if(first != null && first.getzPos() < 1.0) first.render(gc);

		// Direction bound
		gc.setStroke(Color.hsb(200, 0.75, 0.3, mvmtSize / 2));
		gc.setLineWidth(sq.ratioToSize(mvmtSize * 2 / 768));
		gc.strokeOval(sq.ratioToSize(-Constants.MVMT_INPUT_SIZE / 2),
				sq.ratioToSize(-Constants.MVMT_INPUT_SIZE / 2),
				sq.ratioToSize(Constants.MVMT_INPUT_SIZE), sq.ratioToSize(Constants.MVMT_INPUT_SIZE));
		
		// Direction indicator
		if(mvmtSize > 0.2) {
			gc.save();
			gc.rotate(Math.toDegrees(mvmt.arg()));
			Resource.drawImage(gc, Resource.imgMvmtIndicator,
					sq.ratioToSize(Constants.MVMT_INPUT_SIZE * mvmtSize / 2), 0,
					sq.ratioToSize(Constants.MVMT_INDICATOR_SIZE * mvmtSize),
					sq.ratioToSize(Constants.MVMT_INDICATOR_SIZE));
			gc.restore();
		}
		
		// Move (0, 0) back to top-left corner for UI stuff
		gc.translate(-sW / 2, -sH / 2);
		
		// Score
		if(refScore != GameState.score) {
			refScore = GameState.score;
			displayScore.setTarget(GameState.score);
		}
		displayScore.animate(deltaT / 5e7);
		gc.setFont(Resource.fntScore);
		gc.setTextBaseline(VPos.TOP);
		gc.setTextAlign(TextAlignment.LEFT);
		gc.setFill(Color.BLACK);
		gc.fillText(String.format("%07d", displayScore.getCurrent()), sq.ratioToSize(0.02), sq.ratioToSize(0.01));
		gc.setTextAlign(TextAlignment.RIGHT);
		gc.fillText(String.format("%04d", GameState.wallsPassed), sW - sq.ratioToSize(0.02), sq.ratioToSize(0.01));
		gc.setFont(Resource.fntUi);
		gc.setTextAlign(TextAlignment.LEFT);
		gc.fillText("score", sq.ratioToSize(0.022), sq.ratioToSize(0.07));
		gc.setTextAlign(TextAlignment.RIGHT);
		gc.fillText("walls", sW - sq.ratioToSize(0.024), sq.ratioToSize(0.07));
		
		// Cursor
		gc.setGlobalBlendMode(BlendMode.DIFFERENCE);
		if(curGamePart == 1) {
			gc.setStroke(Color.hsb(360 - hue, 0.2, 1));
			gc.setLineWidth(3);
			gc.beginPath();
			gc.lineTo(mX, mY);
			gc.stroke();
			gc.setStroke(Color.hsb(360 - hue, 0.2, 0.33));
			gc.lineTo(mX - mdX, mY - mdY);
			gc.stroke();
		}
		
		lastCursorX = mX;
		lastCursorY = mY;
		gc.restore();
		
		GameState.stateBusy = false;
	}
}
