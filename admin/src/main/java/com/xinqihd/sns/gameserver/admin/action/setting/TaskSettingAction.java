package com.xinqihd.sns.gameserver.admin.action.setting;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskSettingPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskSettingAction extends AbstractAction {
	
	private static TaskSettingAction instance = new TaskSettingAction();
	
	private TaskSettingPanel taskSettingPanel = null;
	
	TaskSettingAction() {
		super("系统设置管理", ImageUtil.createImageSmallIcon("Wrench.png", "系统设置管理"));
		this.taskSettingPanel = TaskSettingPanel.getInstance();
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_map;
	}
	
	public static TaskSettingAction getInstance() {
		return instance;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(taskSettingPanel);
			}
		});
	}

}
