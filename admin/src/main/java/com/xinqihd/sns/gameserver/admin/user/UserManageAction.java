package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.GuildManagePanel;
import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class UserManageAction extends AbstractAction {
	
	public UserManageAction() {
		super("公会管理", ImageUtil.createImageSmallIcon("Site Map.png", "游戏公会管理"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_guild_manage;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(GuildManagePanel.getInstance());
			}
		});
	}

}
