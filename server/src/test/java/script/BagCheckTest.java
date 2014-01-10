package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;

public class BagCheckTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCheckPropData() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById("4049");
		PropData propData = weapon.toPropData(30, WeaponColor.ORGANCE);
		propData.setBaseAttack(3000);
		propData.setBaseDefend(3000);
		propData.setBaseAgility(3000);
		propData.setBaseLuck(3000);
		EquipCalculator.weaponUpLevel(propData, 12);
		/**
			 * 20005	水神石Lv5
			 * 20010	土神石Lv5
			 * 20015	风神石Lv5
			 * 20020	火神石Lv5
			 * 20025	强化石Lv5
		 */
		PropDataSlot slot = new PropDataSlot();
		slot.setSlotType(PropDataEnhanceField.ATTACK);
		EquipCalculator.calculateForgeData(propData, 5, ItemManager.attackStoneId, slot);
		EquipCalculator.calculateForgeData(propData, 5, ItemManager.defendStoneId, slot);
		EquipCalculator.calculateForgeData(propData, 5, ItemManager.agilityStoneId, slot);
		EquipCalculator.calculateForgeData(propData, 5, ItemManager.luckStoneId, slot);
		
		System.out.println(propData.toDetailString());
		
		BagCheck.checkPropData(propData);
		System.out.println(propData.toDetailString());
		
		fail("Not yet implemented");
	}

}
