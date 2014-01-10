package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;


public class IconCellRenderer extends DefaultTableCellRenderer
	implements ListCellRenderer {
	
	public IconCellRenderer() {
		this.setOpaque(true);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		String iconName = value.toString().concat(".png");
//		Icon icon = ImageUtil.createImageIconFromAssets(iconName, 24);
		Icon icon = MainFrame.ICON_MAPS.get(value);
		this.setIcon(icon);
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		Icon icon = null;
		String text = null;
		if ( value instanceof File ) {
			String fileName = ((File)value).getName();
			icon = ImageUtil.createImageIconFromAssets(fileName, 40);
			String iconId = fileName.substring(0, fileName.length()-4);
			text = iconId;
		} else if ( value instanceof String ) {
			String iconId = value.toString();
			text = iconId;
			icon = MainFrame.ICON_MAPS.get(iconId);
		}
		this.setIcon(icon);
		this.setText(text);
		if ( isSelected ) {
      super.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		} else {
			super.setBorder(BorderFactory.createEmptyBorder());
		}
		return this;
	}
	
}
