package com.xinqihd.sns.gameserver.admin.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class ArrayListModel extends AbstractListModel {
	
	private ArrayList list = new ArrayList();
	
	public ArrayListModel() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return list.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index) {
		return list.get(index);
	}
	
	public void insertRow(Object row) {
		list.add(row);
		this.fireContentsChanged(list, 0, list.size()-1);
	}
	
	public void deleteRow(int row) {
		if ( row >= 0 && row < list.size() ) {
			list.remove(row);
			this.fireContentsChanged(list, 0, list.size()-1);
		}
	}
	
	public void clear() {
		this.list.clear();
		this.fireContentsChanged(list, 0, list.size()-1);
	}
	
	public List getList() {
		return list;
	}
}
