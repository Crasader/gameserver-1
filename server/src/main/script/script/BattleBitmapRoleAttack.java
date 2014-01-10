package script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleAuditItem;
import com.xinqihd.sns.gameserver.battle.BattleBitSetBullet;
import com.xinqihd.sns.gameserver.battle.BattleBitSetMap;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.battle.BattleManager;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.BattleUserAudit;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.BulletTrack;
import com.xinqihd.sns.gameserver.battle.HurtUserDesc;
import com.xinqihd.sns.gameserver.battle.RoleAttack;
import com.xinqihd.sns.gameserver.battle.RoleStatus;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.boss.BossWinType;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.geom.BitSetImage;
import com.xinqihd.sns.gameserver.geom.BitmapUtil;
import com.xinqihd.sns.gameserver.proto.XinqiAtkBltInfo.AtkBltInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleAttack.BseRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiHurtUserInfo.HurtUserInfo;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Calculate the attack data.
 * 
 * @author wangqi
 *
 */
public class BattleBitmapRoleAttack {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleBitmapRoleAttack.class);

	public static ScriptResult func(Object[] parameters) {
		int expRate = 1;
    
		ScriptResult result = ScriptManager.checkParameters(parameters, 6);
		if ( result != null ) {
			return result;
		}
		
		Battle battle    				 = (Battle)parameters[0];
		BattleUser battleUser    = (BattleUser)parameters[1];
		Collection battleUsers   = (Collection)parameters[2];
		BattleBitSetMap battleBitSetMap    = (BattleBitSetMap)parameters[3];
		BceRoleAttack roleAttack = (BceRoleAttack)parameters[4];
		int roundWind        = (Integer)parameters[5];
		BulletTrack[] clientBulletTracks = (BulletTrack[])parameters[6];
		
		if ( battleUser.getTools().contains(BuffToolType.Wind) ) {
			roundWind = - roundWind;
		}
		
		BitSetImage battleMap = battleBitSetMap.getMapBitSet();
		BseRoleAttack.Builder bseRoleAttackBuilder = BseRoleAttack.newBuilder();
		
//		String userName = battleUser.getUser().getUsername();
		String roleName = battleUser.getUser().getRoleName();
		int userx = roleAttack.getUserx();
		int usery = roleAttack.getUsery();
		if ( usery <= 0 ) {
			usery = 0;
		}
		int angle = roleAttack.getAngle();
		//if ( angle > 1000 ) {
		angle /= 1000;
		//}

		int power = roleAttack.getPower();

		if ( logger.isDebugEnabled() ) {
			logger.info("RoleAttack: userPos: ({}, {}), angle: {}, power: {}", 
					new Object[]{userx, usery, angle, power});
		}
		battleUser.setPosX(userx);
		battleUser.setPosY(usery);
		
		String bulletId = Constant.EMPTY;
		List propDataList = battleUser.getUser().getBag().getWearPropDatas();
		PropData propData = (PropData)(propDataList.get(PropDataEquipIndex.WEAPON.ordinal()));
		if ( propData != null ) {
			WeaponPojo weapon = (WeaponPojo)propData.getPojo();
			bulletId = weapon.getBullet();
		}
		BattleBitSetBullet battleBullet = BattleDataLoader4Bitmap.getBattleBulletByName(bulletId);
		if ( battleBullet == null ) {
			logger.info("The bulletId {} is not found. Script will fail", bulletId);
			result = new ScriptResult();
			result.setType(ScriptResult.Type.SCRIPT_FAIL);
			return result;
		}
		
		int bulletMode = 0;
		//TODO 第二位：1：使用了冰冻；第三位：1：使用了引导；
		boolean userPower = false, useFly = false, useIce = false, useGuide = false;
		if ( battleUser.isPowerAttack() ) {
			bulletMode |= 8;
			userPower = true;
			battleUser.setEnergy(0);
			logger.debug("Battle user {} apply power attack: {}", roleName, bulletMode);
		} else if ( battleUser.getTools().contains(BuffToolType.Fly) ) {
			bulletMode |= 1;
			useFly = true;
			logger.debug("Battle user {} apply fly: {}", roleName, bulletMode);
		} else if ( battleUser.getTools().contains(BuffToolType.Ice) ) {
			bulletMode |= 2;
			useIce = true;
			logger.debug("Battle user {} apply ice: {}", roleName, bulletMode);
		} else if ( battleUser.getTools().contains(BuffToolType.Guide ) ) {
			bulletMode |= 4;
			useGuide = true;
			logger.debug("Battle user {} apply guide: {}", roleName, bulletMode);
		}

		if ( logger.isDebugEnabled() ) {
			logger.debug("bulletMode: {}", Integer.toBinaryString(bulletMode));
		}

		RoleAttack rAttack = BattleBulletCount.getBulletTracks(battle, battleUser, roleAttack, roundWind);
		BulletTrack[] bullets = rAttack.getBulletTracks();
		
		int bulletCount = 0;
		try {
			bulletCount = Math.min(bullets.length, clientBulletTracks.length);
		} catch (Exception e) {
			logger.debug("Failed to get bullet count", e);
		}
		
		//Calcualte the bombx and bomby
		int bombx = userx;
		if ( angle <= 90 ) {
			bombx += 15;
		} else {
			bombx -= 15;
		}
		int bomby = usery - 15;
		
		//Setup the common roleAttack data.
		bseRoleAttackBuilder.setSessionId(battleUser.getUserSessionKey().toString());
		bseRoleAttackBuilder.setAngle(angle);
		bseRoleAttackBuilder.setBltMode(bulletMode);
		bseRoleAttackBuilder.setBltCount(bulletCount);
		bseRoleAttackBuilder.setBltQuantity(rAttack.getBulletQuatity());
		bseRoleAttackBuilder.setBltAtkTimes(rAttack.getAttackTimes());
		bseRoleAttackBuilder.setUserx(bombx);
		bseRoleAttackBuilder.setUsery(bomby);
		bseRoleAttackBuilder.setDirection(roleAttack.getDirection());
		bseRoleAttackBuilder.setPower(power);
		
		double maxFlyingSeconds = 0;
		boolean isBulletHitGround = false;
		
		//update user statistic
		battleUser.addTotalAttack(bulletCount);
		
		for ( int i=0; i<bulletCount; i++ ) {
  		//TODO debug
			/*
			ArrayList line = new ArrayList();
			bullets[i] = BitmapUtil.caculateBulletTrack(BitmapUtil.DEFAULT_SCALE, bombx, bomby, 
					power, angle+bulletAngleDiff*i, roundWind.intValue(), battleMap, battleMap.getHeight(), line);
			*/
			if ( clientBulletTracks == null ) {
				bullets[i].startX = bombx;
				bullets[i].startY = bomby;
				bullets[i] = BitmapUtil.caculateBulletTrack(
					BitmapUtil.DEFAULT_SCALE, bullets[i], battleMap, 
					battleMap.getHeight(), null, battle);
				
			} else {
				bullets[i] = clientBulletTracks[i];
			}
			if ( bullets[i].flyingSeconds > maxFlyingSeconds ) {
				maxFlyingSeconds = bullets[i].flyingSeconds;
			}
			
			AtkBltInfo.Builder atkBltInfoBuilder = AtkBltInfo.newBuilder();
			
			atkBltInfoBuilder.setBltIdx(i);
			atkBltInfoBuilder.setSpeedX((int)bullets[i].speedX);
	    atkBltInfoBuilder.setSpeedY((int)bullets[i].speedY);
	    
			if ( bullets[i].result == 1 && bullets[i].hitPoint != null ) {
				//返回结果：1：爆炸 2：出界
				if ( bullets[i].result > 0 ) {
					atkBltInfoBuilder.setResult(bullets[i].result);	
				} else {
					atkBltInfoBuilder.setResult(1);
				}
				
				atkBltInfoBuilder.setTime((int)(bullets[i].flyingSeconds*1000));
				isBulletHitGround = true;
								
	      // double blty = starty - (t * speedy - g * t * t / 2);

				//Calculate the force on ground
				BitSetImage bulletGeometry = battleBullet.getBullet();
				if ( battleUser.isPowerAttack() ) {
					logger.debug("User starts a power attack");
					bulletGeometry = battleBullet.getsBullet();
				}
				
				int hitPointX = bullets[i].hitPoint.getX();
				int hitPointY = bullets[i].hitPoint.getY();
				logger.debug("The hit point is {}, {}", hitPointY, hitPointY);
				atkBltInfoBuilder.setBltX(hitPointX);
				atkBltInfoBuilder.setBltY(hitPointY);
								
				//Check if the bullet attack user
				double scaleBullet = 1.0;
				int pngNum = 100;
				if ( clientBulletTracks == null ) {
					scaleBullet = calculateScale(angle);
					pngNum = (int)(100*scaleBullet);					
				} else {
					pngNum = bullets[i].pngNum;
				}
				for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
					BattleUser bUser = (BattleUser) iterator.next();
					//Ignore the dead user
					if ( bUser.containStatus(RoleStatus.DEAD) ) continue;
					
					double ratio = calculateRatio(battleUser, bulletGeometry, bullets[i], bUser, 
							battleUsers, useGuide);
					
					if ( battle.getBoss() != null && battle.getBoss().getBossPojo().getBossWinType() == 
							BossWinType.COLLECT_DIAMOND ) {
						//钻石副本不判断命中
						ratio = 0.0;
					}
					if ( ratio > 0.0 ) {
						/**
						 * Do not adjust the hit point to user
						 * 2012-11-19
						 */
						//atkBltInfoBuilder.setBltX(bUser.getPosX());
						//atkBltInfoBuilder.setBltY(bUser.getPosY());
						atkBltInfoBuilder.setBltX(bullets[i].hitPoint.getX());
						atkBltInfoBuilder.setBltY(bullets[i].hitPoint.getY());
//						scaleBullet = scaleBullet/2;
//						if ( scaleBullet < 0.25 ) {
//							scaleBullet = 0.25;
//						}
//						pngNum = (int)(scaleBullet * 100);
						logger.debug("user {} is being attacked, pngNum: {}", roleName, pngNum);
						int finalHurt = 0;
						if ( useIce ) {
							logger.debug("user {} is now in frozen status", roleName);
							bUser.addStatus(RoleStatus.ICED);
							bUser.setFrozenStartRound(battle.getRoundCount());
							
							HurtUserInfo.Builder hurtUserInfo = HurtUserInfo.newBuilder();
							hurtUserInfo.setUserId(bUser.getUserSessionKey().toString());
							hurtUserInfo.setBlood(bUser.getBlood());
							int userEnergy = bUser.getEnergy() + calculateEnergy(battle, battleUser, bUser, finalHurt);
							hurtUserInfo.setEnergy(calculateEnergy(battle, battleUser, bUser, finalHurt));
							bUser.setEnergy(userEnergy);
							hurtUserInfo.addDesc(HurtUserDesc.Frozen.ordinal());
							
							//Update user status if he is in either hidden or iced status
							int userMode = processUserMode(bUser, useIce, false);
							hurtUserInfo.setUserMode(userMode);
							
							atkBltInfoBuilder.addHurtUser(hurtUserInfo.build());
						} else if ( useFly ) {
							logger.debug("user {} will fly to that position", roleName);
						} else {
							if ( bUser.containStatus(RoleStatus.ICED) ) {
								//冰冻后或死亡状态下被攻击不应造成伤害
								HurtUserInfo.Builder hurtUserInfo = HurtUserInfo.newBuilder();
								hurtUserInfo.setUserId(bUser.getUserSessionKey().toString());
								hurtUserInfo.setBlood(bUser.getBlood());
								int userEnergy = bUser.getEnergy() + calculateEnergy(battle, battleUser, bUser, finalHurt);
								hurtUserInfo.setEnergy(calculateEnergy(battle, battleUser, bUser, finalHurt));
								hurtUserInfo.addDesc(HurtUserDesc.Unfrozen.ordinal());
								bUser.setEnergy(userEnergy);
								
								//Update user status if he is in either hidden or iced status
								int userMode = processUserMode(bUser, useIce, false);
								hurtUserInfo.setUserMode(userMode);
								
								atkBltInfoBuilder.addHurtUser(hurtUserInfo.build());
								
								bUser.removeStatus(RoleStatus.ICED);
								//增加delay回合数值，避免解冻后连续回合
								int currentRound = battle.getRoundCount();
								int startRound = battleUser.getFrozenStartRound();
								int delay = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.DELAY_ROLE_SAVE, 100);
								int addDelay = (currentRound-startRound)*delay;
								bUser.setDelay(battleUser.getDelay()+addDelay);
								logger.debug("user is unfrozen and his delay should be added {}", addDelay);
							} else if ( bUser.containStatus(RoleStatus.DEAD) || bUser.getBlood() <= 0 ) {
								//死亡状态下被攻击不应造成伤害
							} else {
								RoomType roomType = battle.getBattleRoom().getRoomLeft().getRoomType();
								
								double criticalRatio = 0.0;
								HurtUserInfo.Builder hurtUserInfo = HurtUserInfo.newBuilder();

								ArrayList tools = battleUser.getTools();
								criticalRatio = UserCalculator.calculateCritialAttack(battleUser.getUser());
								
								if ( criticalRatio > 1.0 ) {
									//bulletMode |= 8;
									hurtUserInfo.setUserMode( hurtUserInfo.getUserMode() | 32 );
									hurtUserInfo.addDesc(HurtUserDesc.Critical.ordinal());
									bseRoleAttackBuilder.setBltMode(bulletMode);
									logger.debug("user {} uses a critical attack: {}", roleName, criticalRatio);
								}
								finalHurt = UserCalculator.calculateHurt(
										battleUser.getUser(), bUser.getUser(), tools, ratio, criticalRatio);
								if ( useGuide ) {
									double hurtRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_GUIDE_HURT_RATIO, 0.4);
									finalHurt = (int)(finalHurt*hurtRatio);
									hurtUserInfo.addDesc(HurtUserDesc.Guided.ordinal());
									logger.debug("Since use guide, the finalHurt will be {}", finalHurt);
								}
								if ( userPower ) {
									double hurtRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.TOOL_POWER_VALUE, 1.5);
									finalHurt = (int)(finalHurt*hurtRatio);
									logger.debug("Since use power, the finalHurt will be {}", finalHurt);
								}
								if ( battleUser.getTools().contains(BuffToolType.AttackThreeBranch) ) {
									double hurtRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_TREE_BRANCH_HURT_RATIO, 0.4);
									finalHurt = (int)(finalHurt*hurtRatio);
									logger.debug("Since use treebrach, the finalHurt will be {}", finalHurt);
								}
								if ( battleUser.getTools().contains(BuffToolType.AttackTwoMoreTimes) ) {
									double hurtRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_TWO_CONTINUE_HURT_RATIO, 0.6);
									finalHurt = (int)(finalHurt*hurtRatio);
									logger.debug("Since use twomore, the finalHurt will be {}", finalHurt);
								}
								if ( battleUser.getTools().contains(BuffToolType.AttackOneMoreTimes) ) {
									double hurtRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_ONE_CONTINUE_HURT_RATIO, 0.85);
									finalHurt = (int)(finalHurt*hurtRatio);
									logger.debug("Since use onemore, the finalHurt will be {}", finalHurt);
								}
								boolean heatMyself = false;
								if ( bUser.getUserSessionKey().equals(battleUser.getUserSessionKey()) ) {
									//Hit itself
									heatMyself = true;
									if ( finalHurt >= bUser.getBlood() ) {
										//The user should not suicide itself
										bUser.setBlood(1);
									} else {
										bUser.setBlood(bUser.getBlood() - finalHurt);
									}
								} else {
									if ( finalHurt > bUser.getBlood() ) {
										finalHurt = bUser.getBlood() + 1;
									} else if ( finalHurt <=0  ) {
										finalHurt = 1;
									}
									//Hit enemies
									if ( ratio < 0.3 ) {
										hurtUserInfo.addDesc(HurtUserDesc.Rough.ordinal());
									} else if ( ratio > 0.8 && !useGuide ){
										hurtUserInfo.addDesc(HurtUserDesc.Accurate.ordinal());
									}
									if ( finalHurt * 1.0f / bUser.getUser().getBlood() >= 0.85f ) {
										hurtUserInfo.addDesc(HurtUserDesc.Deadly.ordinal());
									}
									//For training room
									if ( roomType == RoomType.TRAINING_ROOM ) {
										finalHurt = bUser.getUser().getBlood()/3 + 1;
									}
									bUser.setBlood(bUser.getBlood() - finalHurt);
									bUser.addTotalSelfHurt(finalHurt);
									//Create the audit
									BattleUserAudit audit = battleUser.getHurtUser(bUser.getUserSessionKey());
									audit.setBattleUser(bUser);
									audit.setHurtBlood(audit.getHurtBlood()+finalHurt);
									//检查是否符合精确打击AccurateNum.
									if ( ratio >= 0.9 && !useGuide ) {
										audit.setAccurateNum( audit.getAccurateNum()+1 );
									}

								  //update statistic
									if ( !bUser.getUserSessionKey().equals(battleUser.getUserSessionKey())
											&& bUser.getCamp() != battleUser.getCamp() ) {
										//User does not hit himself or his own camp users.
										//battleUser.addTotalEnemyHurt(finalHurt);
										battleUser.addTotalHit(1);
										audit.setEnemy(true);
									}
								}

								BattleUserAudit audit = battleUser.getHurtUser(bUser.getUserSessionKey());
								if ( bUser.getBlood() <= 0 ) {
									bUser.clearStatus();
									bUser.addStatus(RoleStatus.DEAD);
									
									//检查是否符合SecondKill:秒杀
									String secondKillMark = (String)audit.getBattleAuditItem(BattleAuditItem.SecondKill);
									if ( secondKillMark == null ) {
										audit.setSecondKillNum(true);
										audit.setBattleAuditItem(BattleAuditItem.SecondKill, Constant.ONE);
										//logger.debug("{} secondKill {}", battleUser.getUser().getRoleName(), bUser.getUser().getRoleName());
										
										//String secondKillContent = Text.text("battle.secondkill", 
										//		new Object[]{bUser.getUser().getRoleName(), battleUser.getUser().getRoleName()});
										//ChatManager.getInstance().processChatToWorldAsyn(null, secondKillContent);
									}
									audit.setKilled(true);
									bUser.setKillerUser(battleUser);
									if (  !bUser.getUser().isAI() && 
											bUser.getUser().getViplevel() >= 5 &&
											!battleUser.getUser().isAI()
											&& battleUser.getUser().getViplevel() == 0 ) {
										String content = Text.text("battle.diaosi", 
												new Object[]{battleUser.getUser().getRoleName(), bUser.getUser().getViplevel(), bUser.getUser().getRoleName()});
										ChatManager.getInstance().processChatToWorldAsyn(null, content);
									}
								} else {
									//非秒杀
									String secondKillMark = (String)audit.getBattleAuditItem(BattleAuditItem.SecondKill);
									if ( secondKillMark == null ) {
										audit.setSecondKillNum(false);
										audit.setBattleAuditItem(BattleAuditItem.SecondKill, Constant.ZERO);
									}
						    	//如果玩家没有体力值了，不能产生宝箱
						    	boolean hasActionPoint = RoleActionManager.getInstance().
						    			checkUserHasRoleActionPoint(battleUser.getUser());
						    	if ( hasActionPoint && !heatMyself ) {
										//Generate the treasure box
										//Generate random treasure box
										if ( roomType == RoomType.SINGLE_ROOM || 
												roomType == RoomType.MULTI_ROOM ||
												roomType == RoomType.PVE_ROOM ) {
											ScriptHook hook = ScriptHook.BATTLE_BOX_REWARD;
											if ( roomType == RoomType.PVE_ROOM ) {
												hook = ScriptHook.PVE_BATTLE_BOX_REWARD;
											}
											if ( finalHurt*1.0/battleUser.getUser().getBlood() > 0.3 ){
												int tBoxCount = battle.getTreasureBox().size();
												for ( ; tBoxCount<1; tBoxCount++ ) {
													Reward reward = (Reward)
															GameContext.getInstance().getScriptManager().
															runScriptForObject(hook, 
																	new Object[]{battleUser.getUser(), bullets[i].topPoint});
													battle.getTreasureBox().put(tBoxCount, reward);
													logger.debug("TreasureBox index:{}, point:{}", tBoxCount, bullets[i].hitPoint);
												}
											}
										}
						    	}
								}

								hurtUserInfo.setUserId(bUser.getUserSessionKey().toString());
								hurtUserInfo.setBlood(bUser.getBlood());
								int userEnergy = bUser.getEnergy() + 
										calculateEnergy(battle, battleUser, bUser, finalHurt);
								hurtUserInfo.setEnergy(userEnergy);
								bUser.setEnergy(userEnergy);
								
								//Update user status if he is in either hidden or iced status
								int userMode = processUserMode(bUser, useIce, criticalRatio > 1.0);
								hurtUserInfo.setUserMode(userMode);
								
								atkBltInfoBuilder.addHurtUser(hurtUserInfo.build());
							} // else is forzen
						}
					} else {
						logger.debug("User bullet {} does not hit target. userx:{}", battleUser.getUser().getRoleName(), bUser.getPosX());
					}
				}
				
				/*
				for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
					BattleUser bUser = (BattleUser) iterator.next();
					if ( bUser.containStatus(RoleStatus.DEAD) ) {
						if ( !battle.containsKill(bUser.getUserSessionKey()) ) {
							battle.getRoundOwner().addTotalKill(bUser.getUserSessionKey());
							battle.addTotalKill(bUser.getUserSessionKey());
						}
					}
				}
				*/
				
				//check if the map can be destroyed.
				if ( battleBitSetMap.getMapPojo().isDamage() ) {
					atkBltInfoBuilder.setPngNum(pngNum);
				} else {
					atkBltInfoBuilder.setPngNum(0);
				}
				
				if (  !BattleManager.getInstance().isUseDistributed() && battleBitSetMap.getMapPojo().isDamage() ) {
					bulletGeometry = BitmapUtil.scaleBitSetImage(bulletGeometry, scaleBullet);
					try {
						battleBitSetMap.getMapBitSet().substract(bulletGeometry, bullets[i].hitPoint.getX(), 
								bullets[i].hitPoint.getY(), BitmapUtil.DEFAULT_SCALE);
						//TODO debug
						/*
						BitmapUtil.drawBitSetImage(battleBitSetMap.getMapBitSet(), new File("battle_mapbitset.png"));
						String mapImgDir = System.getProperty("mapImgdir", "data/mapimg");
						BitmapUtil.drawBulletTrack(mapImgDir, battleBitSetMap, battleBitSetMap.getMapBitSet(), 
								line, bullets[i], BitmapUtil.DEFAULT_SCALE);
						*/
					} catch (Exception e) {
						logger.debug("map geometry: {}", e.getMessage());
					}
					
					//Check if the user will drop
					/**
					 * It leaves for the client to check.
					 * However, if the server checks map damage, it code should be enabled.
					 * wangqi 2012-7-21
					 */
					/*
					for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
						BattleUser bUser = (BattleUser) iterator.next(); 
						boolean willDrop = BitmapUtil.canItDropToBottom(bUser.getPosX(), bUser.getPosY(), BitmapUtil.DEFAULT_SCALE, battleMap);
						if ( willDrop ) {
							logger.debug("The user {} will drop at pox:({},{})", new Object[]{roleName, bUser.getPosX(), bUser.getPosY()});
							bUser.clearStatus();
							bUser.addStatus(RoleStatus.DEAD);
							//The user kill rival by dropping it.
							battleUser.addTotalKill(bUser.getUserSessionKey());
							
							DropUserInfo.Builder dropUserInfoBuilder = DropUserInfo.newBuilder(); 
							dropUserInfoBuilder.setUserId(bUser.getUserSessionKey().toString());
							dropUserInfoBuilder.setPosX(bUser.getPosX());
							dropUserInfoBuilder.setPosY(bUser.getPosY());
							dropUserInfoBuilder.setUserDead(1);
							atkBltInfoBuilder.addDropUser(dropUserInfoBuilder.build());
						}
					}
					*/
				}
				
			} else {        
				//返回结果：1：爆炸 2：出界
				atkBltInfoBuilder.setResult(bullets[i].result);
				atkBltInfoBuilder.setTime((int)(bullets[i].flyingSeconds*1000));
				atkBltInfoBuilder.setPngNum(-1);
			  atkBltInfoBuilder.setBltX(Integer.MAX_VALUE);
	      atkBltInfoBuilder.setBltY(Integer.MAX_VALUE);
				
				logger.debug("It does not hit the ground. bullet: {}", bullets[i]);
			}
    
			bseRoleAttackBuilder.addBltInfo(atkBltInfoBuilder.build());
		}
		
		//Send the response back to all users.
		//boolean sendRoundOver = false;
		/*
		if ( useFly && !isBulletHitGround ) {
			logger.debug("User {} use fly but does not hit on ground.", battleUser.getUser().getRoleName());
			sendRoundOver = true;
		} else {
		}
		*/
		for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
			BattleUser user = (BattleUser) iterator.next();
			GameContext.getInstance().writeResponse(user.getUserSessionKey(), bseRoleAttackBuilder.build());
		}

		
		ArrayList list = new ArrayList();
		list.add(maxFlyingSeconds);
		//list.add(sendRoundOver?Boolean.TRUE:Boolean.FALSE);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
	/**
	 * 设置用户当前的状态
	 * userMode = 5 [default = 0];	
	 * //用户状态 从低位到高位：
	 * 第一位：1：是否受到伤害（有血量变化）；
	 * 第二位：1：隐身；
	 * 第三位：1：被解除隐身；
	 * 第四位：1：被冰冻；
	 * 第五位：1：被解冰冻
	 * 第六位: 1: 暴击
	 * @param beingAttackedUser
	 */
	public static int processUserMode(BattleUser beingAttackedUser, boolean useIce, boolean critialAttack) {
	  //第一位：1：是否受到伤害（有血量变化）；
		int userMode = 0;
		if ( useIce ) {
			userMode |= 8;
		} else {
			userMode = 1;
			if ( critialAttack ) {
				userMode |= 32;
			}
			if ( beingAttackedUser.containStatus(RoleStatus.HIDDEN) ) {
				beingAttackedUser.clearStatus();
				beingAttackedUser.addStatus(RoleStatus.NORMAL);
				//第三位：1：被解除隐身；
				userMode |= 4;
			} else if ( beingAttackedUser.containStatus(RoleStatus.ICED) ) {
				beingAttackedUser.clearStatus();
				beingAttackedUser.addStatus(RoleStatus.NORMAL);
				//第五位：1：被解冰冻
				userMode |= 16;
			}
		}
		logger.debug("processUserMode: {}", Integer.toBinaryString(userMode));
		return userMode;
	}
	
	/**
	 * Calculate how much energy will grow when a user is hurt.
	 * 
	 * @param beingAttackedUser
	 * @param hurt
	 * @return
	 */
	public static int calculateEnergy(Battle battle, BattleUser attackUser, 
			BattleUser beingAttackedUser, int hurt) {
		
		int blood = beingAttackedUser.getUser().getBlood();
		double ratio = hurt * 1.0 / blood;
		int totalEnergy = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_DEFAULT_ENERGY, 100);
		int finalEnergy = finalEnergy = FastMath.min((int)(ratio * totalEnergy), totalEnergy);

		return finalEnergy;
	}
	
	/**
	 * Calculate the bullet scale ratio
	 * @param attackAngle
	 * @param hitUser
	 * @return
	 */
	public static double calculateScale(int attackAngle) {
		/*
		if ( attackAngle > 60 ) {
			return 1.0;
		} else if ( attackAngle > 30 ) {
			return 0.5;
		} else {
			return 0.25;
		}
		*/
		return 1.0;
	}
	
	/**
	 * Calculate the final target ratio
	 * @return
	 */
	public static double calculateRatio(BattleUser battleUser, 
			BitSetImage bulletGeometry, BulletTrack bullet, 
			BattleUser bUser, Collection battleUsers, boolean useGuide) {
		String roleName = battleUser.getUser().getRoleName();
		
		int userWidth = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.USER_BODY_WIDTH, 60);
		int userHeight = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.USER_BODY_HEIGHT, 60);
		double ratio = 0.0;
		if ( !BattleManager.getInstance().isUseDistributed() ) {
			userWidth /= BitmapUtil.DEFAULT_SCALE;
			userHeight /= BitmapUtil.DEFAULT_SCALE;
			
			ratio = BitmapUtil.intersectRatio(
					bulletGeometry.getWidth()/BitmapUtil.DEFAULT_SCALE,
					bulletGeometry.getHeight()/BitmapUtil.DEFAULT_SCALE,
					bullet.hitPoint.getX(),
					bullet.hitPoint.getY(),
					bUser.getPosX(),
					bUser.getPosY(), 
					userWidth, userHeight);
		} else {
			if ( useGuide ) {
				/**
				 * 计算距离落点最近的玩家，作为引导攻击的对象，否则
				 * 所有的玩家都会受伤
				 * wangqi 2012-11-11
				 */
				int minDistance = Integer.MAX_VALUE;
				BattleUser targetUser = null;
				int guideRange = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.BATTLE_GUIDE_RANGE, 150);
				for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
					BattleUser tmpUser = (BattleUser) iterator.next();
					if ( tmpUser.containStatus(RoleStatus.DEAD) ) continue;
					if ( tmpUser.getCamp() == battleUser.getCamp() ) continue;
					if ( bullet.hitPoint.getX() >= tmpUser.getPosX() - guideRange && 
							bullet.hitPoint.getX() <= tmpUser.getPosX() + guideRange ) {
						/**
						 * 该用户在目标落点范围内,再判断距离
						 */
						int xdis = (tmpUser.getPosX()-bullet.hitPoint.getX());
						int ydis = (tmpUser.getPosY()-bullet.hitPoint.getY());
						int distance = (xdis*xdis)+(ydis*ydis);
						if ( minDistance > distance ) {
							minDistance = distance;
							targetUser = tmpUser;
						}
					}
				}
				if ( targetUser != null && targetUser == bUser ) {
					logger.debug("The guide bullet hit an enemy user {}.", bUser.getUser().getRoleName());
					ratio = 1.0;
				} else {
					ratio = 0.0;
				}
			} else {
				ratio = BitmapUtil.intersectRatio(
						bulletGeometry.getWidth(),
						bulletGeometry.getHeight(),
						bullet.hitPoint.getX(),
						bullet.hitPoint.getY(),
						bUser.getPosX(),
						bUser.getPosY(), 
						userWidth, userHeight);
				/*
				logger.debug("#intersectRatio: {}, {}, {}, {}, {}, {}, {}, {}",
						new Object[]{
						bulletGeometry.getWidth(),
						bulletGeometry.getHeight(),
						bullet.hitPoint.getX(),
						bullet.hitPoint.getY(),
						bUser.getPosX(),
						bUser.getPosY(), 
						userWidth, userHeight});
						*/
			}
		}
		return ratio;
	}
	
}
