package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskMapPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskMapAction extends AbstractAction {
	
	public TaskMapAction() {
		super("游戏地图管理", ImageUtil.createImageSmallIcon("Image.png", "游戏地图管理"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_map;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskMapPanel.getInstance());
			}
		});
	}

}
