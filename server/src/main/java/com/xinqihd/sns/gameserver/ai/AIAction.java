package com.xinqihd.sns.gameserver.ai;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math.util.FastMath;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.battle.BuffToolIndex;
import com.xinqihd.sns.gameserver.chat.ChatType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.geom.SimplePoint;
import com.xinqihd.sns.gameserver.proto.XinqiBceAskRoundOver.BceAskRoundOver;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleUseTool.BceRoleUseTool;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;

public class AIAction {
	
	private static final Logger logger = LoggerFactory.getLogger(AIAction.class);

	/**
	 * Determine the role attack
	 * @param aiUser
	 * @param serverIoSession
	 * @param userAngle
	 * @param userPower
	 * @param userHitPoint
	 * @param lastAngle
	 * @param lastPower
	 * @param lastHitPoint
	 */
	public static final BceRoleAttack roleAttack(User aiUser, IoSession serverIoSession, 
			int wind, int myX, int myY, int targetX, int targetY) {
		//Get last round data.
		String      mapId       = (String)aiUser.getUserData(AIManager.BATTLE_MAP_ID);
		Integer     lastAngle   = (Integer)aiUser.getUserData(AIManager.BATTLE_LAST_ROUND_ANGLE);
		Integer     lastPower   = (Integer)aiUser.getUserData(AIManager.BATTLE_LAST_ROUND_POWER);
		SimplePoint lastHitPoint    = (SimplePoint)aiUser.getUserData(AIManager.BATTLE_LAST_ROUND_HITPOINT);
		SimplePoint lastTargetPoint = (SimplePoint)aiUser.getUserData(AIManager.BATTLE_LAST_ROUND_TARGETPOINT);
		Integer     userAngle   = (Integer)aiUser.getUserData(AIManager.BATTLE_USER_ANGLE);
		Integer     userPower   = (Integer)aiUser.getUserData(AIManager.BATTLE_USER_POWER);
		SimplePoint userHitPoint    = (SimplePoint)aiUser.getUserData(AIManager.BATTLE_USER_HITPOINT);
		Boolean     userHurtAI  = (Boolean)aiUser.getUserData(AIManager.BATTLE_USER_HURTAI);
		Integer     totalRoundNotHit   = (Integer)aiUser.getUserData(AIManager.BATTLE_TOTAL_ROUND_NOT_HIT);
		
		int angle = 30, power = 50;
		CHECK: {
			/**
			 * 不使用用户的数值
			 */
			/*
			if ( userHurtAI != null && userHurtAI.booleanValue() ) {
				angle = 180 - userAngle;
				power = userPower;
				aiUser.putUserData(AIManager.BATTLE_USER_HURTAI, null);
				break CHECK;
			}
			*/
			if ( lastAngle != null ) {
				angle = lastAngle.intValue();
				//当玩家位置改变时，调整方向
				if ( myX < targetX && angle > 90 ) {
					angle = 180 - angle;
					logger.debug("当玩家位置改变时，调整方向. angle:{}, power:{}", angle, power);
				} else if ( myX > targetX && angle < 90 ) {
					angle = 180 - angle;
					logger.debug("当玩家位置改变时，调整方向. angle:{}, power:{}", angle, power);
				}
			} else {
			  //第一次射击，计算起始力度
				/**
				 * 当敌人在X轴距离我较近，但Y轴距离我较高时，使用大力度平直射击
				 */
				if ( myY - targetY >= 200 ) {
					double a = Math.abs(myY - targetY);
					double b = Math.abs(myX - targetX);
					double c = FastMath.sqrt(a*a+b*b);
					//double ang = FastMath.asin(a/c)*180/Math.PI;
					double ang = 80;
					if ( myX > targetX ) {
						ang = 180 - ang;
					}
					angle = (int)ang;
					power = calculatePower(angle, targetX, targetY, 0);
					logger.debug("当敌人在X轴距离我较近，但Y轴距离我较高时，使用大力度平直射击. angle:{}, power:{}", angle, power);
				} else {
					angle = calculateAngle(myX, myY, targetX, targetY);
					logger.debug("为第一次射击计算数值. angle:{}, power:{}", angle, power);
				}
			}
			if ( lastPower != null ) {
				power = lastPower.intValue();
				if ( lastTargetPoint != null ) {
					if ( lastTargetPoint.getX() < targetX - 20 || lastTargetPoint.getX() > targetX + 20 ) {
						//The target user moved.
						power = Math.abs(calculatePower(angle,
								Math.abs((myX-targetX)), 
								Math.abs((myY-targetY)), 
								wind));
						logger.debug("敌人在X轴移动超过20像素. angle:{}, power:{}", angle, power);
					} else if ( lastTargetPoint.getY() < targetY - 50 || lastTargetPoint.getY() > targetY + 50 ) {
						power = Math.abs(calculatePower(angle,
								Math.abs((myX-targetX)), 
								Math.abs((myY-targetY)), 
								wind));
						logger.debug("敌人在Y轴移动超过50像素. angle:{}, power:{}", angle, power);
					} else {
						//The target user does not move
						if ( lastHitPoint != null && lastHitPoint.getX() < targetX+60 && lastHitPoint.getX() > targetX-60 &&
								lastHitPoint.getY() < targetY+60 && lastHitPoint.getY() > targetY - 60 ) {
							logger.debug("Last power hit the user. use it");
							logger.debug("上一次的打击命中，继续保持. angle:{}, power:{}", angle, power);
						} else {
							if ( totalRoundNotHit == null ) {
								totalRoundNotHit = new Integer(1);
							} else {
								totalRoundNotHit = totalRoundNotHit.intValue() + 1;
							}
							boolean recalcuate = totalRoundNotHit.intValue() > 5;
							if ( recalcuate ) {
								//如果很久没有击中用户了，需要更换一下策略
								aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_ANGLE, null);
								aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_POWER, null);
								aiUser.putUserData(AIManager.BATTLE_TOTAL_ROUND_NOT_HIT, null);
								angle = 50 + (int)(MathUtil.nextDouble()*20); 
								if ( myX > targetX ) {
									angle = 180 - angle;
								}
								power = Math.abs(calculatePower(angle,
										Math.abs((myX-targetX)), 
										Math.abs((myY-targetY)), 
										wind));
								logger.debug("连续5回合没有命中了，重新计算. angle:{}, power:{}", angle, power);
							} else {
								//否则继续统计是否命中
								aiUser.putUserData(AIManager.BATTLE_TOTAL_ROUND_NOT_HIT, totalRoundNotHit);
								
								if ( lastHitPoint == null ) {
									//子弹出屏幕了, 计算起始力度
									aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_ANGLE, null);
									aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_POWER, null);
									/**
									 * 当敌人在X轴距离我较近，但Y轴距离我较高时，使用大力度平直射击
									 */
									if ( myY - targetY >= 200 ) {
										double a = Math.abs(myY - targetY);
										double b = Math.abs(myX - targetX);
										double c = FastMath.sqrt(a*a+b*b);
										double ang = FastMath.asin(a/c)*180/Math.PI;
										if ( myX > targetX ) {
											ang = 180 - (ang + 10);
										}
										angle = (int)ang;
										power = 90;
										logger.debug("当敌人在X轴距离我较近，但Y轴距离我较高时，使用大力度平直射击. angle:{}, power:{}", angle, power);
									} else {
										angle = calculateAngle(myX, myY, targetX, targetY);
										logger.debug("为第一次射击计算数值. angle:{}, power:{}", angle, power);
									}
								} else {
									Boolean aiHurtEnemy = (Boolean)aiUser.getUserData(AIManager.BATTLE_AI_HURTENEMY);
									if ( aiHurtEnemy == null || !aiHurtEnemy.booleanValue() ) {
										int distancex = lastHitPoint.getX() - targetX;
										if ( distancex < 0 && angle > 90 ) {
											power -= Math.abs(distancex)/40.0f;
											logger.debug("Last power is too high to hit. distance:{}",distancex);
										} else if ( distancex < 0 && angle < 90 ) {
											power += Math.abs(distancex)/40.0f;
											logger.debug("Last power is too low to hit. distance:{}",distancex);
										} else if ( distancex > 0 && angle > 90 ) {
											power += distancex/40.0f;
											logger.debug("Last power is too low to hit. distance:{}",distancex);
										} else if ( distancex > 0 && angle < 90 ) {
											power -= distancex/40.0f;
											logger.debug("Last power is too high to hit. distance:{}",distancex);
										}
										int distancey = lastHitPoint.getY() - targetY;
										if ( distancey > 200 ) {
											//玩家在AI的高点位置，之前的炮弹落点太低了
											power += 10;
										} else if ( distancey > 0 ) {
											//angle += distancey/60;
											power += distancey/30;
										}
									} else {
										logger.debug("AI已经命中目标，不需要再次调整. angle:{}, power:{}", angle, power);	
									}
								}
							}
							
							if ( power <= 0 ) {
								power = 10;
							}
							logger.debug("根据先前的打击情况进行调整. angle:{}, power:{}", angle, power);
						}
					}
				}
			} else {
				power = Math.abs(calculatePower(angle,
					Math.abs((myX-targetX)), 
					Math.abs((myY-targetY)), 
					wind));
				//稍减小一点力度
				if ( angle <= 40 || angle >= 160 ) {
					power -= 5;
				}
				logger.debug("上一回合没有力度值，重新计算. angle:{}, power:{}", angle, power);
			}	
			
		}
				
		logger.debug("AI my position:({},{}), target:({},{}), angle: {}, power: {}", 
				new Object[]{myX, myY, targetX, targetY, angle, power});
		aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_ANGLE, angle);
		aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_POWER, power);
		aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_TARGETPOINT, new SimplePoint(targetX, targetY));
		aiUser.putUserData(AIManager.BATTLE_LAST_ROUND_HITPOINT, null);
		
		BceRoleAttack.Builder roleAttackBuilder = BceRoleAttack.newBuilder();
		roleAttackBuilder.setAngle(angle*1000);
		roleAttackBuilder.setAtkAngle(angle*1000);
		roleAttackBuilder.setPower(power);
		roleAttackBuilder.setUserx(myX);
		roleAttackBuilder.setUsery(myY);
		roleAttackBuilder.setDirection(0);
		BceRoleAttack attack = roleAttackBuilder.build();
		
		return attack;
	}

	/**
	 * @param myX
	 * @param myY
	 * @param targetX
	 * @param targetY
	 * @param power
	 * @return
	 */
	private static int calculateAngle(int myX, int myY, int targetX, int targetY) {
		int angle;
		if ( myY > targetY + 200 ) {
			//高抛
			angle = 55 + (int)(MathUtil.nextDouble()*20);
			logger.debug("玩家位置高于我200像素，使用高抛. angle:{}", angle);
		} else {
			//平射
			angle = 40 + (int)(MathUtil.nextDouble()*20);
			logger.debug("玩家位置在200像素内，使用平射. angle:{}", angle);
		}
		//纠正方向
		if ( myX > targetX ) {
			angle = 180 - angle;
		}
		return angle;
	}
	
	/**
	 * Random use a tool
	 */
	public static final void roleUseTool(User aiUser, IoSession serverIoSession, BseRoundStart roundStart) {
		//Use BuffTool
		if ( aiUser == null || aiUser.getSessionKey() == null ) return;
		final String aiUserSessionId = aiUser.getSessionKey().toString();
		if ( roundStart.getSessionId().equals(aiUserSessionId) ) {
			int aiUserIndex = 0;
			boolean loop = true;
			for ( int i=0; loop && i<roundStart.getUserIdCount(); i++ ) {
				String sessionId = roundStart.getUserId(i);
				
				if (aiUserSessionId.equals(sessionId)) {
					aiUserIndex = i;
					loop = false;
				}
			}
			final int blood = roundStart.getBlood(aiUserIndex);
			final int energy = roundStart.getEnergy(aiUserIndex);
			final int thew = roundStart.getStrength(aiUserIndex);
			ArrayList toolList = UserCalculator.calculateMaxHurtRatio(aiUser, thew);
			for ( int i=0; i<toolList.size(); i++ ) {
				BuffToolIndex toolIndex = (BuffToolIndex)toolList.get(i);
				BceRoleUseTool useTool = BceRoleUseTool.newBuilder().setSlot(toolIndex.slot()).build();
				AIManager.getInstance().sendServerMessageToAIClient(
						serverIoSession, aiUser.getSessionKey(), useTool, 2000, TimeUnit.MILLISECONDS);
			}
			/*
			BuffToolIndex toolIndex = BuffToolIndex.values()[(int)(MathUtil.nextDouble()*(BuffToolIndex.values().length-3))];
			int index = toolIndex.slot();
			if ( blood < blood * 0.2f ) {
				index = BuffToolIndex.UserTool1.ordinal();
			}
			
			BceRoleUseTool useTool = BceRoleUseTool.newBuilder().setSlot(index).build();
			AIManager.getInstance().sendServerMessageToAIClient(
					serverIoSession, aiUser.getSessionKey(), useTool, 2000, TimeUnit.MILLISECONDS);
			*/
			if ( energy >= 100 ) {
				BceRoleUseTool useTool = BceRoleUseTool.newBuilder().setSlot(
						BuffToolIndex.UsePower.ordinal()).build();
				AIManager.getInstance().sendServerMessageToAIClient(
						serverIoSession, aiUser.getSessionKey(), useTool, 2000, TimeUnit.MILLISECONDS);
			}
			
			UserCalculator.calculateMaxHurtRatio(aiUser);
		}
	}
	
	/**
	 * Random use a tool
	 */
	public static final void roleUseTool(User aiUser, IoSession serverIoSession, int blood, int energy) {
		//Use BuffTool
		BuffToolIndex toolIndex = BuffToolIndex.values()[(int)(MathUtil.nextDouble()*(BuffToolIndex.values().length-3))];
		int index = toolIndex.slot();
		if ( blood < blood * 0.2f ) {
			index = BuffToolIndex.UserTool1.ordinal();
		}
		
		BceRoleUseTool useTool = BceRoleUseTool.newBuilder().setSlot(index).build();
		AIManager.getInstance().sendServerMessageToAIClient(
				serverIoSession, aiUser.getSessionKey(), useTool, 2000, TimeUnit.MILLISECONDS);

		if ( energy >= 100 ) {
			useTool = BceRoleUseTool.newBuilder().setSlot(
					BuffToolIndex.UsePower.ordinal()).build();
			AIManager.getInstance().sendServerMessageToAIClient(
					serverIoSession, aiUser.getSessionKey(), useTool, 2000, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * Random use a tool
	 */
	public static final void roleUseGivenTool(User aiUser, IoSession serverIoSession, 
			BuffToolIndex toolIndex) {
		//Use BuffTool
		BceRoleUseTool useTool = BceRoleUseTool.newBuilder().setSlot(
				toolIndex.slot()).build();
		AIManager.getInstance().sendServerMessageToAIClient(
				serverIoSession, aiUser.getSessionKey(), useTool, 2000, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 
	 * @param aiUser
	 * @param serverIoSession
	 */
	public static final void roleSendExpress(User aiUser, 
			IoSession serverIoSession, int delay) {
		String message = "/e"+(int)((MathUtil.nextDouble()*8));
		roleChat(aiUser, serverIoSession, message, delay);
	}
	
	/**
	 * 
	 * @param aiUser
	 * @param serverIoSession
	 */
	public static final void roleChat(User aiUser, IoSession serverIoSession, 
			String message, int delay) {
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgType(ChatType.ChatCurrent.ordinal());
		builder.setMsgContent(message);
		
		AIManager.getInstance().sendServerMessageToAIClient(
				serverIoSession, aiUser.getSessionKey(), builder.build(), 
				delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Change the round to user.
	 * @param aiUser
	 * @param serverIoSession
	 * @param delay
	 */
	public static final void askRoundOver(User aiUser, IoSession serverIoSession, int delay) {
		BceAskRoundOver.Builder roundOver = BceAskRoundOver.newBuilder();
		AIManager.getInstance().sendServerMessageToAIClient(
				serverIoSession, aiUser.getSessionKey(), roundOver.build(), 
				delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Use the (hitx,hity) point and given angle to calculate the power needed.
	 * 
	 * @param angle
	 * @param hitx
	 * @param hity
	 * @return
	 */
	public static final int calculatePower(int angle, int hitx, int hity, int wind) {
		//Caculate the running time
		//0.055
		//double K = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_K, 0.059081);
		//double F = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_F, 0.075);
		//int    g = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.BATTLE_ATTACK_G, 760);
		double rad = angle/180.0*Math.PI;
		double sin = FastMath.sin(rad);
		double cos = FastMath.cos(rad);
		double tx = hitx/3;
		int ty = 0;

		double a = sin;
		double b = -ty;
		double c = 0;
		double d = Math.abs(tx*tx / (2*cos));
		int power = (int)MathUtil.solveCubicEquation(a, b, c, d);
		logger.debug("a:{},b:{},c:{},d:{},wind:{},power:{}", new Object[]{a, b, c, d,wind, power});
		if ( power < 0 ) {
			power = -power;
		}
		if ( power > 100 ) {
			power = 100;
		}
		/**
		 * wind < 0 风向向右侧
		 * wind > 0 风向向左侧
		 */
		if ( wind < 0 && angle > 90 ) {
			power += -wind * 2 + 5;
		} else if ( wind < 0 && angle < 90 ) {
			/**
			 * 村口小桥顺风情况下计算的力度偏小，所以
			 * 这里去掉了风力的数值
			 * 2013-01-14
			 */
			//power -= -wind * 2 - 5;
		} else if ( wind > 0 && angle < 90 ) {
			power += wind * 2 + 5;
		} else if ( wind > 0 && angle > 90 ) {
			power -= wind * 2 - 5;
		}
		return (int)power;
	}

}
