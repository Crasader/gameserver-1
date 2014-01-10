package com.xinqihd.sns.gameserver.entity.user;

import com.xinqihd.sns.gameserver.config.equip.EquipType;

/**
 * According to the original protocol, the PropData array's 1 to 19 indexes are
 * used to store equipments that users wear. So this Enum is used to index those
 * equipments.
 * 
 * @author wangqi
 *
 */
public enum PropDataEquipIndex {
	
	NOTHING(0),
	//1: 帽子
	HAT(1),
	//2：眼镜
	GLASS(2),
	//3：头发 
	HAIR(3),
	//4：脸饰
	FACE(4),
	//5. 衣服
	CLOTH(5),
	//6. 眼睛                 
	EYE(6),
	//7. 套装
	SUIT(7),
	//8. 翅膀
	WING(8),
	//9. 手镯
	BRACELET1(9),
	//10.戒指
	RING1(10),
	//11.手镯
	BRACELET2(11),
	//12. 戒指
	RING2(12),
	//13. 项链
	NECKLACE(13),
	//14. 泡泡
	BUBBLE(14),
	//15. 婚戒
	WEDRING(15),
	//16. Crust现在不用
	RESERVED(16),
	//17. 武器
	WEAPON(17),
	//18. 副手
	SUBWEAPON(18),
	//19. 宠物
	PET(19);
	
	private int index;
	
	PropDataEquipIndex(int index) {
		this.index = index;
	}
	
	public int index() {
		return this.index;
	}
	
	/** 
	 * Note: It is only a convenient implementation. The index and the ordinal
	 * may not be the same. Use it for caution. And please DO NOT change the Enum
	 * field's order.
	 *  
	 */
	public static PropDataEquipIndex fromIndex(int index) {
		if ( index >= 0 && index < PropDataEquipIndex.values().length ) {
			return PropDataEquipIndex.values()[index];
		}
		return null;
	}
	
	/**
	 * Get the proper slot type for given position.
	 * @return
	 */
	public EquipType getPropEquipType() {
		switch ( this ) {
	    //1: 帽子
			case HAT:
				return EquipType.HAT;
			//2：眼镜
			case GLASS:
				return EquipType.GLASSES;
			//3：头发 
			case HAIR:
				return EquipType.HAIR;
			//4：脸饰
			case FACE:
				return EquipType.DECORATION;
			//5. 衣服
			case CLOTH:
				return EquipType.CLOTHES;
			//6. 眼睛                 
			case EYE:
				return EquipType.EXPRESSION;
			//7. 套装
			case SUIT:
				return EquipType.SUIT;
			//8. 翅膀
			case WING:
				return EquipType.WING;
			//9. 手镯
			case BRACELET1:
				return EquipType.BRACELET;
			//10.戒指
			case RING1:
				return EquipType.RING;
			//11.手镯
			case BRACELET2:
				return EquipType.BRACELET;
			//12. 戒指
			case RING2:
				return EquipType.RING;
			//13. 项链
			case NECKLACE:
				return EquipType.NECKLACE;
			//14. 泡泡
			case BUBBLE:
				return EquipType.BUBBLE;
			//15. 婚戒
			case WEDRING:
				return EquipType.WEDDINGRING;
			//16. Crust现在不用
			case RESERVED:
				return EquipType.OTHER;
			//17. 武器
			case WEAPON:
				return EquipType.WEAPON;
			//18. 副手
			case SUBWEAPON:
				return EquipType.OFFHANDWEAPON;
			//19. 宠物
			case PET:
				return EquipType.OTHER;
		}
		return null;
	}
}
