package com.xinqihd.sns.gameserver.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.MongoUserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildSearchMember.BceGuildSearchMember;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildSearchMember.BseGuildSearchMember;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildSearchMemberHandler is used for protocol GuildSearchMember 
 * @author wangqi
 *
 */
public class BceGuildSearchMemberHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildSearchMemberHandler.class);
	
	private static final BceGuildSearchMemberHandler instance = new BceGuildSearchMemberHandler();
	
	private BceGuildSearchMemberHandler() {
		super();
	}

	public static BceGuildSearchMemberHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildSearchMember");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildSearchMember payload = (BceGuildSearchMember)request.payload;

		BseGuildSearchMember.Builder builder = BseGuildSearchMember.newBuilder();
		int action = payload.getAction();
		if ( action == 0 ) {
			Collection<GuildMember> members = GuildManager.getInstance().
					searchGuildMember(user.getGuildId(), payload.getKey());
			for ( GuildMember member : members ) {
				BasicUser bUser = MongoUserManager.getInstance().queryBasicUser(member.getUserId());
				builder.addMembers(member.toGuildMember(bUser));
			}	
		} else {
			List<BasicUser> users = UserManager.getInstance().queryBasicUserByRoleNameRegex(payload.getKey());
			for ( BasicUser buser : users ) {
				if ( buser.getGuildId() == null ) {
					builder.addMembers(buser.toGuildMember());
				}
			}
		}

		GameContext.getInstance().writeResponse(userSessionKey, builder.build());
	}
	
	
}
