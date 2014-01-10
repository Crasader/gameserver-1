package com.xinqihd.sns.gameserver.service;

import java.util.HashSet;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.StatusBar;
import com.xinqihd.sns.gameserver.admin.user.UserBagDialog;
import com.xinqihd.sns.gameserver.admin.util.MyWindowUtil;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;

/**
 * 查询用户的背包数据
 * @author wangqi
 *
 */
public class UserBagQueryService extends SwingWorker<Void, Void> {
	
	private User   user   = null;
	private String databaseName = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
	private String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	private String collection = "bags";
	private HashSet<String> hiddenFields = new HashSet<String>();
	
	public UserBagQueryService(User user) {
		hiddenFields.add("_id");
		this.user = user;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		StatusBar statusBar = MainPanel.getInstance().getStatusBar();
		statusBar.updateStatus("读取玩家背包数据");
		statusBar.progressBarAnimationStart();
		
		try {
			if ( this.user != null ) {
				UserManager.getInstance().queryUserBag(user);
			} else {
				JOptionPane.showMessageDialog(null, "未指定要查询的用户");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		statusBar.progressBarAnimationStop();
		statusBar.updateStatus("读取玩家背包数据完毕");
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		UserBagDialog panel = new UserBagDialog(this.user);
		panel.setVisible(true);
		
	}

}
