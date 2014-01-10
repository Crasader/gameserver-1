package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceDeleteRole.BceDeleteRole;
import com.xinqihd.sns.gameserver.proto.XinqiBseDeleteRole.BseDeleteRole;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The BceLoginHandler is used for protocol Login
 * 
 * @author wangqi
 * 
 */
public class BceDeleteRoleHandler extends SimpleChannelHandler {

	private static final Logger logger = LoggerFactory.getLogger(BceDeleteRoleHandler.class);

	private static final BceDeleteRoleHandler instance = new BceDeleteRoleHandler();

	private BceDeleteRoleHandler() {
		super();
	}

	public static BceDeleteRoleHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {

		XinqiMessage request = (XinqiMessage) message;
		BceDeleteRole createRole = (BceDeleteRole) request.payload;
		String userIdStr = createRole.getUserid();
		String serverId = createRole.getServerid();
		
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("->BceDeleteRole");
			}

			UserId userId = UserId.fromString(userIdStr);
			User user = UserManager.getInstance().queryUser(userId);
			String roleName = user.getRoleName();
			/**
			 * Use the new account system
			 */
			//LoginManager.getInstance().createGameRole(session, userToken, roleName, gender);
			//AccountManager.getInstance().deleteGameRole(session, user, serverId);
			AccountManager.getInstance().disableGameRole(session, user, serverId);

		} catch (Throwable e) {
			BseDeleteRole.Builder builder = BseDeleteRole.newBuilder();
			builder.setErrorcode(1);
			builder.setDesc(Text.text("deleterole.failure"));
			GameContext.getInstance().writeResponse(session, builder.build(), sessionKey);

			logger.warn("Failed to delete role by userId", userIdStr, e.getMessage());
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage(), e);
			}
		}
	}
	
}
