package com.xinqihd.sns.gameserver.geom;

import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The dimension class contains width & height, as well as center of a geometry.
 * @author wangqi
 *
 */
public class Dim {

	private int width;
	
	private int height;
	
	//The center's x
	private double cx;
	
	//The center's y
	private double cy;

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the cx
	 */
	public double getCx() {
		return cx;
	}

	/**
	 * @param cx the cx to set
	 */
	public void setCx(double cx) {
		this.cx = cx;
	}

	/**
	 * @return the cy
	 */
	public double getCy() {
		return cy;
	}

	/**
	 * @param cy the cy to set
	 */
	public void setCy(double cy) {
		this.cy = cy;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return StringUtil.concat("Dim [width=",  width,  ", height=",  height,  ", cx=",  cx
				+ ", cy=",  cy,  "]");
	}
	
}
