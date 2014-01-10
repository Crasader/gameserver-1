package com.xinqihd.sns.gameserver.cron;

import java.util.Calendar;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.boss.BossType;
import com.xinqihd.sns.gameserver.boss.BossWinType;
import com.xinqihd.sns.gameserver.boss.condition.GoldenCondition;
import com.xinqihd.sns.gameserver.boss.condition.LevelCondition;
import com.xinqihd.sns.gameserver.config.MapPojo.Point;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceBulletin.BceBulletin;
import com.xinqihd.sns.gameserver.proto.XinqiBceReloadConfig.BceReloadConfig;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class BossUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(BossUtil.class);
	
	/**
	 * @param id The unique id for the given boss in database 
	 * @param bossId The same boss will have same bossId
	 * @param name
	 * @param title
	 * @param desc
	 * @param target
	 * @param bossType
	 * @param bossWinType
	 * @param mapId
	 * @param blood
	 * @param level
	 * @param width
	 * @param height
	 * @param hurtRadius
	 * @param suitPropId
	 * @param minUserLevel
	 * @param maxUserLevel
	 * @param requiredGolden
	 * @param startCal
	 * @param endCal
	 * @param gifts
	 * @param limit
	 * @param increasePerHour
	 * @param winProgress
	 */
	public static final void addNewBoss(String id, String bossId, String name, String title, String desc,
			String target, BossType bossType, BossWinType bossWinType, String mapId,
			int blood, int level, int width, int height, int hurtRadius,
			String suitPropId, int minUserLevel, int maxUserLevel,
			int requiredGolden, Calendar startCal, Calendar endCal, Reward[] gifts,
			int limit, int increasePerHour, int winProgress, int totalProgress, String weaponId, 
			ScriptHook roleAttack, ScriptHook roleDead, int totalRound) {
		addNewBoss(id, bossId, name, title, desc, 
				target, bossType, bossWinType, mapId, blood, level, width, 
				height, hurtRadius, suitPropId, minUserLevel, maxUserLevel, 
				requiredGolden, startCal, endCal, gifts, limit, increasePerHour, 
				winProgress, totalProgress, weaponId, roleAttack, roleDead, 
				totalRound, null, null, null);
	}
	
	
	public static final void addNewBoss(String id, String bossId, String name, String title, String desc,
			String target, BossType bossType, BossWinType bossWinType, String mapId,
			int blood, int level, int width, int height, int hurtRadius,
			String suitPropId, int minUserLevel, int maxUserLevel,
			int requiredGolden, Calendar startCal, Calendar endCal, Reward[] gifts,
			int limit, int increasePerHour, int winProgress, int totalProgress, String weaponId, 
			ScriptHook roleAttack, ScriptHook roleDead, int totalRound, ScriptHook createScript, 
			ScriptHook createUserScript, ScriptHook battleRewardScript) {
		BossPojo boss = new BossPojo();
		boss.setId(id);
		boss.setBossId(bossId);
		boss.setName(name);
		boss.setTitle(title);
		boss.setDesc(desc);
		boss.setTarget(target);
		boss.setBossType(bossType);
		boss.setBossWinType(bossWinType);
		boss.setMapId(mapId);
		/**
		 * Boss base attributes
		 */
		//8亿
		boss.setBlood(blood);
		boss.setLevel(level);
		User user = createBossUser(level);
		boss.setAttack(user.getAttack());
		boss.setDefend(user.getDefend());
		boss.setAgility(user.getAgility());
		boss.setLucky(user.getLuck());
		boss.setThew(user.getTkew());
		boss.setWidth(width);
		boss.setHeight(height);
		boss.setHurtRadius(hurtRadius);
		if ( totalRound>0 ) {
			boss.setTotalRound(totalRound);
		}
		
		boss.setSuitPropId(suitPropId);
		boss.setWeaponPropId(weaponId);
		boss.setRoleAttackScript(roleAttack.getHook());
		boss.setRoleDeadScript(roleDead.getHook());
		if ( createScript != null ) {
			boss.setCreateBossScript(createScript.getHook());
		}
		if ( createUserScript != null ) {
			boss.setCreateUserScript(createUserScript.getHook());
		}
		if ( battleRewardScript != null ) {
			boss.setBattleRewardScript(battleRewardScript.getHook());
		}

		Point hitpoint = new Point();
		hitpoint.x = 0;
		hitpoint.y = 0;
		boss.addHitpoints(hitpoint);
		
		if ( minUserLevel >= 0 && maxUserLevel>= 0 ) {
			LevelCondition levelCondition = new LevelCondition();
			//等级达到20级
			levelCondition.setMinLevel(minUserLevel);
			levelCondition.setMaxLevel(maxUserLevel);
			boss.addRequiredConditions(levelCondition);
		}
		if ( requiredGolden >= 0 ) {
			GoldenCondition goldenCondition = new GoldenCondition();
			goldenCondition.setGolden(requiredGolden);
			boss.addRequiredConditions(goldenCondition);
		}
		if ( gifts != null ) {
			for ( Reward gift : gifts ) {
				boss.addRewards(gift);
			}
		}
		boss.setChallengeLimit(limit);
		boss.setChallengeIncreasePerHour(increasePerHour);
		
		/**
		 * Disable update the database
		 */
		BossManager.getInstance().addBoss(boss);

		BossManager manager = BossManager.getInstance();
		Boss instance = manager.createBossInstance(bossId, 
				boss, startCal, endCal);
		instance.setLimit(limit);
		instance.setIncreasePerHour(increasePerHour);
		instance.setWinProgress(winProgress);
		instance.setTotalProgress(totalProgress);
		
		BossManager.getInstance().saveBossInstance(instance);
	}
	
	public static final User createBossUser(int level) {
		User user = new User();
		user.setLevel(level);
		int strengthLevel = 5;
		WeaponColor color = WeaponColor.WHITE;
		if ( level >= 80 ) {
			strengthLevel = 12;
			color = WeaponColor.ORGANCE;
		} else if ( level >= 60 ) {
			strengthLevel = 8;
			color = WeaponColor.PINK;
		} else if ( level >= 40 ) {
			strengthLevel = 5;
			color = WeaponColor.GREEN;
		} else if ( level >= 40 ) {
			strengthLevel = 3;
			color = WeaponColor.WHITE;
		} else {
			strengthLevel = 0;
			color = WeaponColor.WHITE;
		}

		Bag bag = user.getBag();
		PropDataEquipIndex[] equips = PropDataEquipIndex.values();
		for ( int i=0; i<equips.length; i++ ) {
			EquipType type = null;
			if ( equips[i] == PropDataEquipIndex.BRACELET1 ) {
					type = EquipType.DECORATION;
			} else if ( equips[i] == PropDataEquipIndex.BRACELET2 ) {
					type = EquipType.DECORATION;
			} else if ( equips[i] == PropDataEquipIndex.BUBBLE ) {
					type = EquipType.BUBBLE;
			} else if ( equips[i] == PropDataEquipIndex.CLOTH ) {
					type = EquipType.CLOTHES;
			} else if ( equips[i] == PropDataEquipIndex.EYE ) {
					type = EquipType.EXPRESSION;
			} else if ( equips[i] == PropDataEquipIndex.FACE ) {
					type = EquipType.FACE;
			} else if ( equips[i] == PropDataEquipIndex.GLASS ) {
					type = EquipType.GLASSES;
			} else if ( equips[i] == PropDataEquipIndex.HAIR ) {
					type = EquipType.HAIR;
			} else if ( equips[i] == PropDataEquipIndex.HAT ) {
					type = EquipType.HAT;
			} else if ( equips[i] == PropDataEquipIndex.NECKLACE ) {
					type = EquipType.DECORATION;
			} else if ( equips[i] == PropDataEquipIndex.RING1 ) {
					type = EquipType.JEWELRY;
			} else if ( equips[i] == PropDataEquipIndex.RING2) {
					type = EquipType.JEWELRY;
			} else if ( equips[i] == PropDataEquipIndex.SUIT ) {
					type = EquipType.SUIT;
			} else if ( equips[i] == PropDataEquipIndex.WEAPON ) {
					type = EquipType.WEAPON;
			} else if ( equips[i] == PropDataEquipIndex.WEDRING ) {
					type = EquipType.JEWELRY;
			} else if ( equips[i] == PropDataEquipIndex.WING ) {
					type = EquipType.WING;
			}
			Collection slot = EquipManager.getInstance().getWeaponsBySlot(type);
			if ( slot != null ) {
				Object[] weaponObjs = MathUtil.randomPick(slot, 1);
				PropData propData = null;
				WeaponPojo weapon = (WeaponPojo)weaponObjs[0];
				weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weapon.getTypeName(), user.getLevel());
				while ( weapon.isUsedByBoss() ) {
					weaponObjs = MathUtil.randomPick(slot, 1);
					weapon = (WeaponPojo)weaponObjs[0];
				}
				propData = weapon.toPropData(10, color);
				EquipCalculator.weaponUpLevel(propData, strengthLevel);
				user.getBag().addOtherPropDatas(propData);
				user.getBag().wearPropData(propData.getPew(), equips[i].index());
			} else {
				logger.debug("Slot is null: {}", equips[i]);
			}
		}
		ScriptManager.getInstance().runScript(ScriptHook.USER_LEVEL_UPGRADE, 
				new Object[]{user, level});
		
		return user;
	}
	
	public static final void updateServer(String[] hostIds, String message) {
		GameClient client = null;
		//提示服务器更新副本
		BceReloadConfig.Builder reload = BceReloadConfig.newBuilder();
		reload.addConfigname("bosses");
		XinqiMessage msg = new XinqiMessage();
		msg.payload = reload.build();
		for ( String hostId : hostIds ) {
			String[] fields = StringUtil.splitMachineId(hostId);
			String host = fields[0];
			int port = StringUtil.toInt(fields[1], 3443);
			client = new GameClient(host, port);
			client.sendMessageToServer(msg);
		}

		//推送全局消息
		BceBulletin.Builder builder = BceBulletin.newBuilder();
		builder.setType(Type.CONFIRM.ordinal());
		builder.setExpire(0);
		builder.setMessage(message);
		msg = new XinqiMessage();
		msg.payload = builder.build();
		client.sendMessageToServer(msg);
	}
	
	/**
	 * 删除所有玩家已经领取的状态
	 */
	public static final void cleanRewardKey(String bossId) {
		Jedis jedis = JedisFactory.getJedisDB();
		String key = StringUtil.concat(BossManager.KEY_BOSS_USER, bossId, "*");
		Set<String> userRewardKeys = jedis.keys(key);
		String[] arrays = userRewardKeys.toArray(new String[0]);
		if ( arrays.length>0 ) {
			jedis.del(arrays);
		}
	}
}
