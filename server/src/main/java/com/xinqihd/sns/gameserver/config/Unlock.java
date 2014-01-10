package com.xinqihd.sns.gameserver.config;

import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseFuncUnlock.FuncUnlock;

/**
 * For a new user, some of the functions in game are locked by default.
 * When the user's level and exp are growing, the function will be unlocked
 * one by one.
 * 
 * @author wangqi
 *
 */
public class Unlock {

	/**
	 * The user's userId for this unlock object.
	 */
	private UserId _id = null;
	
  /**
   * 功能索引
   * 0: 房间
   * 1: 铁匠铺
   * ...
   */
	private GameFuncType funcType = null;
	
	private int funcValue = -1;

	/**
	 * @return the _id
	 */
	public UserId getId() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void setId(UserId _id) {
		this._id = _id;
	}

	/**
	 * @return the funcType
	 */
	public GameFuncType getFuncType() {
		return funcType;
	}

	/**
	 * @param funcType the funcType to set
	 */
	public void setFuncType(GameFuncType funcType) {
		this.funcType = funcType;
	}

	/**
	 * @return the funcValue
	 */
	public int getFuncValue() {
		return funcValue;
	}

	/**
	 * @param funcValue the funcValue to set
	 */
	public void setFuncValue(int funcValue) {
		this.funcValue = funcValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Unlock [_id=%s, funcType=%s, funcValue=%s]", _id,
				funcType, funcValue);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((funcType == null) ? 0 : funcType.hashCode());
		result = prime * result + funcValue;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Unlock other = (Unlock) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (funcType != other.funcType)
			return false;
		if (funcValue != other.funcValue)
			return false;
		return true;
	}
	
	/**
	 * To google's protobuf.
	 * @return
	 */
	public FuncUnlock toFuncUnlock() {
		FuncUnlock.Builder builder = FuncUnlock.newBuilder();
		builder.setFunctype(this.funcType.ordinal());
		builder.setFuncvalue(this.funcValue);
		return builder.build();
	}
}
