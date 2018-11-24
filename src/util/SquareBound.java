package util;

public class SquareBound {
	private double x, y, s;

	public SquareBound(double w, double h) {
		if(w < h) {
			s = w;
			x = 0;
			y = (h - w) / 2;
		} else {
			s = h;
			x = (w - h) / 2;
			y = 0;
		}
	}
	
	public double xOffset() {
		return xOffset(0);
	}
	
	public double xOffset(double a) {
		return x + a;
	}
	
	public double yOffset() {
		return yOffset(0);
	}

	public double yOffset(double a) {
		return y + a;
	}

	public double size() {
		return s;
	}
	
	public double ratioToSize(double ratio) {
		return s * ratio;
	}
}
