package com.xinqihd.sns.gameserver.admin.gui.ext;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * 编辑表格单元格的Editor超类，封装了modelIndex
 * @author wangqi
 *
 */
public abstract class MyAbstractCellEditor extends AbstractCellEditor 
	implements TableCellEditor, ActionListener {
	
	/* (non-Javadoc)
	 * @see javax.swing.AbstractCellEditor#isCellEditable(java.util.EventObject)
	 */
	@Override
	public boolean isCellEditable(EventObject e) {
		if ( e instanceof MouseEvent ) {
			if ( ((MouseEvent)e).getClickCount() == 2 ) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public abstract Object getCellEditorValue();

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		int modelRow    = table.convertRowIndexToModel(row);
		int modelColumn = table.convertColumnIndexToModel(column);
		return getTableCellEditorComponentAtModel(table, value, isSelected, modelRow, modelColumn);
	}
	
	public abstract Component getTableCellEditorComponentAtModel(JTable table, Object value,
			boolean isSelected, int modelRowIndex, int modelColumnIndex);
		

}
