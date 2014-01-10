package com.xinqihd.sns.gameserver.entity.user;

import java.io.Serializable;

public class RoleInfo implements Serializable {

	private static final long serialVersionUID = 1625206191514059603L;

	// The session key for the combat that users enter.
  private String sessionId;
  
  // 阵营号
  private int campId;
  
  // 在房间里的位置
  private int roomIdx;
  
  // 移动速度
  private int moveSpeed;
  
  //技能1 - 技能8
  private int[] buffs = new int[8];
  
  //在对局房间里的位置
  private int battleRoomIdx;
  
	//用户类型：0：玩家 1：机器人，其他：敌人ID	
  private int roleTypeID;
  
  //工会ID
  private int guildID = 47;
  
  //工会名称
  private String guildName;
  
	//--------------------------------- Properties method

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return the campId
	 */
	public int getCampId() {
		return campId;
	}

	/**
	 * @param campId the campId to set
	 */
	public void setCampId(int campId) {
		this.campId = campId;
	}

	/**
	 * @return the roomIdx
	 */
	public int getRoomIdx() {
		return roomIdx;
	}

	/**
	 * @param roomIdx the roomIdx to set
	 */
	public void setRoomIdx(int roomIdx) {
		this.roomIdx = roomIdx;
	}

	/**
	 * @return the moveSpeed
	 */
	public int getMoveSpeed() {
		return moveSpeed;
	}

	/**
	 * @param moveSpeed the moveSpeed to set
	 */
	public void setMoveSpeed(int moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	/**
	 * @return the buffs
	 */
	public int[] getBuffs() {
		return buffs;
	}

	/**
	 * @param buffs the buffs to set
	 */
	public void setBuffs(int[] buffs) {
		this.buffs = buffs;
	}

	/**
	 * @return the battleRoomIdx
	 */
	public int getBattleRoomIdx() {
		return battleRoomIdx;
	}

	/**
	 * @param battleRoomIdx the battleRoomIdx to set
	 */
	public void setBattleRoomIdx(int battleRoomIdx) {
		this.battleRoomIdx = battleRoomIdx;
	}

	/**
	 * @return the roleTypeID
	 */
	public int getRoleTypeID() {
		return roleTypeID;
	}

	/**
	 * @param roleTypeID the roleTypeID to set
	 */
	public void setRoleTypeID(int roleTypeID) {
		this.roleTypeID = roleTypeID;
	}

	/**
	 * @return the guildID
	 */
	public int getGuildID() {
		return guildID;
	}

	/**
	 * @param guildID the guildID to set
	 */
	public void setGuildID(int guildID) {
		this.guildID = guildID;
	}

	/**
	 * @return the guildName
	 */
	public String getGuildName() {
		return guildName;
	}

	/**
	 * @param guildName the guildName to set
	 */
	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}

}
