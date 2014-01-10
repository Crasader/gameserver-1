package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXComboBox;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor;
import com.xinqihd.sns.gameserver.admin.util.ObjectUtil;

/**
 * 用于从固定的数组中生成一个Table的CellEditor
 * @author wangqi
 *
 */
public class ObjectArrayListEditor extends MyAbstractCellEditor 
		implements TableCellEditor, ActionListener {

	private JXComboBox comboBox = new JXComboBox();
	private ArrayList<Object> columnValues = new ArrayList<Object>();
	private Object cellValue = null;
	private Object originalValue = null;
	
	public ObjectArrayListEditor(Object[] arrays) {
		this.comboBox.setEditable(false);
		this.comboBox.setMinimumSize(new Dimension(40, 40));
		for ( Object o : arrays ) {
			columnValues.add(o);
			comboBox.addItem(o);
		}
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
		this.originalValue = value;
		this.cellValue = value;
		for ( int i=0; i<columnValues.size(); i++ ) {
			Object co = columnValues.get(i);
			if ( co.equals(value) ) {
				this.comboBox.setSelectedIndex(i);
				break;
			}
		}
		this.comboBox.addActionListener(this);
		return this.comboBox;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object newObject = this.comboBox.getSelectedItem();
		this.cellValue = ObjectUtil.parseStringToObject(newObject.toString(), newObject.toString(), this.originalValue.getClass());
		this.stopCellEditing();
	}
}
