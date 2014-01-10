package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.JXLabel;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;

/**
 * 用来渲染Equpiment和Items表的ID字段
 * @author wangqi
 *
 */
public class EquipAndItemRenderer extends JXLabel implements ListCellRenderer {

	public EquipAndItemRenderer() {
		setOpaque(true);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if ( index < 0 ) {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
			return this;
		}
		
    if (isSelected) {
      setBorder(BorderFactory.createLineBorder(Color.blue));
    } else {
    	setBorder(BorderFactory.createEmptyBorder());
    }

    //Set the icon and text.  If icon was null, say so.
    String iconName = null;
    String id = null;
    String name = null;
    if ( value instanceof ItemPojo )  {
    	ItemPojo item = ((ItemPojo)value);
    	iconName = item.getIcon();
    	id = item.getId();
    	name = item.getName();
    } else if ( value instanceof WeaponPojo ) {
    	WeaponPojo weapon = ((WeaponPojo)value);
    	iconName = weapon.getIcon();
    	id = weapon.getId();
    	name = weapon.getName();
    }
    Icon icon = 
    		MainFrame.ICON_MAPS.get(iconName);
//    		ImageUtil.createImageIconFromAssets(String.valueOf(dbObj.get("icon"))+".png", 24);
    setIcon(icon);
    setText(id+"-"+name);

    return this;
	}

}
