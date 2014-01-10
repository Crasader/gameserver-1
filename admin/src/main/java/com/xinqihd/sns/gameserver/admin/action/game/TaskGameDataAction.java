package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskGameDataPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskGameDataAction extends AbstractAction {
	
	public TaskGameDataAction() {
		super("游戏基本数值设定", ImageUtil.createImageSmallIcon("Calculator.png", "游戏基本数值设定"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_data;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskGameDataPanel.getInstance());
			}
		});
	}

}
