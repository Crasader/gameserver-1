package com.xinqihd.sns.gameserver.admin.user;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.xinqihd.sns.gameserver.admin.gui.UserManagePanel;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class PushAction extends AbstractAction {
	
	private UserTreeTableModel model = null;
	private UserManagePanel panel = null;
	private User user = null;
	private String gameServerHost = "192.168.0.77";
	private int gameServerPort = 3443;

	public PushAction(UserManagePanel panel) {
		super("消息推送");
		this.panel = panel;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return this.user;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( this.user == null ) {
			JOptionPane.showMessageDialog(this.panel, "您还没有选择用户");
			return;
		}
		String gameServerId = this.panel.getGameServerId();
		String[] ids = StringUtil.splitMachineId(gameServerId);
		PushDialog dialog = new PushDialog(this.user, ids[0], StringUtil.toInt(ids[1], 3443));
		dialog.setVisible(true);
	}
	
}
