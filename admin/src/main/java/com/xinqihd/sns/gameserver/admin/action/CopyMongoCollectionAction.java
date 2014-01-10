package com.xinqihd.sns.gameserver.admin.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JDialog;

import com.xinqihd.sns.gameserver.service.CopyMongoCollectionService;

/**
 * 用于备份和恢复Mongo中的一个collection
 * @author wangqi
 *
 */
public class CopyMongoCollectionAction extends AbstractAction {
	
	private String sourceDatabase;
	private String sourceNamespace;
	private String sourceCollection; 
	private String targetDatabase;
	private String targetNamespace;
	private String targetCollection;

	public CopyMongoCollectionAction(
			String sourceDatabase, String sourceNamespace, String sourceCollection, 
			String targetDatabase, String targetNamespace, String targetCollection) {
		super("");
		this.sourceDatabase = sourceDatabase; 
		this.sourceNamespace = sourceNamespace;
		this.sourceCollection = sourceCollection; 
		this.targetDatabase = targetDatabase;
		this.targetNamespace = targetNamespace;
		this.targetCollection = targetCollection;
	}
	
	public void setIcon(Icon icon) {
		super.putValue(Action.SMALL_ICON, icon);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		CopyMongoCollectionService service = new CopyMongoCollectionService(
				sourceDatabase, sourceNamespace, sourceCollection, 
				targetDatabase, targetNamespace, targetCollection
				);
		JDialog dialog = service.getDialog();
		service.execute();
		dialog.setVisible(true);
	}
	
}
