package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.item.EditRewardDialog;
import com.xinqihd.sns.gameserver.config.DailyMarkPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskDailyMarkPanel extends AbstractTaskPanel {
	
	private static TaskDailyMarkPanel instance = new TaskDailyMarkPanel(); 
		
	public TaskDailyMarkPanel() {
		init();
	}
	
	public static TaskDailyMarkPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "dailymarks";
		super.initModel(collection);
		
//		myTable.getTable().setColumnOrders(new String[]{"_id", "propInfoId", "info", "discount", "buyPrices", "catalogs"});
		
		hiddenFields.add("class");
		
		model.setHiddenFields(hiddenFields);
		
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("每日登陆打卡奖励");
		myTable.setTableModel(model);
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		MapEditorRenderFactory factory = new MapEditorRenderFactory(this.myTable);
		myTable.getTable().setRenderFactory(factory);
		myTable.getTable().setEditorFactory(factory);
		myTable.setBackupAction(backupAction);
//		myTable.setColumnEditor(3, new GameDataEditor());
		myTable.getTable().addMouseListener(new MouseAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if ( e.getClickCount() == 2 ) {
					int rowIndex = myTable.getTable().getSelectedRow();
					if ( rowIndex >= 0 ) {
						int modelRowIndex = myTable.getTable().convertRowIndexToModel(rowIndex);
						DailyMarkPojo dailyMark = null;
						DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
						dailyMark = (DailyMarkPojo)MongoUtil.constructObject(dbObj);
						
						EditRewardDialog dialog = new EditRewardDialog((ArrayList)dailyMark.getRewards());
						dialog.setVisible(true);
						
						List<Reward> rewards = dialog.getRewards();
						dailyMark.setRewards(rewards);
						MapDBObject dbObject = new MapDBObject();
						dbObject.putAll(dailyMark);
						if ( modelRowIndex >= 0 ) {
							myTable.getTableModel().updateRow(dbObject, modelRowIndex);
						} else {
							myTable.getTableModel().insertRow(dbObject);
						}
					}
				}
			}
			
		});
		this.add(myTable, "width 100%, height 100%");
	}
	
	
	static class MapEditorRenderFactory implements MyTableCellEditorFactory, 
			MyTableCellRenderFactory {
	
	private MyTablePanel myTable = null;
	
	public MapEditorRenderFactory(MyTablePanel myTable) {
		this.myTable = myTable;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory#getCellRenderer(int, int, java.lang.String)
	 */
	@Override
	public TableCellRenderer getCellRenderer(int row, int column,
			String columnName, TableModel tableModel, JTable table) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory#getCellEditor(int, int, java.lang.String)
	 */
	@Override
	public TableCellEditor getCellEditor(int row, int column, 
			String columnName, TableModel tableModel, JTable table) {
		return null;
	}
	
}
}
