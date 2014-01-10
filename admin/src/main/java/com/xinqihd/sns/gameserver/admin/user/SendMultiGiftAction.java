package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;

import org.jdesktop.swingx.JXTextArea;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;
import com.xinqihd.sns.gameserver.admin.item.AddRewardDialog;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.proto.XinqiBceMailSend.BceMailSend;
import com.xinqihd.sns.gameserver.proto.XinqiMailData.MailData;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 向多人群发邮件的支持
 * @author wangqi
 *
 */
public class SendMultiGiftAction extends AbstractAction {
	
	private UserTreeTableModel model = null;
	private UserManagePanel panel = null;
	private JXTextArea roleNameList = new JXTextArea();

	public SendMultiGiftAction(UserTreeTableModel model, UserManagePanel panel) {
		super("群发");
		this.model = model;
		this.panel = panel;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		AddRewardDialog dialog = new AddRewardDialog("赠送道具");
		dialog.setVisible(true);
		Reward reward = dialog.getReward();
		String subject = dialog.getSubject();
		String content = dialog.getContent();
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		if ( reward != null ) {
			rewards.add(reward);
			/*
			BceSendGift.Builder builder = BceSendGift.newBuilder();
			builder.addGift(reward.toGift());
			builder.setFromUserName("GameAdmin");
			builder.setToUserIdStr(this.userId.toString());			
			xinqi.payload = builder.build();

			String remoteHost = ConfigManager.getConfigAsString(ConfigKey.gameHost);
			if ( remoteHost == null ) {
				remoteHost = "g1.babywar.xinqihd.com";
			}
			System.out.println("game remote host:" + remoteHost);
			int remotePort = StringUtil.toInt(ConfigManager.getConfigAsString(ConfigKey.gamePort), 3443);
			GameClient client = new GameClient(remoteHost, remotePort);
			client.sendMessageToServer(xinqi);
			*/
		}
		//Send the gift to user.
		XinqiMessage xinqi = new XinqiMessage();
		BceMailSend.Builder builder = BceMailSend.newBuilder();
		MailData.Builder mailData = MailData.newBuilder();
		mailData.setSubject(subject);
		mailData.setContent(content);
		mailData.setFromuser("系统");
		mailData.setIsnew(true);
		BasicUser toUser = UserManager.getInstance().queryBasicUser("");
		mailData.setTouser(toUser.getRoleName());
		mailData.setSentdate(DateUtil.formatDate(new Date()));
		for ( Reward r : rewards ) {
			mailData.addGifts(r.toGift());
		}
		builder.setMail(mailData.build());
		xinqi.payload = builder.build();
		String gameServerId = panel.getGameServerId();
		String[] gameServer = StringUtil.splitMachineId(gameServerId);
		String remoteHost = gameServer[0];
		if ( remoteHost == null ) {
			remoteHost = "g1.babywar.xinqihd.com";
		}
		System.out.println("game remote host:" + remoteHost);
		int remotePort = StringUtil.toInt(gameServer[1], 3443);
		GameClient client = new GameClient(remoteHost, remotePort);
		client.sendMessageToServer(xinqi);

	}
	
}
