package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;

public class AccountDeleteAction extends AbstractAction {
	
	private AccountTreeTableModel model = null;
	private UserManagePanel panel = null;

	public AccountDeleteAction(AccountTreeTableModel model, UserManagePanel panel) {
		super("删除账户");
		this.model = model;
		this.panel = panel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		AccountDeleteService service = new AccountDeleteService(model, panel);
		service.execute();
	}
	
}
