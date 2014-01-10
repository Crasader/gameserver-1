package com.xinqihd.sns.gameserver.boss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.xinqihd.sns.gameserver.config.MapPojo.Point;
import com.xinqihd.sns.gameserver.reward.Reward;

/**
 * The Boss wrapper object
 * @author wangqi
 *
 */
public class BossPojo {
	
	/**
	 * The boss's id
	 */
	private String _id;
	
	/**
	 * The boss thumbnail id
	 */
	private String bossId;
	
	/**
	 * The boss's name
	 */
	private String name;
	
	/**
	 * The boss's name
	 */
	private String title;
	
	/**
	 * The winning target.
	 */
	private String target;
	
	/**
	 * The map for this boss 
	 */
	private String mapId;
	
	/**
	 * The boss's description
	 */
	private String desc;
	
	/**
	 * Boss base attribute
	 */
	private int blood;
	private int attack;
	private int defend;
	private int agility;
	private int lucky;
	private int thew;
	
	/**
	 * The boss's level
	 */
	private int level;
	
	/**
	 * The boss type.
	 */
	private BossType bossType;
	
	/**
	 * 
	 */
	private BossWinType bossWinType;
	
	/**
	 * The condition to challenge the boss.
	 */
	private ArrayList<BossCondition> requiredConditions = 
			new ArrayList<BossCondition>();
	
	/**
	 * The total bosses in a single game to be killed.
	 */
	private int totalBosses = 1;
	
	/**
	 * The total round numbers for this boss
	 */
	private int totalRound = 30;
	
	/**
	 * The challenge's reward list.
	 */
	private HashMap<HardMode, ArrayList<Reward>> rewards =
			new HashMap<HardMode, ArrayList<Reward>>();
	
	/**
	 * The boss's width
	 */
	private int width = 0;
	
	/**
	 * The boss's height
	 */
	private int height = 0;
	
	/**
	 * The center points in the boss's body
	 * that can hurt him
	 */
	private ArrayList<Point> hitpoints = 
			new ArrayList<Point>();
	
	/**
	 * The radius of the hurt area.
	 * HurtArea = 
	 *   hitpoint.x-hurtRadius, hitpoint.x+hurtRadius
	 *   hitpoint.y-hurtRadius, hitpiont.y+hurtRadius
	 */
	private int hurtRadius = 0;

	/**
	 * The boss's suit propId.
	 */
	private String suitPropId = null;

	/**
	 * The boss's weapon
	 */
	private String weaponPropId = null;

	/**
	 * The BitmapRoleAttack script name for this boss
	 */
	private String roleAttackScript = null;

	/**
	 * The customized role dead script for this boss
	 */
	private String roleDeadScript = null;
	
	/**
	 * Create the boss dynamically.
	 */
	private String createBossScript = null;
	
	/**
	 * Create the boss dynamically.
	 */
	private String createUserScript = null;
	
	/**
	 * 战斗抽奖脚本
	 */
	private String battleRewardScript = null;
	
	/**
	 * 每日可用次数
	 */
	private int challengeLimit = 0;

	/**
	 * 每小时可用次数恢复情况
	 */
	private int challengeIncreasePerHour = 0;

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
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
	 * @return the attack
	 */
	public int getAttack() {
		return attack;
	}

	/**
	 * @param attack the attack to set
	 */
	public void setAttack(int attack) {
		this.attack = attack;
	}

	/**
	 * @return the defend
	 */
	public int getDefend() {
		return defend;
	}

	/**
	 * @param defend the defend to set
	 */
	public void setDefend(int defend) {
		this.defend = defend;
	}

	/**
	 * @return the agility
	 */
	public int getAgility() {
		return agility;
	}

	/**
	 * @param agility the agility to set
	 */
	public void setAgility(int agility) {
		this.agility = agility;
	}

	/**
	 * @return the lucky
	 */
	public int getLucky() {
		return lucky;
	}

	/**
	 * @param lucky the lucky to set
	 */
	public void setLucky(int lucky) {
		this.lucky = lucky;
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
	 * @return the requiredConditions
	 */
	public ArrayList<BossCondition> getRequiredConditions() {
		return requiredConditions;
	}

	/**
	 * @param requiredConditions the requiredConditions to set
	 */
	public void addRequiredConditions(BossCondition requiredCondition) {
		this.requiredConditions.add(requiredCondition);
	}

	/**
	 * @return the _id
	 */
	public String getId() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void setId(String _id) {
		this._id = _id;
	}

	/**
	 * @return the bossType
	 */
	public BossType getBossType() {
		return bossType;
	}

	/**
	 * @param bossType the bossType to set
	 */
	public void setBossType(BossType bossType) {
		this.bossType = bossType;
	}

	/**
	 * @return the totalBosses
	 */
	public int getTotalBosses() {
		return totalBosses;
	}

	/**
	 * @param totalBosses the totalBosses to set
	 */
	public void setTotalBosses(int totalBosses) {
		this.totalBosses = totalBosses;
	}

	/**
	 * @return the rewards
	 */
	public ArrayList<Reward> getRewards() {
		return getRewards(HardMode.simple);
	}
	
	/**
	 * Get the boss as given hard mode.
	 * @param hardMode
	 * @return
	 */
	public ArrayList<Reward> getRewards(HardMode hardMode) {
		return rewards.get(hardMode);
	}
	
	/**
	 * 
	 * @return
	 */
	public HashMap<HardMode, ArrayList<Reward>> getRewardMap() {
		return rewards;
	}

	/**
	 * @param rewards the rewards to set
	 */
	public void addRewards(Reward reward) {
		addRewards(reward, HardMode.simple);
	}
	
	/**
	 * 
	 * @param reward
	 * @param hardMode
	 */
	public void addRewards(Reward reward, HardMode hardMode) {
		ArrayList<Reward> list = this.rewards.get(hardMode);
		if ( list == null ) {
			list = new ArrayList<Reward>(); 
			this.rewards.put(hardMode, list);
		}
		list.add(reward);
	}
	
	/**
	 * 
	 * @param reward
	 * @param hardMode
	 */
	public void addRewards(Collection<Reward> rewards, HardMode hardMode) {
		ArrayList<Reward> list = this.rewards.get(hardMode);
		if ( list == null ) {
			list = new ArrayList<Reward>(); 
			this.rewards.put(hardMode, list);
		}
		list.addAll(rewards);
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the totalRound
	 */
	public int getTotalRound() {
		return totalRound;
	}

	/**
	 * @param totalRound the totalRound to set
	 */
	public void setTotalRound(int totalRound) {
		this.totalRound = totalRound;
	}

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
	 * @return the mapId
	 */
	public String getMapId() {
		return mapId;
	}

	/**
	 * @param mapId the mapId to set
	 */
	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	/**
	 * @return the hitpoints
	 */
	public ArrayList<Point> getHitpoints() {
		return hitpoints;
	}

	/**
	 * @param hitpoints the hitpoints to set
	 */
	public void addHitpoints(Point hitpoint) {
		this.hitpoints.add(hitpoint);
	}

	/**
	 * @return the hurtRadius
	 */
	public int getHurtRadius() {
		return hurtRadius;
	}

	/**
	 * @param hurtRadius the hurtRadius to set
	 */
	public void setHurtRadius(int hurtRadius) {
		this.hurtRadius = hurtRadius;
	}

	/**
	 * @return the suitPropId
	 */
	public String getSuitPropId() {
		return suitPropId;
	}

	/**
	 * @param suitPropId the suitPropId to set
	 */
	public void setSuitPropId(String suitPropId) {
		this.suitPropId = suitPropId;
	}

	/**
	 * @return the roleAttackScript
	 */
	public String getRoleAttackScript() {
		return roleAttackScript;
	}

	/**
	 * @param roleAttackScript the roleAttackScript to set
	 */
	public void setRoleAttackScript(String roleAttackScript) {
		this.roleAttackScript = roleAttackScript;
	}

	/**
	 * @return the roleDeadScript
	 */
	public String getRoleDeadScript() {
		return roleDeadScript;
	}

	/**
	 * @param roleDeadScript the roleDeadScript to set
	 */
	public void setRoleDeadScript(String roleDeadScript) {
		this.roleDeadScript = roleDeadScript;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the bossId
	 */
	public String getBossId() {
		return bossId;
	}

	/**
	 * @param bossId the bossId to set
	 */
	public void setBossId(String bossId) {
		this.bossId = bossId;
	}

	/**
	 * @return the weaponPropId
	 */
	public String getWeaponPropId() {
		return weaponPropId;
	}

	/**
	 * @param weaponPropId the weaponPropId to set
	 */
	public void setWeaponPropId(String weaponPropId) {
		this.weaponPropId = weaponPropId;
	}

	/**
	 * @return the bossWinType
	 */
	public BossWinType getBossWinType() {
		return bossWinType;
	}

	/**
	 * @param bossWinType the bossWinType to set
	 */
	public void setBossWinType(BossWinType bossWinType) {
		this.bossWinType = bossWinType;
	}
	
	/**
	 * @return the createBossScript
	 */
	public String getCreateBossScript() {
		return createBossScript;
	}

	/**
	 * @param createBossScript the createBossScript to set
	 */
	public void setCreateBossScript(String createBossScript) {
		this.createBossScript = createBossScript;
	}

	/**
	 * @return the createUserScript
	 */
	public String getCreateUserScript() {
		return createUserScript;
	}

	/**
	 * @param createUserScript the createUserScript to set
	 */
	public void setCreateUserScript(String createUserScript) {
		this.createUserScript = createUserScript;
	}

	/**
	 * @return the battleRewardScript
	 */
	public String getBattleRewardScript() {
		return battleRewardScript;
	}

	/**
	 * @param battleRewardScript the battleRewardScript to set
	 */
	public void setBattleRewardScript(String battleRewardScript) {
		this.battleRewardScript = battleRewardScript;
	}

	/**
	 * @return the challengeLimit
	 */
	public int getChallengeLimit() {
		return challengeLimit;
	}

	/**
	 * @param challengeLimit the challengeLimit to set
	 */
	public void setChallengeLimit(int challengeLimit) {
		this.challengeLimit = challengeLimit;
	}

	/**
	 * @return the challengeIncreasePerHour
	 */
	public int getChallengeIncreasePerHour() {
		return challengeIncreasePerHour;
	}

	/**
	 * @param challengeIncreasePerHour the challengeIncreasePerHour to set
	 */
	public void setChallengeIncreasePerHour(int challengeIncreasePerHour) {
		this.challengeIncreasePerHour = challengeIncreasePerHour;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format(
						"BossPojo [_id=%s, name=%s, title=%s, target=%s, mapId=%s, desc=%s, " +
						"blood=%s, attack=%s, defend=%s, agility=%s, lucky=%s, thew=%s, " +
						"level=%s, bossType=%s, requiredConditions=%s, totalBosses=%s, " +
						"totalRound=%s, rewards=%s, width=%s, height=%s, hitpoints=%s, " +
						"hurtRadius=%s, suitPropId=%s, weaponPropId=%s, roleAttackScript=%s]",
						
						_id, name, title, target, mapId, desc, blood, attack, defend,
						agility, lucky, thew, level, bossType, requiredConditions,
						totalBosses, totalRound, rewards, width, height, hitpoints,
						hurtRadius, suitPropId, weaponPropId, roleAttackScript);
	}
	
	@Override
	public BossPojo clone() {
		BossPojo bossPojo = new BossPojo();
		bossPojo._id = this._id;
		bossPojo.bossId = this.bossId;
		bossPojo.name = this.name;
		bossPojo.title = this.title;
		bossPojo.target = this.target;
		bossPojo.mapId = this.mapId;
		bossPojo.desc = this.desc;
		bossPojo.blood = this.blood;
		bossPojo.attack = this.attack;
		bossPojo.defend = this.defend;
		bossPojo.agility = this.agility;
		bossPojo.lucky = this.lucky;
		bossPojo.thew = this.thew;
		bossPojo.level = this.level;
		bossPojo.bossType = this.bossType;
		bossPojo.bossWinType = this.bossWinType;
		bossPojo.requiredConditions = new ArrayList<BossCondition>(this.requiredConditions);
		bossPojo.totalBosses = this.totalBosses;
		bossPojo.totalRound = this.totalRound;
		//bossPojo.rewards = new ArrayList<Reward>(this.rewards);
		bossPojo.rewards = new HashMap<HardMode, ArrayList<Reward>>();
		for ( HardMode hardMode : this.rewards.keySet() ) {
			ArrayList<Reward> list = this.rewards.get(hardMode);
			ArrayList<Reward> newList = new ArrayList<Reward>(list);
			bossPojo.rewards.put(hardMode, newList);
		}
		bossPojo.width = this.width;
		bossPojo.height = this.height;
		bossPojo.hitpoints = new ArrayList<Point>(this.hitpoints);
		bossPojo.hurtRadius = this.hurtRadius;
		bossPojo.suitPropId = this.suitPropId;
		bossPojo.weaponPropId = this.weaponPropId;
		bossPojo.roleAttackScript = this.roleAttackScript;
		bossPojo.roleDeadScript = this.roleDeadScript;
		bossPojo.createBossScript = this.createBossScript;
		bossPojo.createUserScript = this.createUserScript;
		bossPojo.battleRewardScript = this.battleRewardScript;
		bossPojo.challengeLimit = this.challengeLimit;
		bossPojo.challengeIncreasePerHour = this.challengeIncreasePerHour;

		return bossPojo;
	}
}
