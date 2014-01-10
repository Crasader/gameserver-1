package com.xinqihd.sns.gameserver.config;

/**
 * Different task types.
 * @author wangqi
 *
 */
public enum TaskType {
	/**
	 * Main thread tasks
	 */
	TASK_MAIN,
	/**
	 * Sub thread tasks
	 */
	TASK_SUB,
	/**
	 * Daily tasks
	 */
	TASK_DAILY,
	/**
	 * Activity tasks.
	 */
	TASK_ACTIVITY,
	/**
	 * The hidden achievement tasks
	 */
	TASK_ACHIVEMENT,
	/**
	 * The random achievement task.
	 * It is like a online reward.
	 */
	TASK_RANDOM,
}
