package com.xinqihd.sns.gameserver.admin.action.game;

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
public class MyTableRefreshAction extends AbstractAction {
	
	private MyTablePanel panel = null;
	private CopyMongoCollectionAction backupAction = null;

	public MyTableRefreshAction(MyTablePanel panel) {
		super("", ImageUtil.createImageSmallIcon("Button Reload.png", "Refresh"));
		this.panel = panel;
	}

	/**
	 * @return the backupAction
	 */
	public CopyMongoCollectionAction getBackupAction() {
		return backupAction;
	}

	/**
	 * @param backupAction the backupAction to set
	 */
	public void setBackupAction(CopyMongoCollectionAction backupAction) {
		this.backupAction = backupAction;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton)e.getSource();
		MyTableModel myTableModel = this.panel.getTableModel();
		RefreshMyTableService service = new RefreshMyTableService(myTableModel);
		service.setBackupAction(backupAction);
		service.execute();
	}
	
}
