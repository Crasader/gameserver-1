package com.xinqihd.sns.gameserver.db.mongo;

import static com.xinqihd.sns.gameserver.config.Constant.*;
import static com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.BiblioAgilityComparator;
import com.xinqihd.sns.gameserver.config.BiblioAttackComparator;
import com.xinqihd.sns.gameserver.config.BiblioDefendComparator;
import com.xinqihd.sns.gameserver.config.BiblioLuckyComparator;
import com.xinqihd.sns.gameserver.config.BiblioPojo;
import com.xinqihd.sns.gameserver.config.BiblioPowerComparator;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserBiblio;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseGetUserBiblio;
import com.xinqihd.sns.gameserver.proto.XinqiBseGetUserBiblio.BseGetUserBiblio;
import com.xinqihd.sns.gameserver.proto.XinqiBseTakeUserBiblioReward.BseTakeUserBiblioReward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 图鉴的功能
 * 
 * @author wangqi
 *
 */
public class BiblioManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(BiblioManager.class);
	
	public static BiblioManager instance = new BiblioManager();
	
	private static final String COLL_NAME = "bibilo";
	
	private static final String INDEX_NAME = "_id";
	private static final String USERNAME = "roleName";
	
	//This array contains weaponTypeId
	private HashMap<String, Integer> powerMap = new HashMap<String, Integer>();
	private HashMap<String, Integer> attackMap = new HashMap<String, Integer>();
	private HashMap<String, Integer> defendMap = new HashMap<String, Integer>();
	private HashMap<String, Integer> agilityMap = new HashMap<String, Integer>();
	private HashMap<String, Integer> luckyMap = new HashMap<String, Integer>();
	
	
	public BiblioManager() {
		super(
				GlobalConfig.getInstance().getStringProperty("mongdb.database"),
				GlobalConfig.getInstance().getStringProperty("mongdb.namespace"),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		//Ensure Index
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_NAME, USERNAME, true);

		reload();
	}

	/**
	 * 
	 */
	public void reload() {
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		
		BiblioPowerComparator powerComparator = new BiblioPowerComparator();
		BiblioAttackComparator attackComparator = new BiblioAttackComparator();
		BiblioDefendComparator defendComparator = new BiblioDefendComparator();
		BiblioAgilityComparator agilityComparator = new BiblioAgilityComparator();
		BiblioLuckyComparator luckyComparator = new BiblioLuckyComparator();
		TreeSet<BiblioPojo> powerSet = new TreeSet<BiblioPojo>(powerComparator);
		TreeSet<BiblioPojo> attackSet = new TreeSet<BiblioPojo>(attackComparator);
		TreeSet<BiblioPojo> defendSet = new TreeSet<BiblioPojo>(defendComparator);
		TreeSet<BiblioPojo> agilitySet = new TreeSet<BiblioPojo>(agilityComparator);
		TreeSet<BiblioPojo> luckySet = new TreeSet<BiblioPojo>(luckyComparator);
		
		for ( WeaponPojo weapon : weapons ) {
			if ( weapon.getUserLevel() == 90 && 
					!weapon.isUsedByBoss() ) {
					//weapon.isCanBeRewarded() ) {
				BiblioPojo biblio = new BiblioPojo();
				biblio.setWeaponId(weapon.getId());
				biblio.setWeaponType(weapon.getTypeName());
				biblio.setPower(weapon.getPower());
				biblio.setAttack(weapon.getAddAttack());
				biblio.setDefend(weapon.getAddDefend());
				biblio.setLucky(weapon.getAddLuck());
				biblio.setAgility(weapon.getAddAgility());
				powerSet.add(biblio);
				attackSet.add(biblio);
				defendSet.add(biblio);
				agilitySet.add(biblio);
				luckySet.add(biblio);
			}
		}
		int index = 0;
		for (Iterator iter = powerSet.iterator(); iter.hasNext();) {
			BiblioPojo biblioPojo = (BiblioPojo) iter.next();
			powerMap.put(biblioPojo.getWeaponType(), index++);
		}
		index = 0;
		for (Iterator iter = attackSet.iterator(); iter.hasNext();) {
			BiblioPojo biblioPojo = (BiblioPojo) iter.next();
			attackMap.put(biblioPojo.getWeaponType(), index++);
		}
		index = 0;
		for (Iterator iter = defendSet.iterator(); iter.hasNext();) {
			BiblioPojo biblioPojo = (BiblioPojo) iter.next();
			defendMap.put(biblioPojo.getWeaponType(), index++);
		}
		index = 0;
		for (Iterator iter = agilitySet.iterator(); iter.hasNext();) {
			BiblioPojo biblioPojo = (BiblioPojo) iter.next();
			agilityMap.put(biblioPojo.getWeaponType(), index++);
		}
		index = 0;
		for (Iterator iter = luckySet.iterator(); iter.hasNext();) {
			BiblioPojo biblioPojo = (BiblioPojo) iter.next();
			luckyMap.put(biblioPojo.getWeaponType(), index++);
		}
	}
	
	public static BiblioManager getInstance() {
		return instance;
	}
	
	/**
	 * Get all the types id
	 * @return
	 */
	public HashMap<String, Integer> getPowerList() {
		return powerMap; 
	}

	/**
	 * Query the user's bibilo object.
	 * @param userId
	 * @return
	 */
	public UserBiblio queryUserBiblio(UserId userId) {
		if ( userId == null ) {
			logger.warn("queryUserBiblio user or userId is null. {}", userId);
			return null;
		}
		DBObject query = createDBObject();
		query.put(_ID, userId.getInternal());
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, COLL_NAME, null);
		UserBiblio biblio = null;
		if ( dbObj != null ) {
			biblio = (UserBiblio)MongoDBUtil.constructObject(dbObj);
		}
		return biblio;
	}
	
	/**
	 * Save the user's biblio to database
	 * @param userBiblio
	 * @return
	 */
	public boolean saveUserBiblio(UserBiblio userBiblio) {
		boolean success = false;
		if ( userBiblio != null ) {
			DBObject query = createDBObject();
			query.put(_ID, userBiblio.getId().getInternal());
			DBObject objectToSave = createMapDBObject(userBiblio);
			MongoDBUtil.saveToMongo(query, objectToSave, databaseName, namespace, COLL_NAME, isSafeWrite);
			success = true;
		}
		return success;
	}
	
	/**
	 * Save the user's biblio to database
	 * @param userBiblio
	 * @return
	 */
	public boolean updateUserBiblio(UserId userId,
			String weaponTypeName, String weaponId) {
		boolean success = false;
		if ( userId != null ) {
			DBObject query = createDBObject();
			query.put(_ID, userId.getInternal());
			DBObject setObj = createDBObject("biblio.".concat(weaponTypeName), weaponId);
			//setObj.put("roleName", roleName);
			DBObject objectToSave = createDBObject(OP_SET, setObj);
			MongoDBUtil.saveToMongo(query, objectToSave, databaseName, namespace, COLL_NAME, isSafeWrite);
			success = true;
		}
		return success;
	}
	
	/**
	 * Save the user's biblio to database
	 * @param userBiblio
	 * @return
	 */
	public boolean updateUserBiblio(UserId userId,
			List<String> weaponTypeName, List<String> weaponId) {
		boolean success = false;
		if ( userId != null ) {
			DBObject query = createDBObject();
			query.put(_ID, userId.getInternal());
			DBObject setObj = createDBObject();
			//setObj.put("roleName", roleName);
			for ( int i=0; i<weaponTypeName.size(); i++ ) {
				setObj.put(weaponTypeName.get(i), weaponId.get(i));
				setObj = createDBObject("biblio.".concat(weaponTypeName.get(i)), weaponId.get(i));
			}
			DBObject objectToSave = createDBObject(OP_SET, setObj);
			objectToSave.put(OP_SET, setObj);
			MongoDBUtil.saveToMongo(query, objectToSave, databaseName, namespace, COLL_NAME, isSafeWrite);
			success = true;
		}
		return success;
	}
	
	/**
	 * Add a new PropData to user's biblio list
	 * @param weaponTypeName
	 * @param weaponId
	 */
	public void addBiblio(User user, PropData propData) {
		if ( propData != null && propData.isWeapon() ) {
			Pojo pojo = propData.getPojo();
			if ( pojo != null && pojo instanceof WeaponPojo ) {
				WeaponPojo weapon = (WeaponPojo)pojo;
				String typeName = weapon.getTypeName();
				String weaponId = weapon.getId();
				UserBiblio biblio = user.getBiblio();
				boolean isNew = false;
				if ( biblio == null ) {
					biblio = new UserBiblio();
					user.setBiblio(biblio);
					isNew = true;
				}
				String oldWeaponId = (String)biblio.getWeaponId(typeName);
				if ( oldWeaponId == null || weaponId.compareTo(oldWeaponId)> 0 ) {
					biblio.addBiblio(typeName, weaponId);
					if ( isNew ) {
						BiblioManager.getInstance().saveUserBiblio(biblio);
					} else {
						BiblioManager.getInstance().updateUserBiblio(
								user.get_id(), typeName, weaponId);
					}
				}
			}
		}
	}
	
	/**
	 * Add a new PropData to user's biblio list
	 * @param weaponTypeName
	 * @param weaponId
	 */
	public void addBiblioList(User user, List<PropData> propDatas) {
		ArrayList<String> weaponTypes = new ArrayList<String>();
		ArrayList<String> weaponIds = new ArrayList<String>();
		for ( PropData propData : propDatas ) {
			if ( propData != null && propData.isWeapon() ) {
				Pojo pojo = propData.getPojo();
				if ( pojo != null && pojo instanceof WeaponPojo ) {
					WeaponPojo weapon = (WeaponPojo)pojo;
					String typeName = weapon.getTypeName();
					String weaponId = weapon.getId();
					UserBiblio biblio = user.getBiblio();
					if ( biblio == null ) {
						biblio = new UserBiblio();
						user.setBiblio(biblio);
					}
					String oldWeaponId = (String)biblio.getWeaponId(typeName);
					if ( oldWeaponId == null || weaponId.compareTo(oldWeaponId)> 0 ) {
						biblio.addBiblio(typeName, weaponId);
						weaponTypes.add(typeName);
						weaponIds.add(weaponId);
					}
				}
			}
		}
		if ( weaponTypes.size() > 0 ) {
			BiblioManager.getInstance().updateUserBiblio(user.get_id(), 
					weaponTypes, weaponIds);
		}
	}

	/**
	 * Remove the biblio
	 * @param userId
	 * @return
	 */
	public boolean removeUserBiblio(UserId userId) {
		boolean success = false;
		try {
			DBObject query = createDBObject();
			query.put(_ID, userId.getInternal());
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_NAME, isSafeWrite);
			success = true;
		} catch (Exception e) {
			logger.warn("Failed to remove user bibilo: {}", e.getMessage());
		}
		return success;
	}
	
	/**
	 * Scan user's bag and add all items in bag.
	 * It should be done when the user logins.
	 */
	public void scanUserBag(User user) {
		UserBiblio biblio = queryUserBiblio(user.get_id());
		if ( biblio == null ) {
			biblio = new UserBiblio();
			user.setBiblio(biblio);
			saveUserBiblio(biblio);
		} else {
			user.setBiblio(biblio);
		}
		Bag bag = user.getBag();
		List<PropData> list = bag.getWearPropDatas();
		addBiblioList(user, list);
		list = bag.getOtherPropDatas();
		addBiblioList(user, list);
		//debug purpose
		/*
		for ( String weaponId : powerMap.keySet() ) {
			WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weaponId, 100);
			addBiblio(user, weapon.toPropData(30, WeaponColor.WHITE));
		}
		*/
	}
	
	/**
	 * Get all the UserBiblio objects and sort them by type
	 * 
   * 图鉴的排序方式
   * 0: 战斗力排序
   * 1: 攻击排序
   * 2: 防御排序
   * 3: 敏捷排序
   * 4: 幸运排序
	 * 
	 * @param sortType
	 * @return
	 */
	public void sendUserBiblioList(User user, int sortType) {
		UserBiblio biblio = user.getBiblio();
		if ( biblio == null ) {
			biblio = BiblioManager.getInstance().queryUserBiblio(user.get_id());
			user.setBiblio(biblio);
		}
		HashMap<String, Integer> sortMap = null;
		String desc = Constant.EMPTY;
		switch ( sortType ) {
			case 0:
				sortMap = powerMap;
				desc = Text.text("biblio.power");
				break;
			case 1:
				sortMap = attackMap;
				desc = Text.text("biblio.attack");
				break;
			case 2:
				sortMap = defendMap;
				desc = Text.text("biblio.defend");
				break;
			case 3:
				sortMap = agilityMap;
				desc = Text.text("biblio.agility");
				break;
			case 4:
				sortMap = luckyMap;
				desc = Text.text("biblio.luck");
				break;
			default:
				logger.warn("unknown sort type: {}", sortType);
				break;
		}
		BseGetUserBiblio.Builder listBuilder = BseGetUserBiblio.newBuilder();
		listBuilder.setDesc(desc);
		for ( Map.Entry<String, String> entry : biblio.getBiblio().entrySet() ) {
			String weaponTypeName = entry.getKey();
			String weaponId = entry.getValue();
			Integer indexObj = sortMap.get(weaponTypeName);
			if ( indexObj == null ) {
				logger.warn("No index for weaponType:{}", weaponTypeName);
			} else {
				XinqiBseGetUserBiblio.UserBiblio.Builder builder = XinqiBseGetUserBiblio.UserBiblio.newBuilder();
				builder.setIndex(indexObj.intValue());
				builder.setWeaponId(weaponId);
				listBuilder.addBiblio(builder.build());
				
				//logger.debug("Weapon:{}, index:{}", EquipManager.getInstance().getWeaponById(weaponId), indexObj);
			}
		}
		int total = 0;
		int unlocked = 0;
		if ( sortMap != null ) {
			total = sortMap.size();
			unlocked = biblio.getBiblio().size();
			listBuilder.setTotal(total);
			int percent = Math.round(unlocked*100.0f/total);
			if ( percent > 100 ) {
				percent = 100;
			}
			listBuilder.setPercent(percent);
			listBuilder.setRewarddesc(Text.text("biblio.reward.desc", percent));
			if ( percent == 100 ) {
				listBuilder.setRewardenable(true);
			}
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), listBuilder.build());
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.BiblioList, unlocked, total);
	}
	
	/**
	 * Take the user bibilo reward
	 * @param user
	 */
	public void takenUserBiblioReward(User user) {
		UserBiblio biblio = user.getBiblio();
		if ( biblio == null ) {
			biblio = new UserBiblio();
			user.setBiblio(biblio);
		}
		boolean success = false;
		String message = null;
		if ( !biblio.isTakenReward() ) {
			if ( biblio.getBiblio().size() >= powerMap.size() ) {
				success = true;
				message = Text.text("biblio.reward.success");
			} else {
				success = false;
				message = Text.text("biblio.reward.unfinished");
			}
		} else {
			success = false;
			message = Text.text("biblio.reward.taken");
		}
		BseTakeUserBiblioReward.Builder builder = BseTakeUserBiblioReward.newBuilder();
		if ( success ) {
			builder.setStatus(0);
			builder.setMessage(message);
			biblio.setTakenReward(success);
			ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.BIBLIO_REWARD, user);
			List rewardList = null;
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				rewardList = result.getResult();
			}
			RewardManager.getInstance().pickReward(user, rewardList, StatAction.BiblioTakeReward);
			saveUserBiblio(biblio);
		} else {
			builder.setStatus(1);
			builder.setMessage(message);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
	}
	
}
