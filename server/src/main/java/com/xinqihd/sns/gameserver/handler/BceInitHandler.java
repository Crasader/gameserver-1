package com.xinqihd.sns.gameserver.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.ServerListManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.ServerRoleList;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.proto.XinqiBceInit.BceInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit;
import com.xinqihd.sns.gameserver.proto.XinqiBsePromotion.BsePromotion;
import com.xinqihd.sns.gameserver.proto.XinqiBseTaskList;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.session.CipherManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.LocaleThreadLocal;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The BceInitHandler is used for protocol Init 
 * @author wangqi
 *
 */
public class BceInitHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceInitHandler.class);
	
	private static final BceInitHandler instance = new BceInitHandler();
	
	private BceInitHandler() {
		super();
	}

	public static BceInitHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceInit");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseInit.BseInit.Builder builder = XinqiBseInit.BseInit.newBuilder();
		
		BceInit bceInit = (BceInit)request.payload;
		String token = bceInit.getToken();
		SessionKey userSessionKey = null;
		boolean success = false;
		boolean refresh = false;
		/**
		 * Since the auto-login should refresh many user data,
		 * the logic is really complicated. I disable the auto-login.
		 * User have to login after timeout.
		 * wangqi 2012-11-11
		 */
		success = false;
		//builder.setRefresh(true);
		//builder.setSuccess(false);

		if ( !StringUtil.checkNotEmpty(token) ) {
			logger.debug("Token is null. Reconnect fail");
			builder.setSuccess(false);
		} else {
			UserId userId = CipherManager.getInstance().checkEncryptedUserToken(token);
			if ( userId != null ) {
				refresh = true;
				userSessionKey = GameContext.getInstance().findSessionKeyByUserId(userId);
				if ( userSessionKey != null ) {
					logger.debug("Restore the session from Redis. {}", userSessionKey);
					refresh = false;
				}
				
				User user = GameContext.getInstance().getUserManager().queryUser(userId);
				if ( user != null ) {
					Locale locale = GameContext.getInstance().getSessionManager().findUserLocale(userSessionKey);
					Account account = AccountManager.getInstance().queryAccountByRoleName(user.getRoleName());
					loginProcess(session, userSessionKey, locale, user, account);

					//Regenerate the token
					String newToken = CipherManager.getInstance().generateEncryptedUserToken(userId);
					success = true;
					builder.setSuccess(success);
					builder.setToken(newToken);
					builder.setRefresh(refresh);
					if ( user.getSessionKey() != null ) {
						builder.setSessionid(user.getSessionKey().toString());
					}
					
					StatClient.getIntance().sendDataToStatServer(user, StatAction.Init, refresh, success);
				} else {
					logger.debug("Failed to query the user.");
					success = false;
					builder.setSuccess(success);
				}
			} else {
				logger.debug("Cannot find the userId {}. Reconnect fail.", userId);
				success = false;
				builder.setSuccess(success);
			}
		}

		response.payload = builder.build();
		GameContext.getInstance().writeResponse(session, response, userSessionKey);
		
	}
	
	/**
	 * 
	 * @param loginConfigVersion
	 * @param session
	 * @param uuid
	 * @param screen
	 * @param deviceToken
	 * @param client
	 * @param lang
	 * @param channel
	 * @param sessionKey
	 * @param user
	 * @param accounId
	 * @return
	 */
	public final boolean loginProcess(IoSession session, 
			SessionKey sessionKey, Locale locale,
			final User user, Account account) {

		logger.debug("User {} successfully login", user.getRoleName());
											
		//Get user's bag and relation and unlock
		GameContext.getInstance().getUserManager().queryUserBag(user);
		GameContext.getInstance().getUserManager().queryUserRelation(user);
		GameContext.getInstance().getUserManager().queryUserUnlock(user);
		
		//user.setUuid(uuid);
		//user.setScreen(screen);
		//user.setDeviceToken(deviceToken);
		/**
		 * Change last login role
		 */
		if ( account != null ) {
			user.setAccount(account);
			/**
			 * Check user server id
			 */
			ArrayList<ServerRoleList> serverRoles = account.getServerRoles();
			for ( ServerRoleList serverRole: serverRoles ) {
				ArrayList<String> userIds = serverRole.getUserIds();
				for (String userIdStr : userIds ) {
					if ( user.get_id().toString().equals(userIdStr) ) {
						user.setServerPojo(ServerListManager.getInstance().getServerById(serverRole.getServerId()));
						user.setServerId(serverRole.getServerId());
						break;
					}
				}
			}
		}
		
		LoginManager.getInstance().checkMulitDeviceLogin(user.getClient(), sessionKey, user);

		// Register user's session
		GameContext.getInstance().registerUserSession(session, user, sessionKey);
		
		//Find locale
		if ( locale != null ) {
			user.setUserLocale(locale);
			LocaleThreadLocal threadLocal = GameContext.getInstance().getLocaleThreadLocal();
			Locale userLocale = threadLocal.get();
			userLocale = GameContext.getInstance().getSessionManager().
						findUserLocale(sessionKey);
			threadLocal.set(userLocale);
		} else {
			user.setCountry(Constant.DEFAULT_LOCALE);
			user.setUserLocale(Locale.SIMPLIFIED_CHINESE);
		}
		logger.debug("User {} locale is {}", user.getRoleName(), user.getUserLocale());
		
		// 1. 玩家基本角色信息
		ScriptManager.getInstance().runScript(ScriptHook.BAG_CHECK, user);
		//update user basic prop
		UserCalculator.updateUserBasicProp(user);
		// 获取用户公会数据
		if ( StringUtil.checkNotEmpty(user.getGuildId()) ) {
			Guild guild = GuildManager.getInstance().queryGuildById(user.getGuildId());
			if ( guild == null ) {
				//The guild does not exist
				user.setGuildId(null);
				logger.warn("The guildId {} is cleared for user {}", user.getRoleName());
			} else {
				if ( GuildManager.getInstance().checkOperationFee(user, guild) ) {
					GuildMember member = GuildManager.getInstance().queryGuildMemberByUserId(user.get_id());
					user.setGuild(guild);
					user.setGuildMember(member);
					if ( member != null ) {
						if ( user.getRoleName().equals(member.getRoleName()) ) {
							member.setRoleName(user.getRoleName());
							GuildManager.getInstance().saveGuildMember(member);
						}
					} else {
						logger.warn("The user's guild member is null.");
					}
					GuildManager.getInstance().markGuildMemberOnline(user, true, false);
					/**
					 * Improve guild abilities if exist
					 */
					ScriptManager.getInstance().runScript(ScriptHook.GUILD_USER_ABILITY, user);
				} else {
					user.setGuildId(null);
				}
			}
		}

		XinqiMessage roleInfo = new XinqiMessage();
		roleInfo.payload = user.toBseRoleInfo();
		GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
		

		// 4. 玩家的任务列表（登陆时， 需要通过该协议初始化任务列表，游戏过程中，可以通过AddTask,
		// DelTask同步客户端的任务列表）
		//Check if we need to refresh daily tasks
		TaskManager taskManager = GameContext.getInstance().getTaskManager();
		TreeSet<TaskPojo> tasks = new TreeSet<TaskPojo>(taskManager.getUserLoginTasks(user));
		for ( TaskType taskType : TaskType.values() ) {
			if ( taskType != TaskType.TASK_ACHIVEMENT && 
					taskType != TaskType.TASK_RANDOM ) {
				tasks.addAll(user.getTaskFinished(taskType));
			}
		}
		
		//Set the todo task list to user's field.
		XinqiBseTaskList.BseTaskList.Builder taskListBuilder = XinqiBseTaskList.BseTaskList
				.newBuilder();
		BsePromotion.Builder promoBuilder = BsePromotion.newBuilder();
		Collection<TaskPojo> awardedTasks = taskManager.acquireAwardedTasks(user, TaskType.TASK_ACTIVITY);
		tasks.addAll(awardedTasks);
		for ( TaskPojo task : tasks ) {
			if ( task.getType() == TaskType.TASK_ACHIVEMENT || 
					task.getType() == TaskType.TASK_RANDOM ) {
				continue;
			} else if ( task.getType() == TaskType.TASK_ACTIVITY ) {
				logger.debug("Add activity task:{};{}", task.getId(), task.getName());
				promoBuilder.addPromotion(task.toPromotion(user));
			} else {
				taskListBuilder.addTaskList(taskManager.toTaskProtoInfo(user, task));
			}
		}
		XinqiMessage todoTask = new XinqiMessage();
		todoTask.payload = taskListBuilder.build();
		GameContext.getInstance().writeResponse(user.getSessionKey(), todoTask);
		
		XinqiMessage promotion = new XinqiMessage();
		promotion.payload = promoBuilder.build();
		GameContext.getInstance().writeResponse(user.getSessionKey(), promotion);
		
		// 5.检查用户邮件
		MailMessageManager.getInstance().checkMail(user);
		
		// 6.更新所有成就进度信息
		TaskManager.getInstance().sendBseUserAchievements(user);
		
		/**
		 * Check if the "script.task.UserLevelUp" is finished
		 * Because sometimes user may level up more than one levels, and
		 * User.setLevel can only sent one ModiTask a time. So when user
		 * login, we double check all the others tasks for user upgrading
		 * Call script to trigger task
		 */
		TaskManager.getInstance().processUserTasks(user, TaskHook.USER_UPGRADE);
		
		// 7. 玩家好友信息
		LoginManager.getInstance().sendFriendStatus(user, true);

		//Update user statistic
		user.setLdate(new Date());
		/**
		 * Make the change after user entering training room
		 * 2012-09-10
		 * wangqi
		 */
		//user.setTutorial(false);
		GameContext.getInstance().getUserManager().saveUser(user, false);
				
		return true;
	}
}
