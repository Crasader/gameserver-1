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

public class PropDataListCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if ( value instanceof PropData ) {
			String icon = null;
			String itemId   = null;
			String name = null;
			PropData propData = (PropData)value;
			itemId = propData.getItemId();
			WeaponPojo wpojo = EquipManager.getInstance().getWeaponById(itemId);
			if ( wpojo != null ) {
				icon = wpojo.getIcon();
				name = wpojo.getName();
			} else {
				ItemPojo ipojo = ItemManager.getInstance().getItemById(itemId);
				if ( ipojo != null ) {
					icon = ipojo.getIcon();
					name = ipojo.getName();
				}				
			}
			label.setIcon(MainFrame.ICON_MAPS.get(icon));
			label.setText(itemId + "_" + name);
		} else {
			label.setText(""+value);
		}
		return label;
	}

}
