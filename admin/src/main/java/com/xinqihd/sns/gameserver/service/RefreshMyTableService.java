package com.xinqihd.sns.gameserver.service;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import com.xinqihd.sns.gameserver.admin.action.CopyMongoCollectionAction;
import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.StatusBar;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;

public class RefreshMyTableService extends SwingWorker<Void, Void> {
	
	private MyTableModel tableModel = null;
	private JComponent   parent = null;
	private CopyMongoCollectionAction action = null;
	
	public RefreshMyTableService(MyTableModel tableModel) {
		this(tableModel, null);
	}
	
	public RefreshMyTableService(MyTableModel tableModel, JComponent parent) {
		this.tableModel = tableModel;
		this.parent = parent;
	}

	/**
	 * @return the action
	 */
	public CopyMongoCollectionAction getBackupAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setBackupAction(CopyMongoCollectionAction action) {
		this.action = action;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		StatusBar statusBar = MainPanel.getInstance().getStatusBar();
		statusBar.updateStatus("读取数据库内容");
		statusBar.progressBarAnimationStart();
		if ( this.action != null ) {
			this.action.actionPerformed(null);
		}
		tableModel.reload();
		statusBar.progressBarAnimationStop();
		statusBar.updateStatus("读取数据库完毕");
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		super.done();
		if ( parent != null ) {
			if ( parent instanceof JList ) {
				((JList)parent).setModel(null);
				((JList)parent).setModel(tableModel);
			} else if ( parent instanceof JTable ) {
				((JTable)parent).setModel(null);
				((JTable)parent).setModel(tableModel);
			}
		}
	}

}
