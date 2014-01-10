package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.VipPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager.ConfirmCallback;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseBuyVip.BseBuyVip;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseVipInfo.BseVipInfo;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class VipManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(VipManager.class);
	
	private static final String COLL_NAME = "vips";
	
	private static final String INDEX_NAME = "_id";
	
	private ConcurrentHashMap<Integer, VipPojo> dataMap = 
			new ConcurrentHashMap<Integer, VipPojo>();
	
	private TreeSet<VipPojo> vipSet = new TreeSet();
	
	private static final VipManager instance = new VipManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static VipManager getInstance() {
		return instance;
	}
	
	VipManager() {
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
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, 
				namespace, COLL_NAME, null);
		dataMap.clear();
		vipSet.clear();
		for ( DBObject obj : list ) {
			VipPojo item = (VipPojo)MongoDBUtil.constructObject(obj);
			dataMap.put(item.getId(), item);
			//logger.debug("Load item id {} name {} from database.", item.getId(), item.getName());
		}
		vipSet.addAll(dataMap.values());
		logger.debug("Load total {} vip period from database.", dataMap.size());
	}
	
	/**
	 * Get the given item by its id.
	 * @param id
	 * @return
	 */
	public VipPojo getVipPojoById(int id) {
		 return dataMap.get(id);
	}
	
	/**
	 * Get the underlying item collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<VipPojo> getVipPojos() {
		return vipSet;
	}
	
	/**
	 * Get the proper VipPojo object according to user's 
	 * cumulated charged yuanbao.
	 * 
	 * @param chargedYuanbao
	 * @return
	 */
	public VipPojo getVipPojoByYuanbao(int chargedYuanbao) {
		VipPojo validVip = null;
		if ( chargedYuanbao > 0 ) {
			for ( VipPojo vip : vipSet ) {
				if ( vip.getYuanbaoPrice() <= chargedYuanbao ) {
					validVip = vip;
				} else {
					break;
				}
			}
		}
		return validVip;
	}
	
	/**
	 * Get the vip given level's bag space
	 * @param level
	 * @return
	 */
	public int processVipOfflineExp(User user, long currentTimeMillis, boolean sendMail) {
		Date lastDate = user.getLdate();
		int offlineExp = 0;
		if ( user.isVip() ) {
			if ( lastDate != null ) {
				long lastMillis = lastDate.getTime();
				float hours = (currentTimeMillis-lastMillis)/3600000.0f;
				if ( hours > 1.0 ) {
					int [] vipOfflineExp = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.VIP_OFFLINE_EXP);
					int [] vipOfflineMaxExp = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.VIP_OFFLINE_MAX_EXP);
					offlineExp = Math.min(Math.round(vipOfflineExp[user.getViplevel()-1] * hours), vipOfflineMaxExp[user.getViplevel()-1]);
					if ( offlineExp > 0 && sendMail) {
						ArrayList<Reward> rewards = new ArrayList<Reward>();
						Reward reward = new Reward();
						reward.setPropId("-5");
						reward.setPropLevel(-1);
						reward.setType(RewardType.EXP);
						reward.setPropCount(offlineExp);
						rewards.add(reward);
						String subject = Text.text("vip.offlineexp.subject", user.getViplevel());
						String content = Text.text("vip.offlineexp.content", 
								user.getViplevel(), offlineExp);
						MailMessageManager.getInstance().sendMail(
								null, user.get_id(), subject, content, rewards, true);
						logger.debug("User {} offline hours {}", user.getRoleName(), hours);
					}
				}
			}
		}
		return offlineExp;
	}
	
	/**
	 * Get the extra buyCount for vip user
	 * @param user
	 * @return
	 */
	public int getVipLevelRoleActionBuyCount(User user) {
		int buyCount = 0;
		if ( user.isVip() ) {
			int[] vipRoleActionBuyCount = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.VIP_BUY_ROLEACTION);
			int targetIndex = user.getViplevel()-1;
			if ( targetIndex < 0 ) targetIndex = 0;
			if ( targetIndex >= vipRoleActionBuyCount.length ) targetIndex = vipRoleActionBuyCount.length-1;
			return vipRoleActionBuyCount[targetIndex];
		}
		return buyCount;
	}
	
	/**
	 * Get the caishen buyCount for vip user 
	 * @param user
	 * @return
	 */
	public int getVipLevelCaishenBuyCount(User user) {
		int buyCount = 0;
		if ( user.isVip() ) {
			int[] vipCaishenBuyCount = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.VIP_BUY_CAISHEN);
			int targetIndex = user.getViplevel()-1;
			if ( targetIndex < 0 ) targetIndex = 0;
			if ( targetIndex >= vipCaishenBuyCount.length ) targetIndex = vipCaishenBuyCount.length-1;
			return vipCaishenBuyCount[targetIndex];
		}
		return buyCount;
	}
	
	/**
	 * Check if the given vip level can do unlimited treasure hunt.
	 * @param user
	 * @return
	 */
	public boolean getVipLevelCanBuyTreasureHunt(User user) {
		boolean success = false;
		if ( user.isVip() ) {
			int[] vipTreasureHunt = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.VIP_CAN_TREASURE_HUNT);
			int targetIndex = user.getViplevel()-1;
			if ( targetIndex < 0 ) targetIndex = 0;
			if ( targetIndex >= vipTreasureHunt.length ) targetIndex = vipTreasureHunt.length-1;
			return vipTreasureHunt[targetIndex] == 1;
		}
		return success;
	}
	
	/**
	 * Check if the given vip level can transfer strength level cross weapon grade.
	 * @param user
	 * @return
	 */
	public boolean getVipLevelCanTransferCrossLevel(User user, int weaponLevel) {
		boolean success = false;
		if ( user.isVip() ) {
			int[] canArray = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.VIP_CAN_TRANSFER_LEVEL);
			int targetIndex = user.getViplevel()-1;
			if ( targetIndex < 0 ) targetIndex = 0;
			if ( targetIndex >= canArray.length ) targetIndex = canArray.length-1;
			return canArray[targetIndex] > weaponLevel;
		}
		return success;
	}
	
	/**
	 * Check if the given vip level can transfer strength level cross weapon color.
	 * @param user
	 * @return
	 */
	public boolean getVipLevelCanTransferCrossColor(User user, WeaponColor weaponColor) {
		boolean success = false;
		if ( user.isVip() ) {
			int[] canArray = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.VIP_CAN_TRANSFER_COLOR);
			int targetIndex = user.getViplevel()-1;
			if ( targetIndex < 0 ) targetIndex = 0;
			if ( targetIndex >= canArray.length ) targetIndex = canArray.length-1;
			return canArray[targetIndex] >= weaponColor.ordinal();
		}
		return success;
	}
	
	/**
	 * Get the battle exp gain ratio for given vip level.
	 * @param user
	 * @return
	 */
	public double getVipLevelBattleExpRatio(User user) {
		double expRatio = 0;
		if ( user.isVip() ) {
			double[] vipExpRatios = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.VIP_BATTLE_EXP_RATIO);
			int targetIndex = user.getViplevel()-1;
			if ( targetIndex < 0 ) targetIndex = 0;
			if ( targetIndex >= vipExpRatios.length ) targetIndex = vipExpRatios.length-1;
			if ( targetIndex >= 0 && vipExpRatios.length>=0 ) {
				return vipExpRatios[targetIndex];
			}
		}
		return expRatio;
	}
	
	// ---------------------------------------------------- Old methods
	
	/**
	 * User wants to become a VIP for a period
	 * @param user
	 * @param id
	 * @deprecated
	 */
	public final void userBuyVipPeriod(User user, int id, int payType) {
		final VipPojo vipPeriod = getVipPojoById(id);
		if ( vipPeriod != null ) {
			int price = 0;
			final MoneyType moneyType = MoneyType.fromType(payType);
			if ( moneyType != null ) {
				switch ( moneyType ) {
					case YUANBAO:
						price = vipPeriod.getYuanbaoPrice();
						break;
					default:
						logger.warn("#userBuyVipPeriod: unsupported for moneyType:{}", moneyType);
						break;
				}
				ConfirmManager manager = ConfirmManager.getInstance();
				String message = null;
				boolean isVip = checkUserVipStatus(user);
				final Calendar startCal = Calendar.getInstance();
				Calendar endCal = null;
				int validSecond = vipPeriod.getValidSeconds();
				if ( validSecond > 0 ) {
					if ( isVip ) {
						startCal.setTime(user.getVipedate());
						endCal = (Calendar)startCal.clone();
						endCal.add(Calendar.SECOND, validSecond);
						message = Text.text("vip.resubscribe", DateUtil.formatDate(startCal.getTime()), DateUtil.formatDate(endCal.getTime()));
					} else {
						endCal = (Calendar)startCal.clone();
						endCal.add(Calendar.SECOND, validSecond);
						message = Text.text("vip.subscribe", validSecond/86400, DateUtil.formatDate(endCal.getTime()));
					}					
				}
				final int finalPrice = price;
				final Calendar finalEndCalendar = endCal;
				manager.sendConfirmMessage(user, message, "vip.buy", new ConfirmCallback() {
					
					@Override
					public void callback(User user, int selected) {
						if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
							buyVip(user, vipPeriod, finalPrice, moneyType, startCal, finalEndCalendar);
						}
					}
				});
			} else {
				logger.warn("#userBuyVipPeriod: failed to find MoneyType:{}",payType);
			}
		} else {
			logger.warn("Does not find the VipPeriod by id:{}", id);
			//Send failure message
			SysMessageManager.getInstance().sendClientInfoMessage(user, "vip.buy.failure", Type.NORMAL);
		}
	}

	/**
	 * Buy the VIP at given price
	 * @param user
	 * @param vipPeriod
	 * @param price
	 * @param moneyType
	 * @param isVip
	 * @deprecated
	 */
	private void buyVip(User user, VipPojo vipPeriod, int price,
			MoneyType moneyType, Calendar startCal, Calendar endCal) {
		boolean success = ShopManager.getInstance().payForSomething(
				user, moneyType, price, 1, null, false);
		BseBuyVip.Builder builder = BseBuyVip.newBuilder();
		builder.setSuccess(success);
		String validDate = Constant.EMPTY;
		if ( success ) {
			user.setVipbdate(startCal.getTime());
			user.setVipedate(endCal.getTime());
			user.setIsvip(true);

			//Enlarge user's bag
			int maxCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_BAG_MAX, 70);
			Bag bag = user.getBag();
			bag.setMaxCount(maxCount*2);

			//Update user's info
			UserManager.getInstance().saveUser(user, false);
			UserManager.getInstance().saveUserBag(user, false);
			BseRoleInfo roleInfo = user.toBseRoleInfo();
			GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);

			validDate = DateUtil.formatDate(user.getVipedate());
			String message = Text.text("vip.buy.success", validDate);
//						SysMessageManager.getInstance().sendClientInfoRawMessage(
//								user, message, Action.NOOP, Type.NORMAL);
			//Send notify
			String notifyMessage = Text.text("notice.vip", user.getRoleName(), 1);
			ChatManager.getInstance().processChatToWorldAsyn(null, notifyMessage);

			logger.info("User {} becomes an VIP user. End date:{}", user.getRoleName(), user.getVipedate());
			int expireDays = (int)(user.getVipedate().getTime()/1000);
			builder.setExpireDate(expireDays);
			builder.setMessage(message);

			StatClient.getIntance().sendDataToStatServer(user, StatAction.ConsumeBuyVip, 
					moneyType, price, validDate, expireDays);
		} else {
			logger.info("User {} cannot pay for VIP with enough money", user.getRoleName());
			builder.setExpireDate(-1);
			String message = Text.text("vip.nomoney", user.getYuanbao());
			builder.setMessage(message);
//						SysMessageManager.getInstance().sendClientInfoRawMessage(
//								user, message, Action.NOOP, Type.NORMAL);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());

		StatClient.getIntance().sendDataToStatServer(user, StatAction.BuyVip, success, moneyType, price, success, validDate);
	}
	
	/**
	 * Check whether the user is a valid VIP user.
	 * If the VIP expires, re-set the VIP status.
	 * @param user
	 * @return
	 * @deprecated
	 */
	public final boolean checkUserVipStatus(final User user) {
		//Check user's vipStatus
		boolean isVip = user.isVip();
		String userName = user.getRoleName();
		String beginDateStr = DateUtil.formatDate(user.getVipbdate());
		final String endDateStr = DateUtil.formatDate(user.getVipedate());
		if ( isVip ) {
			Date vipEndDate = user.getVipedate();
			boolean expire = false;
			long millisDiff = 0l;
			if ( vipEndDate == null ) {
				expire = true;
			} else {
				millisDiff =  vipEndDate.getTime() - System.currentTimeMillis();
				if ( millisDiff<0 ) {
					expire = true;
				}
			}
			if ( expire ) {
				logger.info("User '{}' VIP expired now. {}", userName, vipEndDate);
//				SysMessageManager.getInstance().sendClientInfoMessage(user, "vip.expire");
				user.setIsvip(false);
				isVip = false;

				/**
				Bag bag = user.getBag();
				int normalSize = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_BAG_MAX, 70);
				bag.setMaxCount(normalSize);
				UserManager.getInstance().saveUserBag(user, false);
				*/

				StatClient.getIntance().sendDataToStatServer(user, StatAction.VipExpire, 
						beginDateStr, endDateStr);
			} else {
				final int days = (int)millisDiff/86400000;
				if ( days <= 5 ) {
					GameContext.getInstance().scheduleTask(new Runnable() {
						public void run() {
							String message = Text.text("vip.tobeexpired", days, endDateStr);
							SysMessageManager.getInstance().sendClientInfoRawMessage(
									user.getSessionKey(), message, 10000);
						}
					}, 5, TimeUnit.SECONDS);
				}
			}
		}
		return isVip;
	}
	
	/**
	 * Convert this object to BseVipInfo
	 * @return
	 */
	public BseVipInfo toBseVipInfo() {
		BseVipInfo.Builder builder = BseVipInfo.newBuilder();
		for ( VipPojo vipPojo : vipSet ) {
			builder.addVipinfo(vipPojo.toBseVipInfo());
		}
		return builder.build();
	}
}
