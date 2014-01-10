package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class MongoDBUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConstructMapObject() throws Exception {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(30, WeaponColor.ORGANCE);
		propData = EquipCalculator.weaponUpLevel(propData, 12);
		propData.setDuration(100);
		
		String userName = "test001";
		User user = prepareUser(userName);
		user.getBag().addOtherPropDatas(propData);
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		User actualUser = UserManager.getInstance().queryUser(userName);
		Bag actualBag = UserManager.getInstance().queryUserBag(actualUser);
		PropData actualData = actualBag.getOtherPropData(20);
		
		assertEquals(propData.toDetailString(), actualData.toDetailString());
	}

	private User prepareUser(String userName) throws Exception {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = new User();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		String password = StringUtil.encryptSHA1(userName);
		user.setPassword(password);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
