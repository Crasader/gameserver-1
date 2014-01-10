package com.xinqihd.sns.gameserver.admin.gui.ext;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTreeTable;

public class MyTreeTable extends JXTreeTable {

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.JXTreeTable#getCellEditor(int, int)
	 */
	@Override
	public TableCellEditor getCellEditor(int row, int column) {
    TableColumn tableColumn = getColumnModel().getColumn(column);
    TableCellEditor editor = tableColumn.getCellEditor();
    if (editor == null) {
    	Object value = this.getValueAt(row, column);
    	if ( value != null ) {
    		editor = getDefaultEditor(value.getClass());
    	} else {
    		editor = getDefaultEditor(getColumnClass(column));
    	}
    }
    return editor;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.JXTreeTable#getCellRenderer(int, int)
	 */
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if ( column == 0 ) {
			return super.getCellRenderer(row, column);
		}
    TableColumn tableColumn = getColumnModel().getColumn(column);
    TableCellRenderer renderer = tableColumn.getCellRenderer();
    if (renderer == null) {
      Object value = this.getValueAt(row, column);
      if ( value != null ) {
      	renderer = getDefaultRenderer(value.getClass());
      } else {
      	renderer = getDefaultRenderer(getColumnClass(column));
      }
    }
    return renderer;
	}

	
}
