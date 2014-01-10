package com.xinqihd.sns.gameserver.admin.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXList;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.WeaponOrItemListCellRenderer;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class WeaponBalanceSettingPanel extends MyPanel implements ActionListener, ChangeListener {
	
	private static final String COMMAND_SELECTED_WEAPON = "selectWeapon";
	private static final String COMMAND_RANDOM_WEAPON = "randomWeapon";
	private static final String COMMAND_SET_LEVEL = "setMinMaxLevel";
	private static final String COMMAND_USE_STRENGTH = "useStrength";
	
	public static final String[] COMPOSE_STONES = {
		"1级合成",
		"1级合成+15%",
		"1级合成+25%",
		"2级合成",
		"2级合成+15%",
		"2级合成+25%",
		"3级合成",
		"3级合成+15%",
		"3级合成+25%",
		"4级合成",
		"4级合成+15%",
		"4级合成+25%",
		"5级合成",
		"5级合成+15%",
		"5级合成+25%",
	};
	
	private String title = null;
	private JLabel minLabel = new JLabel("最小等级");
	private JLabel maxLabel = new JLabel("最大等级");
	private JSpinner minField = new JSpinner();
	private JSpinner maxField = new JSpinner();
	
	private JRadioButton selectWeapon = new JRadioButton("指定武器");
	private JRadioButton randomWeapon = new JRadioButton("随机武器");
	private ButtonGroup  weaponBtnGroup = new ButtonGroup();
	private JComboBox    selectWeaponField = new JComboBox();
	
//	private JLabel otherEquipLabel = new JLabel("挑选其他装备");
//	private JXList otherEquipField = new JXList();
	private JCheckBox otherEquipEnableField = new JCheckBox("激活其他装备");
	
	private JCheckBox strengthWeapon = new JCheckBox("启用强化(1-12级)");
	private JLabel strengthWeaponLbl = new JLabel("->");
	private JSpinner strengthMinField = new JSpinner();
	private JSpinner strengthMaxField = new JSpinner();
	
	private JLabel composeWeaponLbl = new JLabel("启用合成功能");
	private DefaultComboBoxModel composeStoneModel = new DefaultComboBoxModel(COMPOSE_STONES);
	private JComboBox composeStoneField = new JComboBox(composeStoneModel);
	private JCheckBox attackComposeField = new JCheckBox("火(攻击)");
	private JCheckBox defendComposeField = new JCheckBox("土(防御)");
	private JCheckBox agilityComposeField = new JCheckBox("风(敏捷)");
	private JCheckBox luckComposeField = new JCheckBox("水(幸运)");
	
	private DefaultComboBoxModel weaponListModel = new DefaultComboBoxModel();
	
	public WeaponBalanceSettingPanel(String title) {
		this.title = title;
		init();
	}
	
	public void init() {
		this.selectWeapon.addActionListener(this);
		this.selectWeapon.setActionCommand(COMMAND_SELECTED_WEAPON);
		this.randomWeapon.addActionListener(this);
		this.randomWeapon.setSelected(true);
		this.randomWeapon.setActionCommand(COMMAND_RANDOM_WEAPON);
		this.selectWeaponField.setEnabled(false);
		this.selectWeaponField.setRenderer(new WeaponOrItemListCellRenderer());
		this.refreshWeaponList();
		this.selectWeaponField.setModel(this.weaponListModel);
		this.minField.addChangeListener(this);
		this.maxField.addChangeListener(this);
		this.strengthMinField.setValue(1);
		this.strengthMaxField.setValue(12);
		this.strengthMinField.setEnabled(false);
		this.strengthMaxField.setEnabled(false);
		this.strengthWeapon.addActionListener(this);
		this.strengthWeapon.setActionCommand(COMMAND_USE_STRENGTH);
		
		this.setBorder(BorderFactory.createTitledBorder(title));
		this.setLayout(new MigLayout("wrap 4", "[25%][25%][25%][25%]"));
		
		this.minField.setValue(1);
		this.maxField.setValue(100);
		
		this.weaponBtnGroup.add(selectWeapon);
		this.weaponBtnGroup.add(randomWeapon);
		
		this.add(minLabel, "");
		this.add(minField, "growx");
		this.add(maxLabel, "");
		this.add(maxField, "growx");
				
		this.add(selectWeapon, "span 2");
		this.add(randomWeapon, "span 2");
		this.add(selectWeaponField, "span 4, growx, ");
		
//		this.add(otherEquipLabel, "span 4");
//		this.add(otherEquipField, "span 4, grow");
		
		this.add(strengthWeapon, "");
		this.add(strengthMinField, "growx");
		this.add(strengthWeaponLbl, "");
		this.add(strengthMaxField, "growx");
		
		this.add(composeWeaponLbl, "span 2");
		this.add(composeStoneField, "span");
		this.add(attackComposeField, "span, split 4");
		this.add(defendComposeField, "");
		this.add(agilityComposeField, "");
		this.add(luckComposeField, "");
		
		this.add(otherEquipEnableField, "newline");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( COMMAND_RANDOM_WEAPON.equals(e.getActionCommand()) ) {
			this.selectWeaponField.setEnabled(false);
		} else if ( COMMAND_SELECTED_WEAPON.equals(e.getActionCommand()) ) {
			this.selectWeaponField.setEnabled(true);
		} else if ( COMMAND_USE_STRENGTH.equals(e.getActionCommand()) ) {
			if ( this.strengthWeapon.isSelected() ) {
				this.strengthMinField.setEnabled(true);
				this.strengthMaxField.setEnabled(true);
			} else {
				this.strengthMinField.setEnabled(false);
				this.strengthMaxField.setEnabled(false);
			}
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if ( e.getSource() == minField || e.getSource() == maxField ) {
			this.refreshWeaponList();
		}
	}
	
	/**
	 * 获取配置对象
	 * @return
	 */
	public WeaponBalanceTestConfig getWeaponBalanceTestConfig() {
		WeaponBalanceTestConfig config = new WeaponBalanceTestConfig();
		config.setMinUserLevel(StringUtil.toInt(minField.getValue().toString(), 1));
		config.setMaxUserLevel(StringUtil.toInt(maxField.getValue().toString(), 100));
		config.setUseRandomWeapon(this.randomWeapon.isSelected());
		if ( this.selectWeapon.isSelected() ) {
			config.setSelectedWeapon((WeaponPojo)this.selectWeaponField.getSelectedItem());
		}
		config.setUseStrength(this.strengthWeapon.isSelected());
		config.setUseOtherEquips(this.otherEquipEnableField.isSelected());
		config.setStrengthMin((Integer)this.strengthMinField.getValue());
		config.setStrengthMax((Integer)this.strengthMaxField.getValue());
		config.setAttackCompose(this.attackComposeField.isSelected());
		config.setDefendCompose(this.defendComposeField.isSelected());
		config.setAgilityCompose(this.agilityComposeField.isSelected());
		config.setLuckCompose(this.luckComposeField.isSelected());
		
		config.setComposeStone(this.composeStoneField.getSelectedItem().toString());
		return config;
	}
	
	private void refreshWeaponList() {
		this.weaponListModel.removeAllElements();
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		ArrayList<WeaponPojo> weaponList = new ArrayList<WeaponPojo>(weapons);
		Collections.sort(weaponList);
		for ( WeaponPojo  weapon : weaponList ) {
			if ( weapon.getSlot() == EquipType.WEAPON ) {
				int minLevel = (Integer)minField.getValue();
				int maxLevel = (Integer)maxField.getValue();
				if ( weapon.getUserLevel() <= maxLevel) {
					this.weaponListModel.addElement(weapon);
				}
			}
		}
	}
	
}
