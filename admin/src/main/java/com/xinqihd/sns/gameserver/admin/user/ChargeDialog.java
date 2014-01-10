package com.xinqihd.sns.gameserver.admin.user;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JSpinner;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class ChargeDialog extends MyDialog implements ActionListener {

	private static String info = 
			"VIP1	VIP2	VIP3	VIP4	VIP5	VIP6	VIP7	VIP8	VIP9	VIP10\n"+
			"N	20	40	110	200	999	1999	5000	10000	20000 \n"+
			"10~60	200	400	1100	2000	9990	19990	50000	100000	200000";

	private JXLabel    statusLabel = new JXLabel("输入充入的RMB数量:");
	private JSpinner   valueField = new JSpinner();

	private JTextArea infoField = new JTextArea();
	private JXButton okButton = new JXButton("确定");
	private JXButton cancelButton = new JXButton("取消");
	
	private UserId userId = null;
	private String gameServerHost = "192.168.0.77";
	private int gameServerPort = 3443;
	
	public ChargeDialog(UserId userId, String gameServerHost, int gameServerPort) {
		this.userId = userId;
		this.gameServerHost = gameServerHost;
		this.gameServerPort = gameServerPort;
		init();
	}
	
	public void init() {
		this.setTitle("充值管理");
		this.setSize(900, 280);
		Point p = WindowUtils.getPointForCentering(this);
		this.setLocation(p);
		this.setModal(true);

		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		this.infoField.setText(info);
		this.infoField.setEditable(false);
		
		JXPanel loginPanel = new JXPanel(new MigLayout("wrap 2, gap 10px", "[45%][55%]"));
		loginPanel.add(this.statusLabel, "sg lbl");
		loginPanel.add(this.valueField, "sg fd, width 10%");
		loginPanel.setBorder(BorderFactory.createTitledBorder("充值管理"));
						
		JXPanel panel = new JXPanel(new MigLayout("wrap 1, gap 10px", "[100%]"));
		this.setLayout(new MigLayout("wrap 1"));
		panel.add(loginPanel, "growx");
		panel.add(infoField, "grow");
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
			BceChargeInternal.Builder charge = BceChargeInternal.newBuilder();
			charge.setChargeid(0);
			//xq001
			charge.setOrderid(StringUtil.concat(System.currentTimeMillis(), userId.toString()));
			charge.setUserid(userId.toString());
			charge.setFreecharge(true);
			charge.setChargemoney((Integer)valueField.getValue());
			charge.setChannel("admin");
			XinqiMessage msg = new XinqiMessage();
			msg.payload=charge.build();
			
			GameClient client = new GameClient(gameServerHost, gameServerPort);
			//GameClient client = new GameClient("192.168.0.77", 3443);
			client.sendMessageToServer(msg);
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.userId = null;
			this.dispose();
		}
	}

}
