package com.xinqihd.sns.gameserver.admin.i18n;

import java.util.HashMap;

public class ColumnNames {

	private static HashMap<String, String> map = new HashMap<String, String>();
	
	static {
		map.put("profile" , "profile");
		map.put("vip" , "vip");
		map.put("ability" , "ability");
		map.put("login" , "登陆");
		map.put("config" , "配置");
		map.put("wealth" , "wealth");
		map.put("loc" , 	 "位置");
		map.put("tools" , "便携道具");
		map.put("wears" , "wears");
		map.put("count" , "count");
		map.put("items" , "items");
		map.put("class" , "class");
		map.put("name" , "名称");
		map.put("type" , "类型");
		map.put("reqlv" , "reqlv");
		map.put("scrollAreaX" , "scrollAreaX");
		map.put("scrollAreaY" , "scrollAreaY");
		map.put("scrollAreaWidth" , "scrollAreaWidth");
		map.put("scrollAreaHeight" , "scrollAreaHeight");
		map.put("layers" , "layers");
		map.put("bgm" , "bgm");
		map.put("damage" , "damage");
		map.put("bosses" , "bosses");
		map.put("enemies" , "enemies");
		map.put("startPoints" , "startPoints");
		map.put("index" , "index");
		map.put("quality" , "品质");
		map.put("qualityColor" , "品质颜色");
		map.put("sName" , "名称");
		map.put("equipType" , "装备类型");
		map.put("addAttack" , "加攻击");
		map.put("addDefend" , "加防御");
		map.put("addAgility" , "加敏捷");
		map.put("addLuck" , "加幸运");
		map.put("addBlood" , "加血量");
		map.put("addBloodPercent" , "加血量比例");
		map.put("addThew" , "加体力");
		map.put("addDamage" , "加伤害");
		map.put("addSkin" , "加护甲");
		map.put("sex" , "性别");
		map.put("unused1" , "unused1");
		map.put("unused2" , "unused2");
		map.put("unused3" , "unused3");
		map.put("indate1" , "indate1");
		map.put("indate2" , "indate2");
		map.put("indate3" , "indate3");
		map.put("sign" , "sign");
		map.put("lv" , "lv");
		map.put("autoDirection" , "自动定位");
		map.put("sAutoDirection" , "大招定位");
		map.put("specialAction" , "特殊攻击");
		map.put("radius" , "攻击半径");
		map.put("sRadius" , "大招攻击半径");
		map.put("expBlend" , "混合(不用)");
		map.put("expSe" , "音效");
		map.put("power" , "战斗力(未使用)");
		map.put("autoDestory" , "自动伤害");
		map.put("bullet" , "子弹标识");
		map.put("icon" , "图标");
		map.put("info" , "描述");
		map.put("bubble" , "bubble");
		map.put("slot" , "卡槽");
		map.put("avatar" , "形象");
		map.put("propInfoId" , "propInfoId");
		map.put("level" , "level");
		map.put("moneyType" , "货币类型");
		map.put("buyPrices" , "购买价格");
		map.put("banded" , "是否绑定");
		map.put("discount" , "折扣");
		map.put("sell" , "出售");
		map.put("limitCount" , "limitCount");
		map.put("limitGroup" , "limitGroup");
		map.put("shopId" , "shopId");
		map.put("isItem" , "isItem");
		map.put("catalogs" , "商品目录");
		map.put("desc" , "描述");
		map.put("taskTarget" , "taskTarget");
		map.put("step" , "步骤(step)");
		map.put("exp" , "经验");
		map.put("gold" , "金币");
		map.put("ticket" , "礼券");
		map.put("gongxun" , "功勋");
		map.put("caifu" , "财富");
		map.put("seq" , "顺序(seq)");
		map.put("userLevel" , "用户等级");
		map.put("script" , "脚本");
		map.put("awards" , "奖励");
		map.put("rival" , "rival");
		map.put("typeId" , "typeId");
		map.put("q" , "概率q");
		map.put("rewards" , "奖励");
		map.put("conditions" , "开启条件");
		map.put("dayNum" , "天数");
		map.put("price" , "价格");
		map.put("currency" , "货币");
		map.put("yuanbao" , "元宝");
		map.put("isHotSale" , "是否热卖");
		map.put("month" , "月");
		map.put("yuanbaoPrice" , "元宝价格");
		map.put("voucherPrice" , "礼券价格");
		map.put("medalPrice" , "勋章价格");
	}
	
	public static String translate(String columnName) {
		String name = map.get(columnName);
		if ( name != null ) {
			return name;
		}
		return columnName;
	}
}
