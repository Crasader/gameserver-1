package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.CDKeyManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.proto.XinqiBceTaskReward.BceTaskReward;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The BceTaskRewardHandler is used for protocol TaskReward 
 * @author wangqi
 *
 */
public class BceTaskRewardHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceTaskRewardHandler.class);
	
	private static final BceTaskRewardHandler instance = new BceTaskRewardHandler();
	
	private BceTaskRewardHandler() {
		super();
	}

	public static BceTaskRewardHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceTaskReward");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceTaskReward reward = (BceTaskReward)request.payload;
		String taskId = String.valueOf(reward.getTaskID());
		String cdkey = reward.getCdkey();
		int choose = reward.getChoose();
		
		//Take the reward
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		TaskPojo task = TaskManager.getInstance().getTaskById(taskId);
		if ( task != null && task.isInputCode() ) {
			CDKeyManager.getInstance().takeCDKeyReward(user, cdkey);
		} else {
			TaskManager manager = GameContext.getInstance().getTaskManager();
			boolean success = manager.takeTaskReward(user, taskId, choose);
			
			if ( StatClient.getIntance().isStatEnabled() ) {
				task = TaskManager.getInstance().getTaskById(taskId);
				String taskName = Constant.EMPTY;
				if ( task != null ) {
					taskName = task.getName();
				}
				StatClient.getIntance().sendDataToStatServer(user, 
						StatAction.TaskReward, taskId, taskName, success);
				UserActionManager.getInstance().addUserAction(user.getRoleName(), 
						UserActionKey.TaskReward);
			}
		}
	}
	
}
