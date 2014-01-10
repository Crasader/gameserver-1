package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.reward.AddOrEditCDKeyDialog;
import com.xinqihd.sns.gameserver.admin.reward.AddOrEditRewardDialog;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.CDKeyPojo;
import com.xinqihd.sns.gameserver.config.RewardPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * @author wangqi
 *
 */
public class TaskCDKeyPanel extends AbstractTaskPanel {
	
	private static TaskCDKeyPanel instance = new TaskCDKeyPanel(); 
	
	
	public TaskCDKeyPanel() {
		init();
	}
	
	public static TaskCDKeyPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "cdkeys";
		super.initModel(collection);
		
		hiddenFields.add("class");
		model.setHiddenFields(hiddenFields);
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("CDKEY管理");
		myTable.setTableModel(model);
		myTable.setAddRowAction(new AddCDKeyPojoAction());
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setBackupAction(backupAction);

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
						CDKeyPojo selectedRewardPojo = null;
						DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
						selectedRewardPojo = getSelectedPojo();
						
						AddOrEditCDKeyDialog dialog = new AddOrEditCDKeyDialog(selectedRewardPojo, false);
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
	
	private CDKeyPojo getSelectedPojo() {
		int rowIndex = myTable.getTable().getSelectedRow();
		if ( rowIndex >= 0 ) {
			int modelRowIndex = myTable.getTable().convertRowIndexToModel(rowIndex);
			CDKeyPojo selectedRewardPojo = null;
			DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
			selectedRewardPojo = (CDKeyPojo)MongoUtil.constructObject(dbObj);
			return selectedRewardPojo;
		}
		return null;
	}
	
	private class AddCDKeyPojoAction extends AbstractAction {
		
		public AddCDKeyPojoAction() {
			super("", ImageUtil.createImageSmallIcon("Button Add.png", "Add Row"));
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			CDKeyPojo selectedPojo = getSelectedPojo();
			AddOrEditCDKeyDialog dialog = null;
			if ( selectedPojo != null ) {
				dialog = new AddOrEditCDKeyDialog(selectedPojo, true);
			} else {
				dialog = new AddOrEditCDKeyDialog();
			}
			dialog.setVisible(true);
			
			CDKeyPojo newRewardPojo = dialog.getRewardPojo();
			if ( newRewardPojo != null ) {
				MapDBObject objToSave = new MapDBObject();
				objToSave.putAll(newRewardPojo);
				model.insertRow(objToSave);
			}
		}
	}
}
