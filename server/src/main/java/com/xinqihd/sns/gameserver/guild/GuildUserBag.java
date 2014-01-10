package com.xinqihd.sns.gameserver.guild;

import java.util.ArrayList;
import java.util.HashMap;

import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.UserId;

/**
 * The guild bag object
 * 
 * @author wangqi
 *
 */
public class GuildUserBag {

	//The userId as the primary key
	private UserId _id = null;
	
	private ArrayList<PropData> propList = new ArrayList<PropData>();
	
	private int max = 8;
	
	/**
	 * @return the _id
	 */
	public UserId get_id() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void set_id(UserId _id) {
		this._id = _id;
	}

	/**
	 * @return the propList
	 */
	public ArrayList<PropData> getPropList() {
		return propList;
	}

	/**
	 * @param propList the propList to set
	 */
	public void setPropList(ArrayList<PropData> propList) {
		this.propList = propList;
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
	 * Add a new user PropData
	 * @param propData
	 */
	public boolean addPropData(PropData propData) {
		if ( this.propList.size() >= this.max ) {
			return false;
		}
		this.propList.add(propData);
		return true;
	}
	
	/**
	 * Set the propData at the given index. If the index already
	 * has a propData, then do nothing.
	 * 
	 * @param index
	 * @param propData
	 * @return
	 */
	public boolean setPropData(int index, PropData propData) {
		if ( index < max ) {
			if ( index >= this.propList.size() ) {
				for ( int i=this.propList.size(); i<=index; i++ ) {
					this.propList.add(null);
				}
			}
			this.propList.set(index, propData);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Remove the propData from bag
	 * @param index
	 * @return
	 */
	public boolean removePropData(int index) {
		if ( index < this.propList.size() ) {
			this.propList.set(index, null);
			return true;
		}
		return false;
	}
}
