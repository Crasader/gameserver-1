package com.xinqihd.sns.gameserver.admin.user;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.proto.XinqiBceForbidUser.BceForbidUser;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

public class ManageAccountStatusDialog extends MyDialog implements ActionListener {

	private static final String CMD_STATUS_SELECT = "statusSelect";
	private static final String CMD_PREVIEW = "statusPreview";
	
	private JXLabel    statusLabel = new JXLabel("登陆状态:");
	private JXComboBox statusField = new JXComboBox(new String[]{"正常", "禁用"});
	private JXTextField descField = new JXTextField();
		
	private JXButton okButton = new JXButton("确定");
	private JXButton cancelButton = new JXButton("取消");
	private JXButton previewButton = new JXButton("消息预览");
	
	private Account account = null;
	private String gameServerId = null;
	
	public ManageAccountStatusDialog(Account account, String gameServerId) {
		this.account = account;
		this.gameServerId = gameServerId;
		init();
	}
	
	public void init() {
		this.setTitle("管理账户状态");
		this.setSize(320, 430);
		Point p = WindowUtils.getPointForCentering(this);
		this.setLocation(p);
		this.setModal(true);
		this.statusField.setActionCommand(CMD_STATUS_SELECT);
		this.statusField.addActionListener(this);
		this.statusField.setSelectedIndex(0);
		
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		this.previewButton.setActionCommand(CMD_PREVIEW);
		this.previewButton.addActionListener(this);
		
		JXPanel loginPanel = new JXPanel(new MigLayout("wrap 2, gap 10px", "[45%][55%]"));
		loginPanel.add(this.statusLabel, "sg lbl");
		loginPanel.add(this.statusField, "sg fd, grow");
		loginPanel.add(this.descField, "span, grow");
		loginPanel.add(this.previewButton, "gaptop 5px, span, split 3, align center");		
		loginPanel.setBorder(BorderFactory.createTitledBorder("登陆管理"));
						
		JXPanel panel = new JXPanel(new MigLayout("wrap 1, gap 10px", "[100%]"));
		this.setLayout(new MigLayout("wrap 1"));
		panel.add(loginPanel);
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
			if ( this.statusField.getSelectedIndex() == 2 ) {
				String desc = this.descField.getText();
				JOptionPane.showMessageDialog(this, desc);
			}
		} else if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			boolean success = false;
			if ( this.statusField.getSelectedIndex() == 0 ) {
				//normal mode
				success = AccountManager.getInstance().unforbiddenAccount(account.getUserName());
			} else {
				//forbidden mode
				String message = this.descField.getText();
				if ( Constant.EMPTY.equals(message) ) {
					message = Text.text("account.forbidden");
				}
				BceForbidUser.Builder builder = BceForbidUser.newBuilder();
				builder.setAccountname(this.account.getUserName());
				builder.setMessage(message);
				//success = AccountManager.getInstance().forbiddenAccount(null, account.getUserName(), message);
				String[] machineId = StringUtil.splitMachineId(gameServerId);
				GameClient client = new GameClient(machineId[0], StringUtil.toInt(machineId[1], 3443));
				XinqiMessage msg = new XinqiMessage();
				msg.payload = builder.build();
				client.sendMessageToServer(msg);
				JOptionPane.showConfirmDialog(this, "已经通知"+gameServerId+"禁用玩家账号:"+this.account.getUserName());
			}
			if ( success ) {
				JOptionPane.showMessageDialog(this, "状态修改成功！");
			}
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.account = null;
			this.dispose();
		}
	}

	public Account getAccount() {
		return this.account;
	}
}
