package com.xinqihd.sns.gameserver.admin.guild;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.gui.GuildManagePanel;

public class GuildSaveAction extends AbstractAction {
	
	private GuildTreeTableModel model = null;
	private GuildManagePanel panel = null;

	public GuildSaveAction(GuildTreeTableModel model, GuildManagePanel panel) {
		super("保存公会");
		this.model = model;
		this.panel = panel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int option = JOptionPane.showConfirmDialog(null, "您是否要保存修改后的数据?", "公会数据保存", JOptionPane.YES_NO_OPTION);
		if ( option == JOptionPane.YES_OPTION ) {
			GuildSaveService service = new GuildSaveService(model, panel);
			service.execute();
 		}
	}
	
}
