package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.service.UserBagQueryService;

public class ManageBagAction extends AbstractAction {
	
	private UserTreeTableModel model = null;
	private UserManagePanel panel = null;
	private User selectedUser = null;

	public ManageBagAction(UserTreeTableModel model, UserManagePanel panel) {
		super("管理背包");
		this.model = model;
		this.panel = panel;
	}
	
	public void setUser(User user) {
		this.selectedUser = user;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( this.selectedUser != null ) {
			UserBagQueryService service = new UserBagQueryService(this.selectedUser);
			service.execute();
		}
	}
	
}
