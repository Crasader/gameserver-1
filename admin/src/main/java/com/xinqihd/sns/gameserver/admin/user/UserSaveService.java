package com.xinqihd.sns.gameserver.admin.user;

import java.util.HashMap;
import java.util.Set;

import javax.swing.SwingWorker;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.StatusBar;
import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;
import com.xinqihd.sns.gameserver.admin.model.DBObjectTreeTableNode;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.entity.user.UserId;

public class UserSaveService extends SwingWorker<Void, Void> {
	
	private StatusBar statusBar = MainPanel.getInstance().getStatusBar();
	private UserTreeTableModel model = null;
	private String databaseName = null;
	private String namespace = null;
	private String collection = "users";
	private UserManagePanel panel = null;
	
	public UserSaveService(UserTreeTableModel model, UserManagePanel panel) {
		this.model = model;
		this.panel = panel;
		this.databaseName = ConfigManager.getConfigAsString(ConfigKey.mongoDBName);
		this.namespace = ConfigManager.getConfigAsString(ConfigKey.mongoNamespace);
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		statusBar.updateStatus("保存用户数据");
		statusBar.progressBarAnimationStart();
		HashMap<UserId, DBObjectTreeTableNode> map = model.getChangedMap();
		Set<UserId> set = map.keySet();
		for ( UserId userId : set ) {
			DBObject query = MongoUtil.createDBObject("_id", userId.getInternal());
			DBObject objectToSave = (DBObject)map.get(userId).getUserObject();
			MongoUtil.saveToMongo(query, objectToSave, databaseName, namespace, collection, true);
		}
		map.clear();
		statusBar.progressBarAnimationStop();
		statusBar.updateStatus("保存用户数据");
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		if ( this.panel != null ) {
			this.panel.updateButtonStatus();
		}
		statusBar.progressBarAnimationStop();
		statusBar.updateStatus("保存用户数据完毕");
	}

}
