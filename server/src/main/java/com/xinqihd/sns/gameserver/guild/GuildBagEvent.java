package com.xinqihd.sns.gameserver.guild;

import java.util.Date;

import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildBagEvent;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * When an user uses the guild storage, the PUT and TAKE actions are recorded as events.
 * @author wangqi
 *
 */
public class GuildBagEvent {
	
	//It is the random id
	private String _id = null;
	
	private String guildId = null;
	
	private UserId userId = null;
	
	private String roleName = null;
	
	private long timestamp = 0l;
	
	private GuildBagEventType type = null;
	
	private String event = null;

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
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the type
	 */
	public GuildBagEventType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(GuildBagEventType type) {
		this.type = type;
	}

	/**
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(String event) {
		this.event = event;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GuildBagEvent [_id=");
		builder.append(_id);
		builder.append(", guildId=");
		builder.append(guildId);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", roleName=");
		builder.append(roleName);
		builder.append(", timestamp=");
		builder.append(new Date(timestamp));
		builder.append(", type=");
		builder.append(type);
		builder.append(", event=");
		builder.append(event);
		builder.append("]");
		return builder.toString();
	}
	
	public XinqiBseGuildBagEvent.GuildBagEvent toGuildBagEvent() {
		XinqiBseGuildBagEvent.GuildBagEvent.Builder builder = 
				XinqiBseGuildBagEvent.GuildBagEvent.newBuilder();
		builder.setTime(DateUtil.formatDateTime(new Date(this.timestamp)));
		builder.setRolename(roleName);
		builder.setAction(Text.text(type.toString()));
		builder.setItem(event);
		return builder.build();
	}
}
