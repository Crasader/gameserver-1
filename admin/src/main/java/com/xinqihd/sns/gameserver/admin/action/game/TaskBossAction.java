package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskBossPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskBossAction extends AbstractAction {
	
	public TaskBossAction() {
		super("副本怪物设定", ImageUtil.createImageSmallIcon("Game Pad.png", "副本的Boss数值设定"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_bosses;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskBossPanel.getInstance());
			}
		});
	}

}
