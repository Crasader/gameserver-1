package com.xinqihd.sns.gameserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.Field;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseFinishAchievement.BseFinishAchievement;
import com.xinqihd.sns.gameserver.proto.XinqiBseModiTask.BseModiTask;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * This class abstract the 'step' related processing and let
 * task script class process task specific logic.
 * 
 * @author wangqi
 *
 */
public class TaskStep {
	
	private static final Logger logger = LoggerFactory.getLogger(TaskStep.class);
	
	/**
	 * The Task script should call this method if the user's action
	 * is compatible with task's target. This method will check if
	 * current step reaches the limit and do the rest thing.
	 * 
	 * @param task
	 * @param user
	 * @return
	 */
	public static boolean step(TaskPojo task, User user) {
		return step(task, user, 1);
	}

	/**
	 * The Task script should call this method if the user's action
	 * is compatible with task's target. This method will check if
	 * current step reaches the limit and do the rest thing.
	 * 
	 * @param task
	 * @param user
	 * @return
	 */
	public static boolean step(TaskPojo task, User user, int stepUnit) {
		return step(task, user, stepUnit, false);
	}
	
	/**
	 * The Task script should call this method if the user's action
	 * is compatible with task's target. This method will check if
	 * current step reaches the limit and do the rest thing.
	 * 'absolute' means if the stepUnit is a absolute value rather than a relative value.
	 * 
	 * @param task
	 * @param user
	 * @param stepUnit
	 * @param absolue
	 * @return
	 */
	public static boolean step(TaskPojo task, User user, int stepUnit, boolean absolute) {
		boolean result = false;
		String taskId = task.getId();
		TaskManager manager = TaskManager.getInstance();
		
		int totalStep = task.getStep();
		int currentStep = stepUnit;
		if ( !absolute ) {
			String currentStepString = manager.queryTaskSpecificData(user, taskId, Field.STEP);
			if ( currentStepString != null ) {
				currentStep = StringUtil.toInt(currentStepString, 1) + stepUnit;
			}
		}
		if ( currentStep < totalStep ) {
			manager.storeTaskSpecificData(user, taskId, Field.STEP, currentStep);
		} else {
			manager.finishTask(user, task.getId());
			result = true;
		}
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("User {} task: id:{},name:{} step: {}, total:{}", 
				new Object[]{user.getRoleName(), task.getId(), task.getName(), currentStep, totalStep});
		}
		
		sendNotifyToClient(user, task, currentStep, totalStep);
		
		return result;
	}
	
	/**
	 * For the rank type achievement, the step value is reversed. Rank 1 is 
	 * the highest number to reach.
	 * 
	 * @param task
	 * @param user
	 * @param stepUnit
	 * @return
	 */
	public static boolean reverseStep(TaskPojo task, User user, int stepUnit) {
		boolean result = false;
		String taskId = task.getId();
		TaskManager manager = TaskManager.getInstance();
		
		int totalStep = task.getStep();
		int currentStep = stepUnit;

		if ( currentStep > totalStep ) {
			/**
			 * Check if the task step changes.
			 * 2012-08-07 
			 */
			String currentStepString = manager.queryTaskSpecificData(user, taskId, Field.STEP);
			if ( !String.valueOf(currentStep).equals(currentStepString) ) {
				manager.storeTaskSpecificData(user, taskId, Field.STEP, currentStep);
				sendNotifyToClient(user, task, currentStep, totalStep);
			}
		} else {
			manager.finishTask(user, task.getId());
			result = true;
			sendNotifyToClient(user, task, totalStep, totalStep);
		}
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("User {} reverse task: id:{},name:{} step: {}, total:{}", 
				new Object[]{user.getRoleName(), task.getId(), task.getName(), currentStep, totalStep});
		}
		
		return result;
	}
	
	/**
	 * Check if the user reach current level. It is a pure utility method.
	 * and does not call finishTask method.
	 * 
	 * @param task
	 * @param user
	 * @param currentLevel
	 * @return
	 */
	public static boolean level(TaskPojo task, User user, int currentLevel ) {
		
		int targetLevel = task.getCondition1();
		
		if ( currentLevel == targetLevel ) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Send the task or achievement notification to clients.
	 * @param task
	 * @param currentStep
	 * @param totalStep
	 * @param reverse
	 */
	public static void sendNotifyToClient(User user, TaskPojo task, int currentStep, 
			int totalStep ) {
		if ( task.getType() == TaskType.TASK_ACHIVEMENT ) {
			//notify the achievement progress
			BseFinishAchievement.Builder achievement = BseFinishAchievement.newBuilder();
			achievement.setId(task.getId());
			//int percent = Math.round(currentStep * 1.0f / totalStep * 100);
			//if ( percent > 100 ) percent = 100;
			//achievement.setPercent(percent);
			achievement.setPercent(currentStep);
			GameContext.getInstance().writeResponse(user.getSessionKey(), achievement.build());
			
		} else {
			BseModiTask.Builder modiBuilder = BseModiTask.newBuilder();
			modiBuilder.setTaskID(StringUtil.toInt(task.getId(), 0));
			modiBuilder.setStep(currentStep);
			XinqiMessage xinqiMsg = new XinqiMessage();
			xinqiMsg.payload = modiBuilder.build();
			
			GameContext.getInstance().writeResponse(user.getSessionKey(), xinqiMsg);
		}
	}
}
