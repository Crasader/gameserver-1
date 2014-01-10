package com.xinqihd.sns.gameserver.config;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.ServerRoleList;
import com.xinqihd.sns.gameserver.proto.XinqiBseServerList;

/**
 * The gameserver's config pojo
 * @author wangqi
 *
 */
public class ServerPojo implements Comparable<ServerPojo> {

	private String _id;
	
	private String host;
	
	private int port;
	
	private String name;
	
	private boolean isNew;
	
	private boolean isHot;
	
	private long startMillis;
	
	private String channel;
	
	private boolean isRegistable = true;
	
	/**
	 * 如果此字段不为null，则该服务组下
	 * 的所有玩家将合并到指定的服务组中
	 */
	private String mergeId = null;

	/**
	 * 进入服务器需要输入的密码
	 */
	private String passKey = null;
	
	/**
	 * Make sure the version pattern can see 
	 * this version
	 */
	private String version = null;
	
	/**
	 * The update url for this version.
	 */
	private String versionUrl = null;

	/**
	 * @return the _id
	 */
	public String getId() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void setId(String _id) {
		this._id = _id;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
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
	 * @return the isNew
	 */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * @param isNew the isNew to set
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	/**
	 * @return the isHot
	 */
	public boolean isHot() {
		return isHot;
	}

	/**
	 * @param isHot the isHot to set
	 */
	public void setHot(boolean isHot) {
		this.isHot = isHot;
	}

	/**
	 * @return the isRegistable
	 */
	public boolean isRegistable() {
		return isRegistable;
	}

	/**
	 * @param isRegistable the isRegistable to set
	 */
	public void setRegistable(boolean isRegistable) {
		this.isRegistable = isRegistable;
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
	 * @return the mergeId
	 */
	public String getMergeId() {
		return mergeId;
	}

	/**
	 * @param mergeId the mergeId to set
	 */
	public void setMergeId(String mergeId) {
		this.mergeId = mergeId;
	}

	/**
	 * @return the passKey
	 */
	public String getPassKey() {
		return passKey;
	}

	/**
	 * @param passKey the passKey to set
	 */
	public void setPassKey(String passKey) {
		this.passKey = passKey;
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
	 * @return the versionUrl
	 */
	public String getVersionUrl() {
		return versionUrl;
	}

	/**
	 * @param versionUrl the versionUrl to set
	 */
	public void setVersionUrl(String versionUrl) {
		this.versionUrl = versionUrl;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServerPojo [_id=" + _id + ", host=" + host + ", port=" + port
				+ ", name=" + name + ", isNew=" + isNew + ", isHot=" + isHot
				+ ", startMillis=" + startMillis + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + (isHot ? 1231 : 1237);
		result = prime * result + (isNew ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + port;
		result = prime * result + (int) (startMillis ^ (startMillis >>> 32));
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
		ServerPojo other = (ServerPojo) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (isHot != other.isHot)
			return false;
		if (isNew != other.isNew)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (port != other.port)
			return false;
		if (startMillis != other.startMillis)
			return false;
		return true;
	}

	@Override
	public int compareTo(ServerPojo o) {
		if ( o == null ) return -1;
		int diff = (int)(o.startMillis - this.startMillis);
		if ( diff != 0 ) {
			return diff;
		} else {
			return o._id.compareTo(_id);
		}
	}
	
	/**
	 * Convert this object to xinqi serverpojo
	 * @return
	 */
	public XinqiBseServerList.Server toXinqiServer(Account account) {
		XinqiBseServerList.Server.Builder builder = XinqiBseServerList.Server.newBuilder();
		builder.setServerid(_id);
		builder.setHost(host);
		builder.setPort(port);
		builder.setName(name);
		builder.setIshot(isHot);
		builder.setIsnew(isNew);
		builder.setStartSecond((int)(this.startMillis/1000));
		if ( account != null ) {
			ArrayList<ServerRoleList> serverRoleLists = account.getServerRoles();
			for ( int i=serverRoleLists.size()-1; i>=0; i-- ) {
				ServerRoleList serverRole = serverRoleLists.get(i);
				if ( serverRole.getServerId().equals(this._id) ) {
					builder.setRolenum(serverRole.getRoleNames().size());
					break;
				}
			}
		}
		return builder.build();
	}
}
