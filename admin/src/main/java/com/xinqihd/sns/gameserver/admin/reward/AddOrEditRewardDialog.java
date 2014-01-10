package com.xinqihd.sns.gameserver.admin.reward;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.AddRemoveComboList;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.config.RewardPojo;
import com.xinqihd.sns.gameserver.config.RewardPojoType;
import com.xinqihd.sns.gameserver.reward.Reward;

public class AddOrEditRewardDialog extends MyDialog implements ActionListener, PropertyChangeListener {
	
	private static final String COMMAND_SCRIPT_SELECT = "scriptSelect";
	
	private RewardPojo rewardPojo = new RewardPojo();
	
	private JXLabel idLbl = new JXLabel("奖励ID:");
	private JXLabel nameLbl = new JXLabel("名称:");
	private JXLabel includeLbl = new JXLabel("包含项目:");
	private JXLabel startMillisLbl = new JXLabel("开始日期:");
	private JXLabel endMillisLbl = new JXLabel("结束日期:");
	private JXLabel happenRatioLbl = new JXLabel("概率(0-1000):");
	private JXLabel rewardLbl = new JXLabel("奖励:");
	
	private JXTextField idField = new JXTextField();
	private JXTextField nameField = new JXTextField();
	private AddRemoveComboList includeField = new AddRemoveComboList(RewardPojoType.values());
	private JXDatePicker startMillisField = new JXDatePicker(new Date());
	private JXDatePicker endMillisField = new JXDatePicker(new Date());
	private JSpinner happenRatioField = new JSpinner();
	
	private RewardPanel rewardPanel = new RewardPanel();
	
	private JXButton okButton = new JXButton("保存");
	private JXButton cancelButton = new JXButton("取消");
	
	private boolean createdNew = true;
	
	
	public AddOrEditRewardDialog() {
		init();
	}
	
	public AddOrEditRewardDialog(RewardPojo rewardPojo, boolean isNew) {
		if ( rewardPojo != null ) {
			if ( !isNew ) {
				this.createdNew = false;
			}
			this.rewardPojo.set_id(rewardPojo.get_id());
			this.rewardPojo.setName(rewardPojo.getName());
			this.rewardPojo.setRatio(rewardPojo.getRatio());
			this.rewardPojo.setStartMillis(rewardPojo.getStartMillis());
			this.rewardPojo.setEndMillis(rewardPojo.getEndMillis());
			this.rewardPojo.setIncludes(rewardPojo.getIncludes());
			this.rewardPojo.setReward(rewardPojo.getReward());
		}
		init();
	}
	
	public void init() {
		if (!createdNew) {
			this.idField.setEnabled(false);
		}
		this.idField.setColumns(20);
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		if ( this.rewardPojo != null ) {
			updateRewardPojo();
		}
		
		this.setSize(800, 600);
		this.setResizable(true);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		this.setModal(true);
		
		JXPanel pojoPanel = new JXPanel();
		pojoPanel.setBorder(BorderFactory.createTitledBorder("奖励配置"));
		pojoPanel.setLayout(new MigLayout("wrap 4",
				//column
				"[][]30[][]",
				"10"
				));
		pojoPanel.add(idLbl, "sg lbl");
		pojoPanel.add(idField, "sg fd, growx");
		pojoPanel.add(includeLbl, "span 2 1");
		pojoPanel.add(nameLbl, "sg lbl");
		pojoPanel.add(nameField, "sg fd");
		pojoPanel.add(includeField, "span 2 4");
		pojoPanel.add(happenRatioLbl, "sg lbl");
		pojoPanel.add(happenRatioField, "sg fd");
		pojoPanel.add(startMillisLbl, "sg lbl");
		pojoPanel.add(startMillisField, "sg fd");
		pojoPanel.add(endMillisLbl, "sg lbl");
		pojoPanel.add(endMillisField, "sg fd");
		
		this.setLayout(new MigLayout("wrap 1, ins 5px"));
		this.add(pojoPanel, "width 100%, height 50%");
		
		rewardPanel.setBorder(BorderFactory.createTitledBorder("奖励内容"));
		
		this.add(rewardPanel, "width 100%, height 50%");
	
		this.add(okButton, "split 2, align center");
		this.add(cancelButton, "");
	}
	
	public void updateRewardPojo() {
		this.idField.setText(rewardPojo.get_id());
		this.nameField.setText(rewardPojo.getName());
		this.happenRatioField.setValue(rewardPojo.getRatio());
		this.includeField.changeValues(rewardPojo.getIncludes());
		this.startMillisField.setDate(new Date(rewardPojo.getStartMillis()));
		this.endMillisField.setDate(new Date(rewardPojo.getEndMillis()));
		Reward reward = this.rewardPojo.getReward();
		if ( reward != null ) {
			this.rewardPanel.updateReward(reward);
		}
	}

	public RewardPojo getRewardPojo() {
		return this.rewardPojo;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			this.rewardPojo.set_id(this.idField.getText());
			this.rewardPojo.setName(this.nameField.getText());
			this.rewardPojo.setRatio((Integer)this.happenRatioField.getValue());
			this.rewardPojo.setStartMillis(this.startMillisField.getDate().getTime());
			this.rewardPojo.setEndMillis(this.endMillisField.getDate().getTime());
			List list = this.includeField.getListModel().getList();
			this.rewardPojo.getIncludes().clear();
			this.rewardPojo.setIncludes(list);
			Reward reward = this.rewardPanel.getReward();
			if ( reward != null ) {
				this.rewardPojo.setReward(reward);
			}
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.rewardPojo = null;
			this.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

}

