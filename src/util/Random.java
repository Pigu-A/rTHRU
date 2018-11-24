package util;

public class Random extends java.util.Random {
	private static final long serialVersionUID = -1441811662233073644L;
	private long seed;
	
	public Random(long seed) {
		super(seed);
		this.seed = seed;
	}

	/**
	 * Creates a new random number generator using a 64-bit hash of string as a seed.
	 * If the string is empty, it will use the string representation of <code>System.currentTimeMillis()</code> instead.
	 * @param seed the initial seed
	 */
	public Random(String seed) {
		super();
		String s = seed;
		if(s.equals("")) s = String.valueOf(System.currentTimeMillis());
		// this algorithm is the same as String.hashCode() but with 64-bit space instead of 32-bit
		long e = 1;
		long t = 0;
		for(int i = s.length() - 1; i >= 0; i--) {
			t += s.codePointAt(i) * e;
			e *= 31;
		}
		this.seed = t;
		setSeed(t);
	}
	
	/**
	 * Randomly generates a number in the range [a, b)
	 */
	public double nextRange(double a, double b) {
		return nextDouble() * (b - a) + a;
	}
	
	public long getSeed() {
		return seed;
	}

}
