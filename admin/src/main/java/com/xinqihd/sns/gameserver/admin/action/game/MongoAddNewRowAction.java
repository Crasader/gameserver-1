package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.AbstractAddRowDialog;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

/**
 * Add DBObject as a new row.
 * @author wangqi
 *
 */
public class MongoAddNewRowAction extends AbstractAction {
	
	private final AbstractAddRowDialog dialog;
	private final MyTableModel model;

	public MongoAddNewRowAction(AbstractAddRowDialog dialog, MyTableModel model) {
		super("确定");
		this.dialog = dialog;
		this.model = model;
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_addnew;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				dialog.dispose();
				Object objToSave = dialog.constructNewRowObject();
				model.insertRow(objToSave);
			}
		});
	}

}
