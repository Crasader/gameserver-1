package com.xinqihd.sns.gameserver.admin.gui;

import java.util.HashSet;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.gui.table.MongoDBObjectEditor;
import com.xinqihd.sns.gameserver.admin.item.AddOrEditRewardAction;
import com.xinqihd.sns.gameserver.admin.model.GeneralMongoModel;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskMapPanel extends AbstractTaskPanel {
	
	private static TaskMapPanel instance = new TaskMapPanel(); 
		
	public TaskMapPanel() {
		init();
	}
	
	public static TaskMapPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "maps";
		super.initModel(collection);
		
		myTable.getTable().setColumnOrders(new String[]{"_id", "name", "scrollAreaWidth", "scrollAreaHeight"});
		
		hiddenFields.add("class");
		hiddenFields.add("enemies");
		hiddenFields.add("bosses");
		
		model.setHiddenFields(hiddenFields);
		
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("游戏地图数据");
		myTable.setTableModel(model);
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		MapEditorRenderFactory factory = new MapEditorRenderFactory(this.myTable);
		myTable.getTable().setRenderFactory(factory);
		myTable.getTable().setEditorFactory(factory);
//		myTable.setColumnEditor(3, new GameDataEditor());
		myTable.setBackupAction(backupAction);
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
		if ( "layers".equals(columnName) || "startPoints".equals(columnName) ) {
			AddOrEditRewardAction addAction = null;
			Object obj = this.myTable.getTable().getValueAt(row, column);
			if ( obj instanceof DBObject ) {
				MongoDBObjectEditor editor = new MongoDBObjectEditor(addAction);
				return editor;
			}
		}
		return null;
	}
	
}
}
