package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class ChargeAction extends AbstractAction {
	
	private UserTreeTableModel model = null;
	private UserManagePanel panel = null;
	private UserId userId = null;
	private String gameServerHost = "192.168.0.77";
	private int gameServerPort = 3443;

	public ChargeAction(UserManagePanel panel) {
		super("玩家充值");
		this.panel = panel;
	}
	
	public void setUserId(UserId userId) {
		this.userId = userId;
	}
	
	public UserId getUserId() {
		return this.userId;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( this.userId == null ) {
			JOptionPane.showMessageDialog(this.panel, "您还没有选择用户");
			return;
		}
		String gameServerId = this.panel.getGameServerId();
		String[] ids = StringUtil.splitMachineId(gameServerId);
		ChargeDialog dialog = new ChargeDialog(this.userId, ids[0], StringUtil.toInt(ids[1], 3443));
		dialog.setVisible(true);
	}
	
}
