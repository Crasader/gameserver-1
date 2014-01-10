package com.xinqihd.sns.gameserver.admin.item;

import java.util.ArrayList;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class TBoxTableModel extends MyTableModel {
	
	private double q = 1.0;
	private ArrayList<Reward> rewards = new ArrayList<Reward>();
	private ArrayList<Double> p = new ArrayList<Double>();
	private static final String[] COLUMNS = {
		"奖品类型", "道具名称", "性别", "道具ID", "类型ID", "等级", "数量", "有效期", "颜色", "概率"     
	};
//	private ItemPojo pojo;
	private String script = null;
	
	public TBoxTableModel(ArrayList<Reward> rewards, String script, double q) {
//		this.pojo = pojo;
		this.rewards = rewards;
		this.script = script;
		this.q = q;
		reload();
	}
	
	public void setQ(double q) {
		this.q = q;
		reload();
	}

	@Override
	public void setValueAtWithoutUndo(Object aValue, int row, int column) {
		Reward reward = rewards.get(row);
		//"奖品类型", "道具ID", "类型ID", "等级", "数量", "有效期", "颜色", "概率" 
		switch ( column ) {
			case 0:
//				reward.setType(RewardType.valueOf(aValue.toString()));
				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				reward.setPropId(aValue.toString());
				break;
			case 4:
				reward.setTypeId(aValue.toString());
				break;
			case 5:
				reward.setPropLevel(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 6:
				reward.setPropCount(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 7:
				reward.setPropIndate(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 8:
				reward.setPropColor(WeaponColor.from(aValue.toString()));
				break;
			case 9:
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
		//Recalculate the random point if the box type is RandomBox
		if ( ScriptHook.ITEM_BOX_RANDOM_BOX.getHook().equals(
				this.script) ) {
			int low = 0;
			int high = this.rewards.size();
			int[] ps = new int[high];
			for ( int i=0; i<5000; i++ ) {
				int index = MathUtil.nextGaussionInt(low, high, this.q);
				ps[index]++;
			}
			for ( int i=0; i<ps.length; i++ ) {
				p.add(ps[i]/5000.0);
			}
		}
	}

	@Override
	public void insertRow(Object row) {
		Object rowObj = row;
		if ( row instanceof DBObject ) {
			rowObj = MongoUtil.constructObject((DBObject)row);
		}
		if ( rowObj instanceof Reward ) {
			isDataChanged = true;
			this.rewards.add((Reward)rowObj);
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
		Reward reward = rewards.get(rowIndex);
		//"奖品类型", "道具名称", "性别", "道具ID", "等级", "数量", "有效期", "颜色", "概率"     
		switch ( columnIndex ) {
			case 0:
				return reward.getType();
			case 1:
				String name = Constant.EMPTY;
				if ( reward.getType() == RewardType.WEAPON ) {
					WeaponPojo weaponPojo = null;
					if ( StringUtil.checkNotEmpty(reward.getTypeId()) && !Constant.ONE_NEGATIVE.equals(reward.getTypeId()) ) {
						weaponPojo = GameContext.getInstance().getEquipManager().
								getWeaponById(reward.getPropId());
						boolean useType = false;
						if ( weaponPojo == null ) {
							weaponPojo = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(reward.getTypeId(), 0);
							useType = true;
						}
						if ( weaponPojo != null ) {
							name = weaponPojo.getName();
							int index = name.indexOf('●');
							if ( useType && index > 0 ) {
								name = name.substring(index+1);
							}
						} else {
							name = "null";
						}
					} else {
						String typeName = reward.getTypeId();
						if ( "-1".equals(typeName) ) {
							weaponPojo = GameContext.getInstance().getEquipManager().
									getWeaponById(reward.getPropId());
							if ( weaponPojo != null ) {
								name = weaponPojo.getName();
							}
						} else {
							weaponPojo = GameContext.getInstance().getEquipManager().
									getWeaponByTypeNameAndUserLevel(typeName, 1);
							if ( weaponPojo != null ) {
								name = weaponPojo.getName();
							}
						}
					}
				} else if (reward.getType() == RewardType.ITEM ||
						reward.getType() == RewardType.STONE ) {
					ItemPojo item = ItemManager.getInstance().getItemById(reward.getPropId());
					if ( item != null ) {
						name = item.getName();
					}
				} else {
					name = "null";
				}
				return name;
			case 2:
				Gender gender = Gender.ALL;
				if ( reward.getType() == RewardType.WEAPON ) {
					WeaponPojo weaponPojo = null;
					if ( StringUtil.checkNotEmpty(reward.getTypeId()) && !Constant.ONE_NEGATIVE.equals(reward.getTypeId()) ) {
						weaponPojo = GameContext.getInstance().getEquipManager().
								getWeaponById(reward.getPropId());
					}
					if ( weaponPojo == null ) {
						weaponPojo = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(reward.getTypeId(), 0);
					}
					if ( weaponPojo != null ) {
						return weaponPojo.getSex();
					}
				}
				return gender;
			case 3:
				return reward.getPropId();
			case 4:
				return reward.getTypeId();
			case 5:
				return reward.getPropLevel();
			case 6:
				return reward.getPropCount();
			case 7:
				return reward.getPropIndate();
			case 8:
				return reward.getPropColor();
			case 9:
				if ( p.size() > rowIndex ) {
					return Math.round(p.get(rowIndex)*10000)/100.0+"%";
				} else {
					return "100%";
				}
		}
		return null;
	}

}
