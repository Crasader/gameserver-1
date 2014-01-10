package com.xinqihd.sns.gameserver.db;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.Unlock;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.UserLoginStatus;
import com.xinqihd.sns.gameserver.db.mongo.MongoUserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is the interface to manipulate User entity.
 * @author wangqi
 *
 */
public abstract class UserManager {
	
	private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
	
	private static UserManager instance = new MongoUserManager();
	
	//580	黑铁●夺命刀
	public static String basicWeaponItemId = "580";
	
	//25102	新手助长礼包
	public static String basicUserGiftBoxId = "25102";

	protected static PropData basicPropData = null;
	protected static PropData giftPropData = null;
	
	protected static Pattern pattern = Pattern.compile("^s\\d\\d\\d\\d\\.");
	
	protected static User DEFAULT_USER;
	
	static {
		basicWeaponItemId = GlobalConfig.getInstance().getStringProperty("user.basic.weapon");
		
		WeaponPojo weaponPojo = EquipManager.getInstance().getWeaponById(basicWeaponItemId);
		basicPropData = weaponPojo.toPropData(Integer.MAX_VALUE, WeaponColor.WHITE, 5, null);
		ItemPojo giftPojo = ItemManager.getInstance().getItemById(basicUserGiftBoxId);
		giftPropData  = giftPojo.toPropData();
		
		DEFAULT_USER = new User();
		DEFAULT_USER.setDefaultUser(true);
		DEFAULT_USER.setUsername("godofwar");
		DEFAULT_USER.setRoleName("godofwar");
		DEFAULT_USER.setAttack(0);
		DEFAULT_USER.setAgility(0);
		DEFAULT_USER.setDefend(0);
		DEFAULT_USER.setLuck(0);
		DEFAULT_USER.setPowerSimple(0);
		DEFAULT_USER.setExpSimple(0);
		DEFAULT_USER.setLevelSimple(1);
		//Call script to upgrade properties
		ScriptManager.getInstance().runScript(ScriptHook.USER_LEVEL_UPGRADE, DEFAULT_USER, 1 );
	  // 金币
		DEFAULT_USER.setGoldenSimple(GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_DEFAULT_GOLDEN, 50000));
	  // 元宝
		DEFAULT_USER.setYuanbaoSimple(0);
		DEFAULT_USER.setYuanbaoFreeSimple(GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_DEFAULT_YUANBAO, 10));
	  // 礼券
//  DEFAULT_USER.setVoucher(GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_DEFAULT_VOUCHER, 0));
  // 勋章
//  DEFAULT_USER.setMedal(GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_DEFAULT_MEDAL, 0));
		
		DEFAULT_USER.getBag().addOtherPropDatas(basicPropData.clone());
		
		DEFAULT_USER.getBag().wearPropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
	}
	
	/**
	 * Use factory method to get it.
	 */
	protected UserManager(){
	}

	/**
	 * Create a new User entity with default settings.
	 * It is used when a new user is registering. 
	 */
	public User createDefaultUser() {
		User user = new User();
		user.setExpSimple(DEFAULT_USER.getExp());
		user.setLevelSimple(DEFAULT_USER.getLevel());
		
		user.setBlood(DEFAULT_USER.getBlood());
		user.setSkin(DEFAULT_USER.getSkin());
		user.setDamage(DEFAULT_USER.getDamage());
		user.setTkew(DEFAULT_USER.getTkew());
		user.setPowerSimple(DEFAULT_USER.getPower());
		
		user.setAttack(DEFAULT_USER.getAttack());
		user.setDefend(DEFAULT_USER.getDefend());
		user.setLuck(DEFAULT_USER.getLuck());
		user.setAgility(DEFAULT_USER.getAgility());
		
	  // 金币
	  user.setGoldenSimple(DEFAULT_USER.getGolden());
	  // 元宝
	  user.setYuanbaoSimple(0);
	  user.setYuanbaoFreeSimple(DEFAULT_USER.getYuanbaoFree());
	  
	  user.getBag().addOtherPropDatas(giftPropData.clone());
	  user.getBag().setWearPropData(basicPropData.clone(), 
	  		PropDataEquipIndex.WEAPON.index());
		return user;
	}
	
	/**
	 * The user for training room
	 * @return
	 */
	public User createTrainingUser(User realUser) {
		//1680	水果硬糖	Bubble0004
		User user = new User();
		user.setAI(true);
		String userName = Text.text("trainer.name");
		user.set_id(new UserId(userName, MathUtil.nextFakeInt(Integer.MAX_VALUE)));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGender(Gender.FEMALE);
		if ( realUser != null ) {
			user.setLevel(realUser.getLevel());
		}
		user.setBlood(realUser.getBlood()+100);
		/*
		user.setTkew(300);
		user.setPowerSimple(100);
		user.setAttack(100);
		user.setDefend(100);
		user.setAgility(100);
		user.setLuck(100);
		user.setDamage(100);
		user.setSkin(100);
		*/
		user.setIsvip(true);
		user.getBag().setWearPropData(
				UserManager.basicPropData, PropDataEquipIndex.WEAPON.index());
		WeaponPojo pojo = EquipManager.getInstance().getWeaponById("1680");
		if ( pojo != null ) {
			PropData bubble = pojo.toPropData(100, WeaponColor.WHITE);
			user.getBag().setWearPropData(bubble, PropDataEquipIndex.BUBBLE.index());
		}
		return user;
	}
	
	/**
	 * Save an User entity. If the user does not exists, insert it into database.
	 * Otherwise, update all the value in it. The data in bag and relation maybe
	 * stored in different tables/collections.
	 * 
	 * @param user
	 * @param isNewUser if true, save all fields in User object.
	 * @return 
	 */
	public abstract boolean saveUser(User user, boolean isNewUser);
	
	/**
	 * Save an User's Bag entity. If the user does not exists, insert it into database.
	 * Otherwise, update all the value in it. 
	 * 
	 * @param bag
	 * @return 
	 */
	public abstract boolean saveUserBag(User user, boolean isNewBag);
	
	/**
	 * Save an User's Relation entity. If the user does not exists, insert it into database.
	 * Otherwise, update all the value in it. 
	 * 
	 * @param relations
	 */
	public abstract boolean saveUserRelation(Collection<Relation> relations);
	
	/**
	 * Query a user by its name. Note the bag and relation are lazily got.
	 * @param userName
	 * @return
	 */
	public abstract User queryUser(String userName);
	
	/**
	 * Query a user by its userId. Note the bag and relation are lazily got.
	 * @param userId
	 * @return
	 */
	public abstract User queryUser(UserId userId);
	
	/**
	 * Query a BasicUser by its name
	 * @param userName
	 * @return
	 */
	public abstract BasicUser queryBasicUser(String userName);
	
	/**
	 * Query a BasicUser by its userId
	 * @param userId
	 * @return
	 */
	public abstract BasicUser queryBasicUser(UserId userId);
	
	/**
	 * Get the user's Bag from database.
	 * @param userName
	 * @return
	 */
	public abstract Bag queryUserBag(User user);
	
	/**
	 * Get the user's Bag from database.
	 * @param userId
	 * @return
	 */
	public abstract Bag queryUserBag(UserId userId);
	
	/**
	 * Get the user's relation from database.
	 * @param userName
	 * @return
	 */
	public abstract User queryUserRelation(User user);
	
	
	/**
	 * Check if the given username already exists.
	 * 
	 * @param userName
	 * @return
	 */
	public abstract boolean checkUserNameExist(String userName);
	
	
	/**
	 * It makes changing the underlying database easy.
	 * @return
	 */
	public static UserManager getInstance() {
		return instance;
	}
	
	/**
	 * Delete an user from database by his/her id, including all the bag and 
	 * relation data.
	 * 
	 * @param userId
	 * @return
	 */
	public abstract void removeUser(UserId userId);

	/**
	 * Delete an user from database by his/her name, including all the bag and 
	 * relation data.
	 * 
	 * @param userId
	 * @return
	 */
	public abstract void removeUser(String userName);

	/**
	 * Query user by his role name
	 * 
	 * @param roleName
	 * @return
	 */
	public abstract User queryUserByRoleName(String roleName);

	/**
	 * Check if the role name is already exist
	 * @param roleName
	 * @return
	 */
	public abstract boolean checkRoleNameExist(String roleName);

	/**
	 * Query the basic user by roleName
	 * @param roleName
	 * @return
	 */
	public abstract BasicUser queryBasicUserByRoleName(String roleName);

	/**
	 * Check if the email is already verified
	 * @param userId
	 * @return
	 */
	public abstract boolean checkEmailVerified(UserId userId);

	/**
	 * Store the win or lose status with friends
	 * @param user
	 * @param relationType
	 * @param friendRoleName
	 * @param winning
	 * @return
	 */
	public abstract Map<RelationType, Collection<People>> saveFriendWinOrLose(User user, 
			User friend, boolean winning);

	/**
	 * Get all unlocked object for given user.
	 * @param user
	 * @return
	 */
	public abstract Collection<Unlock> queryUserUnlock(User user);

	/**
	 * Unlock a given function
	 * @param user
	 * @param unlock
	 * @return
	 */
	public abstract boolean addUserNewUnlock(User user, Unlock unlock);

	/**
	 * Convert an User object to json style string, 
	 * for backup purpose.
	 * 
	 * @param user
	 * @return
	 */
	public abstract String convertUserToString(User user);
	
	/**
	 * Convert a Bag object to json style string,
	 * for backup purpose.
	 * 
	 * @param bag
	 * @return
	 */
	public abstract User convertStringToUser(String userStr);
	
	/**
	 * 
	 * @param bagStr
	 * @return
	 */
	public abstract Bag convertStringToBag(String bagStr);

	/**
	 * 
	 * @param bag
	 * @return
	 */
	public abstract String convertBagToString(Bag bag);
	
	/**
	 * Remove the server prefix and display the role name.
	 */
	public static final String getDisplayRoleName(String roleName) {
		/*
		if ( roleName != null ) {
			Matcher matcher = pattern.matcher(roleName);
			String r = matcher.replaceFirst(Constant.EMPTY);
			return r;
		} else {
			return Constant.EMPTY;
		}
		*/
		return roleName;
	}

	public abstract ArrayList<BasicUser> queryBasicUserByRoleNameRegex(String roleName);

	/**
	 * 
	 * @param roleName
	 */
	public abstract void removeUserByRoleName(String roleName);

	/**
	 * 
	 * @param userId
	 * @return
	 */
	public abstract UserLoginStatus checkUserLoginStatus(UserId userId);

	public abstract String queryUserGuildId(UserId userId);

	public abstract boolean removeUserGuildId(UserId userId);
	
	/**
	 * Serialize the User to bytes
	 * @param user
	 * @return
	 */
	/*
	public byte[] serializeUser(User user) {
		try {
			Kryo kryo = new Kryo();
			//kryo.register(User.class);
			//kryo.register(UserId.class);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Output output = new Output(baos);
			kryo.writeObject(output, user);
			output.close();
			return baos.toByteArray();
		} catch (KryoException e) {
			logger.warn("Failed to serialize user.", e);
		}
		return null;
	}
	*/
	
	/**
	 * Deserialize the bytes to User object.
	 * @param userBytes
	 * @return
	 */
	/*
	public User deserializeUser(byte[] userBytes) {
		try {
			Kryo kryo = new Kryo();
			Input input = new Input(userBytes);
			User user = kryo.readObject(input, User.class);
			input.close();
			return user;
		} catch (Exception e) {
			logger.warn("Failed to deserialize user.", e);
		}
		return null;
	}
	*/
}
