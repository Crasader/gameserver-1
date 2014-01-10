package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskItemPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskItemAction extends AbstractAction {
	
	public TaskItemAction() {
		super("游戏道具数据设定", ImageUtil.createImageSmallIcon("Database.png", "游戏道具数据设定"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_item;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskItemPanel.getInstance());
			}
		});
	}

}
