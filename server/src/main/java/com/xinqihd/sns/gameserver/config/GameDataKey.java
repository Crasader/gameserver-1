package com.xinqihd.sns.gameserver.config;

import java.util.HashMap;

import com.xinqihd.sns.gameserver.battle.BuffToolType;

/**
 * The game needs a lot of configurable data. This enum type 
 * will list all the data keys that can be easily adjusted from
 * outside by editors. 
 * 
 * @author wangqi
 *
 */
public enum GameDataKey {

	STANDARD_USER_EXP("standrad_user_exp", "系统标准经验值"),
	
	SESSION_TIMEOUT("session.timeout",     "玩家断线后的会话保留时间(秒)"),
	
	//The database config data version
	CONFIG_VERSION("gamecontext_version", "10000"),
	//Battle
	BATTLE_ATTACK_K("battle_attack_k", "力度微调系数"),
	BATTLE_ATTACK_F("battle_attack_F", "风力微调系数"),
	BATTLE_ATTACK_G("battle_attack_G", "重力微调系数"),
	BATTLE_USER_MAX_IDLE("battle_user_max_idle_seconds", "战斗中当前回合玩家最大空闲时间,超时后回合自动切换(秒)"),
	BATTLE_MAX_SECONDS("battle_max_seconds", "战斗最长持续的时间,超时后战斗自动结束(秒)"),
	BATTLE_CRITICAL_MAX("battle_critical_max", "计算暴击的上限值"),
	BATTLE_GUIDE_RANGE("battle_guide_range", "使用自动引导时,如果炮弹落点距离玩家在该范围内,则命中"),
	BATTLE_GUIDE_HURT_RATIO("battle_guide_hurt_ratio", "使用自动引导命中的伤害是正常伤害的百分比"),
	BATTLE_TREE_BRANCH_HURT_RATIO("battle_three_branch_hurt_ratio", "使用三叉戟命中的伤害是正常伤害的百分比"),
	BATTLE_TWO_CONTINUE_HURT_RATIO("battle_two_continue_hurt_ratio", "使用连续两次攻击命中的伤害是正常伤害的百分比"),
	BATTLE_ONE_CONTINUE_HURT_RATIO("battle_one_continue_hurt_ratio", "使用连续一次攻击命中的伤害是正常伤害的百分比"),
	BATTLE_CHECK_IDLE_MILLIS("battle_check_idle_millis", "战斗检查的时间间隔(毫秒)"),
	BATTLE_MAX_LIVE_MILLIS("battle_check_idle_millis", "战斗最长的时间限制(毫秒)"),
	BATTLE_ROOM_MATCH_TIMEOUT("battle_room_match_timeout", "两个房间匹配后的超时秒数，超过这个时间可以再次匹配"),
	
	GAME_ATTACK_INDEX("game_attack_index", "攻击力转换为伤害的比率"),
	GAME_SKIN_INDEX("game_skin_index",     "护甲值转换为伤害的比率"),
	GAME_DEFEND_INDEX("game_defend_index", "防御力转换为伤害的比率"),
	GAME_AGILITY_UNIT("game_agility_index","敏捷转换为体力的比率"),
	GAME_AGILITY_MAX("game_agility_max",   "敏捷通常的最大值"),
	
	USER_DEFAULT_GOLDEN("user_default_golden", "玩家初始的金币数"),
	USER_DEFAULT_YUANBAO("user_default_yuanbao", "玩家初始的元宝数"),
	USER_DEFAULT_VOUCHER("user_default_voucher", "玩家初始的礼券数"),
	USER_DEFAULT_MEDAL("user_default_medal", "玩家初始的勋章数"),
	/*
	USER_DEFAULT_EXP("user_default_exp", "玩家初始经验值"),
	USER_DEFAULT_POWER("user_default_power", "玩家初始战斗力值"),
	USER_DEFAULT_ATTACK("user_default_attack", "玩家初始攻击力值"),
	USER_DEFAULT_DEFEND("user_default_defend", "玩家初始防御力值"),
	USER_DEFAULT_AGILITY("user_default_agility", "玩家初始敏捷值"),
	USER_DEFAULT_LUCK("user_default_luck", "玩家初始幸运值"),
	USER_DEFAULT_DAMAGE("user_default_damage", "玩家初始伤害值"),
	USER_DEFAULT_SKIN("user_default_skin", "玩家初始护甲值"),
	USER_DEFAULT_THEW("user_default_thew", "玩家初始体力值"),
	USER_DEFAULT_BLOOD("user_default_blood", "玩家初始体力值"),
	*/
	USER_DEFAULT_ENERGY("user_default_energy", "玩家发起大招的能量上限"),
//	USER_EXP_INDEX_A("user_exp_index_a", "计算玩家经验值的二项式A参数(A*x*x+B*x+C)"),
//	USER_EXP_INDEX_B("user_exp_index_b", "计算玩家经验值的二项式B参数(A*x*x+B*x+C)"),
//	USER_EXP_INDEX_C("user_exp_index_c", "计算玩家经验值的二项式C参数(A*x*x+B*x+C)"),
	
	USER_THEW_BASE("user_thew_base", "玩家基础体力值"),
	
	USER_BAG_MAX("user_bag_max", 	 "玩家背包的最大容量"),
	USER_TOOL_MAX("user_tool_max", "玩家便携道具的最大数量"),
	USER_TASK_NUMBER("user_task_number", "玩家任务列表中新任务的最大数量"),
	
	USER_ONLINE_REWARD_MAX("user_online_reward_max", "玩家每日登陆奖励的最大次数(3次)"),
	
	USER_RANK_MAX("user_rank_max", "排行榜显示的最大玩家数量"),
	
	//Weapons
	WEAPON_INDATE_SIMPLE("weapon_indate_simple",   "'简陋'武器使用的最大次数"),
	WEAPON_INDATE_NORMAL("weapon_indate_normal",   "'普通'武器使用的最大次数"),
	WEAPON_INDATE_SOLID("weapon_indate_solid",     "'坚固'武器使用的最大次数"),
	WEAPON_INDATE_ETERNAL("weapon_indate_eternal", "'恒久'武器使用的最大次数"),
	
	//Strength Stone
	STRENGTH_MAX_LEVEL("strength_max_level", "强化的最大等级限制12"),
	STRENGTH_BASE_RATIO("strength_base_ratio", "强化道具1-5等级时属性提升的比率"),
	STRENGTH_NORMAL_RATIO("strength_normal_ratio", "强化道具1-5等级时属性提升的比率"),
	STRENGTH_ADVANCE_RATIO("strength_advance_ratio", "强化道具1-5等级时属性提升的比率"),
	STRENGTH_DEFEND_RATIO("strength_defend_ratio", "强化防御属性时额外提升的比率"),
	STRENGTH_STONE_RATIO("strength_stone_ratio", "强化石强化概率列表"),
	STRENGTH_GODSTONE_RANGE("strength_godstone_range", "神恩符的作用范围"),
	
	//Forge Stone
	//用于区分各个等级合成石成功率的正态分布参数q
	FORGE_SIGMA_RATIO("forge_sigma_ratio", "合成石基于等级提升的基础数值(新数值不会低于该倍率)"),
	FORGE_LUCKY_TIMES("forge_lucky_ratio", "合成石幸运符的加成参数"),
	
	CRAFT_TRANSFER_RATIO("craft_transfer_ratio", "VIP等级转移的成功率影响"),
	
	COMPOSE_ITEM_BASE_RATIO("compose_item_base_ratio", "熔炼成功的基础概率"),
	COMPOSE_ITEM_ADD_RATIO("compose_item_add_ratio",  "熔炼时每增加一块石头提升的成功概率"),
	COMPOSE_WEAPON_COLOR_RATIO("compose_weapon_color_ratio", "熔炼颜色的单项成功概率"),
	CRAFT_EQUIP_STONE_RATIO("compose_equip_stone_ratiomei", "将合成石合成到装备上的成功率"),
	
	//水神石
	CRAFT_STONE_LUCK("craft_stone_luck", "与装备合成后提高幸运属性的道具类型ID"),
	//土神石
	CRAFT_STONE_DEFEND("craft_stone_defend", "与装备合成后提高防御属性的道具类型ID"),
	//风神石
	CRAFT_STONE_AGILITY("craft_stone_agility", "与装备合成后提高敏捷属性的道具类型ID"),
	//火神石
	CRAFT_STONE_ATTACK("craft_stone_attack", "与装备合成后提高攻击属性的道具类型ID"),
	//黄钻石
	CRAFT_STONE_DIAMOND("craft_stone_diamond", "非常珍贵的钻石，可以用来在装备上开孔"),
  //水晶石
	CRAFT_STONE_CRYSTAL("craft_stone_crystal", "魔域地下蕴藏的石头，可以提升装备的等级"),
	//强化石
	CRAFT_STONE_STRENGTH("craft_stone_strength", "用来强装备提高武器伤害或装备护甲的道具类型ID"),
	//幸运符+15%
	CRAFT_STONE_LUCKY1("craft_stone_lucky1", "幸运符+15%的道具类型ID"),
	//幸运符+25%
	CRAFT_STONE_LUCKY2("craft_stone_lucky2", "幸运符+25%的道具类型ID"),
  //必成符
	CRAFT_STONE_WIN("craft_stone_win", "必成符的道具类型ID"),
	//幸运符的通用类型ID
	CRAFT_STONE_LUCKY_TYPEID("craft_stone_lucky_typeid", "幸运符的通用道具类型ID"),
	//神恩符
	CRAFT_STONE_GOD("stone_god_typeid", "神恩符的道具类型ID"),
	//熔炼公式
	CRAFT_STONE_FUNC("stone_func_typeid", "熔炼公式的道具类型ID"),
	//21001 水神石炼化符
	CRAFT_STONE_FUNC_LUCK("stone_func_luck", "水神石炼化符ID"),
	//21002	土神石炼化符
	CRAFT_STONE_FUNC_DEFEND("stone_func_defend", "土神石炼化符ID"),
	//21003	风神石炼化符
	CRAFT_STONE_FUNC_AGILITY("stone_func_agility", "风神石炼化符ID"),
	//21004	火神石炼化符
	CRAFT_STONE_FUNC_ATTACK("stone_func_attack", "火神石炼化符ID"),
	//21005	强化石炼化符
	CRAFT_STONE_FUNC_STRENGTH("stone_func_strength", "强化石炼化符ID"),
	//熔炼戒指和手镯
	CRAFT_STONE_RING("stone_ring_typeid", "熔炼戒指和手镯的道具类型ID"),
	//开孔的成功率
	CRAFT_DIAMOND_RATIO("craft_diamond_ratio", "装备开孔的成功率"),
	//升级宝箱的类型
	BOX_LEVELUP_TYPEID("box_levelup_typeid", "升级奖励的宝箱的道具类型ID"),
	//
	CRAFT_FAILURE_LEVEL_DOWN("craft_failure_level_down", "低于该等级的状态在强化失败后会降级"),
	CRAFT_FAILURE_VIP_LEVEL_DOWN("craft_failure_vip_level_down", "VIP低于该等级的状态在强化失败后会降级"),
	
	USER_BODY_WIDTH("user_body_width", "玩家在地图中的宽度(攻击范围判定用)"),
	USER_BODY_HEIGHT("user_body_height", "玩家在地图中的高度(攻击范围判定用)"),
	//Room
	ROOM_MAX_USER("room_max_user", "房间能容纳的最大用户数(4)"),
	ROOM_MATCH_POWER("room_match_power", "房间匹配时比较用的战斗力差值"),
	ROOM_MATCH_LEVEL("room_match_level", "房间匹配时比较用的等级差值"),
	ROOM_READY_TIMEOUT("room_ready_timeout", "房间从创建到进入准备状态的总时间"),
	ROOM_UNFULL_TIMEOUT("room_unfull_timeout", "多人房间用户未加满的总超时"),
	ROOM_JOIN_TIMEOUT("room_userjoin_timeout", "玩家从加入房间到进入准备状态的总时间(应小于‘room_ready_timeout’)"),
	//Delay
	DELAY_AGLITY_BASE("delay_aglity_base", "计算敏捷对Delay值影响的基数(值越大,敏捷影响越高)"),
	DELAY_ROLE_ATTACK("delay_role_attack", "玩家开火增加的Delay值"),
	DELAY_ROLE_SAVE(  "delay_role_save",   "回合切换增加的Delay值"),
	DELAY_ROLE_FLY(   "delay_role_fly",    "玩家使用纸飞机增加的Delay值"),
	DELAY_POWER(  "delay_power",  "玩家发起大招增加的Delay值"),
	//附加攻击2次
	DELAY_TOOL_AttackTwoMoreTimes("delay_tool_".concat(BuffToolType.AttackTwoMoreTimes.name()), "玩家使用附加攻击2次增加的Delay值"),
	DELAY_TOOL_AttackThreeBranch( "delay_tool_".concat(BuffToolType.AttackThreeBranch.name()),  "玩家使用三叉戟攻击增加的Delay值"),
	DELAY_TOOL_AttackOneMoreTimes("delay_tool_".concat(BuffToolType.AttackOneMoreTimes.name()), "玩家使用附加攻击1次增加的Delay值"),
	DELAY_TOOL_HurtAdd50("delay_tool_".concat(BuffToolType.HurtAdd50.name()), "玩家使用伤害50%增加的Delay值"),
	DELAY_TOOL_HurtAdd40("delay_tool_".concat(BuffToolType.HurtAdd40.name()), "玩家使用伤害40%增加的Delay值"),
	DELAY_TOOL_HurtAdd30("delay_tool_".concat(BuffToolType.HurtAdd30.name()), "玩家使用伤害30%增加的Delay值"),
	DELAY_TOOL_HurtAdd20("delay_tool_".concat(BuffToolType.HurtAdd20.name()), "玩家使用伤害20%增加的Delay值"),
	DELAY_TOOL_HurtAdd10("delay_tool_".concat(BuffToolType.HurtAdd10.name()), "玩家使用伤害10%增加的Delay值"),
	//血瓶
	DELAY_TOOL_RECOVER("delay_tool_".concat(BuffToolType.Recover.name()), "玩家使用加血技能增加的Delay值"),
	//团队血瓶
	DELAY_TOOL_ALLRECOVER("delay_tool_".concat(BuffToolType.AllRecover.name()), "玩家使用团队加血技能增加的Delay值"),
	//隐身
	DELAY_TOOL_HIDDEN("delay_tool_".concat(BuffToolType.Hidden.name()), "玩家使用隐身技能增加的Delay值"),
	//团队隐身
	DELAY_TOOL_ALLHIDDEN("delay_tool_".concat(BuffToolType.AllHidden.name()), "玩家使用团队隐身技能增加的Delay值"),
	//改变风向
	DELAY_TOOL_WIND("delay_tool_".concat(BuffToolType.Wind.name()), "玩家使用改变风向技能增加的Delay值"),
	//冰弹
	DELAY_TOOL_ICE("delay_tool_".concat(BuffToolType.Ice.name()), "玩家使用冰弹技能增加的Delay值"),
	//传送
	DELAY_TOOL_FLY("delay_tool_".concat(BuffToolType.Fly.name()), "玩家使用传送技能增加的Delay值"),
	//引导
	DELAY_TOOL_GUIDE("delay_tool_".concat(BuffToolType.Guide.name()), "玩家使用引导技能增加的Delay值"),
	//怒气
	DELAY_TOOL_ENERGY("delay_tool_".concat(BuffToolType.Energy.name()), "玩家使用怒气技能增加的Delay值"),
	//核弹
	DELAY_TOOL_ATOM("delay_tool_".concat(BuffToolType.Atom.name()), "玩家使用核弹技能增加的Delay值"),
	//免坑
	DELAY_TOOL_NOHOLE("delay_tool_".concat(BuffToolType.NoHole.name()), "玩家使用免坑技能增加的Delay值"),
	
	//玩家发起大招
	THEW_POWER(  "thew_power",  "玩家发起大招减少的体力值"),
	THEW_ROLE_MOVE(  "thew_role_move",  "玩家每移动1次减少的体力值"),
	THEW_ROUND_PERCENT("thew_round_percent", "体力每回合增加的百分比"),
	//附加攻击2次
	THEW_TOOL_AttackTwoMoreTimes("thew_tool_".concat(BuffToolType.AttackTwoMoreTimes.name()), "玩家使用附加攻击2次消耗的体力值"),
	THEW_TOOL_AttackThreeBranch( "thew_tool_".concat(BuffToolType.AttackThreeBranch.name()),  "玩家使用三叉戟攻击消耗的体力值"),
	THEW_TOOL_AttackOneMoreTimes("thew_tool_".concat(BuffToolType.AttackOneMoreTimes.name()), "玩家使用附加攻击1次消耗的体力值"),
	THEW_TOOL_HurtAdd50("thew_tool_".concat(BuffToolType.HurtAdd50.name()), "玩家使用伤害50%消耗的体力值"),
	THEW_TOOL_HurtAdd40("thew_tool_".concat(BuffToolType.HurtAdd40.name()), "玩家使用伤害40%消耗的体力值"),
	THEW_TOOL_HurtAdd30("thew_tool_".concat(BuffToolType.HurtAdd30.name()), "玩家使用伤害30%消耗的体力值"),
	THEW_TOOL_HurtAdd20("thew_tool_".concat(BuffToolType.HurtAdd20.name()), "玩家使用伤害20%消耗的体力值"),
	THEW_TOOL_HurtAdd10("thew_tool_".concat(BuffToolType.HurtAdd10.name()), "玩家使用伤害10%消耗的体力值"),
	//血瓶
	THEW_TOOL_RECOVER("thew_tool_".concat(BuffToolType.Recover.name()), "玩家使用加血技能消耗的体力值"),
	//团队血瓶
	THEW_TOOL_ALLRECOVER("thew_tool_".concat(BuffToolType.AllRecover.name()), "玩家使用团队加血技能消耗的体力值"),
	//隐身
	THEW_TOOL_HIDDEN("thew_tool_".concat(BuffToolType.Hidden.name()), "玩家使用隐身技能消耗的体力值"),
	//团队隐身
	THEW_TOOL_ALLHIDDEN("thew_tool_".concat(BuffToolType.AllHidden.name()), "玩家使用团队隐身技能消耗的体力值"),
	//改变风向
	THEW_TOOL_WIND("thew_tool_".concat(BuffToolType.Wind.name()), "玩家使用改变风向技能消耗的体力值"),
	//冰弹
	THEW_TOOL_ICE("thew_tool_".concat(BuffToolType.Ice.name()), "玩家使用冰弹技能消耗的体力值"),
	//传送
	THEW_TOOL_FLY("thew_tool_".concat(BuffToolType.Fly.name()), "玩家使用传送技能消耗的体力值"),
	//引导
	THEW_TOOL_GUIDE("thew_tool_".concat(BuffToolType.Guide.name()), "玩家使用引导技能消耗的体力值"),
	//怒气
	THEW_TOOL_ENERGY("thew_tool_".concat(BuffToolType.Energy.name()), "玩家使用怒气技能消耗的体力值"),
	//核弹
	THEW_TOOL_ATOM("thew_tool_".concat(BuffToolType.Atom.name()), "玩家使用核弹技能消耗的体力值"),
	//免坑
	THEW_TOOL_NOHOLE("thew_tool_".concat(BuffToolType.NoHole.name()), "玩家使用免坑技能消耗的体力值"),
	
	PRICE_TOOL_AttackTwoMoreTimes("price_tool_".concat(BuffToolType.AttackTwoMoreTimes.name()), "玩家购买附加攻击2次的价格"),
	PRICE_TOOL_AttackThreeBranch( "price_tool_".concat(BuffToolType.AttackThreeBranch.name()),  "玩家购买三叉戟攻击的价格"),
	PRICE_TOOL_AttackOneMoreTimes("price_tool_".concat(BuffToolType.AttackOneMoreTimes.name()), "玩家购买附加攻击1次的价格"),
	PRICE_TOOL_HurtAdd50("price_tool_".concat(BuffToolType.HurtAdd50.name()), "玩家购买伤害50%的价格"),
	PRICE_TOOL_HurtAdd40("price_tool_".concat(BuffToolType.HurtAdd40.name()), "玩家购买伤害40%的价格"),
	PRICE_TOOL_HurtAdd30("price_tool_".concat(BuffToolType.HurtAdd30.name()), "玩家购买伤害30%的价格"),
	PRICE_TOOL_HurtAdd20("price_tool_".concat(BuffToolType.HurtAdd20.name()), "玩家购买伤害20%的价格"),
	PRICE_TOOL_HurtAdd10("price_tool_".concat(BuffToolType.HurtAdd10.name()), "玩家购买伤害10%的价格"),
	//血瓶
	PRICE_TOOL_RECOVER("price_tool_".concat(BuffToolType.Recover.name()), "玩家购买加血技能的价格"),
	//团队血瓶
	PRICE_TOOL_ALLRECOVER("price_tool_".concat(BuffToolType.AllRecover.name()), "玩家购买团队加血技能的价格"),
	//隐身
	PRICE_TOOL_HIDDEN("price_tool_".concat(BuffToolType.Hidden.name()), "玩家购买隐身技能的价格"),
	//团队隐身
	PRICE_TOOL_ALLHIDDEN("price_tool_".concat(BuffToolType.AllHidden.name()), "玩家购买团队隐身技能的价格"),
	//改变风向
	PRICE_TOOL_WIND("price_tool_".concat(BuffToolType.Wind.name()), "玩家购买改变风向技能的价格"),
	//冰弹
	PRICE_TOOL_ICE("price_tool_".concat(BuffToolType.Ice.name()), "玩家购买冰弹技能的价格"),
	//传送
	PRICE_TOOL_FLY("price_tool_".concat(BuffToolType.Fly.name()), "玩家购买传送技能的价格"),
	//引导
	PRICE_TOOL_GUIDE("price_tool_".concat(BuffToolType.Guide.name()), "玩家购买引导技能的价格"),
	//怒气
	PRICE_TOOL_ENERGY("price_tool_".concat(BuffToolType.Energy.name()), "玩家购买怒气技能的价格"),
	//核弹
	PRICE_TOOL_ATOM("price_tool_".concat(BuffToolType.Atom.name()), "玩家购买核弹技能的价格"),
	//免坑
	PRICE_TOOL_NOHOLE("price_tool_".concat(BuffToolType.NoHole.name()), "玩家购买免坑技能的价格"),
	
	//BuffTool Value
	TOOL_RECOVER_VALUE("tool_recover_value", "玩家使用加血道具每次恢复的血量"),
	TOOL_ALL_RECOVER_VALUE("tool_all_recover_value", "玩家使用团队加血道具每次恢复的血量"),
	TOOL_ENERGY_VALUE("tool_energy_value", "玩家使用蓄力道具每次增加的怒气值"),
	TOOL_ICED_VALUE("tool_iced_value", "玩家使用冰冻道具冻结的回合数"),
	TOOL_HIDDEN_VALUE("tool_hidden_value", "玩家使用隐身道具隐藏的回合数"),
	TOOL_POWER_VALUE("tool_power_value", "玩家使用大招造成的伤害倍率"),
	
	//玩家向系统出售便携道具时的折扣率
	SELL_TOOL_DISCOUNT("sell_tool_", "出售便携道具的价格折扣(0-100:0为全价出售)"),
	SELL_GOOD_DISCOUNT("sell_good",  "出售商品的价格折扣(0-100:0为全价出售)"),
	
	PRICE_CRAFT_COMPOSE("price_craft_compose",   "合成装备的金币价格"),
	PRICE_CRAFT_FORGE("price_craft_forge",       "强化装备的金币价格"),
	PRICE_CRAFT_TRANSFER("price_craft_transfer", "转移装备属性的金币价格"),
	
	//在线奖励步骤
	USER_ONLINE_REWARD_STEP("user_online_reward_step", 
			"用户在线奖励的时间点，例如'07:30'表示7点30"),
	
	//VIP
	NORMAL_SHOP_DISCOUNT("normal_shop_discount", "普通用户在商城中享受的折扣(0-100)"),
	VIP_SHOP_DISCOUNT("vip_shop_discount", "VIP用户在商城中享受的折扣(0-100)"),
	VIP_CHARGE_DISCOUNT("vip_charge_discount", "VIP用户充值RMB的折扣金额(0.0-9.9)"),
	
	//幸运值和敏捷值的基数
	LUCK_BASE("luck_base", "幸运值的基数，默认10000"),
	AGILITY_BASE("agility_base", "敏捷值的基数，默认10000"),
	
	//抽奖金币数量
	REWARD_GOLDEN_LIST("reward_golden_list", "抽奖使用的金币数量列表"),
	REWARD_YUANBAO_LIST("reward_yuanbao_list", "抽奖使用的元宝数量列表"),
	
	//Mail
	MAIL_MAX_COUNT("mail_max_count", "邮件最多可存放的信息数量"),
	MAIL_MAX_SUBJECT("mail_max_subject", "邮件标题最大的字数"),
	MAIL_MAX_CONTENT("mail_max_content", "邮件内容的最大字数"),
	MAIL_EXPIRE_SECONDS("mail_expire_seconds", "邮箱最长可保留的秒数"),
	
	//CoolDown time
	CHAT_USER_COOLDOWN("chat_user_cooldown", "普通用户发送世界聊天消息的冷却时间"),
	CHALLENGE_USER_COOLDOWN("challenge_user_cooldown", "向其他玩家发起挑战的冷却时间"),
	
	//Email reward itemid
	EMAIL_REWARD_ITEMID("email.reward.itemid", "验证邮箱后用户获得的奖励ID（itemid）"),
	
  //颜色武器的基础能力加成值
	WEAPON_COLOR_GREEN_RATIO("weapon_color_green_ratio",  "绿色武器的基础属性加成值(1.1f)"),
	WEAPON_COLOR_BLUE_RATIO("weapon_color_blue_ratio",   "蓝色武器的基础属性加成值(1.25f)"),
	WEAPON_COLOR_PINK_RATIO("weapon_color_pink_ratio",   "粉色武器的基础属性加成值(1.5f)"),
	WEAPON_COLOR_ORANGE_RATIO("weapon_color_orange_ratio", "橙色武器的基础属性加成值(2.0f)"),
	WEAPON_COLOR_PURPLE_RATIO("weapon_color_orange_ratio", "紫色武器的基础属性加成值(3.0f)"),
	
	WEAPON_WARRANTY_UNIT("weapon_warranty_unit", "武器的最高每日损耗的单位"),
	
	/**
	 * Secure limit for game system
	 */
	SECURE_LIMIT_YUANBAO_DAILY("secure_limit_yuanbao_daily", "每日元宝数额累加的上限"),
	SECURE_LIMIT_GOLDEN_DAILY("secure_limit_golden_daily",  "每日金币数额累加的上限"),
	SECURE_LIMIT_EXP_DAILY("secure_limit_exp_daily",     "每日经验数额累加的上限"),
	
	/**
	 * 玩家一日获得的最高行动值
	 */
	ROLE_ACTION_LIMIT("role_action_limit", "玩家每日行动值上限"),
	ROLE_ACTION_GAIN_HOURLY("role_action_gain_hourly", "玩家每小时自动增长的行动值"),
	ROLE_ACTION_GAIN_LEVELUP("role_action_gain_levelup", "玩家每次升级自动增长的行动值"),
	
	//元宝到金币的转换率
	YUANBAO_TO_GOLDEN_RATIO("yuanbao_to_golden_ratio", "元宝到金币的转换率"),
	
	TREASURE_HUNT_FREE_COUNT("treasure_hunt_free_count", "每日免费寻宝的次数"),
	TREASURE_HUNT_REFRESH_COUNT("treasure_hunt_refresh_count", "每日免费刷新寻宝的次数"),
	TREASURE_HUNT_NORMAL_PRICE("treasure_hunt_normal_price", "每日正常寻宝的价格"),
	TREASURE_HUNT_ADVANCE_PRICE("treasure_hunt_advance_price", "每日高级寻宝的价格"),
	TREASURE_HUNT_PRO_PRICE("treasure_hunt_pro_price", "每日专家寻宝的价格"),
	TREASURE_HUNT_FRESH_MILLIS("treasure_hunt_fresh_millis", "每次刷新寻宝的时间间隔(毫秒)"),
	
	//各武器等级属性提升比例
	WEAPON_LEVEL_RATIO("weapon_level_ratio", "装备分为十个等级，每提升一个等级属性会相应提升倍率"),
	
	//VIP giftbox id
	VIP_GIFT_BOX_ID("vip_gift_box_id", "VIP各个等级获得的礼包ID"),
	VIP_BAG_SPACE("vip_bag_space",     "VIP各个等级的背包空间"),
	VIP_OFFLINE_EXP("vip_offline_exp", "VIP各个等级挂机经验封顶值"),
	VIP_OFFLINE_MAX_EXP("vip_offline_max_exp", "VIP各个等级挂机经验封顶值"),
	VIP_PVP_GOOD_PROP("vip_pvp_good_prop",   "VIP各个等级PVP战斗中掉落精良武器概率"),
	VIP_PVE_GOOD_PROP("vip_pve_good_prop",   "VIP各个等级PVE战斗中掉落精良武器概率"),
	VIP_BUY_ROLEACTION("vip_buy_roleaction", "VIP各个等级可购买的体力值数量"),
	VIP_BUY_CAISHEN("vip_buy_caishen",       "VIP各个等级可购买的招财数量"),
	VIP_CAN_TREASURE_HUNT("vip_can_treasure_hunt",   "VIP各个等级是否可以无限寻宝"),
	VIP_CAN_TRANSFER_LEVEL("vip_can_transfer_level",  "VIP各个等级是否可以跨等级转换强化级别"),
	VIP_CAN_TRANSFER_COLOR("vip_can_transfer_color",  "VIP各个等级是否可以跨颜色转换强化级别"),
	VIP_BATTLE_EXP_RATIO("vip_battle_exp_ratio",      "VIP各个等级战斗经验加成"),
	VIP_STRENGTH_RATIO("vip_strength_ratio",          "VIP各个等级强化概率加成"),
	
	//小喇叭
	SMALL_SPEAKER_ID("small_speaker", "小喇叭的物品ID"),
	
	//新手引导
	NEWBIE_BATTLE_GUIDE("newbie_battle_guide", "新手引导的引导线"),
	
	//计费渠道的KEY
	CHARGE_DANGLE_KEY("charge_dangle_key", "当乐计费渠道的MerchantKey"),
	CHARGE_XIAOMI_KEY("charge_xiaomi_key", "小米计费渠道的AppKey"),
	
	//改名卡
	ITEM_CHANGE_NAME_ID("item_changename_id", "改名卡的ID"),
	
	LOGIN_MAJOR_VERSION("login.major.version", "登陆的大版本号"),
	LOGIN_MINOR_VERSION("login.minor.version", "登陆的小版本号"),
	
	EMAIL_SMTP("email.smtp", "发送邮件的SMTP服务器"),
	
	BOSS_SINGLE_EXPIRE("boss.single.expire",  "单人BOSS限定的通关时间(秒)"),
	
	//商城价格设置
	SHOP_DPR_TO_GOLDEN("shop.dpr.to.golden",   "商城中战斗力对应的金币数量"),
	SHOP_DPR_TO_YUANBAO("shop.dpr.to.yuanbao", "商城中战斗力对应的元宝数量"),
	SHOP_PRICE_SIMPLE_RATIO("shop.price.simple", "商城中简单品质价格倍率"),
	SHOP_PRICE_NORMAL_RATIO("shop.price.normal", "商城中普通品质价格倍率"),
	SHOP_PRICE_TOUGH_RATIO("shop.price.tough",  "商城中坚固品质价格倍率"),
	
	
	GUILD_CREATE_GOLDEN("guild.create.golden", "公会升级所需的金币"),
	GUILD_ABILITY_CREDIT("guild.ability.credit", "升级公会个人技能所需要的贡献度"),
	GUILD_LEVEL_WEALTH("guild.level.wealth",   "公会升级所需的财富"),
	GUILD_LEVEL_GOLDEN("guild.level.golden",   "公会升级所需的金币"),
	GUILD_LEVEL_MAXCOUNT("guild.level.maxcount", "公会各个等级的最大人数"),
	GUILD_LEVEL_MANAGER("guild.level.manager", "公会各个等级的官员数量"),
	GUILD_LEVEL_EXPRATIO("guild.level.expratio", "公会各个等级的战斗经验加成"),
	GUILD_OPFEE("guild.opfee", "公会每周的维持费用"),
	GUILD_YUANBAO_CREDIT("guild.yuanbao.credit", "公会 元宝:贡献"),
	GUILD_YUANBAO_WEALTH("guild.yuanbao.wealth", "公会 元宝:财富"),
	GUILD_GOLDEN_CREDIT("guild.yuanbao.credit", "公会 金币:贡献"),
	GUILD_GOLDEN_WEALTH("guild.yuanbao.wealth", "公会 金币:财富"),
	GUILD_FACILITY_MIN_CREDIT("guild.facility.min.credit", "公会设施各个等级所需要的贡献度"),
	GUILD_STORAGE_SIZE("guild.storage.size",     "公会仓库各个等级的可用尺寸"),
	GUILD_FACILITY_COOLDOWN("guild.facility.cooldown",     "公会设施升级后的冷却时间"),
	GUILD_FACILITY_COOLDOWN_YUANBAO("guild.facility.cooldown.yuanbao",     "公会设施立即冷却的元宝"),
	GUILD_CRAFT_STRENGTH("guild.craft.strength", "公会铁匠铺各个等级强化加成"),
	GUILD_ABILITY_ATTACK("guild.ability.attack",  "攻击等级升级后，可提升的攻击比例"),
	GUILD_ABILITY_DEFEND("guild.ability.defend",  "防御等级升级后，可提升的防御比例"),
	GUILD_ABILITY_AGILITY("guild.ability.agility",  "敏捷等级升级后，可提升的敏捷比例"),
	GUILD_ABILITY_LUCKY("guild.ability.lucky",  "幸运等级升级后，可提升的幸运比例"),
	GUILD_ABILITY_BLOOD("guild.ability.blood",  "血量等级升级后，可提升的血量比例"),
	GUILD_ABILITY_PRAY("guild.ability.pray",    "祈福等级升级后，可提升的祈福比例"),
	GUILD_ABILITY_TREASURE("guild.ability.treasure",  "寻宝等级升级后，可提升的寻宝比例");
	
	//The key itself
	private String key;
	//The desc for the key.
	private String desc;
	//The cache mapping
	private static final HashMap<String, GameDataKey> KEY_MAP = new
			HashMap<String, GameDataKey>();
	static {
		for ( GameDataKey key : GameDataKey.values() ) {
			KEY_MAP.put(key.key, key);
		}
	}
	
	GameDataKey(String key, String desc) {
		this.key = key;
		this.desc = desc;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	/**
	 * Get the GameDataKey by its key string. It may be null.
	 * 
	 * @param key
	 * @return
	 */
	public static GameDataKey fromKey(String key) {
		return KEY_MAP.get(key);
	}
}
