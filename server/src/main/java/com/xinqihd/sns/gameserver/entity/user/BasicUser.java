package com.xinqihd.sns.gameserver.entity.user;

import static com.xinqihd.sns.gameserver.config.Constant.*;

import java.io.Serializable;

import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.proto.XinqiGuildMember.GuildMember;

/**
 * It only contains basic user information. It's primary usage is 
 * for displaying information on friends' list or in combat room.
 * 
 * @author wangqi
 *
 */
public class BasicUser implements Serializable {

	private static final long serialVersionUID = -8384603805575562363L;

	//Our customized userid (shardkey)
	private UserId _id = null;
	// The device uuid
	private String uuid = null;
	// 用户名
	private String username = EMPTY;
	// 国家
	private String country = EMPTY;
  // 用户别名
	private String roleName = EMPTY;
	// 性别 1: female, 2: male
	private Gender gender = Gender.FEMALE;
	// 用户上传的头像路径
	private String iconurl = null;
	// The guildId
	private String guildId = null;
  // 玩家等级
	private int level = 1;
	private int power = 0;
	
  //  是否为vip玩家, true/false
	private boolean isvip = false;
  //  vip的级别
	private int     viplevel = 0;
	//玩家胜场
	private int wins = 0;
	//失败次数
	private int fails = 0;
	
	//--------------------------------- Properties method
	
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
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
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
	 * @return the iconurl
	 */
	public String getIconurl() {
		return iconurl;
	}
	/**
	 * @param iconurl the iconurl to set
	 */
	public void setIconurl(String iconurl) {
		this.iconurl = iconurl;
	}
	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}
	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}
	/**
	 * @return the isvip
	 */
	public boolean isVip() {
		return isvip;
	}
	/**
	 * @param isvip the isvip to set
	 */
	public void setIsvip(boolean isvip) {
		this.isvip = isvip;
	}
	/**
	 * @return the viplevel
	 */
	public int getViplevel() {
		return viplevel;
	}
	/**
	 * @param viplevel the viplevel to set
	 */
	public void setViplevel(int viplevel) {
		this.viplevel = viplevel;
	}
	
	/**
	 * @return the power
	 */
	public int getPower() {
		return power;
	}
	/**
	 * @param power the power to set
	 */
	public void setPower(int power) {
		this.power = power;
	}
	/**
	 * @return the wins
	 */
	public int getWins() {
		return wins;
	}
	/**
	 * @param wins the wins to set
	 */
	public void setWins(int wins) {
		this.wins = wins;
	}
	/**
	 * @return the fails
	 */
	public int getFails() {
		return fails;
	}
	/**
	 * @param fails the fails to set
	 */
	public void setFails(int fails) {
		this.fails = fails;
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
	 * @return the guildId
	 */
	public String getGuildId() {
		return guildId;
	}
	/**
	 * @param guildId the guildId to set
	 */
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	/**
	 * Convert this basic user object to guild member.
	 * @return
	 */
	public GuildMember toGuildMember() {
		GuildMember.Builder builder = GuildMember.newBuilder();
		builder.setUserid(_id.toString());
		builder.setName(roleName);
		builder.setLevel(level);
		builder.setGender(gender.ordinal());
		builder.setPower(power);
		return builder.build();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + fails;
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result + ((iconurl == null) ? 0 : iconurl.hashCode());
		result = prime * result + (isvip ? 1231 : 1237);
		result = prime * result + level;
		result = prime * result + power;
		result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + viplevel;
		result = prime * result + wins;
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
		BasicUser other = (BasicUser) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (fails != other.fails)
			return false;
		if (gender != other.gender)
			return false;
		if (iconurl == null) {
			if (other.iconurl != null)
				return false;
		} else if (!iconurl.equals(other.iconurl))
			return false;
		if (isvip != other.isvip)
			return false;
		if (level != other.level)
			return false;
		if (power != other.power)
			return false;
		if (roleName == null) {
			if (other.roleName != null)
				return false;
		} else if (!roleName.equals(other.roleName))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (viplevel != other.viplevel)
			return false;
		if (wins != other.wins)
			return false;
		return true;
	}
	
}
