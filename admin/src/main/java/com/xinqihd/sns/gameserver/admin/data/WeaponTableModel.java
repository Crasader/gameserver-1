package com.xinqihd.sns.gameserver.admin.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.battle.BattleBitSetBullet;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class WeaponTableModel extends MyTableModel {
	
	private ArrayList<WeaponPojo> weapons = new ArrayList<WeaponPojo>();
	private static final String[] COLUMNS = {
		"ID", "名称", "用户等级", "类型ID", "类型", "DPR", 
		"攻击", "防御", "敏捷", "幸运", "血量", "血量增量%", "护甲",
		"子弹", "子弹宽度", "子弹高度"     
	};
	private String bulletPath = null;
	private boolean loadBullet = false;
	
	public WeaponTableModel() {
	}
	
	/**
	 * @return the bulletPath
	 */
	public String getBulletPath() {
		return bulletPath;
	}

	/**
	 * @param bulletPath the bulletPath to set
	 */
	public void setBulletPath(String bulletPath) {
		this.bulletPath = bulletPath;
	}

	@Override
	public void setValueAtWithoutUndo(Object aValue, int row, int column) {
		WeaponPojo weapon = weapons.get(row);
		//"ID", "名称", "用户等级", "类型ID", "类型", "DPR", "攻击", "防御", "敏捷", "幸运", "血量", "血量增量%" 
		switch ( column ) {
			case 0:
				weapon.setId(aValue.toString());
				break;
			case 1:
				weapon.setName(aValue.toString());
				break;
			case 2:
				weapon.setUserLevel(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 3:
				weapon.setTypeName(aValue.toString());
				break;
			case 4:
				weapon.setSlot(EquipType.valueOf(aValue.toString().toUpperCase()));
				break;
			case 5:
				weapon.setPower(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 6:
				weapon.setAddAttack(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 7:
				weapon.setAddDefend(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 8:
				weapon.setAddAgility(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 9:
				weapon.setAddLuck(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 10:
				weapon.setAddBlood(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 11:
				weapon.setAddBloodPercent(StringUtil.toInt(aValue.toString(), 0));
				break;
			case 12:
				weapon.setAddSkin(StringUtil.toInt(aValue.toString(), 0));
				break;
		}
		System.out.println(aValue);
	}

	@Override
	public void reload() {
		if ( StringUtil.checkNotEmpty(bulletPath) ) {
			if ( !loadBullet ) {
				GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, 
						bulletPath);
				BattleDataLoader4Bitmap.loadBattleBullet();
				loadBullet = true;
			}
		}
		Collection<WeaponPojo> weaponList = EquipManager.getInstance().getWeapons();
		for ( WeaponPojo weapon : weaponList ) {
			//if ( weapon.getUserLevel() == 0 )
			this.weapons.add(weapon);
		}
	}
	
	public void setWeapons(Collection<WeaponPojo> newWeapons) {
		this.weapons.clear();
		this.weapons.addAll(newWeapons);
		this.fireTableDataChanged();
	}

	@Override
	public void insertRow(Object row) {
		Object rowObj = row;
		if ( row instanceof DBObject ) {
			rowObj = MongoUtil.constructObject((DBObject)row);
		}
		if ( rowObj instanceof WeaponPojo ) {
			isDataChanged = true;
			this.weapons.add((WeaponPojo)rowObj);
			this.reload();
			this.fireTableDataChanged();
		}
	}

	@Override
	public void deleteRow(int rowIndex) {
		isDataChanged = true;
		this.weapons.remove(rowIndex);
		this.reload();
		this.fireTableDataChanged();
	}

	@Override
	public String getOriginalColumnName(int columnIndex) {
		return COLUMNS[columnIndex];
	}

	@Override
	public Object getRowObject(int rowIndex) {
		return weapons.get(rowIndex);
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return weapons.size();
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
	
	public String[] getColumnNames() {
		return COLUMNS;
	}
	
	public List<WeaponPojo> getWeaponList() {
		return this.weapons;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		WeaponPojo weapon = weapons.get(rowIndex);
		//"ID", "名称", "用户等级", "类型ID", "类型", "DPR", "攻击", "防御", "敏捷", "幸运", "血量", "血量增量%" 
		switch ( columnIndex ) {
			case 0:
				return weapon.getId();
			case 1:
				return weapon.getName();
			case 2:
				return weapon.getUserLevel(); 
			case 3:
				return weapon.getTypeName();
			case 4:
				return weapon.getSlot(); 
			case 5:
				return calculateDpr(weapon);
			case 6:
				return weapon.getAddAttack();
			case 7:
				return weapon.getAddDefend();
			case 8:
				return weapon.getAddAgility();
			case 9:
				return weapon.getAddLuck();
			case 10:
				return weapon.getAddBlood();
			case 11:
				return weapon.getAddBloodPercent();
			case 12:
				return weapon.getAddSkin();
			case 13:
				return weapon.getBullet();
			case 14:
				String bullet = weapon.getBullet();
				if ( StringUtil.checkNotEmpty(bullet) ) {
					BattleBitSetBullet battbleBullet = 
							BattleDataLoader4Bitmap.getBattleBulletByName(bullet);
					if ( battbleBullet != null ) {
						return battbleBullet.getBullet().getWidth();
					}
				}
			case 15:
				bullet = weapon.getBullet();
				if ( StringUtil.checkNotEmpty(bullet) ) {
					BattleBitSetBullet battbleBullet = 
							BattleDataLoader4Bitmap.getBattleBulletByName(bullet);
					if ( battbleBullet != null ) {
						return battbleBullet.getBullet().getHeight();
					}
				}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.model.MyTableModel#export(java.io.File)
	 */
	@Override
	public void export(File file) {
		try {
			FileWriter fw = new FileWriter(file, false);
			for ( int i=0; i<COLUMNS.length; i++ ) {
				fw.append(COLUMNS[i]).append('\t');
			}
			fw.append('\n');
			for ( WeaponPojo weapon : weapons ) {
				fw.append(weapon.getId()).append('\t');
				fw.append(weapon.getName()).append('\t');
				fw.append(String.valueOf(weapon.getUserLevel())).append('\t');
				fw.append(weapon.getTypeName()).append('\t');
				fw.append(weapon.getSlot().toString()).append('\t');
				fw.append(String.valueOf(calculateDpr(weapon))).append('\t');
				fw.append(String.valueOf(weapon.getAddAttack())).append('\t');
				fw.append(String.valueOf(weapon.getAddDefend())).append('\t');
				fw.append(String.valueOf(weapon.getAddAgility())).append('\t');
				fw.append(String.valueOf(weapon.getAddLuck())).append('\t');
				fw.append(String.valueOf(weapon.getAddBlood())).append('\t');
				fw.append(String.valueOf(weapon.getAddBloodPercent())).append('\t');
				fw.append(String.valueOf(weapon.getAddSkin())).append('\t');
				String bullet = weapon.getBullet();
				if ( StringUtil.checkNotEmpty(bullet) ) {
					BattleBitSetBullet battbleBullet = 
							BattleDataLoader4Bitmap.getBattleBulletByName(bullet);
					if ( battbleBullet != null ) {
						fw.append(bullet).append('\t');
						fw.append(String.valueOf(battbleBullet.getBullet().getWidth())).append('\t');
						fw.append(String.valueOf(battbleBullet.getBullet().getHeight())).append('\t');
					}
				}
				fw.append('\n');
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Import the exported format file into memory.
	 * If a line starts with '#‘, ignore it as a comment.
	 */
	public void importExcel(File file, ArrayList<Double> levelDprList) {
		if ( file == null || !file.exists() ) {
			System.out.println("File " + file + " does not exist.");
			return;
		}
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while ( line != null ) {
				if ( line.charAt(0) == '#' ) {
					line = br.readLine();
					continue;
				}
				/*
					0:700
					1:黑铁●玄武壳
					2:0
					3:70
					4:weapon
					5:
					6:10
					7:20
					8:20
					9:40
					10:0
					11:0
					12:bullet_black
					13:150
					14:150
					15:攻击范围非常大，适合埋人
				 */
				String[] fields = line.split("\t");
				int id = StringUtil.toInt(fields[0], 0);
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(String.valueOf(id));
				if ( weapon != null ) {
					String name = fields[1];
					int typeId = StringUtil.toInt(fields[3], 0);
					EquipType slot = EquipType.valueOf(fields[4].toUpperCase());
					int attack = StringUtil.toInt(fields[6], 0);
					int defend = StringUtil.toInt(fields[7], 0);
					int agility = StringUtil.toInt(fields[8], 0);
					int luck = StringUtil.toInt(fields[9], 0);
					int blood = StringUtil.toInt(fields[10], 0);
					int bloodPercent = StringUtil.toInt(fields[11], 0);
					int skin = StringUtil.toInt(fields[12], 0);
					int power = (int)EquipCalculator.calculateWeaponPower(attack, defend, agility, luck, blood, skin);
					//weapon.setName(name);
					weapon.setSlot(slot);
					weapon.setAddAttack(attack);
					weapon.setAddDefend(defend);
					weapon.setAddAgility(agility);
					weapon.setAddLuck(luck);
					weapon.setAddBlood(blood);
					weapon.setAddBloodPercent(bloodPercent);
					weapon.setAddSkin(skin);
					weapon.setPower(power);
					if ( levelDprList != null ) {
						for ( int i = 1; i<10; i++ ) {
							double dprRatio = 1.0;
							if ( levelDprList.size()>=i ) {
								dprRatio = levelDprList.get(i-1);
							} else {
								dprRatio = levelDprList.get(levelDprList.size()-1);
							}
							id++;
							weapon = EquipManager.getInstance().getWeaponById(String.valueOf(id));
							if ( weapon != null ) {
								int newAttack = (int)(attack * dprRatio);
								int newDefend = (int)(defend * dprRatio);
								int newAgility = (int)(agility * dprRatio);
								int newLuck = (int)(luck * dprRatio);
								int newSkin = (int)(skin * dprRatio);
								int newPower = (int)EquipCalculator.calculateWeaponPower(
										newAttack, newDefend, newAgility, newLuck, blood, newSkin);
								//weapon.setName(name);
								weapon.setAddAttack(newAttack);
								weapon.setAddDefend(newDefend);
								weapon.setAddAgility(newAgility);
								weapon.setAddLuck(newLuck);
								weapon.setAddSkin(newSkin);
								weapon.setPower(newPower);
								weapon.setSlot(slot);
							} else {
								System.out.println("#importExcel: not found weapon id:'"+id+"'");			
							}
						}
					}
				} else {
					System.out.println("#importExcel: not found weapon id:'"+id+"'");
				}
				line = br.readLine();
			}
			this.setWeapons(EquipManager.getInstance().getWeapons());
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if ( columnIndex < 12 ) {
			return true;
		} else {
			return false;
		}
	}

	private int calculateDpr(WeaponPojo weapon) {
		/*
		double attack = weapon.getAddAttack()/1.3;
		double defend = weapon.getAddDefend()/1.5;
		double agility = (attack+defend)*(1+weapon.getAddAgility()/3740);
		//luck = DPR * 暴击发动的概率 * 暴击伤害率
		double luck = (attack+defend)*(weapon.getAddLuck()/4253.9)*(1.5+2*weapon.getAddLuck()/10000);
		
		return (int)(attack + defend + agility + luck);
		*/
		return (int)EquipCalculator.calculateWeaponPower(
				weapon.getAddAttack(), weapon.getAddDefend(),
				weapon.getAddAgility(), weapon.getAddLuck(), 
				weapon.getAddBlood(), weapon.getAddSkin());
	}
}
