package com.xinqihd.sns.gameserver.admin.item;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.WeaponAndItemPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.reward.RewardCondition;
import com.xinqihd.sns.gameserver.reward.RewardType;

public class AddRewardConditionDialog extends MyDialog implements ActionListener, ListSelectionListener {
	
	private JLabel _idLbl    = new JLabel("道具ID");
	private JLabel nameLbl   = new JLabel("道具名称");
	private JLabel rewardTypeLbl   = new JLabel("奖励类型");
	private JLabel countLbl   = new JLabel("道具数量");
	private JLabel infoLbl   = new JLabel("道具描述");
	
	private JXTextField _idTf    = new JXTextField("道具ID");
	private JXTextField nameTf   = new JXTextField("道具名称");
	private JSpinner countTf   = new JSpinner();
	private JXTextArea infoTf   = new JXTextArea("道具描述");
	private JXComboBox  rewardTypeTf = new JXComboBox(RewardType.values());
	
	private JXButton okButton = new JXButton("保存修改");
	private JXButton cancelButton = new JXButton("取消修改");
	
	private WeaponAndItemPanel listPanel = null;
	
	private RewardCondition rewardCondition = null;

	public AddRewardConditionDialog() {
		init();
	}
	
	public void init() {
		this.setSize(500, 500);
		this.setResizable(false);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		this.setModal(true);
		this.listPanel = new WeaponAndItemPanel(WeaponAndItemPanel.ENABLE_ITEM);
		this.listPanel.addListSelectionListener(this);
		
		this._idTf.setEnabled(true);
		this.nameTf.setEnabled(true);
		this.countTf.setValue(1);
		this.infoTf.setEnabled(true);
		this.listPanel.setEnabled(true);
		
		this.okButton.setActionCommand(ActionName.OK.name());
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.okButton.addActionListener(this);
		this.cancelButton.addActionListener(this);
		
		this.setLayout(new MigLayout("wrap 2, ins 15px"));
		
		this.add(_idLbl, "sg lbl");
		this.add(_idTf, "sg fd, growx, pushx");
		this.add(nameLbl, "sg lbl");
		this.add(nameTf, "sg fd, growx, pushx");
		this.add(rewardTypeLbl, "sg lbl");
		this.add(rewardTypeTf, "sg fd, growx, pushx");
		this.add(countLbl, "sg lbl");
		this.add(countTf, "sg fd, growx, pushx");
		this.add(infoLbl, "span, sg lbl, wrap");
		JScrollPane pane = new JScrollPane(infoTf);
		this.add(pane, "span, grow, push");
		
		this.add(okButton, "span, split 2, align center");
		this.add(cancelButton, "");
		
		this.add(listPanel, "dock east, width 40%, height 100%");
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			this.rewardCondition = new RewardCondition();
			rewardCondition.setId(this._idTf.getText());
			rewardCondition.setRewardType((RewardType)this.rewardTypeTf.getSelectedItem());
			rewardCondition.setCount((Integer)this.countTf.getValue());
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.rewardCondition = null;
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
					this._idTf.setText(itemPojo.getId());
					this.nameTf.setText(itemPojo.getName());
					this.infoTf.setText(itemPojo.getInfo());
					this.rewardTypeTf.setSelectedItem(RewardType.ITEM);
				}
			}
		}
	}
	
	/**
	 * 返回保存的对象，如果这个对象为null，表示没有修改或者放弃了修改
	 * @return
	 */
	public RewardCondition getSavedRewardCondition() {
		return rewardCondition;
	}
}
