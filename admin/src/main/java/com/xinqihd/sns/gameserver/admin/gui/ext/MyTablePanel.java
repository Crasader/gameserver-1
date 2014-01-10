package com.xinqihd.sns.gameserver.admin.gui.ext;

import java.awt.Dimension;
import java.awt.event.InputEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.action.CopyMongoCollectionAction;
import com.xinqihd.sns.gameserver.admin.action.game.MongoAddDBObjectRowAction;
import com.xinqihd.sns.gameserver.admin.action.game.MongoExportAction;
import com.xinqihd.sns.gameserver.admin.action.game.MongoRowFilterAction;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableRefreshAction;
import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.gui.table.MongoDBObjectEditor;
import com.xinqihd.sns.gameserver.admin.gui.table.MyTableRowSorter;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.undo.MyUndoManager;
import com.xinqihd.sns.gameserver.admin.undo.RedoAction;
import com.xinqihd.sns.gameserver.admin.undo.UndoAction;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class MyTablePanel extends JXPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(MyTablePanel.class);
	private MyUndoManager undoManager = new MyUndoManager();
	
	private MyTable table = new MyTable();
	private String title = "";
	private TitledBorder titleBorder = BorderFactory.createTitledBorder(title);
	private JPanel  buttonBarPanel = new JPanel();
	private UndoAction undoAction = new UndoAction(undoManager);
	private RedoAction redoAction = new RedoAction(undoManager);
	private JXButton undoButton = new JXButton(undoAction);
	private JXButton redoButton = new JXButton(redoAction);
	private JXButton addRowButton = new JXButton(new MongoAddDBObjectRowAction(table));
	private JXButton delRowButton = new JXButton(new MyTableModelDeleteRowAction(table));
	private MyTableRefreshAction refreshAction = new MyTableRefreshAction(this);
	private JXButton refreshButton = new JXButton(refreshAction);
	private JXButton exportButton = new JXButton();
	private JXButton saveButton = new JXButton(ImageUtil.createImageSmallIcon("Folder.png", "保存设置"));
	private JXTextField filterField = new JXTextField("输入表达式可以过滤表格结果");
	
	/**
	 * It is used to make a backup of original data.
	 */
	private CopyMongoCollectionAction backupAction = null;
	
	private MyTableModel tableModel = null;
	
	public MyTablePanel() {
		init();
	}
	
	public void setTableModel(MyTableModel model) {
		this.tableModel = model;
		this.tableModel.setUndoManager(undoManager);
//		DefaultTableModel defaultModel = new DefaultTableModel(new Object[][]{
//				{"key1", "value1"},
//				{"key2", "value2"},
//		}, new Object[]{"col1", "col2"});
//		this.table.setModel(defaultModel);
		this.table.setModel(model);
		MyTableRowSorter sorter = new MyTableRowSorter();
		sorter.setModel(this.tableModel);
		this.table.setRowSorter(sorter);
		logger.debug("colCount:"+model.getColumnCount()+",rowCount:"+model.getRowCount());
		MainFrame.getMainPanel().getStatusBar().updateStatus("工作区:"+
				model.getCollectionName());
	}
	
	public MyTableModel getTableModel() {
		return this.tableModel;
	}
	
	public void setTitle(String title) {
		this.title = title;
		this.titleBorder.setTitle(title);
	}
	
	public void setEditable(boolean editable) {
		this.table.setEditable(editable);
	}
	
	public void setAddRowAction(Action addRowAction) {
		this.addRowButton.setAction(addRowAction);
	}
	
	public void setDelRowAction(Action deleteRowAction) {
		this.delRowButton.setAction(deleteRowAction);
	}
	
	public void setExportAction(Action exportAction) {
		this.exportButton.setAction(exportAction);
	}
	
	public void setEnableRrefresh(boolean enabled) {
		this.refreshButton.setEnabled(enabled);
	}
	
	public void setEnableAddRow(boolean enabled) {
		this.addRowButton.setEnabled(enabled);
	}
	
	public void setEnableDelRow(boolean enabled) {
		this.delRowButton.setEnabled(enabled);
	}
	
	public void setRefreshAction(Action refreshAction) {
		this.refreshButton.setAction(refreshAction);
	}
	
	public void setColumnEditor(int columnIndex, TableCellEditor editor) {
		this.table.getColumn(columnIndex).setCellEditor(editor);
	}
	
	public String getFilterText() {
		return this.filterField.getText();
	}
	
	public MyTable getTable() {
		return this.table;
	}
	
	public void setEnableSaveButton(boolean enabled) {
		this.saveButton.setEnabled(enabled);
	}
	
	public void setSaveButtonAction(Action action) {
		this.saveButton.setAction(action);
	}
	
	/**
	 * @return the backupAction
	 */
	public CopyMongoCollectionAction getBackupAction() {
		return backupAction;
	}

	/**
	 * @param backupAction the backupAction to set
	 */
	public void setBackupAction(CopyMongoCollectionAction backupAction) {
		this.backupAction = backupAction;
		this.refreshAction.setBackupAction(backupAction);
	}

	/**
	 * Initialize method
	 */
	public void init() {
		this.setLayout(new MigLayout("ins 0, gap 0"));
		
		this.titleBorder.setTitleFont(MainFrame.BIG_FONT);
		this.setBorder(titleBorder);
		this.table.setEditable(false);
		this.table.setHighlighters(HighlighterFactory.createAlternateStriping());
		this.table.setDragEnabled(true);
		this.table.setHorizontalScrollEnabled(true);
		this.table.setColumnControlVisible(true);
		this.table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.table.setEditorFactory(new DefaultEditorFactory(this));
		this.table.packAll();
		
		this.filterField.setColumns(10);
		this.filterField.setAction(new MongoRowFilterAction(this));
		
		this.undoButton.setMaximumSize(new Dimension(36, 36));
		this.undoButton.setToolTipText("撤销上一步操作");
		
		this.redoButton.setMaximumSize(new Dimension(36, 36));
		this.redoButton.setToolTipText("重做上一步操作");
		
		this.addRowButton.setEnabled(true);
		this.addRowButton.setMaximumSize(new Dimension(36, 36));
		this.addRowButton.setToolTipText("增加一行记录");
		
		this.delRowButton.setEnabled(false);
		this.delRowButton.setMaximumSize(new Dimension(36, 36));
		this.delRowButton.setToolTipText("删除一行记录");
		
		this.refreshButton.setEnabled(true);
		this.refreshButton.setMaximumSize(new Dimension(36, 36));
		this.refreshButton.setToolTipText("刷新数据");
		
		this.exportButton.setAction(new MongoExportAction(this));
		this.exportButton.setMaximumSize(new Dimension(36, 36));
		this.exportButton.setToolTipText("导出为CSV文件");
		
		this.saveButton.setEnabled(false);
		this.saveButton.setMaximumSize(new Dimension(36, 36));
		this.saveButton.setToolTipText("保存临时工作区的内容");
		
//		this.table.setPreferredSize(new Dimension(MainFrame.screenWidth, MainFrame.screenHeight));
		JScrollPane pane = new JScrollPane(table);
		
		buttonBarPanel.setLayout(new MigLayout("wrap 8, align right, insets 5"));
//		buttonBarPanel.setMaximumSize(new Dimension(500, 40));
		buttonBarPanel.add(undoButton,    "");
		buttonBarPanel.add(redoButton,    "");
		buttonBarPanel.add(addRowButton,  "");
		buttonBarPanel.add(delRowButton,  "");
		buttonBarPanel.add(refreshButton, "");
		buttonBarPanel.add(exportButton, 	"");
		buttonBarPanel.add(saveButton,    "");
		buttonBarPanel.add(filterField,   "height 36px");
		
		this.add(buttonBarPanel, "wrap, align right, shrink, height 50px");
		this.add(pane, "span, width 100%, height 100%");
		
		//Register key map
		this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke('z', InputEvent.CTRL_DOWN_MASK), ActionName.UNDO);
		this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke('r', InputEvent.CTRL_DOWN_MASK), ActionName.REDO);
		this.getActionMap().put(ActionName.UNDO, undoAction);
		this.getActionMap().put(ActionName.REDO, redoAction);
	}
	
	static class DefaultEditorFactory implements MyTableCellEditorFactory {

		private MyTablePanel myTable = null;

		public DefaultEditorFactory(MyTablePanel myTable) {
			this.myTable = myTable;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory#
		 * getCellEditor(int, int, java.lang.String, javax.swing.table.TableModel,
		 * javax.swing.JTable)
		 */
		@Override
		public TableCellEditor getCellEditor(int row, int column,
				String columnName, TableModel tableModel, JTable table) {
			Object value = table.getValueAt(row, column);
			if ( value instanceof BasicDBObject ) {
				MongoDBObjectEditor editor = new MongoDBObjectEditor();
				return editor;
			}
			return null;
		}

	}
}
