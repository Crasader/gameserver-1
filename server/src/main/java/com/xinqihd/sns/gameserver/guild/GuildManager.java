package com.xinqihd.sns.gameserver.guild;

import static com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.ShopCatalog;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.AbstractMongoManager;
import com.xinqihd.sns.gameserver.db.mongo.BulletinManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserSync.BceUserSync;
import com.xinqihd.sns.gameserver.proto.XinqiBseCreateGuild.BseCreateGuild;
import com.xinqihd.sns.gameserver.proto.XinqiBseEnterGuild.BseEnterGuild;
import com.xinqihd.sns.gameserver.proto.XinqiBseExitGuild.BseExitGuild;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildApply.BseGuildApply;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildApplyProcess.BseGuildApplyProcess;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildBag.BseGuildBag;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildBagPut.BseGuildBagPut;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildBagTake.BseGuildBagTake;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildChangeAnnounce.BseGuildChangeAnnounce;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildChangeRole.BseGuildChangeRole;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildContribute.BseGuildContribute;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildContributeQuery.BseGuildContributeQuery;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFacilityLevelList.BseGuildFacilityLevelList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFacilityLevelUp.BseGuildFacilityLevelUp;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFacilityList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFacilityList.BseGuildFacilityList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFire.BseGuildFire;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildInvite.BseGuildInvite;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildList.BseGuildList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildPrivilegeList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildPrivilegeList.BseGuildPrivilegeList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildShopping.BseGuildShopping;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildTransfer.BseGuildTransfer;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.JSON;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Manage the guild function.
 * 
 * The database structure is as follows:
 * guilds - the collection contains all the main data for a guild, excluding its members and facilities
 * guildmembers - the collection contains all the members for a guild.
 * guildfacilities - the collection contains all facilities for a guild
 * guildapplys  - all the applications for given guild
 * guildbags - the guild storage.
 * 
 * @author wangqi
 *
 */
public final class GuildManager extends AbstractMongoManager {

	/**
	 * Wrapper class
	 * @author wangqi
	 *
	 */
	private static class TargetUser {
		User user = null;
		String gameServerId = null;
		SessionKey sessionKey = null;
		boolean atRemoteServer = false;;
	}

	public static final String REDIS_GUILDBAG_LOCK = "guildbag:lock";
	
	public static final String REDIS_GUILDMEMBER = "guildmember:";
	
	private static final Logger logger = LoggerFactory.getLogger(GuildManager.class);
	private static final String COLL_NAME = "guilds";
	private static final String COLL_APPLY_NAME = "guildapplys";
	private static final String COLL_MEMBER_NAME = "guildmembers";
	private static final String COLL_FACILITY_NAME = "guildfacilities";
	private static final String COLL_BAG_NAME = "guildbags";
	
	private static final String COLL_BAGEVENT_NAME = "guildbagevents";
	private static final String INDEX_NAME = "_id";
	private static final String WEALTH_NAME = "wealth";
	private static final String LEVEL_NAME = "level";
	private static final String USERID_NAME = "userId";
	private static final String ROLENAME_NAME = "roleName";
	private static final String GUILDID_NAME = "guildId";
	private static final String GUILDTITLE_NAME = "title";
	private static final String VERSION_NAME = "version";
	private static final String MAX_NAME = "max";
	private static final String COUNT_NAME = "count";
	
	private static final String RANK_NAME = "rank";
	private static final DBObject sorts = new BasicDBObject();
	private static final DBObject applySorts = new BasicDBObject();
	private static final DBObject bagEventSorts = new BasicDBObject();
	
	private static final DBObject memSorts = new BasicDBObject();
	
	private static final DBObject applyFields = new BasicDBObject();

	private static GuildManager instance = new GuildManager();
	
	public final static GuildManager getInstance() {
		return instance;
	}
	
	private GuildManager() {
		super(
				GlobalConfig.getInstance().getStringProperty("mongdb.database"),
				GlobalConfig.getInstance().getStringProperty("mongdb.namespace"),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		sorts.put(LEVEL_NAME, "DESC");
		sorts.put(WEALTH_NAME, "DESC");
		applySorts.put("applytime", "DESC");
		applyFields.put("guildId", Constant.ONE);
		applyFields.put("guildName", Constant.ONE);
		// -1表示逆序，从大到小排列
		bagEventSorts.put("timestamp", -1);
		memSorts.put("totalCredit", -1);
		
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_APPLY_NAME, USERID_NAME, false);
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_APPLY_NAME, GUILDID_NAME, false);
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_MEMBER_NAME, USERID_NAME, false);
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_MEMBER_NAME, GUILDID_NAME, false);
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_BAG_NAME, USERID_NAME, false);
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_BAG_NAME, GUILDID_NAME, false);
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_BAGEVENT_NAME, GUILDID_NAME, false);
	}
	
	/**
	 * Add a new bag event to guild ( max 100 events ) 
	 * @param event
	 */
	public boolean addGuildBagEvent(GuildBagEvent event) {
		boolean success = false;
		try {
			int count = queryGuildBagEventCount(event.getGuildId());
			if ( count >= 100 ) {
				//Find the oldest one
				DBObject query = MongoDBUtil.createDBObject(GUILDID_NAME, event.getGuildId());
				DBObject fields = MongoDBUtil.createDBObject(INDEX_NAME, Constant.ONE);
				DBObject oldBagEvent = (DBObject)MongoDBUtil.queryFromMongo(
						query, databaseName, namespace, COLL_BAGEVENT_NAME, fields, bagEventSorts);
				MongoDBUtil.removeDocument(databaseName, namespace, COLL_BAGEVENT_NAME, 
						MongoDBUtil.createDBObject(INDEX_NAME, oldBagEvent.get(INDEX_NAME)));
			}
			DBObject dbObj = MongoDBUtil.createMapDBObject(event);
			MongoDBUtil.saveToMongo(null, dbObj, databaseName, namespace, COLL_BAGEVENT_NAME, true);
		} catch (Exception e) {
			logger.warn("Failed to save guild bag event", e);
			success = false;
		}
		return success;
	}
	
	/**
	 * Add a new PropData to GuildBag. When doing this,
	 * we need not to refresh the whole guildBag list.
	 * Instead, only the max count of the guildBag need
	 * to refresh.
	 * 
	 * @param user
	 * @param guildBag
	 * @return
	 */
	public boolean addGuildBagPropData(User user, GuildBag guildBag, PropData propData) {
		try {
			/**
			 * Check the guildBag version first to check if someone else
			 * are changing it too.
			 */
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guildBag.get_id());
			DBObject versionField = MongoDBUtil.createDBObject(VERSION_NAME, Constant.ONE);
			DBObject versionObj = MongoDBUtil.queryFromMongo(
					query, databaseName, namespace, COLL_BAG_NAME, versionField);
			long currentVersion = 0l;
			if ( versionObj != null ) {
				currentVersion = (Long)versionObj.get(VERSION_NAME);
			}
			if ( guildBag.getVersion() != currentVersion ) {
				DBObject maxField = MongoDBUtil.createDBObject(MAX_NAME, Constant.ONE);
				DBObject maxObj = MongoDBUtil.queryFromMongo(
						query, databaseName, namespace, COLL_BAG_NAME, versionField);
				if ( maxObj != null ) {
					int max = (Integer)maxObj.get(MAX_NAME);
					int count = (Integer)maxObj.get(COUNT_NAME);
					guildBag.setMax(max);
					guildBag.setCount(count);
				}
			}
			/**
			 * Check the size is in range.
			 */
			if ( guildBag.getCount() < guildBag.getMax() ) {
				//do the insert
				guildBag.addPropData(propData);
				DBObject propObj = MongoDBUtil.createMapDBObject(propData);
				DBObject pushObj = MongoDBUtil.createDBObject("$inc", MongoDBUtil.createDBObject(COUNT_NAME, 1));
				pushObj.put("$set", MongoDBUtil.createDBObject(
						"propList.".concat(String.valueOf(propData.getPew())), propObj));
				MongoDBUtil.saveToMongo(query, pushObj, databaseName, namespace, COLL_BAG_NAME, isSafeWrite);
				return true;
			}
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
		}
		return false;
	}
	
	/**
	 * An user wants to join a guild.
			2）如果公会人数已满，则弹出提示：
				该公会人数已满，请选择其他公会！
			3）申请加入公会，最多同时申请5个公会，超过5个则无法继续申请，并弹出提示：
				您的申请已满，无法继续申请！
			4）如果玩家的申请被拒绝，则弹出提示：
				您加入公会“XXXXX”的申请被拒绝！
				并在申请移除该申请记录，增加玩家可申请公会数量一条。
			5）如果玩家的申请被同意，则弹出提示：
				恭喜您成功加入“xxx”公会！

	 * 
	 * @param guildId
	 * @param user
	 */
	public final boolean applyGuild(User user, String guildId) {
		boolean success = false;
		String message = null;
		int maxApplyCount = 5;
		/**
		 * 检查公会ID有效
		 */
		Guild guild = queryGuildById(guildId);
		if ( guild == null ) {
			message = Text.text("guild.apply.noid");
		} else {
			/**
			 * 检查玩家未加入公会
			 */
			if ( user.getGuild() != null ) {
				message = Text.text("guild.apply.exist", user.getGuild().getTitle());
			} else {
				int maxCount = getGuildMaxCount(guild);
				if ( guild.getCount() >= maxCount ) {
					message = Text.text("guild.apply.full");
				} else {
					//Check the total number of apply that an user send.
					boolean alreadyApplied = false;
					Collection<Apply> applys = queryGuildApply(user.get_id());
					ArrayList<Apply> deleteQueue = new ArrayList<Apply>();
					for (Iterator iter = applys.iterator(); iter.hasNext();) {
						Apply apply = (Apply) iter.next();
						if ( apply.getGuildId().equals(guildId) && 
								apply.getStatus() == ApplyStatus.pending ) {
							alreadyApplied = true;
							break;
						}
						if ( apply.getStatus() != ApplyStatus.pending ) {
							deleteQueue.add(apply);
							iter.remove();
						}
					}
					if ( alreadyApplied ) {
						message = Text.text("guild.apply.submited", guild.getTitle());
					} else {
						if ( applys.size() >= maxApplyCount ) {
							message = Text.text("guild.apply.maxapply", maxApplyCount);
						} else {
							message = Text.text("guild.apply.success");

							Apply apply = new Apply();
							apply.setGuildId(guildId);
							apply.setGuildName(guild.getTitle());
							apply.setUserId(user.get_id());
							apply.setApplytime(System.currentTimeMillis());
							apply.setStatus(ApplyStatus.pending);
							
							success = saveGuildApply(apply);
						}
					}

					if ( deleteQueue.size()>0 ) {
						for ( Apply apply : deleteQueue ) {
							removeGuildApply(user.get_id(), guildId);
						}
					}
				}
			}
		}

		BseGuildApply.Builder builder = BseGuildApply.newBuilder();
		if ( success ) {
			builder.setResult(0);
		} else {
			builder.setResult(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
			logger.debug("applyGuild message:{}", message);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildApply, guildId, success, message);
		return success;
	}
	
	/**
	 * The the guild's announcement
	 * 
	 * @param owner
	 * @param guild
	 * @parame type: 0: change announce; 1: declaration
	 * @return
	 */
	public final boolean changeGuildAnnounce(User owner, String announce, int type) {
		boolean success = false;
		String message = null;
		Guild guild = owner.getGuild();
		if ( guild != null ) {
			boolean hasPrivilege = ScriptManager.getInstance().runScriptForBoolean(ScriptHook.GUILD_CHECK_PRVIILEGE, 
					owner, GuildPrivilege.announce);
			if ( hasPrivilege ) {
				announce = processAnnounce(announce);
				DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guild.get_id());
				String field = null;
				if ( type == 0 ) {
					field = "announce";
					guild.setAnnounce(announce);
				} else {
					field = "declaration";
					guild.setDeclaration(announce);
				}
				DBObject dbObj = MongoDBUtil.createDBObject("$set", 
						MongoDBUtil.createDBObject(field, announce));
				MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_NAME, isSafeWrite);
				
  			//Ask all online users to refresh 
				syncGuildWithOnlineUsers(guild);
				
				success = true;
			} else {
				message = Text.text("guild.announce.nopriv");
			}
		} else {
			message = Text.text("guild.announce.noguild");
		}
		BseGuildChangeAnnounce.Builder builder = BseGuildChangeAnnounce.newBuilder();
		if ( success ) {
			builder.setStatus(0);
			builder.setMessage(Text.text("guild.announce.success"));
		} else {
			builder.setStatus(1);
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(owner.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(owner, StatAction.GuildChangeAnno, owner.getGuildId(), success, message, announce);
		return success;
	}
	
	/**
	 * Change the guild member's role.
	 * @param guild
	 * @param member
	 * @param newRole
	 * @return
	 */
	public final boolean changeGuildMemberRole(User user, String targetUserIdStr, GuildRole newRole) {
		boolean success = false;
		String message = null;
		UserId targetUserId = UserId.fromString(targetUserIdStr);
		/**
		 * 检查权限
		 */
		success = ScriptManager.getInstance().runScriptForBoolean(
				ScriptHook.GUILD_CHECK_PRVIILEGE, user, GuildPrivilege.guildrole);
		if ( !success ) {
			message = Text.text("guild.changerole.nopriv");
		}
		/**
		 * 会长不能转让
		 */
		if ( success ) {
			if ( newRole == GuildRole.chief ) {
				success = false;
				message = Text.text("guild.changerole.owner");
			}
		}
		/**
		 * 检查目标等级当前的人数
		 */
		if ( success ) {
			if ( newRole != GuildRole.member ) {
				int[][] roleCounts = GameDataManager.getInstance().getGameDataAsIntArrayArray(GameDataKey.GUILD_LEVEL_MANAGER);
				Guild guild = user.getGuild();
				int levelIndex = guild.getLevel()-1;
				int[] roleCount = roleCounts[levelIndex];
				int rc = roleCount[newRole.ordinal()-1];
				Collection<GuildMember> members = queryGuildMemberByGuildId(guild.get_id());
				int count = 0;
				for ( GuildMember member : members ) {
					if ( member.getRole() == newRole ) {
						count++;
						if ( count >= rc ) {
							success = false;
							message = Text.text("guild.changerole.max", guild.getLevel(), newRole.getTitle(), rc);
							break;
						}
					}
				}
			}
		}
		if ( success ) {
			Guild guild = user.getGuild();
			if ( guild.getUserId().equals(targetUserId) ) {
				success = false;
				message = Text.text("guild.changerole.owner");
			}
		}
		if ( success ) {
			String id = StringUtil.concat(user.getGuildId(), Constant.COLON, targetUserIdStr.toString());
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, id);
			DBObject dbObj = MongoDBUtil.createDBObject("$set", 
					MongoDBUtil.createDBObject("role", newRole.toString()));
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_MEMBER_NAME, isSafeWrite);
			success = true;
		}
		BseGuildChangeRole.Builder builder = BseGuildChangeRole.newBuilder();
		if ( success ) {
			builder.setStatus(0);
			message = Text.text("guild.changerole.success");
			builder.setMessage(message);
			
		  //Ask all online users to refresh 
			syncGuildWithOnlineUsers(user.getGuild());
		} else {
			builder.setStatus(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildChangeRole, 
				user.getGuildId(), success, message, newRole);
		return success;
	}
	
	/**
	 * Check if the guild name exists.
	 * 
	 * @param guildId
	 * @return
	 */
	public boolean checkGuildIdExist(String guildId) {
		DBObject query = createDBObject();
		query.put(INDEX_NAME, guildId);
		long count = MongoDBUtil.countQueryResult(query, databaseName, namespace, COLL_NAME);
		return count == 1;
	}

	/**
	 * Check if the guild need to pay its operation fee
	 * 
	 * 3.	当公会财富值连续1周不够维护费扣取时，公会自动解散并在所有成员上线
	 * 在线时候弹出提示：您的公会xxxx由于财富值过低，公会自动解散！
	 * 
	 * 解散前3天每次登陆时给予即将解散的提示

	 * @param guild
	 */
	public final boolean checkOperationFee(User user, Guild guild) {
		boolean success = true;
		if ( guild == null ) {
			logger.warn("checkOperationFee: guild is null");
			return success;
		}
		boolean payed = ScriptManager.getInstance().runScriptForBoolean(ScriptHook.GUILD_OPFEE_CHECK, user, guild);
		if ( !payed ) {
			//Dismiss guild
			success = false;
			Collection<GuildMember> members = queryGuildMemberByGuildId(guild.get_id());
			for ( GuildMember member : members ) {
				String message = Text.text("guild.opfee.notify.dismiss", guild.getTitle());
				sendMessageToUser(member.getUserId(), message, message);
				TargetUser targetUser = findTargetUser(member.getUserId());
				if ( targetUser.sessionKey != null ) {
					if ( !targetUser.atRemoteServer ) {
						targetUser.user.setGuildId(null);
						targetUser.user.setGuild(null);
						targetUser.user.setGuildMember(null);
						UserManager.getInstance().saveUser(targetUser.user, false);
						GameContext.getInstance().writeResponse(targetUser.sessionKey, user.toBseRoleInfo());
					} else {
						BceUserSync.Builder builder = BceUserSync.newBuilder();
						builder.setMode(5);
						GameContext.getInstance().proxyToRemoteGameServer(targetUser.sessionKey, targetUser.gameServerId, builder.build());
					}
				}
			}
			/**
			 * For safty consideration, change the removeGuild to dismissGuild
			 * wangqi 2013-03-22
			 */
			removeGuild(guild.get_id());
			/**
			 * This method should be called before setting current user's fields
			 * so that it does not need to set them null.
			 */
//			user.setGuildId(null);
//			user.setGuild(null);
//			user.setGuildMember(null);
//			UserManager.getInstance().saveUser(user, false);
//			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
			StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildDismiss, guild.get_id());
		}
		return success;
	}
	
	/**
	 * An user wants to create a new guild
	 * 1. 公会名称最多5个字符，不能为空，不能使用空格
	 * 2. 公会宣言最多可输入128个字符，可以为空，可以使用空格及符号。
	 * 
	 * 创建公会条件：
     A）玩家等级≥20级
     B）需要500000金币

     下行信息
     a. 恭喜您成功创建公会“公会五个字”！
     b. 公会名称已被使用，请返回修改！
     c. 您的等级没有达到20级，无法创建公会！
     d. 您的金币数额不足500000，无法创建公会！

	 * 
	 * @param user
	 * @param guildName
	 * @param declaration
	 * @return
	 */
	public final Guild createGuild(User user, String guildName, String declaration) {
		String message = null;
		boolean success = false;
		Guild guild = null;
		ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.GUILD_CREATE_CHECK, user, guildName);
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			List list = result.getResult();
			success = (Boolean)list.get(0);
			message = (String)list.get(1);
			if ( success ) {
				guild = new Guild();
				guild.set_id(guildName);
				guild.setTitle(guildName);
				declaration = processAnnounce(declaration);
				guild.setDeclaration(declaration);
				guild.setUserId(user.get_id());
				guild.setRoleName(user.getRoleName());
				guild.setLastchargetime(System.currentTimeMillis());
				guild.setCount(1);
				guild.setLevel(1);
				guild.setOwnerJoinMillis(System.currentTimeMillis());
				int[] maxCounts = GameDataManager.getInstance().getGameDataAsIntArray(
						GameDataKey.GUILD_LEVEL_MAXCOUNT);
				guild.setMaxCount(maxCounts[0]);
				/**
				 * 初始化公会背包
				 */
				GuildBag guildBag = new GuildBag();
				guildBag.set_id(guild.get_id());
				guildBag.setCount(0);
				int max = getGuildBagMaxCount(guild);
				guildBag.setMax(max);
				saveGuildBag(user, guildBag);
				
				//Add the user as the guild owner
				createGuildMember(guild, user, true, GuildRole.chief);
				
				/**
				 * 初始化公会设施，需要在创建公会成员后调用
				 */
				ScriptManager.getInstance().runScript(ScriptHook.GUILD_INIT_FACILITY, user, guild);
				success = saveGuild(guild);
				
				int golden = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.GUILD_CREATE_GOLDEN, 500000);
				user.setGolden(user.getGolden()-golden);
				user.setGuildId(guild.get_id());
				user.setGuild(guild);
				UserManager.getInstance().saveUser(user, false);
				GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
				
				message = Text.text("guild.create.success", guild.getTitle());
				
			}
		}
		
		BseCreateGuild.Builder builder = BseCreateGuild.newBuilder();
		if ( success ) {
			builder.setResult(0);
			builder.setGuild(guild.toGuildInfo(user));
		} else {
			builder.setResult(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
			logger.debug("User {} create build message {}", user.getRoleName(), message);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
		if ( guild != null ) {
			StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildCreate, success, message, guild.get_id());
		} else {
			StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildCreate, success, message);
		}
		return guild;
	}
	
	/**
	 * @param guild
	 * @param targetUser
	 */
	public GuildMember createGuildMember(Guild guild, User user, boolean online, GuildRole role) {
		GuildMember member = new GuildMember();
		String id = StringUtil.concat(guild.get_id(), Constant.COLON, user.get_id().toString());
		member.set_id(id);
		member.setGuildId(guild.get_id());
		member.setUserId(user.get_id());
		member.setRoleName(user.getRoleName());
		member.setCredit(0);
		member.setOnline(online);
		member.setRole(role);
		
		/**
		 * 为成员初始化技能数据
		 */
		ScriptManager.getInstance().runScript(ScriptHook.GUILD_INIT_USER, user, member);
		saveGuildMember(member);
		
		user.setGuildMember(member);
		return member;
	}
	
	/**
	 * List all the guild number for given guild.
	 * @param guildId
	 * @return
	 */
	public final int countGuildMemberOnline(User user) {
		Guild guild = user.getGuild();
		if ( guild != null ) {
			Jedis jedis = JedisFactory.getJedis();
			String key = StringUtil.concat(REDIS_GUILDMEMBER, guild.get_id());
			Long result = jedis.hlen(key);
			if(result != null) {
				return result.intValue();
			}
		}
		return 0;
	}
	
	/**
	 * @param apply
	 * @param approve
	 * @param guild
	 * @return
	 */
	private boolean doProcessGuildApply(Apply apply, boolean approve, Guild guild) {
		boolean success = false;
		String subject, content;
		if ( approve ) {
			/**
			 * The target user may apply for more than one guilds, there 
			 * should be put a check on it.
			 */
			TargetUser targetUser = findTargetUser(apply.getUserId());
			if ( targetUser.user != null ) {
				if ( !StringUtil.checkNotEmpty(targetUser.user.getGuildId()) ) {
					Status status = joinGuild(targetUser.user, guild, targetUser.sessionKey!=null);
					success = status.isSuccess();
					if ( !success ) {
						content = Text.text("guild.apply.approve.full", 
								guild.getTitle());
					}
				}
			}
			if ( targetUser.atRemoteServer ) {
				BceUserSync.Builder builder = BceUserSync.newBuilder();
				builder.setMode(4);
				builder.setObject(guild.get_id());
				GameContext.getInstance().proxyToRemoteGameServer(
						targetUser.sessionKey, targetUser.gameServerId, builder.build());
			}
			
			subject = Text.text("guild.apply.approve", guild.getTitle());
			content = subject;
			
			//Ask all online users to refresh 
			syncGuildWithOnlineUsers(guild);
		} else {
			subject = Text.text("guild.apply.deny", guild.getTitle());
			content = subject;
		}
		success = removeGuildApply(apply.getUserId(), apply.getGuildId());
		//Send the targetUser SysMessage or Mail notification.
		sendMessageToUser(apply.getUserId(), subject, content);
		
		return success;
	}
	
	/**
	 * The user enters his own guild.
	 * @param user
	 */
	public final void enterGuild(User user, String guildId) {
		Guild guild = user.getGuild();
		if ( guild != null ) {
			GuildManager.getInstance().markGuildMemberOnline(user, true, true);
			BseEnterGuild.Builder builder = BseEnterGuild.newBuilder();
			builder.setGuildID(guildId);
			builder.setGuildInfo(guild.toGuildInfo(user));
			int count = queryGuildApplyCount(guildId);
			if ( count > 0 ) {
				builder.setApplynum(count);
			}
			GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
			StatClient.getIntance().sendDataToStatServer(user, StatAction.EnterGuild, guildId);
		} else {
			SysMessageManager.getInstance().sendClientInfoMessage(
					user.getSessionKey(), Text.text("guild.enter.success"), Type.NORMAL);
		}
	}
	
	/**
	 * Fire a guild member from this guild.
	 * 
	 * @param owner
	 * @param targetUserIdStr
	 * @return
	 */
	public final boolean fireGuildMember(User owner, String targetUserIdStr) {
		boolean success = ScriptManager.getInstance().runScriptForBoolean(
				ScriptHook.GUILD_CHECK_PRVIILEGE, owner, GuildPrivilege.firememeber);
		String message = null;
		/**
		 * 检查权限
		 */
		if ( !success ) {
			message = Text.text("guild.fire.nopriv");
		}
		/**
		 * 公会会长不能开除自己
		 */
		UserId targetUserId = UserId.fromString(targetUserIdStr);
		if ( success ) {
			if ( owner.get_id().equals(targetUserId) ) {
				success = false;
				message = Text.text("guild.fire.self");
			}
		}
		/**
		 * 自己也不能开出自己
		 */
		if ( success ) {
			if ( owner.getGuild().getUserId().equals(targetUserId) ) {
				success = false;
				message = Text.text("guild.fire.owner");
			}
		}
		if ( success ) {
			success = removeGuildMember(owner.getGuild(), targetUserId, owner.getGuildId());
			message = Text.text("guild.fire.success");
			String mail = Text.text("guild.fire.mail", owner.getRoleName());
			sendMessageToUser(targetUserId, mail, mail);
			TargetUser targetUser = findTargetUser(targetUserId);
			if ( targetUser.sessionKey != null ) {
				//Online
				if ( targetUser.atRemoteServer ) {
					BceUserSync.Builder builder = BceUserSync.newBuilder();
					builder.setMode(5);
					GameContext.getInstance().proxyToRemoteGameServer(
							targetUser.sessionKey, targetUser.gameServerId, builder.build());
				} else {
					/**
					 * Remove the target user's guildId
					 */
					targetUser.user.setGuild(null);
					targetUser.user.setGuildId(null);
					targetUser.user.setGuildMember(null);
					UserManager.getInstance().saveUser(targetUser.user, false);
					markGuildMemberOnline(targetUser.user, false, false);
				}
				if ( success ) {
					//Ask all online users to refresh 
					syncGuildWithOnlineUsers(owner.getGuild());
					
					BseExitGuild.Builder b = BseExitGuild.newBuilder();
					b.setResult(0);
					GameContext.getInstance().writeResponse(targetUser.sessionKey, b.build());
				}
			} else {
				/**
				 * Remove the target user's guildId
				 */
				success = UserManager.getInstance().removeUserGuildId(targetUser.user.get_id());
			}
		}
		BseGuildFire.Builder builder = BseGuildFire.newBuilder();
		if ( success ) {
			builder.setStatus(0);
		} else {
			builder.setStatus(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(owner.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(owner, StatAction.GuildFire, 
				owner.getGuildId(), success, targetUserIdStr, message);
		return success;
	}
	
	/**
	 * @param guild
	 * @return
	 */
	public int getGuildFacilityCoolDown(GuildFacility facility) {
		int[] maxCounts = GameDataManager.getInstance().
				getGameDataAsIntArray(GameDataKey.GUILD_FACILITY_COOLDOWN);
		int guildLevel = facility.getLevel();
		if ( guildLevel <= maxCounts.length-1 ) {
			int maxCount = maxCounts[guildLevel];
			return maxCount;
		}
		return -1;
	}
	
	/**
	 * 获取使用公会设置所需要的最低贡献度
	 * 
	 * @param type
	 * @param level
	 * @return
	 */
	public int getGuildFacilityMinCredit(GuildFacilityType type, int level) {
		if ( level == 0 ) return 0;
		int[][] minCredits = GameDataManager.getInstance().getGameDataAsIntArrayArray(
				GameDataKey.GUILD_FACILITY_MIN_CREDIT);
		if ( type.id() < minCredits.length ) {
			int minCredit = minCredits[type.id()][level-1];
			return minCredit;
		}
		return 0;
	}
		
	/**
	 * Display all the shop items by given shopLevel. If the shopLevel is greater than
	 * guild's level, display a warnning message.
	 * 
	 * @param guild
	 * @param shopLevel
	 * @return
	 */
	public final Collection<ShopPojo> getGuildShop(User user, Guild guild, int shopLevel) {
		GuildFacility facility = guild.getFacility(GuildFacilityType.shop);
		String message = null;
		boolean success = false;
		Collection<ShopPojo> shops = null;
		if ( facility.getLevel() == 0 ) {
			message = Text.text("guild.shop.zero");
		} else if ( shopLevel > facility.getLevel() ) {
			message = Text.text("guild.shop.overlevel");
		} else {
			/**
			 * 检查玩家的贡献度是否达到了公会设施要求的限度
			 */
			ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.GUILD_USER_CREDIT_CHECK, 
					user, guild.getFacility(GuildFacilityType.shop));
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				 List list = result.getResult();
				 success = (Boolean)list.get(0);
				 message = (String)list.get(1);
				 int maxShopLevel = (Integer)list.get(2);
				 if ( shopLevel <= maxShopLevel+1 ) {
					 success = true;
				 } else {
					 message = Text.text("guild.facility.unmeetcredit", user.getGuildMember().getCredit(), shopLevel);
					 success = false;
				 }
				 if ( success ) {
					 int index = ShopCatalog.GUILD_LV1.ordinal()+shopLevel-1;
					 ShopCatalog catalog = ShopCatalog.values()[index];
					 shops = ShopManager.getInstance().getShopsByCatalog(catalog);
				 }
			}
		}
		BseGuildShopping.Builder builder = BseGuildShopping.newBuilder();
		if ( success ) {
			builder.setStatus(0);
			if ( shops != null ) {
				for ( ShopPojo shop : shops ) {
					builder.addShoplevel(shop.getLevel());
					builder.addGoodsInfo(shop.toGoodsInfo());
				}
			}
		} else {
			builder.setStatus(1);
			if ( message != null ) {
				builder.setMessage(message);
			}
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildShop, user.getGuildId(), shopLevel);
		return shops;
	}
	
	/**
	 * The user wants to contribute to his/her guild.
			 1. 捐献公式：
					捐献1元宝=5贡献=10财富
					捐献100000金币=1贡献=2财富
       2. 捐献元宝不能为0，最低为1元宝，否则“捐献”按钮为不可点击状态
       3. 捐献金币最低为100000金币，输入的单位以万为单位，如果玩家输入不是万的单位金币，系统自动取整
       4. 玩家捐献后，会在公会频道内显示玩家的捐献行为，显示格式为：
          小小飞弹 捐献了 68 元宝、10000金币，获得了 100 点个人贡献。
       5. 当玩家点“捐献”时候，弹出提示框，提示玩家是否捐献当前的元宝跟金币。

	 * 
	 * @param user
	 * @param guild
	 * @param wealth
	 * @return
	 */
	public final boolean contributeToGuild(User user, int yuanbao, int golden) {
		boolean success = false;
		Guild guild = user.getGuild();
		GuildMember member = user.getGuildMember();
		try {
			/**
			 * Make sure the user has enough money
			 */
			if ( user.getGolden() < golden ) {
				SysMessageManager.getInstance().sendClientInfoMessage(user.getSessionKey(), "shop.error.nogold", Type.NORMAL);
				success = false;
			} else {
				success = true;
			}
			if ( success ) {
				if ( !user.canPayYuanbao(yuanbao) ) {
					success = false;
					SysMessageManager.getInstance().sendClientInfoMessage(user.getSessionKey(), "shop.error.noyuanbao", Type.NORMAL);
				} else {
					success = true;
				}
			}
			if ( success ) {
				ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.GUILD_CONTRIBUTE_QUERY, 
						user, yuanbao, golden);
				int wealth = 0, credit = 0, goldenWealth = 0, goldenCredit = 0;
				if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
					List list = result.getResult();
					wealth = (Integer)list.get(0);
					credit = (Integer)list.get(1);
					goldenWealth = (Integer)list.get(2);
					goldenCredit = (Integer)list.get(3);
				}

				guild.setWealth(guild.getWealth()+wealth+goldenWealth);
				//guild.setCredit(guild.getCredit()+credit+goldenCredit);
				member.setCredit(member.getCredit()+credit+goldenCredit);
				
				saveGuild(guild);
				saveGuildMember(member);
				
			  //Ask all online users to refresh 
				syncGuildWithOnlineUsers(guild);
				
				//pay for it
				user.setGolden(user.getGolden() - golden);
				user.payYuanbao(yuanbao);
				UserManager.getInstance().saveUser(user, false);
				GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
				success = true;
				
				StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildContribute, 
						user.getGuildId(), wealth, credit, goldenWealth, goldenCredit);
			}
			
			//Subtract user's yuanbao and golden
			//user.setGolden(user.getGolden() - )
		} catch (Exception e) {
			logger.warn("Failed to contribute", e);
			success = false;
		}
		
		BseGuildContribute.Builder builder = BseGuildContribute.newBuilder();
		builder.setResult(success?0:1);
		builder.setGuildInfo(guild.toGuildInfo(user));
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
		return success;
	}
	
	/**
	 * Query the contribute condition
	 * 捐献公式：
			捐献1元宝=5贡献=10财富
			捐献100000金币=1贡献=2财富
	 * 
	 * @param user
	 * @param guild
	 */
	public final void queryContribute(User user, int yuanbao, int golden) {
		ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.GUILD_CONTRIBUTE_QUERY, 
				user, yuanbao, golden);
		BseGuildContributeQuery.Builder builder = BseGuildContributeQuery.newBuilder();
		Guild guild = user.getGuild();
		GuildMember member = user.getGuildMember();
		builder.setGuildID(guild.get_id());
		int wealth = 0, credit = 0, goldenWealth = 0, goldenCredit = 0;
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			List list = result.getResult();
			wealth = (Integer)list.get(0);
			credit = (Integer)list.get(1);
			goldenWealth = (Integer)list.get(2);
			goldenCredit = (Integer)list.get(3);
		}
		builder.setCredit(credit);
		builder.setWealth(wealth);
		builder.setGoldencredit(goldenCredit);
		builder.setGoldenwealth(goldenWealth);
		builder.setGuildwealth(wealth+goldenWealth);
		builder.setMycredit(credit+goldenCredit);
		
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
	}
	
	/**
	 * @param apply
	 * @param guild
	 * @param targetUser
	 */
	public Status joinGuild(User user, Guild guild, boolean online) {
		Status status = new Status();
		int maxCount = getGuildMaxCount(guild);
		/**
		 * Check if the target user is already in a guild
		 */
		if ( StringUtil.checkNotEmpty(user.getGuildId()) ) {
			status.setSuccess(false);
			status.setMessage(Text.text("guild.recruit.already", user.getRoleName()));
		} else {
			status.setSuccess(true);
		}
		/**
		 * Check if the max count reaches.
		 */
		if ( status.isSuccess() ) {
			if ( guild.getCount() < maxCount ) {
				user.setGuildId(guild.get_id());
				user.setGuild(guild);
				UserManager.getInstance().saveUser(user, false);
				//Create and add the guildmember
				createGuildMember(guild, user, online, GuildRole.member);
				
				updateGuildCount(guild, 1);
				//Ask all online users to refresh 
				syncGuildWithOnlineUsers(guild);
				
				status.setSuccess(true);
			} else {
				status.setSuccess(false);
			}
		}
		return status;
	}
	
	/**
	 * Upgrade a guild facility.
	 * 
	 *  注意，如果升级了公会，则需要相应的开启新的技能
			公会技能	升级要求	升级后效果
			1	公会等级1、消耗财富3000	可学习技能：防御
			2	公会等级2、消耗财富8000	可学习技能：防御、幸运
			3	公会等级3、消耗财富50000	可学习技能：防御、幸运、攻击
			4	公会等级4、消耗财富150000	可学习技能：防御、幸运、攻击、敏捷
			5	公会等级5、消耗财富300000	可学习技能：防御、幸运、攻击、敏捷、生命、寻宝、祈福

			升级规则
			*.	公会设施升级需要有公会设施升级权限
			*.	当公会设施升级后，会有CD时间，在CD时间内不能再升级任何公会设施，选中公会设施后，“升级”按钮上会变成倒计时
			*.	当公会设施没有CD时，有权限升级的成员点击“立即冷却”，会弹出提示：
					当前公会设施可升级，无需冷却！

			*.	玩家学习升级公会各技能，不会有CD时间
			*.	玩家学习升级公会各技能不能超过当前公会设施公会技能的等级。
			*.	玩家已学习的公会技能，退出公会不会消失，但是暂时无法生效，当玩家再次加入公会时，恢复当前公会技能等级相对应的技能；（不妥）

	 * @param user
	 * @param type
	 * @return
	 */
	public boolean levelUpGuildFacility(User user, GuildFacilityType type, boolean isCoolDown) {
		boolean success = true;
		Guild guild = user.getGuild();
		GuildFacility facility = guild.getFacility(type);
		/**
		 * 检查是否有权利升级公会设施
		 */
		if ( facility != null ) {
			success = ScriptManager.getInstance().runScriptForBoolean(
					ScriptHook.GUILD_CHECK_PRVIILEGE, user, GuildPrivilege.levelup);
		} else {
			facility = user.getGuildMember().getFacility(type);
		}
		if ( success ) {
			ScriptManager.getInstance().runScript(ScriptHook.GUILD_FACILITY_LEVELUP, 
					user, type, facility, isCoolDown);
			
		  //Ask all online users to refresh 
			syncGuildWithOnlineUsers(guild);
		} else {
			BseGuildFacilityLevelUp.Builder builder = BseGuildFacilityLevelUp.newBuilder();
			builder.setStatus(1);
			builder.setMessage(Text.text("guild.levelup.nopriv"));
			GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		}
		return success;
	}
	
	/**
	 * List all applys for a guild
	 * @param user
	 * @param startPos
	 * @param count
	 * @return
	 */
	public final Collection<Apply> listGuildApplys(User user, String guildId, int startPos, int count) {
		ArrayList<Apply> applys = new ArrayList<Apply>();
		List<DBObject> applyList = null;
		DBObject query = MongoDBUtil.createDBObject();
		query.put(GUILDID_NAME, guildId);
		if ( count > 0 ) {
			applyList = MongoDBUtil.queryAllFromMongo(query, databaseName, namespace, COLL_APPLY_NAME, null, applySorts, startPos, count);
		} else {
			applyList = MongoDBUtil.queryAllFromMongo(query, databaseName, namespace, COLL_APPLY_NAME, null, applySorts);
		}
		
		for ( DBObject dbObj : applyList ) {
			Apply apply = (Apply)MongoDBUtil.constructObject(dbObj);
			applys.add(apply);
		}
		
		return applys;
	}
	
	/**
	 * List all the facilities in a guild for the given user. Note:
	 * should check the cooldown time.
	 * 
	 * @param user
	 * @param guild
	 */
	public final void listGuildFacility(User user, Guild guild) {
		BseGuildFacilityList.Builder builder = BseGuildFacilityList.newBuilder();
		//公会设施
		HashMap<GuildFacilityType, GuildFacility> facilities = guild.getFacilities(); 
		for ( GuildFacility facility : facilities.values() ) {
			XinqiBseGuildFacilityList.GuildFacility f = facility.toGuildFacility();
			if ( f != null ) {
				builder.addFacility(f);
			}
		}
		//公会个人技能
		facilities = user.getGuildMember().getFacilities(); 
		for ( GuildFacility facility : facilities.values() ) {
			XinqiBseGuildFacilityList.GuildFacility f = facility.toGuildFacility();
			if ( f != null ) {
				builder.addFacility(f);
			}
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
	}
	
	/**
	 * 公会设施的升级权限和数据
	 * @param user
	 * @param guild
	 */
	public final void listGuildFacilityLevelList(User user, Guild guild) {
		BseGuildFacilityLevelList.Builder builder = BseGuildFacilityLevelList.newBuilder();
		
		HashMap<GuildFacilityType, GuildFacility> facilities = guild.getFacilities();
		for ( GuildFacilityType type : facilities.keySet() ) {
			GuildFacility facility = facilities.get(type);
			builder.addFacility(facility.toLevelGuildFacility(guild));
		  /**
		   * 下一次升级的冷却时间(秒), 为0表示可以立即升级
		   */
			long currentMillis = System.currentTimeMillis();
			int diffSecond = (int)((guild.getUpgradeEndTime() - currentMillis)/1000);
			int coolDownSecond = 0;
			if ( diffSecond > 0 ) {
				coolDownSecond = diffSecond;
			}
		  /**
		   * 取消冷却时间，立即升级的元宝价格
		  */
			int yuanbao = ScriptManager.getInstance().runScriptForInt(ScriptHook.GUILD_CALCULATE_CD_YUANBAO, 
					user, facility);
			builder.setCooldown(coolDownSecond);
			builder.setYuanbao(yuanbao);
			if ( user.getGuildMember() != null ) {
				builder.setMycredit(user.getGuildMember().getCredit());
			}
		}
		
		facilities = user.getGuildMember().getFacilities();
		for ( GuildFacilityType type : facilities.keySet() ) {
			GuildFacility facility = facilities.get(type);
			builder.addFacility(facility.toLevelGuildFacility(guild));
		  /**
		   * 下一次升级的冷却时间(秒), 为0表示可以立即升级
		   */
			long currentMillis = System.currentTimeMillis();
			int diffSecond = (int)((guild.getUpgradeEndTime() - currentMillis)/1000);
			int coolDownSecond = 0;
			if ( diffSecond > 0 ) {
				coolDownSecond = diffSecond;
			}
		  /**
		   * 取消冷却时间，立即升级的元宝价格
		  */
			int yuanbao = ScriptManager.getInstance().runScriptForInt(ScriptHook.GUILD_CALCULATE_CD_YUANBAO, 
					user, facility);
			builder.setCooldown(coolDownSecond);
			builder.setYuanbao(yuanbao);
			if ( user.getGuildMember() != null ) {
				builder.setMycredit(user.getGuildMember().getCredit());
			}
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
	}
	
	/**
	 * List all the guild number for given guild.
	 * @param guildId
	 * @return
	 */
	public final Collection<GuildMember> listGuildMember(User user) {
		Guild guild = user.getGuild();
		TreeSet<GuildMember> members = new TreeSet<GuildMember>();
		Jedis jedis = JedisFactory.getJedis();
		if ( guild != null ) {
			Collection<String> onlineSessionKeyStrs = listGuildMemberOnline(user.getGuildId());
			HashMap<UserId, SessionKey> map = new HashMap<UserId, SessionKey>();
			String key = StringUtil.concat(REDIS_GUILDMEMBER, user.getGuildId());
			for ( String sessionKeyStr : onlineSessionKeyStrs ) {
				SessionKey sessionKey = SessionKey.createSessionKeyFromHexString(sessionKeyStr);
				UserId userId = GameContext.getInstance().findUserIdBySessionKey(sessionKey);
				if ( userId != null ) {
					map.put(userId, sessionKey);
				} else {
					jedis.hdel(key, sessionKeyStr);
				}
			}
			Collection<GuildMember> all = queryGuildMemberByGuildId(guild.get_id());
			for ( GuildMember member : all ) {
				 if ( map.containsKey(member.getUserId()) ) {
					 member.setOnline(true);
				 }
				 members.add(member);
			}
		}
		return members;
	}
	
	/**
	 * List all the guild number for given guild.
	 * @param guildId
	 * @return
	 */
	public final Collection<String> listGuildMemberOnline(String guildId) {
		if ( guildId != null ) {
			Jedis jedis = JedisFactory.getJedis();
			String key = StringUtil.concat(REDIS_GUILDMEMBER, guildId);
			Map<String, String> map = jedis.hgetAll(key);
			return map.keySet();
		}
		return null;
	}
	
	/**
	 * List the guild's all role and privileges.
	 * @param user
	 * @return
	 */
	public final void listGuildPrivilege(User user, String guildId) {
		HashMap<GuildRole, Set<GuildPrivilege>> privilegeMap = 
				(HashMap<GuildRole, Set<GuildPrivilege>>)
				ScriptManager.getInstance().runScriptForObject(ScriptHook.GUILD_LIST_PRIVILEGE, user);
		
		BseGuildPrivilegeList.Builder builderList = BseGuildPrivilegeList.newBuilder();
		for ( GuildRole role : privilegeMap.keySet() ) {
			XinqiBseGuildPrivilegeList.GuildPrivilege.Builder builder = 
					XinqiBseGuildPrivilegeList.GuildPrivilege.newBuilder();
			builder.setRolekey(role.toString());
			builder.setRoledesc(role.getTitle());
			Set<GuildPrivilege> privileges = privilegeMap.get(role);
			if ( privileges != null ) {
				for ( GuildPrivilege priv : privileges ) {
					builder.addPrivilege(Text.text(priv.toString()));
				}
			}
			builderList.addPrivileges(builder.build());
		}
		
		GameContext.getInstance().writeResponse(user.getSessionKey(), builderList.build());
	}
		
	/**
	 * List all the guilds in system according the following rules:
      a．公会等级：公会等级越高，排名越靠前；
      b. 如果公会等级相同，则根据公会财富排列，财富越高，排名越靠前；
	 * @param startPos
	 * @param count
	 * @return
	 */
	public final Collection<Guild> listGuilds(User user, int startPos, int count) {
		TreeSet<Guild> guilds = new TreeSet<Guild>();
		List<DBObject> guildList = null;
		DBObject query = MongoDBUtil.createDBObject();
		query.put("status", GuildStatus.normal.toString());
		if ( count > 0 ) {
			guildList = MongoDBUtil.queryAllFromMongo(query, databaseName, namespace, COLL_NAME, null, sorts, startPos, count);
		} else {
			guildList = MongoDBUtil.queryAllFromMongo(query, databaseName, namespace, COLL_NAME, null, sorts);
		}
		
		BseGuildList.Builder builder = BseGuildList.newBuilder();
		for ( DBObject dbObj : guildList ) {
			Guild guild = (Guild)MongoDBUtil.constructObject(dbObj);
			guilds.add(guild);
		}
		/**
		 * Check the rank
		 *
		 */
		int rank = 0;
		for ( Guild guild : guilds ) {
			rank++;
			if ( guild.getRank() != rank ) {
				updateGuildRank(guild, rank);
			}

			if ( user.getGuildId() != null ) {
				builder.setMyGuildId(user.getGuildId());
			}
			builder.addGuildList(guild.toGuildSimpleInfo(rank));
		}
		/**
		 * Get user's apply list
		 */
		Map<String, String> applyMap = listUserGuildApply(user);
		for (Iterator iter = applyMap.keySet().iterator(); iter.hasNext();) {
			String guildId = (String) iter.next();
			String guildName = applyMap.get(guildId);
			builder.addRequestGuildId(guildId);
			builder.addRequestGuildNames(guildName);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildList, startPos, count);
		
		return guilds;
	}
	
	/**
	 * List guild all propData.
	 * @param user
	 * @return
	 */
	public final void listGuildStorage(User user, String guildId) {
		GuildBag guildBag = queryGuildBag(guildId);
		BseGuildBag.Builder builder = BseGuildBag.newBuilder();
		builder.setGuildID(guildId);
		int pew = 0;
		for ( String key : guildBag.getPropList().keySet() ) {
			PropData propData = guildBag.getPropList().get(key);
			if ( propData == null ) continue;
			propData.setPew(StringUtil.toInt(key, 0));
			builder.addBag(propData.toXinqiPropData(user, guildId));
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildBagList, guildId);
	}
	
	/**
	 * At the guild list UI, the user's applied guilds should be listed.
	 * @param user
	 * @return
	 */
	public final Map<String,String> listUserGuildApply(User user) {
		ArrayList<Apply> applys = new ArrayList<Apply>();
		DBObject query = MongoDBUtil.createDBObject();
		query.put(USERID_NAME, user.get_id().getInternal());
		List<DBObject> applyList = MongoDBUtil.queryAllFromMongo(
				query, databaseName, namespace, COLL_APPLY_NAME, applyFields);
		
		HashMap<String, String> map = new HashMap<String, String>();
		for ( DBObject dbObj : applyList ) {
			map.put(dbObj.get("guildId").toString(), dbObj.get("guildName").toString());
		}

		return map;
	}
	
	/**
	 * Mark the guild member logins to game.
	 * @param user
	 * @param member
	 */
	public final void markGuildMemberOnline(User user, boolean isOnline, boolean enterGuild) {
		Guild guild = user.getGuild();
		if ( guild != null ) {
			Jedis jedis = JedisFactory.getJedis();
			String key = StringUtil.concat(REDIS_GUILDMEMBER, guild.get_id());
			if ( isOnline ) {
				if ( enterGuild ) {
					jedis.hset(key, user.getSessionKey().toString(), Constant.ONE);
				} else {
					jedis.hset(key, user.getSessionKey().toString(), Constant.ZERO);
				}
			} else {
				jedis.hdel(key, user.getSessionKey().toString());
			}
		}
	}
	
	/**
	 * Move propData from guild storage to user's bag. This action need to check privilege first.
	 * @param user
	 * @param guild
	 * @param propData
	 * @return
	 */
	public final boolean moveGuildStorageToBag(User user, Guild guild, int storageIndex) {
		String message = null;
		boolean success = ScriptManager.getInstance().runScriptForBoolean(
				ScriptHook.GUILD_CHECK_PRVIILEGE, user, GuildPrivilege.takebag);
		GuildBag guildBag = null;
		PropData propData = null;
		if ( success ) {
			guildBag = queryGuildBag(guild.get_id());
			Jedis jedis = JedisFactory.getJedis();
			String roleName = lockGuildBag(user, jedis, guildBag.get_id());
			try {
				if ( roleName != null ) {
					success = false;
					message = Text.text("guild.bag.progress", roleName);
				} else {
					propData = guildBag.removePropData(storageIndex);
					if ( propData != null ) {
						success = saveGuildBag(user, guildBag);
						if ( success ) {
							user.getBag().addOtherPropDatas(propData);
							UserManager.getInstance().saveUserBag(user, false);
							message = Text.text("guild.bag.take.success", propData.getName());
							
							/**
							 * Save the bagEvent
							 */
							GuildBagEvent event = new GuildBagEvent();
							event.setGuildId(guild.get_id());
							event.setEvent(Text.text(propData));
							event.setRoleName(user.getRoleName());
							event.setTimestamp(System.currentTimeMillis());
							event.setUserId(user.get_id());
							event.setType(GuildBagEventType.TAKE);
							addGuildBagEvent(event);
							
							StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildBagTake, 
									user.getGuildId(), propData.toString(), storageIndex);
						} else {
							propData = null;
							success = false;
							message = Text.text("guild.bag.take.fail");
						}
						
					} else {
						success = false;
						message = Text.text("guild.bag.take.taken");
					}
				}
			} catch (Exception e) {
				logger.warn("Failed to moveGuildStorage to user bag.", e);
			} finally {
				unlockGuildBag(user, jedis, guildBag.get_id());
			}
		} else {
			success = false;
			message = Text.text("guild.bag.take.nopriv");
		}
		BseGuildBagTake.Builder builder = BseGuildBagTake.newBuilder();
		builder.setStatus(success?0:1);
		builder.setPew(storageIndex);
		if ( propData != null ) {
			builder.setPropData(propData.toXinqiPropData(user));
		}
		if ( message !=null ) {
			builder.setMessage(message);
		}
		/**
		if ( guildBag != null ) {
			for (PropData pd : guildBag.getPropList()) {
				builder.addBag(pd.toXinqiPropData());
			}
		}
		*/
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		return success;
	}
	
	/**
	 * Move bag's propData to storage. This action do not need checking privilege
	 * @param user
	 * @param guild
	 * @param propData
	 * @return
	 */
	public final boolean movePropDataToGuildStorage(User user, Guild guild, int pew) {
		String message = null;
		boolean success = false;
		PropData propData = user.getBag().getOtherPropData(pew);
		if ( propData != null ) {
			success = ScriptManager.getInstance().runScriptForBoolean(
					ScriptHook.GUILD_CHECK_PRVIILEGE, user, GuildPrivilege.takebag);
			if ( success ) {
				/**
				 * Remove the propData from bag first 
				 * because it will change the PropData's pew
				 * 2013-03-07
				 */
				user.getBag().removeOtherPropDatas(pew);
				GuildBag guildBag = queryGuildBag(guild.get_id());
				success = addGuildBagPropData(user, guildBag, propData);
				if ( success ) {
					message = Text.text("guild.bag.put.success", propData.getName());
					UserManager.getInstance().saveUserBag(user, false);
					/**
					 * Save the bagEvent
					 */
					GuildBagEvent event = new GuildBagEvent();
					event.setGuildId(guild.get_id());
					event.setEvent(Text.text(propData));
					event.setRoleName(user.getRoleName());
					event.setTimestamp(System.currentTimeMillis());
					event.setUserId(user.get_id());
					event.setType(GuildBagEventType.PUT);
					addGuildBagEvent(event);
					
					StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildBagPut, 
							user.getGuildId(), propData.toString(), pew);
				} else {
					message = Text.text("guild.bag.put.full");
				}
			} else {
				message = Text.text("guild.bag.put.nopriv");
			}
		} else {
			success = false;
			message = Text.text("guild.bag.put.null");
		}
		BseGuildBagPut.Builder builder = BseGuildBagPut.newBuilder();
		if ( success ) {
			builder.setStatus(0);
			builder.setPew(pew);
			builder.setPropData(propData.toXinqiPropData(user, guild.get_id()));
		} else {
			builder.setStatus(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildBagPut, propData.toString(), success, message);
		return success;
	}
	
	/**
	 * Process the given application.
	 * 
	 * @param owner
	 * @param apply
	 * @param approve
	 */
	public final boolean processGuildApply(User owner, Apply apply, boolean approve) {
		Guild guild = owner.getGuild();
		boolean success = false;
		String message = null;
		if ( guild != null ) {
			boolean hasPrivilege = ScriptManager.getInstance().runScriptForBoolean(ScriptHook.GUILD_CHECK_PRVIILEGE, 
					owner, GuildPrivilege.recruit);
			if ( hasPrivilege ) {
				success = doProcessGuildApply(apply, approve, guild);
			} else {
				message = Text.text("guild.apply.nopriv");
			}
		} else {
			message = Text.text("guild.apply.noguild");
		}
		
		BseGuildApplyProcess.Builder builder = BseGuildApplyProcess.newBuilder();
		if ( success ) {
			builder.setStatus(0);
		} else {
			builder.setStatus(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
		} else {
			if ( success ) {
				message = Text.text("guild.owner.apply.success");
				builder.setMessage(message);
			}
		}
		GameContext.getInstance().writeResponse(owner.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(owner, StatAction.GuildProcessApply, success, apply.getUserId(), approve);
		return success;
	}
	
	/**
	 * Process the applications in a batch.
				1. 玩家点击“全部同意”则系统按照申请列表玩家顺序加入到公会中，如果公会人满，则剩余的成员都不再加入到公会，并弹出提示通知有招收成员权限的玩家；
          公会人员已满，无法继续招收成员！
        2. 玩家申请被同意，则弹出提示：
          申请已被同意，恭喜您加入XXX公会！
        3. 玩家申请被拒绝，则弹出提示：
          您申请加入XXX公会被拒绝！
        4. 只有有权限的成员才可以招收/拒绝申请玩家，否则按钮为不可点击状态
	 * 
	 * @param owner
	 * @param apply
	 * @param approve
	 */
	public final boolean processMultiGuildApply(User owner, boolean approve) {
		Guild guild = owner.getGuild();
		boolean success = false;
		String message = null;
		if ( guild != null ) {
			boolean hasPrivilege = ScriptManager.getInstance().runScriptForBoolean(ScriptHook.GUILD_CHECK_PRVIILEGE, 
					owner, GuildPrivilege.recruit);
			if ( hasPrivilege ) {
				String subject, content;
				Collection<Apply> applies = listGuildApplys(owner, guild.get_id(), 0, -1);
				for ( Apply apply : applies ) {
					success = doProcessGuildApply(apply, approve, guild);
					if ( !success ) {
						break;
					}
				}
			} else {
				message = Text.text("guild.apply.nopriv");
			}
		} else {
			message = Text.text("guild.apply.noguild");
		}
		
		BseGuildApplyProcess.Builder builder = BseGuildApplyProcess.newBuilder();
		if ( success ) {
			builder.setStatus(0);
		} else {
			builder.setStatus(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
		} else {
			message = Text.text("guild.owner.apply.success");
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(owner.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(owner, StatAction.GuildProcessApply2, success, approve);
		return success;
	}
	
	/**
	 * Check if the guild name exists.
	 * 
	 * @param guildId
	 * @return
	 */
	public Apply queryGuildApply(String guildId, UserId userId) {
		DBObject query = createDBObject(GUILDID_NAME, guildId);
		query.put(USERID_NAME, userId.getInternal());
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, COLL_APPLY_NAME, null);
		if ( dbObj != null ) {
			Apply apply = (Apply)MongoDBUtil.constructObject(dbObj);
			return apply;
		}
		return null;
	}
	
	/**
	 * Check if the guild name exists.
	 * 
	 * @param guildId
	 * @return
	 */
	public Collection<Apply> queryGuildApply(UserId userId) {
		DBObject query = createDBObject();
		query.put(USERID_NAME, userId.getInternal());
		List<DBObject> dbObjs = MongoDBUtil.queryAllFromMongo(query, databaseName, namespace, COLL_APPLY_NAME, null);
		ArrayList<Apply> list = new ArrayList<Apply>();
		for ( DBObject dbObj : dbObjs ) {
			Apply apply = (Apply)MongoDBUtil.constructObject(dbObj);
			list.add(apply);
		}
		return list;
	}
	
	/**
	 * Check if the guild name exists.
	 * 
	 * @param guildId
	 * @return
	 */
	public int queryGuildApplyCount(String guildId) {
		DBObject query = createDBObject();
		query.put(GUILDID_NAME, guildId);
		int count = (int)MongoDBUtil.countQueryResult(query, databaseName, namespace, COLL_APPLY_NAME);
		return count;
	}

	/**
	 * Query the GuildBag for given guild.
	 * @param guildId
	 * @return
	 */
	public GuildBag queryGuildBag(String guildId) {
		DBObject query = createDBObject();
		query.put(INDEX_NAME, guildId);
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, COLL_BAG_NAME, null);
		GuildBag guildBag = (GuildBag)MongoDBUtil.constructObject(dbObj);
		//Check the current count.
		if ( guildBag != null ) {
			if ( guildBag.getCount() != guildBag.getPropList().size() ) {
				guildBag.setCount(guildBag.getPropList().size());
			}
		}
		return guildBag;
	}
	
	/**
	 * Query the GuildBag for given guild.
	 * @param guildId
	 * @return
	 */
	public PropData queryGuildBagPropData(String guildId, String propDataKey) {
		DBObject query = createDBObject();
		query.put(INDEX_NAME, guildId);
		DBObject field = createDBObject("propList.".concat(propDataKey), Constant.ONE);
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, COLL_BAG_NAME, field);
		if ( dbObj != null ) {
			DBObject pdObj = (DBObject)dbObj.get("propList");
			if ( pdObj != null ) {
				pdObj = (DBObject)pdObj.get(propDataKey);
			}
			PropData propData = (PropData)MongoDBUtil.constructObject(pdObj);
			//Check the current count.
			return propData;
		} 
		return null;
	}
	
	/**
	 * Query how many bag events in for a guild bag.
	 * @param userId
	 * @param guildId
	 * @return
	 */
	public int queryGuildBagEventCount(String guildId) {
		DBObject query = MongoDBUtil.createDBObject(GUILDID_NAME, guildId);
		int count = (int)MongoDBUtil.countQueryResult(query, databaseName, namespace, COLL_BAGEVENT_NAME);
		return count;
	}
	
	/**
	 * Query all the guildbag events for given guild.
	 * @param guildId
	 * @return
	 */
	public Collection<GuildBagEvent> queryGuildBagEvents(String guildId) {
		DBObject query = MongoDBUtil.createDBObject(GUILDID_NAME, guildId);
		Collection<DBObject> objs = MongoDBUtil.queryAllFromMongo(
				query, databaseName, namespace, COLL_BAGEVENT_NAME, 
				null, bagEventSorts);
		ArrayList<GuildBagEvent> events = new ArrayList<GuildBagEvent>(objs.size());
		for ( DBObject obj : objs ) {
			GuildBagEvent event = (GuildBagEvent)MongoDBUtil.constructObject(obj);
			events.add(event);
		}
		return events;
	}

	/**
	 * Query the user account by id.
	 * @param accountId
	 * @return
	 */
	public Guild queryGuildById(String guildId) {
		Guild guild = null;
		DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guildId);
		guild = (Guild)MongoDBUtil.queryObjectFromMongo(query, databaseName, namespace, COLL_NAME, null);
		return guild;
	}
	
	/**
	 * Query the user account by id.
	 * @param accountId
	 * @return
	 */
	public Collection<GuildMember> queryGuildMemberByGuildId(String guildId) {
		DBObject query = MongoDBUtil.createDBObject(GUILDID_NAME, guildId);
		ArrayList<GuildMember> members = new ArrayList<GuildMember>();
		List<DBObject> dbObjs = MongoDBUtil.queryAllFromMongo(
				query, databaseName, namespace, COLL_MEMBER_NAME, null, memSorts);
		for ( DBObject dbObj : dbObjs ) {
			members.add((GuildMember)MongoDBUtil.constructObject(dbObj));
		}
		return members;
	}
	
	/**
	 * Query the user account by id.
	 * @param accountId
	 * @return
	 */
	public GuildMember queryGuildMemberByUserId(UserId userId) {
		GuildMember member = null;
		DBObject query = MongoDBUtil.createDBObject(USERID_NAME, userId.getInternal());
		member = (GuildMember)MongoDBUtil.queryObjectFromMongo(query, databaseName, namespace, COLL_MEMBER_NAME, null);
		return member;
	}
	
	/**
	 * Query the user account by id.
	 * @param accountId
	 * @return
	 */
	public Collection<UserId> queryGuildMemberIdByGuildId(String guildId) {
		DBObject query = MongoDBUtil.createDBObject(GUILDID_NAME, guildId);
		List<UserId> members = new ArrayList<UserId>();
		List<DBObject> dbObjs = MongoDBUtil.queryAllFromMongo(
				query, databaseName, namespace, COLL_MEMBER_NAME, MongoDBUtil.createDBObject(USERID_NAME, Constant.ONE));
		for ( DBObject dbObj : dbObjs ) {
			members.add(UserId.fromBytes((byte[])dbObj.get(USERID_NAME)));
		}
		return members;
	}
	
	/**
	 * Query the guild for given user
	 * @param user
	 * @return
	 */
	public final Guild queryUserGuild(UserId userId) {
		DBObject query = createDBObject();
		query.put(USERID_NAME, userId.getInternal());
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, COLL_NAME, null);
		Guild guild = (Guild)MongoDBUtil.constructObject(dbObj);
		return guild;
	}
		
	/**
	 * The user will quit from his/her guild.
	 * 
	 * 您确定要退出XXX公会吗？
	 * 
	 * 1. 会长不可以退出公会
	 * 2. 玩家退出公会清除所有个人贡献：剩余个人贡献及总贡献

	 * @param user
	 * @param guildId
	 * @return
	 */
	public final boolean quitGuild(User user, String guildId) {
		boolean success = false;
		String message = null;
		Guild guild = user.getGuild();
		if ( guild == null ) {
			message = Text.text("guild.exit.notinguild");
		} else {
			if ( guild.getUserId().equals(user.get_id()) ) {
				//会长不可以退出公会
				message = Text.text("guild.exit.owner");
			} else {
				removeGuildMember(guild, user.get_id(), guildId);
				user.setGuild(null);
				user.setGuildId(null);
				user.setGuildMember(null);
				UserManager.getInstance().saveUser(user, false);
				message = Text.text("guild.exit.success", guild.getTitle());
				success = true;
			}
		}
		
		BseExitGuild.Builder builder = BseExitGuild.newBuilder();
		if ( success ) {
			builder.setResult(0);
			/**
			 * Remove the user from guild online members
			 */
			markGuildMemberOnline(user, false, false);
		} else {
			builder.setResult(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		return success;
	}
	
	/**
	 * Remove the guild from system.
	 * @param account
	 * @return
	 */
	public boolean removeGuild(String guildId) {
		try {
			//Delete the guild
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guildId);
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_NAME, isSafeWrite);
			//Delete the related applys
			query = MongoDBUtil.createDBObject(GUILDID_NAME, guildId);
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_APPLY_NAME, isSafeWrite);
			//Delete the guild member
			query = MongoDBUtil.createDBObject(GUILDID_NAME, guildId);
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_MEMBER_NAME, isSafeWrite);
			//Delete the guild bag
			query = MongoDBUtil.createDBObject(INDEX_NAME, guildId);
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_BAG_NAME, isSafeWrite);
			//Delete the guild bag event
			query = MongoDBUtil.createDBObject(GUILDID_NAME, guildId);
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_BAGEVENT_NAME, isSafeWrite);
		} catch (Exception e) {
			logger.warn("Failed to delete guild", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Remove the guild from system.
	 * @param account
	 * @return
	 */
	public boolean removeGuildApply(UserId userId, String guildId) {
		try {
			DBObject query = MongoDBUtil.createDBObject(USERID_NAME, userId.getInternal());
			query.put(GUILDID_NAME, guildId);
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_APPLY_NAME, isSafeWrite);
		} catch (Exception e) {
			logger.warn("Failed to delete guild", e);
			return false;
		}
		return true;
	}
		
	/**
	 * Remove the guild from system.
	 * @param account
	 * @return
	 */
	public boolean removeGuildBag(String guildId) {
		try {
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guildId);
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_BAG_NAME, isSafeWrite);
			//remove lock key if exist
			Jedis jedis = JedisFactory.getJedis();
			String lockKey = StringUtil.concat(REDIS_GUILDBAG_LOCK, guildId);
			jedis.del(lockKey);
		} catch (Exception e) {
			logger.warn("Failed to delete guild", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Remove the user from given guild.
	 * 
	 * @param userId
	 * @param guildId
	 * @return
	 */
	public final boolean removeGuildMember(Guild guild, UserId userId) {
		boolean success = false;
		if ( userId == null ) return success;
		DBObject query = MongoDBUtil.createDBObject(USERID_NAME, userId.getInternal());
		try {
			MongoDBUtil.removeDocument(databaseName, namespace, COLL_MEMBER_NAME, query);
			success = true;
			updateGuildCount(guild, 1);
		} catch (Exception e) {
			logger.warn("Failed to remove guild member.", e);
		}
		return success;
	}
	
	/**
	 * Remove the user from given guild.
	 * 
	 * @param userId
	 * @param guildId
	 * @return
	 */
	public final boolean removeGuildMember(Guild guild, UserId userId, String guildId) {
		boolean success = false;
		if ( userId == null || guildId == null ) return success;
		/**
		 * Remove the guild member
		 */
		DBObject query = MongoDBUtil.createDBObject(USERID_NAME, userId.getInternal());
		query.put(GUILDID_NAME, guildId);
		try {
			MongoDBUtil.removeDocument(databaseName, namespace, COLL_MEMBER_NAME, query);
			success = true;
			updateGuildCount(guild, -1);
		} catch (Exception e) {
			logger.warn("Failed to remove guild member.", e);
		}
		return success;
	}
	
	/**
	 * Save the gulid to database.
	 * @param guild
	 */
	public final boolean saveGuild(Guild guild) {
		try {
			MapDBObject dbObj = MongoDBUtil.createMapDBObject(guild);
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guild.get_id());
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_NAME, isSafeWrite);
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Save the gulid to database.
	 * @param guild
	 */
	public final boolean saveGuildApply(Apply apply) {
		try {
			MapDBObject dbObj = MongoDBUtil.createMapDBObject(apply);
			DBObject query = null; //MongoDBUtil.createDBObject();
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_APPLY_NAME, isSafeWrite);
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Save the propData into user's storage in guild bag.
	 * Since many guild members may update the guild bag at the same time,
	 * in oder to prevent lose something, we need to use the Redis as a 
	 * global concurrent lock for that.
	 * 
	 * @param user
	 * @param propData
	 */
	public boolean saveGuildBag(User user, GuildBag guildBag) {
		try {
			/**
			 * Check the guildBag version first to check if someone else
			 * are changing it too.
			 */
			DBObject queryForVersion = MongoDBUtil.createDBObject(INDEX_NAME, guildBag.get_id());
			DBObject versionField = MongoDBUtil.createDBObject(VERSION_NAME, Constant.ONE);
			DBObject versionObj = MongoDBUtil.queryFromMongo(
					queryForVersion, databaseName, namespace, COLL_BAG_NAME, versionField);
			long currentVersion = 0l;
			if ( versionObj != null ) {
				currentVersion = (Long)versionObj.get(VERSION_NAME);
			}
			if ( guildBag.getVersion() != currentVersion ) {
				/**
				 * Someone else are changing the underlying guild bag. 
				 * Need to refresh first.
				 */
				String message = Text.text("guild.bag.obsolete");
				GameContext.getInstance().writeResponse(user.getSessionKey(), message);
				return false;
			} else {
				/**
				 * Lock the guildbag and save all the data into it.
				 */
				Jedis jedis = JedisFactory.getJedis();
				String roleName = lockGuildBag(user, jedis, guildBag.get_id());
				if ( roleName != null && !roleName.equals(user.getRoleName()) ) {
					String message = Text.text("guild.bag.progress", roleName);
					SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 3000);
					return false;
				} else {
					try {
						return saveGuildBagWithoutCheck(guildBag);
					} finally {
						unlockGuildBag(user, jedis, guildBag.get_id());
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
			return false;
		}
	}

	/**
	 * @param user
	 * @param guildBag
	 * @param jedis
	 * @return
	 */
	public boolean saveGuildBagWithoutCheck(GuildBag guildBag) {
		DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guildBag.get_id());
		guildBag.setVersion(System.currentTimeMillis());
		DBObject dbObj = MongoDBUtil.createMapDBObject(guildBag);
		MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_BAG_NAME, isSafeWrite);
		return true;
	}

	/**
	 * Save a new GuildMemeber into guildmembers collection
	 * @param member
	 * @return
	 */
	public final boolean saveGuildMember(GuildMember member) {
		try {
			MapDBObject dbObj = MongoDBUtil.createMapDBObject(member);
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, member.get_id());
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_MEMBER_NAME, isSafeWrite);
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
			return false;
		}
		return true;
	}

	// ------------------------------------------------------------ Private methods
	
	/**
	 * Search the guild by given regex name
	 * @param guildName
	 * @return
	 */
	public final Collection<Guild> searchGuild(String guildName, int startPos, int count) {
		DBObject query = createDBObject();
		DBObject cond =createDBObject("$regex", guildName);
		cond.put("$options", "i");
		query.put(GUILDTITLE_NAME, cond);
		List<DBObject> dbObjs = null;
		if ( count > 0 ) {
			dbObjs = MongoDBUtil.queryAllFromMongo(
				query, databaseName, namespace, COLL_NAME, null, sorts, startPos, count);
		} else {
			dbObjs = MongoDBUtil.queryAllFromMongo(
				query, databaseName, namespace, COLL_NAME, null, sorts);
		}
		ArrayList<Guild> list = new ArrayList<Guild>();
		for ( DBObject dbObj : dbObjs ) {
			Guild guild = (Guild)MongoDBUtil.constructObject(dbObj);
			list.add(guild);
		}
		return list;
	}
	
	/**
	 * Search the guild by given regex.
	 * 
	 * @param guildId
	 * @param roleName
	 * @return
	 */
	public final Collection<GuildMember> searchGuildMember(String guildId, String roleName) {
		DBObject query = MongoDBUtil.createDBObject(GUILDID_NAME, guildId);
		DBObject cond =createDBObject("$regex", roleName);
		cond.put("$options", "i");
		query.put(ROLENAME_NAME, cond);
		List<GuildMember> members = new ArrayList<GuildMember>();
		List<DBObject> dbObjs = MongoDBUtil.queryAllFromMongo(
				query, databaseName, namespace, COLL_MEMBER_NAME, null);
		for ( DBObject dbObj : dbObjs ) {
			members.add((GuildMember)MongoDBUtil.constructObject(dbObj));
		}
		return members;
	}
	
	/**
	 * Send a message to given user. If the user is online, send him sysmessge.
	 * If the user is offline, send him a mail.
	 * 
	 * @param userId
	 * @param subject
	 * @param content
	 */
	public void sendMessageToUser(UserId userId, String subject, String content) {
		SessionKey sessionKey = GameContext.getInstance().findSessionKeyByUserId(userId);
		if ( sessionKey != null ) {
			//User is online
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					sessionKey, content, 5000);
			ChatManager.getInstance().sendPrivateSysChat(sessionKey, content);
		} else {
			//User is offline
			MailMessageManager.getInstance().sendAdminMail(userId, subject, content, null);
			logger.debug("Send mail {} to userId:{}", content, userId.toString());
		}
	}
	
	/**
	 * The guild officer send guild invitation to another user.
	 * 
	 * We can add a new entry in the user quick dialog that can be popup from chat window.
	 * 
		输入玩家昵称后，点击“邀请”后
		1.系统搜索该玩家，如果没有则弹出提示：
		  没有找到该玩家，请确认昵称是否正确！
		2.如果该玩家不在线，则弹出提示：
		  该玩家目前不在线，无法发出邀请！
		3.如果该玩家在线，则弹出提示：
		  邀请信息已发送，等待玩家确认！
		4.如果玩家拒绝，则弹出提示：
		  xxx拒绝了您的入帮邀请！

	 * 
	 * @param owner
	 * @param targetUser
	 */
	public final void sendRecruitInvite(final User owner, final String targetUserIdStr) {
		/**
		 * Check privilege
		 */
		boolean success = ScriptManager.getInstance().runScriptForBoolean(
				ScriptHook.GUILD_CHECK_PRVIILEGE, owner, GuildPrivilege.recruit);
		String message = null;
		if ( !success ) {
			success = false;
			message = Text.text("guild.recruit.nopriv");
		}
		/**
		 * Check the guild count
		 */
		Guild guild = owner.getGuild();
		if ( guild.getCount() >= getGuildMaxCount(guild) ) {
			success = false;
			message = Text.text("guild.recruit.full");
		}
		/**
		 * Check if the target user is already in a guild
		 */
		final UserId targetUserId = UserId.fromString(targetUserIdStr);
		if ( success ) {
			String guildId = UserManager.getInstance().queryUserGuildId(targetUserId);
			if ( guildId != null ) {
				success = false;
				message = Text.text("guild.recruit.already");
			}
		}
		/**
		 * Check if the target user is himself
		 */
		if ( success ) {
			if ( owner.get_id().equals(targetUserId) ) {
				success = false;
				message = Text.text("guild.recruit.self");
			}
		}
		if ( success ) {
			final SessionKey targetSessionKey = GameContext.getInstance().findSessionKeyByUserId(targetUserId);
			if ( targetSessionKey != null ) {
				//The user is online
				String prompt = Text.text("guild.recruit.prompt", owner.getRoleName(), owner.getGuild().getTitle());
				ConfirmManager.getInstance().sendConfirmMessage(owner, targetSessionKey, prompt, "guild.recruit", 
						new ConfirmManager.ConfirmCallback() {
					
					@Override
					public void callback(User user, int selected) {
						TargetUser target = findTargetUser(targetUserId);
						User targetUser = target.user;
						if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
							boolean success = true;
							if ( !target.atRemoteServer ) {
								//目标用户在本机
								Status status = joinGuild(targetUser, user.getGuild(), true);
								success = status.isSuccess();
							} else {
								//目标用户在远程服务器
								BceUserSync.Builder builder = BceUserSync.newBuilder();
								builder.setMode(4);
								builder.setObject(owner.getGuildId());
								GameContext.getInstance().proxyToRemoteGameServer(
										target.sessionKey, target.gameServerId, builder.build());
							}
							if ( success ) {
								String backMessage = Text.text("guild.recruit.backmsg", targetUser.getRoleName());
								SysMessageManager.getInstance().sendClientInfoRawMessage(
									owner.getSessionKey(), backMessage, 3000);
							} else {
								String backMessage = Text.text("guild.recruit.full");
								SysMessageManager.getInstance().sendClientInfoRawMessage(
										owner.getSessionKey(), backMessage, 3000);
							}
						} else {
							String backMessage = Text.text("guild.recruit.cancel", targetUser.getRoleName());
							SysMessageManager.getInstance().sendClientInfoRawMessage(
									owner.getSessionKey(), backMessage, 3000);
						}
					}
				});
				success = true;
				message = Text.text("guild.recruit.success");
			} else {
				//user is offline
				String prompt = Text.text("guild.recruit.prompt", owner.getRoleName(), owner.getGuild().getTitle());
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("guildId", guild.get_id());
				params.put(BulletinManager.PROMPT_MESSAGE, prompt);
				BulletinManager.getInstance().sendPersonalConfirmMessage(targetUserIdStr, 
						ScriptHook.GUILD_CONFIRM_INVITE, params, 86400*7);
				success = true;
				message = Text.text("guild.recruit.success");
			}
		}
		
		BseGuildInvite.Builder builder = BseGuildInvite.newBuilder();
		if ( success ) {
			builder.setStatus(0);
		} else {
			builder.setStatus(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(owner.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(
				owner, StatAction.GuildInvite, targetUserIdStr, success, message);
	}

	/**
	 * Transfer the guild to another user.
			您确认将公会转让给XXX吗？
			确定则将公会转让给该玩家；取消则不转让；
			会长转让后，7天内不可再将会长转移给其他玩家！
			当玩家在7天内再次转移时，弹出提示：
			您成为会长不满7天，无法转移会长！
	 * 
	 * @param guild
	 * @param owner
	 * @param targetUser
	 * @return
	 */
	public final boolean transferGuild(final User owner, String targetUserIdStr) {
		boolean success = false;
		String message = null;
		/**
		 * 检查当前用户为公会会长
		 */
		final Guild guild = owner.getGuild();
		if ( !guild.getUserId().equals(owner.get_id()) ) {
			success = false;
			message = Text.text("guild.transfer.notowner");
		} else {
			success = true;
		}
		/**
		 * 检查当前会长就职需满7天时间
		 */
		if ( success ) {
			long currentMillis = System.currentTimeMillis();
			int diff = (int)((currentMillis - guild.getOwnerJoinMillis())/1000);
			if ( diff < 86400*7) {
				success = false;
				message = Text.text("guild.transfer.notdays");
			} else {
				success = true;
			}
		}
		if ( success ) {
			success = false;
			final UserId targetUserId = UserId.fromString(targetUserIdStr);
			/**
			 * 检查目标玩家也是公会的会员
			 */
			GuildMember targetMember = queryGuildMemberByUserId(targetUserId);
			if ( targetMember == null ) {
				success = false;
				message = Text.text("guild.transfer.notmember");
			} else {
				final SessionKey targetSessionKey = GameContext.getInstance().findSessionKeyByUserId(targetUserId);
				if ( targetSessionKey != null ) {
					//The user is online
					String prompt = Text.text("guild.transfer.prompt", owner.getRoleName(), owner.getGuild().getTitle());
					ConfirmManager.getInstance().sendConfirmMessage(owner, targetSessionKey, prompt, "guild.transfer", 
							new ConfirmManager.ConfirmCallback() {
						
						@Override
						public void callback(User user, int selected) {
							TargetUser target = findTargetUser(targetUserId);
							User targetUser = target.user;
							if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
								boolean success = true;
								try {
									GuildMember guildMember = owner.getGuildMember();
									GuildMember targetMember = targetUser.getGuildMember();
									guild.setUserId(targetUserId);
									guild.setRoleName(targetUser.getRoleName());
									guild.setOwnerJoinMillis(System.currentTimeMillis());
									guildMember.setRole(GuildRole.member);
									targetMember.setRole(GuildRole.chief);
									saveGuild(guild);
									saveGuildMember(guildMember);
									saveGuildMember(targetMember);
									
								  //Ask all online users to refresh 
									syncGuildWithOnlineUsers(guild);
									
									StatClient.getIntance().sendDataToStatServer(
											owner, StatAction.GuildTransfer, owner.getGuildId(), targetUser.getRoleName(), true);
								} catch (Exception e) {
									success = false;
									logger.warn("Failed to transfer guild owner", e);
								}
								/*
								if ( target.atRemoteServer ) {
									//目标用户在远程,需要刷新
									BceUserSync.Builder builder = BceUserSync.newBuilder();
									builder.setMode(6);
									GameContext.getInstance().proxyToRemoteGameServer(
											target.sessionKey, target.gameServerId, builder.build());
								}
								*/
								if ( success ) {
									String backMessage = Text.text("guild.transfer.backmsg", targetUser.getRoleName());
									SysMessageManager.getInstance().sendClientInfoRawMessage(
										owner.getSessionKey(), backMessage, 3000);
								} else {
									String backMessage = Text.text("guild.transfer.fail");
									SysMessageManager.getInstance().sendClientInfoRawMessage(
											owner.getSessionKey(), backMessage, 3000);
								}
							} else {
								String backMessage = Text.text("guild.transfer.cancel", targetUser.getRoleName());
								SysMessageManager.getInstance().sendClientInfoRawMessage(
										owner.getSessionKey(), backMessage, 3000);
								
								StatClient.getIntance().sendDataToStatServer(
										owner, StatAction.GuildTransfer, targetUser.getRoleName(), false);
							}
						}
					});
					success = true;
					message = Text.text("guild.recruit.pending");
				} else {
					success = false;
					message = Text.text("guild.transfer.offline");
				}
			}
		}
		
		BseGuildTransfer.Builder builder = BseGuildTransfer.newBuilder();
		if ( success ) {
			builder.setStatus(0);
			message = Text.text("guild.transfer.pending");
		} else {
			builder.setStatus(1);
		}
		if ( message != null ) {
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(owner.getSessionKey(), builder.build());
		return success;
	}
	
	/**
	 * Update the max count for given guild's bag.
	 * @param user
	 * @param guildBag
	 * @param max
	 * @return
	 */
	public boolean updateGuildBagMaxCount(User user, GuildBag guildBag, int maxCount) {
		try {
			//do the insert
			guildBag.setMax(maxCount);
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guildBag.get_id());
			DBObject dbObj = MongoDBUtil.createDBObject("$set", MongoDBUtil.createDBObject(MAX_NAME, maxCount));
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_BAG_NAME, isSafeWrite);
			return true;
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
		}
		return false;
	}
	
	/**
	 * 
	 * @param guild
	 * @param addedCount 1 or -1
	 * @return
	 */
	public final boolean updateGuildCount(Guild guild, int addedCount) {
		try {
			int finalCount = guild.getCount() + addedCount;
			if ( finalCount < 0 ) finalCount = 0;
			guild.setCount(finalCount);
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guild.get_id());
			DBObject dbObj = MongoDBUtil.createDBObject("$inc", MongoDBUtil.createDBObject("count", addedCount));
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_NAME, isSafeWrite);
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Update the guild's rank.
	 * @param guild
	 * @param newRank
	 * @return
	 */
	public final boolean updateGuildRank(Guild guild, int newRank) {
		if ( guild.getRank() != newRank ) {
			guild.setRank(newRank);
		}
		try {
			DBObject dbObj = MongoDBUtil.createDBObject("$set", MongoDBUtil.createDBObject(RANK_NAME, newRank));
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, guild.get_id());
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_NAME, isSafeWrite);
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Convert the guild member to string if exists
	 * @param member
	 * @return
	 */
	public String convertGuildMemberToString(GuildMember member) {
		DBObject dbObj = MongoDBUtil.createMapDBObject(member);
		String str = JSON.serialize(dbObj);
		return str;
	}
	
	/**
	 * Convert the guild member to string if exists
	 * @param member
	 * @return
	 */
	public GuildMember convertStringToGuildMember(String memberStr) {
		DBObject dbObj = (DBObject)JSON.parse(memberStr);
		GuildMember member = (GuildMember)MongoDBUtil.constructObject(dbObj);
		return member;
	}
	
	/**
	 * Convert the guild member to string if exists
	 * @param member
	 * @return
	 */
	public String convertGuildBagToString(GuildBag member) {
		DBObject dbObj = MongoDBUtil.createMapDBObject(member);
		String str = JSON.serialize(dbObj);
		return str;
	}
	
	/**
	 * Convert the guild member to string if exists
	 * @param member
	 * @return
	 */
	public GuildBag convertStringToGuildBag(String memberStr) {
		DBObject dbObj = (DBObject)JSON.parse(memberStr);
		GuildBag bag = (GuildBag)MongoDBUtil.constructObject(dbObj);
		return bag;
	}
	
	/**
	 * Convert the guild member to string if exists
	 * @param guild
	 * @return
	 */
	public String convertGuildToString(Guild guild) {
		DBObject dbObj = MongoDBUtil.createMapDBObject(guild);
		String str = JSON.serialize(dbObj);
		return str;
	}
	
	/**
	 * Convert the guild member to string if exists
	 * @param member
	 * @return
	 */
	public Guild convertStringToGuild(String guildStr) {
		DBObject dbObj = (DBObject)JSON.parse(guildStr);
		Guild guild = (Guild)MongoDBUtil.constructObject(dbObj);
		return guild;
	}
	
	/**
	 * Get the target user.
	 * @param userId
	 * @return
	 */
	private TargetUser findTargetUser(UserId userId) {
		TargetUser targetUser = new TargetUser();
		SessionKey userSessionKey = GameContext.getInstance().findSessionKeyByUserId(userId);
		if ( userSessionKey != null ) {
			//User is online
			targetUser.sessionKey = userSessionKey;
			String gameServerId = GameContext.getInstance().getSessionManager().findUserGameServerId(userSessionKey);
			if ( GameContext.getInstance().getGameServerId().equals(gameServerId) )  {
				//User is at local server
				targetUser.user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
			} else {
				//User is at remote server
				targetUser.user = UserManager.getInstance().queryUser(userId);
				targetUser.atRemoteServer = true;
				targetUser.gameServerId = gameServerId;
			}
		}
		if ( targetUser.user == null ) {
			targetUser.user = UserManager.getInstance().queryUser(userId);
		}
		return targetUser;
	}
	
	/**
	 * @param guild
	 * @return
	 */
	private int getGuildBagMaxCount(Guild guild) {
		int[] maxCounts = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.GUILD_STORAGE_SIZE);
		int guildLevel = guild.getLevel();
		int maxCount = maxCounts[guildLevel-1];
		return maxCount;
	}

	/**
	 * @param guild
	 * @return
	 */
	private int getGuildMaxCount(Guild guild) {
//		int[] maxCounts = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.GUILD_LEVEL_MAXCOUNT);
//		int guildLevel = guild.getLevel();
//		int maxCount = maxCounts[guildLevel-1];
//		return maxCount;
		return guild.getMaxCount();
	}
	
	/**
	 * @param announcement
	 * @param guild
	 */
	private String processAnnounce(String announcement) {
		if ( announcement != null ) {
			if ( announcement.length() > 128 ) {
				announcement = StringUtil.concat(announcement.substring(0, 125), "...");
			}
		}
		return announcement;
	}
	
	
	/**
	 * Lock the guildBag
	 * @param user
	 * @param jedis
	 * @param guildBagId
	 * @return
	 */
	private String lockGuildBag(User user, Jedis jedis, String guildBagId) {
		String lockKey = StringUtil.concat(REDIS_GUILDBAG_LOCK, guildBagId);
		Long result = jedis.setnx(lockKey, user.getRoleName());
		if ( result!= null && result.longValue() == 1 ) {
			jedis.set(lockKey, user.getRoleName());
			jedis.expire(lockKey, 10);
			return null;
		} else {
			return jedis.get(lockKey);
		}
	}
	
	/**
	 * 
	 * @param user
	 * @param jedis
	 * @param guildBagId
	 * @return
	 */
	private void unlockGuildBag(User user, Jedis jedis, String guildBagId) {
		try {
			String lockKey = StringUtil.concat(REDIS_GUILDBAG_LOCK, guildBagId);
			jedis.del(lockKey);
		} catch (Exception e) {
			logger.warn("Failed to unlock the guildBag:{}. exception:{}", guildBagId, e.getMessage());
		}
	}
	
	/**
	 * Ask all the guild users to refresh guild data
	 * 6: 公会职位变更的刷新协议，刷新公会和公会成员信息
	 * 
	 * @param guild
	 */
	public void syncGuildWithOnlineUsers(Guild guild) {
		int mode = 6;
		Collection<String> allOnlineMembers = listGuildMemberOnline(guild.get_id());
		Jedis jedis = JedisFactory.getJedis();
		String key = StringUtil.concat(REDIS_GUILDMEMBER, guild.get_id());
		for ( String sessionKeyStr : allOnlineMembers ) {
			SessionKey sessionKey = SessionKey.createSessionKeyFromHexString(sessionKeyStr);
			User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
			if ( user != null ) {
				/**
				 * 公会职位变更的刷新协议，刷新公会和公会成员信息
				 */
					guild = GuildManager.getInstance().queryGuildById(user.getGuildId());
					user.setGuild(guild);
					GuildMember member = GuildManager.getInstance().queryGuildMemberByUserId(user.get_id());
					user.setGuildMember(member);
			} else {
				String machineId = GameContext.getInstance().findMachineId(sessionKey);
				if ( machineId != null ) {
					syncGuildWithOnlineUsers(sessionKey, machineId, mode, null);
				} else {
					jedis.hdel(key, sessionKeyStr);
				}
			}
		}
	}
	
	/**
	 * 4: 加入公会的刷新协议
	 * 5: 解散/退出/被开除的公会的刷新协议
	 * 6: 公会职位变更的刷新协议，刷新公会和公会成员信息
	 * @param guild
	 */
	public void syncGuildWithOnlineUsers(SessionKey userSessionKey, String gameServerId, int mode, String value) {
		BceUserSync.Builder builder = BceUserSync.newBuilder();
		builder.setMode(mode);
		if ( value != null ) {
			builder.setObject(value);
		}
		GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, builder.build());
	}
}
