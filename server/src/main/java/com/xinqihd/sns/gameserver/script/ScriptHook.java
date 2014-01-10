package com.xinqihd.sns.gameserver.script;

import java.util.HashMap;

/**
 * The script hooking list. Every string in this interface may 
 * have a script object to run.
 *  
 * @author wangqi
 *
 */
public enum ScriptHook {
	
	HELLO("script.Hello"),
	TEST("script.Test"),
//	USER_LOGIN("script.UserLogin"),
	USER_CHARGE_LIST("script.UserChargeList"),
	//The script to calculate user's blood
	USER_CALCULATE_BLOOD("script.UserCalculateBlood"),
	//The script to calculate user's thew.
	USER_CALCULATE_THEW("script.UserCalculateThew"),
	//Upgrade the user's level
	USER_LEVEL_UPGRADE("script.UserLevelUpgrade"),
	//Send user gift box
	USER_LEVEL_PROCESSING("script.UserLevelProcessing"),
	//Upgrade the user's stat like win/fail/odd
	USER_UPDATE_STAT("script.UserUpdateStat"),
	//Calculate user's power.
	USER_PROP_CALCULATE("script.UserPropCalculate"),
	//Upgrade the weapon's level
	WEAPON_LEVEL_UPGRADE("script.WeaponLevelUpgrade"),
	//The weapon's depreciation in battle
	WEAPON_DEPRECIATE("script.WeaponDepreciate"),
	//Script to get the user's next level required EXP.
	NEXT_LEVEL_EXP("script.NextLevelExp"),
	VERSION_CHECK("script.VersionCheck"),
	UUID_CHECK("script.UUIDCheck"),
	BAG_CHECK("script.BagCheck"),
	CHECK_SERVER_OPTION("script.CheckServerOption"),
	CHANNEL_CHECK("script.ChannelCheck"),

	BATTLE_WIND("script.BattleWind"),
	//Caculate the current rate of experience for given user.
	//It may be used for promotion.
	BATTLE_EXP_RATE("script.BattleExpRate"),
	BATTLE_TIMEOUT_WINNER("script.BattleTimeoutWinner"),
	BATTLE_ROUND_OVER("script.BattleRoundOver"),
	BATTLE_BULLET_COUNT("script.BattleBulletCount"),
	//The battle is over.
	BATTLE_OVER("script.BattleOver"),
	BATTLE_REMARK("script.BattleRemark"),
	PICK_ROUND_USER("script.PickRoundUser"),
	//Pick a random map for battle
	PICK_BATTLE_MAP("script.PickBattleMap"),
	//Check if two rooms are matched.
	PICK_BATTLE_MATCH_ROOM("script.BattleMatchRoom"),
//	BATTLE_ROLE_ATTACK("script.BattleRoleAttack"),
	BATTLE_BITMAP_ROLE_ATTACK("script.BattleBitmapRoleAttack"),
	BATTLE_ROLE_MOVE("script.BattleRoleMove"),
	BATTLE_ROLE_POWER("script.BattleRolePower"),
	BATTLE_ROLE_USETOOL("script.BattleRoleUseTool"),
	BATTLE_ROUND_START_CHECK_ROLE_STATUS("script.BattleRoundStartCheckRoleStatus"),
	
	//Reward
	BATTLE_REWARD("script.reward.BattleReward"),
  //Generate random treasure boxes in a battle.
	BATTLE_BOX_REWARD("script.reward.BattleBoxReward"),
	//Reward
	PVE_BATTLE_REWARD("script.reward.PVEBattleReward"),
  //Generate random treasure boxes in a battle.
	PVE_BATTLE_BOX_REWARD("script.reward.PVEBattleBoxReward"),
	USER_LOGIN_REWARD("script.reward.UserLoginReward"),
	USER_ONLINE_REWARD("script.reward.UserOnlineReward"),
	USER_DAILY_MARK_REWARD("script.reward.UserDailyMarkReward"),
	DIAMOND_REWARD("script.reward.DiamondReward"),
	BIBLIO_REWARD("script.reward.BiblioReward"),
	MAKE_WEAPON("script.reward.MakeWeaponPojo"),
	BOSS_ITEM_REWARD("script.reward.BossItemReward"),
	BOSS_WEAPON_REWARD("script.reward.BossWeaponReward"),
	
	//Craft
	CRAFT_COMPOSE_ITEM("script.CraftComposeItem"),
	CRAFT_FORGE_EQUIP("script.CraftForgeEquip"),
	CRAFT_TRANSFER_EQUIP("script.CraftTransferEquip"),
	CRAFT_COMPOSE_PRICE("script.CraftComposePrice"),
	CRAFT_FORGE_PRICE("script.CraftForgePrice"),
	CRAFT_TRANSFER_PRICE("script.CraftTransferPrice"),
	CRAFT_CAL_DIAMOND("script.CraftCalDiamond"),

	//Merge server
	MERGE_SERVER("script.MergeServer"),
	
//	CRAFT_TRANSFER_EQUIP("script.CraftTransferEquip"),
	SHOP_SELL_GOOD("script.ShopSellGood"),
	//Task related scripts
	TASK_ANY_COMBAT("script.task.AnyCombat"),
	TASK_ANY_COMBAT_WIN("script.task.AnyCombatWin"),
	TASK_BEAT_USERS("script.task.BeatUsers"),
	TASK_BUFF_TOOL_ATTACKADD10("script.task.BuffToolAttackAdd10"),
	TASK_BUFF_TOOL_ATTACKADD50("script.task.BuffToolAttackAdd50"),
	TASK_BUFF_TOOL_ATTACKONE("script.task.BuffToolAttackOne"),
	TASK_BUFF_TOOL_ATTACKTWO("script.task.BuffToolAttackTwo"),
	TASK_BUFF_TOOL_BLOOD("script.task.BuffToolBlood"),
	TASK_BUFF_TOOL_BRANCHTREE("script.task.BuffToolBranchTree"),
	TASK_BUFF_TOOL_CHANGEWIND("script.task.BuffToolChangeWind"),
	TASK_BUFF_TOOL_FROZEN("script.task.BuffToolFrozen"),
	TASK_BUFF_TOOL_GUIDE("script.task.BuffToolGuide"),
	TASK_BUFF_TOOL_HIDDEN("script.task.BuffToolHidden"),
	TASK_BUFF_TOOL_POW("script.task.BuffToolPow"),
	TASK_BUFF_TOOL_FLY("script.task.BuffToolFly"),
	TASK_BUFF_TOOL_TEAMHIDE("script.task.BuffToolTeamHide"),
	TASK_BUY_ITEM_BY_GOLDEN("script.task.BuyItemByGolden"),
	TASK_BUY_ITEM_BY_MEDAL("script.task.BuyItemByMedal"),
	TASK_BUY_ITEM_BY_VOUCHER("script.task.BuyItemByVoucher"),
	TASK_BUY_ITEM_BY_YUANBAO("script.task.BuyItemByYuanbao"),
	TASK_CRAFT_COMPOSE_FIRE("script.task.CraftComposeFire"),
	TASK_CRAFT_COMPOSE_WATER("script.task.CraftComposeWater"),
	TASK_CRAFT_COMPOSE_COLOR("script.task.CraftComposeColor"),
	TASK_CRAFT_COMPOSE_EQUIP("script.task.CraftComposeEquip"),
	TASK_CRAFT_COMPOSE_WEAPON("script.task.CraftComposeWeapon"),
	TASK_CRAFT_FORGE_FIRE("script.task.CraftForgeFire"),
	TASK_CRAFT_FORGE_WATER("script.task.CraftForgeWater"),
	TASK_LOGIN("script.task.Login"),
	TASK_LOGIN_DATE("script.task.LoginDate"),
	TASK_SINGLE_COMBAT("script.task.SingleCombat"),
	TASK_SINGLE_COMBAT_WIN("script.task.SingleCombatWin"),
	TASK_GUILD_COMBAT("script.task.GuildCombat"),
	TASK_GUILD_COMBAT_WIN("script.task.GuildCombatWin"),
	TASK_STRENGTH_CLOTHES("script.task.StrengthClothes"),
	TASK_STRENGTH_HAT("script.task.StrengthHat"),
	TASK_STRENGTH_WEAPON("script.task.StrengthWeapon"),
	TASK_TEAM_COMBAT("script.task.TeamCombat"),
	TASK_TEAM_COMBAT_WIN("script.task.TeamCombatWin"),
	TASK_PVE_COMBAT_WIN("script.task.PVECombatWin"),
	TASK_TRAINING("script.task.Training"),
	TASK_USER_LEVELUP("script.task.UserLevelUp"),
	TASK_WEAR_CLOTHES("script.task.WearClothes"),
	TASK_USER_GOLDEN("script.task.UserGolden"),
	TASK_USER_YUANBAO("script.task.UserYuanbao"),
	TASK_USER_POWER("script.task.UserPower"),
	TASK_USER_RANK_POWER("script.task.UserRankPower"),
	TASK_USER_RANK_WEALTH("script.task.UserRankWealth"),
	TASK_ADD_ITEM("script.task.AddItemToBag"),
	TASK_USER_BAG_COUNT("script.task.UserBagCount"),
	TASK_SELL_GOOD("script.task.SellGood"),
	TASK_WEIBO_BOUND("script.task.WeiboBound"),
	TASK_WEIBO_ANYTYPE("script.task.WeiboAnyType"),
	TASK_WEIBO_ACHIEVEMENT("script.task.WeiboAchievement"),
	TASK_WEIBO_COMBAT("script.task.WeiboCombat"),
	TASK_WEIBO_FORGE("script.task.WeiboForge"),
	TASK_WEIBO_LEVELUP("script.task.WeiboLevelup"),
	TASK_WEIBO_RANKING("script.task.WeiboRanking"),
	//New task 2012-09-28
	TASK_FRIEND_COMBAT("script.task.FriendCombat"),
	TASK_OFFLINE_COMBAT("script.task.OfflineCombat"),
	TASK_CAISHEN_PRAY("script.task.CaishenPray"),
	TASK_TREASURE_HUNT("script.task.TreasureHunt"),
	TASK_CHECK_RANKING("script.task.CheckRanking"),
	TASK_CHAT_WORLD("script.task.ChatWorld"),
	TASK_ADD_FRIEND("script.task.AddFriend"),
	//充值类型的活动任务
	TASK_CHARGE("script.task.Charge"),
	//收集类型的活动任务
	TASK_COLLECT("script.task.Collect"),
	TASK_COLLECT_POST("script.task.CollectPost"),
	//恢复体力值的任务
	TASK_ROLEACTION_POST("script.task.RoleActionPost"),
	//首次充值返元宝
	TASK_CHARGEFIRST("script.task.ChargeFirst"),
	//经验加成
	TASK_EXPGAIN("script.task.ExpGain"),
	//无特别经验
	TASK_NOSCRIPT("script.task.NoOp"),
	TASK_VIP_LEVELUP("script.task.VipLevelUp"),
	//限量发放类型任务的POST钩子
	TASK_STRUGGLE_POST("script.task.StrugglePost"),
	//玩家加入了一个公会
	TASK_JOIN_GUILD("script.task.JoinGuild"),
	
	//Item box
	ITEM_BOX_RANDOM_BOX("script.box.RandomBox"),
	ITEM_BOX_EQUIP_BOX("script.box.EquipBox"),
	ITEM_BOX_RANDOM_BOX_VIP("script.box.RandomBoxForVIP"),
	ITEM_BOX_PACKAGE_BOX("script.box.PackageBox"),
	ITEM_BOX_LEVELUP_BOX("script.box.LevelUpBox"),
	ITEM_BOX_EXP_BOX("script.box.ExpBox"),
	ITEM_BOX_DOUBLE_EXP_BOX("script.box.DoubleExpBox"),
	ITEM_BOX_BIGBAG_BOX("script.box.BigBagBox"),
	ITEM_BOX_LITTLEBAG_BOX("script.box.LittleBagBox"),
	ITEM_BOX_TREASURECARD_BOX("script.box.TreasureCard"),
	ITEM_BOX_CHANGE_NAME("script.box.ChangeNameCard"),
	
	//AI related
	AI_USER_CREATE("script.ai.UserCreate"),
  AI_USER_CHAT("script.ai.UserChat"),
  AI_BATTLE_INIT("script.ai.BattleInit"),
	AI_BATTLE_ROLE_ATTACK("script.ai.BattleRoleAttack"),
	AI_BATTLE_ROLE_MOVE("script.ai.BattleRoleMove"),
	AI_BATTLE_ROLE_DEAD("script.ai.BattleRoleDead"),
	//PVE related
	CREATE_BOSS_LIMIT_DAILY("script.boss.BossLimitDaily"),
	CREATE_BOSS_USER("script.boss.CreateBossUser"),
	BOSS_ICE_RABBIT_ROLEATTACK("script.boss.IceRabbitRoleAttack"),
	BOSS_ICE_RABBIT_ROLEDEAD("script.boss.IceRabbitRoleDead"),
	BOSS_ICE_RABBIT_ROLECREATE("script.boss.IceRabbitRoleCreate"),
	BOSS_ICE_RABBIT_USERCREATE("script.boss.IceRabbitUserCreate"),
	BOSS_ICE_RABBIT_BATTLEREWARD("script.boss.IceRabbitBattleReward"),
	BOSS_KILLMANY_ROLEATTACK("script.boss.KillManyRoleAttack"),
	BOSS_KILLMANY_ROLEDEAD("script.boss.KillManyRoleDead"),
	BOSS_DIAMOND_COLLECT_ROLEATTACK("script.boss.DiamondCollectRoleAttack"),
	BOSS_DIAMOND_COLLECT_ROLEDEAD("script.boss.DiamondCollectRoleDead"),
	BOSS_BATTLE_SYNC("script.boss.BossBattleSync"),
	BOSS_SEND_RANKING_REWARD("script.boss.BossSendRankingReward"),
	BOSS_FIND_BOSS_ABILITY("script.boss.FindBossAbility"),
	
	//ActionLimit
	ROOM_CHECK_LOCK("script.RoomCheckLock"),
	ROLE_ACTION_LIMIT_DAILY("script.ActionLimitDaily"),
	ROLE_ACTION_CARD("script.box.RoleActionCard"),
	ROLE_ACTION_CONSUME("script.RoleActionConsume"),
	//Caishen
	CAISHEN_PRAY_LIMIT_DAILY("script.CaishenPrayLimitDaily"),
	//TreasureHunt
	TREASURE_HUNT_GEN("script.TreasureHuntGen"),
	TREASURE_HUNT_PICK("script.TreasureHuntPick"),
	
	CHARGE_DISCOUNT("script.ChargeDiscount"),
	
	//Channel gift
	GIFT_FOR_CHANNEL("script.channel.GiftForChannel"),
	
	//Charge
	CHARGE_APPLE("script.charge.Apple"),
	CHARGE_CHANGYOU("script.charge.Changyou"),
	CHARGE_LEGEND("script.charge.Legend"),
	CHARGE_XINQIHD("script.charge.Xinqihd"),
	CHARGE_BAORUAN("script.charge.Baoruan"),
	CHARGE_UC("script.charge.UC"),
	CHARGE_DANGLE("script.charge.Dangle"),
	CHARGE_HUAWEI("script.charge.Huawei"),
	CHARGE_KUPAI("script.charge.Kupai"),
	CHARGE_91("script.charge.Wanglong91"),
	CHARGE_OPPO("script.charge.Oppo"),
	CHARGE_YEEPAY("script.charge.Yeepay"),
	CHARGE_SHENZHOUFU("script.charge.ShenZhouFu"),
	CHARGE_ANZHI("script.charge.Anzhi"),
	CHARGE_MOBAGE("script.charge.Mobage"),
	CHARGE_CMCC("script.charge.CMCC"),

	//The VIP level yuanbao check
	VIP_LEVEL_QUERY("script.query.VipLevelQuery"),

	//VIP Charge
	VIP_CHARGE_LEVEL("script.VipChargeLevel"),

	//Guild
	GUILD_CREATE_CHECK("script.guild.CreateCheck"),
	GUILD_CHECK_PRVIILEGE("script.guild.CheckPrivilege"),
	GUILD_CALCULATE_CD_YUANBAO("script.guild.CalculateCDYuanbao"),
	GUILD_OPFEE_CHECK("script.guild.OperationFeeCheck"),
	GUILD_OPFEE("script.guild.GuildOpFee"),
	GUILD_INIT_FACILITY("script.guild.InitialGuildFacility"),
	GUILD_INIT_USER("script.guild.InitialUserFacility"),
	GUILD_USER_CREDIT_CHECK("script.guild.UserCreditCheck"),
	GUILD_CRAFT_ADDRATIO("script.guild.CraftRatio"),
	GUILD_LIST_PRIVILEGE("script.guild.GuildListPrivilege"),
	GUILD_CONTRIBUTE_QUERY("script.guild.GuildContributeQuery"),
	GUILD_FACILITY_LEVELUP("script.guild.GuildFacilityLevelUp"),
	GUILD_CONFIRM_INVITE("script.guild.GuildConfirmInvite"),
	GUILD_USER_ABILITY("script.guild.GuildUserAbility"),

	//Promotion
	PROMOTION_CHARGE("script.promotion.ChargePromotion"),
	PROMOTION_ONLINE("script.promotion.OnlinePromotion"),
	PROMOTION_PUZZLE("script.promotion.PuzzlePromotion"),
	PROMOTION_CHAT("script.promotion.ChatPromotion");
	
	
	private String hook = null;
	private static HashMap<String, ScriptHook> hookMap = 
			new HashMap<String, ScriptHook>();
	static {
		for ( ScriptHook hook : ScriptHook.values() ) {
			hookMap.put(hook.hook, hook);
		}
	}
	
	ScriptHook(String hook) {
		this.hook = hook;
	}
	
	/**
	 * Get the script hook value.
	 * @return
	 */
	public String getHook() {
		return hook;
	}

	/**
	 * Get the ScriptHook object by its hook name.
	 * @param hook
	 * @return
	 */
	public static ScriptHook getScriptHook(String hook) {
		return hookMap.get(hook);
	}
}
