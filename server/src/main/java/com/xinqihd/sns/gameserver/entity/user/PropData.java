package com.xinqihd.sns.gameserver.entity.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.QualityType;
import com.xinqihd.sns.gameserver.config.equip.TextColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ExcludeField;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.proto.XinqiPropData;
import com.xinqihd.sns.gameserver.proto.XinqiPropDataDesc.PropDataDesc;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;


/**
 * All the items in user's bag.
 * 
 * @author wangqi
 *
 */
public class PropData implements Serializable,Comparable<PropData> {
	
	private static final String SEP = "---";
	
	@ExcludeField
	private static final long serialVersionUID = -1369515086881050921L;

	//道具的ID，对照ZooKeeper中的道具ID
  private String itemId;
  
  //道具的名称，索引到ZooKeeper中保存的道具
  private String name;
  
  //剩余时间
  private int propIndate = Integer.MAX_VALUE;
  
  //道具已经使用的次数
  private int propUsedTime = 0;
  
  /**
   * 商城购买的道具享受双保险
   * 1. 时间上保证最低可用日期
   * 2. 次数上保证最高可用次数
   * 
   * 两个条件都达到才需要进行修理。
   */
  //道具加入背包的时间戳
  private long warrantMillis = 0l;
  //当天最低的消耗下限，耐久不可低于该值
  private int warrantDateLimit = 0;
  //当天的日期值
  private String warrantDateKey = null;

  //数量
  private int count = 1;

  //道具当前的等级（玩家强化）
  private int level = 0;

  //The user' min level to equip it.
  private int userLevel = 0;

  //攻击合成等级
  private int attackLev = 0;

  //防御合成等级
  private int defendLev = 0;
 
  //敏捷合成等级
  private int agilityLev = 0;
  
  //幸运合成等级
  private int luckLev = 0;
  
  //武器基础攻击数值，无颜色的武器等于weapon的attack
  private int baseAttack = 0;
  //武器基础防御数值，无颜色的武器等于weapon的defend
  private int baseDefend = 0;
  //武器基础敏捷数值，无颜色的武器等于weapon的agility
  private int baseAgility = 0;
  //武器基础幸运数值，无颜色的武器等于weapon的luck
  private int baseLuck = 0;
  //武器基础战斗力数值
  private int basePower = 0;

  private int bloodLev = 0;
  //0-100. Add the percent of blood 
  private int bloodPercent = 0;
  //only 0
  private int thewLev = 0;
  private int damageLev = 0;
  //0, 60, 65: Maybe the coordinates
  private int skinLev = 0;
  
  //标志位：8 - 不可冲有效期；
  private int sign = 0;
  
  //购买时的价值单位(来源确定) 0:金币/任务/历史遗留/战斗获得 1:礼金 2:功勋 3:元宝券 4.元宝
  private PropDataValueType valuetype = PropDataValueType.GAME;
  
  //是否绑定
  private boolean banded = true;
  
  //如果装备是绑定的，显示绑定的玩家名
  private String bandUserName = null;
  
  //The item's power
  private int power = 0;
  
  //This propData added timestamp
  private long addTimestamp = 0l;
  
  //The max strength level for this PropData
  private int maxLevel = 12;
  
  //剩余的有效期
  //The duration is set to 0 if isExpire is true.
  //Which means it does not need to store in database.
  private transient int duration = 100;
  
  //这个属性字段保存了装备进行各种强化的历史结果
  private HashMap<PropDataEnhanceType, HashMap<PropDataEnhanceField, Integer>>
  	enhanceMap = new HashMap<PropDataEnhanceType, HashMap<PropDataEnhanceField, Integer>>();
    
  private boolean isWeapon = false;
  
  /**
   * This propData's resubscribed times.
   */
  private int lengthenTimes = 0;
  
  /**
   * The slots for this propData
   */
  private ArrayList<PropDataSlot> slots = new ArrayList<PropDataSlot>();
  
  /**
   * All the items that are merged into 
   * this PropData
   */
  private HashMap<String, Integer> itemMap = new HashMap<String, Integer>();
  
  /**
   * The total number of golden that are used for this equip.
   */
  private int totalGolden = 0;
  
  /**
   * 胜利增加5点熟练度，失败增加1点  
   */
  private int skill = 0;
  
  /**
   * 升级嵌入的水晶石数量
   */
  private int crystal = 0;
  
  // -------------------------------- Internal use.
  
  //The position in User's bag.
  private transient int pew;
  
  // 道具颜色
  private WeaponColor weaponColor;
    
  /**
   * This PropData may be a WeaponPojo or ItemPojo.
   * This field cache the Pojo from EquipPojo or ItemsPojo for later use.
   * It's a read-only field.
   */
  @ExcludeField
  private transient Pojo pojo;
  
  /**
   * This propData is expired and cannot be used.
   */
  private transient boolean expired;
  
  /**
   * It is a just reward type PropData
   */
  private transient boolean reward;

	//--------------------------------- Properties method
  
	/**
	 * @return the itemId
	 */
	public String getItemId() {
		return itemId;
	}
	
	/**
	 * @param itemId the itemId to set
	 */
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	
  public int getColor() {
  	if ( weaponColor != null ) {
  		return weaponColor.toIntColor();
  	} else {
  		return 0;
  	}
  }
  
  public WeaponColor getWeaponColor() {
    return this.weaponColor;
  }

  public void setWeaponColor(WeaponColor color) {
    this.weaponColor = color;
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
	 * @return the attackLev
	 */
	public int getAttackLev() {
		return attackLev;
	}

	/**
	 * @param attackLev the attackLev to set
	 */
	public void setAttackLev(int attackLev) {
		this.attackLev = attackLev;
	}

	/**
	 * @return the defendLev
	 */
	public int getDefendLev() {
		return defendLev;
	}

	/**
	 * @param defendLev the defendLev to set
	 */
	public void setDefendLev(int defendLev) {
		this.defendLev = defendLev;
	}

	/**
	 * @return the agilityLev
	 */
	public int getAgilityLev() {
		return agilityLev;
	}

	/**
	 * @param agilityLev the agilityLev to set
	 */
	public void setAgilityLev(int agilityLev) {
		this.agilityLev = agilityLev;
	}

	/**
	 * @return the luckLev
	 */
	public int getLuckLev() {
		return luckLev;
	}

	/**
	 * @param luckLev the luckLev to set
	 */
	public void setLuckLev(int luckLev) {
		this.luckLev = luckLev;
	}

	/**
	 * @return the sign
	 */
	public int getSign() {
		return sign;
	}

	/**
	 * @param sign the sign to set
	 */
	public void setSign(int sign) {
		this.sign = sign;
	}

	/**
	 * @return the valuetype
	 */
	public PropDataValueType getValuetype() {
		return valuetype;
	}

	/**
	 * @param valuetype the valuetype to set
	 */
	public void setValuetype(PropDataValueType valuetype) {
		this.valuetype = valuetype;
	}

	/**
	 * @return the banded
	 */
	public boolean isBanded() {
		return banded;
	}

	/**
	 * @param banded the banded to set
	 */
	public void setBanded(boolean banded) {
		this.banded = banded;
	}

	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * @return the propIndate
	 */
	public int getPropIndate() {
		return propIndate;
	}

	/**
	 * @param propIndate the propIndate to set
	 */
	public void setPropIndate(int propIndate) {
		this.propIndate = propIndate;
	}

	/**
	 * @return the propUsedTime
	 */
	public int getPropUsedTime() {
		return propUsedTime;
	}
	
	/**
	 * @param propUsedTime the propUsedTime to set
	 */
	public void setPropUsedTime(int propUsedTime) {
		this.propUsedTime = propUsedTime;
	}
	
	/**
	 * Check if the propData expires.
	 * @return
	 */
	public boolean isExpire() {
		return this.expired;
	}
	
	/**
	 * Mark that this propData is already expired and cannot be used.
	 * Why we use a standalone property to do it rather than just 
	 * use 'this.propUsedTime >= this.propIndate' to check? Because
	 * only when users login, the expire mark is checked. That will
	 * case status inconsistency.
	 *  
	 * @param expired
	 */
	public void setExpire(boolean expired) {
		this.expired = expired;
	}

	/**
	 * Check to see if the propUsedTime is still less than
	 * propIndate. If it is true, add one to the propUsedTime
	 * and return true.
	 */
	public boolean addPropUsedTime() {
		if ( this.propUsedTime < this.propIndate ) {
			this.propUsedTime++;
			return true;
		}
		return false;
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
	
	/**
	 * Add 1 to the propdata
	 */
	public void addCount() {
		this.count++;
	}
	
	/**
	 * Remove 1 from the propdata.
	 */
	public void subCount() {
		this.count--;
	}

	/**
	 * @return the pew
	 */
	public int getPew() {
		return pew;
	}

	/**
	 * @param pew the pew to set
	 */
	public void setPew(int pew) {
		this.pew = pew;
	}

	/**
	 * @return the bloodLev
	 */
	public int getBloodLev() {
		return bloodLev;
	}

	/**
	 * @param bloodLev the bloodLev to set
	 */
	public void setBloodLev(int bloodLev) {
		this.bloodLev = bloodLev;
	}

	/**
	 * @return the bloodPercent
	 */
	public int getBloodPercent() {
		return bloodPercent;
	}

	/**
	 * @param bloodPercent the bloodPercent to set
	 */
	public void setBloodPercent(int bloodPercent) {
		this.bloodPercent = bloodPercent;
	}

	/**
	 * @return the thewLev
	 */
	public int getThewLev() {
		return thewLev;
	}

	/**
	 * @param thewLev the thewLev to set
	 */
	public void setThewLev(int thewLev) {
		this.thewLev = thewLev;
	}

	/**
	 * @return the damageLev
	 */
	public int getDamageLev() {
		return damageLev;
	}

	/**
	 * @param damageLev the damageLev to set
	 */
	public void setDamageLev(int damageLev) {
		this.damageLev = damageLev;
	}

	/**
	 * @return the skinLev
	 */
	public int getSkinLev() {
		return skinLev;
	}

	/**
	 * @param skinLev the skinLev to set
	 */
	public void setSkinLev(int skinLev) {
		this.skinLev = skinLev;
	}

	/**
	 * @return the power
	 */
	public int getPower() {
		return power;
	}

	/**
	 * @param power the power to set
	 */
	public void setPower(int power) {
		this.power = power;
	}

	/**
	 * @return the userLevel
	 */
	public int getUserLevel() {
		return userLevel;
	}

	/**
	 * @param userLevel the userLevel to set
	 */
	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}

	/**
	 * Get the proper pojo for this itemid.
	 * Maybe a WeaponPojo or ItemPojo.
	 * It's a lazy get method.
	 * 
	 * @return a pojo according to itemId or null.
	 */
	public Pojo getPojo() {
		if ( pojo == null ) {
			if ( isWeapon) {
				pojo = EquipManager.getInstance().getWeaponById(itemId);
			} else {
				pojo = ItemManager.getInstance().getItemById(itemId);
			}
		}
		if ( pojo == null ) {
			pojo = GameContext.getInstance().getEquipManager().getWeaponById(String.valueOf(itemId));	
		}
		return pojo;
	}
	
	/**
	 * Replace the pojo object.
	 * @param newPojo
	 */
	public void setPojo(Pojo newPojo) {
		this.pojo = newPojo;
	}
	
	/**
	 * @return the enhanceMap
	 */
	public HashMap<PropDataEnhanceType, HashMap<PropDataEnhanceField, Integer>> getEnhanceMap() {
		return enhanceMap;
	}

	/**
	 * @return the isWeapon
	 */
	public boolean isWeapon() {
		return isWeapon;
	}

	/**
	 * @param isWeapon the isWeapon to set
	 */
	public void setWeapon(boolean isWeapon) {
		this.isWeapon = isWeapon;
	}

	/**
	 * @param enhanceMap the enhanceMap to set
	 */
	public void setEnhanceMap(
			HashMap<PropDataEnhanceType, HashMap<PropDataEnhanceField, Integer>> enhanceMap) {
		this.enhanceMap = enhanceMap;
	}
	
	/**
	 * Set an enhance value for this propData and given type & field.
	 * @param type
	 * @param field
	 * @param value
	 */
	public void setEnhanceValue(PropDataEnhanceType type, PropDataEnhanceField field, int value) {
		HashMap<PropDataEnhanceField, Integer> map = this.enhanceMap.get(type);
		if ( map == null ) {
			map = new HashMap<PropDataEnhanceField, Integer>();
			this.enhanceMap.put(type, map);
		}
		map.put(field, value);
	}
	
	/**
	 * Get the enhance value for this propData. If it does not exist, return 0
	 * @param type
	 * @param field
	 * @return
	 */
	public int getEnhanceValue(PropDataEnhanceType type, PropDataEnhanceField field) {
		int value = 0;
		HashMap<PropDataEnhanceField, Integer> map = this.enhanceMap.get(type);
		if ( map == null ) {
			map = this.enhanceMap.get(type.toString());
			if ( map != null ) {
				this.enhanceMap.remove(type.toString());
				this.enhanceMap.put(type, map);
			}
		}
		if ( map != null ) {
			Integer v = map.get(field);
			if ( v == null ) {
				v = map.get(field.toString());
				if ( v != null ) {
					map.remove(field.toString());
					map.put(field, v);
				}
			}
			if ( v != null ) {
				value = v.intValue();
			}
		}
		return value;
	}
	
	/**
	 * Get the current slot's count
	 * @return
	 */
	public int getSlotCurrentCount() {
		int count = 0;
		for ( PropDataSlot slot : slots ) {
			if ( slot.getStoneLevel() > 0 ) {
				count++;
			}
		}
		return count;
	}
	
	public void addNewSlot(PropDataSlot slot) {
		this.slots.add(slot);
	}
	
	/**
	 * 获得所有插槽中空白的插槽
	 * @param field
	 * @return
	 */
	public PropDataSlot getGivenSlot(PropDataEnhanceField field) {
		return getGivenSlot(field, Integer.MAX_VALUE);
	}
	
	/**
	 * 获得比指定的石头等级低的插槽
	 * @return
	 */
	public PropDataSlot getGivenSlot(PropDataEnhanceField field, int stoneLevel) {
		PropDataSlot givenSlot = null;
		int leastLevel = stoneLevel;
		/**
		 * Find the empty slot first.
		 */
		for ( PropDataSlot slot : slots ) {
			if ( slot.getStoneId() == null && slot.canEmbedField(field) ) {
				givenSlot = slot;
				break;
			}
		}
		/**
		 * If there are no empty slots, find the first available one.
		 */
		if ( givenSlot == null ) {
			for ( PropDataSlot slot : slots ) {
				if ( slot.getSlotType() == field ) {
					if ( leastLevel >= slot.getStoneLevel() ) {
						leastLevel = slot.getStoneLevel();
						givenSlot = slot;
						break;
					}
				}
			}
		}
		return givenSlot;
	}
	
	/**
	 * 获得所有插槽中属性值的和
	 * 
	 * @param field
	 * @return
	 */
	public int getSlotTotalValue(PropDataEnhanceField field) {
		int totalValue = 0;
		for ( PropDataSlot slot : slots ) {
			if ( slot.getSlotType() == field ) {
				totalValue += slot.getValue();
			}
		}
		return totalValue;
	}
	
	/**
	 * Get all the slots
	 * @return
	 */
	public Collection<PropDataSlot> getSlots() {
		return slots;
	}
	
	/**
	 * Set the slots.
	 * @param slots
	 */
	public void setSlots(Collection<PropDataSlot> slots) {
		this.slots.clear();
		this.slots.addAll(slots);
	}
	
	/**
	 * @return the baseAttack
	 */
	public int getBaseAttack() {
		return baseAttack;
	}

	/**
	 * @param baseAttack the baseAttack to set
	 */
	public void setBaseAttack(int baseAttack) {
		this.baseAttack = baseAttack;
	}

	/**
	 * @return the baseDefend
	 */
	public int getBaseDefend() {
		return baseDefend;
	}

	/**
	 * @param baseDefend the baseDefend to set
	 */
	public void setBaseDefend(int baseDefend) {
		this.baseDefend = baseDefend;
	}

	/**
	 * @return the baseAgility
	 */
	public int getBaseAgility() {
		return baseAgility;
	}

	/**
	 * @param baseAgility the baseAgility to set
	 */
	public void setBaseAgility(int baseAgility) {
		this.baseAgility = baseAgility;
	}

	/**
	 * @return the baseLuck
	 */
	public int getBaseLuck() {
		return baseLuck;
	}

	/**
	 * @param baseLuck the baseLuck to set
	 */
	public void setBaseLuck(int baseLuck) {
		this.baseLuck = baseLuck;
	}

	/**
	 * @return the basePower
	 */
	public int getBasePower() {
		return basePower;
	}

	/**
	 * @param basePower the basePower to set
	 */
	public void setBasePower(int basePower) {
		this.basePower = basePower;
	}

	/**
	 * The warrantMillis is the final timeout millis
	 * @return the warrantMillis
	 */
	public long getWarrantMillis() {
		return warrantMillis;
	}

	/**
	 * The warrantMillis is the final timeout millis
	 * @param warrantMillis the warrantMillis to set
	 */
	public void setWarrantMillis(long warrantMillis) {
		this.warrantMillis = warrantMillis;
	}

	/**
	 * @return the warrantDateLimit
	 */
	public int getWarrantDateLimit() {
		return warrantDateLimit;
	}

	/**
	 * @param warrantDateLimit the warrantDateLimit to set
	 */
	public void setWarrantDateLimit(int warrantDateLimit) {
		this.warrantDateLimit = warrantDateLimit;
	}

	/**
	 * @return the warrantDateKey
	 */
	public String getWarrantDateKey() {
		return warrantDateKey;
	}

	/**
	 * @param warrantDateKey the warrantDateKey to set
	 */
	public void setWarrantDateKey(String warrantDateKey) {
		this.warrantDateKey = warrantDateKey;
	}

	/**
	 * Test if the enhance type has been done
	 * 
	 * @param type
	 * @return
	 */
	public boolean containEnhanceValue(PropDataEnhanceType type) {
		return this.enhanceMap.containsKey(type);
	}

	/**
	 * @return the lengthenTimes
	 */
	public int getLengthenTimes() {
		return lengthenTimes;
	}

	/**
	 * @param lengthenTimes the lengthenTimes to set
	 */
	public void setLengthenTimes(int lengthenTimes) {
		this.lengthenTimes = lengthenTimes;
	}

	/**
	 * @return the addTimestamp
	 */
	public long getAddTimestamp() {
		return addTimestamp;
	}

	/**
	 * @return the diamond
	 */
	public int getCrystal() {
		return crystal;
	}

	/**
	 * @param crystal the diamond to set
	 */
	public void setCrystal(int crystal) {
		this.crystal = crystal;
	}

	/**
	 * @param addTimestamp the addTimestamp to set
	 */
	public void setAddTimestamp(long addTimestamp) {
		this.addTimestamp = addTimestamp;
	}

	/**
	 * @return the bandUserName
	 */
	public String getBandUserName() {
		return bandUserName;
	}

	/**
	 * @param bandUserName the bandUserName to set
	 */
	public void setBandUserName(String bandUserName) {
		this.bandUserName = bandUserName;
	}

	/**
	 * @return the itemMap
	 */
	public HashMap<String, Integer> getItemMap() {
		return itemMap;
	}

	/**
	 * @param itemMap the itemMap to set
	 */
	public void setItemMap(HashMap<String, Integer> itemMap) {
		this.itemMap = itemMap;
	}
	
	/**
	 * Add item into this propData.
	 * @param itemId
	 * @param count
	 */
	public void addItem(String itemId, int count) {
		if ( itemId == null || count <= 0) return;
		Integer alreadyCount = this.itemMap.get(itemId);
		if ( alreadyCount != null ) {
			this.itemMap.put(itemId, alreadyCount.intValue()+count);
		} else {
			this.itemMap.put(itemId, count);
		}
	}

	/**
	 * @return the maxLevel
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @param maxLevel the maxLevel to set
	 */
	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	/**
	 * @return the reward
	 */
	public boolean isReward() {
		return reward;
	}

	/**
	 * @param reward the reward to set
	 */
	public void setReward(boolean reward) {
		this.reward = reward;
	}

	/**
	 * @return the slot
	 */
	public int getTotalSlot() {
		return slots.size();
	}

	/**
	 * @return the totalGolden
	 */
	public int getTotalGolden() {
		return totalGolden;
	}

	/**
	 * @param totalGolden the totalGolden to set
	 */
	public void setTotalGolden(int totalGolden) {
		this.totalGolden = totalGolden;
	}

	/**
	 * @return the skill
	 */
	public int getSkill() {
		return skill;
	}

	/**
	 * @param skill the skill to set
	 */
	public void setSkill(int skill) {
		this.skill = skill;
	}

	/**
	 * Clone the propdata.
	 */
	@Override
	public PropData clone() {
		PropData propData = new PropData();
		propData.itemId=this.itemId;
		propData.name=this.name;
		propData.propIndate=this.propIndate;
		propData.propUsedTime=this.propUsedTime;
		propData.warrantMillis=this.warrantMillis;
		propData.warrantDateLimit=this.warrantDateLimit;
		propData.warrantDateKey=this.warrantDateKey;
		propData.count=this.count;
		propData.level=this.level;
		propData.userLevel=this.userLevel;
		propData.attackLev=this.attackLev;
		propData.defendLev=this.defendLev;
		propData.agilityLev=this.agilityLev;
		propData.luckLev=this.luckLev;
		propData.bloodLev=this.bloodLev;
		propData.bloodPercent=this.bloodPercent;
		propData.thewLev=this.thewLev;
		propData.damageLev=this.damageLev;
		propData.skinLev=this.skinLev;
		propData.sign=this.sign;
		propData.valuetype=this.valuetype;
		propData.banded=this.banded;
		propData.power=this.power;
		propData.duration=this.duration;
		propData.isWeapon=this.isWeapon;
		propData.pew=this.pew;
		propData.weaponColor=this.weaponColor;
		propData.pojo=this.pojo;
		propData.expired=this.expired;
		propData.lengthenTimes=this.lengthenTimes;
		propData.baseAttack=this.baseAttack;
		propData.baseDefend=this.baseDefend;
		propData.baseAgility=this.baseAgility;
		propData.baseLuck=this.baseLuck;
		propData.basePower=this.basePower;
		propData.maxLevel = this.maxLevel;
		propData.skill = this.skill;
		propData.totalGolden = this.totalGolden;
		propData.crystal = this.crystal;
		propData.banded = this.banded;
		propData.bandUserName = this.bandUserName;

		propData.enhanceMap = 
				new HashMap<PropDataEnhanceType, HashMap<PropDataEnhanceField, Integer>>();
		for (Iterator iter = this.enhanceMap.keySet().iterator(); iter.hasNext();) {
			Object typeObj = (Object) iter.next();
			PropDataEnhanceType type = null;
			if ( typeObj instanceof String ) {
				type = PropDataEnhanceType.valueOf(typeObj.toString());
			} else {
				type = (PropDataEnhanceType)typeObj;
			}
			HashMap<PropDataEnhanceField, Integer> omap = this.enhanceMap.get(type);
			if ( omap != null ) {
				HashMap<PropDataEnhanceField, Integer> map = new HashMap<PropDataEnhanceField, Integer>();
				propData.enhanceMap.put(type, map);
				for (Iterator fieldIter = omap.keySet().iterator(); fieldIter.hasNext();) {
					Object fieldObj = (Object) fieldIter.next();
					PropDataEnhanceField field = null;
					if ( fieldObj instanceof String ) {
						field = PropDataEnhanceField.valueOf(fieldObj.toString());
					} else {
						if ( fieldObj instanceof PropDataEnhanceType.Field ) {
							PropDataEnhanceType.Field oldField = (PropDataEnhanceType.Field)fieldObj;
							field = PropDataEnhanceField.values()[oldField.ordinal()];
						} else {
							field = (PropDataEnhanceField)fieldObj;
						}
					}
					map.put(field, omap.get(field));
				}
			}
		}
		propData.slots.clear();
		for ( PropDataSlot slot : this.getSlots() ) {
			propData.slots.add(slot.clone());
		}
		return propData;
	}
	
	/**
	 * Clone the propdata.
	 */
	public void copyFrom(PropData propData) {
		this.itemId=propData.itemId;
		this.name=propData.name;
		this.propIndate=propData.propIndate;
		this.propUsedTime=propData.propUsedTime;
		this.warrantMillis=propData.warrantMillis;
		this.warrantDateLimit=propData.warrantDateLimit;
		this.warrantDateKey=propData.warrantDateKey;
		this.count=propData.count;
		this.level=propData.level;
		this.userLevel=propData.userLevel;
		this.attackLev=propData.attackLev;
		this.defendLev=propData.defendLev;
		this.agilityLev=propData.agilityLev;
		this.luckLev=propData.luckLev;
		this.bloodLev=propData.bloodLev;
		this.bloodPercent=propData.bloodPercent;
		this.thewLev=propData.thewLev;
		this.damageLev=propData.damageLev;
		this.skinLev=propData.skinLev;
		this.sign=propData.sign;
		this.valuetype=propData.valuetype;
		this.banded=propData.banded;
		this.power=propData.power;
		this.duration=propData.duration;
		this.isWeapon=propData.isWeapon;
		this.pew=propData.pew;
		this.weaponColor=propData.weaponColor;
		this.pojo=propData.pojo;
		this.expired=propData.expired;
		this.lengthenTimes=propData.lengthenTimes;
		this.baseAttack=propData.baseAttack;
		this.baseDefend=propData.baseDefend;
		this.baseAgility=propData.baseAgility;
		this.baseLuck=propData.baseLuck;
		this.basePower=propData.basePower;
		this.maxLevel = propData.maxLevel;
		this.skill = propData.skill;
		this.totalGolden = propData.totalGolden;
		this.crystal = propData.crystal;
		this.banded = propData.banded;
		this.bandUserName = propData.bandUserName;
		
		this.enhanceMap = 
				new HashMap<PropDataEnhanceType, HashMap<PropDataEnhanceField, Integer>>();
		for ( PropDataEnhanceType type : propData.enhanceMap.keySet() ) {
			HashMap<PropDataEnhanceField, Integer> omap = propData.enhanceMap.get(type);
			if ( omap != null ) {
				HashMap<PropDataEnhanceField, Integer> map = new HashMap<PropDataEnhanceField, Integer>();
				this.enhanceMap.put(type, map);
				for ( PropDataEnhanceField field : omap.keySet() ) {
					map.put(field, omap.get(field));
				}
			}
		}
		this.slots.clear();
		for ( PropDataSlot slot : propData.getSlots() ) {
			this.slots.add(slot.clone());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (addTimestamp ^ (addTimestamp >>> 32));
		result = prime * result + agilityLev;
		result = prime * result + attackLev;
		result = prime * result
				+ ((bandUserName == null) ? 0 : bandUserName.hashCode());
		result = prime * result + (banded ? 1231 : 1237);
		result = prime * result + baseAgility;
		result = prime * result + baseAttack;
		result = prime * result + baseDefend;
		result = prime * result + baseLuck;
		result = prime * result + basePower;
		result = prime * result + bloodLev;
		result = prime * result + bloodPercent;
		result = prime * result + count;
		result = prime * result + damageLev;
		result = prime * result + defendLev;
		result = prime * result
				+ ((enhanceMap == null) ? 0 : enhanceMap.hashCode());
		result = prime * result + (isWeapon ? 1231 : 1237);
		result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
		result = prime * result + ((itemMap == null) ? 0 : itemMap.hashCode());
		result = prime * result + lengthenTimes;
		result = prime * result + level;
		result = prime * result + luckLev;
		result = prime * result + maxLevel;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + power;
		result = prime * result + propIndate;
		result = prime * result + propUsedTime;
		result = prime * result + sign;
		result = prime * result + skill;
		result = prime * result + skinLev;
		result = prime * result + ((slots == null) ? 0 : slots.hashCode());
		result = prime * result + thewLev;
		result = prime * result + totalGolden;
		result = prime * result + userLevel;
		result = prime * result + ((valuetype == null) ? 0 : valuetype.hashCode());
		result = prime * result
				+ ((warrantDateKey == null) ? 0 : warrantDateKey.hashCode());
		result = prime * result + warrantDateLimit;
		result = prime * result + (int) (warrantMillis ^ (warrantMillis >>> 32));
		result = prime * result
				+ ((weaponColor == null) ? 0 : weaponColor.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropData other = (PropData) obj;
		if (addTimestamp != other.addTimestamp)
			return false;
		if (agilityLev != other.agilityLev)
			return false;
		if (attackLev != other.attackLev)
			return false;
		if (bandUserName == null) {
			if (other.bandUserName != null)
				return false;
		} else if (!bandUserName.equals(other.bandUserName))
			return false;
		if (banded != other.banded)
			return false;
		if (baseAgility != other.baseAgility)
			return false;
		if (baseAttack != other.baseAttack)
			return false;
		if (baseDefend != other.baseDefend)
			return false;
		if (baseLuck != other.baseLuck)
			return false;
		if (basePower != other.basePower)
			return false;
		if (bloodLev != other.bloodLev)
			return false;
		if (bloodPercent != other.bloodPercent)
			return false;
		if (count != other.count)
			return false;
		if (damageLev != other.damageLev)
			return false;
		if (defendLev != other.defendLev)
			return false;
		if (enhanceMap == null) {
			if (other.enhanceMap != null)
				return false;
		} else if (!enhanceMap.equals(other.enhanceMap))
			return false;
		if (isWeapon != other.isWeapon)
			return false;
		if (itemId == null) {
			if (other.itemId != null)
				return false;
		} else if (!itemId.equals(other.itemId))
			return false;
		if (itemMap == null) {
			if (other.itemMap != null)
				return false;
		} else if (!itemMap.equals(other.itemMap))
			return false;
		if (lengthenTimes != other.lengthenTimes)
			return false;
		if (level != other.level)
			return false;
		if (luckLev != other.luckLev)
			return false;
		if (maxLevel != other.maxLevel)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (power != other.power)
			return false;
		if (propIndate != other.propIndate)
			return false;
		if (propUsedTime != other.propUsedTime)
			return false;
		if (sign != other.sign)
			return false;
		if (skill != other.skill)
			return false;
		if (skinLev != other.skinLev)
			return false;
		if (slots == null) {
			if (other.slots != null)
				return false;
		} else if (!slots.equals(other.slots))
			return false;
		if (thewLev != other.thewLev)
			return false;
		if (totalGolden != other.totalGolden)
			return false;
		if (userLevel != other.userLevel)
			return false;
		if (valuetype != other.valuetype)
			return false;
		if (warrantDateKey == null) {
			if (other.warrantDateKey != null)
				return false;
		} else if (!warrantDateKey.equals(other.warrantDateKey))
			return false;
		if (warrantDateLimit != other.warrantDateLimit)
			return false;
		if (warrantMillis != other.warrantMillis)
			return false;
		if (weaponColor != other.weaponColor)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(itemId).append(",");
		builder.append(name).append(",");
		builder.append(pew).append(",");
		builder.append(weaponColor).append(",");
		builder.append(level);
		return builder.toString();
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toDetailString() {
		StringBuilder builder = new StringBuilder();
		builder.append("itemId \t ");
		builder.append(itemId);
		builder.append(" \t name \t ");
		builder.append(name);
		builder.append(" \t propIndate \t ");
		builder.append(propIndate);
		builder.append(" \t propUsedTime \t ");
		builder.append(propUsedTime);
		builder.append(" \t count \t ");
		builder.append(count);
		builder.append(" \t level \t ");
		builder.append(level);
		builder.append(" \t attackLev \t ");
		builder.append(attackLev);
		builder.append(" \t defendLev \t ");
		builder.append(defendLev);
		builder.append(" \t agilityLev \t ");
		builder.append(agilityLev);
		builder.append(" \t luckLev \t ");
		builder.append(luckLev);
		builder.append(" \t bloodLev \t ");
		builder.append(bloodLev);
		builder.append(" \t bloodPercent \t ");
		builder.append(bloodPercent);
		builder.append(" \t thewLev \t ");
		builder.append(thewLev);
		builder.append(" \t damageLev \t ");
		builder.append(damageLev);
		builder.append(" \t skinLev \t ");
		builder.append(skinLev);
		builder.append(" \t sign \t ");
		builder.append(sign);
		builder.append(" \t valuetype \t ");
		builder.append(valuetype);
		builder.append(" \t banded \t ");
		builder.append(banded);
		builder.append(" \t duration \t ");
		builder.append(duration);
		builder.append(" \t color \t ");
		builder.append(weaponColor);
		builder.append(" \t power \t ");
		builder.append(power);
		builder.append(" \t map \t ");
		builder.append(enhanceMap);
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PropData o) {
		if ( o == null ) {
			return -1;
		} else {
			int result = 1;
			boolean hasWeapon = true;
			Pojo thisPojo = this.getPojo();
			if ( thisPojo == null ) {
				return 1;
			}
			Pojo thatPojo = o.getPojo();
			if ( thatPojo == null ) {
				return -1;
			}
			if ( thisPojo instanceof ItemPojo && thatPojo instanceof ItemPojo ) {
				result = this.name.compareTo(o.name);
				hasWeapon = false;
			} else if ( thisPojo instanceof ItemPojo && thatPojo instanceof WeaponPojo ) {
				return 1;
			} else if ( thisPojo instanceof WeaponPojo && thatPojo instanceof ItemPojo ) {
				return -1;
			} else {
				WeaponPojo thisWeapon = (WeaponPojo)thisPojo;
				WeaponPojo thatWeapon = (WeaponPojo)thatPojo;
				if ( thisWeapon != null && thatWeapon != null ) {
					result = thisWeapon.getTypeName().compareTo(thatWeapon.getTypeName());
				}
			}
			if ( result == 0 ) {
				if ( hasWeapon ) {
					result = (int)(this.addTimestamp - o.addTimestamp);
					if ( result == 0 ) {
						return -1;
					}
				} else {
					if ( this.equals(o) ) {
						return 0;
					} else {
						return 1;
					}
				}
			}
			return result;
		}
	}

	/**
	 * 
	 * @return
	 */
	public XinqiPropData.PropData toXinqiPropData() {
		return toXinqiPropData(null, null);
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public XinqiPropData.PropData toXinqiPropData(User user) {
		return toXinqiPropData(user, null);
	}
	/**
	 * Convert this PropData to protobuf object.
	 * @param abtestKey The A/B test key for this PropData. The User object has this field.
	 * 	It can be null if not accessible.
	 * 
	 * @return
	 */
	public XinqiPropData.PropData toXinqiPropData(User user, String guildBagId) {
		XinqiPropData.PropData.Builder builder = XinqiPropData.PropData.newBuilder();
    builder.setPropID(this.getItemId());
    builder.setPropPew(this.getPew());
    builder.setPropIndate(this.getPropUsedTime());
    builder.setLevel(this.getLevel());
  	int damage = getEnhanceValue(PropDataEnhanceType.STRENGTH, 
  			PropDataEnhanceField.DAMAGE);
  	builder.setDamageLev(damage);
  	int skin = getEnhanceValue(PropDataEnhanceType.STRENGTH, 
  			PropDataEnhanceField.SKIN);
  	builder.setSkinLev(skin);
  	if ( this.weaponColor == null ) {
  		builder.setColor(WeaponColor.WHITE.toIntColor());
  	} else {
  		builder.setColor(this.weaponColor.toIntColor());
  	}

  	if ( isWeapon ) {
  		/**
  		 * TODO
  		 * Add four extra fields to display base data.
  		 */
  		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(itemId);
      if ( weapon != null ) {
      	int attack = attackLev - baseAttack;
        builder.setAttackLev(attack);
        int defend = defendLev - baseDefend;
        builder.setDefendLev(defend);
        int agility = agilityLev - baseAgility;
        builder.setAgilityLev(agility);
        int luck = luckLev - baseLuck;
        builder.setLuckLev(luck);
        int addPower = power - basePower;
        if ( addPower < 0 ) {
        	addPower = 0;
        }
        builder.setPower(addPower);
      }
    } else {
    	builder.setAttackLev(0);
    	builder.setDefendLev(0);
    	builder.setAgilityLev(0);
    	builder.setLuckLev(0);
    	builder.setPower(0);
    }
  	builder.setBaseAttack(baseAttack);
  	builder.setBaseDefend(baseDefend);
  	builder.setBaseAgility(baseAgility);
  	builder.setBaseLucky(baseLuck);
  	builder.setBasePower(basePower);
  	if ( !isWeapon ) {
  		ItemPojo pojo = GameContext.getInstance().getItemManager().getItemById(itemId);
  		if ( pojo != null && pojo.getTypeId() != null) {
  			builder.setTypeid(pojo.getTypeId());
  		}
  	}
    builder.setSign(this.getSign());
    builder.setCount(this.getCount());
    if ( guildBagId != null ) {
    	builder.setId(guildBagId);
    } else {
	    if ( user != null ) {
	    	builder.setId(user.get_id().toString());
	    }
    }
    builder.setValuetype(this.getValuetype().ordinal());
    builder.setBanded(this.isBanded());
    if ( isExpire() ) {
    	builder.setDuration(0);
    } else {
    	int percent = Math.round( (this.propIndate - this.propUsedTime) * 100.0f / this.propIndate);
    	//Only the isExpire field is true, the duration should be 0
    	if ( percent <= 0 ) {
    		percent = 1;
    	}
    	builder.setDuration(percent);
    }
		return builder.build();
	}
	
	/**
	 * Convert this PropData to protobuf object.
	 * @param abtestKey The A/B test key for this PropData. The User object has this field.
	 * 	It can be null if not accessible.
	 * 
	 * @return
	 */
	public PropDataDesc toXinqiPropDataDesc(User user) {
		PropDataDesc.Builder builder = PropDataDesc.newBuilder();
		builder.setId(itemId);
		if ( weaponColor == null ) weaponColor = WeaponColor.WHITE;
		builder.setColor(weaponColor.toIntColor());
    /**
     * 基础属性
     */
    //builder.addDesc(SEP);
    if ( isWeapon ) {
  		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(itemId);
      if ( weapon != null ) {
        //可强化最大等级
      	builder.setQuality(weapon.getQuality());
      	builder.addDesc(processString("maxlevel", this.maxLevel, TextColor.GRAY));
        if ( this.isBanded() ) {
        	builder.addDesc(processString("binded", null, TextColor.RED));
        } else {
        	builder.addDesc(processString("unbind", null, TextColor.GREEN));
        }
      	//强化等级
      	builder.setLevel(this.getLevel());
      	if ( this.userLevel > user.getLevel() ) {
      		builder.addDesc(processString("level", weapon.getUserLevel(), TextColor.RED));
      	} else if ( this.userLevel > 0 ) {
      		builder.addDesc(processString("level", weapon.getUserLevel(), TextColor.GREEN));
      	}
      	if ( weapon.getSex() == Gender.ALL || weapon.getSex() == user.getGender() ) {
      		builder.addDesc(processString("gender", weapon.getSex().getTitle()));
      	} else {
      		builder.addDesc(processString("gender", weapon.getSex().getTitle(), TextColor.RED));
      	}
        if ( isExpire() ) {
        	builder.addDesc(processString("duration", "0%", TextColor.RED));
        } else {
        	int percent = Math.round( (this.propIndate - this.propUsedTime) * 100.0f / this.propIndate);
        	//Only the isExpire field is true, the duration should be 0
        	if ( percent <= 0 ) {
        		percent = 1;
        	}
        	builder.addDesc(processString("duration", percent, "%", TextColor.WHITE));
        }
      	builder.setIcon(weapon.getIcon());
      	builder.setName(StringUtil.concat(this.weaponColor.
      			toColorString().concat(weapon.getName())));
      	
      	builder.addDesc(processString("type", weapon.getSlot().getTitle()));
      	//Override the default quality
      	QualityType quality = QualityType.values()[weapon.getQuality()];
      	builder.addDesc(processString("quality", quality.getTitle()));
      	
      	if ( weapon.getSlot() == EquipType.WEAPON ) {
      		builder.addDesc(processString("radius", weapon.getRadius()));
      		builder.addDesc(processString("sradius", weapon.getsRadius()));
      	}
      	/**
      	 * 四个基础属性的数值
      	 */
      	builder.addDesc(SEP);
      	builder.addDesc(processAbilityStr(user, weapon, PropDataEnhanceField.ATTACK, 
      			this.attackLev, this.baseAttack));
      	builder.addDesc(processAbilityStr(user, weapon, PropDataEnhanceField.DEFEND, 
      			this.defendLev, this.baseDefend));
      	builder.addDesc(processAbilityStr(user, weapon, PropDataEnhanceField.AGILITY, 
      			this.agilityLev, this.baseAgility));
      	builder.addDesc(processAbilityStr(user, weapon, PropDataEnhanceField.LUCKY, 
      			this.luckLev, this.baseLuck));
      	
        /**
         * 强化数值输出
         */
      	int damage = getEnhanceValue(PropDataEnhanceType.STRENGTH, 
      			PropDataEnhanceField.DAMAGE);
      	int skin = getEnhanceValue(PropDataEnhanceType.STRENGTH, 
      			PropDataEnhanceField.SKIN);
      	if ( damage > 0 ) {
      		builder.addDesc(SEP);
      		builder.addDesc(processString("damage", damage, TextColor.YELLOW));
      	}
      	if ( skin > 0 ) {
      		builder.addDesc(processString("skin", skin, TextColor.YELLOW));
      	}
        //战斗力
      	builder.addDesc(processString("power", this.power, TextColor.YELLOW));

      	//最大插槽数
      	builder.addDesc(SEP);
      	if ( this.getTotalSlot() <= 0 ) {
      		builder.addDesc(processString("noslot", null, TextColor.GRAY));
      	} else {
      		builder.addDesc(processString("slot", 
      				StringUtil.concat(getSlotCurrentCount(), "/", this.getTotalSlot())));
      		if ( !reward ) {
	      		for ( PropDataSlot slot : slots ) {
	      			builder.addDesc(slot.getDesc());
	      		}
      		} else {
      			builder.addDesc(processString("slot.pending", null, TextColor.GRAY));		
      		}
      	}
        /**
         * 合成数值输出
         */
      	/*
      	builder.addDesc(SEP);
      	int addedAttack = getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.ATTACK);
      	int addedDefend = getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.DEFEND);
      	int addedAgility = getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.AGILITY);
      	int addedLucky = getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.LUCKY);
      	if ( addedAttack > 0 || addedDefend>0 || addedAgility>0 || addedLucky>0 ) {
      		builder.addDesc(SEP);
      		if ( addedAttack > 0 ) 
      			builder.addDesc(processString("attack", addedAttack, TextColor.BLUE));
      		if ( addedDefend > 0 )
      			builder.addDesc(processString("defend", addedDefend, TextColor.BLUE));
      		if ( addedAgility > 0 )
      			builder.addDesc(processString("agility", addedAgility, TextColor.BLUE));
      		if ( addedLucky > 0 )
      			builder.addDesc(processString("lucky", addedLucky, TextColor.BLUE));
      	}
      	*/
      	/**
      	 * 装备升级数量
      	 */
      	int count = ScriptManager.getInstance().runScriptForInt(ScriptHook.CRAFT_CAL_DIAMOND, user, this);
      	if ( count > 0 ) {
      		builder.addDesc(SEP);
      		String level = Text.text(StringUtil.concat("weapon.level.", userLevel+10));
      		String desc = Text.text("propdata.diamond", level, count);
      		String desc2 = Text.text("propdata.diamond.embed", crystal);
      		builder.addDesc(desc);
      		builder.addDesc(desc2);
      	}
      }
      builder.addDesc(SEP);
      builder.addDesc(processString("golden.payed", totalGolden));
      builder.addDesc(processString("rare", 
      		Math.round(EquipCalculator.calculatePropDataRareRatio(this)*1000000)));
      
      builder.addDesc(SEP);
      builder.addDesc(TextColor.YELLOW.makeColor(weapon.getInfo()));
    } else {
      if ( this.getCount() > 1 ) {
      	builder.addDesc(processString("count", this.getCount()));
      }
    	ItemPojo item = ItemManager.getInstance().getItemById(itemId);
    	if ( item != null ) {
    		builder.setIcon(item.getIcon());
    		if ( this.level < 2 ) { 
    			builder.setName(item.getName());
    		} else if ( this.level < 3 ) {
    			builder.setName(TextColor.GREEN.makeColor(item.getName()));
    		} else if ( this.level < 4 ) {
    			builder.setName(TextColor.CYAN.makeColor(item.getName()));
    		} else if ( this.level < 5 ) {
    			builder.setName(TextColor.ORANGE.makeColor(item.getName()));
    		} else {
    			builder.setName(TextColor.PURPLE.makeColor(item.getName()));
    		}
	    	builder.addDesc(TextColor.YELLOW.makeColor(item.getInfo()));
    	}
    }
    int goldenPrice = ShopManager.getInstance().findPriceForItemInBag(user, this);
    builder.addDesc(SEP);
    builder.addDesc(TextColor.ORANGE.makeColor(Text.text("propdata.golden.price", goldenPrice)));
		return builder.build();
	}

	/**
	 * @param user
	 * @param builder
	 * @param weapon
	 */
	public String processAbilityStr(User user, WeaponPojo weapon, PropDataEnhanceField field, 
			int ability, int baseAbility) {
		PropData wearedPropData = null;
		if ( pew>=Bag.BAG_WEAR_COUNT ) {
			EquipType slot = weapon.getSlot();
			PropDataEquipIndex[] indexes = slot.getPropDataEquipIndex();
			if ( indexes != null ) {
				for ( PropDataEquipIndex index : indexes ) {
					PropData pd = user.getBag().getWearPropDatas().get(index.index());
					if ( pd != null ) {
						if ( wearedPropData == null ) {
							wearedPropData = pd;
						} /*
						} else {
							switch (field) {
								case ATTACK:
									if ( wearedPropData.getAttackLev() < pd.getAttackLev() ) {
										wearedPropData = pd;
									}
									break;
								case DEFEND:
									if ( wearedPropData.getDefendLev() < pd.getDefendLev() ) {
										wearedPropData = pd;
									}
									break;
								case AGILITY:
									if ( wearedPropData.getAgilityLev() < pd.getAgilityLev() ) {
										wearedPropData = pd;
									}
									break;
								case LUCKY:
									if ( wearedPropData.getLuckLev() < pd.getLuckLev() ) {
										wearedPropData = pd;
									}
									break;
								default:
									if ( wearedPropData.getPower() < pd.getPower() ) {
										wearedPropData = pd;
									}
									break;
							}
						}
						*/
					}
				}
			}
		}
		String key = "propdata.".concat(field.toString().toLowerCase());
		String value = Text.text(key, baseAbility);
		int mydiff = (ability - baseAbility);
		if ( mydiff > 0 ) {
			value = StringUtil.concat(value, "  (", ability, ") ");
		}
		if ( wearedPropData != null ) {
			int diff = 0;
			switch (field) {
				case ATTACK:
					diff = this.attackLev - wearedPropData.getAttackLev();
					break;
				case DEFEND:
					diff = this.defendLev - wearedPropData.getDefendLev();
					break;
				case AGILITY:
					diff = this.agilityLev - wearedPropData.getAgilityLev();
					break;
				case LUCKY:
					diff = this.luckLev - wearedPropData.getLuckLev();
					break;
				default:
					break;
			}
			/**
			 * ↑	Upwards Arrow	 U+2191
			 * ↓	Downwards Arrow: U+2193
			 * ⇈	Upwards Paired: U+21c8
			 * ⇊	Downwards Paired: U+21ca
			 */
			if ( diff > 0  ) {
				value = TextColor.GREEN.makeColor( StringUtil.concat(value, " \u2191", diff) );
			} else if ( diff < 0 ) {
				value = TextColor.RED.makeColor( StringUtil.concat(value, " \u2193", diff) );
			}
		}
		return value;
	}
	
	/**
	 * Format the given string as localized value.
	 * @param key
	 * @param value
	 * @return
	 */
	private String processString(String key) {
		return processString(key, null);
	}

	/**
	 * Format the given string as localized value.
	 * @param key
	 * @param value
	 * @return
	 */
	private String processString(String key, Object value) {
		return processString(key, value, null, null);
	}

	/**
	 * Format the given string as localized value.
	 * @param key
	 * @param value
	 * @return
	 */
	private String processString(String key, Object value, TextColor color) {
		return processString(key, value, null, color);
	}

	/**
	 * Format the given string as localized value.
	 * @param key
	 * @param value
	 * @return
	 */
	private String processString(String key, Object value, 
			String suffix, TextColor color) {
		String text = null;
		String textKey = "propdata.".concat(key);
		if ( value != null && suffix != null ) {
			text = Text.text(textKey, value).concat(suffix);
		} else if ( value != null ) {
			text = Text.text(textKey, value);
		} else if ( suffix != null ) {
			text = Text.text(textKey).concat(suffix);
		} else {
			text = Text.text(textKey);
		}
		if ( color != null ) {
			return color.makeColor(text);
		}
		return text;
	}

}
