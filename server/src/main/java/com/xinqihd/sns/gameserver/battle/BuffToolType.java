package com.xinqihd.sns.gameserver.battle;

import java.util.HashMap;

/**
 * All of the buff tools type an user can use in a combat.
 * @author wangqi
 *
 */
public enum BuffToolType {
	
	POWER(0),
	
	//The attack will be sent two more time
  //elseif (buffId == 2) then
  //char:addBuff(BuffAttackTimes2:new(1, bool))
	AttackTwoMoreTimes(2),
	
	//The attack will be sent in three direction
	/*
		elseif (buffId == 3) then
    char:addBuff(BuffThreeWay:new(1, bool));
	 */
	AttackThreeBranch(3),
	
	//The attack will be sent one more time
	/*
	  elseif (buffId == 4) then
    char:addBuff(BuffAttackTimes1:new(1, bool));
	 */
  AttackOneMoreTimes(4),
  
  //The hurt will add 50 percent than normal
  /*
  	elseif (buffId == 5) then
    char:addBuff(BuffAttack50:new(1, bool));
   */
  HurtAdd50(5),
  
  //The hurt will add 40 percent than normal
  HurtAdd40(6),
  
  //The hurt will add 30 percent than normal
  HurtAdd30(7),
  
  //The hurt will add 20 percent than normal
  HurtAdd20(8),
  
  //The hurt will add 10 percent than normal
  HurtAdd10(9),
  
  //The recover of current user's blood
  /*
  elseif (buffId == 24) then
    char:addBuff(BuffCure:new(0, bool));
   */
  Recover(24),
  
  //The recover of all team members' blood
  /*
  elseif (buffId == 28) then
    char:addBuff(BuffPartyCure:new(0, bool));
   */
  AllRecover(28),
  
  //The current user is hidden
  /*
  elseif (buffId == 26) then
    char:addBuff(BuffHide:new(3, bool));
   */
  Hidden(26),
  
  //The team is hidden
  /*
  elseif (buffId == 27) then
    char:addBuff(BuffPartyHide:new(2, bool));
   */
  AllHidden(27),
  
  //The current round's wind will be zero
  /*
  elseif (buffId == 30) then
    char:addBuff(BuffChangeWind:new(0, bool));
   */
  Wind(30),
  
  //The target user will be iced
  /*
  elseif (buffId == 29) then
    char:addBuff(BuffFreeze:new(1, bool));
   */
  Ice(29),
  
  //The current user will fly
  /*
  elseif (buffId == 1) then
    char:addBuff(BuffPlane:new(1))
   */
  Fly(1),
  
  //The current user will use guide missile
  /*
  elseif (buffId == 20) then
    char:addBuff(BuffGuide:new(1, bool));
   */
  Guide(20),
  
  //The current user will have full power
  /*
  elseif (buffId == 25) then
    char:addBuff(BuffAnger:new(0, bool));
   */
  Energy(25),
  
  //The current user will use a nuclear weapon
  /*
  elseif (buffId == 21) then
    char:addBuff(BuffNuclear:new(1));	
   */
  Atom(21),
  
  //The next round attack cannot damage surface.
  /*
  elseif (buffId == 22) then
    char:addBuff(BuffProtection:new(2));
   */
  NoHole(22);
  
  private int id = 0;
  
  private static final HashMap<Integer, BuffToolType> ID_MAP =
  		new HashMap<Integer, BuffToolType>();
  static {
  	for ( BuffToolType tool : BuffToolType.values() ) {
  		ID_MAP.put(tool.id, tool);
  	}
  }
  
	BuffToolType(int id) {
		this.id = id;
	}
	
	public final int id() {
		return this.id;
	}
	
	/**
	 * Get the corresponding BuffToolType by its id. It may be null.
	 * 
	 * @param id
	 * @return
	 */
	public final static BuffToolType fromId(int id) {
		return ID_MAP.get(id);
	}
}
