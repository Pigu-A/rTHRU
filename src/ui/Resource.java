package ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import logic.Constants;
import util.SquareBound;

public class Resource {
	public static Image imgMvmtIndicator;
	public static Font fntScore;
	public static Font fntUi;
	public static Font fntTitle;
	public static AudioClip sndBlankPassed;
	public static AudioClip sndColorPassed;
	public static AudioClip sndCollided;
	public static AudioClip sndTransition;
	
	static {
		try {
			imgMvmtIndicator = new Image(getResource("mvmt_indicator.png"));
			testResourceExists("Offside-Regular.ttf");
			testResourceExists("Share-Regular.ttf");
			sndBlankPassed = new AudioClip(getResourceAsURL("blank_cell_passed.wav"));
			sndColorPassed = new AudioClip(getResourceAsURL("colored_cell_passed.wav"));
			sndCollided = new AudioClip(getResourceAsURL("collided.wav"));
			sndTransition = new AudioClip(getResourceAsURL("transition.wav"));
		} catch (FileNotFoundException e) {
			SceneManager.closeProgram();
			Alert alert = new Alert(AlertType.ERROR, "Missing resource: " + e.getMessage(), ButtonType.OK);
			alert.setHeaderText("");
			alert.show();
		}
	}

	public static InputStream getResource(String res) throws FileNotFoundException {
		InputStream is = ClassLoader.getSystemResourceAsStream(res);
		if(is == null) throw new FileNotFoundException(res);
		return is;
	}

	public static String getResourceAsURL(String res) throws FileNotFoundException {
		URL url = ClassLoader.getSystemResource(res);
		if(url == null) throw new FileNotFoundException(res);
		return url.toExternalForm();
	}
	
	public static void testResourceExists(String res) throws FileNotFoundException {
		if(ClassLoader.getSystemResource(res) == null)
			throw new FileNotFoundException(res);
	}
	
	public static void regenFonts() {
		SquareBound sq = new SquareBound(SceneManager.width, SceneManager.height);
		try {
			fntScore = Font.loadFont(getResource("Offside-Regular.ttf"), sq.ratioToSize(Constants.FONT_SCORE_SIZE));
			fntUi = Font.loadFont(getResource("Share-Regular.ttf"), sq.ratioToSize(Constants.FONT_UI_SIZE));
			fntTitle = Font.loadFont(getResource("Offside-Regular.ttf"), sq.ratioToSize(Constants.FONT_TITLE_SIZE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * Draws an image with x and y as the center
	 * @param gc GraphicsContext to draw an image to
	 * @param img Image to be drawn
	 * @param x X position to draw, this will be at the center of an image
	 * @param y Y position to draw, this will be at the center of an image
	 * @param scale Ratio to scale img's size
	 */
	public static void drawImage(GraphicsContext gc, Image img, double x, double y, double scale) {
		drawImage(gc, img, x, y, scale, scale);
	}
	
	/***
	 * Draws an image with x and y as the center
	 * @param gc GraphicsContext to draw an image to
	 * @param img Image to be drawn
	 * @param x X position to draw, this will be at the center of an image
	 * @param y Y position to draw, this will be at the center of an image
	 * @param sx Ratio to scale img's width, 1 is no scaling
	 * @param sy Ratio to scale img's height, 1 is no scaling
	 */
	public static void drawImage(GraphicsContext gc, Image img, double x, double y, double sx, double sy) {
		double sw = img.getWidth() * sx;
		double sh = img.getHeight() * sy;
		gc.drawImage(img, x - sw / 2, y - sh / 2, sw, sh);
	}
}
