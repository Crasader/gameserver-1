package com.xinqihd.sns.gameserver.entity.user;

/**
 * Since User entity is big and ofter it only changes some field,
 * so if we update all fields when saving an user, it is a costly operation.
 * In addition, the MongoDB's write is single thread and global lock operation,
 * we have to optimize it.
 * 
 * @author wangqi
 *
 */
public enum UserChangeFlag {
	
	//Common
	COUNTRY("country"),
	GENDER("gender"),
	ICONURL("icon"),
	ISVIP("isvip"),
	LEVEL("level"),
	ROLENAME("rolename"),
	USERNAME("username"),
	VIPLEVEL("viplevel"),
	
	ABTEST("abtest"),
	AGILITY("agility"),
	ATTACK("attack"),
	BATTLECOUNT("battlecount"),
	BLOOD("blood"),
	CDATE("cdate"),
	CHANNEL("channel"),
	CLIENT("client"),
	CONFIGEFFECTSWITCH("eftswitch"),
	CONFIGEFFECTVOLUME("eftvolume"),
	CONFIGHIDEGLASS("hideglass"),
	CONFIGHIDEHAT("hidehat"),
	CONFIGHIDESUITE("hidesuite"),
	CONFIGLEADFINISH("leadfinish"),
	CONFIGMUSICSWITCH("muswitch"),
	CONFIGMUSICVOLUME("muvolume"),
	CONTINULOGINTIMES("logintimes"),
	DAMAGE("damage"),
	DEFEND("defend"),
	EMAIL("email"),
	EXP("exp"),
	FAILCOUNT("failcount"),
	GOLDEN("golden"),
	LDATE("ldate"),
	TDATE("tdate"),
	LOC("loc"),
	LUCK("luck"),
	MEDAL("medal"),
	PASSWORD("password"),
	POWER("power"),
	REMAINLOTTERYTIMES("lotterytimes"),
	SKIN("skin"),
	TKEW("tkew"),
	TOTALMIN("totalmin"),
	TOTALKILL("kills"),
	SCREEN("screen"),
	VIPBDATE("vipbdate"),
	VIPEDATE("vipedate"),
	VIPEXP("vipexp"),
	VOUCHER("voucher"),
	WINODDS("winodds"),
	WINS("wins"),
	YUANBAO("yuanbao"),
	YUANBAO_FREE("yuanbaoFree"),
	TOOLS("tools"),
	MAX_TOOL_COUNT("maxToolCount"),
	LOGIN_STATUS("loginstatus"),
	LOGIN_STATUS_DESC("loginstatusdesc"),
	CURRENT_TOOL_COUNT("currentToolCount"),
	USER_STATUS("status"),
	ACHIEVEMENT("achievement"),
	ISGUEST("guest"),
	TUTORIAL("tutorial"),
	VERIFIED("verified"),
	DEVICETOKEN("devicetoken"),
	TUTORIALMARK("tutormark"),
	ROLETOTALACTIONS("actions"),
	CHARGED_YUANBAO("charged"),
	CHARGED_COUNT("chargecount"),
	ACCOUNT_NAME("account"),
	UUID("uuid"),
	WEIBO("weibo"),
	GUILDID("guildid"),
	SERVERID("serverid"),
	ISADMIN("admin"),
	VALUEMAP("valuemap");
	
//	BAG("bag"),
//	RELATION("relations");
	
	private String value;
	
	UserChangeFlag(String value) {
		this.value = value;
	}
	
	public String value() {
		return this.value;
	}

}
