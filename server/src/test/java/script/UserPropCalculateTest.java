package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;

public class UserPropCalculateTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBagWithWeapon() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		User expectUser = UserManager.getInstance().createDefaultUser();
		
		WeaponPojo weapon = new WeaponPojo();
	  weapon.setAddAttack(10); 
	  weapon.setAddDefend(20); 
	  weapon.setAddAgility(30); 
	  weapon.setAddLuck(40); 
	  weapon.setAddBlood(50);
	  weapon.setAddBloodPercent(60);
	  weapon.setAddThew(70);
	  weapon.setAddDamage(80);
	  weapon.setAddSkin(90);
	  
	  user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
	  user.getBag().addOtherPropDatas(weapon.toPropData(30, WeaponColor.WHITE));
	  user.getBag().wearPropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
		
		//default power = 2980
		assertTrue(user.getPower()>0);
		assertEquals(expectUser.getAttack()+weapon.getAddAttack(),user.getAttack());
		assertEquals(expectUser.getDefend()+weapon.getAddDefend(),user.getDefend());
		assertEquals(expectUser.getAgility()+weapon.getAddAgility(),user.getAgility());
		assertEquals(expectUser.getLuck()+weapon.getAddLuck(),user.getLuck());
		assertEquals((int)(
				(expectUser.getBlood()+weapon.getAddBlood())
					*(1.0+weapon.getAddBloodPercent()/100.0)
				),user.getBlood());
		assertEquals(expectUser.getTkew()+weapon.getAddThew()+6,user.getTkew());
		assertEquals(expectUser.getDamage()+weapon.getAddDamage(),user.getDamage());
		assertEquals(expectUser.getSkin()+weapon.getAddSkin(),user.getSkin());
	}
	
	@Test
	public void testBagUnwear() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		User expectUser = UserManager.getInstance().createDefaultUser();
		
		WeaponPojo weapon = new WeaponPojo();
	  weapon.setAddAttack(10); 
	  weapon.setAddDefend(20); 
	  weapon.setAddAgility(30); 
	  weapon.setAddLuck(40); 
	  weapon.setAddBlood(50);
	  weapon.setAddBloodPercent(60);
	  weapon.setAddThew(70);
	  weapon.setAddDamage(80);
	  weapon.setAddSkin(90);
	  
	  user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
	  user.getBag().addOtherPropDatas(weapon.toPropData(30, WeaponColor.WHITE));
	  user.getBag().wearPropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
		
		//default power = 2980
		assertTrue(user.getPower()>0);
		assertEquals(expectUser.getAttack()+weapon.getAddAttack(),user.getAttack());
		assertEquals(expectUser.getDefend()+weapon.getAddDefend(),user.getDefend());
		assertEquals(expectUser.getAgility()+weapon.getAddAgility(),user.getAgility());
		assertEquals(expectUser.getLuck()+weapon.getAddLuck(),user.getLuck());
		assertEquals((int)(
				(expectUser.getBlood()+weapon.getAddBlood())
					*(1.0+weapon.getAddBloodPercent()/100.0)
				),user.getBlood());
		assertEquals(expectUser.getTkew()+weapon.getAddThew()+6,user.getTkew());
		assertEquals(expectUser.getDamage()+weapon.getAddDamage(),user.getDamage());
		assertEquals(expectUser.getSkin()+weapon.getAddSkin(),user.getSkin());

		user.getBag().wearPropData(PropDataEquipIndex.WEAPON.index(), Bag.BAG_WEAR_COUNT);
		
		assertEquals(expectUser.getAttack(),user.getAttack());
		assertEquals(expectUser.getDefend(),user.getDefend());
		assertEquals(expectUser.getAgility(),user.getAgility());
		assertEquals(expectUser.getLuck(),user.getLuck());
 		assertEquals(expectUser.getTkew(),user.getTkew());
		assertEquals(expectUser.getDamage(),user.getDamage());
		assertEquals(expectUser.getSkin(),user.getSkin());
	}
	
	@Test
	public void testWearAndUnWearBloodEquip() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setBlood(100);
		User expectUser = UserManager.getInstance().createDefaultUser();
		expectUser.setBlood(100);
		
		WeaponPojo weapon = new WeaponPojo();
	  weapon.setAddBlood(100);
	  
	  user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
	  user.getBag().addOtherPropDatas(weapon.toPropData(30, WeaponColor.WHITE));
	  user.getBag().wearPropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
		
		//default power = 2980
		assertEquals(expectUser.getBlood()+weapon.getAddBlood(),user.getBlood());
		
		user.getBag().wearPropData(PropDataEquipIndex.WEAPON.index(), -1);
		
		assertEquals(expectUser.getBlood(),user.getBlood());
	}
	
	@Test
	public void testWearAndUnWearBloodPercentEquip() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setBlood(100);
		User expectUser = UserManager.getInstance().createDefaultUser();
		expectUser.setBlood(100);
		
		WeaponPojo weapon = new WeaponPojo();
	  weapon.setAddBloodPercent(15);
	  
	  user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
	  user.getBag().addOtherPropDatas(weapon.toPropData(30, WeaponColor.WHITE));
	  user.getBag().wearPropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
		
		//default power = 2980
		assertEquals(Math.round(expectUser.getBlood()* 1.15), user.getBlood());
		
		user.getBag().wearPropData(PropDataEquipIndex.WEAPON.index(), -1);
		
		assertEquals(expectUser.getBlood(),user.getBlood());
	}
}
