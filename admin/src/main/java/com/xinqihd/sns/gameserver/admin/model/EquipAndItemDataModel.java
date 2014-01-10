package com.xinqihd.sns.gameserver.admin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;

public class EquipAndItemDataModel extends AbstractListModel implements ComboBoxModel {
	
	private boolean isItemList = false;
	private List<ItemPojo> items = new ArrayList<ItemPojo>();
	private List<WeaponPojo> weapons = new ArrayList<WeaponPojo>();
	private Object selectedItem = null;

	public EquipAndItemDataModel(boolean itemList) {
		this.isItemList = itemList;
		if ( this.isItemList ) {
			items.addAll(ItemManager.getInstance().getItems());
			Collections.sort(items);
		} else {
			weapons.addAll(EquipManager.getInstance().getWeapons());
			Collections.sort(weapons);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index) {
		if ( this.isItemList ) {
			return items.get(index);
		} else {
			return weapons.get(index);
		}
	}

	@Override
	public int getSize() {
		if ( this.isItemList ) {
			return this.items.size();
		} else {
			return this.weapons.size();
		}
	}

	@Override
	public void setSelectedItem(Object anItem) {
		this.selectedItem = anItem;
	}

	@Override
	public Object getSelectedItem() {
		return this.selectedItem;
	}

}
