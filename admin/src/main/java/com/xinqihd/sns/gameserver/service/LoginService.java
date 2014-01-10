package com.xinqihd.sns.gameserver.service;

import java.awt.Point;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.callback.TimelineCallbackAdapter;

import com.mongodb.Mongo;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.LoginDialog;
import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.security.AdminUser;
import com.xinqihd.sns.gameserver.admin.security.AdminUserManager;
import com.xinqihd.sns.gameserver.admin.util.MyWindowUtil;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Manage user's login action.
 * @author wangqi
 *
 */
public class LoginService extends SwingWorker<Boolean, Void> {
	
	private boolean loginResult = false;
	private String username = null;
	private String password = null;
	private String mongoServer = null;
	
	public LoginService() {
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Boolean doInBackground() throws Exception {
		username = LoginDialog.getInstance().getUsername();
		password = LoginDialog.getInstance().getPassword();
		mongoServer = LoginDialog.getInstance().getMongoServer();
		
		if ( StringUtil.checkNotEmpty(username) ) {
			if ( (username+"123").equals(password) ) {
				loginResult = true;
			}
		}

		Mongo mongo = MongoUtil.initUserMongo(mongoServer, 27017);
		if ( mongo == null ) {
			JOptionPane.showMessageDialog(LoginDialog.getInstance(), 
					"无法连接到Mongo数据库:"+mongoServer, "数据库连接失败", JOptionPane.ERROR_MESSAGE);
		}
		/*
		if ( adminUser != null ) {
			loginResult = true;
			MainFrame.loginUserName = adminUser.getUsername();
		}
		return loginResult;
		*/
		/*
		AdminUser adminUser = AdminUserManager.getInstance().queryAdminUser(username);
		if ( adminUser.getPassword().equals(password) ) {
			loginResult = true;
			MainFrame.loginUserName = adminUser.getUsername();
		}
		*/
		MainFrame.loginUserName = username;
		return loginResult;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		final LoginDialog dialog = LoginDialog.getInstance();
		if ( !loginResult ) {
			dialog.getResultLabel().setText("登陆失败");
			dialog.getOKButton().setEnabled(true);
			dialog.getOKButton().setText("确定");
		} else {
			ConfigManager.saveConfigKeyValue(ConfigKey.adminUsername, username);
			ConfigManager.saveConfigKeyValue(ConfigKey.adminPassword, password);
			ConfigManager.saveConfigKeyValue(ConfigKey.adminDatabaseServer, mongoServer);
			
			Point to = dialog.getLocation();
			to.y = MainFrame.screenHeight;
			Timeline timeline = MyWindowUtil.createLocationTimeline(dialog, to, 200);
			timeline.addCallback(new TimelineCallbackAdapter() {

				/* (non-Javadoc)
				 * @see org.pushingpixels.trident.callback.TimelineCallbackAdapter#onTimelineStateChanged(org.pushingpixels.trident.Timeline.TimelineState, org.pushingpixels.trident.Timeline.TimelineState, float, float)
				 */
				@Override
				public void onTimelineStateChanged(TimelineState oldState,
						TimelineState newState, float durationFraction,
						float timelinePosition) {
					if ( newState == TimelineState.DONE ) {
						dialog.dispose();
					}
				}
				
			});
			timeline.play();
		}
	}

	
}
