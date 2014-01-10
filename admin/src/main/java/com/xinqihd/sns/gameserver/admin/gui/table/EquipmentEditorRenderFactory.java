package com.xinqihd.sns.gameserver.admin.gui.table;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;

public class EquipmentEditorRenderFactory implements MyTableCellEditorFactory, 
	MyTableCellRenderFactory {
	
	private MyTablePanel myTable = null;
	
	public EquipmentEditorRenderFactory(MyTablePanel myTable) {
		this.myTable = myTable;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory#getCellRenderer(int, int, java.lang.String)
	 */
	@Override
	public TableCellRenderer getCellRenderer(int row, int column,
			String columnName, TableModel tableModel, JTable table) {
		if ( "icon".equals(columnName) ) {
			return new IconCellRenderer();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory#getCellEditor(int, int, java.lang.String)
	 */
	@Override
	public TableCellEditor getCellEditor(int row, int column, String columnName,
			TableModel tableModel, JTable table) {
//		if ( "icon".equals(columnName) ) {
//			return new IconCellEditor();
//		}
		if ( "avatar".equals(columnName) ) {
			MongoDBObjectEditor editor = new MongoDBObjectEditor();
			return editor;
		}
		return null;
	}
	
}
