package com.xinqihd.sns.gameserver.admin.gui.ext;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

/**
 * Create proper table cell renderer
 * 
 * @author wangqi
 *
 */
public interface MyTableCellEditorFactory {

	public TableCellEditor getCellEditor(int row, int column, 
			String columnName, TableModel tableModel, JTable table);
	
}
