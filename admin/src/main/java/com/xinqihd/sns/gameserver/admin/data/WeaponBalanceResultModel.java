package com.xinqihd.sns.gameserver.admin.data;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;

/**
 * 保存武器平衡测试的结果模型
 * 
 * @author wangqi
 *
 */
public class WeaponBalanceResultModel extends MyTableModel {
	
	private ArrayList<WeaponBalanceResult> results = 
			new ArrayList<WeaponBalanceResult>();
	
	private static final String[] COLUMNS = {
		"玩家1", "玩家2", "玩家1武器", "玩家2武器", "玩家1战斗力", "玩家2战斗力", "玩家1等级", "玩家2等级", "等级差", "结果", "回合数","玩家1经验","玩家2经验"     
	};

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAtWithoutUndo(Object aValue, int row, int column) {

	}

	@Override
	public void reload() {
	}

	@Override
	public void insertRow(Object row) {
		if ( row instanceof WeaponBalanceResult ) {
			results.add((WeaponBalanceResult)row);
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
		WeaponBalanceResult result = results.get(rowIndex);
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
		//"玩家1战斗力", "玩家2战斗力", "玩家1等级", "玩家2等级", "结果", "回合数", "玩家1武器", "玩家2武器",
		WeaponBalanceResult result = results.get(rowIndex);
		switch ( columnIndex ) {
			case 0:
				return result.getUser1();
			case 1:
				return result.getUser2();
			case 2:
				PropData propData = result.getUser1().getBag().getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
				String weapon = propData.getName()+"-Lv"+propData.getLevel();
				return weapon;
			case 3:
				propData = result.getUser2().getBag().getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
				weapon = propData.getName()+"-Lv"+propData.getLevel();
				return weapon;
			case 4:
				return result.getUser1().getPower();
			case 5:
				return result.getUser2().getPower();
			case 6:
				return result.getUser1().getLevel();
			case 7:
				return result.getUser2().getLevel();
			case 8:
				return result.getUser1().getLevel() - result.getUser2().getLevel();
			case 9:
				if ( result.getUser1Win()== 0) {
					return"玩家1胜";
				} else if ( result.getUser1Win() == 1 ) {
					return "玩家1败";
				} else if ( result.getUser1Win() == -1 ) {
					return "无限";
				}
			case 10:
				return result.getRoundCount();
			case 11:
				return result.getUser1Exp();
			case 12:
				return result.getUser2Exp();
		}
		return null;
	}

}
