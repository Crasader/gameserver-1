package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;
import com.xinqihd.sns.gameserver.entity.user.Account;

/**
 * 管理用户的登陆状态
 * @author wangqi
 *
 */
public class ManageAccountStatusAction extends AbstractAction {
	
	private UserTreeTableModel model = null;
	private UserManagePanel panel = null;
	private Account account = null;

	public ManageAccountStatusAction(UserTreeTableModel model, UserManagePanel panel) {
		super("登陆管理");
		this.model = model;
		this.panel = panel;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
	
	public Account getAccount() {
		return this.account;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( this.account == null ) {
			JOptionPane.showMessageDialog(this.panel, "您还没有选择账号");
			return;
		}
		ManageAccountStatusDialog dialog = new ManageAccountStatusDialog(account, this.panel.getGameServerId());
		dialog.setVisible(true);
	}
	
}
