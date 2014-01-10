package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskShopPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskShopAction extends AbstractAction {
	
	public TaskShopAction() {
		super("游戏商城管理", ImageUtil.createImageSmallIcon("Shopping Cart.png", "游戏商城管理"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_shop;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskShopPanel.getInstance());
			}
		});
	}

}
