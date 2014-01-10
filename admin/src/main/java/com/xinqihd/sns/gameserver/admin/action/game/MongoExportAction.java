package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.service.ExportMyTableService;

public class MongoExportAction extends AbstractAction {
	
	private final MyTablePanel parent;

	public MongoExportAction(MyTablePanel parent) {
		super("", ImageUtil.createImageSmallIcon("Drive Download.png", "Download"));
		this.parent = parent;
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_export;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				File currentFileDir = null;
				String lastExportDir = ConfigManager.getConfigAsString(ConfigKey.dataExportDir);
				if ( lastExportDir != null ) {
					currentFileDir = new File(lastExportDir);
				} else {
					currentFileDir = new File(System.getProperty("user.dir"));
				}
				ConfigManager.saveConfigKeyValue(ConfigKey.dataExportDir, currentFileDir.getAbsolutePath());
				JFileChooser chooser = new JFileChooser(currentFileDir);
				int select = chooser.showSaveDialog(parent);
				if ( select == JFileChooser.APPROVE_OPTION ) {
					File exportFile = chooser.getSelectedFile();
					if ( exportFile.exists() ) {
						int option = JOptionPane.showConfirmDialog(parent, 
								"文件已经存在，确定要覆盖它吗?", "数据导出", JOptionPane.YES_NO_OPTION);
						if ( option == JOptionPane.YES_OPTION ) {
							exportFile.delete();
						}
					}
					if ( !exportFile.exists() ) {
						ExportMyTableService service = new ExportMyTableService(exportFile, parent.getTableModel());
						service.execute();
					}
				}
			}
		});
	}

}
