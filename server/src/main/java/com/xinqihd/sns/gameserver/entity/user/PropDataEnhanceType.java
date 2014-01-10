package com.xinqihd.sns.gameserver.entity.user;

/**
 * 用于表示对一个PropData进行各种强化产生的数值变化
 * 
 * @author wangqi
 *
 */
public enum PropDataEnhanceType {

	//强化
	STRENGTH,
	//熔炼
	FORGE,
	//镶嵌
	EMBED,
	//全套:攒齐全套装备带来的提升
	COLLECT;
	//装备插槽
//	SLOT1,
//	SLOT2,
//	SLOT3,
//	SLOT4,
//	SLOT5,
//	SLOT6;
	
	public enum Field {
		//伤害
		DAMAGE,
		//护甲
		SKIN,
		//攻击
		ATTACK,
		//防御
		DEFEND,
		//敏捷
		AGILITY,
		//幸运
		LUCKY,
		//血量
		BLOOD,
		//血量百分比
		BLOODPERCENT;
		
		public PropDataEnhanceField toField() {
			switch ( this ) {
				case ATTACK:
					return PropDataEnhanceField.ATTACK;
				case DEFEND:
					return PropDataEnhanceField.DEFEND;
				case AGILITY:
					return PropDataEnhanceField.AGILITY;
				case BLOOD:
					return PropDataEnhanceField.BLOOD;
				case BLOODPERCENT:
					return PropDataEnhanceField.BLOODPERCENT;
				case DAMAGE:
					return PropDataEnhanceField.DAMAGE;
				case LUCKY:
					return PropDataEnhanceField.LUCKY;
				case SKIN:
					return PropDataEnhanceField.SKIN;
			}
			return null;
		}
	}
}
