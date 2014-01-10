package com.xinqihd.sns.gameserver.script.function;

import org.apache.commons.math.util.FastMath;

/**
 * Calculate the battle combating parameters.
 * @author wangqi
 *
 */
public class BattleCalculator {

	/**
		power
		angle
		wind
		F
		K
		g
		X Function:
		 ax = -wind/F
		 bx = (power/K)*cos(angle)
		 a*t^2 + b*t - x = 0
		 t = (Math.sqrt(bx^2+4*ax*x) - bx)/(2*ax)
	 *
	 *
	 * @param power
	 * @param angle
	 * @param wind
	 * @param F
	 * @param K
	 * @param g
	 * @param x
	 * @return
	 */
	public static double calculateTByX(int power, int angle, double wind, double F, double K, int g, int x) {
		double ax = -wind * F;
		double bx = (power/K)*FastMath.cos(angle*Math.PI/180);
		double t  = Double.NaN;
		if ( wind != 0 ) {
			double delta = bx*bx+4*ax*x;
			if ( delta > 0 ) {
				t  = (Math.sqrt(delta) - bx)/(2*ax);
			}
		} else {
			t  = x/bx; 
		}
		return t;
	}
	
	/**
	 * 
	 * @param power
	 * @param angle
	 * @param wind
	 * @param F
	 * @param K
	 * @param g
	 * @param t
	 * @return
	 */
	public static double calculateYByT(int power, int angle, double wind, double F, double K, int g, double t) {
		double ay = -g/2;
		double by = (power/K)*FastMath.sin(angle*Math.PI/180);
		double y  = ay*t*t + by*t ;
		return y;
	}
	
	/**
	 * Note: If the angle is near 90 degree, you should not use this method because the result is invalid.
	 * 
			Y Function:
			 ay = -g/2
			 by = (power/k)*sin(angle)
			 y = ay*t^2 + by*t + 0
			 y = ay*(Math.sqrt(-4*ax*x+bx^2) - bx)/(2*ax)^2 + by*(Math.sqrt(-4*ax*x+bx^2) - bx)/(2*ax)
	 * 
	 * @param power
	 * @param angle
	 * @param wind
	 * @param F
	 * @param K
	 * @param g
	 * @param x
	 * @return
	 */
	public static double calculateYByX(int power, int angle, double wind, double F, double K, int g, int x) {
		double t  = calculateTByX(power, angle, wind, F, K, g, x);
		if ( t == Double.NaN ) return Double.NaN;
		double ay = -g/2;
		double by = (power/K)*FastMath.sin(angle*Math.PI/180);
		double y  = ay*t*t + by*t ;
		return y;
	}

}
