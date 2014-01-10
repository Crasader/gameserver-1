package com.xinqihd.sns.gameserver.entity.user;

import java.util.HashMap;

import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.BiblioManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseGetUserBiblio;

/**
 * The user's biblio map
 * 
 * @author wangqi
 *
 */
public class UserBiblio {

	private UserId _id = null;
	
	private String roleName = null;
	
	private boolean takenReward = false;
	
	/**
	 * The weaponTypeName : weaponId map
	 */
	private HashMap<String, String> biblio = 
			new HashMap<String, String>();

	/**
	 * @return the userId
	 */
	public UserId getId() {
		return _id;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setId(UserId userId) {
		this._id = userId;
	}

	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * @param roleName the roleName to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * @return the biblio
	 */
	public HashMap<String, String> getBiblio() {
		return biblio;
	}

	/**
	 * @param biblio the biblio to set
	 */
	public void setBiblio(HashMap<String, String> biblio) {
		this.biblio = biblio;
	}
	
	/**
	 * Get the weapon id for given type
	 * @param index
	 * @return
	 */
	public String getWeaponId(String weaponType) {
		return this.biblio.get(weaponType);
	}
	
	/**
	 * @return the takenReward
	 */
	public boolean isTakenReward() {
		return takenReward;
	}

	/**
	 * @param takenReward the takenReward to set
	 */
	public void setTakenReward(boolean takenReward) {
		this.takenReward = takenReward;
	}

	/**
	 * Add new entry for biblio
	 * @param weaponType
	 * @param weaponId
	 */
	public void addBiblio(String weaponType, String weaponId) {
		this.biblio.put(weaponType, weaponId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((biblio == null) ? 0 : biblio.hashCode());
		result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
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
		UserBiblio other = (UserBiblio) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (biblio == null) {
			if (other.biblio != null)
				return false;
		} else if (!biblio.equals(other.biblio))
			return false;
		if (roleName == null) {
			if (other.roleName != null)
				return false;
		} else if (!roleName.equals(other.roleName))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserBiblio [_id=" + _id + ", roleName=" + roleName
				+ ", takenReward=" + takenReward + ", biblio=" + biblio + "]";
	}
	
}
