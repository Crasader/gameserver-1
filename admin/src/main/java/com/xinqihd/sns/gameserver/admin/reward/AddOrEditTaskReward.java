package com.xinqihd.sns.gameserver.admin.reward;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.WeaponAndItemPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.TaskPojo.Award;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;

public class AddOrEditTaskReward extends MyDialog implements 
		ActionListener, ListSelectionListener {
	
	private static final String COMMAND_SELECT_TYPE = "selectType";
	
	private Award award = new Award();
	private boolean createdNew = false;
	
	private JXLabel typeLbl = new JXLabel("类型:");
	private JXLabel idLbl = new JXLabel("道具或装备ID:");
	private JXLabel typeIdLbl = new JXLabel("装备类型ID");
	private JXLabel levelLbl = new JXLabel("装备等级");
	private JXLabel sexLbl = new JXLabel("性别");
	private JXLabel countLbl = new JXLabel("数量");
	private JXLabel indateLbl = new JXLabel("有效次数");
	private JXLabel colorLbl = new JXLabel("颜色");
	private JXLabel resourceLbl = new JXLabel("资源");
	
	private JComboBox typeField = new JComboBox();
	private JXTextField idField = new JXTextField();
	private JXTextField typeIdField = new JXTextField();
	private JSpinner levelField = new JSpinner();
	private JComboBox sexField = new JComboBox();
	private JSpinner countField = new JSpinner();
	private JSpinner indateField = new JSpinner();
	private JComboBox colorField = new JComboBox();
	private JXTextField resourceField = new JXTextField();
	
	private WeaponAndItemPanel listPanel = null;
	
	private JXButton okButton = new JXButton("保存");
	private JXButton cancelButton = new JXButton("取消");
	
	private DefaultComboBoxModel sexModel = 
			new DefaultComboBoxModel(Gender.values());
	private DefaultComboBoxModel typeModel = 
			new DefaultComboBoxModel(new String[]{
					"道具",
					"装备",
					"成就"
			});
	private DefaultComboBoxModel colorModel = 
			new DefaultComboBoxModel(WeaponColor.values());

	public AddOrEditTaskReward() {
		this(null);
	}
	
	public AddOrEditTaskReward(Award award) {
		if ( award != null ) {
			this.award.color = award.color;
			this.award.count = award.count;
			this.award.id = award.id;
			this.award.indate = award.indate;
			this.award.lv = award.lv;
			this.award.sex = award.sex;
			this.award.type = award.type;
			this.award.typeId = award.typeId;
			this.createdNew = false;
		}
		init();
	}
	
	public void init() {
		this.sexField.setModel(sexModel);
		this.typeField.setModel(typeModel);
		this.colorField.setModel(colorModel);
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		this.typeField.setActionCommand(COMMAND_SELECT_TYPE);
		this.typeField.addActionListener(this);
		this.listPanel = new WeaponAndItemPanel(WeaponAndItemPanel.ENABLE_ITEM);
		this.listPanel.addListSelectionListener(this);
		this.updateAward();
		
		this.setSize(600, 400);
		this.setResizable(true);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		this.setModal(true);
		
		this.setLayout(new MigLayout("wrap 2, ins 10px", "[10%][40%]"));

		this.add(typeLbl, "sg lbl");
		this.add(typeField, "sg fd, grow");
		this.add(idLbl, "sg lbl");
		this.add(idField, "sg fd, grow");
		this.add(typeIdLbl, "sg lbl");
		this.add(typeIdField, "sg fd, grow");
		this.add(levelLbl, "sg lbl");
		this.add(levelField, "sg fd, grow");
		this.add(sexLbl, "sg lbl");
		this.add(sexField, "sg fd, grow");
		this.add(countLbl, "sg lbl");
		this.add(countField, "sg fd, grow");
		this.add(indateLbl, "sg lbl");
		this.add(indateField, "sg fd, grow");
		this.add(colorLbl, "sg lbl");
		this.add(colorField, "sg fd, grow");
		this.add(resourceLbl, "sg lbl");
		this.add(resourceField, "span, grow");
		
		this.add(okButton, "split 2, align center");
		this.add(cancelButton, "");
		this.add(listPanel, "dock east, width 35%");
	}
	
	public void updateAward() {
		this.idField.setText(award.id);
		if ( Constant.ITEM.equals(award.type) ) {
			this.typeField.setSelectedIndex(0);
		} else if ( Constant.WEAPON.equals(award.type) ) {
			this.typeField.setSelectedIndex(1);
		} else if ( Constant.ACHIEVEMENT.equals(award.type) ) {
			this.typeField.setSelectedIndex(2);
		}
		this.typeIdField.setText(String.valueOf(award.typeId));
		this.levelField.setValue(award.lv);
		this.sexField.setSelectedItem(award.sex);
		this.countField.setValue(award.count);
		this.indateField.setValue(award.indate);
		this.colorField.setSelectedItem(award.color);
	}
	
	public Award getTaskAward() {
		return this.award;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		//"道具", "装备", "成就"
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			this.award.id = this.idField.getText();
			this.award.color = (WeaponColor)this.colorField.getSelectedItem();
			this.award.count = (Integer)this.countField.getValue();
			this.award.indate = (Integer)this.indateField.getValue();
			this.award.lv = (Integer)this.levelField.getValue();
			this.award.sex = (Gender)this.sexField.getSelectedItem();
			this.award.typeId = Integer.parseInt(this.typeIdField.getText());
			int index = this.typeField.getSelectedIndex();
			switch ( index ) {
				case 0:
					this.award.type = Constant.ITEM;
					break;
				case 1:
					this.award.type = Constant.WEAPON;
					break;
				case 2:
					this.award.type = Constant.ACHIEVEMENT;
					this.award.id = Constant.ONE_NEGATIVE;
					break;
			}
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.award = null;
			this.dispose();
		} else if ( COMMAND_SELECT_TYPE.equals(e.getActionCommand()) ) {
			int index = this.typeField.getSelectedIndex();
			switch ( index ) {
				case 0:
					 this.idField.setEnabled(true);
					 this.typeIdField.setEnabled(true);
					 this.levelField.setEnabled(true);
					 this.sexField.setEnabled(true);
					 this.countField.setEnabled(true);
					 this.indateField.setEnabled(true);
					 this.colorField.setEnabled(true);
					 this.resourceField.setEnabled(false);
					break;
				case 1:
					 this.idField.setEnabled(true);
					 this.typeIdField.setEnabled(true);
					 this.levelField.setEnabled(true);
					 this.sexField.setEnabled(true);
					 this.countField.setEnabled(true);
					 this.indateField.setEnabled(true);
					 this.colorField.setEnabled(true);
					 this.resourceField.setEnabled(false);
					break;
				case 2:
					 this.idField.setEnabled(false);
					 this.typeIdField.setEnabled(false);
					 this.levelField.setEnabled(false);
					 this.sexField.setEnabled(false);
					 this.countField.setEnabled(true);
					 this.indateField.setEnabled(false);
					 this.colorField.setEnabled(false);
					 this.resourceField.setEnabled(true);
					break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if ( !e.getValueIsAdjusting() ) {
			Object obj = listPanel.getSelectedValue();
			if ( obj instanceof ItemPojo ) {
				ItemPojo itemPojo = (ItemPojo)obj;
				if ( itemPojo != null ) {
					this.typeField.setSelectedIndex(0);
					this.idField.setText(itemPojo.getId());
					this.typeIdField.setText(itemPojo.getTypeId());
					this.sexField.setSelectedItem(Gender.ALL);
				}
			} else if ( obj instanceof WeaponPojo ) {
				WeaponPojo itemPojo = (WeaponPojo)obj;
				if ( itemPojo != null ) {
					this.typeField.setSelectedIndex(1);
					this.idField.setText(Constant.ONE_NEGATIVE);
					this.typeIdField.setText(itemPojo.getTypeName());
					this.sexField.setSelectedItem(itemPojo.getSex());
					int indate = GameDataManager.getInstance().
							getGameDataAsInt(GameDataKey.WEAPON_INDATE_SIMPLE, 30);
					this.indateField.setValue(indate);
				}
			}
		}		
	}
	
}
