package com.xinqihd.sns.gameserver.handler;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.UserLoginStatus;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.ServerRoleList;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserRoleList.BceUserRoleList;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit.BseInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseUserRoleList.BseUserRoleList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * 
 * @author wangqi
 *
 */
public class BceUserRoleListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceUserRoleListHandler.class);
	
	private static final BceUserRoleListHandler instance = new BceUserRoleListHandler();
	
	private BceUserRoleListHandler() {
		super();
	}

	public static BceUserRoleListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceUserRoleList");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceUserRoleList roleList = (BceUserRoleList)request.payload;
		String serverId = roleList.getServerid();
		User user = null;
		if ( sessionKey != null ) {
			user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		}
		Account account = null;
		if ( user != null ) {
			account = user.getAccount();
		} else {
			account = (Account)session.getAttribute(AccountManager.IOSESSION_ACCOUNT);
		}
		if ( account == null && user != null ) {
			account = AccountManager.getInstance().queryAccountByName(user.getAccountName());
		}
		ServerRoleList serverRoleList = null;
		if ( account != null ) {
			ArrayList<ServerRoleList> serverRoles = account.getServerRoles();
			for ( ServerRoleList srl : serverRoles ) {
				if ( srl.getServerId().equals(serverId) ) {
					serverRoleList = srl;
					break;
				}
			}
			if ( serverRoleList != null ) {
				BseUserRoleList.Builder builder = BseUserRoleList.newBuilder();
				builder.setServerid(serverRoleList.getServerId());
				/**
				 * TODO 老的用户没有保存userid属性，需要在这里检查下
				 */
				ArrayList<String> roleNames = serverRoleList.getRoleNames();
				ArrayList<String> userIds = serverRoleList.getUserIds();
				if ( roleNames.size() > userIds.size() ) {
					userIds.clear();
					for (Iterator iterator = roleNames.iterator(); iterator.hasNext();) {
						String roleName = (String) iterator.next();
						BasicUser basicUser = UserManager.getInstance().queryBasicUserByRoleName(roleName);
						if ( basicUser != null ) {
							userIds.add(basicUser.get_id().toString());
						} else {
							iterator.remove();
							logger.info("Remove roleName {} for account {} because it does not exist", roleName, account.getUserName());
						}
					}
				}
				/**
				 * Check forbidden status
				 */
				for ( int i=0; i<userIds.size(); i++ ) {
					String userIdStr = userIds.get(i);
					String roleName = roleNames.get(i);
					UserId userId = UserId.fromString(userIdStr);
					if ( userId != null ) {
						UserLoginStatus status = UserManager.getInstance().checkUserLoginStatus(userId);
						if ( status != UserLoginStatus.HIDE ) {
							builder.addUserid(userIdStr);
							builder.addRolename(roleName);
						}
					}
				}
				GameContext.getInstance().writeResponse(session, builder.build(), sessionKey);
			} else {
				logger.warn("The serverId {} is not found ", serverId);
				BseUserRoleList.Builder builder = BseUserRoleList.newBuilder();
				builder.setServerid(roleList.getServerid());
				GameContext.getInstance().writeResponse(session, builder.build(), sessionKey);
			}
		} else {
			if ( user != null ) {
				logger.warn("The given account is null for {}", user.getRoleName());
			} else {
				logger.warn("The given account & user is null for sessionkey {}", sessionKey);
			}
			BseInit.Builder builder = BseInit.newBuilder();
			builder.setSuccess(false);
//			BseUserRoleList.Builder builder = BseUserRoleList.newBuilder();
//			builder.setServerid(roleList.getServerid());
			GameContext.getInstance().writeResponse(session, builder.build(), null);
		}
	}
	
}
