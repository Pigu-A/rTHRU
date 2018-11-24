package ui;

import java.util.ArrayList;
import java.util.Comparator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.text.TextAlignment;
import logic.Constants;
import logic.walls.WallCell;
import util.Complex;
import util.Util;

public class TitleCanvas extends SceneManagedCanvas {
	public long textBlinkTimer;
	public long fadeOutTimer;
	private ArrayList<TitleSquare> squaresRenderList;
	private double newWallCounter;
	private long particleTimer;
	private double screenSize;
	private TextInputDialog seedDlg;
	private String seed;

	public TitleCanvas(double width, double height) {
		super(width, height);
		textBlinkTimer = 0;
		squaresRenderList = new ArrayList<>();
		newWallCounter = 0;
		resizeOccured = false;
		fadeOutTimer = -1;
		seedDlg = new TextInputDialog();
		seedDlg.setTitle("Enter Seed");
		seedDlg.setHeaderText("");
		seedDlg.setContentText("Please enter the seed for generating walls.\nYou can leave this field blank to randomly choose it.");
	}
	
	private class TitleSquare {
		private double zPos;
		private double x;
		private double y;
		private double size;
		private Color color;
		
		public TitleSquare(double x, double y, double size, double zOffset, Color color) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.zPos = Constants.WALL_START_Z + zOffset;
			this.color = color;
		}
		
		public void render(GraphicsContext gc, double deltaZ) {
			double tx = x * 2 * screenSize / zPos;
			double ty = y * 2 * screenSize / zPos;
			double ts = size * 2 * screenSize / zPos;
			double op = Util.clamp((Constants.WALL_START_Z - zPos) / Constants.WALL_DISTANCE_BETWEEN, 0, 1);
			gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity() * op));
			gc.strokeRect(tx - ts / 2, ty - ts / 2, ts, ts);
			zPos -= deltaZ;
		}

		private double getZpos() {
			return zPos;
		}
	}

	@Override
	public void doAnimation(long deltaT) {
		if(fadeOutTimer < 0) {
			if(mouse.getClicked().contains(MouseButton.PRIMARY) && !seedDlg.isShowing()) seedDlg.show();
			seed = seedDlg.getResult();
			if(seed != null) {
				seedDlg.setResult("");
				fadeOutTimer = 0;
				Resource.sndTransition.play();
			}
		}
		
		GraphicsContext gc = getGraphicsContext2D();
		double sW = SceneManager.width;
		double sH = SceneManager.height;
		screenSize = Math.min(sW, sH);
		
		textBlinkTimer = (textBlinkTimer + deltaT) % 1500000000;
		double dZ = deltaT * Constants.WALL_ADVANCE_RATE;
		newWallCounter -= dZ;
		if(newWallCounter < 0) {
			squaresRenderList.add(new TitleSquare(0, 0, 1, newWallCounter, Color.WHITE));
			newWallCounter += Constants.WALL_DISTANCE_BETWEEN;
		}
		particleTimer += deltaT;
		while(particleTimer > 10000000) {
			if(Math.random() < 0.2) {
				double x = Math.random() * 4 - 2;
				double y = Math.random() * 4 - 2;
				double s = Math.random() * 0.1;
				Color c = Color.WHITE;
				if(Math.random() < Constants.WALL_MAX_COLOR_DENSITY) {
					WallCell wc = WallCell.COLORS[(int)(Math.random() * WallCell.COLORS.length)];
					c = WallCell.FILL_COLORS.get(wc);
					c = Color.hsb(c.getHue(), c.getSaturation() / 2, c.getBrightness());
				}
				squaresRenderList.add(new TitleSquare(x, y, s, 0, c));
			}
			particleTimer -= 10000000;
		}
		
		gc.save();
		gc.setFill(Color.gray(0, resizeOccured ? 1 : 0.25));
		gc.fillRect(0, 0, sW, sH);
		
		gc.translate(sW / 2, sH / 2);
		gc.setLineWidth(screenSize * 1.5/768.0);
		drawDepthGuide(gc, -0.5, -0.5);
		drawDepthGuide(gc,  0.5, -0.5);
		drawDepthGuide(gc, -0.5,  0.5);
		drawDepthGuide(gc,  0.5,  0.5);
		squaresRenderList.removeIf(p -> p.getZpos() < 0);
		Comparator<TitleSquare> cmp = Comparator.comparing(TitleSquare::getZpos).reversed();
		squaresRenderList.sort(cmp);
		for(TitleSquare i : squaresRenderList) i.render(gc, dZ);
		gc.translate(-sW / 2, -sH / 2);

		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFill(Color.WHITE);
		gc.setFont(Resource.fntTitle);
		gc.fillText("->THRU", sW / 2, sH * 0.3);
		if(textBlinkTimer >= 750000000) {
			gc.setFont(Resource.fntUi);
			gc.fillText("left click to begin", sW / 2, sH * 5 / 6);
		}
		if(fadeOutTimer >= 0) {
			gc.setFill(Color.gray(1, Math.min(fadeOutTimer / 2e9, 1.0)));
			gc.fillRect(0, 0, sW, sH);
			fadeOutTimer += deltaT;
		}
		if(fadeOutTimer > 2000000000) {
			SceneManager.gameSeed = seed;
			SceneManager.setCanvas(CanvasID.GAME);
		}
		
		gc.restore();
	}
	
	private void drawDepthGuide(GraphicsContext gc, double x, double y) {
		Complex dg = new Complex(x, y);
		Complex dc = new Complex(x * 2 * screenSize, y * 2 * screenSize);
		if(dg.abs() > 0) {
			double mb = new Complex(SceneManager.width, SceneManager.height).abs();
			dg = Complex.fromPolar(mb, dg.arg());
			gc.setStroke(RadialGradient.valueOf(String.format("center 0px 0px, radius %fpx, transparent %f%%, white 100%%",
					dc.abs() / 2, 200 / Constants.WALL_START_Z)));
			gc.strokeLine(0, 0, dg.re, dg.im);
		}
	}

}