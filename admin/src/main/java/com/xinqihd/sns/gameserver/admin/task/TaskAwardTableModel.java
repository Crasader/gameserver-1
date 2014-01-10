package com.xinqihd.sns.gameserver.admin.task;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.config.TaskPojo.Award;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class TaskAwardTableModel extends MyTableModel {
	
	private ArrayList<Award> rewards = null;
	private static final String[] COLUMNS = {
		"奖品类型", "道具名称", "道具ID", "类型ID", "性别", "等级", "资源", "数量", "有效期", "颜色",  
	};
//	private ItemPojo pojo;
	private String script = null;
	
	public TaskAwardTableModel(List<Award> rewards) {
//		this.pojo = pojo;
		this.rewards = (ArrayList)rewards;
		reload();
	}
	
	@Override
	public void setValueAtWithoutUndo(Object aValue, int row, int column) {
		Award reward = rewards.get(row);
		//"奖品类型", "道具ID", "类型ID", "等级", "数量", "有效期", "颜色",       
		switch ( column ) {
			case 0:
				reward.type = aValue.toString();
				break;
			case 1:
				break;
			case 2:
				reward.id = aValue.toString();
				break;
			case 3:
				reward.typeId = Integer.parseInt(aValue.toString());
				break;
			case 4:
				reward.sex = (Gender)aValue;
				break;
			case 5:
				reward.lv = StringUtil.toInt(aValue.toString(), 0);
				break;
			case 6:
				reward.resource = aValue.toString();
				break;
			case 7:
				reward.count = StringUtil.toInt(aValue.toString(), 0);
				break;
			case 8:
				reward.indate = StringUtil.toInt(aValue.toString(), 0);
				break;
			case 9:
				reward.color = WeaponColor.valueOf(aValue.toString());
				break;
			case 10:
				break;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if ( columnIndex != 0 ) {
			return true;
		}
		return false;
	}

	@Override
	public void reload() {
	}

	@Override
	public void insertRow(Object row) {
		Object rowObj = row;
		if ( row instanceof DBObject ) {
			rowObj = MongoUtil.constructObject((DBObject)row);
		}
		if ( rowObj instanceof Award ) {
			isDataChanged = true;
			this.rewards.add((Award)rowObj);
			this.reload();
			this.fireTableDataChanged();
		}
	}

	@Override
	public void deleteRow(int rowIndex) {
		isDataChanged = true;
		this.rewards.remove(rowIndex);
		this.reload();
		this.fireTableDataChanged();
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#updateRow(java.lang.Object, int)
	 */
	@Override
	public void updateRow(Object row, int modelRowIndex) {
		this.rewards.set(modelRowIndex, (Award)row);
		this.fireTableRowsUpdated(modelRowIndex, modelRowIndex+1);
	}

	@Override
	public String getOriginalColumnName(int columnIndex) {
		return COLUMNS[columnIndex];
	}

	@Override
	public Object getRowObject(int rowIndex) {
		return rewards.get(rowIndex);
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return rewards.size();
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Award reward = rewards.get(rowIndex);
		//"奖品类型", "道具ID", "类型ID", "等级", "数量", "有效期", "颜色",       
		switch ( columnIndex ) {
			case 0:
				return reward.type;
			case 1:
				if ( reward.id != null ) {
					if ( reward.type.equals(RewardType.ITEM.toString().toLowerCase()) ) {
						ItemPojo item = ItemManager.getInstance().getItemById(reward.id);
						if ( item != null ) {
							return item.getName();
						}
					} else if ( reward.type.equals(RewardType.WEAPON.toString().toLowerCase() ) ) {
						WeaponPojo weapon = EquipManager.getInstance().getWeaponById(reward.id);
						String name = "";
						if ( weapon == null ) {
							weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(
								String.valueOf(reward.typeId), 1);
							name = weapon.getName().substring(3);
						} else {
							name = weapon.getName();
						}
						return name;
					}
				}
			case 2:
				return reward.id;
			case 3:
				return reward.typeId;
			case 4:
				return reward.sex;
			case 5:
				return reward.lv;
			case 6:
				return reward.resource;
			case 7:
				return reward.count;
			case 8:
				return reward.indate;
			case 9:
				return reward.color;
		}
		return null;
	}

}
