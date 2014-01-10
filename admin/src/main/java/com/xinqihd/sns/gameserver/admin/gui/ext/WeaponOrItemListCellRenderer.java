package com.xinqihd.sns.gameserver.admin.gui.ext;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;

public class WeaponOrItemListCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		String icon = null;
		String itemId   = null;
		String name = null;
		int level = 0;
		if ( value instanceof WeaponPojo ) {
			WeaponPojo wpojo = (WeaponPojo)value;
			if ( wpojo != null ) {
				itemId = wpojo.getId();
				icon = wpojo.getIcon();
				name = wpojo.getName();
				level = wpojo.getLv();
			}
			label.setIcon(MainFrame.ICON_MAPS.get(icon));
			if ( level > 0 ) {
				label.setText(itemId + "_" + name + "_Lv"+level);
			} else {
				label.setText(itemId + "_" + name);
			}
		} else if ( value instanceof ItemPojo ) {
			ItemPojo ipojo = (ItemPojo)value;
			if ( ipojo != null ) {
				itemId = ipojo.getId();
				icon = ipojo.getIcon();
				name = ipojo.getName();
			}
			label.setIcon(MainFrame.ICON_MAPS.get(icon));
			label.setText(itemId + "_" + name);
		} else {
			label.setText(""+value);
		}
		return label;
	}

}
