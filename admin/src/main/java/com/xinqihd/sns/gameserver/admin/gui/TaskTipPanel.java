package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.util.HashSet;

import javax.swing.AbstractAction;

import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.action.game.MongoExportAction;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableRefreshAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.model.GeneralMongoModel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskTipPanel extends AbstractTaskPanel {
	
	private static TaskTipPanel instance = new TaskTipPanel(); 
	
	
	public TaskTipPanel() {
		init();
	}
	
	public static TaskTipPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "tips";
		super.initModel(collection);
		
		hiddenFields.add("class");
		model.setHiddenFields(hiddenFields);
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("游戏提示信息");
		myTable.setTableModel(model);
		myTable.setAddRowAction(new AbstractAction("", ImageUtil.createImageSmallIcon("Button Add.png", "增加新的记录")){

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				TaskTipAddDialog addRowDialog = new TaskTipAddDialog(model);
				addRowDialog.setVisible(true);
			}
			
		});
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setBackupAction(backupAction);
//		myTable.setColumnEditor(3, new GameDataEditor());
		this.add(myTable, "width 100%, height 100%");
	}
	
}
