package com.xinqihd.sns.gameserver.admin.user;

import java.util.HashMap;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.admin.model.DBObjectTreeTableNode;

public class AccountTreeTableModel extends DefaultTreeTableModel {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountTreeTableModel.class);
	
	//保存所有产生了修改行为的用户名列表
	private HashMap<String, DBObjectTreeTableNode> userNodeSet = new HashMap<String, DBObjectTreeTableNode>();

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#isCellEditable(java.lang.Object, int)
	 */
	@Override
	public boolean isCellEditable(Object obj, int column) {
		if ( obj instanceof DBObjectTreeTableNode ) {
			DBObjectTreeTableNode node = (DBObjectTreeTableNode)obj;
			if ( node.isLeaf() && column == 1 ) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
	 */
	@Override
	public void setValueAt(Object value, Object node, int column) {
		if ( node != null && node instanceof DBObjectTreeTableNode ) {
			DBObjectTreeTableNode userNode = (DBObjectTreeTableNode)node;
			TreeTableNode parent = userNode.getParent();
			while ( parent.getParent() != root ) {
				parent = parent.getParent();
			}
			userNode = (UserTreeTableNode)parent;
			String userId = (String)userNode.getKey();
			userNodeSet.put(userId, userNode);
		}
		super.setValueAt(value, node, column);
	}

	public boolean isDataChanged() {
		return userNodeSet.size()>0;
	}
	
	public HashMap<String, DBObjectTreeTableNode> getChangedMap() {
		return userNodeSet;
	}
}
