package com.xinqihd.sns.gameserver.admin.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.i18n.ColumnNames;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.admin.util.MyDBObjectComparator;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;

/**
 * 该类型仅选择数据库中指定的几列数据
 * @author wangqi
 *
 */
public class FieldsMongoModel extends MyTableModel {
	
	private static final Logger logger = LoggerFactory.getLogger(GameDataManager.class);
	
	protected ArrayList<DBObject> rows = new ArrayList<DBObject>();
	protected ArrayList<String> columnNames = new ArrayList<String>();
	protected ArrayList<Class>  columnClasses = new ArrayList<Class>();
	protected HashSet<String> hiddenFields = new HashSet<String>();
	protected String database;
	protected String namespace;
	protected String collection;
	protected int columnSize = 0;
	
	public FieldsMongoModel(String database, String namespace, String collection, String[] columnNames) {
		this.database = database;
		this.namespace = namespace;
		this.collection = collection;
		for ( String c : columnNames ) {
			this.columnNames.add(c);
		}
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

	@Override
	public void setHiddenFields(Collection<String> hiddenFields) {
		this.hiddenFields.addAll(hiddenFields);
	}
	
	@Override
	public HashSet<String> getHiddenFields() {
		return this.hiddenFields;
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
		return false;
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
		columnClasses.clear();
		rows.clear();
		
		DBObject query = MongoUtil.createDBObject();
		DBObject fields = MongoUtil.createDBObject();
		for ( String c : columnNames ) {
			fields.put(c, "1");
		}
		List<DBObject> results = MongoUtil.queryAllFromMongo(query, database, 
				namespace, collection, fields);
		rows.addAll(results);
		if ( rows.size()>=0 ) {
			DBObject row = rows.get(0);
			for ( String key : row.keySet() ) {
				Object value = row.get(key);
				if ( value != null ) {
					columnClasses.add(value.getClass());
				} else {
					columnClasses.add(Object.class);
				}
			}
			columnSize = columnNames.size();
		}
		Collections.sort(rows, new MyDBObjectComparator());
		logger.debug("Load total {} rows into model", rows.size());
		this.fireTableStructureChanged();
		this.fireTableDataChanged();
		this.fireContentsChanged(this, 0, rows.size());
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
				bw.append(column).append('\t');
			}
			bw.append('\n');
			for ( DBObject dbObj : rows ) {
				for ( String column : columnNames ) {
					bw.append(String.valueOf(dbObj.get(column))).append('\t');
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
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#deleteRow(int)
	 */
	@Override
	public void deleteRow(int rowIndex) {
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAtWithoutUndo(Object aValue, int rowIndex, int columnIndex) {
	}
	
}
