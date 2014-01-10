package com.xinqihd.sns.gameserver.guild;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.util.CommonUtil;

public class GuildBag {

	//same as the guild id
	private String _id = null;
	
	//The max size for this guild bag
	private int max = 40;
	
	//The current number of items
	private int count = 0;
	
	/**
	 * The GuildBag must have a version code to check
	 * if the data in database are changed by other
	 * players in the same time.
	 */
	private long version = 0l;
	
	/**
	 * The integer should be a unique key on the propData
	 */
	private HashMap<String, PropData> propList = new HashMap<String, PropData>();

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
	 * @return the version
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(long version) {
		this.version = version;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the max
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @param max the max to set
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * @return the propList
	 */
	public HashMap<String, PropData> getPropList() {
		return propList;
	}

	/**
	 * @param propList the propList to set
	 */
	public void setPropList(HashMap<String, PropData> propList) {
		this.propList = propList;
	}

	/**
	 * Add a new user PropData
	 * @param propData
	 */
	public boolean addPropData(PropData propData) {
		if ( this.count >= this.max ) {
			return false;
		}
		/**
		 * Check duplicate
		 */
		for ( PropData pd : this.propList.values() ) {
			if ( pd == propData ) {
				return false;
			}
		}
		/**
		 * Generate a unique key
		 */
		int r = CommonUtil.getRandomInt(Integer.MAX_VALUE);
		String key = String.valueOf(r);
		while ( this.propList.containsKey(key) ) {
			r = CommonUtil.getRandomInt(Integer.MAX_VALUE);
			key = String.valueOf(r);
		}
		this.propList.put(key, propData);
		propData.setPew(r);
		this.count++;
		return true;
	}
	
	/**
	 * Remove the propData from bag
	 * @param index
	 * @return
	 */
	public PropData removePropData(int index) {
		PropData propData = this.propList.remove(String.valueOf(index));
		return propData;
	}
}
