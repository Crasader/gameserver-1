package com.xinqihd.sns.gameserver.admin.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

/**
 * It is a global action used to refresh the data and setting from database 
 * and gameservers.
 * 
 * @author wangqi
 *
 */
public class RefreshAction extends AbstractAction {
	
	public RefreshAction() {
		super("刷新系统", ImageUtil.createImageSmallIcon("Button Reload.png", "Button Reload.png"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MainFrame.getMainPanel().getStatusBar().updateStatus("F5 is pressed");
		MainFrame.getMainPanel().getStatusBar().updateProgress(20);
	}

}
