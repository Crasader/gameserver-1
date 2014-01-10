package com.xinqihd.sns.gameserver.script.function;

import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * Calculate the final damage on a user.
 * @author wangqi
 *
 */
public class HurtCalculator {

	/**
   * 伤害计算			
   * 1 实际伤害=自身伤害*（1+自身攻击/1000）			
   * 2 实际护甲=自身护甲*（1+（自身防御-对方幸运）/1000）			
   * 3 护甲减免率=（60+实际护甲）/（480+实际护甲）			
   * 4 最终伤害值=攻击方实际伤害*（1-被攻击方的护甲减免率）			
   * 5 武器具有爆炸半径，对方处于爆炸范围内才会受到伤害			
   * 6 将爆炸半径除以100，划分为100等分，每远离半径中心1等分，伤害值减少0.5%，即在最边缘的地方，伤害值只有50%			
   * 7 弹坑在命中玩家时会缩小，直接完全命中则弹坑只有40%，然后按照距离逐渐变大，未命中时弹坑为100%			
   * 			
   * 数值范围			
   * 实际伤害		200~1000	
   * 实际护甲		60~700	
   * 护甲减免		20%~65%	
   * 最终伤害		70~800	
   * 
	 * @param attackUserDamage 攻击方自身伤害
	 * @param attackUserAttack 攻击方自身攻击
	 * @param targetUserDefend 被攻击方防御
	 * @param attackUserLucky  攻击方幸运
	 * @param targetUserSkin   被攻击方护甲
	 * @param bloomAreaRatio   爆炸范围，为[0.0 ~ 1.0]范围
	 * @return
	 */
	public static final double calculateHurt(double attackUserDamage, double attackUserAttack, 
			double targetUserDefend, double attackUserLucky, double targetUserSkin, double bloomAreaRatio) {
		double actualDamage = attackUserDamage * ( 1 + attackUserAttack/1000);
		double actualSkin   = targetUserSkin * ( 1 + (targetUserDefend - attackUserLucky) / 1000 );
		double skinRatio    = ( 60 + actualSkin ) / ( 480 + actualSkin );
		double finalDamage  = actualDamage * ( 1 - skinRatio );
		return finalDamage;
	}
	
	/**
	 * 爆擊（也有時被稱為「重擊」，而其英文名 " Critical hits " 常被玩家簡稱為 " Crits " ）是指一種比平常多出額外傷害值的攻擊
	 * 
   * 暴击率=10%+幸运/2000		
   * 暴击倍率=1.2+幸运/2000		
   * 		
   * 幸运	暴击率	暴击倍率
   * 100	15.00%	1.3
   * 200	20.00%	1.4
   * 300	25.00%	1.5
   * 400	30.00%	1.6
   * 500	35.00%	1.7
   * 600	40.00%	1.8
   * 700	45.00%	1.9
   * 800	50.00%	2
   * 900	55.00%	2.1
   * 1000	60.00%	2.2
   * 1100	65.00%	2.3
   * 1200	70.00%	2.4
   * 1300	75.00%	2.5
   * 1400	80.00%	2.6
   * 1500	85.00%	2.7
   * 1506	85.30%	2.706
	 *
	 * @param attackUserLucky 攻击方的幸运值 
	 * @return
	 */
	public static final double calculateCritsRatio(double attackUserLucky) {
		double happenRatio = 0.1 + attackUserLucky / 2000;
		double hurtRatio   = 1.2 + attackUserLucky / 2000; 
		if ( MathUtil.nextDouble() < happenRatio ) {
			return hurtRatio;
		}
		return 1.0;
	}
}
