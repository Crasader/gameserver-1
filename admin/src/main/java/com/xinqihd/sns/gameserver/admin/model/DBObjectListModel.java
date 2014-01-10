package com.xinqihd.sns.gameserver.admin.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.i18n.ColumnNames;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;

/**
 * Use DBObject as the data model
 * 这个模型针对数据库中某一列对象为DBObject的情况
 * @author wangqi
 *
 */
public class DBObjectListModel extends MyTableModel {
	
	private static final Logger logger = LoggerFactory.getLogger(GameDataManager.class);
	
	private ArrayList<DBObject> rows = new ArrayList<DBObject>();
	private ArrayList<String> columnNames = new ArrayList<String>();
	private ArrayList<Class>  columnClasses = new ArrayList<Class>();
	private HashSet<String> hiddenFields = new HashSet<String>();
	private int columnSize = 0;
	private List<DBObject> dbObjList = null;
	private BasicDBList cellValue = null;
	
	public DBObjectListModel(BasicDBList cellValue) {
		this.cellValue = cellValue;
		this.dbObjList = new ArrayList();
		BasicDBList dbObj = (BasicDBList)cellValue;
		if ( dbObj != null ) {
			for ( Object obj : dbObj ) {
				this.dbObjList.add((DBObject)obj);
			}
		}
	}
	
	public void setHiddenFields(Collection<String> hiddenFields) {
		this.hiddenFields.addAll(hiddenFields);
	}
	
	public HashSet<String> getHiddenFields() {
		return this.hiddenFields;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return rows.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columnSize;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DBObject dbObject = rows.get(rowIndex);
		String columnKey = columnNames.get(columnIndex);
		return dbObject.get(columnKey);
	}
	
	@Override
	public Object getRowObject(int rowIndex) {
		return rows.get(rowIndex);
	}
	

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		String cn = columnNames.get(column);
		return ColumnNames.translate(cn);
	}
	
	@Override
	public String getOriginalColumnName(int column) {
		return columnNames.get(column);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses.get(columnIndex);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	@Override
	public String getCollectionName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#reload()
	 */
	@Override
	public void reload() {
		//Load data from database
		columnNames.clear();
		columnClasses.clear();
		rows.clear();
		
		DBObject query = MongoUtil.createDBObject();
		rows.addAll(this.dbObjList);
		if ( rows.size()>0 ) {
			DBObject row = rows.get(0);
			for ( String key : row.keySet() ) {
				if ( hiddenFields.contains(key) ) {
					continue;
				}
				columnNames.add(key);
				Object value = row.get(key);
				if ( value != null ) {
					columnClasses.add(value.getClass());
				} else {
					columnClasses.add(Object.class);
				}
			}
			columnSize = columnNames.size();
		}
		logger.debug("Load total {} rows into model", rows.size());
		this.fireTableStructureChanged();
		this.fireTableDataChanged();
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#export(java.io.File)
	 */
	@Override
	public void export(File file) {
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			//output header
			for ( String column : columnNames ) {
				if ( !hiddenFields.contains(column) ) {
					bw.append(column).append('\t');
				}
			}
			bw.append('\n');
			for ( DBObject dbObj : rows ) {
				for ( String column : columnNames ) {
					if ( !hiddenFields.contains(column) ) {
						bw.append(String.valueOf(dbObj.get(column))).append('\t');
					}
				}
				bw.append('\n');
			}
			bw.close();
		} catch (IOException e) {
			logger.warn("Failed to export file: {}", file);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#insertRow(java.lang.Object)
	 */
	@Override
	public void insertRow(Object row) {
		DBObject objectToSave = (DBObject)row;
		rows.add(objectToSave);
		cellValue.add(objectToSave);
		isDataChanged = true;
		this.fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#deleteRow(int)
	 */
	@Override
	public void deleteRow(int rowIndex) {
		DBObject objToDelete = rows.get(rowIndex);
		rows.remove(rowIndex);
		cellValue.remove(rowIndex);
		isDataChanged = true;
		this.fireTableRowsDeleted(rowIndex, rowIndex);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAtWithoutUndo(Object aValue, int rowIndex, int columnIndex) {
		DBObject objectToSave = rows.get(rowIndex);
		String column = columnNames.get(columnIndex);
		Object oldValue = objectToSave.get(column);
		if ( aValue!=null && !aValue.equals(oldValue) ) {
			logger.debug("Save {} at ({}) to database.", aValue, rowIndex+","+columnIndex);
			objectToSave.put(column, aValue);
			isDataChanged = true;
		}
		cellValue.set(rowIndex, objectToSave);
	}
	
}
