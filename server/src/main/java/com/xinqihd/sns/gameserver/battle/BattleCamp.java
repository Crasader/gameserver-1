package com.xinqihd.sns.gameserver.battle;

/**
 * The Battle's camp id, left or right.
 * @author wangqi
 *
 */
public enum BattleCamp {
	LEFT(1),
	RIGHT(2),
	BOTH(3);
	
	private int id;
	
	BattleCamp(int id) {
		this.id = id;
	}
	
	public int id() {
		return this.id;
	}
}
