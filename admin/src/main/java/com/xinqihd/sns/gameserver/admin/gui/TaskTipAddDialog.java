package com.xinqihd.sns.gameserver.admin.gui;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTextField;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.CloseDialogAction;
import com.xinqihd.sns.gameserver.admin.action.game.MongoAddNewRowAction;
import com.xinqihd.sns.gameserver.admin.model.GeneralMongoModel;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class TaskTipAddDialog extends AbstractAddRowDialog {
	
	private JXTextField tipFields = new JXTextField("请输入新的提示信息");
	private JSpinner idField = new JSpinner();
	private JXButton okButton = new JXButton();
	private JXButton cancelButton = new JXButton();
	private GeneralMongoModel model = null;
	private MongoAddNewRowAction action = null;
	
	public TaskTipAddDialog(GeneralMongoModel model) {
		this.model = model;
		this.action = new MongoAddNewRowAction(this, model);
		init();
	}

	public void init() {
		super.init();
		this.tipFields.setAction(action);
		this.okButton.setAction(action);
		this.cancelButton.setAction(new CloseDialogAction(this));
		this.tipFields.setColumns(20);
		
		this.setLayout(new MigLayout("wrap 4"));
		this.setSize(400, 100);
		this.add(new JLabel("ID:"));
		this.add(idField, "width 10%");
		this.add(new JLabel("提示信息:"));
		this.add(tipFields);
		this.add(okButton, "span, split 2, align center");
		this.add(cancelButton);
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.AbstractAddRowDialog#constructNewRowObject()
	 */
	@Override
	public Object constructNewRowObject() {
		String tip = tipFields.getText();
		DBObject objectToSave = MongoUtil.createDBObject();
		objectToSave.put("_id", idField.getValue());
		objectToSave.put("class", "com.xinqihd.sns.gameserver.config.TipPojo");
		objectToSave.put("tip", tip);
		return objectToSave;
	}
	
	@Override
	public Action getAddRowAction() {
		return action;
	}
}
