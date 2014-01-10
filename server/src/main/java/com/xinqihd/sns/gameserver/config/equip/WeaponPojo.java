package com.xinqihd.sns.gameserver.config.equip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.GameResourceManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.proto.XinqiBseEquipment;
import com.xinqihd.sns.gameserver.proto.XinqiBseEquipment.WeaponData;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class WeaponPojo implements Pojo, Comparable<WeaponPojo> {
	
	private static final long serialVersionUID = -7667528325595524780L;

	//The item id
	private String _id;

	@Override
	public void setId(String id) {
		this._id = id;
	}

	@Override
	public String getId() {
		return _id;
	}

	// --------------------------------------- Properties
  private int index = 0; 
  /**
   * 1: 普通
   * 2: 精良(元宝武器)
   * 3: 优秀
   * 4: 神圣
   */
  private int quality = 1; 
  
  private WeaponColor qualityColor = WeaponColor.WHITE; 
  
  //Maybe short name
  private String sName = Constant.EMPTY;
  /**
      equip_type = 1:   weapons
      equip_type = 130, offhandweapons
      equip_type = 132: expressions 
      equip_type = 133: faces
      equip_type = 134, decorations 
      equip_type = 135, hair
      equip_type = 136, wing
      equip_type = 137, clothes
      equip_type = 138, hat
      equip_type = 139, glasses
      equip_type = 140, jewels bracelet
      equip_type = 141, jewels finger ring
      equip_type = 142, jewels others
      equip_type = 143, bubble
      equip_type = 146, suit
   * 
   */
  private int equipType = 0; 
  private int addAttack = 0; 
  private int addDefend = 0; 
  private int addAgility = 0; 
  private int addLuck = 0; 
  private int addBlood = 0;
  //0-100. Add the percent of blood 
  private int addBloodPercent = 0;
  //only 0
  private int addThew = 0;
  private int addDamage = 0;
  //0, 60, 65: Maybe the coordinates
  private int addSkin = 0;
  /**
   * 1: female
   * 2: male
   */
  private Gender sex = Gender.NONE; 
  private int unused1 = -1; 
  private int unused2 = 0; 
  private int unused3 = 0;
  //valid hours
  private int indate1 = 0; 
  private int indate2 = 0; 
  private int indate3 = 0;
  /**
   * ???
   * 0:
   * 2: 
   * 3: 
   * 6:
   */
  private int sign = 0;
  //only 0
  private int lv = 1;
  /**
   * 0: disable auto direction
   * 1: enable auto direction
   */
  private int autoDirection = 0;
  /**
   * 0: disable super attack auto direction
   * 1: enable super attack auto direction
   */
  private int sAutoDirection = 0;
  /**
   * 0:
   * 1: 
   */
  private int specialAction = 0; 
  /**
   * 0 - 150: the long radius
   */
  private int radius = 0;
  /**
   * 0 - 150: the short radius
   */
  private int sRadius = 0; 
  /**
   * "":
   * "normal":
   * "add":
   */
  private String expBlend = Constant.EMPTY;
  /**
   * sound effect
   * 0:
   * 1:
   * 2:
   * 3: 
   * 4:
   */
  private int expSe = 0; 
  private int power = 0; 
  //only 0
  private int autoDestory = 0;
  /**
    * "", 
    * "EXBullet_blueRune", 
    * "EXBullet_longinus", 
    * "EXBullet_redRune", 
    * "EXBullet_vivienne", 
    * "bullet_bird", 
    * "bullet_bird_s", 
    * "bullet_black", 
    * "bullet_boss_xiezi_b", 
    * "bullet_boss_xiezi_s", 
    * "bullet_cannon", 
    * "bullet_chainsaw_g", 
    * "bullet_corn", 
    * "bullet_crossbow", 
    * "bullet_fireGun", 
    * "bullet_fist", 
    * "bullet_highHeeled", 
    * "bullet_hp", 
    * "bullet_knife", 
    * "bullet_missile", 
    * "bullet_pumpkin", 
    * "bullet_soap", 
    * "bullet_tomato", 
    * "bullet_white", 
    * "pve1_big_bullet", 
    * "pve1_boss_bullet", 
    * "pve1_magic_bullet", 
    * "pve1_twin1_bullet", 
    * "pve1_twin2_bullet", 
   */
  private String bullet = Constant.EMPTY; 
  /**
   * icon file name
   */
  private String icon = Constant.EMPTY;
  /**
   * normal name
   */
  private String name = Constant.EMPTY;
  /**
   * description
   */
  private String info = Constant.EMPTY;
  /**
   * ""
   */
  private String bubble = Constant.EMPTY;
  /**
    * "bubble", 
    * "clothes", 
    * "decoration", 
    * "expression", 
    * "face", 
    * "glasses", 
    * "hair", 
    * "hat", 
    * "jewelry", 
    * "offhandweapon", 
    * "suit", 
    * "weapon", 
    * "wing", 
   */
  private EquipType slot = EquipType.WEAPON; 
  private ArrayList<Avatar> avatar = null;

  /**
   * This is the common type name for a weapon.
   */
  private String typeName = null;
  /**
   * This is the mininum user level for this weapon.
   */
  private int userLevel = 0;
  
  /**
   * If it is true, this weapon can be used in random reward
   */
  private boolean canBeRewarded = true;
  
  /**
   * The weapon is only used by Boss
   */
  private boolean isUsedByBoss = false;

  public static class Avatar {
  	public String id;
  	/**
	    * "arm_back", 
	    * "arm_front", 
	    * "body", 
	    * "decoration", 
	    * "expression", 
	    * "face", 
	    * "glasses", 
	    * "hair_back", 
	    * "hair_front", 
	    * "hair_high", 
	    * "hair_low", 
	    * "hat", 
	    * "weapon_back", 
	    * "weapon_front", 
	    * "wing_back", 
	    * "wing_front", 
  	 */
  	public String layer = Constant.EMPTY;
  	
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Avatar [id=");
			builder.append(id);
			builder.append(", layer=");
			builder.append(layer);
			builder.append("]");
			return builder.toString();
		}
  	
  }

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the quality
	 */
	public int getQuality() {
		return quality;
	}

	/**
	 * @param quality the quality to set
	 */
	public void setQuality(int quality) {
		this.quality = quality;
	}

	/**
	 * @return the sName
	 */
	public String getsName() {
		return sName;
	}

	/**
	 * @param sName the sName to set
	 */
	public void setsName(String sName) {
		this.sName = sName;
	}

	/**
	 * @return the equipType
	 */
	public int getEquipType() {
		return equipType;
	}

	/**
	 * @param equipType the equipType to set
	 */
	public void setEquipType(int equipType) {
		this.equipType = equipType;
	}

	/**
	 * @return the addAttack
	 */
	public int getAddAttack() {
		return addAttack;
	}

	/**
	 * @param addAttack the addAttack to set
	 */
	public void setAddAttack(int addAttack) {
		this.addAttack = addAttack;
	}

	/**
	 * @return the addDefend
	 */
	public int getAddDefend() {
		return addDefend;
	}

	/**
	 * @param addDefend the addDefend to set
	 */
	public void setAddDefend(int addDefend) {
		this.addDefend = addDefend;
	}

	/**
	 * @return the addAgility
	 */
	public int getAddAgility() {
		return addAgility;
	}

	/**
	 * @param addAgility the addAgility to set
	 */
	public void setAddAgility(int addAgility) {
		this.addAgility = addAgility;
	}

	/**
	 * @return the addLuck
	 */
	public int getAddLuck() {
		return addLuck;
	}

	/**
	 * @param addLuck the addLuck to set
	 */
	public void setAddLuck(int addLuck) {
		this.addLuck = addLuck;
	}

	/**
	 * @return the addBlood
	 */
	public int getAddBlood() {
		return addBlood;
	}

	/**
	 * @param addBlood the addBlood to set
	 */
	public void setAddBlood(int addBlood) {
		this.addBlood = addBlood;
	}

	/**
	 * @return the addBloodPercent
	 */
	public int getAddBloodPercent() {
		return addBloodPercent;
	}

	/**
	 * @param addBloodPercent the addBloodPercent to set
	 */
	public void setAddBloodPercent(int addBloodPercent) {
		this.addBloodPercent = addBloodPercent;
	}

	/**
	 * @return the addThew
	 */
	public int getAddThew() {
		return addThew;
	}

	/**
	 * @param addThew the addThew to set
	 */
	public void setAddThew(int addThew) {
		this.addThew = addThew;
	}

	/**
	 * @return the addDamage
	 */
	public int getAddDamage() {
		return addDamage;
	}

	/**
	 * @param addDamage the addDamage to set
	 */
	public void setAddDamage(int addDamage) {
		this.addDamage = addDamage;
	}

	/**
	 * @return the addSkin
	 */
	public int getAddSkin() {
		return addSkin;
	}

	/**
	 * @param addSkin the addSkin to set
	 */
	public void setAddSkin(int addSkin) {
		this.addSkin = addSkin;
	}

	/**
	 * @return the sex
	 */
	public Gender getSex() {
		return sex;
	}

	/**
	 * @param gender the sex to set
	 */
	public void setSex(Gender gender) {
		this.sex = gender;
	}

	/**
	 * @return the unused1
	 */
	public int getUnused1() {
		return unused1;
	}

	/**
	 * @param unused1 the unused1 to set
	 */
	public void setUnused1(int unused1) {
		this.unused1 = unused1;
	}

	/**
	 * @return the unused2
	 */
	public int getUnused2() {
		return unused2;
	}

	/**
	 * @param unused2 the unused2 to set
	 */
	public void setUnused2(int unused2) {
		this.unused2 = unused2;
	}

	/**
	 * @return the unused3
	 */
	public int getUnused3() {
		return unused3;
	}

	/**
	 * @param unused3 the unused3 to set
	 */
	public void setUnused3(int unused3) {
		this.unused3 = unused3;
	}

	/**
	 * @return the indate1
	 */
	public int getIndate1() {
		return indate1;
	}

	/**
	 * @param indate1 the indate1 to set
	 */
	public void setIndate1(int indate1) {
		this.indate1 = indate1;
	}

	/**
	 * @return the indate2
	 */
	public int getIndate2() {
		return indate2;
	}

	/**
	 * @param indate2 the indate2 to set
	 */
	public void setIndate2(int indate2) {
		this.indate2 = indate2;
	}

	/**
	 * @return the indate3
	 */
	public int getIndate3() {
		return indate3;
	}

	/**
	 * @param indate3 the indate3 to set
	 */
	public void setIndate3(int indate3) {
		this.indate3 = indate3;
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
	 * @return the lv
	 */
	public int getLv() {
		return lv;
	}

	/**
	 * @param lv the lv to set
	 */
	public void setLv(int lv) {
		this.lv = lv;
	}

	/**
	 * @return the autoDirection
	 */
	public int getAutoDirection() {
		return autoDirection;
	}

	/**
	 * @param autoDirection the autoDirection to set
	 */
	public void setAutoDirection(int autoDirection) {
		this.autoDirection = autoDirection;
	}

	/**
	 * @return the sAutoDirection
	 */
	public int getsAutoDirection() {
		return sAutoDirection;
	}

	/**
	 * @param sAutoDirection the sAutoDirection to set
	 */
	public void setsAutoDirection(int sAutoDirection) {
		this.sAutoDirection = sAutoDirection;
	}

	/**
	 * @return the specialAction
	 */
	public int getSpecialAction() {
		return specialAction;
	}

	/**
	 * @param specialAction the specialAction to set
	 */
	public void setSpecialAction(int specialAction) {
		this.specialAction = specialAction;
	}

	/**
	 * @return the radius
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}

	/**
	 * @return the sRadius
	 */
	public int getsRadius() {
		return sRadius;
	}

	/**
	 * @param sRadius the sRadius to set
	 */
	public void setsRadius(int sRadius) {
		this.sRadius = sRadius;
	}

	/**
	 * @return the expBlend
	 */
	public String getExpBlend() {
		return expBlend;
	}

	/**
	 * @param expBlend the expBlend to set
	 */
	public void setExpBlend(String expBlend) {
		this.expBlend = expBlend;
	}

	/**
	 * @return the expSe
	 */
	public int getExpSe() {
		return expSe;
	}

	/**
	 * @param expSe the expSe to set
	 */
	public void setExpSe(int expSe) {
		this.expSe = expSe;
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
	 * @return the autoDestory
	 */
	public int getAutoDestory() {
		return autoDestory;
	}

	/**
	 * @param autoDestory the autoDestory to set
	 */
	public void setAutoDestory(int autoDestory) {
		this.autoDestory = autoDestory;
	}

	/**
	 * @return the bullet
	 */
	public String getBullet() {
		return bullet;
	}

	/**
	 * @param bullet the bullet to set
	 */
	public void setBullet(String bullet) {
		this.bullet = bullet;
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
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
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * @return the bubble
	 */
	public String getBubble() {
		return bubble;
	}

	/**
	 * @param bubble the bubble to set
	 */
	public void setBubble(String bubble) {
		this.bubble = bubble;
	}

	/**
	 * @return the slot
	 */
	public EquipType getSlot() {
		return slot;
	}

	/**
	 * @param slot the slot to set
	 */
	public void setSlot(EquipType slot) {
		this.slot = slot;
	}

	/**
	 * @return the avatar
	 */
	public ArrayList<Avatar> getAvatar() {
		return avatar;
	}

	/**
	 * @param avatar the avatar to set
	 */
	public void setAvatar(ArrayList<Avatar> avatar) {
		this.avatar = avatar;
	}

	/**
	 * @return the qualityColor
	 */
	public WeaponColor getQualityColor() {
		return qualityColor;
	}

	/**
	 * @param qualityColor the qualityColor to set
	 */
	public void setQualityColor(WeaponColor qualityColor) {
		this.qualityColor = qualityColor;
	}
	
	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
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
	 * @return the canBeRewarded
	 */
	public boolean isCanBeRewarded() {
		return canBeRewarded;
	}

	/**
	 * @param canBeRewarded the canBeRewarded to set
	 */
	public void setCanBeRewarded(boolean canBeRewarded) {
		this.canBeRewarded = canBeRewarded;
	}

	/**
	 * @return the isUsedByBoss
	 */
	public boolean isUsedByBoss() {
		return isUsedByBoss;
	}

	/**
	 * @param isUsedByBoss the isUsedByBoss to set
	 */
	public void setUsedByBoss(boolean isUsedByBoss) {
		this.isUsedByBoss = isUsedByBoss;
	}

	/**
	 * Convert this pojo to BseEquipment.WeaponData for transport.
	 * @return
	 */
	public WeaponData toWeaponData() {
		WeaponData.Builder builder = WeaponData.newBuilder();
		
		String name = this.name;
		if ( Constant.I18N_ENABLE ) {
			Locale locale = GameContext.getInstance().getLocaleThreadLocal().get();
			name = GameResourceManager.getInstance().getGameResource(
					"equipments_new_name_".concat(_id), locale, this.name);
		}
		builder.setName(name);
		builder.setSName(name);
		builder.setId(_id);
		builder.setIndex(index);
		builder.setQuality(quality);
		builder.setQualityColor(qualityColor.name());
		builder.setEquipType(equipType);
		builder.setAddAttack(addAttack);
		builder.setAddDefend(addDefend);
		builder.setAddAgility(addAgility);
		builder.setAddLuck(addLuck);
		builder.setAddBlood(addBlood);
		builder.setAddBloodPercent(addBloodPercent);
		builder.setAddThew(addThew);
		builder.setAddDamage(addDamage);
		builder.setAddSkin(addSkin);
		builder.setSex(sex.ordinal());
		builder.setSign(sign);
		builder.setLv(lv);
		builder.setSpecialAction(specialAction==1);
		builder.setRadius(radius);
		builder.setSRadius(sRadius);
		builder.setPower(power);
		builder.setBullet(bullet);
		builder.setIcon(icon);
		builder.setInfo(info);
		builder.setSlot(slot.toString());
		builder.setExpSe(expSe);
		builder.setExpBlend(expBlend);
		
		for ( Avatar av : avatar ) {
			XinqiBseEquipment.Avatar.Builder avBuilder = XinqiBseEquipment.Avatar.newBuilder();
			avBuilder.setId(av.id);
			avBuilder.setLayer(av.layer);
			builder.addAvatar(avBuilder.build());
		}
		return builder.build();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("id").append(_id).append(" = {\n");
		builder.append("\t id=").append(_id).append(",\n");
		builder.append("\t index=");
		builder.append(index).append(",\n");
		builder.append("\t quality=");
		builder.append(quality).append(",\n");
		builder.append("\t qualityColor=\"");
		builder.append(qualityColor).append("\",\n");
		builder.append("\t sName=\"");
		builder.append(sName).append("\",\n");
		builder.append("\t equipType=");
		builder.append(equipType).append(",\n");
		builder.append("\t addAttack=");
		builder.append(addAttack).append(",\n");
		builder.append("\t addDefend=");
		builder.append(addDefend).append(",\n");
		builder.append("\t addAgility=");
		builder.append(addAgility).append(",\n");
		builder.append("\t addLuck=");
		builder.append(addLuck).append(",\n");
		builder.append("\t addBlood=");
		builder.append(addBlood).append(",\n");
		builder.append("\t addBloodPercent=");
		builder.append(addBloodPercent).append(",\n");
		builder.append("\t addThew=");
		builder.append(addThew).append(",\n");
		builder.append("\t addDamage=");
		builder.append(addDamage).append(",\n");
		builder.append("\t addSkin=");
		builder.append(addSkin).append(",\n");
		builder.append("\t sex=");
		builder.append(sex).append(",\n");
		builder.append("\t sign=");
		builder.append(sign).append(",\n");
		builder.append("\t lv=");
		builder.append(lv).append(",\n");
		builder.append("\t radius=");
		builder.append(radius).append(",\n");
		builder.append("\t sRadius=");
		builder.append(sRadius).append(",\n");
		builder.append("\t power=");
		builder.append(power).append(",\n");
		builder.append("\t bullet=\"");
		builder.append(bullet).append("\",\n");
		builder.append("\t icon=\"");
		builder.append(icon).append("\",\n");
		builder.append("\t name=\"");
		builder.append(name).append("\",\n");
		builder.append("\t info=\"");
		builder.append(info).append("\",\n");
		builder.append("\t slot=\"");
		builder.append(slot).append("\",\n");
		if ( avatar != null ) {
			builder.append("\t avatar= {\n");
			for ( Avatar a : avatar ) {
				if ( StringUtil.checkNotEmpty(a.id) ) {
					builder.append(a.id).append("={");
					builder.append("\t\t id=\"").append(a.id).append("\", \n");
					builder.append("\t\t layer=\"").append(a.layer).append("\",\n");
					builder.append("\t\t}, \n");
				}
			}
			builder.append("\t}, \n");
		}
		builder.append("},\n");
		return builder.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toLuaString(Locale locale) {
		String resName = this.name;
		if ( Constant.I18N_ENABLE ) {
			resName = GameResourceManager.getInstance().getGameResource(
					"tasks_name_".concat(_id), locale, this.name);
		}
		StringBuilder builder = new StringBuilder();
		builder.append("id").append(_id).append(" = {\n");
		builder.append("\t id=").append(_id).append(",\n");
		builder.append("\t index=");
		builder.append(index).append(",\n");
		builder.append("\t quality=");
		builder.append(quality).append(",\n");
		builder.append("\t qualityColor=\"");
		builder.append(qualityColor).append("\",\n");
		builder.append("\t sName=\"");
		builder.append(resName).append("\",\n");
		builder.append("\t equipType=");
		builder.append(equipType).append(",\n");
		builder.append("\t addAttack=");
		builder.append(addAttack).append(",\n");
		builder.append("\t addDefend=");
		builder.append(addDefend).append(",\n");
		builder.append("\t addAgility=");
		builder.append(addAgility).append(",\n");
		builder.append("\t addLuck=");
		builder.append(addLuck).append(",\n");
		builder.append("\t addBlood=");
		builder.append(addBlood).append(",\n");
		builder.append("\t addBloodPercent=");
		builder.append(addBloodPercent).append(",\n");
		builder.append("\t addThew=");
		builder.append(addThew).append(",\n");
		builder.append("\t addDamage=");
		builder.append(addDamage).append(",\n");
		builder.append("\t addSkin=");
		builder.append(addSkin).append(",\n");
		builder.append("\t sex=");
		builder.append(sex).append(",\n");
		builder.append("\t sign=");
		builder.append(sign).append(",\n");
		builder.append("\t lv=");
		builder.append(lv).append(",\n");
		builder.append("\t radius=");
		builder.append(radius).append(",\n");
		builder.append("\t sRadius=");
		builder.append(sRadius).append(",\n");
		builder.append("\t power=");
		builder.append(power).append(",\n");
		builder.append("\t bullet=\"");
		builder.append(bullet).append("\",\n");
		builder.append("\t icon=\"");
		builder.append(icon).append("\",\n");
		builder.append("\t name=\"");
		builder.append(resName).append("\",\n");
		builder.append("\t info=\"");
		//builder.append(info).append("\",\n");
		builder.append("").append("\",\n");
		builder.append("\t slot=\"");
		builder.append(slot).append("\",\n");
		if ( avatar != null ) {
			builder.append("\t avatar= {\n");
			for ( Avatar a : avatar ) {
				if ( StringUtil.checkNotEmpty(a.id) ) {
					builder.append(a.id).append("={");
					builder.append("\t\t id=\"").append(a.id).append("\", \n");
					builder.append("\t\t layer=\"").append(a.layer).append("\",\n");
					builder.append("\t\t}, \n");
				}
			}
			builder.append("\t}, \n");
		}
		builder.append("},\n");
		return builder.toString();
	}

	/**
	 * Convert to the PropDate with random max strength level and slot number.
	 * @param indate
	 * @param color
	 * @return
	 */
	public PropData toPropData(int indate, WeaponColor color) {
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.MAKE_WEAPON, this, color);
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			ArrayList list = (ArrayList)result.getResult();
			int maxStrengthLevel = (Integer)list.get(0);
			Collection<PropDataSlot> slots = (Collection<PropDataSlot>)list.get(1);
			return toPropData(indate, color, maxStrengthLevel, slots);	
		}
		return toPropData(indate, color, 8, null);
	}
	/**
	 * Convert this pojo to PropData
	 * @return
	 */
	public PropData toPropData(int indate, WeaponColor color, 
			int maxStrengthLevel, Collection<PropDataSlot> slots) {
		PropData propData = new PropData();
		propData.setItemId(this._id);
		propData.setName(this.name);
		propData.setDuration(1);
		propData.setPropIndate(indate);
		propData.setPropUsedTime(0);
		propData.setCount(1);
		propData.setWeaponColor(color);
		if ( maxStrengthLevel == 0 ) {
			maxStrengthLevel = MathUtil.nextGaussionInt(5, 13, 2.0);
		}
		propData.setMaxLevel(maxStrengthLevel);
		if ( slots != null ) {
			propData.setSlots(slots);
		}
		float ratio = 1.0f;
		switch ( color ) {
			case WHITE:
				break;
			case GREEN:
				ratio *= GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.WEAPON_COLOR_GREEN_RATIO, 1.1);
				break;
			case BLUE:
				ratio *= GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.WEAPON_COLOR_BLUE_RATIO, 1.25);
				break;
			case PINK:
				ratio *= GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.WEAPON_COLOR_PINK_RATIO, 1.5);
				break;
			case ORGANCE:
				ratio *= GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.WEAPON_COLOR_ORANGE_RATIO, 2.0);
				break;
			case PURPLE:
				ratio *= GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.WEAPON_COLOR_PURPLE_RATIO, 3.0);
				break;
		}
		propData.setLevel(0);

		propData.setAttackLev(Math.round(this.addAttack*ratio));
		propData.setBaseAttack(propData.getAttackLev());
		propData.setDefendLev(Math.round(this.addDefend*ratio));
		propData.setBaseDefend(propData.getDefendLev());
		propData.setAgilityLev(Math.round(this.addAgility*ratio));
		propData.setBaseAgility(propData.getAgilityLev());
		propData.setLuckLev(Math.round(this.addLuck*ratio));
		propData.setBaseLuck(propData.getLuckLev());
		double newPower = EquipCalculator.calculateWeaponPower(
				this.getAddAttack(), this.getAddDefend(), this.getAddAgility(), 
				this.getAddLuck(), this.getAddBlood(), this.getAddSkin(), 
				this.radius, this.addBloodPercent);
		int power = (int)Math.round(newPower);
		propData.setPower(power);
		propData.setBasePower(power);

		propData.setBloodLev(this.addBlood);
		propData.setBloodPercent(this.addBloodPercent);
		propData.setDamageLev(this.addDamage);
		propData.setThewLev(this.addThew);
		propData.setSkinLev(this.addSkin);
		propData.setSign(-1);
		propData.setValuetype(PropDataValueType.GAME);
		propData.setBanded(true);
		propData.setUserLevel(this.userLevel);
		propData.setWeapon(true);
		
		return propData;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WeaponPojo o) {
		if ( o == null ) {
			return -1;
		} else {
			int result = -1;
			if ( this._id != null ) {
				result = this._id.compareTo(o._id);
				if ( result == 0 ) {
					if ( this.name != null ) {
						result = this.name.compareTo(o.name);
					}
				}
			}
			return result;
		}
	}	
	
	@Override
	public WeaponPojo clone() {
		WeaponPojo newWeapon = new WeaponPojo();
		newWeapon._id = this._id;
		newWeapon.index	 = this.index;
		newWeapon.quality	 = this.quality;
		newWeapon.qualityColor	 = this.qualityColor;
		newWeapon.sName	 = this.sName;
		newWeapon.equipType	 = this.equipType;
		newWeapon.addAttack	 = this.addAttack;
		newWeapon.addDefend	 = this.addDefend;
		newWeapon.addAgility	 = this.addAgility;
		newWeapon.addLuck	 = this.addLuck;
		newWeapon.addBlood	 = this.addBlood;
		newWeapon.addBloodPercent	 = this.addBloodPercent;
		newWeapon.addThew	 = this.addThew;
		newWeapon.addDamage	 = this.addDamage;
		newWeapon.addSkin	 = this.addSkin;
		newWeapon.sex	 = this.sex;
		newWeapon.unused1	 = this.unused1;
		newWeapon.unused2	 = this.unused2;
		newWeapon.unused3	 = this.unused3;
		newWeapon.indate1	 = this.indate1;
		newWeapon.indate2	 = this.indate2;
		newWeapon.indate3	 = this.indate3;
		newWeapon.sign	 = this.sign;
		newWeapon.lv	 = this.lv;
		newWeapon.autoDirection	 = this.autoDirection;
		newWeapon.sAutoDirection	 = this.sAutoDirection;
		newWeapon.specialAction	 = this.specialAction;
		newWeapon.radius	 = this.radius;
		newWeapon.sRadius	 = this.sRadius;
		newWeapon.expBlend	 = this.expBlend;
		newWeapon.expSe	 = this.expSe;
		newWeapon.power	 = this.power;
		newWeapon.autoDestory	 = this.autoDestory;
		newWeapon.bullet	 = this.bullet;
		newWeapon.icon	 = this.icon;
		newWeapon.name	 = this.name;
		newWeapon.info	 = this.info;
		newWeapon.bubble	 = this.bubble;
		newWeapon.slot	 = this.slot;
		if ( this.avatar != null ) {
			newWeapon.avatar	 = new ArrayList<Avatar>(this.avatar);
		}

		return newWeapon;
	}
}
