package com.xinqihd.sns.gameserver.admin.data;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import com.xinqihd.sns.gameserver.admin.action.CopyMongoCollectionAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * Refresh the gamedata collection's data.
 * @author wangqi
 *
 */
public class WeaponBalanceTableRefreshAction extends AbstractAction {
	
	private MyTablePanel panel = null;

	public WeaponBalanceTableRefreshAction(MyTablePanel panel) {
		super("", ImageUtil.createImageSmallIcon("Button Reload.png", "Refresh"));
		this.panel = panel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton)e.getSource();
		MyTableModel myTableModel = this.panel.getTableModel();
		int rowCount = myTableModel.getRowCount();
		while ( rowCount > 0 ) {
			myTableModel.deleteRow(0);
			rowCount = myTableModel.getRowCount();
		}
	}
	
}
