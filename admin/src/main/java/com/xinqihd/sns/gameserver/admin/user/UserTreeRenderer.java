package com.xinqihd.sns.gameserver.admin.user;

import java.awt.Component;

import javax.swing.JTree;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValues;

public class UserTreeRenderer extends DefaultTreeRenderer {
	
	public UserTreeRenderer() {
		super(IconValues.NONE);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.DefaultTreeRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
//		Component comp = super.getTreeCellRendererComponent(tree, value, selected, expanded,
//				leaf, row, hasFocus);
		String text = "";
		if ( value != null ) {
			text = value.toString();
		}
		JXLabel label = new JXLabel(text);
		return label;
	}

}
