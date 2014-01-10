package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXComboBox;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor;

/**
 * 这个编辑器将一列中所有可能的值提取出来并形成列表供用户选择
 * @author wangqi
 *
 */
public class TableValueListEditor extends MyAbstractCellEditor 
		implements TableCellEditor, ActionListener {

	private JXComboBox comboBox = new JXComboBox();
	private LinkedHashSet<Object> columnValues = new LinkedHashSet<Object>();
	private Object cellValue = null;
	
	public TableValueListEditor() {
	}


	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		return cellValue;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor#getTableCellEditorComponentAtModel(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponentAtModel(JTable table,
			Object value, boolean isSelected, int modelRowIndex, int modelColumnIndex) {
		TableModel tableModel = table.getModel();
		this.cellValue = value;
		if ( columnValues.size() == 0 ) {
			int rowCount = tableModel.getRowCount();
			columnValues.add(value);
			for ( int i=0; i<rowCount; i++ ) {
				Object v = tableModel.getValueAt(i, modelColumnIndex);
				columnValues.add(v);
			}
		}
		this.comboBox.setEditable(true);
		this.comboBox.setMinimumSize(new Dimension(40, 40));
		for ( Object v : columnValues ) {
			this.comboBox.addItem(v);
		}
		this.comboBox.addActionListener(this);
		return this.comboBox;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		this.cellValue = this.comboBox.getSelectedItem();
		this.stopCellEditing();
	}
}
