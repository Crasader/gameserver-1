package com.xinqihd.sns.gameserver.admin.user;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceOfflinePush.BceOfflinePush;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class PushDialog extends MyDialog implements ActionListener {


	private JXLabel    statusLabel = new JXLabel("输入待推送的消息:");
	private JTextArea infoField = new JTextArea();
	
	private JXButton okButton = new JXButton("确定");
	private JXButton cancelButton = new JXButton("取消");
	
	private User user = null;
	private String gameServerHost = null;
	private int gameServerPort = 3443; 
	
	public PushDialog(User user, String gameServerHost, int gameServerPort) {
		this.user = user;
		this.gameServerHost = gameServerHost;
		this.gameServerPort = gameServerPort;
		init();
	}
	
	public void init() {
		this.setTitle("消息管理");
		this.setSize(500, 300);
		Point p = WindowUtils.getPointForCentering(this);
		this.setLocation(p);
		this.setModal(true);

		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		this.infoField.setText("");
		
		JXPanel loginPanel = new JXPanel(new MigLayout("wrap 2, gap 10px", "[45%][55%]"));
		loginPanel.add(this.statusLabel, "span");
		loginPanel.add(this.infoField,   "span, growx");
		loginPanel.setBorder(BorderFactory.createTitledBorder("PUSH"));
						
		JXPanel panel = new JXPanel(new MigLayout("wrap 1, gap 10px", "[100%]"));
		this.setLayout(new MigLayout("wrap 1"));
		panel.add(loginPanel, "growx");
		panel.add(this.okButton, "gaptop 5px, span, split 3, align center");
		panel.add(this.cancelButton);

		this.add(panel, "width 100%, height 100%");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			String text = infoField.getText();
			if ( StringUtil.checkNotEmpty(text) ) {
				boolean success = false;
				try {
					BceOfflinePush.Builder builder = BceOfflinePush.newBuilder();
					builder.setUserid(user.get_id().toString());
					builder.setMessage(text);

					GameClient client = new GameClient(
							this.gameServerHost, this.gameServerPort);
					XinqiMessage msg = new XinqiMessage();
					msg.payload = builder.build();
					client.sendMessageToServer(msg);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if ( success ) {
					JOptionPane.showMessageDialog(this, "消息发送成功！");
				} else {
					JOptionPane.showMessageDialog(this, "消息发送失败！");
				}
			}
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.user = null;
			this.dispose();
		}
	}

}
