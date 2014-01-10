package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskVipPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.redis.RedisConsole;

public class TaskRedisDBAction extends AbstractAction {
	
	public TaskRedisDBAction() {
		super("Redis数据库管理", ImageUtil.createImageSmallIcon("Database.png", "Redis数据库管理"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_redisdb;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(RedisConsole.getInstance());
			}
		});
	}

}
