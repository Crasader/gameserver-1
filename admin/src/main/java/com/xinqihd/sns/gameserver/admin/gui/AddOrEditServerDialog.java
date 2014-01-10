package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.util.StringUtil;


/**
 * 增加或者修改服务器对象
 * @author wangqi
 *
 */
public class AddOrEditServerDialog extends JDialog implements ActionListener {

	private JXLabel idLbl = new JXLabel("ID");
	private JXLabel nameLbl = new JXLabel("名称");
	private JXLabel channelLbl = new JXLabel("渠道");
	private JXLabel hotLbl = new JXLabel("是否热门");
	private JXLabel newLbl = new JXLabel("是否新");
	private JXLabel hostLbl = new JXLabel("主机");
	private JXLabel portLbl = new JXLabel("端口");
	private JXLabel startMillisLbl = new JXLabel("开服日期");
	private JXLabel mergeIdLbl = new JXLabel("合服ID");
	private JXLabel passKeyLbl = new JXLabel("秘钥");
	private JXLabel versionLbl = new JXLabel("版本限定");
	private JXLabel versionUrlLbl = new JXLabel("版本URL");

	private JXTextField idField = new JXTextField("ID");
	private JXTextField nameField = new JXTextField("名称");
	private JXTextField channelField = new JXTextField("渠道");
	private JCheckBox hotField = new JCheckBox("是否热");
	private JCheckBox newField = new JCheckBox("是否新");
	private JXTextField hostField = new JXTextField("主机");
	private JSpinner portField = new JSpinner();
	private JXDatePicker startMillisField = new JXDatePicker();
	private JXTextField mergeIdField = new JXTextField("合服ID");
	private JXTextField passKeyField = new JXTextField("");
	
	private JCheckBox registerField = new JCheckBox("是否能注册");
	private JXTextField versionField = new JXTextField("版本");
	private JXTextField versionUrlField = new JXTextField("");
	
	private JButton okButton = new JButton("保存");
	private JButton cancelButton = new JButton("取消");

	private ServerPojo serverPojo = new ServerPojo();

	public AddOrEditServerDialog(ServerPojo serverPojo, boolean isCreate) {
		if ( serverPojo != null ) {
			this.serverPojo.setId(serverPojo.getId());
			this.serverPojo.setName(serverPojo.getName());
			this.serverPojo.setChannel(serverPojo.getChannel());
			this.serverPojo.setHot(serverPojo.isHot());
			this.serverPojo.setNew(serverPojo.isNew());
			this.serverPojo.setHost(serverPojo.getHost());
			this.serverPojo.setPort(serverPojo.getPort());
			this.serverPojo.setStartMillis(serverPojo.getStartMillis());
			this.serverPojo.setMergeId(serverPojo.getMergeId());
			this.serverPojo.setPassKey(serverPojo.getPassKey());
			this.serverPojo.setVersion(serverPojo.getVersion());
			this.serverPojo.setVersionUrl(serverPojo.getVersionUrl());
			this.serverPojo.setRegistable(serverPojo.isRegistable());
		}
		init();
	}

	public void init() {
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		
		this.idField.setText(this.serverPojo.getId());
		this.nameField.setText(this.serverPojo.getName());
		this.channelField.setText(this.serverPojo.getChannel());
		this.hotField.setSelected(this.serverPojo.isHot());
		this.newField.setSelected(this.serverPojo.isNew());
		this.hostField.setText(this.serverPojo.getHost());
		this.portField.setValue(this.serverPojo.getPort());
		this.startMillisField.setDate(new Date(this.serverPojo.getStartMillis()));
		this.mergeIdField.setText(this.serverPojo.getMergeId());
		this.passKeyField.setText(this.serverPojo.getPassKey());
		this.versionField.setText(this.serverPojo.getVersion());
		this.versionUrlField.setText(this.serverPojo.getVersionUrl());
		this.registerField.setSelected(this.serverPojo.isRegistable());
		
		this.setLayout(new MigLayout("wrap 4", ""));
		this.setSize(450, 280);
		
		this.add(idLbl, "sg lbl");
		this.add(idField, "sg fd");
		this.add(nameLbl, "sg lbl");
		this.add(nameField, "sg fd");
		this.add(channelLbl, "sg lbl");
		this.add(channelField, "sg fd");
		this.add(hotLbl, "sg lbl");
		this.add(hotField, "sg fd");
		this.add(newLbl, "sg lbl");
		this.add(newField, "sg fd");
		this.add(hostLbl, "sg lbl");
		this.add(hostField, "sg fd");
		this.add(portLbl, "sg lbl");
		this.add(portField, "sg fd");
		this.add(startMillisLbl, "sg lbl");
		this.add(startMillisField, "sg fd");
		this.add(mergeIdLbl, "sg lbl");
		this.add(mergeIdField, "sg fd");
		this.add(passKeyLbl, "sg lbl");
		this.add(passKeyField, "sg fd");
		this.add(versionLbl, "sg lbl");
		this.add(versionField, "sg fd");
		this.add(registerField, "sg fd");
		this.add(versionUrlLbl, "newline, sg lbl");
		this.add(versionUrlField, "span, growx");
		
		this.add(okButton, "newline, span, split 2, align center");
		this.add(cancelButton, "");
		
		this.setResizable(true);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		this.setModal(true);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			this.serverPojo.setId(this.idField.getText());
			this.serverPojo.setName(this.nameField.getText());
			if ( this.channelField.getText() != null ) {
				this.serverPojo.setChannel(this.channelField.getText());
			} else {
				this.serverPojo.setChannel(null);
			}
			this.serverPojo.setHot(this.hotField.isSelected());
			this.serverPojo.setNew(this.newField.isSelected());
			this.serverPojo.setHost(this.hostField.getText());
			this.serverPojo.setPort((Integer)this.portField.getValue());
			long time = this.startMillisField.getDate().getTime();
			if ( time > 0 ) {
				this.serverPojo.setStartMillis(time);
			} else {
				this.serverPojo.setStartMillis(0);
			}
			if ( StringUtil.checkNotEmpty(this.mergeIdField.getText()) ) {
				this.serverPojo.setMergeId(this.mergeIdField.getText());
			} else {
				this.serverPojo.setMergeId(null);
			}
			if ( StringUtil.checkNotEmpty(this.passKeyField.getText()) ) {
				this.serverPojo.setPassKey(this.passKeyField.getText());
			} else {
				this.serverPojo.setPassKey(null);
			}
			if ( StringUtil.checkNotEmpty(this.versionField.getText()) ) {
				this.serverPojo.setVersion(this.versionField.getText());
			} else {
				this.serverPojo.setVersion(null);
			}
			if ( StringUtil.checkNotEmpty(this.versionUrlField.getText()) ) {
				this.serverPojo.setVersionUrl(this.versionUrlField.getText());
			} else {
				this.serverPojo.setVersionUrl(null);
			}
			this.serverPojo.setRegistable(this.registerField.isSelected());
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.serverPojo = null;
			this.dispose();
		}
	}

	public ServerPojo getServerPojo() {
		return serverPojo;
	}
}
