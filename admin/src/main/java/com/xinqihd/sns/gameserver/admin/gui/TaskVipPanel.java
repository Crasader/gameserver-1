package com.xinqihd.sns.gameserver.admin.gui;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskVipPanel extends AbstractTaskPanel {
	
	private static TaskVipPanel instance = new TaskVipPanel(); 
		
	public TaskVipPanel() {
		init();
	}
	
	public static TaskVipPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "vips";
		super.initModel(collection);
		
//		myTable.getTable().setColumnOrders(new String[]{"_id", "propInfoId", "info", "discount", "buyPrices", "catalogs"});
		
		hiddenFields.add("class");
		
		model.setHiddenFields(hiddenFields);
		
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("游戏VIP配置");
		myTable.setTableModel(model);
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		MapEditorRenderFactory factory = new MapEditorRenderFactory(this.myTable);
		myTable.getTable().setRenderFactory(factory);
		myTable.getTable().setEditorFactory(factory);
		myTable.setBackupAction(backupAction);
//		myTable.setColumnEditor(3, new GameDataEditor());
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
