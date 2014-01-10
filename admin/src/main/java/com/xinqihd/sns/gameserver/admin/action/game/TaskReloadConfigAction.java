package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskReloadConfigPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskSettingPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskReloadConfigAction extends AbstractAction {
	
	private static TaskReloadConfigAction instance = new TaskReloadConfigAction();
	
	private TaskReloadConfigPanel taskReloadConfigPanel = null;
	
	TaskReloadConfigAction() {
		super("配置文件管理", ImageUtil.createImageSmallIcon("Wrench.png", "配置文件管理"));
		this.taskReloadConfigPanel = TaskReloadConfigPanel.getInstance();
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_reload_config;
	}
	
	public static TaskReloadConfigAction getInstance() {
		return instance;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(taskReloadConfigPanel);
			}
		});
	}

}
