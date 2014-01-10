package com.xinqihd.sns.gameserver.admin.gui.ext;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Create proper table cell renderer
 * 
 * @author wangqi
 *
 */
public interface MyTableCellRenderFactory {

	public TableCellRenderer getCellRenderer(int row, int column, 
			String columnName, TableModel tableModel, JTable table);
	
}
