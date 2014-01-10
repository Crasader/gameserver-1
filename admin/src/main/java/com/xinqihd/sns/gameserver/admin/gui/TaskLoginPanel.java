package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextArea;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceBulletin.BceBulletin;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskLoginPanel extends MyPanel implements ActionListener {
	
	private static final String ACTION_SEND = "send";
	private static TaskLoginPanel instance = new TaskLoginPanel(); 
	
	private JXLabel clientVersionLabel = new JXLabel("设定客户端最低版本要求:");
	private JSpinner majorVersionField = new JSpinner();
	private JSpinner minorVersionField = new JSpinner();
	private JXLabel configVersionLabel = new JXLabel("基础数据版本(0为默认):");
	private JSpinner configVersionField = new JSpinner();
	
	private JXButton clientVersionOKButton = new JXButton("保存");
	
	private JXComboBox serverBox = new JXComboBox(new String[]{"192.168.0.77:3443", "game1.babywar:3443", "game2.babywar:3443", "game3.babywar:3443"});
	private JXTextArea bulletinField = new JXTextArea();
	private JXButton sendButton = new JXButton();
	private JXComboBox typeField = new JXComboBox(new String[]{"下拉消息", "确认弹窗"});
	private JSpinner expireField = new JSpinner();
	
	public TaskLoginPanel() {
		init();
	}
	
	public static TaskLoginPanel getInstance() {
		return instance;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			int majorVersion = (Integer)this.majorVersionField.getValue();
			int minorVersion = (Integer)this.minorVersionField.getValue();
			int configVersion = (Integer)this.configVersionField.getValue();
			LoginManager.getInstance().setClientVersion(majorVersion, minorVersion, configVersion);
			LoginManager.getInstance().saveClientVersion();
		} else if (ACTION_SEND.equals(e.getActionCommand()) ) {
			String gameServerId = this.serverBox.getSelectedItem().toString();
			String fields[] = StringUtil.splitMachineId(gameServerId);
			String remoteHost = fields[0];
			int type = this.typeField.getSelectedIndex();
			int expire = (Integer)this.expireField.getValue();
			int remotePort = StringUtil.toInt(fields[1], 3443);
			GameClient client = new GameClient(remoteHost, remotePort);
			XinqiMessage msg = new XinqiMessage();
			BceBulletin.Builder builder = BceBulletin.newBuilder();
			builder.setMessage(bulletinField.getText());
			builder.setType(type);
			builder.setExpire(expire);
			msg.payload = builder.build();
			client.sendMessageToServer(msg);
		}
	}

	public void init() {
		//创建工作区域
		this.setLayout(new MigLayout(""));
		LoginManager.getInstance().reload();
		this.majorVersionField.setValue(LoginManager.getInstance().getClientMajorVersion());
		this.minorVersionField.setValue(LoginManager.getInstance().getClientMinorVersion());
		this.configVersionField.setValue(LoginManager.getInstance().getClientConfigVersion());
		this.clientVersionOKButton.setActionCommand(ActionName.OK.name());
		this.clientVersionOKButton.addActionListener(this);
		this.bulletinField.setColumns(100);
		this.bulletinField.setRows(20);
		this.sendButton.setActionCommand(ACTION_SEND);
		this.sendButton.addActionListener(this);
		this.sendButton.setText("发送");
		this.expireField.setValue(0);
		
		JXPanel versionPanel = new JXPanel(new MigLayout("wrap 4"));
		versionPanel.setBorder(BorderFactory.createTitledBorder("登陆管理"));
		
		versionPanel.add(clientVersionLabel, "width 25%");
		versionPanel.add(majorVersionField,  "width 10%");
		versionPanel.add(minorVersionField,  "width 10%");
		versionPanel.add(configVersionLabel,  "newline, width 25%");
		versionPanel.add(configVersionField,  "width 20%, wrap");
		versionPanel.add(clientVersionOKButton, "width 10%, align center");
		
		JXPanel bulletinPanel = new JXPanel(new MigLayout("wrap 1"));
		bulletinPanel.setBorder(BorderFactory.createTitledBorder("消息管理"));
		bulletinPanel.add(bulletinField, "grow");
		bulletinPanel.add(typeField, "split 4");
		bulletinPanel.add(expireField, "width 5%");
		bulletinPanel.add(serverBox, "");
		bulletinPanel.add(sendButton, "align center");
		
		this.add(versionPanel, "width 100%, wrap");
		this.add(bulletinPanel, "width 100%");
	}
	
}
