package com.xinqihd.sns.gameserver.admin.action.game;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.xinqihd.sns.gameserver.admin.model.MyTableModel;

public abstract class AbstractAddRowAction extends AbstractAction {
	
	protected MyTableModel tableModel = null;
	
	public AbstractAddRowAction() {
		super();
	}
	
	public AbstractAddRowAction(String name) {
		super(name);
	}
	
	public AbstractAddRowAction(String name, Icon icon) {
		super(name, icon);
	}

	public void setTableModel(MyTableModel tableModel) {
		this.tableModel = tableModel;
	}
	
	public MyTableModel getTableModel() {
		return this.tableModel;
	}
}
