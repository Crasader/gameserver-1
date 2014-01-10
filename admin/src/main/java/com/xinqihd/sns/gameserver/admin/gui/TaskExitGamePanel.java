package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.reward.AddOrEditExitGameDialog;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.ExitPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskExitGamePanel extends AbstractTaskPanel {
	
	private static TaskExitGamePanel instance = new TaskExitGamePanel(); 
	
	
	public TaskExitGamePanel() {
		init();
	}
	
	public static TaskExitGamePanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "exits";
		super.initModel(collection);
		
		hiddenFields.add("class");
		model.setHiddenFields(hiddenFields);
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("退出游戏奖励管理");
		myTable.setTableModel(model);
		myTable.setAddRowAction(new AddExitPojoAction());
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setBackupAction(backupAction);
//		myTable.setColumnEditor(3, new GameDataEditor());
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
						ExitPojo selectedRewardPojo = null;
						DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
						selectedRewardPojo = getSelectedPojo();
						
						AddOrEditExitGameDialog dialog = new AddOrEditExitGameDialog(selectedRewardPojo, false);
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
		this.add(myTable, "width 100%, height 100%");
	}
	
	private ExitPojo getSelectedPojo() {
		int rowIndex = myTable.getTable().getSelectedRow();
		if ( rowIndex >= 0 ) {
			int modelRowIndex = myTable.getTable().convertRowIndexToModel(rowIndex);
			ExitPojo selectedRewardPojo = null;
			DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
			selectedRewardPojo = (ExitPojo)MongoUtil.constructObject(dbObj);
			return selectedRewardPojo;
		}
		return null;
	}
	
	private class AddExitPojoAction extends AbstractAction {
		
		public AddExitPojoAction() {
			super("", ImageUtil.createImageSmallIcon("Button Add.png", "Add Row"));
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			ExitPojo selectedPojo = getSelectedPojo();
			AddOrEditExitGameDialog dialog = null;
			if ( selectedPojo != null ) {
				dialog = new AddOrEditExitGameDialog(selectedPojo, true);
			} else {
				dialog = new AddOrEditExitGameDialog();
			}
			dialog.setVisible(true);
			
			ExitPojo newRewardPojo = dialog.getRewardPojo();
			if ( newRewardPojo != null ) {
				MapDBObject objToSave = new MapDBObject();
				objToSave.putAll(newRewardPojo);
				model.insertRow(objToSave);
			}
		}
	}
}
