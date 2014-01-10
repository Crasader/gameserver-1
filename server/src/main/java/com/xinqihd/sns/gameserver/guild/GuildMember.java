package com.xinqihd.sns.gameserver.guild;

import java.util.HashMap;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiGuildMember;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The user specific data for his/her guild
 * 
 * @author wangqi
 *
 */
public class GuildMember implements Comparable<GuildMember> {
	
	//It is the guildId:userIdStr
	private String _id = null;

	//The is the same as guild id
	private String guildId = null;
	
	private UserId userId = null;
	
	//The user's roleName for searching.
	private String roleName = null;
	
	private GuildRole role = null;
	
	//个人贡献度,用来升级个人技能
	private int credit = 0;
	//总贡献度
	private int totalCredit = 0;
	//做任务赢得的功勋，可以在公会商城购买物品
	private int medal = 0;
	
	private transient boolean online = false;
	
	/**
	 * 公会设施
	 */
	private HashMap<GuildFacilityType, GuildFacility> 
		facilities = new HashMap<GuildFacilityType, GuildFacility>(); 
	
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
	 * @return the userId
	 */
	public UserId getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(UserId userId) {
		this.userId = userId;
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
	 * @return the role
	 */
	public GuildRole getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(GuildRole role) {
		this.role = role;
	}

	/**
	 * @return the credit
	 */
	public int getCredit() {
		return credit;
	}

	/**
	 * @param credit the credit to set
	 */
	public void setCredit(int credit) {
		this.credit = credit;
	}

	/**
	 * @return the totalCredit
	 */
	public int getTotalCredit() {
		return totalCredit;
	}

	/**
	 * @param totalCredit the totalCredit to set
	 */
	public void setTotalCredit(int totalCredit) {
		this.totalCredit = totalCredit;
	}

	/**
	 * @return the online
	 */
	public boolean isOnline() {
		return online;
	}

	/**
	 * @param online the online to set
	 */
	public void setOnline(boolean online) {
		this.online = online;
	}

	/**
	 * @return the medal
	 */
	public int getMedal() {
		return medal;
	}

	/**
	 * @param medal the medal to set
	 */
	public void setMedal(int medal) {
		this.medal = medal;
	}
	
	/**
	 * @return the facilities
	 */
	public HashMap<GuildFacilityType, GuildFacility> getFacilities() {
		return facilities;
	}

	/**
	 * @param facilities the facilities to set
	 */
	public void setFacilities(HashMap<GuildFacilityType, GuildFacility> facilities) {
		this.facilities = facilities;
	}
	
	/**
	 * Get given facility
	 * @param type
	 * @return
	 */
	public GuildFacility getFacility(GuildFacilityType type) {
		return this.facilities.get(type);
	}
	
	/**
	 * Add a new facility to guild.
	 * @param facility
	 */
	public void addFacility(GuildFacility facility) {
		this.facilities.put(facility.getType(), facility);
	}

	/**
	 * Convert it to protobuf's guild member
	 * @return
	 */
	public XinqiGuildMember.GuildMember toGuildMember(BasicUser user) {
		XinqiGuildMember.GuildMember.Builder builder = XinqiGuildMember.GuildMember.newBuilder();
		if ( role != GuildRole.chief ) {
			builder.setName(user.getRoleName());
		} else {
			builder.setName(
					StringUtil.concat("#ff0000", user.getRoleName(), "#ff0000"));
		}
		builder.setUserid(user.get_id().toString());
		builder.setLevel(user.getLevel());
		builder.setViplevel(user.getViplevel());
		builder.setGender(user.getGender().ordinal());
		builder.setPower(user.getPower());
		builder.setRolekey(role.toString());
		if ( role != GuildRole.chief ) {
			builder.setRoledesc(role.getTitle());
		} else {
			builder.setRoledesc(
					StringUtil.concat("#ff0000", role.getTitle(), "#ff0000"));
		}
		builder.setCredit(credit);
		builder.setTotalcredit(totalCredit);
		/**
		 * It cost a lost
		 * 2013-3-14 
		 */
		/*
		if ( GameContext.getInstance().findSessionKeyByUserId(user.get_id()) != null ) {
			builder.setIsonline(true);
		} else {
			builder.setIsonline(false);
		}
		*/
		builder.setIsonline(online);
		return builder.build();
	}

	@Override
	public int compareTo(GuildMember o) {
		if ( o == null ) {
			return -1;
		} else {
			int diff = this.role.ordinal() - o.role.ordinal();
			if ( diff != 0 ) {
				return diff;
			} else {
				if ( this.online && !o.online ) {
					return -1;
				} else if ( !this.online && o.online ) {
					return 1;
				} else {
					diff = this.totalCredit - o.totalCredit;
					if ( diff != 0 ) {
						return diff;
					} else {
						return this._id.compareTo(o._id);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + credit;
		result = prime * result
				+ ((facilities == null) ? 0 : facilities.hashCode());
		result = prime * result + ((guildId == null) ? 0 : guildId.hashCode());
		result = prime * result + medal;
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
		result = prime * result + totalCredit;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		GuildMember other = (GuildMember) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (credit != other.credit)
			return false;
		if (facilities == null) {
			if (other.facilities != null)
				return false;
		} else if (!facilities.equals(other.facilities))
			return false;
		if (guildId == null) {
			if (other.guildId != null)
				return false;
		} else if (!guildId.equals(other.guildId))
			return false;
		if (medal != other.medal)
			return false;
		if (role != other.role)
			return false;
		if (roleName == null) {
			if (other.roleName != null)
				return false;
		} else if (!roleName.equals(other.roleName))
			return false;
		if (totalCredit != other.totalCredit)
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	
}
