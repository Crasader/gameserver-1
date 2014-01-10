package com.xinqihd.sns.gameserver.admin.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;

public class CloseDialogAction extends AbstractAction {
	
	private MyDialog myDialog = null;
	
	public CloseDialogAction(MyDialog dialog) {
		super("取消");
		this.myDialog = dialog;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		this.myDialog.dispose();
	}

	
}
