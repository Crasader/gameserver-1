package com.xinqihd.sns.gameserver.admin.user;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;

public class UserDelAction extends AbstractAction {
	
	private UserTreeTableModel model = null;
	private UserManagePanel panel = null;
	private UserId selectedUserId = null;
	
	public UserDelAction(UserTreeTableModel model, UserManagePanel panel) {
		super("删除用户");
		this.model = model;
		this.panel = panel;
	}
	
	public void setModel(UserTreeTableModel model) {
		this.model = model;
	}
	
	public void setUserId(UserId userId) {
		this.selectedUserId = userId;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		SwingUtilities.invokeLater(new Runnable(){
//			public void run() {
//				MainPanel.getInstance().setCenterPanel(UserManagePanel.getInstance());
//			}
//		});
		try {
			int option = JOptionPane.showConfirmDialog(panel, "要删除用户'"+selectedUserId+"'吗?");
			if ( option == JOptionPane.YES_OPTION ) {
				int childCount = model.getRoot().getChildCount();
				for ( int i=0; i<childCount; i++ ) {
					UserTreeTableNode treeNode = (UserTreeTableNode)(model.getRoot().getChildAt(i));
					UserId userId = (UserId)treeNode.getKey();
					if ( this.selectedUserId.equals(userId) ) {
						model.removeNodeFromParent(treeNode);
						User user = UserManager.getInstance().queryUser(userId);
						String accountName = user.getAccountName();
						Account account = AccountManager.getInstance().queryAccountByName(accountName);
						user.setAccount(account);
						//UserManager.getInstance().removeUser(selectedUserId);
						AccountManager.getInstance().deleteGameRole(null, user);
						panel.updateButtonStatus();
						break;
					}
				}
			}
		} catch (HeadlessException e1) {
			e1.printStackTrace();
		}
	}

}
