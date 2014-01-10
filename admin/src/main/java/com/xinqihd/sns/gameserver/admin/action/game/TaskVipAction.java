package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskVipPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskVipAction extends AbstractAction {
	
	public TaskVipAction() {
		super("游戏VIP价格数据", ImageUtil.createImageSmallIcon("User.png", "游戏VIP价格数据"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_vipperiods;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskVipPanel.getInstance());
			}
		});
	}

}
