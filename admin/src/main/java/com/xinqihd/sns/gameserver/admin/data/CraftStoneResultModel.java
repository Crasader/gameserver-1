package com.xinqihd.sns.gameserver.admin.data;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;

/**
 * 合成测试的结果模型
 * 
 * @author wangqi
 *
 */
public class CraftStoneResultModel extends MyTableModel {
	
	private ArrayList<CraftStoneResult> results = 
			new ArrayList<CraftStoneResult>();
	
	private boolean percentMode = true;
	
	private static final String[] COLUMNS = {
		"最终数值", 
		"1级合成次数", "1+15%", "1+25%", 
		"2级合成次数", "2+15%", "2+25%",
		"3级合成次数", "3+15%", "3+25%", 
		"4级合成次数", "4+15%", "4+25%", 
		"5级合成次数", "5+15%", "5+25%",  
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

	/**
	 * @return the percentMode
	 */
	public boolean isPercentMode() {
		return percentMode;
	}

	/**
	 * @param percentMode the percentMode to set
	 */
	public void setPercentMode(boolean percentMode) {
		if ( this.percentMode != percentMode ) {
			this.percentMode = percentMode;
			this.fireTableDataChanged();
		}
	}

	@Override
	public void insertRow(Object row) {
		if ( row instanceof CraftStoneResult ) {
			results.add((CraftStoneResult)row);
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
		CraftStoneResult result = results.get(rowIndex);
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
		/**
		"最终数值", 
		"1级合成次数", "1+15%", "1+25%", 
		"2级合成次数", "2+15%", "2+25%",
		"3级合成次数", "3+15%", "3+25%", 
		"4级合成次数", "4+15%", "4+25%", 
		"5级合成次数", "5+15%", "5+25%",  
		 */
		CraftStoneResult result = results.get(rowIndex);
		switch ( columnIndex ) {
			case 0:
				return result.getFinalData();
			case 1:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone1Count());
				} else {
					return result.getStone1Count();
				}
			case 2:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone1Luck15Count());
				} else {
					return result.getStone1Luck15Count();
				}
			case 3:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone1Luck25Count());
				} else {
					return result.getStone1Luck25Count();
				}
			case 4:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone2Count());
				} else {
					return result.getStone2Count();
				}
			case 5:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone2Luck15Count());
				} else {
					return result.getStone2Luck15Count();
				}
			case 6:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone2Luck25Count());
				} else {
					return result.getStone2Luck25Count();
				}
			case 7:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone3Count());
				} else {
					return result.getStone3Count();
				}
			case 8:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone3Luck15Count());
				} else {
					return result.getStone3Luck15Count();
				}
			case 9:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone3Luck25Count());
				} else {
					return result.getStone3Luck25Count();
				}
			case 10:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone4Count());
				} else {
					return result.getStone4Count();
				}
			case 11:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone4Luck15Count());
				} else {
					return result.getStone4Luck15Count();
				}
			case 12:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone4Luck15Count());
				} else {
					return result.getStone4Luck15Count();
				}
			case 13:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone5Count());
				} else {
					return result.getStone5Count();
				}
			case 14:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone5Luck15Count());
				} else {
					return result.getStone5Luck15Count();
				}
			case 15:
				if ( percentMode ) {
					return returnPercentNumber(result.getStone5Luck15Count());
				} else {
					return result.getStone5Luck15Count();
				}
		}
		return null;
	}

	private String returnPercentNumber(int value) {
		return ((int)(value * 10000.0 / totalCount))/100.0 + "%";
	}
}
