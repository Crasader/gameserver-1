package com.xinqihd.sns.gameserver.admin.gui.ext;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.xinqihd.sns.gameserver.admin.action.game.MongoAddDBObjectRowAction;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.gui.table.MongoDBObjectEditor;
import com.xinqihd.sns.gameserver.admin.gui.table.MyTableRowSorter;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

/**
 * 简化版本的MyTablePanel对象，适合于在嵌入其他界面时使用
 * @author wangqi
 *
 */
public class MyMiniTablePanel extends JXPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(MyMiniTablePanel.class);
	
	private MyTable table = new MyTable();
	private JLabel  titleLbl = new JLabel();
	private JPanel  buttonBarPanel = new JPanel();
	private JXButton addRowButton = new JXButton(ImageUtil.createImageSmallIcon("Button Add.png", "Add Row"));
	private JXButton delRowButton = new JXButton(new MyTableModelDeleteRowAction(table));
	
	private MyTableModel tableModel = null;
	
	public MyMiniTablePanel() {
		init();
	}
	
	public void setTableModel(MyTableModel model) {
		this.tableModel = model;
		this.table.setModel(model);
		MyTableRowSorter sorter = new MyTableRowSorter();
		sorter.setModel(this.tableModel);
		this.table.setRowSorter(sorter);
	}
	
	public MyTableModel getTableModel() {
		return this.tableModel;
	}
	
	public void setTitle(String title) {
		this.titleLbl.setText(title);
	}
	
	public void setEditable(boolean editable) {
		this.table.setEditable(editable);
		this.addRowButton.setEnabled(editable);
		this.delRowButton.setEnabled(editable);
	}
	
	public void setAddRowAction(Action addRowAction) {
		this.addRowButton.setAction(addRowAction);
	}
		
	public void setEnableAddRow(boolean enabled) {
		this.addRowButton.setEnabled(enabled);
	}
	
	public void setEnableDelRow(boolean enabled) {
		this.delRowButton.setEnabled(enabled);
	}

	public void setColumnEditor(int columnIndex, TableCellEditor editor) {
		this.table.getColumn(columnIndex).setCellEditor(editor);
	}
	
	public MyTable getTable() {
		return this.table;
	}
	
	/**
	 * Initialize method
	 */
	public void init() {
		this.setLayout(new MigLayout("ins 0, gap 0"));
		
		this.table.setEditable(true);
		this.table.setHighlighters(HighlighterFactory.createAlternateStriping());
		this.table.setDragEnabled(true);
		this.table.setHorizontalScrollEnabled(true);
		this.table.setColumnControlVisible(true);
		this.table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.table.setEditorFactory(new DefaultEditorFactory(this));
		this.table.packAll();
				
		this.addRowButton.setEnabled(true);
		this.addRowButton.setMaximumSize(new Dimension(36, 36));
		this.addRowButton.setToolTipText("增加一行记录");
		
		this.delRowButton.setEnabled(true);
		this.delRowButton.setMaximumSize(new Dimension(36, 36));
		this.delRowButton.setToolTipText("删除一行记录");
		
		
//		this.table.setPreferredSize(new Dimension(MainFrame.screenWidth, MainFrame.screenHeight));
		JScrollPane pane = new JScrollPane(table);
		
		buttonBarPanel.setLayout(new MigLayout("wrap 1, insets 5"));
		buttonBarPanel.add(addRowButton,  "");
		buttonBarPanel.add(delRowButton,  "");
		
		this.add(titleLbl, "dock north, width 15%");
		this.add(buttonBarPanel, "dock east, width 36px");
		this.add(pane, "dock center, height 100%, grow, push");
	}
	
	static class DefaultEditorFactory implements MyTableCellEditorFactory {

		private MyMiniTablePanel myTable = null;

		public DefaultEditorFactory(MyMiniTablePanel myTable) {
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
			return null;
		}

	}
}
