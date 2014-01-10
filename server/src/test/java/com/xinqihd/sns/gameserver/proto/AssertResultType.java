package com.xinqihd.sns.gameserver.proto;

/**
 * The result checking status
 * @author wangqi
 *
 */
public enum AssertResultType {
	//The protocol is ok
	SUCCESS,
	//The protocol is waiting for more response.
	CONTINUE,
	//The protocol fails.
	FAILURE,
	//The protocol has exceptions.
	EXCEPTION,
}
