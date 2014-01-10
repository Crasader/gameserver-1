package com.xinqihd.sns.gameserver.task;

public enum TaskPostStatus {

	/**
	 * The task rewards should be given to users
	 */
	SUCCESS,
	/**
	 * The task rewards should not be given to users
	 */
	FAILURE,
	/**
	 * The task rewards is already processed by script
	 */
	FINISHED,
	
}
