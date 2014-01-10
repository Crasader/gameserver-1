package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.AbstractAddRowAction;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.model.DBObjectListModel;
import com.xinqihd.sns.gameserver.admin.model.DBObjectModel;
import com.xinqihd.sns.gameserver.admin.util.MyWindowUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * Edit the data in Gamedata collection. It may be string, double or double array.
 * @author wangqi
 *
 */
public class MongoDBObjectEditor extends MyAbstractCellEditor 
			implements TableCellEditor, ActionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(MongoDBObjectEditor.class);
	
	private JDialog dialog = null;
	private MyTablePanel myTable = new MyTablePanel();
	private JXButton okButton = new JXButton("确定");
	private Object cellValue = null;
	private AbstractAddRowAction addNewAction = null;
	private MyTableCellEditorFactory editorFactory = null;
	
	public MongoDBObjectEditor() {
		this(null);
	}
	
	public MongoDBObjectEditor(AbstractAddRowAction addAction) {
		this.addNewAction = addAction;
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

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		return this.cellValue;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor#getTableCellEditorComponentAtModel(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponentAtModel(JTable table,
			Object value, boolean isSelected, int modelRowIndex, int modelColumnIndex) {
		this.cellValue = value;
		
		if ( value instanceof BasicDBList ) {
			BasicDBList dbObj = (BasicDBList)value;
			this.cellValue = dbObj.copy();
			
			DBObjectListModel dbObjectModel = null;
			dbObjectModel = new DBObjectListModel((BasicDBList)this.cellValue);
			
			myTable.getTable().setColumnOrders(new String[]{"_id", "name", "icon", "info"});
			myTable.getTable().setCellSelectionEnabled(false);
			
			HashSet<String> hiddenFields = new HashSet<String>();
			hiddenFields.add("class");			
			dbObjectModel.setHiddenFields(hiddenFields);
			
			RefreshMyTableService service = new RefreshMyTableService(dbObjectModel);
			service.execute();
			
			myTable.setTableModel(dbObjectModel);
			myTable.setTitle("游戏数据编辑");
			myTable.setTableModel(dbObjectModel);
			myTable.setEditable(true);
			myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
			if ( editorFactory != null ) {
				myTable.getTable().setEditorFactory(editorFactory);
			}
			if ( this.addNewAction != null ) {
				myTable.setAddRowAction(addNewAction);
				this.addNewAction.setTableModel(dbObjectModel);
			}
			
			this.dialog = MyWindowUtil.getCenterDialog(500, 500, myTable, null);
			this.dialog.setVisible(true);
			
			if ( dbObjectModel.isDataChanged() ) {
				table.getModel().setValueAt(cellValue, modelRowIndex, modelColumnIndex);
			}
		} else if ( value instanceof BasicDBObject ) {
			BasicDBObject dbObj = (BasicDBObject)value;
			this.cellValue = dbObj.copy();
			
			DBObjectModel dbObjectModel = new DBObjectModel(dbObj);
			
			myTable.getTable().setColumnOrders(new String[]{"_id", "name", "icon", "info"});
			myTable.getTable().setCellSelectionEnabled(false);
			
			HashSet<String> hiddenFields = new HashSet<String>();
			hiddenFields.add("class");			
			
			RefreshMyTableService service = new RefreshMyTableService(dbObjectModel);
			service.execute();
			
			myTable.setTableModel(dbObjectModel);
			myTable.setTitle("游戏数据编辑");
			myTable.setTableModel(dbObjectModel);
			myTable.setEditable(true);
			myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
			if ( editorFactory != null ) {
				myTable.getTable().setEditorFactory(editorFactory);
			}
			if ( this.addNewAction != null ) {
				myTable.setAddRowAction(addNewAction);
				this.addNewAction.setTableModel(dbObjectModel);
			}
			
			this.dialog = MyWindowUtil.getCenterDialog(500, 500, myTable, null);
			this.dialog.setVisible(true);
			
			if ( dbObjectModel.isDataChanged() ) {
				table.getModel().setValueAt(dbObjectModel.getDBObject(), modelRowIndex, modelColumnIndex);
			}
		}
		
		return null;
	}



	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	}
	
	public boolean isDataChanged() {
		return false;
	}
	
	public DBObject getNewValue() {
		return null;
	}

}
