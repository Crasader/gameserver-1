package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.gui.table.EquipAndItemIdEditor;
import com.xinqihd.sns.gameserver.admin.gui.table.IconCellEditor;
import com.xinqihd.sns.gameserver.admin.gui.table.IconCellRenderer;
import com.xinqihd.sns.gameserver.admin.gui.table.MongoDBObjectEditor;
import com.xinqihd.sns.gameserver.admin.item.AddOrEditRewardAction;
import com.xinqihd.sns.gameserver.admin.item.EditItemDialog;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskItemPanel extends AbstractTaskPanel {
	
	private static TaskItemPanel instance = new TaskItemPanel(); 

	private final String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName); 
	private final String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	private final String collection = "items";
	
	public TaskItemPanel() {
		init();
	}
	
	public static TaskItemPanel getInstance() {
		return instance;
	}

	public void init() {
		//创建工作区域
		super.initModel(collection);
		
		myTable.getTable().setColumnOrders(new String[]{"_id", "name", "icon", "info"});
		
		hiddenFields.add("class");
		hiddenFields.add("type");
		hiddenFields.add("equipType");
		hiddenFields.add("count");
		
		model.setHiddenFields(hiddenFields);
		
		RefreshMyTableService service = new RefreshMyTableService(model);
		service.execute();
		
		myTable.setTitle("游戏道具数据");
		myTable.setTableModel(model);
		myTable.setAddRowAction(new AddItemPojoAction());
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		ItemEditorRenderFactory factory = new ItemEditorRenderFactory(this.myTable);
		myTable.setEditable(true);
		myTable.getTable().setRenderFactory(factory);
		myTable.getTable().addMouseListener(new MouseAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if ( e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 2 ) {
					int rowIndex = myTable.getTable().getSelectedRow();
					if ( rowIndex >= 0 ) {
						int modelRowIndex = myTable.getTable().convertRowIndexToModel(rowIndex);
						ItemPojo selectedItemPojo = null;
						DBObject dbObj = (DBObject)myTable.getTableModel().getRowObject(modelRowIndex);
						selectedItemPojo = (ItemPojo)MongoUtil.constructObject(dbObj);
						
						EditItemDialog dialog = new EditItemDialog(selectedItemPojo);
						dialog.setVisible(true);
						
						selectedItemPojo = dialog.getSavedItemPojo();
						saveItemPojo(selectedItemPojo, modelRowIndex);
					}
				}
			}
			
		});
		myTable.setBackupAction(backupAction);
//		myTable.getTable().setEditorFactory(factory);
//		myTable.setColumnEditor(3, new GameDataEditor());
		this.add(myTable, "width 100%, height 100%");
	}
	
	/**
	 * 
	 * @param selectedItemPojo
	 */
	private void saveItemPojo(ItemPojo selectedItemPojo, int modelRowIndex) {
		if ( selectedItemPojo != null ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(selectedItemPojo);
			if ( modelRowIndex >= 0 ) {
				myTable.getTableModel().updateRow(dbObject, modelRowIndex);
			} else {
				myTable.getTableModel().insertRow(dbObject);
			}
		}
	}
	
	/**
	 * 道具管理界面中负责编辑各个字段的工厂实现
	 * @author wangqi
	 *
	 */
	public class ItemEditorRenderFactory implements MyTableCellEditorFactory, 
		MyTableCellRenderFactory {
		
		private MyTablePanel myTable = null;
		
		public ItemEditorRenderFactory(MyTablePanel myTable) {
			this.myTable = myTable;
		}

		/* (non-Javadoc)
		 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellRenderFactory#getCellRenderer(int, int, java.lang.String)
		 */
		@Override
		public TableCellRenderer getCellRenderer(int row, int column,
				String columnName, TableModel tableModel, JTable table) {
			if ( "icon".equals(columnName) ) {
				return new IconCellRenderer();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory#getCellEditor(int, int, java.lang.String)
		 */
		@Override
		public TableCellEditor getCellEditor(int row, int column, 
				String columnName, TableModel tableModel, JTable table) {
			return null;
		}
		
	}

	public class AddItemPojoAction extends AbstractAction {
		
		public AddItemPojoAction() {
			super("", ImageUtil.createImageSmallIcon("Button Add.png", "Add Row"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			EditItemDialog dialog = new EditItemDialog(null);
			dialog.setVisible(true);
			
			ItemPojo selectedItemPojo = dialog.getSavedItemPojo();
			saveItemPojo(selectedItemPojo, -1);
		}
	}
}
