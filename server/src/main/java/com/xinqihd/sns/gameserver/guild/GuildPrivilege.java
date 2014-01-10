package com.xinqihd.sns.gameserver.guild;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildPrivilegeList;

/**
 * 权限
 * 修改公告:  announce
 * 职位调整:  guildrole
 * 招收成员:  recruit
 * 开除成员:  firememeber
 * 建筑升级:  levelup
 * 公会战斗:  combat
 * 使用仓库:  takebag
 * 
 * @author wangqi
 *
 */
public enum GuildPrivilege {

	announce,
	guildrole,
	recruit,
	firememeber,
	levelup,
	combat,
	takebag;
	
}
