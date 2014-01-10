package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskDailyMarkPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskDailyMarkAction extends AbstractAction {
	
	public TaskDailyMarkAction() {
		super("每日登陆打卡奖励", ImageUtil.createImageSmallIcon("Calendar.png", "每日登陆打卡奖励"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_dailymarks;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskDailyMarkPanel.getInstance());
			}
		});
	}

}
