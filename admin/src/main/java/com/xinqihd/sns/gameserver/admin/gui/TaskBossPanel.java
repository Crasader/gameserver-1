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
public class TaskBossPanel extends AbstractTaskPanel {
	
	private static TaskBossPanel instance = new TaskBossPanel(); 
	
	
	public TaskBossPanel() {
		init();
	}
	
	public static TaskBossPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "bosses";
		super.initModel(collection);
		
		hiddenFields.add("class");
		model.setHiddenFields(hiddenFields);
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("副本怪物管理");
		myTable.setTableModel(model);
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setBackupAction(backupAction);
//		myTable.setColumnEditor(3, new GameDataEditor());
		this.add(myTable, "width 100%, height 100%");
	}
	
}
