package com.xinqihd.sns.gameserver.admin.item;

import java.util.ArrayList;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardCondition;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class RewardConditionTableModel extends MyTableModel {
	
	private ArrayList<RewardCondition> conditions = new ArrayList<RewardCondition>();
	private static final String[] COLUMNS = {
		"道具ID", "数量", "类型"     
	};
	private ItemPojo pojo;
	
	public RewardConditionTableModel(ItemPojo pojo) {
		this.pojo = pojo;
		this.conditions = pojo.getConditions();
	}


	@Override
	public void setValueAtWithoutUndo(Object aValue, int row, int column) {
		RewardCondition condition = conditions.get(row);
		//"道具ID", "数量", "类型"  
		switch ( column ) {
			case 0:
				condition.setId(aValue.toString());
				break;
			case 1:
				condition.setCount(StringUtil.toInt(aValue.toString(), 1));
				break;
			case 2:
				condition.setRewardType(RewardType.valueOf(aValue.toString()));
				break;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
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
		if ( rowObj instanceof RewardCondition ) {
			isDataChanged = true;
			this.conditions.add((RewardCondition)rowObj);
			this.reload();
			this.fireTableDataChanged();
		}
	}

	@Override
	public void deleteRow(int rowIndex) {
		isDataChanged = true;
		this.conditions.remove(rowIndex);
		this.reload();
		this.fireTableDataChanged();
	}

	@Override
	public String getOriginalColumnName(int columnIndex) {
		return COLUMNS[columnIndex];
	}

	@Override
	public Object getRowObject(int rowIndex) {
		return conditions.get(rowIndex);
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return conditions.size();
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
		RewardCondition reward = conditions.get(rowIndex);
		//"道具ID", "数量", "类型"  
		switch ( columnIndex ) {
			case 0:
				return reward.getId();
			case 1:
				return reward.getCount();
			case 2:
				return reward.getRewardType();
		}
		return null;
	}

}
