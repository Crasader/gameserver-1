package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;

public class GuildMemberDeleteAction extends AbstractAction {
	
	private GuildMemberTreeTableModel model = null;
	private UserManagePanel panel = null;

	public GuildMemberDeleteAction(GuildMemberTreeTableModel model, UserManagePanel panel) {
		super("删除公会成员");
		this.model = model;
		this.panel = panel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		GuildMemberDeleteService service = new GuildMemberDeleteService(model, panel);
		service.execute();
	}
	
}
