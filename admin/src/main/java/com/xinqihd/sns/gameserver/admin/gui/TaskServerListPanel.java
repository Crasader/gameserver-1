package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskServerListPanel extends AbstractTaskPanel {
	
	private static TaskServerListPanel instance = new TaskServerListPanel(); 
	
	
	public TaskServerListPanel() {
		init();
	}
	
	public static TaskServerListPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "servers";
		super.initModel(collection);
		
		hiddenFields.add("class");
		model.setHiddenFields(hiddenFields);
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("游戏开服管理");
		myTable.setTableModel(model);
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setBackupAction(backupAction);
		myTable.setAddRowAction(new AddServerAction());
		myTable.getTable().addMouseListener(new MouseAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if ( e.getClickCount() == 2 ) {
					int rowIndex = myTable.getTable().getSelectedRow();
					if ( rowIndex >= 0 ) {
						int modelRowIndex = myTable.getTable().convertRowIndexToModel(rowIndex);
						ServerPojo selectedTask = null;
						DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
						selectedTask = (ServerPojo)MongoUtil.constructObject(dbObj);

						AddOrEditServerDialog dialog = new AddOrEditServerDialog(selectedTask, false);
						dialog.setVisible(true);

						selectedTask = dialog.getServerPojo();
						if ( selectedTask != null ) {
							MapDBObject objToSave = new MapDBObject();
							objToSave.putAll(selectedTask);
							model.updateRow(objToSave, modelRowIndex);
						}
					}
				}
			}
			
		});
//		myTable.setColumnEditor(3, new GameDataEditor());
		this.add(myTable, "width 100%, height 100%");
	}
	
private class AddServerAction extends AbstractAction {
		
		public AddServerAction() {
			super("", ImageUtil.createImageSmallIcon("Button Add.png", "Add Row"));
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			int rowIndex = myTable.getTable().getSelectedRow();
			if ( rowIndex >= 0 ) {
				int modelRowIndex = myTable.getTable().convertRowIndexToModel(rowIndex);
				ServerPojo selectedTask = null;
				DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
				selectedTask = (ServerPojo)MongoUtil.constructObject(dbObj);
				
				AddOrEditServerDialog dialog = new AddOrEditServerDialog(selectedTask, true);
				dialog.setVisible(true);
				
				selectedTask = dialog.getServerPojo();
				if ( selectedTask != null ) {
					MapDBObject objToSave = new MapDBObject();
					objToSave.putAll(selectedTask);
					model.insertRow(objToSave);
				}
			} else {
				AddOrEditServerDialog dialog = new AddOrEditServerDialog(null, false);
				dialog.setVisible(true);
				
				ServerPojo newTask = dialog.getServerPojo();
				if ( newTask != null ) {
					MapDBObject objToSave = new MapDBObject();
					objToSave.putAll(newTask);
					model.insertRow(objToSave);
				}
			}
		}
	}
}
