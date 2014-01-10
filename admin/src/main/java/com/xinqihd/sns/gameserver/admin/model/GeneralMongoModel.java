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

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.i18n.ColumnNames;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.admin.util.ObjectUtil;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;

public class GeneralMongoModel extends MyTableModel {
	
	private static final Logger logger = LoggerFactory.getLogger(GameDataManager.class);
	
	private ArrayList<DBObject> rows = new ArrayList<DBObject>();
	private ArrayList<String> columnNames = new ArrayList<String>();
	private ArrayList<Class>  columnClasses = new ArrayList<Class>();
	private HashSet<String> hiddenFields = new HashSet<String>();
	private String database;
	private String namespace;
	private String collection;
	private int columnSize = 0;
	
	public GeneralMongoModel(String database, String namespace, String collection) {
		this.database = database;
		this.namespace = namespace;
		this.collection = collection;
	}
	
	@Override
	public void setHiddenFields(Collection<String> hiddenFields) {
		this.hiddenFields.addAll(hiddenFields);
	}
	
	@Override
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
		if ( columnIndex > columnClasses.size()-1 ) {
			return String.class;
		} else {
			return columnClasses.get(columnIndex);
		}
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
		return this.collection;
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
		List<DBObject> results = MongoUtil.queryAllFromMongo(query, database, 
				namespace, collection, null);
		rows.addAll(results);
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
		DBObject query = MongoUtil.createDBObject("_id", objectToSave.get("_id"));
		MongoUtil.saveToMongo(query, objectToSave, database, namespace, collection, true);
		rows.add(objectToSave);
		this.fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#updateRow(java.lang.Object, int)
	 */
	@Override
	public void updateRow(Object row, int modelRowIndex) {
		DBObject objectToSave = (DBObject)row;
		rows.set(modelRowIndex, objectToSave);
		DBObject query = MongoUtil.createDBObject("_id", objectToSave.get("_id"));
		MongoUtil.saveToMongo(query, objectToSave, database, namespace, collection, true);
		this.fireTableRowsUpdated(modelRowIndex, modelRowIndex);
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#deleteRow(int)
	 */
	@Override
	public void deleteRow(int rowIndex) {
		DBObject objToDelete = rows.get(rowIndex);
		MongoUtil.deleteFromMongo(objToDelete, database, namespace, collection, true);
		rows.remove(rowIndex);
		this.fireTableRowsDeleted(rowIndex, rowIndex);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAtWithoutUndo(Object aValue, int rowIndex, int columnIndex) {
		DBObject objectToSave = rows.get(rowIndex);
		Object _id = objectToSave.get("_id");
		DBObject query = MongoUtil.createDBObject();
		if ( _id != null ) {
			query.put("_id", _id);
		}
		String column = columnNames.get(columnIndex);
		Object oldValue = objectToSave.get(column);
		Object newValue = null;
		if ( oldValue != null ) {
			newValue = ObjectUtil.convertValue(aValue, oldValue.getClass());
		} else {
			try {
				newValue = Integer.parseInt(aValue.toString());
			} catch (NumberFormatException e) {
				try {
					newValue = Double.parseDouble(aValue.toString());
				} catch (NumberFormatException e1) {
					newValue = aValue;
				}
			}
			
		}
		if ( aValue!=null ) {
			logger.debug("Save {} at ({}) to database.", newValue, rowIndex+","+columnIndex);
			objectToSave.put(column, newValue);
			MongoUtil.saveToMongo(query, objectToSave, database, namespace, collection, true);			
		}
	}
	
}
