package com.xinqihd.sns.gameserver.admin.guild;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.xinqihd.sns.gameserver.admin.gui.GuildManagePanel;

public class GuildBagDeleteAction extends AbstractAction {
	
	private GuildTreeTableModel model = null;
	private GuildManagePanel panel = null;

	public GuildBagDeleteAction(GuildTreeTableModel model, GuildManagePanel panel) {
		super("删除公会仓库");
		this.model = model;
		this.panel = panel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		GuildDeleteService service = new GuildDeleteService(model, panel);
		service.execute();
	}
	
}
