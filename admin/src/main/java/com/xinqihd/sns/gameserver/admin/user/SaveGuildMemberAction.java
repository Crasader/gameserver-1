package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;

public class SaveGuildMemberAction extends AbstractAction {
	
	private GuildMemberTreeTableModel model = null;
	private UserManagePanel panel = null;

	public SaveGuildMemberAction(GuildMemberTreeTableModel model, UserManagePanel panel) {
		super("保存公会成员");
		this.model = model;
		this.panel = panel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int option = JOptionPane.showConfirmDialog(null, "您是否要保存修改后的数据?", "公会成员数据保存", JOptionPane.YES_NO_OPTION);
		if ( option == JOptionPane.YES_OPTION ) {
			GuildMemberSaveService service = new GuildMemberSaveService(model, panel);
			service.execute();
 		}
	}
	
}
