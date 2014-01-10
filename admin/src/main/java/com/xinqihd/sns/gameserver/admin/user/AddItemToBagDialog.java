package com.xinqihd.sns.gameserver.admin.user;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.WeaponAndItemPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 向玩家的背包中添加一项物品
 * 
 * @author wangqi
 *
 */
public class AddItemToBagDialog extends MyDialog implements ListSelectionListener, ActionListener {
	
	private static final String SLOT_CMD = "slot";
	
	private WeaponAndItemPanel selectPanel = new WeaponAndItemPanel();
	private DefaultListModel listModel = null;
	private PropData propData = null;
	
	private JLabel idLbl = new JLabel("道具ID");
	private JLabel nameLbl = new JLabel("道具名称");
	private JLabel propIndateLbl = new JLabel("总有效战斗次数");
	private JLabel propUsedTimeLbl = new JLabel("道具已使用次数");
	private JLabel durationLbl = new JLabel("剩余有效期");
	private JLabel countLbl = new JLabel("道具数量");
	private JLabel levelLbl = new JLabel("道具等级");
	private JLabel attackLevLbl = new JLabel("攻击合成等级");
	private JLabel defendLevLbl = new JLabel("防御合成等级");
	private JLabel agilityLevLbl = new JLabel("敏捷合成等级");
	private JLabel luckLevLbl = new JLabel("幸运合成等级");
	private JLabel attackBaseLbl = new JLabel("攻击基础等级");
	private JLabel defendBaseLbl = new JLabel("防御基础等级");
	private JLabel agilityBaseLbl = new JLabel("敏捷基础等级");
	private JLabel luckBaseLbl = new JLabel("幸运基础等级");
	private JLabel bloodLevLbl = new JLabel("血量合成等级");
	private JLabel bloodPercentLbl = new JLabel("血量加成");
	private JLabel thewLevLbl = new JLabel("体力合成等级");
	private JLabel damageLevLbl = new JLabel("伤害合成等级");
	private JLabel valuetypeLbl = new JLabel("购买来源");
	private JLabel bandedLbl = new JLabel("是否绑定");
	
	private JLabel colorLbl = new JLabel("道具颜色");
	private JLabel qualityLbl = new JLabel("道具品质");
	private JLabel strengthLvlLbl = new JLabel("强化等级");

	private JXTextField idBtn = new JXTextField("道具ID");
	private JXTextField nameBtn = new JXTextField("道具名称");
	private JXTextField propIndateBtn = new JXTextField("总有效战斗次数");
	private JXTextField propUsedTimeBtn = new JXTextField("道具已使用次数");
	private JXTextField durationBtn = new JXTextField("剩余有效期");
	private JXTextField countBtn = new JXTextField("道具数量");
	private JXTextField levelBtn = new JXTextField("道具等级");
	private JXTextField attackLevBtn = new JXTextField("攻击等级");
	private JXTextField defendLevBtn = new JXTextField("防御等级");
	private JXTextField agilityLevBtn = new JXTextField("敏捷等级");
	private JXTextField luckLevBtn = new JXTextField("幸运等级");
	private JXTextField attackBaseBtn = new JXTextField("攻击基础等级");
	private JXTextField defendBaseBtn = new JXTextField("防御基础等级");
	private JXTextField agilityBaseBtn = new JXTextField("敏捷基础等级");
	private JXTextField luckBaseBtn = new JXTextField("幸运基础等级");
	private JXTextField bloodLevBtn = new JXTextField("血量合成等级");
	private JXTextField bloodPercentBtn = new JXTextField("血量加成");
	private JXTextField thewLevBtn = new JXTextField("体力合成等级");
	private JXTextField damageLevBtn = new JXTextField("伤害合成等级");
	private JXComboBox valuetypeBtn = new JXComboBox(PropDataValueType.values());
	private JCheckBox bandedBtn = new JCheckBox("是否绑定");
	private JXLabel addTimeStampLbl = new JXLabel("加入日期");
	private JXLabel bandedUserNameLbl = new JXLabel("绑定玩家名");
	private JXTextField addTimeStampFd = new JXTextField();
	private JXTextField bandedUserNameFd = new JXTextField();
	
	//private JXTextField colorBtn = new JXTextField("道具颜色");
	private JComboBox colorBtn = new JComboBox(WeaponColor.values());
	private JXTextField qualityBtn = new JXTextField("道具品质");
	private JComboBox strengthLvField = new JComboBox(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
	
	private JTextPane enhanceField = new JTextPane();
	private JXLabel slotLbl = new JXLabel("插槽数");
	private JSpinner slotField = new JSpinner();
	private JButton slotButton = new JButton("修改插槽");
	
	private JButton okButton = new JButton("确定");
	private JButton cancelButton = new JButton("取消");
	
	public AddItemToBagDialog() {
		init();
	}
	
	public void init() {
		this.setSize(800, 600);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		
		selectPanel.addListSelectionListener(this);
		selectPanel.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		enhanceField.setEditorKit(new HTMLEditorKit());
		enhanceField.setEditable(false);
		enhanceField.setCaretPosition(0);
		
		JXPanel panel = new JXPanel();
		this.idBtn.setEnabled(false);
		this.nameBtn.setEnabled(false);
		this.durationBtn.setEnabled(false);
		
		panel.setLayout(new MigLayout("wrap 4, ins panel"));
		panel.add(idLbl, "sg label, push");
		panel.add(idBtn, "sg field, push");
		panel.add(nameLbl, "sg label, push");
		panel.add(nameBtn, "sg field, push");
		panel.add(propIndateLbl, "sg label, push");
		panel.add(propIndateBtn, "sg field, push");
		panel.add(propUsedTimeLbl, "sg label, push");
		panel.add(propUsedTimeBtn, "sg field, push");
		panel.add(durationLbl, "sg label, push");
		panel.add(durationBtn, "sg field, push");
		panel.add(countLbl, "sg label, push");
		panel.add(countBtn, "sg field, push");
		panel.add(levelLbl, "sg label, push");
		panel.add(levelBtn, "sg field, push");
		panel.add(attackLevLbl, "sg label, push");
		panel.add(attackLevBtn, "sg field, push");
		panel.add(defendLevLbl, "sg label, push");
		panel.add(defendLevBtn, "sg field, push");
		panel.add(agilityLevLbl, "sg label, push");
		panel.add(agilityLevBtn, "sg field, push");
		panel.add(luckLevLbl, "sg label, push");
		panel.add(luckLevBtn, "sg field, push");
		
		panel.add(attackBaseLbl, "sg label, push");
		panel.add(attackBaseBtn, "sg field, push");
		panel.add(defendBaseLbl, "sg label, push");
		panel.add(defendBaseBtn, "sg field, push");
		panel.add(agilityBaseLbl, "sg label, push");
		panel.add(agilityBaseBtn, "sg field, push");
		panel.add(luckBaseLbl, "sg label, push");
		panel.add(luckBaseBtn, "sg field, push");
		
		panel.add(bloodLevLbl, "sg label, push");
		panel.add(bloodLevBtn, "sg field, push");
		panel.add(bloodPercentLbl, "sg label, push");
		panel.add(bloodPercentBtn, "sg field, push");
		panel.add(thewLevLbl, "sg label, push");
		panel.add(thewLevBtn, "sg field, push");
		panel.add(damageLevLbl, "sg label, push");
		panel.add(damageLevBtn, "sg field, push");
		panel.add(valuetypeLbl, "sg label, push");
		panel.add(valuetypeBtn, "sg field, push");
		panel.add(bandedLbl, "sg label, push");
		panel.add(bandedBtn, "sg field, push");
		panel.add(colorLbl, "sg label, push");
		panel.add(colorBtn, "sg field, push");
		panel.add(qualityLbl, "sg label, push");
		panel.add(qualityBtn, "sg field, push");
		panel.add(strengthLvlLbl, "sg label, push");
		panel.add(strengthLvField, "sg field, push");
		panel.add(addTimeStampLbl, "sg label, push");
		panel.add(addTimeStampFd, "sg field, push");
		panel.add(bandedUserNameLbl, "sg label, push");
		panel.add(bandedUserNameFd, "sg field, push");
		panel.add(bandedLbl, "sg label, push");
		panel.add(bandedBtn, "sg field, push");
		panel.add(enhanceField, "newline, spanx 2, grow");
		panel.add(slotLbl, "spanx, split 3");
		panel.add(slotField, "sg field");
		panel.add(slotButton, "");
		
		this.slotButton.setActionCommand(SLOT_CMD);
		this.slotButton.addActionListener(this);
		
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		JXPanel btnPanel = new JXPanel();
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);
		
		this.setLayout(new MigLayout(""));
		this.add(panel, "dock center, width 60%");
		this.add(selectPanel, "dock east, width 40%, height 100%");
		this.add(btnPanel, "dock south, width 60%");
		
		this.setModal(true);
		
		updateStatus();
	}

	public void updateStatus() {
		if ( this.propData != null ) {
			this.idBtn.setText(propData.getItemId());
			this.nameBtn.setText(propData.getName());
			this.propIndateBtn.setText(String.valueOf(propData.getPropIndate()));
			this.propUsedTimeBtn.setText(String.valueOf(propData.getPropUsedTime()));
			this.durationBtn.setText(String.valueOf(propData.getDuration()));
			this.countBtn.setText(String.valueOf(propData.getCount()));
			this.levelBtn.setText(String.valueOf(propData.getLevel()));
			this.attackLevBtn.setText(String.valueOf(propData.getAttackLev()));
			this.defendLevBtn.setText(String.valueOf(propData.getDefendLev()));
			this.agilityLevBtn.setText(String.valueOf(propData.getAgilityLev()));
			this.luckLevBtn.setText(String.valueOf(propData.getLuckLev()));
			this.attackBaseBtn.setText(String.valueOf(propData.getBaseAttack()));
			this.defendBaseBtn.setText(String.valueOf(propData.getBaseDefend()));
			this.agilityBaseBtn.setText(String.valueOf(propData.getBaseAgility()));
			this.luckBaseBtn.setText(String.valueOf(propData.getBaseLuck()));
			this.bloodLevBtn.setText(String.valueOf(propData.getBloodLev()));
			this.bloodPercentBtn.setText(String.valueOf(propData.getBloodPercent()));
			this.thewLevBtn.setText(String.valueOf(propData.getThewLev()));
			this.damageLevBtn.setText(String.valueOf(propData.getDamageLev()));
			this.valuetypeBtn.setSelectedItem(propData.getValuetype());
			this.bandedBtn.setSelected( propData.isBanded() );
			this.colorBtn.setSelectedItem(propData.getWeaponColor());
			this.qualityBtn.setText("N/A");
			this.strengthLvField.setSelectedItem(propData.getLevel());
			this.enhanceField.setText(propData.getEnhanceMap().toString());
			this.bandedBtn.setSelected(propData.isBanded());
			this.bandedUserNameFd.setText(propData.getBandUserName());
			this.addTimeStampFd.setText(DateUtil.formatDateTime(new Date(propData.getAddTimestamp())));
			this.slotField.setValue(propData.getTotalSlot());
			this.strengthLvField.setSelectedItem(propData.getMaxLevel());
			this.repaint();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if ( !e.getValueIsAdjusting() ) {
			JList list = (JList)e.getSource();
			if ( this.selectPanel.isWeaponSelected() ) {
				WeaponPojo pojo = (WeaponPojo)list.getSelectedValue();
				if ( pojo != null ) {
					if ( this.propData != null ) {
						this.propData = pojo.toPropData(30, this.propData.getWeaponColor());
					} else {
						this.propData = pojo.toPropData(30, WeaponColor.WHITE);
					}
					updateStatus();
				}
			} else {
				ItemPojo pojo = (ItemPojo)list.getSelectedValue();
				if ( pojo != null ) {
					this.propData = pojo.toPropData();
					updateStatus();
				}
			}
			System.out.println(this.propData);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			int pew = propData.getPew();
			propData.setPropIndate(Integer.parseInt(this.propIndateBtn.getText()));
			propData.setCount(Integer.parseInt(this.countBtn.getText()));
			propData.setAttackLev(Integer.parseInt(this.attackLevBtn.getText()));
			propData.setDefendLev(Integer.parseInt(this.defendLevBtn.getText()));
			propData.setAgilityLev(Integer.parseInt(this.agilityLevBtn.getText()));
			propData.setLuckLev(Integer.parseInt(this.luckLevBtn.getText()));
			propData.setBaseAttack(Integer.parseInt(this.attackBaseBtn.getText()));
			propData.setBaseDefend(Integer.parseInt(this.defendBaseBtn.getText()));
			propData.setBaseAgility(Integer.parseInt(this.agilityBaseBtn.getText()));
			propData.setBaseLuck(Integer.parseInt(this.luckBaseBtn.getText()));
			propData.setBloodLev(Integer.parseInt(this.bloodLevBtn.getText()));
			propData.setBloodPercent(Integer.parseInt(this.bloodPercentBtn.getText()));
			propData.setThewLev(Integer.parseInt(this.thewLevBtn.getText()));
			propData.setDamageLev(Integer.parseInt(this.damageLevBtn.getText()));
			propData.setBanded(this.bandedBtn.isSelected());
			propData.setBandUserName(this.bandedUserNameFd.getText());
			WeaponColor weaponColor = WeaponColor.values()[colorBtn.getSelectedIndex()];
			if ( weaponColor != propData.getWeaponColor() ) {
				propData.setWeaponColor(weaponColor);
				propData = EquipManager.getInstance().getWeaponById(propData.getItemId()).
						toPropData(propData.getPropIndate(), weaponColor);
			}
			Integer levelObj = (Integer)this.strengthLvField.getSelectedItem();
			int maxLevel = 0;
			if ( levelObj != null ) {
				maxLevel = levelObj.intValue();
			}
			propData.setMaxLevel(maxLevel);
			int level = StringUtil.toInt(this.levelBtn.getText(), 5);
			if ( level != propData.getLevel() ) {
				EquipCalculator.weaponUpLevel(this.propData, level);
			}
			propData.setPropUsedTime(Integer.parseInt(this.propUsedTimeBtn.getText()));
			propData.setPew(pew);
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.propData = null;
			this.dispose();
		} else if ( SLOT_CMD.equals(e.getActionCommand()) ) {
			ManagePropDataSlotDialog dialog = new ManagePropDataSlotDialog(propData, (Integer)this.slotField.getValue());
			dialog.setVisible(true);
			//this.propData = dialog.getPropData();
		}
	}
	
	public PropData getPropData() {
		return this.propData;
	}
	
	public void setPropData(PropData propData) {
		this.propData = propData;
		this.updateStatus();
	}
}
