package com.xinqihd.sns.gameserver.redis;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;


public class RedisTreeTableModel extends DefaultTreeTableModel {
	
	private String[] names = new String[]{"Key", "Hex", "type", "value", "ttl"};
	private Class[] types = new Class[]{String.class, String.class, String.class, String.class, String.class};
	
	public RedisTreeTableModel() {
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return names.length;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return names[column];
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#getValueAt(java.lang.Object, int)
	 */
	@Override
	public Object getValueAt(Object node, int column) {
		Object value = null;
		if ( node instanceof DefaultMutableTreeTableNode ) {
			DefaultMutableTreeTableNode mutableNode = (DefaultMutableTreeTableNode)node;
			Object o = mutableNode.getUserObject();
			if ( o != null && o instanceof String[] ) {
				String[] array = (String[])o;
				if ( column >= array.length ) {
					return "";
				} else {
					value = array[column];
				}
			}
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#isCellEditable(java.lang.Object, int)
	 */
	@Override
	public boolean isCellEditable(Object node, int column) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int column) {
		return types[column];
	}

}
