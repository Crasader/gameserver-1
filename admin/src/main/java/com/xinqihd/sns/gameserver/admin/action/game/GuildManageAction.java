package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskTipPanel;
import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class GuildManageAction extends AbstractAction {
	
	public GuildManageAction() {
		super("游戏用户管理", ImageUtil.createImageSmallIcon("User.png", "游戏用户管理"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_user_manage;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(UserManagePanel.getInstance());
			}
		});
	}

}
