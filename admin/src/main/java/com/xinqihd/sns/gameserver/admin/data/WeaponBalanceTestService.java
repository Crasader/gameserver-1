package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.battle.BuffToolIndex;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 生成模拟的玩家数据，并进行战斗测试
 * @author wangqi
 *
 */
public class WeaponBalanceTestService extends SwingWorker<Void, Integer> {
	
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在生成模拟玩家数据");
	private Stage stage = Stage.INIT;
	private WeaponBalanceResultModel model = null;
	private WeaponBalanceTestConfig config1 = null;
	private WeaponBalanceTestConfig config2 = null;
	private int count = 0;
	private int levelDiff = 5;
	private int strengthDiff = 5;
	private MyTablePanel tablePanel;
	
	public WeaponBalanceTestService(WeaponBalanceResultModel model, 
			WeaponBalanceTestConfig config1, WeaponBalanceTestConfig config2, int count,
			int levelDiff, int strengthDiff, MyTablePanel tablePanel) {
		this.model = model;
		this.config1 = config1;
		this.config2 = config2;
		this.count = count;
		this.levelDiff = levelDiff;
		this.strengthDiff = strengthDiff;
		this.tablePanel = tablePanel;
		dialog = new JDialog();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		try {
			stage = Stage.INIT;
			//initialize
			publish();
			Thread.sleep(100);
			
			stage = Stage.DO_BALANCE_TEST;
			for ( int i=0; i<count; i++ ) {
				User user1 = createRandomUser(config1, -1, levelDiff, -1, strengthDiff);
				user1.setUsername("玩家1");
				User user2 = createRandomUser(config2, user1.getLevel(), levelDiff, -1, strengthDiff);
				user2.setUsername("玩家2");
				publish(i);
				
			  WeaponBalanceResult result = simulateCombat(user1, user2, config1, config2);
				
				this.model.insertRow(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static WeaponBalanceResult simulateCombat(
			User user1, User user2, WeaponBalanceTestConfig config1, 
			WeaponBalanceTestConfig config2 ) {
		//计算玩家的敏捷值能产生的最大攻击倍率
		WeaponBalanceResult result = new WeaponBalanceResult();
		result.setUser1(user1);
		result.setUser2(user2);
		int roundNumber = 0;
		boolean user1Attack = true;
		if ( user1.getAgility() < user2.getAgility() ) {
			user1Attack = false;
		}
		int user1Blood = user1.getBlood();
		int user2Blood = user2.getBlood();
		int totalHurt = 0;
		int user1Win = 0;
		int user1Hurt = 0;
		int user2Hurt = 0;
		while ( user1Blood > 0 && user2Blood > 0 ) {
			double userCriticalRatio = 0;
			double userAttackRatio = 0;
			int blood = 0;
			int hurt = 0;
			//模拟情况下玩家使用道具的方法是一样的，所以默认DELAY相同，因此平均每用户一回合。
			if ( user1Attack ) {
				userAttackRatio = 0.8;
				ArrayList<BuffToolIndex> tools = UserCalculator.calculateMaxHurtRatio(user1);
				userCriticalRatio = UserCalculator.calculateCritialAttack(user1);
				hurt = UserCalculator.calculateHurt(user1, user2, tools, userAttackRatio, userCriticalRatio);
				user2Blood -= hurt;
				user1Hurt += hurt;
				blood = user2Blood;
				user1Attack = false;
			} else {
				userAttackRatio = 0.8;
				ArrayList<BuffToolIndex> tools = UserCalculator.calculateMaxHurtRatio(user2);
				userCriticalRatio = UserCalculator.calculateCritialAttack(user2);
				hurt = UserCalculator.calculateHurt(user2, user1, tools, userAttackRatio, userCriticalRatio);
				user1Blood -= hurt;
				user2Hurt += hurt;
				blood = user1Blood;
				user1Attack = true;
			}
			roundNumber++;
			totalHurt += hurt;
			
			HashMap<String, Object> detail = new HashMap<String, Object>();
			detail.put(WeaponBalanceResult.ATT_RATIO, userAttackRatio);
			detail.put(WeaponBalanceResult.ATTACK,  user1Attack);
			detail.put(WeaponBalanceResult.BLOOD,  blood);
			detail.put(WeaponBalanceResult.CRI_RATIO,  userCriticalRatio);
			detail.put(WeaponBalanceResult.HURT, hurt);
			detail.put(WeaponBalanceResult.ROUND, roundNumber);
			result.addDetail(detail);
			if ( roundNumber>=10 && totalHurt <= 0 ) {
				user1Win = -1;
				break;
			}
		}
		result.setRoundCount(roundNumber);
		result.setConfig1(config1);
		result.setConfig2(config2);
		if ( user1Win == -1 ) {
			result.setUser1Win(-1);
		} else {
			result.setUser1Win(user1Blood>0?0:1);
		}
		int user1Exp = (int)UserCalculator.calculateBattleExp(user1Hurt, user1.getBlood(), 
				2, 0.8f, user2.getPower()*1.0f/user1.getPower(), 2, result.getUser1Win()==0, true, true);
		int user2Exp = (int)UserCalculator.calculateBattleExp(user2Hurt, user2.getBlood(), 
				2, 0.8f, user1.getPower()*1.0f/user2.getPower(), 2, result.getUser1Win()==1, true, true);
		result.setUser1Exp(user1Exp);
		result.setUser2Exp(user2Exp);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<Integer> chunks) {
		if ( stage == Stage.INIT ) {
			panel = new JXPanel();
			panel.setLayout(new MigLayout("wrap 1"));
			panel.add(label, "growx, wrap 20");
			panel.add(progressBar, "grow, push");
			
			dialog.add(panel);
			dialog.setSize(300, 120);
			Point p = WindowUtils.getPointForCentering(dialog);
			dialog.setLocation(p);
			dialog.setModal(true);
			dialog.setResizable(false);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			
			label.setFont(MainFrame.BIG_FONT);
			progressBar.setMaximum(count);
			progressBar.setStringPainted(true);
			
			dialog.setVisible(true);
		} else if ( stage == Stage.DO_BALANCE_TEST ) {
			label.setText("正在模拟战斗...");
			if ( chunks != null && chunks.size()>0 ) {
				int percent = chunks.get(chunks.size()-1);
				progressBar.setValue(percent);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		this.dialog.dispose();
		this.tablePanel.getTable().packAll();
	}

	static enum Stage {
		INIT,
		DO_BALANCE_TEST,
	}
	
	/**
	 * 创建随机用户
	 * @return
	 */
	private User createRandomUser(WeaponBalanceTestConfig config, 
			int lastUserLevel, int levelDiff, int lastStrengthLevel, int strengthDiff) {
		User user = UserManager.getInstance().createDefaultUser();;
		
		Random random = new Random();
		int minLevel = config.getMinUserLevel();
		int maxLevel = config.getMaxUserLevel();
		int userLevel = minLevel;
		if ( maxLevel > minLevel ) {
			if ( lastUserLevel == -1 || levelDiff == -1 ) {
				userLevel = minLevel + random.nextInt(maxLevel - minLevel);
			} else if ( levelDiff > 0 ){
				int base = lastUserLevel - levelDiff;
				if ( base < 0 ) base = 0;
				userLevel = base + random.nextInt(levelDiff*2);
			} else {
				userLevel = lastUserLevel;
			}
		}
		int totalExp = 0;
		for ( int i=0; i<userLevel; i++ ) {
			LevelPojo level = LevelManager.getInstance().getLevel(i);
			totalExp += level.getExp();
		}
		user.setExp(totalExp);
		
		if ( config.isUseOtherEquips() ) {
			PropDataEquipIndex[] equips = PropDataEquipIndex.values();
			for ( int i=0; i<equips.length; i++ ) {
				EquipType type = null;
				switch ( equips[i] ) {
					case BRACELET1:
						type = EquipType.DECORATION;
						break;
					case BRACELET2:
						type = EquipType.DECORATION;
						break;
					case BUBBLE:
						type = EquipType.BUBBLE;
						break;
					case CLOTH:
						type = EquipType.CLOTHES;
						break;
					case EYE:
						type = EquipType.EXPRESSION;
						break;
					case FACE:
						type = EquipType.FACE;
						break;
					case GLASS:
						type = EquipType.GLASSES;
						break;
					case HAIR:
						type = EquipType.HAIR;
						break;
					case HAT:
						type = EquipType.HAT;
						break;
					case NECKLACE:
						type = EquipType.DECORATION;
						break;
					case RING1:
						type = EquipType.JEWELRY;
						break;
					case RING2:
						type = EquipType.JEWELRY;
						break;
					case SUIT:
						type = EquipType.SUIT;
						break;
					case WEAPON:
						type = EquipType.WEAPON;
						break;
					case WEDRING:
						type = EquipType.JEWELRY;
						break;
					case WING:
						type = EquipType.WING;
						break;
				}
				Collection<WeaponPojo> slot = EquipManager.getInstance().getWeaponsBySlot(type);
				if ( slot != null ) {
					Object[] weaponObjs = MathUtil.randomPick(slot, 1);
					PropData propData = null;
					WeaponPojo weapon = (WeaponPojo)weaponObjs[0];
					weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weapon.getTypeName(), user.getLevel());
					propData = weapon.toPropData(10, WeaponColor.WHITE);
					user.getBag().addOtherPropDatas(propData);
					user.getBag().wearPropData(propData.getPew(), equips[i].index());
				}
			}
		}
		if ( !config.isUseRandomWeapon() ) {
			WeaponPojo selectedWeapon = config.getSelectedWeapon();
			if ( selectedWeapon != null ) {
//				WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(selectedWeapon.getTypeName(), user.getLevel());
				WeaponPojo weapon = selectedWeapon;
				PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
				user.getBag().wearPropData(PropDataEquipIndex.WEAPON.index(), -1);
				user.getBag().addOtherPropDatas(propData);
				user.getBag().wearPropData(propData.getPew(), PropDataEquipIndex.WEAPON.index());
			} else {
			}
		} else {
			Collection<WeaponPojo> slot = EquipManager.getInstance().getWeaponsBySlot(EquipType.WEAPON);
			Object[] weaponObjs = MathUtil.randomPick(slot, 1);
			WeaponPojo weapon = (WeaponPojo)weaponObjs[0];
			weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weapon.getTypeName(), user.getLevel());
			PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
			user.getBag().wearPropData(PropDataEquipIndex.WEAPON.index(), -1);
			user.getBag().addOtherPropDatas(propData);
			user.getBag().wearPropData(propData.getPew(), PropDataEquipIndex.WEAPON.index());		
		}
		
		if ( config.isUseStrength() ) {
			/**
			 * 对于强化，应该先卸载武器，强化后再装备武器，否则基础数值会被计算2次
			 */
			int strengthMin = config.getStrengthMin();
			int strengthMax = config.getStrengthMax();
			if ( strengthMin >= strengthMax ) {
				strengthMin -= 1;
			}
			if ( strengthMin < 1 ) {
				strengthMin = 1;
			}
			int strengthMinMaxDiff = strengthMax - strengthMin;
			if ( strengthMinMaxDiff <= 0 ) {
				strengthMin = 0;
				strengthMinMaxDiff = 12;
			}
			//强化武器
			int strengthLevel = strengthMin+random.nextInt(strengthMinMaxDiff);
			if ( lastStrengthLevel != -1 && strengthDiff != -1 ) {
				int base = lastStrengthLevel - strengthDiff;
				if ( base < 0 ) {
					base = 0;
				}
				strengthLevel = base + random.nextInt(lastStrengthLevel + strengthDiff- base);
			}

			Bag bag = user.getBag();
			List<PropData> props = bag.getWearPropDatas();
			for ( PropDataEquipIndex index : PropDataEquipIndex.values() ) {
				PropData prop = props.get(index.index());
				if ( prop != null ) {
					//卸载武器
					bag.wearPropData(index.index(), -1);
					script.WeaponLevelUpgrade.func(new Object[]{prop, strengthLevel});
					//装备武器
					bag.wearPropData(prop.getPew(), index.index());
				}
			}
		}
		
		int stoneLevel = 1;
		double luckyCardRatio = 0.0;
		/*
		"1级合成",
		"1级合成+15%",
		"1级合成+25%",
		"2级合成",
		"2级合成+15%",
		"2级合成+25%",
		"3级合成",
		"3级合成+15%",
		"3级合成+25%",
		"4级合成",
		"4级合成+15%",
		"4级合成+25%",
		"5级合成",
		"5级合成+15%",
		"5级合成+25%",
		 */
		String composeStone = config.getComposeStone();
		stoneLevel = composeStone.charAt(0) - '0';
		if ( composeStone.indexOf("15%")>0 ) {
			luckyCardRatio = 0.15;
		} else if ( composeStone.indexOf("25%")>0 ) {
			luckyCardRatio = 0.25;
		}
		
		Bag bag = user.getBag();
		String stoneTypeId = null;
		List<PropData> props = bag.getWearPropDatas();
		for ( PropDataEquipIndex index : PropDataEquipIndex.values() ) {
			PropData prop = props.get(index.index());
			if ( prop != null ) {
				//卸载武器
				bag.wearPropData(index.index(), -1);
				if ( config.isAttackCompose() ) {
					stoneTypeId = ItemManager.attackStoneId;
					EquipCalculator.calculateForgeData(prop, stoneLevel, stoneTypeId);
				}
				if ( config.isDefendCompose() ) {
					stoneTypeId = ItemManager.defendStoneId;
					EquipCalculator.calculateForgeData(prop, stoneLevel, stoneTypeId);
				}
				if ( config.isAgilityCompose() ) {
					stoneTypeId = ItemManager.agilityStoneId;
					EquipCalculator.calculateForgeData(prop, stoneLevel, stoneTypeId);
				}
				if ( config.isLuckCompose() ) {
					stoneTypeId = ItemManager.luckStoneId;
					EquipCalculator.calculateForgeData(prop, stoneLevel, stoneTypeId);
				}
				//装备武器
				bag.wearPropData(prop.getPew(), index.index());
			}
		}
		return user;
	}

}
