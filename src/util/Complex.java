package util;

public class Complex {
	public double re;
	public double im;
	
	public Complex() {
		this.re = 0;
		this.im = 0;
	}
	
	public Complex(double re, double im) {
		this.re = re;
		this.im = im;
	}
	
	public static Complex fromPolar(double r, double phi) {
		return new Complex(r * Math.cos(phi), r * Math.sin(phi));
	}
	
	public double abs() {
		return Math.sqrt(Math.pow(re, 2) + Math.pow(im, 2));
	}
	
	public double arg() {
		return Math.atan2(im, re);
	}
}
