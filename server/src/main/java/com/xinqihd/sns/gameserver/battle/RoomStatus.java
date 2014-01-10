package com.xinqihd.sns.gameserver.battle;

/**
 * The Room's current status.
 * @author wangqi
 *
 */
public enum RoomStatus {

	//The normal status of this room.
	UNFULL,
	//The room is waiting for new users.
//	WAITING,
	//The room is full of users however not ready to combat.
	FULL,
	//The room is ready to match another room to combat.
	READY,
	//The room is already in combat.
	COMBAT,
	//The room is timeout and need to begin combat immediatly.
	TIMEOUT,
	//The room is being deleted.
	DELETED,
	//The room is null or is managed by remote JVM.
	NOOP
}
