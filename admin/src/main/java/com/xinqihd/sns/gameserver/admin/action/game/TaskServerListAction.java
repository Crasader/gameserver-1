package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskServerListPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskServerListAction extends AbstractAction {
	
	public TaskServerListAction() {
		super("服务器开服管理", ImageUtil.createImageSmallIcon("Construction.png", "服务器开服管理"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_serverlist;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskServerListPanel.getInstance());
			}
		});
	}

}
