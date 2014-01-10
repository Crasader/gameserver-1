package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.reward.AddOrEditRewardDialog;
import com.xinqihd.sns.gameserver.admin.task.AddOrEditTask;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.RewardPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskRewardLevelPanel extends AbstractTaskPanel {
	
	private static TaskRewardLevelPanel instance = new TaskRewardLevelPanel(); 
	
	
	public TaskRewardLevelPanel() {
		init();
	}
	
	public static TaskRewardLevelPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "rewardlevels";
		super.initModel(collection);
		
		hiddenFields.add("class");
		model.setHiddenFields(hiddenFields);
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("奖励等级管理");
		myTable.setTableModel(model);
		//myTable.setAddRowAction(new AddRewardPojoAction());
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setBackupAction(backupAction);
//		myTable.setColumnEditor(3, new GameDataEditor());
		/*
		myTable.getTable().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if ( e.getClickCount() == 2 ) {
					int rowIndex = myTable.getTable().getSelectedRow();
					if ( rowIndex >= 0 ) {
						int modelRowIndex = myTable.getTable().convertRowIndexToModel(rowIndex);
						RewardPojo selectedRewardPojo = null;
						DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
						selectedRewardPojo = getSelectedPojo();

						AddOrEditRewardDialog dialog = new AddOrEditRewardDialog(selectedRewardPojo, false);
						dialog.setVisible(true);

						selectedRewardPojo = dialog.getRewardPojo();
						if ( selectedRewardPojo != null ) {
							MapDBObject objToSave = new MapDBObject();
							objToSave.putAll(selectedRewardPojo);
							model.updateRow(objToSave, modelRowIndex);
						}
					}
				}
			}

		});
		*/
		this.add(myTable, "width 100%, height 100%");
	}
	
	private RewardPojo getSelectedPojo() {
		int rowIndex = myTable.getTable().getSelectedRow();
		if ( rowIndex >= 0 ) {
			int modelRowIndex = myTable.getTable().convertRowIndexToModel(rowIndex);
			RewardPojo selectedRewardPojo = null;
			DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
			selectedRewardPojo = (RewardPojo)MongoUtil.constructObject(dbObj);
			return selectedRewardPojo;
		}
		return null;
	}
	
	/*
	private class AddRewardPojoAction extends AbstractAction {
		
		public AddRewardPojoAction() {
			super("", ImageUtil.createImageSmallIcon("Button Add.png", "Add Row"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RewardPojo selectedPojo = getSelectedPojo();
			AddOrEditRewardDialog dialog = null;
			if ( selectedPojo != null ) {
				dialog = new AddOrEditRewardDialog(selectedPojo, true);
			} else {
				dialog = new AddOrEditRewardDialog();
			}
			dialog.setVisible(true);
			
			RewardPojo newRewardPojo = dialog.getRewardPojo();
			if ( newRewardPojo != null ) {
				MapDBObject objToSave = new MapDBObject();
				objToSave.putAll(newRewardPojo);
				model.insertRow(objToSave);
			}
		}
	}
	*/
}
