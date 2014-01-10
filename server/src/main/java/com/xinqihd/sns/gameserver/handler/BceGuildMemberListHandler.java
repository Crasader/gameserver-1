package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildMemberList.BceGuildMemberList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildMemberList.BseGuildMemberList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildMemberListHandler is used for protocol GuildMemberList 
 * @author wangqi
 *
 */
public class BceGuildMemberListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildMemberListHandler.class);
	
	private static final BceGuildMemberListHandler instance = new BceGuildMemberListHandler();
	
	private BceGuildMemberListHandler() {
		super();
	}

	public static BceGuildMemberListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildMemberList");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildMemberList payload = (BceGuildMemberList)request.payload;
		
		BseGuildMemberList.Builder builder = BseGuildMemberList.newBuilder();
		if ( !payload.getOnline() ) {
			Collection<GuildMember> members = GuildManager.getInstance().listGuildMember(user);
			if ( members != null ) {
				for ( GuildMember member : members ) {
					BasicUser buser = UserManager.getInstance().queryBasicUser(member.getUserId());
					if ( buser != null ) {
						builder.addMembers(member.toGuildMember(buser));
					}
				}
			}
		} else {
			Collection<String> sessionKeyStrs = GuildManager.getInstance().listGuildMemberOnline(user.getGuildId());
			for ( String sessionKeyStr : sessionKeyStrs ) {
				SessionKey sessionKey = SessionKey.createSessionKeyFromHexString(sessionKeyStr);
				User u = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
				if ( u != null ) {
					GuildMember member = u.getGuildMember();
					if ( member != null ) {
						member.setOnline(true);
						builder.addMembers(member.toGuildMember(u));
					}
				} else {
					UserId userId = GameContext.getInstance().findUserIdBySessionKey(sessionKey);
					BasicUser bu = UserManager.getInstance().queryBasicUser(userId);
					GuildMember gm = GuildManager.getInstance().queryGuildMemberByUserId(userId);
					if ( gm != null ) {
						builder.addMembers(gm.toGuildMember(bu));
					}
				}
			}
		}
		GameContext.getInstance().writeResponse(userSessionKey, builder.build());
	}
	
	
}
