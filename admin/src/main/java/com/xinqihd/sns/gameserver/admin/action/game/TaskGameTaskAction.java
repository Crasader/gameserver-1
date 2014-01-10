package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskGameTaskPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskShopPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskGameTaskAction extends AbstractAction {
	
	public TaskGameTaskAction() {
		super("游戏任务管理", ImageUtil.createImageSmallIcon("Tag.png", "游戏任务管理"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_map;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskGameTaskPanel.getInstance());
			}
		});
	}

}
