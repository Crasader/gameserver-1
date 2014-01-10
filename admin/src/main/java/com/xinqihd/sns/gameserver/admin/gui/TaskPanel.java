package com.xinqihd.sns.gameserver.admin.gui;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXTaskPane;

import com.xinqihd.sns.gameserver.admin.action.game.GuildManageAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskBossAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskCDKeyAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskChargeAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskCraftTestAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskDailyMarkAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskEquipmentAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskExitGameAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskGameDataAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskGameTaskAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskItemAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskLevelAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskLoginAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskMapAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskPromotionAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskRedisDBAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskReloadConfigAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskRewardAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskRewardLevelAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskServerListAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskShop2Action;
import com.xinqihd.sns.gameserver.admin.action.game.TaskShopDataGeneratorAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskTipAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskUUIDAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskVipAction;
import com.xinqihd.sns.gameserver.admin.action.game.TaskWeaponDataBalanceAction;
import com.xinqihd.sns.gameserver.admin.action.setting.TaskSettingAction;
import com.xinqihd.sns.gameserver.admin.data.WeaponDataGeneratorDataAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.user.UserManageAction;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

/**
 * The FunctionPanel is a list of all functions supported by this system.
 * @author wangqi
 *
 */
public class TaskPanel extends MyPanel {
	
	JXTaskPane reportTaskPane = null;
	JXTaskPane userTaskPane = null;
	JXTaskPane dataTaskPane = null;
	JXTaskPane dataBalanceTaskPane = null;
	JXTaskPane serverTaskPane = null;
	
	public static TaskPanel instance = null;
	
	/**
	 * Create the panel.
	 */
	public TaskPanel() {
		init();
		instance = this;
	}
	
	public static TaskPanel getInstance() {
		return instance;
	}

	public void init() {
//		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		MyPanel panel = new MyPanel();
		panel.setLayout(new MigLayout("wrap 1, width 100%"));
		
		reportTaskPane = new JXTaskPane("统计分析组", ImageUtil.createImageIcon("Dashboard.png", "Users"));
		reportTaskPane.setFont(MainFrame.BIG_FONT);
		
		panel.add(reportTaskPane, "growx");
		
		//User group
		userTaskPane = new JXTaskPane("用户管理组", ImageUtil.createImageIcon("Users.png", "Users"));
		userTaskPane.setFont(MainFrame.BIG_FONT);
//		Action userListAction = new RefreshAction();
//		userTaskPane.add(userListAction);
		Action userManageAction = new UserManageAction();
		userTaskPane.add(userManageAction);
		Action guildManageAction = new GuildManageAction();
		userTaskPane.add(guildManageAction);
		panel.add(userTaskPane, "growx");
		
		//Game group
		dataTaskPane = new JXTaskPane("游戏数值组", ImageUtil.createImageIcon("Game Pad.png", "Users"));
		dataTaskPane.setFont(MainFrame.BIG_FONT);
		
		Action gameDataAction = new TaskGameDataAction();
		dataTaskPane.add(gameDataAction);
		Action gameLevelAction = new TaskLevelAction();
		dataTaskPane.add(gameLevelAction);
		//Action equipmentOldAction = new TaskEquipmentOldAction();
		//dataTaskPane.add(equipmentOldAction);
		Action equipmentAction = new TaskEquipmentAction();
		dataTaskPane.add(equipmentAction);
		Action itemAction = new TaskItemAction();
		dataTaskPane.add(itemAction);
		//Action shopAction = new TaskShopAction();
		//dataTaskPane.add(shopAction);
		Action shop2Action = new TaskShop2Action();
		dataTaskPane.add(shop2Action);
		Action gameTaskAction = new TaskGameTaskAction();
		dataTaskPane.add(gameTaskAction);
		Action gameMapAction = new TaskMapAction();
		dataTaskPane.add(gameMapAction);
		Action chargeAction = new TaskChargeAction();
		dataTaskPane.add(chargeAction);
		Action vipAction = new TaskVipAction();
		dataTaskPane.add(vipAction);
		Action tipAction = new TaskTipAction();
		dataTaskPane.add(tipAction);
		Action promotionAction = new TaskPromotionAction();
		dataTaskPane.add(promotionAction);
		Action bossAction = new TaskBossAction();
		dataTaskPane.add(bossAction);
		Action serverListAction = new TaskServerListAction();
		dataTaskPane.add(serverListAction);
		Action rewardsAction = new TaskRewardAction();
		dataTaskPane.add(rewardsAction);
		Action rewardLevelAction = new TaskRewardLevelAction();
		dataTaskPane.add(rewardLevelAction);
		Action exitGameAction = new TaskExitGameAction();
		dataTaskPane.add(exitGameAction);
		Action cdkeyAction = new TaskCDKeyAction();
		dataTaskPane.add(cdkeyAction);
		Action dailyMarkAction = new TaskDailyMarkAction();
		dataTaskPane.add(dailyMarkAction);
		Action uuidAction = new TaskUUIDAction();
		dataTaskPane.add(uuidAction);
		panel.add(dataTaskPane, "growx");
		Action loginAction = new TaskLoginAction();
		dataTaskPane.add(loginAction);
		
		//Data balance
		dataBalanceTaskPane = new JXTaskPane("数值计算组", ImageUtil.createImageIcon("Keyboard.png", "Users"));
		dataBalanceTaskPane.setFont(MainFrame.BIG_FONT);
		Action weaponGenerator = new WeaponDataGeneratorDataAction();
		dataBalanceTaskPane.add(weaponGenerator);
		Action weaponBalancer = new TaskWeaponDataBalanceAction();
		dataBalanceTaskPane.add(weaponBalancer);
		Action craftBalancer = new TaskCraftTestAction();
		dataBalanceTaskPane.add(craftBalancer);
		Action shopDataGenerator = new TaskShopDataGeneratorAction();
		dataBalanceTaskPane.add(shopDataGenerator);
		panel.add(dataBalanceTaskPane, "growx");
		
		//Server group
		serverTaskPane = new JXTaskPane("服务管理组", ImageUtil.createImageIcon("Gears.png", "Users"));
		serverTaskPane.setFont(MainFrame.BIG_FONT);
		Action settingAction = TaskSettingAction.getInstance();
		serverTaskPane.add(settingAction);
		Action redisDBAction = new TaskRedisDBAction();
		serverTaskPane.add(redisDBAction);
		Action reloadAction = TaskReloadConfigAction.getInstance();
		serverTaskPane.add(reloadAction);
		panel.add(serverTaskPane, "growx");
		
		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		this.setLayout(new MigLayout("gap 0, ins 0"));
		this.add(scrollPane, "width 100%, height 100%, grow");
	}
	
	
}
