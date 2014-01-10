package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.util.WindowUtils;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor;
import com.xinqihd.sns.gameserver.admin.model.EquipAndItemDataModel;
import com.xinqihd.sns.gameserver.service.RefreshMyTableService;

/**
 * 用来从下拉列表中选择装备或者道具
 * @author wangqi
 *
 */
public class EquipAndItemIdEditor extends MyAbstractCellEditor {
	
	private static final String[] columns = {"_id", "name", "icon"};
	private static final String COMMAND_OK = "ok";
	private static final String COMMAND_CANCEL = "cancel";
	
	private Object cellValue = null;
	private JXList list = new JXList();
	private JDialog dialog  = new JDialog();
	private JXButton okButton = new JXButton("确定");
	private JXButton cancelButton = new JXButton("取消");
	private boolean displayItems = true;
	private String selectedId = null;
	private JTable table = null;
	private int row;
	private int column;
	
	public EquipAndItemIdEditor(boolean displayItems) {
		this.displayItems = displayItems;
	}

	@Override
	public Object getCellEditorValue() {
		return cellValue;
	}

	@Override
	public Component getTableCellEditorComponentAtModel(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.cellValue = value;
		this.table = table;
		this.row = row;
		this.column = column;
		
		boolean isItemList = false;
		if ( displayItems ) {
			isItemList = true;
		}
		final EquipAndItemDataModel idModel = new EquipAndItemDataModel(isItemList);
		
		list.setCellRenderer(new EquipAndItemRenderer());
		list.setModel(idModel);
		
		okButton.setActionCommand(COMMAND_OK);
		okButton.addActionListener(this);
		cancelButton.setActionCommand(COMMAND_CANCEL);
		cancelButton.addActionListener(this);
		
		//Layout
		this.dialog.setLayout(new MigLayout("wrap 1"));
		this.dialog.add(new JScrollPane(list), "width 100%, height 90%, grow");
		this.dialog.add(okButton, "split 2, align center");
		this.dialog.add(cancelButton, "");
		this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.dialog.setSize(300, 500);
		Point point = WindowUtils.getPointForCentering(this.dialog);
		this.dialog.setLocation(point);
		this.dialog.setModal(true);
		this.dialog.setVisible(true);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand() == COMMAND_OK ) {
			this.dialog.dispose();
			DBObject dbObj = (DBObject)this.list.getSelectedValue();
			if ( dbObj != null ) {
				this.selectedId = String.valueOf(dbObj.get("_id"));
				this.table.getModel().setValueAt(this.selectedId, this.row, this.column);
			}
		} else if ( e.getActionCommand() == COMMAND_CANCEL ) {
			this.dialog.dispose();
			this.selectedId = null;
		}
	}
	
	public String getSelectedId() {
		return this.selectedId;
	}
}
