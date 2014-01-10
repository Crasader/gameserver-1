package com.xinqihd.sns.gameserver.db.mongo;

import static com.xinqihd.sns.gameserver.config.Constant.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.JndiContextKey;
import com.xinqihd.sns.gameserver.config.ChargePojo;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.CurrencyUnit;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.db.mysql.MysqlUtil;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.proto.XinqiBseCharge.BseCharge;
import com.xinqihd.sns.gameserver.proto.XinqiBseChargeList.BseChargeList;
import com.xinqihd.sns.gameserver.proto.XinqiBseChargeList.ChargeList;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.session.CipherManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.BillingJedis;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;
/**
 * It is used to manage the game's charge objects
 * 
 * @author wangqi
 *
 */
public final class ChargeManager extends AbstractMongoManager {
			
	private static final Logger logger = LoggerFactory.getLogger(ChargeManager.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");
	private static final Logger BILLING_SQL_LOGGER = LoggerFactory.getLogger("BILLING_SQL");

	private static final String BILLING_INSERT_SQL_1 = 
			"insert into billing (orderid, username, rolename, chargeid, channel, device, chargedate, amount, rmb_amount, currency, " +
			"discount, orig_yuanbao, bought_yuanbao, total_yuanbao, receipt_data, success) values (";
	
	private static final String BILLING_ORDERID_SQL =
			"select id from billing where orderid = ";
	
	private static final String COLL_NAME = "charges";

	private static final String INDEX_NAME = "_id";

	private static final String DEFAULT_PRICE_UNIT = "￥";

	private static ConcurrentHashMap<String, ArrayList<ChargePojo>> priceUnitMap = 
			new ConcurrentHashMap<String, ArrayList<ChargePojo>>();

	private static ConcurrentHashMap<Integer, ChargePojo> chargeIdMap = 
			new ConcurrentHashMap<Integer, ChargePojo>();
	
	/**
	 * Use database 'billingIdentifier' as key
	 */
	private static ConcurrentHashMap<String, ChargePojo> billingIdMap = 
			new ConcurrentHashMap<String, ChargePojo>();

	private static final ChargeManager instance = new ChargeManager();
	
	private URL iapVerifyURL = null;
	
	private ExecutorService verifyExectors = Executors.newCachedThreadPool();
	
	private Random random = new Random();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static ChargeManager getInstance() {
		return instance;
	}
	
	ChargeManager() {
		super(
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database),
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null, MongoDBUtil.createDBObject("price", 1));
		priceUnitMap.clear();
		chargeIdMap.clear();
		billingIdMap.clear();
		for ( DBObject obj : list ) {
			ChargePojo chargePojo = (ChargePojo)MongoDBUtil.constructObject(obj);
			ArrayList<ChargePojo> chargeList = priceUnitMap.get(chargePojo.getChannel());
			if ( chargeList == null ) {
				chargeList = new ArrayList<ChargePojo>();
				priceUnitMap.put(chargePojo.getChannel(), chargeList);
			}
			chargeList.add(chargePojo);
			chargeIdMap.put(chargePojo.getId(), chargePojo);
			billingIdMap.put(chargePojo.getBillingIdentifier(), chargePojo);
			logger.debug("Load charge type: {} price: {} from database.", chargePojo.getCurrency(), chargePojo.getPrice());
		}
	}

	/**
	 * Get the given weapon by its id.
	 * @param id
	 * @return
	 */
	public Collection<ChargePojo> getChargePojoByCurrency(String channel) {
		 return priceUnitMap.get(channel);
	}
	
	/**
	 * Get the charge pojo object by id.
	 * @param id
	 * @return
	 */
	public ChargePojo getCharePojoById(int id) {
		return chargeIdMap.get(id);
	}
	
	/**
	 * Get the charge pojo object by id.
	 * @param id
	 * @return
	 */
	public ChargePojo getCharePojoByBillingIdentifier(String billingIdentifier) {
		return billingIdMap.get(billingIdentifier);
	}
	
	/**
	 * Get the given weapon by its id.
	 * @param id
	 * @return
	 */
	public Collection<ChargePojo> getChargePojos() {
		 return chargeIdMap.values();
	}
	
	/**
	 * The user wants to charge money into game to get Yuanbao.
	 * @param id
	 * @return
	 */
	public boolean userChargeMoney(final IoSession session, final User user, final int id, 
			final String receipt, final String token) {
		ChargePojo chargePojo = null;
		boolean success = false;
		if ( id < 0 ) {
			ScriptManager.getInstance().runScript(ScriptHook.CHARGE_APPLE, user, receipt, token);
		} else {
			chargePojo = this.getCharePojoById(id);
			if ( chargePojo != null ) {
				ServerPojo serverPojo = user.getServerPojo();
				String serverId = Constant.EMPTY;
				if ( serverPojo != null ) {
					serverId = serverPojo.getId();
				}
				if ( ChargePojo.CHANNEL_HUAWEI.equals(chargePojo.getChannel())
					|| ChargePojo.CHANNEL_CMCC.equals(chargePojo.getChannel()) 
					|| ChargePojo.CHANNEL_OPPO.equals(chargePojo.getChannel()) 
					|| ChargePojo.CHANNEL_DANGLE.equals(chargePojo.getChannel()) 
					|| ChargePojo.CHANNEL_XIAOMI.equals(chargePojo.getChannel()) ) {
					String transId = generateTranscationID(serverId, user);
					BseCharge.Builder builder = BseCharge.newBuilder();
					builder.setSuccess(false);
					builder.setYuanbao(0);
					builder.setMessage(transId);
					GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
					
					logger.info("transactionId charge request for user {}:{}, transid:{}", new Object[]{serverId, user.getRoleName(), transId});
				} else {
					doCharge(null, user, generateTranscationID(serverId, user), chargePojo, 0, receipt, true);
				}
			} else {
				logger.warn("The ChargePojo with id {} does not exist.", id);
			}
		}
		return success;
	}
	
	/**
	 * It is used for free charge. 
	 * @param user
	 * @param moneyCount
	 * @param token
	 * @return
	 */
	public boolean freeCharge(final User user, final int moneyCount, String token) {
		boolean success = false;
		UserId userId = CipherManager.getInstance().checkEncryptedUserToken(token);
		if ( user.get_id().equals(userId) ) {
			success = true;
			String orderId = LoginManager.getRandomUserName();
			ChargeManager.getInstance().doCharge(user.getSessionKey(), user, orderId, null, moneyCount, null, success);
		} else {
			BseCharge.Builder builder = BseCharge.newBuilder();
			builder.setSuccess(success);
			builder.setMessage(Text.text("charge.invalid"));
			builder.setYuanbao(0);
			GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
			logger.warn("User {}'s token is not valid ", user.getRoleName(), token);
		}
		return success;
	}
	
	/**
	 * Put the money into user's account and record
	 * @param userSessionKey TODO
	 * @param user The user who pay the money
	 * @param orderId The orderId from third party. If it is not null, the orderId should not exist in database.
	 * @param chargePojo null for freecharge
	 * @param moneyCount The money amount for free charge.
	 * @param receiptData The third party receipt data
	 * @param success 
	 * 
	 * @return
	 */
	public boolean doCharge(SessionKey userSessionKey, User userFromDB, String orderId, 
			ChargePojo chargePojo, float moneyCount, String receiptData, boolean success) {
		//username, device, chargedate, amount, rmb_amount, currency,
		// discount, orig_yuanbao, bought_yuanbao, total_yuanbao
		/**
		 * Try to get the current user object in gameserver. If the user is offline,
		 * his bag should be queried.
		 */
		User user = userFromDB;
		if ( userSessionKey != null ) {
			User userOnline = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
			if ( userOnline != null ) {
				user = userOnline;
			} else {
				UserManager.getInstance().queryUserBag(user);
				TaskManager.getInstance().getUserLoginTasks(user);
			}
		} else {
			UserManager.getInstance().queryUserBag(user);
			TaskManager.getInstance().getUserLoginTasks(user);
		}
		String username = user.getUsername();
		String rolename = user.getRoleName();
		String channel = user.getChannel();
		int chargeId = 0;
		float amount = 0f;
		float rmb_amount = 0f;
		String currency = CurrencyUnit.CHINESE_YUAN.name();
		float discount = 1.0f;
		int boughtYuanbao = 0; 
		if ( chargePojo != null ) {
			//channel = chargePojo.getChannel();
			chargeId = chargePojo.getId();
			amount = chargePojo.getPrice();
			if ( chargePojo.getCurrency() != CurrencyUnit.CHINESE_YUAN ) {
				if ( chargePojo.getCurrency() == CurrencyUnit.US_DOLLOR ) {
					rmb_amount = Math.round(amount)*6;
				} else {
					rmb_amount = amount;
				}
			} else {
				rmb_amount = amount;
			}
			currency = chargePojo.getCurrency().name();
			discount = chargePojo.getDiscount();
			boughtYuanbao = chargePojo.getYuanbao();
		} else {
			channel = user.getChannel();
			chargeId = 0;
			amount = moneyCount;
			rmb_amount = amount; 
			currency = "freecharge";
			boughtYuanbao = ScriptManager.getInstance().runScriptForInt(
					ScriptHook.CHARGE_DISCOUNT, user, rmb_amount);
			if ( rmb_amount > 0 && boughtYuanbao <= 0 ) {
				boughtYuanbao = 1;
			}
			float noDiscount = rmb_amount*10.0f;
			discount = (1-(boughtYuanbao-noDiscount)/noDiscount)*10;
			if ( discount < 0 || discount == Double.NaN ) discount = 0;
		}
		//Check if the orderId exist
		boolean orderIdExist = false;
		if ( orderId != null ) {
			String sql = StringUtil.concat(BILLING_ORDERID_SQL, QUOTE, orderId, QUOTE);
			Map row = MysqlUtil.executeQueryFirstRow(sql, JndiContextKey.mysql_billing_db.name());
			if ( row!=null ) {
				orderIdExist = true;
			}
		}
		if ( !orderIdExist ) {
			String device = user.getClient();
			String chargeDateStr = DateUtil.formatDateTime(new Date());

			int origYuanbao = user.getYuanbao();
			int totalYuanbao = origYuanbao + boughtYuanbao;
			if ( receiptData != null ) {
				int length = receiptData.length();
				length = Math.min(length, 200);
				receiptData = receiptData.substring(0, length);
			}
			
			String sql = StringUtil.concat(BILLING_INSERT_SQL_1,
					QUOTE, orderId, QUOTE, COMMA, 
					QUOTE, username, QUOTE, COMMA, 
					QUOTE, rolename, QUOTE, COMMA,
					chargeId, COMMA,
					QUOTE, channel, QUOTE, COMMA, 
					QUOTE, device, QUOTE, COMMA, 
					QUOTE, chargeDateStr, QUOTE, COMMA,
					amount, COMMA,
					rmb_amount, COMMA,
					QUOTE, currency, QUOTE, COMMA,
					discount, COMMA,
					origYuanbao, COMMA,
					boughtYuanbao, COMMA,
					totalYuanbao, COMMA,
					QUOTE, receiptData, QUOTE, COMMA,
					success,
					")"
					);
			BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
					orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, totalYuanbao, receiptData
			});
			BILLING_SQL_LOGGER.info(sql);
			
			try {
				MysqlUtil.executeUpdate(sql, JndiContextKey.mysql_billing_db.name());
			} catch (Exception e) {
				e.printStackTrace();
			}

			//Get user's bag
			if ( success ) {
				user.setYuanbao(totalYuanbao);
				//Caculated the cumulated 
				int chargedYuanbao = user.getChargedYuanbao();
				chargedYuanbao += boughtYuanbao;
				user.setChargedYuanbao(chargedYuanbao);
				user.setChargeCount( user.getChargeCount()+1 );
				
				//Check the vip level
				ScriptManager.getInstance().runScript(ScriptHook.VIP_CHARGE_LEVEL, user, boughtYuanbao);
				
				//Update user status
				UserManager.getInstance().saveUser(user, false);
				UserManager.getInstance().saveUserBag(user, false);
				
				//Send the data back to client
				BseRoleInfo roleInfo = user.toBseRoleInfo();
				GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
				
				String content = Text.text("charge.success", new Object[]{boughtYuanbao, totalYuanbao});
				
				MailMessageManager.getInstance().sendMail(
						null, user.get_id(), content, content, null, true);
				SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), content, 5000);
				
				//Check the promotion
				ScriptManager.getInstance().runScript(ScriptHook.PROMOTION_CHARGE, user, boughtYuanbao);
				TaskManager.getInstance().processUserTasks(user, TaskHook.CHARGE, boughtYuanbao);
				
				logger.debug("The new roleInfo data for user {} is sent to client.", user.getRoleName());
			}
			StatClient.getIntance().sendDataToStatServer(
					user, StatAction.Charge, chargeId, channel, 
					boughtYuanbao, amount, success);
		} else {
			logger.warn("The orderid already exist!");
		}
		return success;
	}
	
	/**
	 * Process the transaction id based billing request.
	 * 
	 * @param success
	 * @param userName
	 * @param orderId
	 * @param chargeDateStr
	 * @param transId
	 * @param device
	 * @param money
	 * @param transtime
	 * @return
	 */
	public static final boolean processTransacIdBilling(
			String orderId, String transId,
			String device, float moneyFloat, String transtime, String channel) {
		
		int money = Math.round(moneyFloat);
		int boughtYuanbao = money * 10;
		boolean success = false;
		/**
		 * Remove the '0'padding on left.
		 */		
		Jedis jedis = JedisFactory.getJedisDB();
		//Remove zero from beginning
		StringBuilder buf = new StringBuilder(10);
		if ( transId == null ) {
			transId = Constant.EMPTY;
		}
		for ( char ch : transId.toCharArray() ) {
			if ( buf.length()==0 && ch == '0') {
				continue;
			}
			buf.append(ch);
		}
		String trimTransId = buf.toString();
		String serverAndUserName = jedis.get(trimTransId);
		if ( serverAndUserName == null ) {
			serverAndUserName = BillingJedis.getInstance().getValue(trimTransId);
		}
		if ( serverAndUserName == null ) {
			BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
					//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "huawei order fail"
					orderId, transId, device, transtime, boughtYuanbao, money, "rmb", 0, 0, 0, 0, "no transid in redis:".concat(channel)
			});
		} else {
			String[] fields = StringUtil.splitMachineId(serverAndUserName);
			String serverId = fields[0];
			String userName = fields[1];
			User user = UserManager.getInstance().queryUser(userName);
			if ( user == null ) {
				logger.warn("Failed to find user by username: {}", userName);
				BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
						//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "huawei order fail"
						orderId, transId, device, transtime, boughtYuanbao, money, "rmb", 0, 0, 0, 0, 
						StringUtil.concat("not found user:", userName)});
			} else {
				success = true;
				SessionManager manager = GameContext.getInstance().getSessionManager();
				SessionKey userSessionKey = manager.findSessionKeyByUserId(user.get_id());
				if ( userSessionKey == null ) {
					userSessionKey = BillingJedis.getInstance().findSessionKeyByUserId(user.get_id());
				}
				if ( userSessionKey != null ) {
					//The user is online now
					String gameServerId = manager.findUserGameServerId(userSessionKey);
					if ( GameContext.getInstance().getGameServerId().equals(gameServerId) )  {
						ChargeManager.getInstance().doCharge(userSessionKey, user, orderId, null, money, "changyou", true);									
					} else {
						logger.info("Proxy charge request to remote server {}", gameServerId);
						BceChargeInternal.Builder bceCharge = BceChargeInternal.newBuilder();
						bceCharge.setUserid(user.get_id().toString());
						bceCharge.setFreecharge(true);
						bceCharge.setChargemoney(Math.round(money));
						bceCharge.setChannel("changyou");
						bceCharge.setOrderid(orderId);
						GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, bceCharge.build());
					}
				} else {
					ChargeManager.getInstance().doCharge(null, user, orderId, null, money, channel, true);
				}
			}
		}
		return success;
	}
	
	/**
	 * 
	 * @param channel
	 * @return
	 */
	public ChargeList toBseChargeList(String channel) {
		return toBseChargeList(channel, true, 0);
	}
	
	/**
	 * Construct Protobuf's BseMap data and 
	 * prepare to send to client.
	 * 
	 * @return
	 */
	public ChargeList toBseChargeList(String channel, boolean enable, int tab) {
		ArrayList<ChargePojo> chargeList = priceUnitMap.get(channel);
		ChargeList.Builder builder = ChargeList.newBuilder();
		builder.setEnable(enable);
		builder.setTab(tab);
		builder.setDesc(Text.text("charge.first.reward"));
		if ( chargeList != null ) {
			for ( ChargePojo chargePojo : chargeList ) {
				builder.addCharges(chargePojo.toChargeData());
			}
		}
		return builder.build();
	}
		
	/**
	 * Generate an unique transaction id for this user.
	 * The valid time is 1800 seconds.
	 * 
	 * @param user
	 * @return
	 */
	public final String generateTranscationID(String serverId, User user) {
		long transId = random.nextInt();
		if ( transId < 0 ) transId = -transId;
		String transIdStr = String.valueOf(transId);
		Jedis jedis = JedisFactory.getJedisDB();
		String value = StringUtil.concat(serverId, Constant.COLON, user.getUsername());
		Long existLong = jedis.setnx(transIdStr, value);
		/**
		 * existLong==1: successfully set
		 * existLong==0: already exist.
		 */
		while ( existLong == null || existLong.longValue() != 1 ) {
			transId = random.nextInt();
			if ( transId < 0 ) transId = -transId;
			transIdStr = String.valueOf(transId);
			existLong = jedis.setnx(transIdStr, value);
		}
		jedis.expire(transIdStr, 1800);
		logger.info("Generate transId {} for user {}", transIdStr, user.getRoleName());
		return transIdStr;
	}
	
	/**
	 * Submit a task for future use.
	 * @param runnable
	 * @return
	 */
	public Future submitTask(Runnable runnable) {
		return verifyExectors.submit(runnable);
	}

}
