package com.xinqihd.sns.gameserver.entity.user;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.equip.Gender;

/**
 * The game's global account in database.
 * An account can have multi roles in different game servers.
 * 
 * @author wangqi
 *
 */
public class Account {

	/**
	 * 为服务器标号加10位随机字符的形式，例如
	 * s0001.asfwq3423d
	 */
	private String _id = null;
	
	/**
	 * 为10位随机字符，与用户名生成规则相同
	 */
	private String userName = null;
	
	private String password = "def";
	
	private Gender gender = Gender.MALE;
	
	private String email = null;
	
	private String channel = null;
	
	private String uuid = null;
	
	private String device = null;
	
	private long regMillis;
	
	/**
	 * When the first time this account 
	 * is created, the newAccount is true.
	 */
	private boolean newAccount;
	
	/**
	 * Indicate whether the email is verified.
	 */
	private boolean emailVerified;
	
	/**
	 * True means it is a guest account
	 */
	private boolean guest;
	
	/**
	 * The last server id that user logins.
	 */
	private String lastServerId;
	
	/**
	 * The last login rolename
	 */
	private UserId lastUserId;
	
	/**
	 * The account status
	 */
	private String status = null;
	
	/**
	 * The client's version
	 */
	private String version = null;
	
	/**
	 * The serverId to roles mapping
	 */
	private ArrayList<ServerRoleList> serverRoles = 
			new ArrayList<ServerRoleList>();

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
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
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
	 * @return the gender
	 */
	public Gender getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(Gender gender) {
		this.gender = gender;
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
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * @return the serverRoles
	 */
	public ArrayList<ServerRoleList> getServerRoles() {
		return serverRoles;
	}

	/**
	 * @param serverRoles the serverRoles to set
	 */
	public void setServerRoles(ArrayList<ServerRoleList> serverRoles) {
		this.serverRoles = serverRoles;
	} 
	
	/**
	 * Add a new server role to list
	 * @param serverRole
	 */
	public void addServerRole(ServerRoleList serverRole) {
		this.serverRoles.add(serverRole);
	}
	
	/**
	 * Remove the old server role from list.
	 * @param serverRole
	 */
	public void removeServerRole(ServerRoleList serverRole) {
		this.serverRoles.remove(serverRole);
	}

	/**
	 * @return the regMillis
	 */
	public long getRegMillis() {
		return regMillis;
	}

	/**
	 * @param regMillis the regMillis to set
	 */
	public void setRegMillis(long regMillis) {
		this.regMillis = regMillis;
	}

	/**
	 * @return the newAccount
	 */
	public boolean isNewAccount() {
		return newAccount;
	}

	/**
	 * @param newAccount the newAccount to set
	 */
	public void setNewAccount(boolean newAccount) {
		this.newAccount = newAccount;
	}

	/**
	 * @return the emailVerified
	 */
	public boolean isEmailVerified() {
		return emailVerified;
	}

	/**
	 * @param emailVerified the emailVerified to set
	 */
	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the guest
	 */
	public boolean isGuest() {
		return guest;
	}

	/**
	 * @param guest the guest to set
	 */
	public void setGuest(boolean guest) {
		this.guest = guest;
	}

	/**
	 * @return the lastServerId
	 */
	public String getLastServerId() {
		return lastServerId;
	}

	/**
	 * @param lastServerId the lastServerId to set
	 */
	public void setLastServerId(String lastServerId) {
		this.lastServerId = lastServerId;
	}

	/**
	 * @return the lastUserId
	 */
	public UserId getLastUserId() {
		return lastUserId;
	}

	/**
	 * @param lastUserId the lastUserId to set
	 */
	public void setLastUserId(UserId lastUserId) {
		this.lastUserId = lastUserId;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the device
	 */
	public String getDevice() {
		return device;
	}

	/**
	 * @param device the device to set
	 */
	public void setDevice(String device) {
		this.device = device;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Account [_id=" + _id + ", userName=" + userName + ", password="
				+ password + ", gender=" + gender + ", email=" + email + ", channel="
				+ channel + ", regMillis=" + regMillis + ", newAccount=" + newAccount
				+ ", emailVerified=" + emailVerified + ", guest=" + guest
				+ ", lastServerId=" + lastServerId + ", lastUserId=" + lastUserId
				+ ", serverRoles=" + serverRoles + "]";
	}
	
}
