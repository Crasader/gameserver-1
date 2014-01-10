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
import com.xinqihd.sns.gameserver.config.CDKeyPojo;
import com.xinqihd.sns.gameserver.config.RewardPojo;
import com.xinqihd.sns.gameserver.config.RewardPojoType;
import com.xinqihd.sns.gameserver.reward.Reward;

public class AddOrEditCDKeyDialog extends MyDialog implements ActionListener, PropertyChangeListener {
	
	private static final String COMMAND_SCRIPT_SELECT = "scriptSelect";
	private static final String COMMAND_RESET_DATE = "resetDate";
	
	private CDKeyPojo cdkeyPojo = new CDKeyPojo();
	
	private JXLabel idLbl = new JXLabel("ID:");
	private JXLabel startMillisLbl = new JXLabel("开始日期:");
	private JXLabel endMillisLbl = new JXLabel("结束日期:");
	private JXLabel channelLbl = new JXLabel("渠道:");
	private JXLabel rewardLbl = new JXLabel("奖励:");
	
	private JXTextField idField = new JXTextField();
	private JXDatePicker startMillisField = new JXDatePicker(new Date());
	private JXDatePicker endMillisField = new JXDatePicker(new Date());
	private JXTextField channelField = new JXTextField();
	
	private RewardPanel rewardPanel = new RewardPanel();
	
	private JXButton okButton = new JXButton("保存");
	private JXButton cancelButton = new JXButton("取消");
	private JXButton resetDateButton = new JXButton("重置日期");
	
	private boolean createdNew = true;
	
	
	public AddOrEditCDKeyDialog() {
		init();
	}
	
	public AddOrEditCDKeyDialog(CDKeyPojo cdkeyPojo, boolean isNew) {
		if ( cdkeyPojo != null ) {
			if ( !isNew ) {
				this.createdNew = false;
			}
			this.cdkeyPojo.setId(cdkeyPojo.getId());
			this.cdkeyPojo.setChannel(cdkeyPojo.getChannel());
			this.cdkeyPojo.setStartMillis(cdkeyPojo.getStartMillis());
			this.cdkeyPojo.setEndMillis(cdkeyPojo.getEndMillis());
			this.cdkeyPojo.setReward(cdkeyPojo.getReward());
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
		if ( this.cdkeyPojo != null ) {
			updateRewardPojo();
		}
		this.resetDateButton.setActionCommand(COMMAND_RESET_DATE);
		this.resetDateButton.addActionListener(this);
		
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
		pojoPanel.add(channelLbl, "sg lbl");
		pojoPanel.add(channelField, "sg fd");
		pojoPanel.add(startMillisLbl, "sg lbl");
		pojoPanel.add(startMillisField, "sg fd");
		pojoPanel.add(endMillisLbl, "sg lbl");
		pojoPanel.add(endMillisField, "sg fd");
		
		this.setLayout(new MigLayout("wrap 1, ins 5px"));
		this.add(pojoPanel, "width 100%, height 50%");
		
		rewardPanel.setBorder(BorderFactory.createTitledBorder("奖励内容"));
		
		this.add(rewardPanel, "width 100%, height 50%");
	
		this.add(okButton, "split 3, align center");
		this.add(resetDateButton, "");
		this.add(cancelButton, "");
	}
	
	public void updateRewardPojo() {
		this.idField.setText(cdkeyPojo.getId());
		this.channelField.setText(cdkeyPojo.getChannel());
		long startMillis = cdkeyPojo.getStartMillis();
		if ( startMillis >0 ) {
			this.startMillisField.setDate(new Date(startMillis));
		} else {
			this.startMillisField.setDate(new Date(0));
		}
		long endMillis = cdkeyPojo.getEndMillis();
		if ( endMillis >0 ) {
			this.endMillisField.setDate(new Date(endMillis));
		} else {
			this.endMillisField.setDate(new Date(0));
		}
		Reward reward = this.cdkeyPojo.getReward();
		if ( reward != null ) {
			this.rewardPanel.updateReward(reward);
		}
	}

	public CDKeyPojo getRewardPojo() {
		return this.cdkeyPojo;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			this.cdkeyPojo.setId(this.idField.getText());
			this.cdkeyPojo.setChannel(this.channelField.getText());
			long startMillis = this.startMillisField.getDate().getTime();
			if ( startMillis > 0 ) {
				this.cdkeyPojo.setStartMillis(startMillis);
			} else {
				this.cdkeyPojo.setStartMillis(0);
			}
			long endMillis = this.endMillisField.getDate().getTime();
			if ( endMillis > 0 ) {
				this.cdkeyPojo.setEndMillis(endMillis);
			} else {
				this.cdkeyPojo.setEndMillis(0);
			}
			Reward reward = this.rewardPanel.getReward();
			if ( reward != null ) {
				this.cdkeyPojo.setReward(reward);
			}
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.cdkeyPojo = null;
			this.dispose();
		} else if ( COMMAND_RESET_DATE.toString().equals(e.getActionCommand()) ) {
			this.startMillisField.setDate(new Date(0));
			this.endMillisField.setDate(new Date(0));
		}
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

}
