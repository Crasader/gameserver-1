package com.xinqihd.sns.gameserver.guild;

import java.util.Collection;
import java.util.HashMap;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiGuildInfo.GuildInfo;
import com.xinqihd.sns.gameserver.proto.XinqiGuildSimpleInfo.GuildSimpleInfo;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;

/**
 * The Guild object stores all required info about guild
 * @author wangqi
 *
 */
public final class Guild implements Comparable<Guild> {
	
	/**
	 * The guild's id, same as title.
	 */
	private String _id = null;

	/**
	 * The guild name, max 5 char
	 */
	private String title = null;
	
	/**
	 * The guild's announcement. max 128 char
	 */
	private String announce = Constant.EMPTY;
	
	/**
	 * The guild's declaration. max 128 char
	 */
	private String declaration = Constant.EMPTY;
	
	/**
	 * The guild's owner's userId
	 */
	private UserId userId = null;
	
	/**
	 * The guild's owner's roleName
	 */
	private String roleName = null;
	
	/**
	 * The guild's level
	 */
	private int level = 1;
	
	/**
	 * The guild's member count
	 */
	private int count = 1;
	
	/**
	 * The guild's max member count
	 */
	private int maxCount = 1;
		
	/**
	 * The guild's current wealth
	 */
	private int wealth = 0;
	
	/**
	 * The guild's credit
	 */
	//private int credit = 0;
	
	/**
	 * The guild's operation fee per week.
	 */
	private int opfee = 0;
	
	/**
	 * The last time to pay the operation fee.
	 */
	private long lastchargetime = 0l;
	
	/**
	 * The battle count between this guild and other guild
	 */
	private int totalbattle = 0;
	
	/**
	 * 获胜的场数
	 */
	private int winbattle = 0;
	
	/**
	 * 公会当前的状态
	 */
	private GuildStatus status = GuildStatus.normal;
	
	/**
	 * 保存公会的排名信息
	 */
	private int rank = 0;
	
	//升级开始的时间戳
	private long upgradeBeginTime = 0l;
	
	//升级结束的时间戳，增加了cooldown的秒数
	private long upgradeEndTime = 0l;
	
	//会长就职的时间，3天后方可转让
	private long ownerJoinMillis = 0l;
	
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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the announce
	 */
	public String getAnnounce() {
		return announce;
	}

	/**
	 * @param announce the announce to set
	 */
	public void setAnnounce(String announce) {
		this.announce = announce;
	}

	/**
	 * @return the declaration
	 */
	public String getDeclaration() {
		return declaration;
	}

	/**
	 * @param declaration the declaration to set
	 */
	public void setDeclaration(String declaration) {
		this.declaration = declaration;
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
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the maxCount
	 */
	public int getMaxCount() {
		return maxCount;
	}

	/**
	 * @param maxCount the maxCount to set
	 */
	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	/**
	 * @return the wealth
	 */
	public int getWealth() {
		return wealth;
	}

	/**
	 * @param wealth the wealth to set
	 */
	public void setWealth(int wealth) {
		this.wealth = wealth;
	}

	/**
	 * @return the credit
	 */
//	public int getCredit() {
//		return credit;
//	}

	/**
	 * @param credit the credit to set
	 */
//	public void setCredit(int credit) {
//		this.credit = credit;
//	}

	/**
	 * @return the opfee
	 */
	public int getOpfee() {
		return opfee;
	}

	/**
	 * @param opfee the opfee to set
	 */
	public void setOpfee(int opfee) {
		this.opfee = opfee;
	}

	/**
	 * @return the lastchargetime
	 */
	public long getLastchargetime() {
		return lastchargetime;
	}

	/**
	 * @param lastchargetime the lastchargetime to set
	 */
	public void setLastchargetime(long lastchargetime) {
		this.lastchargetime = lastchargetime;
	}

	/**
	 * @return the totalbattle
	 */
	public int getTotalbattle() {
		return totalbattle;
	}

	/**
	 * @param totalbattle the totalbattle to set
	 */
	public void setTotalbattle(int totalbattle) {
		this.totalbattle = totalbattle;
	}

	/**
	 * @return the winbattle
	 */
	public int getWinbattle() {
		return winbattle;
	}

	/**
	 * @param winbattle the winbattle to set
	 */
	public void setWinbattle(int winbattle) {
		this.winbattle = winbattle;
	}
	
	/**
	 * @return the status
	 */
	public GuildStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(GuildStatus status) {
		this.status = status;
	}

	/**
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * @return the ownerJoinMillis
	 */
	public long getOwnerJoinMillis() {
		return ownerJoinMillis;
	}

	/**
	 * @param ownerJoinMillis the ownerJoinMillis to set
	 */
	public void setOwnerJoinMillis(long ownerJoinMillis) {
		this.ownerJoinMillis = ownerJoinMillis;
	}

	/**
	 * @return the upgradeBeginTime
	 */
	public long getUpgradeBeginTime() {
		return upgradeBeginTime;
	}

	/**
	 * @param upgradeBeginTime the upgradeBeginTime to set
	 */
	public void setUpgradeBeginTime(long upgradeBeginTime) {
		this.upgradeBeginTime = upgradeBeginTime;
	}

	/**
	 * @return the upgradeEndTime
	 */
	public long getUpgradeEndTime() {
		return upgradeEndTime;
	}

	/**
	 * @param upgradeEndTime the upgradeEndTime to set
	 */
	public void setUpgradeEndTime(long upgradeEndTime) {
		this.upgradeEndTime = upgradeEndTime;
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
	 * Convert to protobuf GuildSimpleInfo
	 * @return
	 */
	public GuildSimpleInfo toGuildSimpleInfo(int rank) {
		GuildSimpleInfo.Builder builder = GuildSimpleInfo.newBuilder();
		builder.setGuildID(_id);
		builder.setGuildName(title);
		builder.setLeaderName(roleName);
		builder.setGuildMemberCnt(count);
		builder.setGuildLevel(level);
		builder.setGuildCredit(wealth);
		builder.setGuildRank(rank);
		if ( announce != null ) {
			builder.setAnnouncements(announce);
		}
		
		return builder.build();
	}
	
	/**
	 * Convert to protobuf GuildInfo
	 * @return
	 */
	public GuildInfo toGuildInfo(User user) {
		GuildInfo.Builder builder = GuildInfo.newBuilder();
		builder.setGuildID(_id);
		builder.setGuildName(title);
		builder.setLeaderID(userId.toString());
		builder.setLeaderName(roleName);
		builder.setGuildWealth(wealth);
		//credit is same as wealth
		builder.setGuildCredit(wealth);
		builder.setGuildMemberCnt(count);
		builder.setGuildLevel(level);
		if ( announce != null ) {
			builder.setAnnouncements(announce);
		}
		if ( declaration != null ) {
			builder.setDeclaration(declaration);
		}
		/**
		 * TODO Do I need a JVM local cache like ehcache here?
		 * The guild member is not kept by Guild object in memory.
		 */
		int onlineCount = GuildManager.getInstance().countGuildMemberOnline(user);
		builder.setOnline(onlineCount);
		int opFee = ScriptManager.getInstance().runScriptForInt(ScriptHook.GUILD_OPFEE, user, this);
		builder.setOpfee(opFee);
		GuildFacility shop = facilities.get(GuildFacilityType.shop);
		builder.setShoplevel(shop.getLevel());
		GuildFacility craft = facilities.get(GuildFacilityType.craft);
		builder.setShoplevel(craft.getLevel());
		GuildFacility storage = facilities.get(GuildFacilityType.storage);
		builder.setShoplevel(storage.getLevel());
		builder.setMemberlimit(maxCount);
		double[] expRatios = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.GUILD_LEVEL_EXPRATIO);
		 //公会战斗经验加成(预留) 0 - 10000表示 0.00%
		int guildIndex = level-1;
		int value = (int)(expRatios[guildIndex] * 10000);
		builder.setExpratio(value);
		for ( GuildRole role : GuildRole.values() ) {
			builder.addPosName(role.getTitle());
		}
		int[][] facLimit = GameDataManager.getInstance().getGameDataAsIntArrayArray(GameDataKey.GUILD_FACILITY_MIN_CREDIT);
		int shopLimit = facLimit[GuildFacilityType.shop.id()][guildIndex];
		int ironLimit = facLimit[GuildFacilityType.craft.id()][guildIndex];
		int storageLimit = facLimit[GuildFacilityType.storage.id()][guildIndex];
		builder.setShoplimit(shopLimit);
		builder.setIronlimit(ironLimit);
		builder.setBaglimit(storageLimit);
		builder.setRank(rank);
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
		Guild other = (Guild) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Guild [_id=");
		builder.append(_id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", announce=");
		builder.append(announce);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", roleName=");
		builder.append(roleName);
		builder.append(", level=");
		builder.append(level);
		builder.append(", count=");
		builder.append(count);
		builder.append(", wealth=");
		builder.append(wealth);
		builder.append(", opfee=");
		builder.append(opfee);
		builder.append(", lastchargetime=");
		builder.append(lastchargetime);
		builder.append(", totalbattle=");
		builder.append(totalbattle);
		builder.append(", winbattle=");
		builder.append(winbattle);
		builder.append("]");
		return builder.toString();
	}

	/**
		公会信息显示区公会显示根据公会排名，排名规则：
      a．公会等级：公会等级越高，排名越靠前；
      b. 如果公会等级相同，则根据公会财富排列，财富越高，排名越靠前；
	 */
	@Override
	public int compareTo(Guild o) {
		if ( o == null ) {
			return -1;
		}
		int levelDiff = o.level - this.level;
		if ( levelDiff != 0 ) {
			return levelDiff;
		} else {
			int wealthDiff = o.wealth - this.wealth;
			if ( wealthDiff != 0 ) {
				return wealthDiff;
			} else {
				return this._id.compareTo(o._id);
			}
		}
	}
	
}
