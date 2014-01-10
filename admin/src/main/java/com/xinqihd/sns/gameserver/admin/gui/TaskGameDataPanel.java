package com.xinqihd.sns.gameserver.admin.gui;

import java.util.HashSet;

import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.action.CopyMongoCollectionAction;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.gui.table.GamedataEditorRenderFactory;
import com.xinqihd.sns.gameserver.admin.model.GeneralMongoModel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskGameDataPanel extends AbstractTaskPanel {
	
	private static TaskGameDataPanel instance = new TaskGameDataPanel(); 

	public TaskGameDataPanel() {
		init();
	}
	
	public static TaskGameDataPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "gamedata";
		super.initModel(collection);
		
		hiddenFields.add("_id");
		model.setHiddenFields(hiddenFields);
		
		myTable.getTable().setColumnOrders(new String[]{"key", "desc", "default", "value"});
		
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("游戏基础数据");
		myTable.setTableModel(model);
		myTable.setEnableAddRow(false);
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setEnableDelRow(false);
		GamedataEditorRenderFactory factory = new GamedataEditorRenderFactory(model);
		myTable.getTable().setRenderFactory(factory);
		myTable.getTable().setEditorFactory(factory);
		myTable.setBackupAction(backupAction);
		
		this.add(myTable, "width 100%, height 100%");
	}
	
}
