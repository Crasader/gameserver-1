package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.gui.AddDBObjectRowDialog;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTable;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

/**
 * Add DBObject as a new row.
 * @author wangqi
 *
 */
public class MongoAddDBObjectRowAction extends AbstractAction {
	
	private final MyTable table;

	public MongoAddDBObjectRowAction(MyTable table) {
		super("", ImageUtil.createImageSmallIcon("Button Add.png", "Add Row"));
		this.table = table;
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_addnew;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				int rowIndex = table.getSelectedRow();
				if ( rowIndex < 0 ) {
					if ( table.getModel().getRowCount()> 0 ) {
						rowIndex = 0;
					}
				}
				if ( rowIndex >= 0 ) {
					if ( table.getModel() instanceof MyTableModel ) {
						MyTableModel model = (MyTableModel)table.getModel();
						int modelRowIndex = table.convertRowIndexToModel(rowIndex);
						DBObject row = (DBObject)model.getRowObject(modelRowIndex);
						AddDBObjectRowDialog dialog = new AddDBObjectRowDialog(table);
						dialog.setPrototype(row);
						
//						Object objToSave = dialog.constructNewRowObject();
//						model.insertRow(objToSave);
					} else {
						JOptionPane.showMessageDialog(null, "您所使用的表不支持添加新行操作", 
								"添加新行", JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(null, "您需要选中一行做参照方能添加新的一行", "添加新行", 
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
	}

}
