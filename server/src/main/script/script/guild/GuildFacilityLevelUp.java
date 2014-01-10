package script.guild;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildBag;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildFacilityType;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFacilityLevelUp.BseGuildFacilityLevelUp;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 公会铁匠铺的百分比加成
 * 
 * @author wangqi
 */
public class GuildFacilityLevelUp {
	
	private static final Logger logger = LoggerFactory.getLogger(GuildFacilityLevelUp.class);
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 4);
		if ( result != null ) {
			return result;
		}

		final User user = (User)parameters[0];
		final GuildFacilityType type = (GuildFacilityType)parameters[1];
		final GuildFacility facility = (GuildFacility)parameters[2];
		final Boolean isCoolDown = (Boolean)parameters[3];
		
		final Guild guild = user.getGuild();
		
		boolean success = true; 
		String message = null;
		
		if ( success ) {
			/**
			 * 检查技能子类型的等级不超过技能等级
			 */
			boolean isMemberFacility = false;
			if ( facility.getType() == GuildFacilityType.ab_agility ||
					facility.getType() == GuildFacilityType.ab_blood ||
					facility.getType() == GuildFacilityType.ab_attack ||
					facility.getType() == GuildFacilityType.ab_defend ||
					facility.getType() == GuildFacilityType.ab_lucky ||
					facility.getType() == GuildFacilityType.ab_pray ||
					facility.getType() == GuildFacilityType.ab_treasure ) {
				isMemberFacility = true;
				GuildFacility ability = guild.getFacility(GuildFacilityType.ability);
				if ( facility.getLevel()+1 > ability.getLevel() ) {
					success = false;
					message = Text.text("guild.levelup.guild.ability.higher"); 
				}
			}
			
			if ( success ) {
				if ( isMemberFacility ) {
					/**
					 * 升级个人技能
					 */
					int[] abilityCredits = GameDataManager.getInstance().
							getGameDataAsIntArray(GameDataKey.GUILD_ABILITY_CREDIT);
					int level = facility.getLevel();
					success = false;
					message = null;
					if ( level < abilityCredits.length ) {
						//level == 1 表示从1级升到2级
						final int credit = abilityCredits[level];
						String prompt = Text.text("guild.levelup.ab.prompt", facility.getType().getName(), credit);
						ConfirmManager.getInstance().sendConfirmMessage(user, prompt, 
								"guild.facility.levelup", new ConfirmManager.ConfirmCallback(){

							public void callback(User user, int selected) {
								if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
									doAbillityLevelUp(user, guild, facility, credit);
								} else {
									sendResponse(user, false, Text.text("guild.levelup.guild.cancel"), 
											facility.getType(), facility.getLevel(), 0, 0);
								}
							}
							
						});
					}
				} else {
					/**
					 * 升级公会技能
					 */
					int[][] facilityLevelUpWealths = GameDataManager.getInstance().
							getGameDataAsIntArrayArray(GameDataKey.GUILD_LEVEL_WEALTH);
					int level = facility.getLevel();
					int[] facilityLevelUpWealth = facilityLevelUpWealths[facility.getType().index()];
					success = false;
					message = null;
					if ( isCoolDown.booleanValue() ) {
						long lastEndMillis = guild.getUpgradeEndTime();
						long currentMillis = System.currentTimeMillis();
						int diff = (int)((lastEndMillis-currentMillis)/1000);
						if ( diff > 0 ) {
							final int yuanbao = calculateCoolDownYuanbao(diff);
							String prompt = Text.text("guild.levelup.guild.prompt.cool", yuanbao);
							ConfirmManager.getInstance().sendConfirmMessage(user, prompt, 
									"guild.facility.levelup", new ConfirmManager.ConfirmCallback(){

								public void callback(User user, int selected) {
									if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
										//doLevelUp(user, guild, facility, wealth, yuanbao);
										doCoolDown(user, guild, facility, yuanbao);
									} else {
										sendResponse(user, false, Text.text("guild.levelup.guild.cancel"), 
												facility.getType(), facility.getLevel(), 0, 0);
									}
								}
								
							});
						}
					} else {
						if ( level < facilityLevelUpWealth.length ) {
							//level == 1 表示从1级升到2级
							final int wealth = facilityLevelUpWealth[level];
							String prompt = Text.text("guild.levelup.guild.prompt", facility.getType().getName(), wealth);
							ConfirmManager.getInstance().sendConfirmMessage(user, prompt, 
									"guild.facility.levelup", new ConfirmManager.ConfirmCallback(){

								public void callback(User user, int selected) {
									if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
										doLevelUp(user, guild, facility, wealth, 0);
									} else {
										sendResponse(user, false, Text.text("guild.levelup.guild.cancel"), 
												facility.getType(), facility.getLevel(), 0, 0);
									}
								}
								
							});
						} else {
							success = false;
							message = Text.text("guild.levelup.guild.max");
							sendResponse(user, success, message, facility.getType(), level, 0, 0);
						}
					}
				}
			} else {
				sendResponse(user, success, message, type, facility.getLevel(), 0, 0);
			}
		} else {
			sendResponse(user, success, message, type, facility.getLevel(), 0, 0);
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	/**
	 * Make the guild level up to next leve.
	 * @param user
	 * @param guild
	 * @param facility
	 */
	public static void doAbillityLevelUp(User user, Guild guild, GuildFacility facility, int credit) {
		boolean success = false;
		String message = null;
		int coolDownSecond = 0;
		int yuanbao = 0;
		boolean useAbilityFresh = false;
		/**
		 * 检查个人贡献度
		 */
		final GuildMember member = user.getGuildMember(); 
		if ( credit > 0 ) { 
			if ( member.getCredit() >= credit ) {
				success = true;
			} else {
				success = false;
				message = Text.text("guild.levelup.ab.nocredit");
			}
		} else {
			success = true;
		}

		if ( success ) {
			success = false;
			int currentLevel = facility.getLevel()+1;
			if ( facility.getType() == GuildFacilityType.ab_agility ) {
				/**
				 * 升级敏捷
				 */
				success = true;
				useAbilityFresh = true;
				if (currentLevel>=4) {
					facility.setEnabled(true);
				}
			} else if ( facility.getType() == GuildFacilityType.ab_attack ) {
				/**
				 * 升级攻击
				 */
				success = true;
				useAbilityFresh = true;
				if (currentLevel>=3) {
					facility.setEnabled(true);
				}
			} else if ( facility.getType() == GuildFacilityType.ab_blood ) {
				/**
				 * 升级血量
				 */
				success = true;
				useAbilityFresh = true;
				if (currentLevel>=5) {
					facility.setEnabled(true);
				}
			} else if ( facility.getType() == GuildFacilityType.ab_defend ) {
				/**
				 * 升级防御
				 */
				success = true;
				useAbilityFresh = true;
			} else if ( facility.getType() == GuildFacilityType.ab_lucky ) {
				/**
				 * 升级幸运
				 */
				success = true;
				useAbilityFresh = true;
				if (currentLevel>=2) {
					facility.setEnabled(true);
				}
			} else if ( facility.getType() == GuildFacilityType.ab_pray ) {
				/**
				 * 升级祈福
				 */
				success = true;
				if (currentLevel>=5) {
					facility.setEnabled(true);
				}
			} else if ( facility.getType() == GuildFacilityType.ab_treasure ) {
				/**
				 * 升级寻宝
				 */
				success = true;
				if (currentLevel>=5) {
					facility.setEnabled(true);
				}
			}
			if ( success ) {
				facility.setLevel(currentLevel);
				
				/**
				 * 扣除个人贡献
				 */
				member.setCredit(member.getCredit()-credit);
				member.addFacility(facility);
				GuildManager.getInstance().saveGuildMember(member);
				/**
				 * 同步用户的属性
				 */
				ScriptManager.getInstance().runScript(ScriptHook.GUILD_USER_ABILITY, new Object[]{user});
				if ( useAbilityFresh ) {
					GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo(false));
				}
				StatClient.getIntance().sendDataToStatServer(
					user, StatAction.GuildAbiLevelUp, new Object[]{user.getGuildId(), facility.getType(), facility.getLevel(), success});
			}
		}
		sendResponse(user, success, message, facility.getType(), facility.getLevel(), coolDownSecond, yuanbao);
	}

	/**
	 * Make the guild level up to next leve.
	 * @param user
	 * @param guild
	 * @param facility
	 */
	public static void doLevelUp(User user, Guild guild, GuildFacility facility, int wealth, int coolDownYuanbao) {
		boolean success = true;
		String message = null;
		int coolDownSecond = 0;
		int yuanbao = 0;
		
		/**
		 * 公会设施的等级不能超过公会等级
		 */
		if ( facility == null ) {
			logger.warn("Failed to find the facility for user {} of type {}", user.getRoleName(), facility.getType());
		}
		if ( facility.getType() != GuildFacilityType.guild ) {
			if ( facility.getLevel() + 1 > guild.getLevel() ) {
				success = false;
				message = Text.text("guild.levelup.guild.higher", guild.getLevel());
			}
		}

		if ( success ) {
			if ( coolDownYuanbao > 0 ) {
				if ( user.canPayYuanbao(coolDownYuanbao) ) {
					success = true;
				} else {
					success = false;
					message = Text.text("guild.levelup.guild.noyuanbao");
				}
			} else {
				success = true;
			}
		}
		/**
		 * 检查公会的财富值
		 */
		if ( success ) {
			success = false;
			if ( guild.getWealth() >= wealth ) {
				success = true;
			} else {
				success = false;
				message = Text.text("guild.levelup.guild.nowealth");
			}
		}
		/**
		 * 公会设施的等级不能超过公会等级
		 */
		if ( facility.getType() != GuildFacilityType.guild ) {
			if ( facility.getLevel() + 1 > guild.getLevel() ) {
				success = false;
				message = Text.text("guild.levelup.guild.higher", guild.getLevel());
			}
		}
		if ( success ) {
			success = false;
			int currentLevel = facility.getLevel()+1;
			if ( facility.getType() == GuildFacilityType.guild ) {
				/**
				 * 升级主公会
				 */
				int[] maxCounts = GameDataManager.getInstance().getGameDataAsIntArray(
						GameDataKey.GUILD_LEVEL_MAXCOUNT);
				//Overwrite the currentLevel if the two value are not matched.
				currentLevel = guild.getLevel()+1;
				int maxCount = maxCounts[currentLevel-1];
				guild.setMaxCount(maxCount);
				guild.setLevel(currentLevel);

				success = true;
			} else if ( facility.getType() == GuildFacilityType.shop ) {
				/**
				 * 升级商城
				 */
				success = true;
			} else if ( facility.getType() == GuildFacilityType.craft ) {
				/**
				 * 升级铁匠铺
				 */
				success = true;
			} else if ( facility.getType() == GuildFacilityType.storage ) {
				/*
				 * 升级仓库
				 */
				int[] bagSize = GameDataManager.getInstance().getGameDataAsIntArray(
						GameDataKey.GUILD_STORAGE_SIZE);
				int maxCount = bagSize[currentLevel-1];
				GuildBag guildBag = GuildManager.getInstance().queryGuildBag(guild.get_id());
				GuildManager.getInstance().updateGuildBagMaxCount(user, guildBag, maxCount);
				success = true;
			} else if ( facility.getType() == GuildFacilityType.ability ) {
				/**
				 * 升级技能
				 */
				success = true;
			}
			if ( success ) {
				facility.setLevel(currentLevel);
				coolDownSecond = GameDataManager.getInstance().getGameDataAsIntArray(
						GameDataKey.GUILD_FACILITY_COOLDOWN)[currentLevel-1];
				yuanbao = calculateCoolDownYuanbao(coolDownSecond);
				long currentMillis = System.currentTimeMillis();
				facility.setUpgradeBeginTime(currentMillis);
				facility.setUpgradeEndTime(currentMillis+coolDownSecond*1000);
				guild.setUpgradeBeginTime(currentMillis);
				guild.setUpgradeEndTime(facility.getUpgradeEndTime());
				
				/**
				 * 扣除公会财富
				 */
				guild.setWealth(guild.getWealth()-wealth);
				user.setGuild(guild);
				GuildManager.getInstance().saveGuild(guild);
			}
			
			StatClient.getIntance().sendDataToStatServer(
					user, StatAction.GuildFacLevelUp, new Object[]{user.getGuildId(), facility.getType(), facility.getLevel(), success});
		}
		sendResponse(user, success, message, facility.getType(), facility.getLevel(), coolDownSecond, yuanbao);
	}
	
	private static void doCoolDown(User user, Guild guild, GuildFacility facility, int coolDownYuanbao) {
		boolean success = false;
		String message = null;

		if ( coolDownYuanbao > 0 ) {
			if ( user.canPayYuanbao(coolDownYuanbao) ) {
				success = true;
			} else {
				success = false;
				message = Text.text("guild.levelup.guild.noyuanbao");
			}
		} else {
			success = true;
		}
		if ( success ) {
			long currentMillis = System.currentTimeMillis();
			facility.setUpgradeBeginTime(currentMillis);
			facility.setUpgradeEndTime(0);
			guild.setUpgradeBeginTime(currentMillis);
			guild.setUpgradeEndTime(0);
			user.setGuild(guild);
			GuildManager.getInstance().saveGuild(guild);
			sendResponse(user, success, message, facility.getType(), facility.getLevel(), 0, 0);
			
			/**
			 * 扣除立即冷却元宝
			 */
			if ( coolDownYuanbao > 0 ) {
				success = ShopManager.getInstance().payForSomething(
						user, MoneyType.YUANBAO, coolDownYuanbao, 1, null, true);
			}
			StatClient.getIntance().sendDataToStatServer(
				user, StatAction.ConsumeGuildCD, new Object[]{facility.getType(), facility.getLevel(), coolDownYuanbao, success});
		} else {
			int coolDownSecond = (int)((guild.getUpgradeEndTime()-guild.getUpgradeBeginTime())/1000);
			sendResponse(user, success, message, facility.getType(), facility.getLevel(), coolDownSecond, coolDownYuanbao);
		}
	}

	/**
	 * @param coolDownSecond
	 * @return
	 */
	private static int calculateCoolDownYuanbao(int coolDownSecond) {
		int yuanbao;
		double yuanbaoRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GUILD_FACILITY_COOLDOWN_YUANBAO, 0);
		yuanbao = (int)Math.round(yuanbaoRatio*coolDownSecond);
		return yuanbao;
	}
	
	/**
	 * Send the response back to client.
	 * 
	 * @param success
	 * @param message
	 * @param level
	 * @param cooldown
	 * @param yuanbao
	 */
	public static void sendResponse(User user, boolean success, String message, GuildFacilityType type, 
			int level, int cooldown, int yuanbao) {
		BseGuildFacilityLevelUp.Builder builder = BseGuildFacilityLevelUp.newBuilder();
		builder.setType(type.id());
		if ( success ) {
			builder.setStatus(0);
			builder.setMessage("guild.levelup.guild.success");
			builder.setLevel(level);
			builder.setCooldown(cooldown);
			builder.setYuanbao(yuanbao);
			
			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
			
			//通知公会所有在线玩家刷新公会数据
			GuildManager.getInstance().syncGuildWithOnlineUsers(user.getGuild());
		} else {
			builder.setStatus(1);
			if ( message == null ) {
				message = Text.text("guild.levelup.guild.failure");
			}
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
	}
}
