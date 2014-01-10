package com.xinqihd.sns.gameserver.admin.item;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.WeaponAndItemPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyMiniTablePanel;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardCondition;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class EditItemDialog extends MyDialog implements 
	ActionListener, ListSelectionListener {
	
	private static final String COMMMAND_BOXTYPE = "boxtype";
	private static final String COMMMAND_SCRIPT = "script";
	private static final String COMMMAND_P = "P";
	
	private ItemPojo pojo = null;
	
	private JLabel _idLbl    = new JLabel("道具ID");
//	private JLabel typeIdLbl = new JLabel("道具类型ID");
	private JLabel levelLbl  = new JLabel("道具等级");
	private JLabel iconLbl   = new JLabel("道具图标");
	private JLabel nameLbl   = new JLabel("道具名称");
	private JLabel infoLbl   = new JLabel("道具描述");
	//以下字段为宝箱类型适用
	private JLabel boxTypeLbl = new JLabel("是否为宝箱类型?");
	private JLabel scriptLbl = new JLabel("宝箱脚本");
	private JLabel qLbl = new JLabel("宝箱概率分布");
	
	private JXTextField _idTf    = new JXTextField("道具ID");
//	private JXTextField typeIdTf = new JXTextField("道具类型ID");
	private JXTextField levelTf  = new JXTextField("道具等级");
	private JXTextField iconTf   = new JXTextField("道具图标");
	private JXTextField nameTf   = new JXTextField("道具名称");
	private JXTextField infoTf   = new JXTextField("道具描述");
	//以下字段为宝箱类型适用
	private JCheckBox   boxTypeBtn = new JCheckBox();
	private JXTextField scriptTf = new JXTextField("宝箱脚本");
	private JXTextField qTf = new JXTextField("宝箱概率分布");
	private JCheckBox broadcastField = new JCheckBox("广播开启宝箱消息");
	private JCheckBox canbeRewardedField = new JCheckBox("开启抽奖");
	private MyMiniTablePanel rewardsTf = new MyMiniTablePanel();
	private MyMiniTablePanel conditionsTf = new MyMiniTablePanel();
	
	private JXButton okButton = new JXButton("保存修改");
	private JXButton cancelButton = new JXButton("取消修改");
	
	private WeaponAndItemPanel listPanel = null;
	
	private AddOrEditRewardAction addRewardAction = new AddOrEditRewardAction();
	private AddRewardConditionAction addRewardConditionAction = new AddRewardConditionAction();
	
	private ItemPojo savedPojo = null;
	private boolean editMode = false;

	public EditItemDialog(ItemPojo pojo) {
		this.pojo = pojo;
		if ( this.pojo == null ) {
			this.pojo = new ItemPojo();
			editMode = false;
		} else {
			editMode = true;
			updateStatus();
		}
		init();
	}
	
	public void init() {
		this.setSize(900, 600);
		this.setResizable(true);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		this.setModal(true);
		this.listPanel = new WeaponAndItemPanel(WeaponAndItemPanel.ENABLE_ITEM);
		this.listPanel.addListSelectionListener(this);
		if ( editMode ) {
			this._idTf.setEnabled(false);
			this.nameTf.setEnabled(false);
			this.listPanel.setEnabled(false);
		} else {
			this._idTf.setEnabled(true);
			this.nameTf.setEnabled(true);
			this.listPanel.setEnabled(true);
		}
		
		this.rewardsTf.setTitle("宝箱奖励");
		this.rewardsTf.setAddRowAction(addRewardAction);
		
		this.conditionsTf.setTitle("宝箱开启条件");
		this.conditionsTf.setAddRowAction(addRewardConditionAction);
		
		this.scriptTf.setActionCommand(COMMMAND_SCRIPT);
		this.scriptTf.addActionListener(this);
		this.qTf.setActionCommand(COMMMAND_P);
		this.qTf.addActionListener(this);
		
		this.boxTypeBtn.setActionCommand(COMMMAND_BOXTYPE);
		this.boxTypeBtn.addActionListener(this);
		
		this.okButton.setActionCommand(ActionName.OK.name());
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.okButton.addActionListener(this);
		this.cancelButton.addActionListener(this);
		
		this.setLayout(new MigLayout("wrap 4, ins 15px"));
		
		this.add(_idLbl, "sg lbl");
		this.add(_idTf, "sg fd, growx, pushx");
		this.add(nameLbl, "sg lbl");
		this.add(nameTf, "sg fd, growx, pushx");
		this.add(levelLbl, "sg lbl");
		this.add(levelTf, "sg fd, growx, pushx");
		this.add(iconLbl, "sg lbl");
		this.add(iconTf, "sg fd, growx, pushx, wrap");
		this.add(infoLbl, "sg lbl");
		this.add(infoTf, "span, growx, pushx");
		
		this.add(boxTypeLbl, "sg lbl");
		this.add(boxTypeBtn, "align left");
		this.add(broadcastField, "");
		this.add(canbeRewardedField, "span");
		
		this.add(scriptLbl, "sg lbl");
		this.add(scriptTf, "sg fd, growx, pushx");
		this.add(qLbl, "sg lbl");
		this.add(qTf, "sg fd, growx, pushx");
		this.add(rewardsTf, "span, grow, push");
		this.add(conditionsTf, "span, grow, push");
		
		this.add(okButton, "span, split 2, align center");
		this.add(cancelButton, "");
		
		this.add(listPanel, "dock east, width 30%, height 100%");
	}
	
	public void updateStatus() {		
		_idTf.setText(pojo.getId());
		levelTf.setText(String.valueOf(pojo.getLevel()));
		iconTf.setText(pojo.getIcon());
		nameTf.setText(pojo.getName());
		infoTf.setText(pojo.getInfo());
		qTf.setText(String.valueOf(pojo.getQ()));
		broadcastField.setSelected(pojo.isBroadcast());
		if ( boxTypeBtn.isSelected() || pojo.getScript().length()>0) {
			boxTypeBtn.setSelected(true);
			scriptTf.setEnabled(true);
			scriptTf.setText(pojo.getScript());
			rewardsTf.setEnabled(true);
			conditionsTf.setEnabled(true);
			broadcastField.setEnabled(true);
		} else {
			boxTypeBtn.setSelected(false);
			scriptTf.setText(pojo.getScript());
			scriptTf.setEnabled(false);
			rewardsTf.setEnabled(false);
			conditionsTf.setEnabled(false);
			broadcastField.setEnabled(false);
		}
		TBoxTableModel tboxModel = new TBoxTableModel(
				pojo.getRewards(), pojo.getScript(), pojo.getQ());
		addRewardAction.setTableModel(tboxModel);
		rewardsTf.setTableModel(tboxModel);
		RewardConditionTableModel condModel = new RewardConditionTableModel(pojo);
		addRewardConditionAction.setTableModel(condModel);
		conditionsTf.setTableModel(condModel);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if ( COMMMAND_BOXTYPE.equals(e.getActionCommand()) || 
				COMMMAND_P.equals(e.getActionCommand()) ) {
			if ( COMMMAND_P.equals(e.getActionCommand()) ) {
				try {
					pojo.setQ(Double.parseDouble(qTf.getText()));
				} catch (NumberFormatException e1) {
				}
			}
			if ( editMode ) {
				updateStatus();
			}
		} else if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			this.savedPojo = this.pojo.clone();
			savedPojo.setId(this._idTf.getText());
			savedPojo.setName(this.nameTf.getText());
			savedPojo.setInfo(this.infoTf.getText());
			savedPojo.setLevel(StringUtil.toInt(this.levelTf.getText(), pojo.getLevel()));
			savedPojo.setIcon(this.iconTf.getText());
			savedPojo.setScript(this.scriptTf.getText());
			savedPojo.setQ(Double.parseDouble(this.qTf.getText()));
			savedPojo.setBroadcast(this.broadcastField.isSelected());
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.savedPojo = null;
			this.dispose();
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
					this.pojo = itemPojo.clone();
					updateStatus();
				}
			}
		}
	}
	
	/**
	 * 返回保存的对象，如果这个对象为null，表示没有修改或者放弃了修改
	 * @return
	 */
	public ItemPojo getSavedItemPojo() {
		return savedPojo;
	}
}
