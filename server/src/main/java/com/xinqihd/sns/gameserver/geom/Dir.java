package com.xinqihd.sns.gameserver.geom;


public enum Dir {
//	LEFT_TOP,     //0
	TOP,          //1
//	RIGHT_TOP,    //2
	RIGHT,        //3
//	RIGHT_BOTTOM, //4
	BOTTOM,       //5
//	LEFT_BOTTOM,  //6
	LEFT;         //7

	/**
	 * Get the reverse direction.
	 * @return
	 */
	public Dir reverseDir() {
		int index = this.ordinal() + Dir.values().length/2;
		if ( index > Dir.values().length ) {
			index = index - Dir.values().length;
		}
		return Dir.values()[index];
	}

}
