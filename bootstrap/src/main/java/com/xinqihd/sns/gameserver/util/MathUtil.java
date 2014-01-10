package com.xinqihd.sns.gameserver.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.util.FastMath;

/**
 * All utility related to do Math operation. For example, the random generator.
 * 
 * @author wangqi
 * 
 */
public class MathUtil {

	private static final int RANGE = 6;

	private static final RandomData randomData = new RandomDataImpl();

	private static final double U = 0.0;
	private static final double Q = 1.0;
	
	//This is the sequence number for fake random 
	private static transient int sequenceNumber = 0;

	/**
	 * Get a uniform distribution random double value as JDK does
	 * 
	 * @return
	 */
	public static final double nextDouble() {
		return randomData.nextUniform(U, Q);
	}

	/**
	 * Get the gaussion distribution random number. It is normal in (-6.0, 6.0)
	 * range.
	 * 
	 * Test Range: Run Uniform Random for 100000000. Time:19843, Heap:0.16635132M
	 * Normal Distribution [9.38663966779904E-11, 0.962247419450565]
	 * 
	 * Test 0-10 distribution: Run Uniform Random for 100000000. Time:18396,
	 * Heap:0.49887848M 0: 45.152159% 1: 31.830864% 2: 15.829606% 3: 5.548033% 4:
	 * 1.369394% 5: 0.23803% 6: 0.02923% 7: 0.002519% 8: 1.57E-4% 9: 8.0E-6%
	 * 
	 * @return
	 */
	public static final double nextGaussionDouble() {
		// u = 0.0; o = 1.0. Standard Normal Distribution
		double r = FastMath.abs(randomData.nextGaussian(U, Q)) / RANGE;
		return r;
	}

	/**
	 * Get the gaussion distribution random number. It is normal in (-q, q) range.
	 */
	public static final double nextGaussionDouble(double u, double q) {
		// u = 0.0; o = 1.0. Standard Normal Distribution
		double r = FastMath.abs(randomData.nextGaussian(u, q)) / RANGE;
		return r;
	}

	/**
	 * Return a int value gt low and less than high: [low, high]
	 * 
	 * @param low
	 * @param high
	 * @return
	 */
	public static final int nextGaussionInt(int low, int high) {
		return nextGaussionInt(low, high, Q);
	}

	/**
	 * Return a int value ge low and le high: [low, high)
	 * 
			i=0, count: 4495, comfreq: 4495, comPct: 0.4495, pct: 0.4495
			i=1, count: 3190, comfreq: 7685, comPct: 0.7685, pct: 0.319
			i=2, count: 1593, comfreq: 9278, comPct: 0.9278, pct: 0.1593
			i=3, count: 541, comfreq: 9819, comPct: 0.9819, pct: 0.0541
			i=4, count: 151, comfreq: 9970, comPct: 0.997, pct: 0.0151
			i=5, count: 26, comfreq: 9996, comPct: 0.9996, pct: 0.0026
			i=6, count: 3, comfreq: 9999, comPct: 0.9999, pct: 3.0E-4
			i=7, count: 1, comfreq: 10000, comPct: 1.0, pct: 1.0E-4
			i=8, count: 0, comfreq: 10000, comPct: 1.0, pct: 0.0
			i=9, count: 0, comfreq: 10000, comPct: 1.0, pct: 0.0
	 * 
	 * @param low
	 * @param high
	 * @return
	 */
	public static final int nextGaussionInt(int low, int high, double q) {
		double d = randomData.nextGaussian(U, q);
		double r = FastMath.abs(d) / RANGE;
		int diff = high - low;
		if (diff <= 0)
			return low;
		int value = (int) (low + diff * r);
		if (value >= high) {
			value = value % diff + low;
		}
		return value;
	}

	/**
	 * Provide a more smooth pro curve
	 * 
			i=0, count: 1385, comfreq: 1385, comPct: 0.1385, pct: 0.1385
			i=1, count: 1306, comfreq: 2691, comPct: 0.2691, pct: 0.1306
			i=2, count: 1209, comfreq: 3900, comPct: 0.39, pct: 0.1209
			i=3, count: 1143, comfreq: 5043, comPct: 0.5043, pct: 0.1143
			i=4, count: 1031, comfreq: 6074, comPct: 0.6074, pct: 0.1031
			i=5, count: 970, comfreq: 7044, comPct: 0.7044, pct: 0.097
			i=6, count: 872, comfreq: 7916, comPct: 0.7916, pct: 0.0872
			i=7, count: 760, comfreq: 8676, comPct: 0.8676, pct: 0.076
			i=8, count: 660, comfreq: 9336, comPct: 0.9336, pct: 0.066
			i=9, count: 664, comfreq: 10000, comPct: 1.0, pct: 0.0664
	 * 
	 * @param low
	 * @param high
	 * @param q
	 * @return
	 */
	public static final int nextGaussionInt2(int low, int high, double q) {
		double d = randomData.nextGaussian(U, q);
		int diff = high - low;
		if (diff <= 0)
			return low;
		double r = FastMath.abs(d) * diff;
		int value = (int) (low + r%diff);
		return value;
	}

	/**
	 * Return the fake random number
	 * @param max
	 * @return
	 */
	public static final int nextFakeInt(int max) {
		return (sequenceNumber++)%max;
	}
	
	/**
	 * 
	 * @param n
	 * @param k
	 * @return
	 */
	public static final int[] nextPermuate(int n, int k) {
		return randomData.nextPermutation(n, k);
	}

	/**
   * Returns an array of <code>k</code> objects selected randomly
   * from the Collection <code>c</code>.
   * <p>
   * Sampling from <code>c</code>
   * is without replacement; but if <code>c</code> contains identical
   * objects, the sample may include repeats.  If all elements of <code>
   * c</code> are distinct, the resulting object array represents a
   * <a href="http://rkb.home.cern.ch/rkb/AN16pp/node250.html#SECTION0002500000000000000000">
   * Simple Random Sample</a> of size
   * <code>k</code> from the elements of <code>c</code>.</p>
   * <p>
   * <strong>Preconditions:</strong><ul>
   * <li> k must be less than or equal to the size of c </li>
   * <li> c must not be empty </li>
   * </ul>
   * If the preconditions are not met, an IllegalArgumentException is
   * thrown.</p>
	 * 
	 * @param collection
	 * @return
	 */
	public static final <T> Object[] randomPick(Collection<T> collection,
			int count) {
		if (collection == null || collection.size() == 0 || count < 0
				|| count > collection.size()) {
			return new Object[0];
		}
		if (count == collection.size()) {
			return collection.toArray();
		}
		return randomData.nextSample(collection, count);
	}
	
	/**
	 * Random pick some object from the collection. Note the count should be less
	 * than the size of collection and the collection should not be null or empty.
	 * 
	 * @param collection
	 * @return
	 */
	public static final <T> List<T> randomPickShuffle(Collection<T> collection,
			int count) {
		ArrayList<T> tList = new ArrayList<T>();
		if (collection == null || collection.size() == 0 || count < 0 ) {
			return tList;
		}
		if (count >= collection.size()) {
			tList.addAll(collection);
			Collections.shuffle(tList);
			return tList;
		}
		Object[] array = randomData.nextSample(collection, count);
		for ( Object obj : array ) {
			tList.add((T)obj);
		}
		return tList;
	}

	/**
	 * Random pick some object from the collection. Note the count should be less
	 * than the size of collection and the collection should not be null or empty.
	 * 
	 * @param collection
	 * @return
	 */
	public static final <T> Collection<T> randomPickGaussion(
			Collection<T> collection, int count, double q) {

		ArrayList<T> list = new ArrayList<T>(count);
		if (collection == null || collection.size() == 0 || count < 0
				|| count > collection.size()) {
			return list;
		}
		if (count == collection.size()) {
			return collection;
		} else {
			ArrayList<T> coll = new ArrayList<T>(collection);
			int low = 0;
			int high = 0;
			int number = 0;
			while (number < count) {
				high = coll.size();
				int index = nextGaussionInt(low, high, q);
				if (index < high) {
					T t = coll.remove(index);
					list.add(t);
					number++;
				}
			}
			return list;
		}
	}

	/**
	 * 用盛金求根法求解一元三次方程组
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return
	 */
	public static final double solveCubicEquation(double a, double b, double c, double d) {
		double FS = 0;
		double A, B, C, delta;
		double x1, x2, x3;
		A = b * b - 3 * a * c;
		B = b * c - 9 * a * d;
		C = c * c - 3 * b * d;
		delta = B * B - 4 * A * C;

		if (A == 0 && B == 0) {
			x1 = x2 = x3 = -c / b;
		}
		/*
		 * Shengjin’s Distinguishing Means ①：当A=B=0时，方程有一个三重实根；
		 * ②：当Δ=B^2－4AC>0时，方程有一个实根和一对共轭虚根； ③：当Δ=B^2－4AC=0时，方程有三个实根，其中有一个两重根；
		 * ④：当Δ=B^2－4AC<0时，方程有三个不相等的实根。
		 */
		if (delta > 0) {
			double y1 = A * b + 3 * a * (-B + Math.pow(B * B - 4 * A * C, 0.5)) * 0.5;
			double y2 = A * b + 3 * a * (-B - Math.pow(B * B - 4 * A * C, 0.5)) * 0.5;

			x1 = (-b - (Math.pow(y1, 1 / 3.0) + Math.pow(y2, 1 / 3.0))) / (3 * a);
			x2 = x3 = -999999999;
		} else if (delta == 0) {
			double K = B / A;
			x1 = -b / a + K;
			x2 = x3 = -K * 0.5;

		} else {
			double T = (2 * A * b - 3 * a * B) / (2 * Math.pow(A, 3 / 2.0));
			double temp = Math.acos(T);

			x1 = (-b - 2 * (2 * Math.pow(Math.cos(temp / 3.0), 0.5))) / (3 * a);
			x2 = (-b + Math.pow(A, 0.5) * Math.cos(temp / 3.0) + Math.pow(3, 0.5)
					* Math.sin(temp / 3.0))
					/ (3 * a);
			x3 = (-b + Math.pow(A, 0.5) * Math.cos(temp / 3.0) - Math.pow(3, 0.5)
					* Math.sin(temp / 3.0))
					/ (3 * a);
		}
		FS = Math.max(Math.max(x1, x2), x3);
		//System.out.println("x1="+x1+", x2="+x2+", x3="+x3);
		return FS;
	}
}
