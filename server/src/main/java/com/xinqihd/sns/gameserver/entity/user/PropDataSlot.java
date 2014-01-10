package com.xinqihd.sns.gameserver.entity.user;

import java.util.Collection;
import java.util.HashSet;

import com.xinqihd.sns.gameserver.config.equip.TextColor;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The Slot used to insert stones
 * @author wangqi
 *
 */
public class PropDataSlot {

	private PropDataEnhanceField slotType = null;
	
	//The last embeded stoneId
	private String stoneId = null;
	
	//The last embeded stone level
	private int stoneLevel = 0;
	
	//The added value
	private int value = 0;
	
	//The all available types
	private HashSet<PropDataEnhanceField> availabeTypes = 
			new HashSet<PropDataEnhanceField>();

	/**
	 * @return the slotType
	 */
	public PropDataEnhanceField getSlotType() {
		return slotType;
	}

	/**
	 * @param slotType the slotType to set
	 */
	public void setSlotType(PropDataEnhanceField slotType) {
		this.slotType = slotType;
	}

	/**
	 * @return the stoneId
	 */
	public String getStoneId() {
		return stoneId;
	}

	/**
	 * @param stoneId the stoneId to set
	 */
	public void setStoneId(String stoneId) {
		this.stoneId = stoneId;
	}

	/**
	 * @return the stoneLevel
	 */
	public int getStoneLevel() {
		return stoneLevel;
	}

	/**
	 * @param stoneLevel the stoneLevel to set
	 */
	public void setStoneLevel(int stoneLevel) {
		this.stoneLevel = stoneLevel;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}
	
	/**
	 * @return the availabeTypes
	 */
	public HashSet<PropDataEnhanceField> getAvailabeTypes() {
		return availabeTypes;
	}

	/**
	 * @param availabeTypes the availabeTypes to set
	 */
	public void setAvailabeTypes(Collection<PropDataEnhanceField> availabeTypes) {
		this.availabeTypes.clear();
		this.availabeTypes.addAll(availabeTypes);
	}
	
	/**
	 * 
	 * @param field
	 */
	public void addAvailableTypes(PropDataEnhanceField field) {
		this.availabeTypes.add(field);
	}

	/**
	 * Test if this slot can embed the given type stone.
	 * @param field
	 * @return
	 */
	public boolean canEmbedField(PropDataEnhanceField field) {
		if ( this.availabeTypes.size() > 0 ) {
			if ( this.availabeTypes.contains(field) ) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	/**
	 * Get the localized description for this slot.
	 * @return
	 */
	public String getDesc() {
		//Generate slot title.
		StringBuilder buf = new StringBuilder(20);
		String attrName = "";
		if ( this.value <= 0 ) {
			if ( availabeTypes.size() > 0 ) {
				for ( PropDataEnhanceField field: availabeTypes ) {
					switch ( field ) {
						case ATTACK:
							buf.append(Text.text("slot.fire")).append("/");
							break;
						case DEFEND:
							buf.append(Text.text("slot.earth")).append("/");
							break;
						case LUCKY:
							buf.append(Text.text("slot.water")).append("/");
							break;
						case AGILITY:
							buf.append(Text.text("slot.wind")).append("/");
							break;
					}
				}
				buf.deleteCharAt(buf.length()-1);
			} else {
				for ( int i=PropDataEnhanceField.ATTACK.ordinal(); 
						i<=PropDataEnhanceField.LUCKY.ordinal(); i++ ) {
					PropDataEnhanceField field = PropDataEnhanceField.values()[i];
					switch ( field ) {
						case ATTACK:
							buf.append(Text.text("slot.fire")).append("/");
							break;
						case DEFEND:
							buf.append(Text.text("slot.earth")).append("/");
							break;
						case LUCKY:
							buf.append(Text.text("slot.water")).append("/");
							break;
						case AGILITY:
							buf.append(Text.text("slot.wind")).append("/");
							break;
					}
				}
				buf.deleteCharAt(buf.length()-1);
			}
		} else {
			switch ( slotType ) {
				case ATTACK:
					buf.append(Text.text("slot.fire")).append("/");
					attrName = Text.text("slot.attack");
					break;
				case DEFEND:
					buf.append(Text.text("slot.earth")).append("/");
					attrName = Text.text("slot.defend");
					break;
				case LUCKY:
					buf.append(Text.text("slot.water")).append("/");
					attrName = Text.text("slot.lucky");
					break;
				case AGILITY:
					buf.append(Text.text("slot.wind")).append("/");
					attrName = Text.text("slot.agility");
					break;
			}
		}
		String title = null;
		if ( stoneLevel == 0 ) {
			title = StringUtil.concat(TextColor.GRAY.getColorStr(), Text.text("slot.empty"));
		} else {
			//ItemPojo stone = ItemManager.getInstance().getItemByTypeIdAndLevel(stoneId, stoneLevel);
			if ( stoneLevel >= 5 ) {
				title = StringUtil.concat(TextColor.ORANGE.getColorStr(), "Lv", stoneLevel, ",", attrName, "+", value);	
			} else if ( stoneLevel >= 4 ) {
				title = StringUtil.concat(TextColor.CYAN.getColorStr(), "Lv", stoneLevel, ",", attrName, "+", value);	
			} else {
				title = StringUtil.concat(TextColor.GREEN.getColorStr(), "Lv", stoneLevel, ",", attrName, "+", value);	
			}
		}
		String desc = Text.text("slot.title", buf.toString(), title);
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PDSlot [slotType=");
		builder.append(slotType);
		builder.append(", stoneId=");
		builder.append(stoneId);
		builder.append(", stoneLevel=");
		builder.append(stoneLevel);
		builder.append(", value=");
		builder.append(value);
		builder.append(", availabeTypes=");
		builder.append(availabeTypes);
		builder.append("]");
		return builder.toString();
	}

	public PropDataSlot clone() {
		PropDataSlot c = new PropDataSlot();
		c.slotType = this.slotType;
		c.stoneId = this.stoneId;
		c.stoneLevel = this.stoneLevel;
		c.value = this.value;
		c.availabeTypes.addAll(this.availabeTypes);
		return c;
	}
}
