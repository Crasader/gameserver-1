package com.xinqihd.sns.gameserver.admin.data;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;

/**
 * 强化测试的结果模型
 * 
 * @author wangqi
 *
 */
public class StrengthTestResultModel extends MyTableModel {
	
	private ArrayList<StrengthTestResult> results = 
			new ArrayList<StrengthTestResult>();
	
	private static final String[] COLUMNS = {
		"强化等级", "强化次数", "成功次数", "降级次数", "消耗金币", "成功率", "提升倍率"
	};
	
	private int totalCount = 0;

	@Override
	public void setValueAtWithoutUndo(Object aValue, int row, int column) {

	}

	@Override
	public void reload() {
	}

	/**
	 * @return the totalCount
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * @param totalCount the totalCount to set
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	@Override
	public void insertRow(Object row) {
		if ( row instanceof StrengthTestResult ) {
			results.add((StrengthTestResult)row);
			this.fireTableRowsInserted(results.size()-2, results.size()-1);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return results.size();
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public void deleteRow(int rowIndex) {
		this.results.remove(rowIndex);
		this.fireTableRowsDeleted(rowIndex, rowIndex);
	}

	@Override
	public String getOriginalColumnName(int columnIndex) {
		return COLUMNS[columnIndex];
	}

	@Override
	public Object getRowObject(int rowIndex) {
		StrengthTestResult result = results.get(rowIndex);
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		//"强化等级", "强化次数", "成功次数", "降级次数", "消耗金币", "成功率",    
		StrengthTestResult result = results.get(rowIndex);
		switch ( columnIndex ) {
			case 0:
				return result.getLevelDesc();
			case 1:
				return result.getTryCount();
			case 2:
				return result.getSuccessCount();
			case 3:
				return result.getDownLevelCount();
			case 4:
				return result.getCostMoney();
			case 5:
				return result.getSuccessRatio() +"%";
			case 6:
				return (int)(result.getIncreaseRatio() * 1000)/1000.0;
		}
		return null;
	}

}
