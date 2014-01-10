package com.xinqihd.sns.gameserver.config.equip;

import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.util.Text;


/**
 * The equipment type enumlator
 * @author wangqi
 *
 */
public enum EquipType {
	
	WEAPON,
	EXPRESSION,
	FACE,
	DECORATION,
	HAIR,
	WING,
	CLOTHES,
	HAT,
	GLASSES,
	JEWELRY,
	//戒指
	RING,
	//结婚戒指
	WEDDINGRING,
	//项链
	NECKLACE,
	//手镯
	BRACELET,
	BUBBLE,
	SUIT,
	OFFHANDWEAPON,
	OTHER,
	ITEM,
	GIFT_PACK;
	
	String value = null;
	String title = null;
	EquipType() {
		value = this.name().toLowerCase();
		title = Text.text("slot.".concat(toString()));
	}

	public String toString() {
		return value;
	}
	
	public String getTitle() {
		return title;
	}
	
	/**
	 * Get the proper slot type for given position.
	 * @return
	 */
	public PropDataEquipIndex[] getPropDataEquipIndex() {
		switch ( this ) {
	    //1: 帽子
			case HAT:
				return new PropDataEquipIndex[]{PropDataEquipIndex.HAT};
			//2：眼镜
			case GLASSES:
				return new PropDataEquipIndex[]{PropDataEquipIndex.GLASS};
			//3：头发 
			case HAIR:
				return new PropDataEquipIndex[]{PropDataEquipIndex.HAIR};
			//4：脸饰
			case DECORATION:
				return new PropDataEquipIndex[]{PropDataEquipIndex.FACE};
			//5. 衣服
			case CLOTHES:
				return new PropDataEquipIndex[]{PropDataEquipIndex.CLOTH};
			//6. 眼睛                 
			case EXPRESSION:
				return new PropDataEquipIndex[]{PropDataEquipIndex.EYE};
			//7. 套装
			case SUIT:
				return new PropDataEquipIndex[]{PropDataEquipIndex.SUIT};
			//8. 翅膀
			case WING:
				return new PropDataEquipIndex[]{PropDataEquipIndex.WING};
			//9. 手镯
			case BRACELET:
				return new PropDataEquipIndex[]{PropDataEquipIndex.BRACELET1, PropDataEquipIndex.BRACELET2};
			//10.戒指
			case RING:
				return new PropDataEquipIndex[]{PropDataEquipIndex.RING1, PropDataEquipIndex.RING2};
			//13. 项链
			case NECKLACE:
				return new PropDataEquipIndex[]{PropDataEquipIndex.NECKLACE};
			//14. 泡泡
			case BUBBLE:
				return new PropDataEquipIndex[]{PropDataEquipIndex.BUBBLE};
			//15. 婚戒
			case WEDDINGRING:
				return new PropDataEquipIndex[]{PropDataEquipIndex.WEDRING};
			//16. Crust现在不用
			case OTHER:
				return new PropDataEquipIndex[]{PropDataEquipIndex.RESERVED, PropDataEquipIndex.PET};
			//17. 武器
			case WEAPON:
				return new PropDataEquipIndex[]{PropDataEquipIndex.WEAPON};
			//18. 副手
			case OFFHANDWEAPON:
				return new PropDataEquipIndex[]{PropDataEquipIndex.SUBWEAPON};
		}
		return null;
	}
}
