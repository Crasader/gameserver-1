package com.xinqihd.sns.gameserver.admin.guild;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.gui.GuildManagePanel;

public class GuildBagSaveAction extends AbstractAction {
	
	private GuildTreeTableModel model = null;
	private GuildManagePanel panel = null;

	public GuildBagSaveAction(GuildTreeTableModel model, GuildManagePanel panel) {
		super("保存公会仓库");
		this.model = model;
		this.panel = panel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int option = JOptionPane.showConfirmDialog(null, "您是否要保存修改后的数据?", "公会仓库数据保存", JOptionPane.YES_NO_OPTION);
		if ( option == JOptionPane.YES_OPTION ) {
			GuildBagSaveService service = new GuildBagSaveService(model, panel);
			service.execute();
 		}
	}
	
}
