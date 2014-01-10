package com.xinqihd.sns.gameserver.admin.user;

import com.xinqihd.sns.gameserver.admin.model.DBObjectTreeTableNode;
import com.xinqihd.sns.gameserver.entity.user.UserId;

public class UserTreeTableNode extends DBObjectTreeTableNode {
	
	public UserTreeTableNode() {
		super();
	}
	
	public UserTreeTableNode(Object dbObject, String keyName) {
		super(dbObject, keyName);
	}
	
	public UserTreeTableNode(Object dbObject, String keyName, boolean allowChildren) {
		super(dbObject, keyName, allowChildren);
	}
	
	@Override
	protected Object convertObject(Object obj) {
		if ( obj != null ) {
			if ( byte[].class.isAssignableFrom(obj.getClass()) ) {
				UserId userid = UserId.fromBytes((byte[])obj);
				return userid;			
			} else {
				return obj;
			}
		}
		return null;
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
    System.out.println("value="+aValue+", column:"+column);
  	
  }  
}
