package com.xinqihd.sns.gameserver.battle;

/**
 * The buff tool's index an user can use in a combat.
 * @author wangqi
 *
 */
public enum BuffToolIndex {
	
	//The power attack
	UsePower(0),
	
	//The flying
	Flying(1),
	
	//The attack will be sent two more time
	AttackTwoMoreTimes(2),
	
	//The attack will be sent in three direction
	AttackThreeBranch(3),
	
	//The attack will be sent one more time
  AttackOneMoreTimes(4),
  
  //The hurt will add 50 percent than normal
  HurtAdd50(5),
  
  //The hurt will add 40 percent than normal
  HurtAdd40(6),
  
  //The hurt will add 30 percent than normal
  HurtAdd30(7),
  
  //The hurt will add 20 percent than normal
  HurtAdd20(8),
  
  //The hurt will add 10 percent than normal
  HurtAdd10(9),
  
  //The user's customized tool index 1
  UserTool1(10),
  
  //The user's customized tool index 2
  UserTool2(11),
  
  //The user's customized tool index 3
  UserTool3(12);
	
	private int slot = 0;
	
	BuffToolIndex(int slot) {
		this.slot = slot;
	}
  
	public int slot() {
		return this.slot;
	}
	
	public static BuffToolIndex fromSlot(int slot) {
		//TODO use the hashmap to check slot in future
		if ( slot >= 0 && slot<BuffToolIndex.values().length ) {
			return BuffToolIndex.values()[slot];
		}
		return null;
	}
}
