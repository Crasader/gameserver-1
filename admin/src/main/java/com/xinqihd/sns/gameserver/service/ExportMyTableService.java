package com.xinqihd.sns.gameserver.service;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.StatusBar;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;

public class ExportMyTableService extends SwingWorker<Void, Void> {
	
	private MyTableModel tableModel = null;
	private File exportFile = null;
	private int rowCount = 0;
	
	public ExportMyTableService(File exportFile, MyTableModel tableModel) {
		this.tableModel = tableModel;
		this.exportFile = exportFile;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		StatusBar statusBar = MainPanel.getInstance().getStatusBar();
		statusBar.updateStatus("正在导出文件...");
		statusBar.progressBarAnimationStart();
		try {
			this.tableModel.export(exportFile);
			statusBar.updateStatus("导出文件完毕");
		} catch (Exception e) {
			statusBar.updateStatus("导出文件失败:"+e.getMessage());
			e.printStackTrace();
		}
		statusBar.progressBarAnimationStop();
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		JOptionPane.showMessageDialog(null, "导出成功!");
		super.done();
	}

}
