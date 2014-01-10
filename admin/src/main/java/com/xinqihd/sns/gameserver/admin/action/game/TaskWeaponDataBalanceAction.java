package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskWeaponDataBalancePanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskWeaponDataBalanceAction extends AbstractAction {
	
	public TaskWeaponDataBalanceAction() {
		super("战斗平衡测试器", ImageUtil.createImageSmallIcon("Application.png", "游戏战斗平衡测试器"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_weapon_balancer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskWeaponDataBalancePanel.getInstance());
			}
		});
	}

}
