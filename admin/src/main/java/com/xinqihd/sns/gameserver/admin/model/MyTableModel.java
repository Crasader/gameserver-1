package com.xinqihd.sns.gameserver.admin.model;

import java.io.File;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.undo.UndoManager;

import com.xinqihd.sns.gameserver.admin.undo.MongoUndoEdit;

public abstract class MyTableModel extends AbstractTableModel implements
		ComboBoxModel {
	
	protected HashSet<String> hiddenFields = new HashSet<String>();

	protected UndoManager undoManager = null;

	protected Object selectedObject = null;

	// For tableModel's listener
	protected List<ListDataListener> tableListeners = new CopyOnWriteArrayList<ListDataListener>();
	// For ListModel's listener
	protected EventListenerList listenerList = new EventListenerList();

	protected boolean isDataChanged = false;

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (undoManager != null) {
			Object oldValue = this.getValueAt(rowIndex, columnIndex);
			MongoUndoEdit edit = new MongoUndoEdit(this, rowIndex, columnIndex,
					oldValue, aValue);
			undoManager.addEdit(edit);
		}
		setValueAtWithoutUndo(aValue, rowIndex, columnIndex);
	}

	/**
	 * 
	 * @param undoManager
	 */
	public void setUndoManager(UndoManager undoManager) {
		this.undoManager = undoManager;
	}

	/**
	 * Return the UndoManager back to table object.
	 * 
	 * @return
	 */
	public UndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * Set a value at given (row,column) without register with undo manager
	 * 
	 * @param aValue
	 * @param row
	 * @param column
	 */
	public abstract void setValueAtWithoutUndo(Object aValue, int row, int column);

	/**
	 * Reload all the table's data
	 */
	public abstract void reload();

	/**
	 * Export the table data to CSV
	 * 
	 * @param file
	 */
	public void export(File file) {
		JOptionPane.showMessageDialog(null, "数据导出功能暂不支持");
	}

	/**
	 * Insert a new row into table
	 * 
	 * @param row
	 */
	public abstract void insertRow(Object row);
	
	
	/**
	 * Update a row in table model
	 * @param row
	 * @param modelRowIndex
	 */
	public void updateRow(Object row, int modelRowIndex) {
		//do nothing.
	}

	/**
	 * Clear all table data.
	 */
	public void clearAll() {
	}
	
	/**
	 * Remove a row from table.
	 * 
	 * @param rowIndex
	 */
	public abstract void deleteRow(int rowIndex);

	/**
	 * Get the original column name.
	 * @param columnIndex
	 * @return
	 */
	public abstract String getOriginalColumnName(int columnIndex);
	
	
	/**
	 * Get the row object. e.g. The DBObject.
	 * @return
	 */
	public abstract Object getRowObject(int rowIndex);
	
	/**
	 * Get all hidden fields
	 * @return
	 */
	public HashSet<String> getHiddenFields() {
		return hiddenFields;
	}

	/**
	 * 
	 * @param hiddenFields
	 */
	public void setHiddenFields(Collection<String> hiddenFields) {
		this.hiddenFields.addAll(hiddenFields);
	}
	
	/**
	 * Return the underlying collection name in MongoDB.
	 * If it is infeasible, return null.
	 * @return
	 */
	public String getCollectionName() {
		return null;
	}
	
	// ------------------------------------------- List Selection Model

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return getRowCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index) {
		if (this.getColumnCount() > 0) {
			Object value = this.getValueAt(index, 0);
			return value;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object anItem) {
		this.selectedObject = anItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		return this.selectedObject;
	}

	public boolean isDataChanged() {
		return isDataChanged;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public abstract Object getValueAt(int rowIndex, int columnIndex);

	/**
	 * Adds a listener to the list that's notified each time a change to the data
	 * model occurs.
	 * 
	 * @param l
	 *          the <code>ListDataListener</code> to be added
	 */
	public void addListDataListener(ListDataListener l) {
		listenerList.add(ListDataListener.class, l);
	}

	/**
	 * Removes a listener from the list that's notified each time a change to the
	 * data model occurs.
	 * 
	 * @param l
	 *          the <code>ListDataListener</code> to be removed
	 */
	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(ListDataListener.class, l);
	}

	/**
	 * Returns an array of all the list data listeners registered on this
	 * <code>AbstractListModel</code>.
	 * 
	 * @return all of this model's <code>ListDataListener</code>s, or an empty
	 *         array if no list data listeners are currently registered
	 * 
	 * @see #addListDataListener
	 * @see #removeListDataListener
	 * 
	 * @since 1.4
	 */
	public ListDataListener[] getListDataListeners() {
		return (ListDataListener[]) listenerList
				.getListeners(ListDataListener.class);
	}

	/**
	 * <code>AbstractListModel</code> subclasses must call this method
	 * <b>after</b> one or more elements of the list change. The changed elements
	 * are specified by the closed interval index0, index1 -- the endpoints are
	 * included. Note that index0 need not be less than or equal to index1.
	 * 
	 * @param source
	 *          the <code>ListModel</code> that changed, typically "this"
	 * @param index0
	 *          one end of the new interval
	 * @param index1
	 *          the other end of the new interval
	 * @see EventListenerList
	 * @see DefaultListModel
	 */
	protected void fireContentsChanged(Object source, int index0, int index1) {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0,
							index1);
				}
				((ListDataListener) listeners[i + 1]).contentsChanged(e);
			}
		}
	}

	/**
	 * <code>AbstractListModel</code> subclasses must call this method
	 * <b>after</b> one or more elements are added to the model. The new elements
	 * are specified by a closed interval index0, index1 -- the enpoints are
	 * included. Note that index0 need not be less than or equal to index1.
	 * 
	 * @param source
	 *          the <code>ListModel</code> that changed, typically "this"
	 * @param index0
	 *          one end of the new interval
	 * @param index1
	 *          the other end of the new interval
	 * @see EventListenerList
	 * @see DefaultListModel
	 */
	protected void fireIntervalAdded(Object source, int index0, int index1) {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0,
							index1);
				}
				((ListDataListener) listeners[i + 1]).intervalAdded(e);
			}
		}
	}

	/**
	 * <code>AbstractListModel</code> subclasses must call this method
	 * <b>after</b> one or more elements are removed from the model.
	 * <code>index0</code> and <code>index1</code> are the end points of the
	 * interval that's been removed. Note that <code>index0</code> need not be
	 * less than or equal to <code>index1</code>.
	 * 
	 * @param source
	 *          the <code>ListModel</code> that changed, typically "this"
	 * @param index0
	 *          one end of the removed interval, including <code>index0</code>
	 * @param index1
	 *          the other end of the removed interval, including
	 *          <code>index1</code>
	 * @see EventListenerList
	 * @see DefaultListModel
	 */
	protected void fireIntervalRemoved(Object source, int index0, int index1) {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0,
							index1);
				}
				((ListDataListener) listeners[i + 1]).intervalRemoved(e);
			}
		}
	}

	/**
	 * Returns an array of all the objects currently registered as
	 * <code><em>Foo</em>Listener</code>s upon this model.
	 * <code><em>Foo</em>Listener</code>s are registered using the
	 * <code>add<em>Foo</em>Listener</code> method.
	 * <p>
	 * You can specify the <code>listenerType</code> argument with a class
	 * literal, such as <code><em>Foo</em>Listener.class</code>. For example, you
	 * can query a list model <code>m</code> for its list data listeners with the
	 * following code:
	 * 
	 * <pre>
	 * ListDataListener[] ldls = (ListDataListener[]) (m
	 * 		.getListeners(ListDataListener.class));
	 * </pre>
	 * 
	 * If no such listeners exist, this method returns an empty array.
	 * 
	 * @param listenerType
	 *          the type of listeners requested; this parameter should specify an
	 *          interface that descends from <code>java.util.EventListener</code>
	 * @return an array of all objects registered as
	 *         <code><em>Foo</em>Listener</code>s on this model, or an empty array
	 *         if no such listeners have been added
	 * @exception ClassCastException
	 *              if <code>listenerType</code> doesn't specify a class or
	 *              interface that implements <code>java.util.EventListener</code>
	 * 
	 * @see #getListDataListeners
	 * 
	 * @since 1.3
	 */
	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listenerList.getListeners(listenerType);
	}
}
