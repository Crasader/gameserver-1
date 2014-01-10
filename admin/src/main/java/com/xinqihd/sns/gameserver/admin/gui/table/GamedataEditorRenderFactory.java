package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Dimension;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTextField;

import com.mongodb.BasicDBList;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;

public class GamedataEditorRenderFactory implements MyTableCellEditorFactory, 
	MyTableCellRenderFactory {
	
	private MyTableModel tableModel = null;
	
	public GamedataEditorRenderFactory(MyTableModel tableModel) {
		this.tableModel = tableModel;
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
		if ( "value".equals(columnName) || "default".equals(columnName) ) {
			int modelColIndex = table.convertColumnIndexToModel(column);
			int modelRowIndex = table.convertRowIndexToModel(row);
			Object value =  this.tableModel.getValueAt(modelRowIndex, modelColIndex);
			if ( value instanceof BasicDBList ) {
				MongoDBArrayEditor editor =  new MongoDBArrayEditor();
				editor.setDBObject((BasicDBList)value);
				return editor;
			} else {
				JXTextField field = new JXTextField(value.toString());
				field.setPreferredSize(new Dimension(100, 36));
				field.setBorder(null);
				return new DefaultCellEditor(field);
			}
		}
		return null;
	}
	
}
