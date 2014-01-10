package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.guild.GuildRole;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserSync.BceUserSync;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The user should refresh his status from database.
 * 
 * 1: add exp
 * 2: add golden
 * 3: add yuanbao
 * 
 * @author wangqi
 *
 */
public class BceUserSyncHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceUserSyncHandler.class);
	
	private static final BceUserSyncHandler instance = new BceUserSyncHandler();
	
	private BceUserSyncHandler() {
		super();
	}

	public static BceUserSyncHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceUserSync");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceUserSync userSync = (BceUserSync)request.payload;
		int mode = userSync.getMode();
		int value = userSync.getValue();
		switch ( mode ) {
			case 1:
				user.setExp(user.getExp()+value);
				UserManager.getInstance().saveUser(user, false);
				GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
				logger.debug("User {} add {} to exp", user.getRoleName(), value);
				break;
			case 2:
				user.setGolden(user.getGolden()+value);
				UserManager.getInstance().saveUser(user, false);
				GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
				logger.debug("User {} add {} to golden", user.getRoleName(), value);
				break;
			case 3:
				user.setYuanbaoFree(user.getYuanbaoFree()+value);
				UserManager.getInstance().saveUser(user, false);
				GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
				logger.debug("User {} add {} to yuanbao", user.getRoleName(), value);
				break;
			case 4:
				/**
				 * 加入公会的刷新协议
				 */
				String guildId = userSync.getObject();
				if ( guildId != null ) {
					Guild guild = GuildManager.getInstance().queryGuildById(guildId);
					if ( guild != null ) {
						user.setGuildId(guildId);
						user.setGuild(guild);
						UserManager.getInstance().saveUser(user, false);

						GuildManager.getInstance().createGuildMember(guild, user, true, GuildRole.member);

						GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
					} else {
						logger.warn("The guildId {} does not exist in db for user:{}", guildId, user.getRoleName());
					}
				}
				break;
			case 5:
				/**
				 * 解散/退出/被开除的公会的刷新协议
				 */
				Guild guild = user.getGuild();
				user.setGuild(null);
				user.setGuildId(null);
				user.setGuildMember(null);
				UserManager.getInstance().saveUser(user, false);
				GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
				break;
			case 6:
				/**
				 * 公会职位变更的刷新协议，刷新公会和公会成员信息
				 */
					guild = GuildManager.getInstance().queryGuildById(user.getGuildId());
					user.setGuild(guild);
					GuildMember member = GuildManager.getInstance().queryGuildMemberByUserId(user.get_id());
					user.setGuildMember(member);
				break;
			default:
				logger.warn("unsupported sync mode: {}", mode);
				break;
		}
	}
	
}
