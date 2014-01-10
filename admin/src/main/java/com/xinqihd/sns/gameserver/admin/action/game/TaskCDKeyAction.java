package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskCDKeyPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskCDKeyAction extends AbstractAction {
	
	public TaskCDKeyAction() {
		super("CDKEY管理", ImageUtil.createImageSmallIcon("Key.png", "CDKEY管理"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_cdkeys;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskCDKeyPanel.getInstance());
			}
		});
	}

}
