package com.xinqihd.sns.gameserver.admin.item;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.EscapeAction;
import com.xinqihd.sns.gameserver.admin.gui.WeaponAndItemPanel;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;

public class AddRewardDialog extends JDialog implements ActionListener, ListSelectionListener {
	
	private JXPanel panel = new JXPanel();
	private JXComboBox  typeList = new JXComboBox(RewardType.values());
	private JXLabel subjectLabel = new JXLabel("邮件主题");
	private JXLabel contentLabel = new JXLabel("邮件内容");
	private JXLabel attachGiftLabel = new JXLabel("添加邮件");
	private JXLabel typeLabel = new JXLabel("请选择奖励的类型");
	private JXLabel typeDescLabel = new JXLabel("您选择了'金币'奖励");
	private JXLabel idLabel = new JXLabel("道具ID");
	private JXLabel typeIdLabel = new JXLabel("武器类型ID");
	private JXLabel levelLabel = new JXLabel("等级");
	private JXLabel countLabel = new JXLabel("数量");
	private JXLabel indateLabel = new JXLabel("有效期(战斗次数)");
	private JXLabel colorLabel = new JXLabel("颜色");
	private JXLabel maxStrLabel = new JXLabel("最大强化");
	private JXLabel slotCountLabel = new JXLabel("插槽数");
	
	private JXTextField subjectField = new JXTextField();
	private JXTextField contentField = new JXTextField();
	private JCheckBox  attachGift = new JCheckBox();
	private JXTextField idList = new JXTextField();
	private JXTextField typeIdField = new JXTextField();
	private JSpinner   levelSpinner = new JSpinner();
	private JSpinner   countSpinner = new JSpinner();
	private JSpinner   indateSpinner = new JSpinner();
	private JXComboBox colorList = new JXComboBox(WeaponColor.values());
	private JSpinner   maxStrSpinner = new JSpinner();
	private JSpinner   slotCountSpinner = new JSpinner();
	
	private WeaponAndItemPanel weaponPanel = new WeaponAndItemPanel();
	
	private JXButton   okButton = new JXButton("确定");
	private JXButton   cancelButton = new JXButton("取消");
	
	private static final String COMMAND_TYPE = "type";
	private static final String COMMAND_OK = "ok";
	private static final String COMMAND_CANCEL = "cancel";
	private static final String COMMAND_ONLYEMAIL = "onlyemail"; 
	
	private static final String[] columns = {"_id", "name", "icon"};
	
	private Reward reward = null;
	private String title = null;
	private boolean userCancel = false;
	private String subject = null;
	private String content = null;

	public AddRewardDialog(String title) {
		this(title, null);
	}
	
	public AddRewardDialog(String title, Reward reward) {
		setResizable(false);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.title = title;
		this.reward = reward;
		init();
	}
	
	public void init() {
		this.setSize(650, 600);
		Point point = WindowUtils.getPointForCentering(this.getOwner());
		this.setLocation(point);
		this.setResizable(true);
		
		this.subjectField.setColumns(100);
		this.contentField.setColumns(100);
		this.idList.setEnabled(false);
		this.typeIdField.setEnabled(false);
		this.typeList.setActionCommand(COMMAND_TYPE);
		this.typeList.addActionListener(this);
		this.okButton.setActionCommand(COMMAND_OK);
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(COMMAND_CANCEL);
		this.cancelButton.addActionListener(this);
		this.countSpinner.setValue(1);
		this.attachGift.setSelected(true);
		this.attachGift.setActionCommand(COMMAND_ONLYEMAIL);
		this.attachGift.addActionListener(this);
		
		this.panel.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
		this.panel.getActionMap().put("escape", new EscapeAction());
		
		this.weaponPanel.setEnabled(true);
		this.weaponPanel.addListSelectionListener(this);
		
	  //layout
		panel.setLayout(new MigLayout("wrap 2, width 100%, gap 10px", "[25%][25%][25%][25%]"));
		if ( this.title != null ) {
			panel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), title));
		} else {
			panel.setBorder(BorderFactory.createEtchedBorder());
		}
		panel.add(subjectLabel, "");
		panel.add(subjectField, "span, growx");
		panel.add(contentLabel, "");
		panel.add(contentField, "span, growx");
		panel.add(attachGiftLabel, "");
		panel.add(attachGift, "");
		panel.add(typeLabel, "");
		panel.add(typeList);
		panel.add(typeDescLabel, "span 2");
		panel.add(idLabel);
		this.idList.setEnabled(false);
//		this.idList.setRenderer(new EquipAndItemRenderer());
		panel.add(idList, "span, grow");
		panel.add(typeIdLabel);
		panel.add(typeIdField, "grow");
		panel.add(levelLabel);
		this.levelSpinner.setEnabled(false);
		panel.add(levelSpinner, "grow");
		panel.add(countLabel);
		panel.add(countSpinner, "grow");
		panel.add(indateLabel);
		this.indateSpinner.setEnabled(false);
		panel.add(indateSpinner, "grow");
		panel.add(colorLabel, "");
		panel.add(colorList, "grow");
		panel.add(maxStrLabel, "");
		panel.add(maxStrSpinner, "grow");
		panel.add(slotCountLabel, "");
		panel.add(slotCountSpinner, "grow");
		
		panel.add(okButton, "newline, gaptop 40px, span, split 2, align center");
		panel.add(cancelButton);
		
		JXPanel contentPanel = new JXPanel();
		contentPanel.setLayout(new MigLayout("wrap 2, width 100%, height 100%"));
		contentPanel.add(panel, "width 60%, height 100%, grow");
		contentPanel.add(weaponPanel, "width 40%, height 100%, grow");
		
		getContentPane().add(contentPanel);
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand() == COMMAND_TYPE ) {
			updateStatus();
		} else if ( e.getActionCommand() == COMMAND_OK ) {
			userCancel = false;
			this.dispose();
		} else if ( e.getActionCommand() == COMMAND_CANCEL ) {
			userCancel = true;
			this.dispose();
		} else if ( e.getActionCommand() == COMMAND_ONLYEMAIL ) {
			if ( !this.attachGift.isSelected() ) {
				this.levelSpinner.setEnabled(false);
				this.indateSpinner.setEnabled(false);
				this.idList.setEnabled(false);
				this.typeIdField.setEnabled(false);
				this.countSpinner.setEnabled(false);
				this.indateSpinner.setEnabled(false);
				this.maxStrSpinner.setEnabled(false);
				this.slotCountSpinner.setEnabled(false);
				this.weaponPanel.setEnabled(false);			
			} else {
				updateStatus();
			}
		}
	}

	/**
	 * 
	 */
	private void updateStatus() {
		RewardType type = (RewardType)this.typeList.getSelectedItem();
		switch ( type ) {
			case EXP:
			case GOLDEN:
			case MEDAL:
			case VOUCHER:
			case YUANBAO:
				switch ( type ) {
					case EXP:
						this.typeDescLabel.setText("您选择了'经验'奖励");
						break;
					case GOLDEN:
						this.typeDescLabel.setText("您选择了'金币'奖励");
						break;
					case MEDAL:
						this.typeDescLabel.setText("您选择了'勋章'奖励");
						break;
					case VOUCHER:
						this.typeDescLabel.setText("您选择了'礼券'奖励");
						break;
					case YUANBAO:
						this.typeDescLabel.setText("您选择了'元宝'奖励");
						break;
				}
				this.idList.setEnabled(false);
				this.typeIdField.setEnabled(false);
				this.levelSpinner.setEnabled(false);
				this.indateSpinner.setEnabled(false);
				this.countSpinner.setEnabled(true);
				this.weaponPanel.setEnabled(false);
				this.colorList.setEnabled(false);
				this.maxStrSpinner.setEnabled(false);
				this.slotCountSpinner.setEnabled(false);
				break;
			case ITEM:
				this.typeDescLabel.setText("您选择了'道具'奖励");
				this.levelSpinner.setEnabled(false);
				this.indateSpinner.setEnabled(false);
				this.idList.setEnabled(true);
				this.typeIdField.setEnabled(false);
				this.countSpinner.setEnabled(true);
				this.weaponPanel.setEnabled(true);
				this.colorList.setEnabled(false);
				this.maxStrSpinner.setEnabled(false);
				this.slotCountSpinner.setEnabled(false);
				break;
			case WEAPON:
				this.typeDescLabel.setText("您选择了'武器'奖励");
				this.levelSpinner.setEnabled(true);
				this.indateSpinner.setEnabled(true);
				this.idList.setEnabled(false);
				this.typeIdField.setEnabled(true);
				this.countSpinner.setEnabled(true);
				this.indateSpinner.setValue(10);
				this.weaponPanel.setEnabled(true);
				this.colorList.setEnabled(true);
				this.maxStrSpinner.setEnabled(true);
				this.slotCountSpinner.setEnabled(true);
				break;
			case UNKNOWN:
				this.typeDescLabel.setText("您选择的类型暂不支持");
				this.idList.setEnabled(false);
				this.typeIdField.setEnabled(false);
				this.levelSpinner.setEnabled(false);
				this.indateSpinner.setEnabled(false);
				this.countSpinner.setEnabled(false);
				this.weaponPanel.setEnabled(false);
				this.colorList.setEnabled(false);
				this.maxStrSpinner.setEnabled(false);
				this.slotCountSpinner.setEnabled(false);
				break;
		}
	}
	
	
	
	/**
	 * Monitor the WeaponAndItemPanel's value change
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if ( !e.getValueIsAdjusting() ) {
			Object value = weaponPanel.getSelectedValue();
			RewardType type = null;
			if ( value instanceof WeaponPojo ) {
				type = RewardType.WEAPON;
				WeaponPojo weapon = (WeaponPojo)value;
				this.idList.setText(weapon.getId());
				this.typeIdField.setText(weapon.getTypeName());
				this.typeList.setSelectedItem(RewardType.WEAPON);
				this.typeList.setSelectedItem(type);
				this.maxStrSpinner.setValue(10);
				this.slotCountSpinner.setValue(0);
			} else if ( value instanceof ItemPojo ) {
				type = RewardType.ITEM;
				ItemPojo item = (ItemPojo)value;
				this.idList.setText(item.getId());
				this.typeIdField.setText(item.getTypeId());
				this.typeList.setSelectedItem(type);
			}
			updateStatus();
		}
	}
	
	/**
	 * Get the final reward object
	 * @return
	 */
	public Reward getReward() {
		if ( !this.attachGift.isSelected() ) {
			return null;
		}
		if ( userCancel ) {
			return null;
		}
		RewardType type = (RewardType)this.typeList.getSelectedItem();
		
		Reward reward = new Reward();
		reward.setId("0");
		reward.setTypeId("");
		reward.setLevel(0);
		reward.setPropIndate(0);
		reward.setPropColor(WeaponColor.WHITE);
		reward.setType(type);
		reward.setPropCount((Integer)this.countSpinner.getValue());
		
		switch ( type ) {
			case EXP:
				break;
			case GOLDEN:
				reward.setId("-1");
				break;
			case MEDAL:
				reward.setId("-4");
				break;
			case VOUCHER:
				reward.setId("-2");
				break;
			case YUANBAO:
				reward.setId("-3");
				break;
			case ITEM:
			case STONE:
				reward.setId(this.idList.getText());
				reward.setTypeId(this.typeIdField.getText());
				break;
			case WEAPON:
				reward.setId(this.idList.getText());
				reward.setTypeId(this.typeIdField.getText());
				reward.setLevel((Integer)this.levelSpinner.getValue());
				reward.setPropIndate((Integer)this.indateSpinner.getValue());
				reward.setPropColor(((WeaponColor)this.colorList.getSelectedItem()));
				reward.setMaxStrength((Integer)this.maxStrSpinner.getValue());
				reward.setSlot((Integer)this.slotCountSpinner.getValue());
				break;
			case ACHIVEMENT:
				//TODO
				break;
			case UNKNOWN:
				return null;
		}
		return reward;
	}

	public Object getNewObject() {
		Reward reward = getReward();
		if ( reward != null ) {
			MapDBObject obj = new MapDBObject();
			obj.putAll(reward);
			return obj;
		}
		return null;
	}
	
	public String getSubject() {
		this.subject = this.subjectField.getText().trim();
		return this.subject;
	}
	
	public String getContent() {
		this.content = this.contentField.getText().trim();
		return this.content;
	}
}
