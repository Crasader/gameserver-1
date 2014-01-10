package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskLoginPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskLoginAction extends AbstractAction {
	
	public TaskLoginAction() {
		super("游戏登陆设定", ImageUtil.createImageSmallIcon("Traffic Lights.png", "游戏登陆设定"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_logins;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskLoginPanel.getInstance());
			}
		});
	}

}
