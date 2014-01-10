package com.xinqihd.sns.gameserver.admin.security;

import java.util.Collection;
import java.util.HashSet;

public class AdminUser {

	private String username; 
	private String password;
	private String email;
	private HashSet<PriviledgeKey> priviledgeGroup = 
			new HashSet<PriviledgeKey>();
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Add a new PriviledgeKey into user's account
	 * @param key
	 */
	public void addPriviledge(PriviledgeKey key) {
		this.priviledgeGroup.add(key);
	}
	
	/**
	 * Check if the user has given PriviledgeKey
	 * @param key
	 * @return
	 */
	public boolean hasPriviledgeKey(PriviledgeKey key) {
		return this.priviledgeGroup.contains(key);
	}
	
	/**
	 * @return the priviledgeGroup
	 */
	public HashSet<PriviledgeKey> getPriviledgeGroup() {
		return priviledgeGroup;
	}
	
	/**
	 * 
	 * @param group
	 */
	public void setPriviledgeGroup(Collection<PriviledgeKey> group) {
		this.priviledgeGroup.addAll(group);
		
	}
}
