package com.xinqihd.sns.gameserver.admin.guild;

import java.util.List;

import javax.swing.SwingWorker;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.user.UserTreeTableNode;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.entity.user.UserId;

public class GuildSearchService extends SwingWorker<Void, Void> {
	
	private DefaultTreeTableModel model = null;
	private String userNameOrId = null;
	private String databaseName = null;
	private String namespace = null;
	private String collection = "guilds";
	private boolean preciMatch = false;
	
	public GuildSearchService(DefaultTreeTableModel model, String userNameOrId, boolean preciMatch) {
		this.model = model;
		this.preciMatch = preciMatch;
		this.userNameOrId = userNameOrId;
		this.databaseName = ConfigManager.getConfigAsString(ConfigKey.mongoDBName);
		this.namespace = ConfigManager.getConfigAsString(ConfigKey.mongoNamespace);
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		//StatusBar statusBar = MainPanel.getInstance().getStatusBar();
		//statusBar.updateStatus("查询账户数据");
		//statusBar.progressBarAnimationStart();
		List<DBObject> list = null;
		if ( !preciMatch ) {
			DBObject regex = MongoUtil.createDBObject("$regex", userNameOrId);
			DBObject query = MongoUtil.createDBObject("title", regex);
			list = MongoUtil.queryAllFromMongo(query, databaseName, namespace, collection, null);
		} else {
			DBObject query = MongoUtil.createDBObject("title", userNameOrId);
			list = MongoUtil.queryAllFromMongo(query, databaseName, namespace, collection, null);
		}
		UserTreeTableNode root = new UserTreeTableNode();
		if ( list != null ) {
			for ( DBObject dbObj : list ) {
				UserTreeTableNode node = new UserTreeTableNode(dbObj, root.getKeyName());
				root.add(node);
			}
		}
		model.setRoot(root);
		//statusBar.progressBarAnimationStop();
		//statusBar.updateStatus("查询账户数据完毕");
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		//StatusBar statusBar = MainPanel.getInstance().getStatusBar();
		//statusBar.progressBarAnimationStop();
		//statusBar.updateStatus("查询账户数据完毕");
	}

}
