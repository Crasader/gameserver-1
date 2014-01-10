package com.xinqihd.sns.gameserver.reward;

/**
 * The result type of a action
 * @author wangqi
 *
 */
public enum PickRewardResult {
	//We got the reward and update user's status
	SUCCESS,
	//The user's bag is full of stuff.
	BAG_FULL,
	//The user's level cannot pick the reward.
	LEVEL_FAIL,
	//Other errors occurred.
	OTHER,
	//We got nothing and did not update user status
	NOTHING,
	//Some box need the key to open it
	CONDITION_FAIL,
	//There is no script bound on the box
	NO_SCRIPT,
	//The item is not a box
	NOT_A_BOX,
	//The itemPojo cannot be found
	NO_ITEM,
	//The user concel the opening
	CANCEL,
}
