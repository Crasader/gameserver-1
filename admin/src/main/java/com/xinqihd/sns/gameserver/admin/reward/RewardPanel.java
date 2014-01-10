package com.xinqihd.sns.gameserver.admin.reward;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.item.AddRewardDialog;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;

public class RewardPanel extends JXPanel implements ActionListener {
	
	private Reward reward = null;
	private JXLabel typeLbl = new JXLabel("类型:");
	private JXLabel idLbl = new JXLabel("ID:");
	private JXLabel nameLbl = new JXLabel("名称:");
	private JXLabel typeIdLbl = new JXLabel("等级ID");
	private JXLabel levelLbl = new JXLabel("装备等级");
	private JXLabel countLbl = new JXLabel("数量");
	private JXLabel indateLbl = new JXLabel("有效次数");
	private JXLabel colorLbl = new JXLabel("颜色");
	private JXLabel attackLbl = new JXLabel("攻击");
	private JXLabel defendLbl = new JXLabel("防御");
	private JXLabel agilityLbl = new JXLabel("敏捷");
	private JXLabel luckyLbl = new JXLabel("幸运");
	
	private JComboBox typeField = new JComboBox(RewardType.values());
	private JXTextField idField = new JXTextField();
	private JXTextField nameField = new JXTextField();
	private JXTextField typeIdField = new JXTextField();
	private JSpinner levelField = new JSpinner();
	private JSpinner countField = new JSpinner();
	private JSpinner indateField = new JSpinner();
	private JComboBox colorField = new JComboBox(WeaponColor.values());
	private JSpinner attackField = new JSpinner();
	private JSpinner defendField = new JSpinner();
	private JSpinner agilityField = new JSpinner();
	private JSpinner luckyField = new JSpinner();
	
	private JXButton modButton = new JXButton("奖品");

	public RewardPanel() {
		init();
	}
	
	public RewardPanel(Reward reward) {
		this.reward = reward;
		init();
	}
	
	public void init() {
		this.setLayout(new MigLayout("wrap 4", "[20%][25%]10%[20%][25%]"));
		this.add(typeIdLbl, "sg lbl");
		this.add(typeIdField, "sg fd, growx");
		this.add(nameLbl, "sg lbl");
		this.add(nameField, "sg fd, growx");
		this.add(idLbl, "sg lbl");
		this.add(idField, "sg fd, growx");
		this.add(typeIdLbl, "sg lbl");
		this.add(typeIdField, "sg fd, growx");
		this.add(levelLbl, "sg lbl");
		this.add(levelField, "sg fd, growx");
		this.add(countLbl, "sg lbl");
		this.add(countField, "sg fd, growx");
		this.add(indateLbl, "sg lbl");
		this.add(indateField, "sg fd, growx");
		this.add(colorLbl, "sg lbl");
		this.add(colorField, "sg fd, growx");
		this.add(attackLbl, "sg lbl");
		this.add(attackField, "sg fd, growx");
		
		this.add(defendLbl, "sg lbl");
		this.add(defendField, "sg fd, growx");
		this.add(agilityLbl, "sg lbl");
		this.add(agilityField, "sg fd, growx");
		this.add(luckyLbl, "sg lbl");
		this.add(luckyField, "sg fd, growx");
		
		this.modButton.addActionListener(this);
		this.modButton.setActionCommand(ActionName.OK.toString());
		
		this.add(modButton, "span,center");
	}
	
	public void updateReward(Reward reward) {
		if ( reward != null ) {
			this.reward = reward;
			this.typeField.setSelectedItem(reward.getType());
			String name = null;
			if ( reward.getType() == RewardType.WEAPON ) {
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(reward.getId());
				if ( weapon != null ) {
					name = weapon.getName();
					if ( !"-1".equals(reward.getTypeId()) ) {
						name = name.substring(3);
					}
				}
			} else if (reward.getType() == RewardType.ITEM || reward.getType()==RewardType.STONE ) {
				ItemPojo item = ItemManager.getInstance().getItemById(reward.getId());
				if ( item != null ) {
					name = item.getName();
				}
			} else if (reward.getType() == RewardType.GOLDEN ) {
				name = "金币";
			} else if (reward.getType() == RewardType.EXP ) {
				name = "经验";
			} else if (reward.getType() == RewardType.YUANBAO ) {
				name = "元宝";
			}
			this.nameField.setText(name);
			this.idField.setText(this.reward.getId());
			this.typeIdField.setText(this.reward.getTypeId());
			this.levelField.setValue(this.reward.getLevel());
			this.countField.setValue(this.reward.getPropCount());
			this.indateField.setValue(this.reward.getPropIndate());
			this.colorField.setSelectedItem(this.reward.getPropColor());
			this.attackField.setValue(this.reward.getAddAttack());
			this.defendField.setValue(this.reward.getAddDefend());
			this.agilityField.setValue(this.reward.getAddAgility());
			this.luckyField.setValue(this.reward.getAddLucky());
		} else {
			this.typeField.setSelectedIndex(0);
			this.idField.setText("");
			this.nameField.setText("");
			this.typeIdField.setText("");
			this.levelField.setValue(0);
			this.countField.setValue(1);
			this.indateField.setValue(0);
			this.colorField.setSelectedItem(WeaponColor.WHITE);
			this.attackField.setValue(0);
			this.defendField.setValue(0);
			this.agilityField.setValue(0);
			this.luckyField.setValue(0);
		}
	}

	/**
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.toString().equals(e.getActionCommand()) ) {
			AddRewardDialog dialog = new AddRewardDialog("赠送道具");
			dialog.setModal(true);
			dialog.setVisible(true);
			this.reward = dialog.getReward();
			updateReward(this.reward);
		}
	}
	
	public Reward getReward() {
		return this.reward;
	}
}
