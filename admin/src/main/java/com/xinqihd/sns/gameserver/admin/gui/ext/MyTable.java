package com.xinqihd.sns.gameserver.admin.gui.ext;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import com.xinqihd.sns.gameserver.admin.i18n.ColumnNames;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.util.ClipboardKeyAdapter;

public class MyTable extends JXTable {
	
	private MyTableCellRenderFactory renderFactory = null;
	
	private MyTableCellEditorFactory editorFactory = null;
	
	private Object[] columnSequcence = null;
	
	public MyTable() {
		super();
		this.addKeyListener(new ClipboardKeyAdapter(this));
	}
	
	/**
	 * @return the renderFactory
	 */
	public MyTableCellRenderFactory getRenderFactory() {
		return renderFactory;
	}

	/**
	 * @param renderFactory the renderFactory to set
	 */
	public void setRenderFactory(MyTableCellRenderFactory renderFactory) {
		this.renderFactory = renderFactory;
	}

	/**
	 * @return the editorFactory
	 */
	public MyTableCellEditorFactory getEditorFactory() {
		return editorFactory;
	}

	/**
	 * @param editorFactory the editorFactory to set
	 */
	public void setEditorFactory(MyTableCellEditorFactory editorFactory) {
		this.editorFactory = editorFactory;
	}

	/**
	 * @return the columnSequcence
	 */
	public Object[] getColumnOrders() {
		return columnSequcence;
	}

	/**
	 * @param columnSequcence the columnSequcence to set
	 */
	public void setColumnOrders(Object[] columnSequcence) {
		String[] tmpOrders = new String[columnSequcence.length];
		for ( int i=0; i<tmpOrders.length; i++ ) {
			Object col = columnSequcence[i];
			tmpOrders[i] = ColumnNames.translate(col.toString()); 
		}
		this.columnSequcence = tmpOrders;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.JXTable#getCellRenderer(int, int)
	 */
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if ( renderFactory != null ) {
			TableModel tableModel = this.getModel();
			if ( tableModel != null ) {
				int modelRowIndex = this.convertRowIndexToModel(row);
				int modelColumnIndex = this.convertColumnIndexToModel(column);
				String columnName = getColumnName(column, tableModel);
				TableCellRenderer renderer = this.renderFactory.getCellRenderer(
						modelRowIndex, modelColumnIndex, columnName, tableModel, this);
				if ( renderer != null ) {
					return renderer;
				}
			}
		}
		return super.getCellRenderer(row, column);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTable#getCellEditor(int, int)
	 */
	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		if ( editorFactory != null ) {
			TableModel tableModel = this.getModel();
			if ( tableModel != null ) {
				String columnName = getColumnName(column, tableModel);
				//Use original row / column index
				TableCellEditor editor = this.editorFactory.getCellEditor(
						row, column, columnName, tableModel, this);
				if ( editor != null ) {
					return editor;
				}
			}
		}
		return super.getCellEditor(row, column);
	}
	
	/**
	 * Get underlying column name.
	 * @param column
	 * @param tableModel
	 * @return
	 */
	private String getColumnName(int column, TableModel tableModel) {
		int modelColumnIndex = this.convertColumnIndexToModel(column);
		String columnName = null;
		if ( tableModel instanceof MyTableModel ) {
			columnName = ((MyTableModel)tableModel).getOriginalColumnName(modelColumnIndex);
		} else {
			columnName = tableModel.getColumnName(modelColumnIndex);
		}
		return columnName;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.JXTable#tableChanged(javax.swing.event.TableModelEvent)
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
		if ( columnSequcence != null ) {
			this.setColumnSequence(columnSequcence);
		}
	}
	
}
