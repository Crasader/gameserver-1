package com.xinqihd.sns.gameserver.admin.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.util.ObjectUtil;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

/**
 * Wrap the DBObject into TableModel
 * 
 * 这是针对Table中可能存在的数组（DBListObject）对象进行编辑的Editor使用的Model
 * 
 * @author wangqi
 *
 */
public class DBObjectModel extends MyTableModel {
	
	private static final Logger logger = LoggerFactory.getLogger(DBObjectModel.class);
	
	private static final String[] COLUMNS = {"关键字", "值"};
	private static final Class[] COLUMN_CLASS = {String.class, Object.class};
	
	private DBObject dbObject = null;
	private int rowCount = 0;
	private List<String> rowKeys = new ArrayList<String>();
	protected boolean isDataChanged = false;
	private HashSet<String> hiddenFields = new HashSet<String>();
	
	/**
	 * 
	 * @param dbObject
	 */
	public DBObjectModel(DBObject obj) {
		if ( obj instanceof BasicDBList ) {
			this.dbObject = (DBObject)((BasicDBList)obj).copy();
		} else {
			this.dbObject = (DBObject)((BasicDBObject)obj).copy();
		}
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_CLASS[columnIndex];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if ( columnIndex > 0 ) {
			return true;
		}
		return false;
	}
	
	public boolean isDataChanged() {
		return isDataChanged;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String key = rowKeys.get(rowIndex);
		if ( columnIndex == 0 ) {
			return key;
		} else {
			return this.dbObject.get(key);
		}
	}
	
	public DBObject getDBObject() {
		return this.dbObject;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#setValueAtWithoutUndo(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAtWithoutUndo(Object aValue, int row, int column) {
		String key = rowKeys.get(row);
		Object value = this.dbObject.get(key);
		Object newValue = aValue;
		if ( value != null ) {
			newValue = ObjectUtil.convertValue(aValue, value.getClass());
		}
		if ( column == 1 && !value.equals(aValue) ) {
			this.dbObject.put(key, newValue);
			isDataChanged = true;
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#reload()
	 */
	@Override
	public void reload() {
		if ( this.dbObject != null ) {
			Set<String> keys = new HashSet<String>(this.dbObject.keySet());
			rowKeys.clear();
			for ( String key: keys ) {
				Object value = dbObject.get(key);
				logger.debug("row: {}, column: {}", key, value);
				rowKeys.add(key);
			}
			rowCount = keys.size();
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#export(java.io.File)
	 */
	@Override
	public void export(File file) {
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#insertRow(java.lang.Object)
	 */
	@Override
	public void insertRow(Object row) {
		if ( row instanceof DBObject ) {
			DBObject dbObj = ((DBObject) row);
			String key = (String)dbObj.get(COLUMNS[0]);
			Object value = dbObj.get(COLUMNS[1]);
			this.dbObject.put(key, value);
			reload();
			isDataChanged = true;
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#deleteRow(int)
	 */
	@Override
	public void deleteRow(int rowIndex) {
		// TODO Auto-generated method stub
		String key = rowKeys.get(rowIndex);
		this.dbObject.removeField(key);
		isDataChanged = true;
		reload();
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getOriginalColumnName(int)
	 */
	@Override
	public String getOriginalColumnName(int columnIndex) {
		return COLUMNS[columnIndex];
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getRowObject(int)
	 */
	@Override
	public Object getRowObject(int rowIndex) {
		String key = rowKeys.get(rowIndex);
		Object value =  this.dbObject.get(key);
		DBObject dbObj = MongoUtil.createDBObject(COLUMNS[0], key);
		dbObj.put(COLUMNS[1], value);
		return dbObj;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getHiddenFields()
	 */
	@Override
	public HashSet<String> getHiddenFields() {
		return hiddenFields;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#setHiddenFields(java.util.Collection)
	 */
	@Override
	public void setHiddenFields(Collection<String> hiddenFields) {
		this.hiddenFields.addAll(hiddenFields);
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getCollectionName()
	 */
	@Override
	public String getCollectionName() {
		return null;
	}

}
