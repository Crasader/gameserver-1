package com.xinqihd.sns.gameserver.admin.model;

import java.util.Set;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.util.ObjectUtil;

public class DBObjectTreeTableNode extends AbstractMutableTreeTableNode {
	
	protected Object key = null;
	protected Object value = null;
	protected DBObject dbObject = null;
	protected String keyName = "_id";
	
	public DBObjectTreeTableNode() {
		super();
	}
	
	public DBObjectTreeTableNode(Object dbObject, String keyName) {
		super(dbObject);
		this.keyName = keyName;
		this.setUserObject(dbObject);
	}
	
	public DBObjectTreeTableNode(Object dbObject, String keyName, boolean allowChildren) {
		super(dbObject, allowChildren);
		this.keyName = keyName;
		this.setUserObject(dbObject);
	}
	
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	
	public String getKeyName() {
		return this.keyName;
	}
	
	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode#setUserObject(java.lang.Object)
	 */
	@Override
	public void setUserObject(Object object) {
		if ( object instanceof DBObject ) {
			this.dbObject = dbObject;
			DBObject dbObj = (DBObject)object;
			Set<String> keys = dbObj.keySet();
			for ( String key : keys ) {
				Object value = this.convertObject(dbObj.get(key));
				DBObjectTreeTableNode node = new DBObjectTreeTableNode(
						new Object[]{key, value}, this.keyName);
				this.add(node);
			}
			this.key = this.convertObject(dbObj.get(this.keyName));	
			this.value = object;
		} else if ( Object[].class.isAssignableFrom(object.getClass()) ) {
			Object[] pair = (Object[])object;
			this.key = this.convertObject(pair[0]);
			this.value = this.convertObject(pair[1]);
			if ( this.value instanceof DBObject ) {
				DBObject dbObj = (DBObject)this.value;
				Set<String> keys = dbObj.keySet();
				for ( String key : keys ) {
					Object value = this.convertObject(dbObj.get(key));
					DBObjectTreeTableNode node = new DBObjectTreeTableNode(
							new Object[]{key, value}, this.keyName);
					node.setKeyName(this.keyName);
					this.add(node);
				}
			}
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	/**
	 * Overridden to specify the return type. Returns the child TreeNode 
	 * at index childIndex. Models that utilize this node should verify
	 * the column count before querying this node, since nodes may return 
	 * differing sizes even for the same model.
	 * 
	 * @return the TreeTableNode corresponding to the specified index
	 * 
	 */
	@Override
	public Object getValueAt(int childIndex) {
		if ( childIndex == 0 ) {
			return this.key;
		} else if ( childIndex == 1 ){
			return this.value;
		} else {
			Class clazz = Object.class;
			if ( this.value != null ) {
				clazz = this.value.getClass();
			}
			return clazz.getSimpleName();
		}
	}

  /**
   * Sets the value for the given {@code column}.
   * 
   * @impl does nothing. It is provided for convenience.
   * @param aValue
   *            the value to set
   * @param column
   *            the column to set the value on
   */
  @Override
  public void setValueAt(Object aValue, int column) {
  	Object parentValue = this.parent.getValueAt(column);
  	Object oldValue = this.value;
  	Object newValue = aValue;
  	if ( oldValue != null ) {
  		newValue = ObjectUtil.convertValue(aValue, oldValue.getClass());
  	}
  	if ( parentValue instanceof DBObject ) {
  		((DBObject) parentValue).put(key.toString(), newValue);
  	}
  	this.value = newValue;
  	System.out.println("value="+newValue+", column:"+column);
  }

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode#toString()
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(key);
		return buf.toString();
	}
 
	/**
	 * @return the key
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the dbObject
	 */
	public DBObject getDBObject() {
		return dbObject;
	}
	/**
	 * Override the method to create object.
	 * @param obj
	 * @return
	 */
	protected Object convertObject(Object obj) {
		if ( obj != null ) {
			return obj;
		}
		return null;
	}
}
