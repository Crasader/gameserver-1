package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskPromotionPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskPromotionAction extends AbstractAction {
	
	public TaskPromotionAction() {
		super("活动公告设定", ImageUtil.createImageSmallIcon("Desktop.png", "活动公告设定"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_promotions;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskPromotionPanel.getInstance());
			}
		});
	}

}
