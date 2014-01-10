package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskExitGamePanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskRewardPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskServerListPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskExitGameAction extends AbstractAction {
	
	public TaskExitGameAction() {
		super("退出游戏管理", ImageUtil.createImageSmallIcon("Present.png", "退出游戏管理"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_rewards;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskExitGamePanel.getInstance());
			}
		});
	}

}
