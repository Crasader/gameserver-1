package com.xinqihd.sns.gameserver.battle;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.session.SessionKey;

/**
 * This class stores a lot of data that an user generated in a battle,
 * including his energy, health, buff tool used etc. 
 * 
 * @author wangqi
 *
 */
public class BattleUser {
	
	//The current user's sessionkey
	private SessionKey userSessionKey;
	
	//The current user's room sessionKey
	private SessionKey roomSessionKey;

	//The last round's wind power
	private int lastWind = 0;
	
	//The last round's weapon power
	private int lastWeaponPower = 0;
	
	//The user's last action's timestamp
	private long lastActionMillis = 0;
	
	//The last round's attack angle
	private int lastAngle = 0;
	
	//The user's current blood
	private int blood = 0;
	
	//The user's current power attack energy
	private int energy = 0;
	
	//The user's current thew to move or use tool
	private int thew = 0;
	
	//The user's delay value in each round. It decides whether
	//this user can do an action.
	private int delay = 0;
	
	//the user's camp number in a battle.
	private int camp = 1;
	
	//Whether the user use a power attack.
	private boolean isPowerAttack = false;
	
	//The user's current attack status
	//dead, hidden, iced, flying
	private EnumSet<RoleStatus> status = EnumSet.noneOf(RoleStatus.class);
	
	//The user's current attack direction, left or right.
	private RoleDirection direction = RoleDirection.LEFT;
	
	private ActionType actionType = ActionType.DEFAULT;
	
	//Mark the user is ready for a round after he loads the scene and lands on ground.
	private boolean isStageReady = false;
	
	//The BuffTools an user selected. 
	private ArrayList<BuffToolType> tools = new ArrayList<BuffToolType>();
	
	//The current user object
	private User user = null;
	
	//The total exp an user will got
	private int totalExp = 0;
	
	//本次战斗的VIP经验
	private int vipExp = 0;
	
	//本次战斗的双倍经验
	private int doubleExp = 0;
	
	//本次战斗的基础经验
	private int baseExp = 0;
	
	//The total hurt that an user brings to enemies.
	private int totalEnemyHurt = 0;
	
	//The total hurt enemies brings to you.
	private int totalSelfHurt = 0;
	
	//The total kill number in the combat.
	private HashMap<SessionKey, BattleUserAudit> hurtUsers = new HashMap<SessionKey, BattleUserAudit>();
	
	private int totalKill = 0;
	
	//The total hit number in the combat.
	private int totalHit = 0;
	
	//The total number of attack in the combat
	private int totalAttack = 0;
	
	//The total number of diamonds got in the combat
	private int totalDiamonds = 0;
	
	//The attack assistance times.
	private int creditDeta = 0;
	
	//The average hit ratio in a combat. 0.0-100.0
	private float hitRatio = 0.0f;
	
	//The average time spent in a round.
	private float roundAvgTime = 0.0f;
	
	//The experience rate for this user in the battle
	private int expRate = 1;
	
	private int posX;
	
	private int posY;
	
	private int hiddenStartRound = -1;
	
	private int frozenStartRound = -1;
	
	private List<BattleUser> friendUsers = null;
	
	/**
	 * This battleUser is killed by who.
	 * Null means this user is alive.
	 */
	private BattleUser killerUser = null;
	
	private boolean online = true;
	
	//The battle remark
	private String remark;
	
	/**
	 * 存储每一项目对应加成的经验值
	 */
	private HashMap<BattleAuditItem, Integer> auditExpMap = 
			new HashMap<BattleAuditItem, Integer>();
	
	/**
	 * For this user to continuously become a round owner's 
	 * time. 
	 */
	private int roundOwnerTimes = 0;
	
	/**
	 * The battle user runs away from battle.
	 */
	private boolean leaveBattle = false;
	
	public BattleUser() {
		status.add(RoleStatus.NORMAL);
	}

	/**
	 * @return the userSessionKey
	 */
	public SessionKey getUserSessionKey() {
		return userSessionKey;
	}

	/**
	 * @param userSessionKey the userSessionKey to set
	 */
	public void setUserSessionKey(SessionKey userSessionKey) {
		this.userSessionKey = userSessionKey;
	}

	/**
	 * @return the lastWind
	 */
	public int getLastWind() {
		return lastWind;
	}

	/**
	 * @param lastWind the lastWind to set
	 */
	public void setLastWind(int lastWind) {
		this.lastWind = lastWind;
	}

	/**
	 * @return the lastWeaponPower
	 */
	public int getLastWeaponPower() {
		return lastWeaponPower;
	}

	/**
	 * @param lastWeaponPower the lastWeaponPower to set
	 */
	public void setLastWeaponPower(int lastWeaponPower) {
		this.lastWeaponPower = lastWeaponPower;
	}

	/**
	 * @return the lastAngle
	 */
	public int getLastAngle() {
		return lastAngle;
	}

	/**
	 * @param lastAngle the lastAngle to set
	 */
	public void setLastAngle(int lastAngle) {
		this.lastAngle = lastAngle;
	}

	/**
	 * @return the blood
	 */
	public int getBlood() {
		return blood;
	}

	/**
	 * @param blood the blood to set
	 */
	public void setBlood(int blood) {
		this.blood = blood;
	}

	/**
	 * @return the energy
	 */
	public int getEnergy() {
		return energy;
	}

	/**
	 * @param energy the energy to set
	 */
	public void setEnergy(int energy) {
		this.energy = energy;
	}

	/**
	 * @return the isPowerAttack
	 */
	public boolean isPowerAttack() {
		return isPowerAttack;
	}

	/**
	 * @param isPowerAttack the isPowerAttack to set
	 */
	public void setPowerAttack(boolean isPowerAttack) {
		this.isPowerAttack = isPowerAttack;
	}

	/**
	 * @return the status
	 */
	public boolean containStatus(RoleStatus status) {
		return this.status.contains(status);
	}

	/**
	 * @param status the status to set
	 */
	public void addStatus(RoleStatus status) {
		this.status.add(status);
	}
	
	/**
	 * Remove the old status
	 * @param status
	 */
	public void removeStatus(RoleStatus status) {
		this.status.remove(status);
	}
	
	/**
	 * Convert user role status to userMode
	 * @return
	 */
	public int convertStatusToUserBit() {
		int userMode = 0;
		for ( RoleStatus status : this.status ) {
			userMode |= status.toUserModeBit();
		}
		return userMode;
	}
	
	/**
	 * Clear all status
	 */
	public void clearStatus() {
		this.status.clear();
	}

	/**
	 * @return the direction
	 */
	public RoleDirection getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(RoleDirection direction) {
		this.direction = direction;
	}

	/**
	 * @return the tools
	 */
	public ArrayList<BuffToolType> getTools() {
		return tools;
	}

	/**
	 * @param tools the tools to set
	 */
	public void setUsedTool(BuffToolType tool) {
		this.tools.add(tool);
	}
	
	/**
	 * Clear the used tool
	 */
	public void clearUsedTool() {
		this.tools.clear();
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the totalExp
	 */
	public int getTotalExp() {
		return totalExp;
	}

	/**
	 * @param totalExp the totalExp to set
	 */
	public void setTotalExp(int totalExp) {
		this.totalExp = totalExp;
	}

	/**
	 * @return the totalEnemyHurt
	 */
	public int getTotalEnemyHurt() {
		return totalEnemyHurt;
	}

	/**
	 * @param totalEnemyHurt the totalEnemyHurt to set
	 */
	/*
	public void addTotalEnemyHurt(int enemyHurt) {
		this.totalEnemyHurt += enemyHurt;
	}
	*/
	
	public void setTotalEnemyHurt(int totalEnemyHurt) {
		this.totalEnemyHurt = totalEnemyHurt;
	}

	/**
	 * @return the totalSelfHurt
	 */
	public int getTotalSelfHurt() {
		return totalSelfHurt;
	}

	/**
	 * @param totalSelfHurt the totalSelfHurt to set
	 */
	public void addTotalSelfHurt(int totalSelfHurt) {
		this.totalSelfHurt += totalSelfHurt;
	}

	/**
	 * @return the totalKill
	 */
	public HashMap<SessionKey, BattleUserAudit> getHurtUsers() {
		return hurtUsers;
	}
	
	/**
	 * @return the totalKill
	 */
	public int getTotalKill() {
		return totalKill;
	}

	/**
	 * @param totalKill the totalKill to set
	 */
	public void setTotalKill(int totalKill) {
		this.totalKill = totalKill;
	}

	/**
	 * @param hurtUsers the totalKill to set
	 */
	public void setHurtUser(SessionKey userSessionKey, BattleUserAudit audit) {
		this.hurtUsers.put(userSessionKey, audit);
	}
	
	/**
	 * Get the given user's audit. If it does not exist,
	 * create a new one and return.
	 * @param userSessionKey
	 * @return
	 */
	public BattleUserAudit getHurtUser(SessionKey userSessionKey) {
		BattleUserAudit audit = this.hurtUsers.get(userSessionKey);
		if ( audit == null ) {
			audit = new BattleUserAudit();
			audit.setHurtUserSessionKey(userSessionKey);
			audit.setBattleUser(this);
			this.hurtUsers.put(userSessionKey, audit);
		}
		return audit;
	}

	/**
	 * @return the hitRatio
	 */
	public float getHitRatio() {
		return hitRatio;
	}

	/**
	 * @param hitRatio the hitRatio to set
	 */
	public void setHitRatio(float hitRatio) {
		this.hitRatio = hitRatio;
	}

	/**
	 * @return the roundAvgTime
	 */
	public float getRoundAvgTime() {
		return roundAvgTime;
	}

	/**
	 * @param roundAvgTime the roundAvgTime to set
	 */
	public void setRoundAvgTime(float roundAvgTime) {
		this.roundAvgTime = roundAvgTime;
	}

	/**
	 * @return the creditDeta
	 */
	public int getCreditDeta() {
		return creditDeta;
	}

	/**
	 * @param creditDeta the creditDeta to set
	 */
	public void addCreditDeta(int creditDeta) {
		this.creditDeta += creditDeta;
	}

	/**
	 * @return the totalHit
	 */
	public int getTotalHit() {
		return totalHit;
	}

	/**
	 * @param totalHit the totalHit to set
	 */
	public void addTotalHit(int totalHit) {
		this.totalHit += totalHit;
	}

	/**
	 * @return the isStageReady
	 */
	public boolean isStageReady() {
		return isStageReady;
	}

	/**
	 * @param isStageReady the isStageReady to set
	 */
	public void setStageReady(boolean isStageReady) {
		this.isStageReady = isStageReady;
	}

	/**
	 * @return the delay
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	/**
	 * @return the posX
	 */
	public int getPosX() {
		return posX;
	}

	/**
	 * @param posX the posX to set
	 */
	public void setPosX(int posX) {
		this.posX = posX;
	}

	/**
	 * @return the posY
	 */
	public int getPosY() {
		return posY;
	}

	/**
	 * @param posY the posY to set
	 */
	public void setPosY(int posY) {
		this.posY = posY;
	}

	/**
	 * @return the actionType
	 */
	public ActionType getActionType() {
		return actionType;
	}

	/**
	 * @param actionType the actionType to set
	 */
	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	/**
	 * @return the thew
	 */
	public int getThew() {
		return thew;
	}

	/**
	 * @param thew the thew to set
	 */
	public void setThew(int thew) {
		this.thew = thew;
	}

	/**
	 * @return the roomSessionKey
	 */
	public SessionKey getRoomSessionKey() {
		return roomSessionKey;
	}

	/**
	 * @param roomSessionKey the roomSessionKey to set
	 */
	public void setRoomSessionKey(SessionKey roomSessionKey) {
		this.roomSessionKey = roomSessionKey;
	}

	/**
	 * @return the camp
	 */
	public int getCamp() {
		return camp;
	}

	/**
	 * @param camp the camp to set
	 */
	public void setCamp(int camp) {
		this.camp = camp;
	}

	/**
	 * @return the expRate
	 */
	public int getExpRate() {
		return expRate;
	}

	/**
	 * @param expRate the expRate to set
	 */
	public void setExpRate(int expRate) {
		this.expRate = expRate;
	}

	/**
	 * @return the lastActionMillis
	 */
	public long getLastActionMillis() {
		return lastActionMillis;
	}

	/**
	 * @param lastActionMillis the lastActionMillis to set
	 */
	public void setLastActionMillis(long lastActionMillis) {
		this.lastActionMillis = lastActionMillis;
	}

	/**
	 * @return the hiddenStartRound
	 */
	public int getHiddenStartRound() {
		return hiddenStartRound;
	}

	/**
	 * @param hiddenStartRound the hiddenStartRound to set
	 */
	public void setHiddenStartRound(int hiddenStartRound) {
		this.hiddenStartRound = hiddenStartRound;
	}

	/**
	 * @return the frozenStartRound
	 */
	public int getFrozenStartRound() {
		return frozenStartRound;
	}

	/**
	 * @param frozenStartRound the frozenStartRound to set
	 */
	public void setFrozenStartRound(int frozenStartRound) {
		this.frozenStartRound = frozenStartRound;
	}

	/**
	 * @return the friendUsers
	 */
	public List<BattleUser> getFriendUsers() {
		return friendUsers;
	}

	/**
	 * @param friendUsers the friendUsers to set
	 */
	public void setFriendUsers(List<BattleUser> friendUsers) {
		this.friendUsers = friendUsers;
	}

	/**
	 * @return the totalAttack
	 */
	public int getTotalAttack() {
		return totalAttack;
	}

	/**
	 * @param totalAttack the totalAttack to set
	 */
	public void addTotalAttack(int newAttackCount) {
		this.totalAttack += newAttackCount;
	}

	/**
	 * @return the totalDiamonds
	 */
	public int getTotalDiamonds() {
		return totalDiamonds;
	}

	/**
	 * @param totalDiamonds the totalDiamonds to set
	 */
	public void addTotalDiamonds(int newTotalDiamonds) {
		this.totalDiamonds += newTotalDiamonds;
	}

	/**
	 * @return the vipExp
	 */
	public int getVipExp() {
		return vipExp;
	}

	/**
	 * @param vipExp the vipExp to set
	 */
	public void setVipExp(int vipExp) {
		this.vipExp = vipExp;
	}

	/**
	 * @return the doubleExp
	 */
	public int getDoubleExp() {
		return doubleExp;
	}

	/**
	 * @param doubleExp the doubleExp to set
	 */
	public void setDoubleExp(int doubleExp) {
		this.doubleExp = doubleExp;
	}

	/**
	 * @return the baseExp
	 */
	public int getBaseExp() {
		return baseExp;
	}

	/**
	 * @param baseExp the baseExp to set
	 */
	public void setBaseExp(int baseExp) {
		this.baseExp = baseExp;
	}

	/**
	 * @return the roundOwnerTimes
	 */
	public int getRoundOwnerTimes() {
		return roundOwnerTimes;
	}

	/**
	 * @param roundOwnerTimes the roundOwnerTimes to set
	 */
	public void setRoundOwnerTimes(int roundOwnerTimes) {
		this.roundOwnerTimes = roundOwnerTimes;
	}

	/**
	 * @return the leaveBattle
	 */
	public boolean isLeaveBattle() {
		return leaveBattle;
	}

	/**
	 * @param leaveBattle the leaveBattle to set
	 */
	public void setLeaveBattle(boolean leaveBattle) {
		this.leaveBattle = leaveBattle;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("BattleUser [user=%s]", user.getRoleName());
	}

	/**
	 * @return the auditExpMap
	 */
	public HashMap<BattleAuditItem, Integer> getAuditExpMap() {
		return auditExpMap;
	}

	/**
	 * @param auditExpMap the auditExpMap to set
	 */
	public void setAuditExpMap(HashMap<BattleAuditItem, Integer> auditExpMap) {
		this.auditExpMap = auditExpMap;
	}
	
	/**
	 * @return the remark
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * @param remark the remark to set
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * @return the killerUser
	 */
	public BattleUser getKillerUser() {
		return killerUser;
	}

	/**
	 * @param killerUser the killerUser to set
	 */
	public void setKillerUser(BattleUser killerUser) {
		this.killerUser = killerUser;
	}

	/**
	 * @return the online
	 */
	public boolean isOnline() {
		return online;
	}

	/**
	 * @param online the online to set
	 */
	public void setOnline(boolean online) {
		this.online = online;
	}
	
}
