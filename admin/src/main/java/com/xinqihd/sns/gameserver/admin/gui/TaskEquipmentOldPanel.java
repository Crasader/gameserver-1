package com.xinqihd.sns.gameserver.admin.gui;

import java.util.HashSet;

import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.action.CopyMongoCollectionAction;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.gui.table.EquipmentEditorRenderFactory;
import com.xinqihd.sns.gameserver.admin.model.GeneralMongoModel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskEquipmentOldPanel extends AbstractTaskPanel {
	
	private static TaskEquipmentOldPanel instance = new TaskEquipmentOldPanel(); 

	public TaskEquipmentOldPanel() {
		init();
	}
	
	public static TaskEquipmentOldPanel getInstance() {
		return instance;
	}

	public void init() {		
		//创建工作区域
		String collection = "equipments";
		super.initModel(collection);
		
		hiddenFields.add("class");
		hiddenFields.add("name");
		hiddenFields.add("index");
		hiddenFields.add("bubble");
		hiddenFields.add("unused1");
		hiddenFields.add("unused2");
		hiddenFields.add("unused3");
		hiddenFields.add("indate1");
		hiddenFields.add("indate2");
		hiddenFields.add("indate3");
		hiddenFields.add("sign");
		hiddenFields.add("lv");
		hiddenFields.add("expBlend");
		hiddenFields.add("autoDestory");
		hiddenFields.add("avatar");
		model.setHiddenFields(hiddenFields);
		
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("游戏武器装备数据");
		myTable.getTable().setColumnOrders(new String[]{"_id", "sName", "icon", "info"});
		myTable.setTableModel(model);
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		EquipmentEditorRenderFactory factory = new EquipmentEditorRenderFactory(this.myTable);
		myTable.getTable().setRenderFactory(factory);
		myTable.getTable().setEditorFactory(factory);
		myTable.setBackupAction(backupAction);
//		myTable.setColumnEditor(3, new GameDataEditor());
		this.add(myTable, "width 100%, height 100%");
	}
	
}
