package com.xinqihd.sns.gameserver.guild;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildApplyList;

/**
 * The user's application for joining in a guild.
 * @author wangqi
 *
 */
public class Apply {

	/**
	 * The target's guild id
	 */
	private String guildId = null;
	
	/**
	 * The apply's guild name
	 */
	private String guildName = null;

	/**
	 * The applier's userId
	 */
	private UserId userId = null;
	
	/**
	 * The application's time
	 */
	private long applytime = 0l;
	
	/**
	 * The guild owner's processing timestamp.
	 */
	private long processtime = 0l;
	
	/**
	 * The status for this application
	 */
	private ApplyStatus status = ApplyStatus.pending;

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
	 * @return the guildName
	 */
	public String getGuildName() {
		return guildName;
	}

	/**
	 * @param guildName the guildName to set
	 */
	public void setGuildName(String guildName) {
		this.guildName = guildName;
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
	 * @return the applytime
	 */
	public long getApplytime() {
		return applytime;
	}

	/**
	 * @param applytime the applytime to set
	 */
	public void setApplytime(long applytime) {
		this.applytime = applytime;
	}

	/**
	 * @return the processtime
	 */
	public long getProcesstime() {
		return processtime;
	}

	/**
	 * @param processtime the processtime to set
	 */
	public void setProcesstime(long processtime) {
		this.processtime = processtime;
	}

	/**
	 * @return the status
	 */
	public ApplyStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(ApplyStatus status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Apply [guildId=");
		builder.append(guildId);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", applytime=");
		builder.append(applytime);
		builder.append(", processtime=");
		builder.append(processtime);
		builder.append(", status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
	
	public XinqiBseGuildApplyList.Member toMember(BasicUser user) {
		XinqiBseGuildApplyList.Member.Builder builder = XinqiBseGuildApplyList.Member.newBuilder();
		builder.setUserid(user.get_id().toString());
		builder.setRolename(user.getRoleName());
		builder.setViplevel(user.getViplevel());
		builder.setGender(user.getGender().ordinal());
		builder.setPower(user.getPower());
		builder.setLevel(user.getLevel());
		builder.setStatus(GameContext.getInstance().findSessionKeyByUserId(user.get_id())!=null);
		return builder.build();
	}
}
