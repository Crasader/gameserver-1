package com.xinqihd.sns.gameserver.admin.user;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import redis.clients.jedis.Jedis;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.admin.util.AdminJedis;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.UserLoginStatus;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserStatus;
import com.xinqihd.sns.gameserver.util.Text;

public class ManageLoginStatusDialog extends MyDialog implements ActionListener {

	private static final String CMD_STATUS_SELECT = "statusSelect";
	private static final String CMD_PREVIEW = "statusPreview";
	
	private JXLabel    statusLabel = new JXLabel("登陆状态:");
	private JXComboBox statusField = new JXComboBox(new String[]{"正常", "暂停", "删除"});
	private JSpinner   valueField = new JSpinner();
	private JXComboBox timeUnitField = new JXComboBox(new String[]{"秒", "分钟", "小时", "天", "周", "月"});
	private JXTextField descField = new JXTextField();
	
	private JXLabel    chatLabel = new JXLabel("聊天状态:");
	private JXComboBox chatField = new JXComboBox(new String[]{"正常", "禁言"});
	
	private JXButton okButton = new JXButton("确定");
	private JXButton cancelButton = new JXButton("取消");
	private JXButton previewButton = new JXButton("消息预览");
	
	private User user = null;
	
	public ManageLoginStatusDialog(User user) {
		this.user = user;
		init();
	}
	
	public void init() {
		this.setTitle("管理登陆状态");
		this.setSize(320, 430);
		Point p = WindowUtils.getPointForCentering(this);
		this.setLocation(p);
		this.setModal(true);
		this.statusField.setActionCommand(CMD_STATUS_SELECT);
		this.statusField.addActionListener(this);
		this.statusField.setSelectedIndex(user.getLoginStatus().ordinal());
		try {
			Jedis jedis = AdminJedis.getInstance().getJedis();
			Long ttlLong = jedis.ttl(LoginManager.getInstance().getUserPauseKey(user.getUsername()));
			if ( ttlLong != null && ttlLong.intValue()>0 ) {
				this.valueField.setValue(ttlLong.intValue());
			} else {
				this.valueField.setValue(0);
				if ( user.getLoginStatus() == UserLoginStatus.PAUSE ) {
					this.statusField.setSelectedIndex(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.descField.setText(user.getLoginStatusDesc());
		switch ( user.getStatus() ) {
			case NORMAL:
				this.chatField.setSelectedIndex(0);
				break;
			case CHAT_DISABLE:
				this.chatField.setSelectedIndex(1);
				break;
		}
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		this.previewButton.setActionCommand(CMD_PREVIEW);
		this.previewButton.addActionListener(this);
		
		JXPanel loginPanel = new JXPanel(new MigLayout("wrap 2, gap 10px", "[45%][55%]"));
		loginPanel.add(this.statusLabel, "sg lbl");
		loginPanel.add(this.statusField, "sg fd, grow");
		loginPanel.add(this.valueField, "sg fd, grow");
		loginPanel.add(this.timeUnitField, "sg fd, grow");
		loginPanel.add(this.descField, "span, grow");
		loginPanel.add(this.previewButton, "gaptop 5px, span, split 3, align center");		
		loginPanel.setBorder(BorderFactory.createTitledBorder("登陆管理"));
						
		JXPanel chatPanel = new JXPanel(new MigLayout("wrap 2, gap 10px", "[45%][55%]"));
		chatPanel.setBorder(BorderFactory.createTitledBorder("聊天管理"));
		chatPanel.add(this.chatLabel, "sg lbl");
		chatPanel.add(this.chatField, "sg fd, grow");
		
		JXPanel panel = new JXPanel(new MigLayout("wrap 1, gap 10px", "[100%]"));
		this.setLayout(new MigLayout("wrap 1"));
		panel.add(loginPanel);
		panel.add(chatPanel);
		panel.add(this.okButton, "gaptop 5px, span, split 3, align center");
		panel.add(this.cancelButton);

		this.add(panel, "width 100%, height 100%");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( CMD_PREVIEW.equals(e.getActionCommand()) ) {
			if ( this.statusField.getSelectedIndex() == 1 ) {
				String timeLeft = LoginManager.getInstance().convertSecondToDesc(calculateSeconds());
				String desc = this.descField.getText();
				String message = Text.text(ErrorCode.S_PAUSE.desc(), desc, timeLeft);
				ErrorCode code = ErrorCode.S_PAUSE;
				JOptionPane.showMessageDialog(this, message);
			} else if ( this.statusField.getSelectedIndex() == 2 ) {
				String desc = this.descField.getText();
				String message = Text.text(ErrorCode.S_REMOVED.desc(), desc);
				JOptionPane.showMessageDialog(this, message);
			}
		} else if ( CMD_STATUS_SELECT.equals(e.getActionCommand()) ) {
			if ( statusField.getSelectedIndex() == 1 ) {
				//"暂停"
				this.timeUnitField.setEnabled(true);
				this.valueField.setEnabled(true);
				this.descField.setEnabled(true);
				this.previewButton.setEnabled(true);
			} else if ( statusField.getSelectedIndex() == 2 ) {
				this.timeUnitField.setEnabled(false);
				this.valueField.setEnabled(false);
				this.descField.setEnabled(true);
				this.previewButton.setEnabled(true);
			} else {
				this.timeUnitField.setEnabled(false);
				this.valueField.setEnabled(false);
				this.descField.setEnabled(false);
				this.previewButton.setEnabled(false);
			}
		} else if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			user.setLoginStatus( UserLoginStatus.values()[this.statusField.getSelectedIndex()]);
			if ( user.getLoginStatus() != UserLoginStatus.NORMAL ) {
				user.setLoginStatusDesc(this.descField.getText());
			}
			if ( user.getLoginStatus() == UserLoginStatus.PAUSE ) {
				int seconds = calculateSeconds();
				
				user.setLoginStatus(UserLoginStatus.PAUSE);
				user.setLoginStatusDesc(user.getLoginStatusDesc());
				String key = LoginManager.getInstance().getUserPauseKey(user.getUsername());
				Jedis jedisDB = AdminJedis.getInstance().getJedis();
				jedisDB.set(key, Constant.ONE);
				jedisDB.expire(key, seconds);
			}
			
			//Chat status
			int chatIndex = this.chatField.getSelectedIndex();
			switch ( chatIndex ) {
				case 0:
					user.setStatus(UserStatus.NORMAL);
					break;
				case 1:
					user.setStatus(UserStatus.CHAT_DISABLE);
					break;
			}
			
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.user = null;
			this.dispose();
		}
	}

	private int calculateSeconds() {
		int value = (Integer)this.valueField.getValue();
		switch ( this.timeUnitField.getSelectedIndex() ) {
			//"秒", "分钟", "小时", "天", "周", "月"
			case 0:
				value = value;
				break;
			case 1:
				value *= 60 + 1;
				break;
			case 2:
				value *= 3600 + 1;
				break;
			case 3:
				value *= 86400 + 1;
				break;
			case 4:
				value *= 604800 + 1;
				break;
			case 5:
				value *= 2592000 + 1;
				break;
		}
		return value;
	}

	public User getUser() {
		return this.user;
	}
}
