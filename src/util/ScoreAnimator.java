package util;

public class ScoreAnimator {
	private int last;
	private int current;
	private int target;
	private double extent;
	private double curX;
	
	public ScoreAnimator(int init, double extent) {
		this.last = init;
		this.current = init;
		this.target = init;
		this.extent = extent;
		this.curX = 0;
	}
	
	public void setCurrent(int value) {
		last = value;
		current = value;
	}
	
	public void setTarget(int value) {
		last = target;
		current = target;
		target = value;
		curX = 0;
	}

	public int getCurrent() {
		return current;
	}
	
	public void animate(double deltaX) {
		if(curX > extent) current = target;
		else {
			// integrate(e**(-x), 0, +inf) = 1
			// integrate(e**(-x), 0, n) = sinh(n) - cosh(n) + 1
			current = last + (int)((target - last) * (Math.sinh(curX) - Math.cosh(curX) + 1) + 0.5);
			curX += deltaX;
		}
	}
}
