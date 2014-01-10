package com.xinqihd.sns.gameserver.config;

import java.util.Collection;
import java.util.HashSet;

import com.xinqihd.sns.gameserver.reward.Reward;

/**
 * The reward config pojo 
 * 
 * @author wangqi
 *
 */
public class RewardPojo implements Comparable<RewardPojo> {
	
	/**
	 * The reward id
	 */
	private String _id = null;
	
	/**
	 * The name desc for this rewardpojo
	 */
	private String name = null;

	/**
	 * The given config type.
	 */
	private HashSet<RewardPojoType> includes = new HashSet<RewardPojoType>();

	/**
	 * The reward config valid begin date.
	 */
	private long startMillis = 0l;

	/**
	 * The reward config valid end date.
	 */
	private long endMillis = 0l;

	/**
	 * The happen ratio between 0 - 1000, 
	 * means 0% ~ 100%
	 */
	private int ratio = 0;
	
	/**
	 * The given reward that an user 
	 * can get.
	 */
	private String rewardStr = null;
	
	/**
	 * The Reward is only for given servers.
	 */
	private HashSet<String> serverIds = null;
	
	/**
	 * The tmp reward object.
	 */
	private transient Reward reward = null;

	/**
	 * @return the _id
	 */
	public String get_id() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void set_id(String _id) {
		this._id = _id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the includes
	 */
	public HashSet<RewardPojoType> getIncludes() {
		return includes;
	}

	/**
	 * @param includes the includes to set
	 */
	public void setIncludes(Collection<RewardPojoType> includes) {
		this.includes.addAll(includes);
	}

	/**
	 * @param includes the includes to set
	 */
	public void addInclude(RewardPojoType type) {
		this.includes.add(type);
	}
	
	/**
	 * Remove the given types
	 * @param type
	 */
	public void removeInclude(RewardPojoType type) {
		this.includes.remove(type);
	}

	/**
	 * @return the startMillis
	 */
	public long getStartMillis() {
		return startMillis;
	}

	/**
	 * @param startMillis the startMillis to set
	 */
	public void setStartMillis(long startMillis) {
		this.startMillis = startMillis;
	}

	/**
	 * @return the endMillis
	 */
	public long getEndMillis() {
		return endMillis;
	}

	/**
	 * @param endMillis the endMillis to set
	 */
	public void setEndMillis(long endMillis) {
		this.endMillis = endMillis;
	}

	/**
	 * @param ratio the ratio to set
	 */
	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	/**
	 * @return the ratio
	 */
	public int getRatio() {
		return ratio;
	}

	/**
	 * @return the reward
	 */
	public Reward getReward() {
		if ( reward == null ) {
			if ( rewardStr != null ) {
				reward = Reward.fromString(rewardStr); 
			}
		}
		return reward;
	}

	/**
	 * @param reward the reward to set
	 */
	public void setReward(Reward reward) {
		if ( reward != null ) {
			this.rewardStr = reward.toString();
		}
	}

	/**
	 * @return the serverIds
	 */
	public HashSet<String> getServerIds() {
		return serverIds;
	}

	/**
	 * @param serverIds the serverIds to set
	 */
	public void setServerIds(Collection<String> serverIds) {
		if ( this.serverIds == null ) {
			this.serverIds = new HashSet<String>(); 
		}
		this.serverIds.addAll(serverIds);
	}

	/**
	 * Check if the RewardPojo is valid for given server
	 * @param serverId
	 * @return
	 */
	public boolean containServerId(String serverId) {
		if ( this.serverIds != null ) {
			return this.serverIds.contains(serverId);
		} else {
			//Default to true
			return true;
		}
	}
	
	/**
	 * Compare two reward pojo object
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(RewardPojo o) {
		if ( o == null ) {
			return -1;
		}
		int ratioDiff = this.ratio - o.ratio;
		if ( ratioDiff != 0 ) {
			return ratioDiff;
		}
		long startDiff = this.startMillis - o.startMillis;
		if ( startDiff != 0 ) {
			return (int)startDiff;
		}
		long endDiff = this.endMillis - o.endMillis;
		if ( endDiff != 0 ) {
			return (int)endDiff;
		}
		return _id.compareTo(o._id);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
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
		RewardPojo other = (RewardPojo) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RewardPojo [_id=" + _id + ", name=" + name + ", includes="
				+ includes + ", startMillis=" + startMillis + ", endMillis="
				+ endMillis + ", ratio=" + ratio + ", rewardStr=" + rewardStr
				+ ", serverIds=" + serverIds + "]";
	}
	
}
