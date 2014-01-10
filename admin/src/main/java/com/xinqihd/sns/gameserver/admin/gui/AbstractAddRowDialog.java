package com.xinqihd.sns.gameserver.admin.gui;

import javax.swing.Action;
import javax.swing.JDialog;

import org.jdesktop.swingx.JXButton;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;

public abstract class AbstractAddRowDialog extends MyDialog {
	
	private JXButton okButton = new JXButton();
	private JXButton cancelButton = new JXButton();
	
	public AbstractAddRowDialog() {
	}

	public void init() {
		this.setModal(true);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setDefaultLookAndFeelDecorated(false);
		this.setResizable(false);
	}
	
	/**
	 * Construct the new object from user's input
	 * @return
	 */
	public abstract Object constructNewRowObject();
	
	/**
	 * Return the action used to add a new row.
	 * @return
	 */
	public abstract Action getAddRowAction();
}
