package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceCreateRole.BceCreateRole;
import com.xinqihd.sns.gameserver.proto.XinqiBseCreateRole.BseCreateRole;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The BceLoginHandler is used for protocol Login
 * 
 * @author wangqi
 * 
 */
public class BceCreateRoleHandler extends SimpleChannelHandler {

	private static final Logger logger = LoggerFactory.getLogger(BceCreateRoleHandler.class);

	private static final BceCreateRoleHandler instance = new BceCreateRoleHandler();

	private BceCreateRoleHandler() {
		super();
	}

	public static BceCreateRoleHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("->BceCreateRoleHandler");
			}

			XinqiMessage request = (XinqiMessage) message;

			BceCreateRole createRole = (BceCreateRole) request.payload;
			String userToken = createRole.getUsertoken();
			String roleName = createRole.getRolename();
			String serverId = createRole.getServerid();
			int gender = createRole.getGender();

			/**
			 * Use the new account system
			 */
			//LoginManager.getInstance().createGameRole(session, userToken, roleName, gender);
			AccountManager.getInstance().createGameRole(session, userToken, roleName, gender, serverId);
			
		} catch (Throwable e) {
			BseCreateRole.Builder createRole = BseCreateRole.newBuilder();
			createRole.setCode(ErrorCode.OTHERS.ordinal());
			createRole.setDesc(Text.text(ErrorCode.OTHERS.desc()));
			XinqiMessage response = new XinqiMessage();
			response.payload = createRole.build();
			GameContext.getInstance().writeResponse(session, response, sessionKey);

			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage());
			}
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage(), e);
			}
		}
	}
	
}
