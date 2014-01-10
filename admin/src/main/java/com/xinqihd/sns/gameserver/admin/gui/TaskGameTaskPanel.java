package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.task.AddOrEditTask;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskGameTaskPanel extends AbstractTaskPanel {
	
	private static TaskGameTaskPanel instance = new TaskGameTaskPanel(); 

	public TaskGameTaskPanel() {
		init();
	}
	
	public static TaskGameTaskPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		String collection = "tasks";
		super.initModel(collection);
		
		myTable.getTable().setColumnOrders(new String[]{"_id", "name", 
				"desc", "taskTarget", "userLevel", "seq", "level", "script"});
		
		hiddenFields.add("class");
		model.setHiddenFields(hiddenFields);
		
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("游戏任务数据");
		myTable.setTableModel(model);
		myTable.setAddRowAction(new AddTaskAction());
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		MapEditorRenderFactory factory = new MapEditorRenderFactory(this.myTable);
		myTable.getTable().setRenderFactory(factory);
//		myTable.getTable().setEditorFactory(factory);
		myTable.setBackupAction(backupAction);
		myTable.setEditable(true);
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
						TaskPojo selectedTask = null;
						DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
						selectedTask = (TaskPojo)MongoUtil.constructObject(dbObj);
						
						AddOrEditTask dialog = new AddOrEditTask(selectedTask, false);
						dialog.setVisible(true);
						
						selectedTask = dialog.getTaskPojo();
						if ( selectedTask != null ) {
							MapDBObject objToSave = new MapDBObject();
							objToSave.putAll(selectedTask);
							model.updateRow(objToSave, modelRowIndex);
						}
					}
				}
			}
			
		});
		this.add(myTable, "width 100%, height 100%");
	}
	
	
	static class MapEditorRenderFactory implements MyTableCellRenderFactory {

		private MyTablePanel myTable = null;

		public MapEditorRenderFactory(MyTablePanel myTable) {
			this.myTable = myTable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory#
		 * getCellRenderer(int, int, java.lang.String)
		 */
		@Override
		public TableCellRenderer getCellRenderer(int row, int column,
				String columnName, TableModel tableModel, JTable table) {
			return null;
		}
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable(){
			public void run() {
//				for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
//					if ( "Nimbus".equals(info.getName()) ) {
//						try {
//							UIManager.setLookAndFeel(info.getClassName());
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						break;
//					}
//				}
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(TaskGameTaskPanel.getInstance());
				frame.setSize(800, 800);
				frame.setVisible(true);
			}
		});
	}
	
	private class AddTaskAction extends AbstractAction {
		
		public AddTaskAction() {
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
				TaskPojo selectedTask = null;
				DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
				selectedTask = (TaskPojo)MongoUtil.constructObject(dbObj);
				
				AddOrEditTask dialog = new AddOrEditTask(selectedTask, true);
				dialog.setVisible(true);
				
				selectedTask = dialog.getTaskPojo();
				if ( selectedTask != null ) {
					MapDBObject objToSave = new MapDBObject();
					objToSave.putAll(selectedTask);
					model.insertRow(objToSave);
				}
			} else {
				AddOrEditTask dialog = new AddOrEditTask();
				dialog.setVisible(true);
				
				TaskPojo newTask = dialog.getTaskPojo();
				if ( newTask != null ) {
					MapDBObject objToSave = new MapDBObject();
					objToSave.putAll(newTask);
					model.insertRow(objToSave);
				}
			}
		}
	}
}
