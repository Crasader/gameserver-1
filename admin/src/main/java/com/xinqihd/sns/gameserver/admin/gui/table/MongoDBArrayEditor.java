package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.EscapeAction;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.model.ObjectDBListModel;

public class MongoDBArrayEditor extends MyAbstractCellEditor 
		implements ActionListener {
	
	private JDialog dialog = new JDialog();
	private MyTablePanel table = new MyTablePanel();
	private ObjectDBListModel dbObjectModel = null;
	private JXButton okButton = new JXButton("确定");
	private Object cellValue = null;
	
	public MongoDBArrayEditor() {
	}
	
	public void setDBObject(DBObject dbObject) {
		this.dbObjectModel = new ObjectDBListModel((BasicDBList)dbObject);
		dbObjectModel.reload();
		this.table.setTableModel(dbObjectModel);
		this.dbObjectModel.fireTableDataChanged();
	}
	
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return this.cellValue;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor#getTableCellEditorComponentAtModel(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponentAtModel(JTable table,
			Object value, boolean isSelected, int modelRowIndex, int modelColumnIndex) {
		this.cellValue = value;
		
		this.okButton.addActionListener(this);
		this.okButton.setActionCommand("OK");
		this.dialog.setMinimumSize(new Dimension(300, 350));
//		this.setResizable(false);
		this.table.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
		this.table.getActionMap().put("escape", new EscapeAction());
		this.table.setEditable(true);
		this.table.setDelRowAction(new MyTableModelDeleteRowAction(this.table.getTable()));
		
		JScrollPane pane = new JScrollPane(this.table);
		
		this.dialog.setLayout(new MigLayout("wrap 1", "[100%]"));
		this.dialog.add(pane, "width 100%, grow, height 300px");
		this.dialog.add(okButton, "align center");
		this.dialog.setModal(true);
		this.dialog.setVisible(true);
		
		Object newObj = this.dbObjectModel.getValueAt( modelRowIndex, modelColumnIndex);
		if ( newObj != null && !newObj.equals(value) ) {
			table.getModel().setValueAt(newObj, modelRowIndex, modelColumnIndex);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( "OK".equals(e.getActionCommand()) ) {
			this.dialog.dispose();
		}
	}

}
