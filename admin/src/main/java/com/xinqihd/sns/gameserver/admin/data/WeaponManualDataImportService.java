package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

/**
 * 武器的数据文件可以导出为EXCEL格式，策划可以手工编辑这个文件，修改新的数值并导入系统。
 * 注意：
 * 1: 以'#'开始的行为注释
 * 2: 该文件应只还有'黑铁'类型的武器数值，其他类型由系统根据强化10级自动计算
 * @author wangqi
 *
 */
public class WeaponManualDataImportService extends SwingWorker<Void, Integer> {
	
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在读取文件中的武器数值");
	private Stage stage = Stage.INIT;
	private int totalCount = 100;
	private WeaponTableModel weaponModel = null;
	private File importExcelFile = null;
	private ArrayList<Double> levelDprList = null;
	
	public WeaponManualDataImportService(
			File file,
			WeaponTableModel model,
			ArrayList<Double> dprList) {
		this.importExcelFile = file;
		this.weaponModel = model;
		this.levelDprList = dprList;
		
		panel = new JXPanel();
		panel.setLayout(new MigLayout("wrap 1"));
		panel.add(label, "growx, wrap 20");
		panel.add(progressBar, "grow, push");
		
		dialog = new JDialog();
		dialog.add(panel);
		dialog.setSize(300, 120);
		Point p = WindowUtils.getPointForCentering(dialog);
		dialog.setLocation(p);
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}
	
	public JDialog getDialog() {
		return dialog;
	}
	 
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		stage = Stage.INIT;
		publish();
				
		stage = Stage.READ_DATA;
		publish();
		((WeaponTableModel)weaponModel).importExcel(
				this.importExcelFile, this.levelDprList);
		
		stage = Stage.SAVE_DATA;
		publish();

		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<Integer> chunks) {
		if ( stage == Stage.INIT ) {
//			dialog.setVisible(true);
			progressBar.setMinimum(0);
			progressBar.setMaximum(totalCount*2);
			progressBar.setStringPainted(true);
		} else {
			int value = 0;
			if ( chunks != null && chunks.size()>0 ) {
				value = chunks.get(chunks.size()-1);
			}
			if ( stage == Stage.READ_DATA ) {
				label.setText("正在读取文件中的武器数值...");
				progressBar.setValue(value);
			} else if ( stage == Stage.SAVE_DATA ) {
				label.setText("正在保存装备数据...");
				progressBar.setValue(totalCount+value);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		this.dialog.dispose();
//		JOptionPane.showMessageDialog(null, "数据同步成功!");
	}

	static enum Stage {
		INIT,
		READ_DATA,
		SAVE_DATA,
	}
}
