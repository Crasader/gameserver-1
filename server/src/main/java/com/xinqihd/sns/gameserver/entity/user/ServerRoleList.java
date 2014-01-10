package com.xinqihd.sns.gameserver.entity.user;

import java.util.ArrayList;

/**
 * The server role's mapping
 * @author wangqi
 *
 */
public class ServerRoleList {
	
	private String serverId;
	
	private ArrayList<String> userIds = new ArrayList<String>();
	private ArrayList<String> roleNames = new ArrayList<String>();

	/**
	 * @return the serverId
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/**
	 * @return the roleNames
	 */
	public ArrayList<String> getRoleNames() {
		return roleNames;
	}

	/**
	 * @param roleNames the roleNames to set
	 */
	public void setRoleNames(ArrayList<String> roleNames) {
		this.roleNames = roleNames;
	}
	
	/**
	 * Add a new roleName
	 * @param roleName
	 */
	public void addRoleName(String roleName) {
		this.roleNames.add(roleName);
	}
	
	/**
	 * Remove the roleName
	 * @param roleName
	 */
	public void removeRoleName(String roleName) {
		this.roleNames.remove(roleName);
	}

	/**
	 * @return the userNames
	 */
	public ArrayList<String> getUserIds() {
		return userIds;
	}

	/**
	 * @param userNames the userNames to set
	 */
	public void setUserNames(ArrayList<String> userIds) {
		this.userIds = userIds;
	}
	
	/**
	 * Add a new roleName
	 * @param roleName
	 */
	public void addUserId(UserId userId) {
		this.userIds.add(userId.toString());
	}
	
	/**
	 * Remove the roleName
	 * @param roleName
	 */
	public void removeUserName(UserId userId) {
		this.userIds.remove(userId.toString());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServerRoleList [serverId=" + serverId + ", roleNames=" + roleNames
				+ "]";
	}
	
}
