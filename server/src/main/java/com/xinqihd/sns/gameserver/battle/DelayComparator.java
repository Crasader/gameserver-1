package com.xinqihd.sns.gameserver.battle;

import java.util.Comparator;

public class DelayComparator implements Comparator<BattleUser> {
	
	private static DelayComparator instance = new DelayComparator();
	
	public static DelayComparator getInstance() {
		return instance;
	}

	@Override
	public int compare(BattleUser bUser1, BattleUser bUser2) {
		if ( bUser1 == null && bUser2 == null ) {
			return 0;
		} else if ( bUser1 == null && bUser2 != null ) {
			return 1;
		} else if ( bUser2 == null && bUser1 != null ) {
			return -1;
		} else {
			if ( bUser1.containStatus(RoleStatus.DEAD) && bUser2.containStatus(RoleStatus.DEAD) ) {
				return bUser1.getUser().getUsername().compareTo(bUser2.getUser().getUsername());
			} else if ( bUser1.containStatus(RoleStatus.DEAD) && !bUser2.containStatus(RoleStatus.DEAD) ) {
				return 1;
			} else if ( bUser2.containStatus(RoleStatus.DEAD) && !bUser1.containStatus(RoleStatus.DEAD) ) {
				return -1;
			} else {
				if ( bUser1.containStatus(RoleStatus.ICED) && !bUser2.containStatus(RoleStatus.ICED) ) {
					return 1;
				} else if ( bUser2.containStatus(RoleStatus.ICED) && !bUser1.containStatus(RoleStatus.ICED) ) {
					return -1;
				} else {
					if ( bUser1.getDelay() != bUser2.getDelay() ) {
						//延迟正序
						return bUser1.getDelay() - bUser2.getDelay();
					} else {
						if ( bUser1.getRoundOwnerTimes() != bUser2.getRoundOwnerTimes() ) {
							//回合数正序
							return bUser1.getRoundOwnerTimes() - bUser2.getRoundOwnerTimes();
						} else {
							//敏捷倒叙
							if ( bUser1.getUser().getAgility() != bUser2.getUser().getAgility() ) {
								return bUser2.getUser().getAgility() - bUser1.getUser().getAgility();
							} else {
								//攻击力正序
								if ( bUser1.getUser().getAttack() != bUser2.getUser().getAttack() ) {
									return bUser1.getUser().getAttack() - bUser2.getUser().getAttack();
								} else {
									return bUser1.getUser().getUsername().compareTo(bUser2.getUser().getUsername());
								}
							}							
						}
					}
				}
			}
		}
	}

}
