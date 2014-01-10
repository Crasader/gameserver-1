package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.task.AddOrEditTask;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.config.PromotionPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskPromotionPanel extends AbstractTaskPanel {
	
	private static TaskPromotionPanel instance = new TaskPromotionPanel(); 
	
	
	public TaskPromotionPanel() {
		init();
	}
	
	public static TaskPromotionPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "promotions";
		super.initModel(collection);
		
		hiddenFields.add("class");
		model.setHiddenFields(hiddenFields);
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("活动公告信息");
		myTable.setTableModel(model);
		myTable.setAddRowAction(new AddPromotionAction());
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
						PromotionPojo selectedTask = null;
						DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
						selectedTask = (PromotionPojo)MongoUtil.constructObject(dbObj);
						
						AddOrEditPromotionDialog dialog = new AddOrEditPromotionDialog(selectedTask, false);
						dialog.setVisible(true);
						
						selectedTask = dialog.getPromotionPojo();
						if ( selectedTask != null ) {
							MapDBObject objToSave = new MapDBObject();
							objToSave.putAll(selectedTask);
							model.updateRow(objToSave, modelRowIndex);
						}
					}
				}
			}
			
		});
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setBackupAction(backupAction);
//		myTable.setColumnEditor(3, new GameDataEditor());
		this.add(myTable, "width 100%, height 100%");
	}
	
	
	private class AddPromotionAction extends AbstractAction {
		
		public AddPromotionAction() {
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
				PromotionPojo selectedTask = null;
				DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
				selectedTask = (PromotionPojo)MongoUtil.constructObject(dbObj);
				
				AddOrEditPromotionDialog dialog = new AddOrEditPromotionDialog(selectedTask, true);
				dialog.setVisible(true);
				
				selectedTask = dialog.getPromotionPojo();
				if ( selectedTask != null ) {
					MapDBObject objToSave = new MapDBObject();
					objToSave.putAll(selectedTask);
					model.insertRow(objToSave);
				}
			} else {
				AddOrEditPromotionDialog dialog = new AddOrEditPromotionDialog(null, false);
				dialog.setVisible(true);
				
				PromotionPojo newTask = dialog.getPromotionPojo();
				if ( newTask != null ) {
					MapDBObject objToSave = new MapDBObject();
					objToSave.putAll(newTask);
					model.insertRow(objToSave);
				}
			}
		}
	}
}
