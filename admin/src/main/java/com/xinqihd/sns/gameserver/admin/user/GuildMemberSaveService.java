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

public class GuildMemberSaveService extends SwingWorker<Void, Void> {
	
	private StatusBar statusBar = MainPanel.getInstance().getStatusBar();
	private GuildMemberTreeTableModel model = null;
	private String databaseName = null;
	private String namespace = null;
	private String collection = "guildmembers";
	private UserManagePanel panel = null;
	
	public GuildMemberSaveService(GuildMemberTreeTableModel model, UserManagePanel panel) {
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
		statusBar.updateStatus("保存公会成员数据");
		statusBar.progressBarAnimationStart();
		HashMap<String, DBObjectTreeTableNode> map = model.getChangedMap();
		Set<String> set = map.keySet();
		for ( String userId : set ) {
			DBObject query = MongoUtil.createDBObject("_id", userId);
			DBObject objectToSave = (DBObject)map.get(userId).getUserObject();
			MongoUtil.saveToMongo(query, objectToSave, databaseName, namespace, collection, true);
		}
		map.clear();
		statusBar.progressBarAnimationStop();
		statusBar.updateStatus("保存账户数据");
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
		statusBar.updateStatus("保存账户数据完毕");
	}

}
