package com.xinqihd.sns.gameserver.admin.data;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;

/**
 * 保存武器平衡测试的结果模型
 * 
 * @author wangqi
 *
 */
public class ShopDataPriceResultModel extends MyTableModel {
	
	private ArrayList<ShopDataPricePrintResult> results = 
			new ArrayList<ShopDataPricePrintResult>();
	
	private static final String[] COLUMNS = {
		"ID", "类型",
		"武器", "战斗力", "范围", "攻击", "防御", "敏捷", "幸运", 
		"简陋金币", //"简陋勋章", "简陋礼券", 
		"简陋元宝",
		"普通金币", //"普通勋章", "普通礼券", 
		"普通元宝",
		"坚固金币", //"坚固勋章", "坚固礼券", 
		"坚固元宝",
		"永久金币", //"永久勋章", "永久礼券", 
		"永久元宝",
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
		if ( row instanceof ShopDataPricePrintResult ) {
			results.add((ShopDataPricePrintResult)row);
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
		ShopDataPricePrintResult result = results.get(rowIndex);
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
		"武器", "战斗力", "攻击", "防御", "敏捷", "幸运", 
		"金币", "勋章", "礼券", "元宝"   
		 */
		ShopDataPricePrintResult result = results.get(rowIndex);
		WeaponPojo weapon = result.getWeaponPojo();
		switch ( columnIndex ) {
			case 0:
				return weapon.getId();
			case 1:
				return weapon.getSlot().name();
			case 2:
				return weapon.getName();
			case 3:
				return EquipCalculator.calculateWeaponPower(weapon);
			case 4:
				return weapon.getRadius();
			case 5:
				return weapon.getAddAttack();
			case 6:
				return weapon.getAddDefend();
			case 7:
				return weapon.getAddAgility();
			case 8:
				return weapon.getAddLuck();
			case 9:
				int price = result.getGoldenPrice();
				if ( price < 15 ) {
					price = 15;
				}
				return price;
				/*
			case 7:
				return result.getMedalPrice();
			case 8:
				return result.getVoucherPrice();
				*/
			case 10:
				price = result.getYuanbaoPrice();
				if ( price < 2 ) {
					price = 2;
				}
				return price;
			case 11:
				price = result.getNormalGoldPrice();
				if ( price == 0 ) {
					price = 30;
				}
				return price;
				/*
			case 11:
				return result.getNormalMedalPrice();
			case 12:
				return result.getNormalVoucherPrice();
				*/
			case 12:
				price = result.getNormalYuanbaoPrice();
				if ( price < 4 ) {
					price = 4;
				}
				return price;
			case 13:
				price = result.getSolidGoldPrice();
				if ( price < 75 ) {
					price = 75;
				}
				return price;
				/*
			case 15:
				return result.getSolidMedalPrice();
			case 16:
				return result.getSolidVoucherPrice();
				*/
			case 14:
				price = result.getSolidYuanbaoPrice();
				if ( price < 10 ) {
					price = 10;
				}
				return price;
			case 15:
				price = result.getEternalGoldPrice();
				if ( price < 150 ) {
					price = 150;
				}
				return price;
				/*
			case 19:
				return result.getEternalMedalPrice();
			case 20:
				return result.getEternalVoucherPrice();
				*/
			case 16:
				price = result.getEternalYuanbaoPrice();
				if ( price < 20 ) {
					price = 20;
				}
				return price;
		}
		return "N/A";
	}

	@Override
	public void clearAll() {
		results.clear();
		this.fireTableDataChanged();
	}
}
