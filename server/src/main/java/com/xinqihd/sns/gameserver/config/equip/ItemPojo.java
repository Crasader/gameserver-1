package com.xinqihd.sns.gameserver.config.equip;

import java.util.ArrayList;
import java.util.Locale;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.TaskPojo.Award;
import com.xinqihd.sns.gameserver.db.mongo.GameResourceManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.proto.XinqiBseItem;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardCondition;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It is the item that users can consume in the game.
 * @author wangqi
 *
 */
public class ItemPojo implements Pojo, Comparable<ItemPojo> {

	private static final long serialVersionUID = 7777799433967539012L;

	//The item id: item_<typeId>_<level>
	private String _id;
	
	//The original item's id, since it may be the same value among same type
	//but different level items, it cannot be as the primary key
	private String typeId;
	

	@Override
	public void setId(String id) {
		this._id = id;
	}

	@Override
	public String getId() {
		return _id;
	}

	// --------------------------------------- Properties

	private int level = 0;
	
	private String icon = Constant.EMPTY;
	
	private String name = Constant.EMPTY;
	
	private String info = Constant.EMPTY;
	
	private String type = "consumable";
	
	//It is only for the box item
	private String script = Constant.EMPTY;
	
	//It is only for the random type box item
	private double q = 1.0;
	
	//It is only for the random type box item
	private int count = 1;
	
	//The itemType of this pojo
	private EquipType equipType = EquipType.ITEM;
	
	//For the treasure box type item, they contain
	//kinds of reward.
	private ArrayList<Reward> rewards = new ArrayList<Reward>();
	
	//If the item is a treasure box, when it is opened, system will 
	//broadcast to all users.
	private boolean broadcast = false;
	
	private ArrayList<RewardCondition> conditions = 
			new ArrayList<RewardCondition>();
	
  /**
   * If it is true, this weapon can be used in random reward
   */
  private boolean canBeRewarded = false;

	/**
	 * Since the ItemPojo may have same id but different level,
	 * we have to use both the id and level as the hash key.
	 * 
	 * @return
	 */
	public String getIdLevel() {
		return _id.concat(String.valueOf(level));
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the typeId
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return the equipType
	 */
	public EquipType getEquipType() {
		return equipType;
	}

	/**
	 * @param equipType the equipType to set
	 */
	public void setEquipType(EquipType equipType) {
		this.equipType = equipType;
	}

	/**
	 * Convert this pojo to ProtoBuf's ItemData
	 * @return
	 */
	public XinqiBseItem.ItemData toItemData() {
		String resName = this.name;
		String resInfo = this.info;
		if ( Constant.I18N_ENABLE ) {
			Locale locale = GameContext.getInstance().getLocaleThreadLocal().get();
			resName = GameResourceManager.getInstance().getGameResource(
					"items_name_".concat(_id), locale, this.name);
			resInfo = GameResourceManager.getInstance().getGameResource(
					"items_info_".concat(_id), locale, this.info);
		}
		XinqiBseItem.ItemData.Builder builder = XinqiBseItem.ItemData.newBuilder();
		builder.setId(_id);
		builder.setName(resName);
		builder.setInfo(resInfo);
		builder.setType(type);
		builder.setIcon(icon);
		builder.setLevel(level);
		return builder.build();
	}
	
	/**
	 * Convert this pojo to PropData
	 * @return
	 */
	public PropData toPropData() {
		return toPropData(1);
	}
	
	/**
	 * Convert this pojo to PropData
	 * @return
	 */
	public PropData toPropData(int count) {
		PropData propData = new PropData();
		propData.setItemId(this._id);
		propData.setName(this.name);
		propData.setDuration(1);
		propData.setPropIndate(Integer.MAX_VALUE);
		propData.setPropUsedTime(0);
		propData.setCount(count);
		propData.setWeaponColor(WeaponColor.WHITE);
		propData.setLevel(this.level);
		propData.setAttackLev(0);
		propData.setBaseAttack(0);
		propData.setDefendLev(0);
		propData.setBaseDefend(0);
		propData.setAgilityLev(0);
		propData.setBaseAgility(0);
		propData.setLuckLev(0);
		propData.setBaseLuck(0);
		propData.setSign(-1);
		propData.setValuetype(PropDataValueType.GAME);
		propData.setBanded(true);
		propData.setWeapon(false);
		return propData;
	}

	/**
	 * @return the rewards
	 */
	public ArrayList<Reward> getRewards() {
		return rewards;
	}

	/**
	 * @param rewards the rewards to set
	 */
	public void addReward(Reward reward) {
		this.rewards.add(reward);
	}

	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param script the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}

	/**
	 * @return the q
	 */
	public double getQ() {
		return q;
	}

	/**
	 * @param q the q to set
	 */
	public void setQ(double q) {
		this.q = q;
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
	 * @return the conditions
	 */
	public ArrayList<RewardCondition> getConditions() {
		return conditions;
	}

	/**
	 * @param conditions the conditions to set
	 */
	public void addConditions(RewardCondition condition) {
		this.conditions.add(condition);
	}

	/**
	 * @return the broadcast
	 */
	public boolean isBroadcast() {
		return broadcast;
	}

	/**
	 * @param broadcast the broadcast to set
	 */
	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ItemPojo [_id=" + _id + ", typeId=" + typeId + ", level=" + level
				+ ", icon=" + icon + ", name=" + name + ", info=" + info + ", type="
				+ type + ", script=" + script + ", q=" + q + ", count=" + count
				+ ", equipType=" + equipType + ", rewards=" + rewards + ", broadcast="
				+ broadcast + ", conditions=" + conditions + ", canBeRewarded="
				+ canBeRewarded + "]";
	}
	
	public String toLuaString(Locale locale) {
		String resName = this.name;
		String resDesc = this.info;
		if ( Constant.I18N_ENABLE ) {
			resName = GameResourceManager.getInstance().getGameResource(
					"items_name_".concat(_id), locale, this.name);
			resDesc = GameResourceManager.getInstance().getGameResource(
					"items_info_".concat(_id), locale, this.info);
		}
		StringBuilder builder = new StringBuilder();
		builder.append("id").append(_id).append(" = {\n");
		builder.append("\t id=").append(_id).append(",\n");
		builder.append("\t  name=\"");
		builder.append(resName).append("\",\n");
		builder.append("\t  info=\"");
		builder.append(resDesc).append("\",\n");
		builder.append("\t  lv=\"");
		builder.append(level).append("\",\n");
		builder.append("\t  type=\"");
		builder.append(type).append("\",\n");
		builder.append("\t  typeId=");
		builder.append(typeId).append(",\n");
		builder.append("\t  icon=\"");
		builder.append(icon).append("\",\n");
		builder.append("\t  equipType=\"");
		builder.append(equipType).append("\",\n");
		builder.append("},\n");
		return builder.toString();
	}

	/**
	 * Convert the prop id from typeId and level
	 * @param typeId
	 * @param level
	 * @return
	 */
	public static final String toId(String typeId, int level) {
		int tid = StringUtil.toInt(typeId, 20001);
  	return toId(tid, level);
	}
	
	/**
	 * Convert the prop id from typeId and level
	 * @param typeId
	 * @param level
	 * @return
	 */
	public static final String toId(int typeId, int level) {
		int tid = typeId;
		if ( tid <= 20005 ) {
  		int id = 20000 + (tid-20001)*5 + level;
  		String newId = String.valueOf(id);
  		return newId;
  	} else if ( tid == 24001 ) {
  		/**
				20026	神恩符Lv1
				20027	神恩符Lv2
				20028	神恩符Lv3
				20029	神恩符Lv4
				20030	神恩符Lv5
  		 */
  		int id = 20026 + level - 1;
  		return String.valueOf(id);
  	}
  	return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ItemPojo o) {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ItemPojo clone() {
		ItemPojo newPojo = new ItemPojo();
		newPojo._id = this._id;
		newPojo.level = this.level;
		newPojo.icon = this.icon;
		newPojo.name = this.name;
		newPojo.info = this.info;
		newPojo.type = this.type;
		newPojo.typeId = this.typeId;
		newPojo.script = this.script;
		newPojo.q = this.q;
		newPojo.count = this.count;
		newPojo.equipType = this.equipType;
		newPojo.rewards.addAll(this.rewards);
		newPojo.conditions.addAll(this.conditions);

		return newPojo;
	}
	
	
}
