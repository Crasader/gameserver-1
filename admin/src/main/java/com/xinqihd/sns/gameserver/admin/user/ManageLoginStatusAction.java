package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;

/**
 * 管理用户的登陆状态
 * @author wangqi
 *
 */
public class ManageLoginStatusAction extends AbstractAction {
	
	private UserTreeTableModel model = null;
	private UserManagePanel panel = null;
	private User user = null;

	public ManageLoginStatusAction(UserTreeTableModel model, UserManagePanel panel) {
		super("登陆管理");
		this.model = model;
		this.panel = panel;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return this.user;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( this.user == null ) {
			JOptionPane.showMessageDialog(this.panel, "您还没有选择用户");
			return;
		}
		ManageLoginStatusDialog dialog = new ManageLoginStatusDialog(user);
		dialog.setVisible(true);
		User user = dialog.getUser();
		if ( user != null ) {
			UserManager.getInstance().saveUser(user, false);
		}
	}
	
}
