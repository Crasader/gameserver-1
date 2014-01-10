package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyTable;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class MyTableModelDeleteRowAction extends AbstractAction {
	
	private final MyTable table;

	public MyTableModelDeleteRowAction(MyTable table) {
		super("", ImageUtil.createImageSmallIcon("Button Remove.png", "Delete"));
		this.table = table;
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_delete;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				int rowCount = table.getSelectedRowCount();
				if ( rowCount == 0 ) {
					JOptionPane.showMessageDialog(table, 
							"您还没有选择任何行", "删除提示", 
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					int option = JOptionPane.showConfirmDialog(table, 
						"是否确定删除选中的"+rowCount+"行?", "删除提示", 
						JOptionPane.YES_NO_OPTION);
					if ( option == JOptionPane.YES_OPTION ) {
						for ( int i=0; i<rowCount; i++ ) {
							int[] rows = table.getSelectedRows();
							if ( rows.length > 0 ) {
								int modelIndex = table.convertRowIndexToModel(rows[0]);
								if ( table.getModel() instanceof MyTableModel ) {
									((MyTableModel)table.getModel()).deleteRow(modelIndex);
								} else {
									JOptionPane.showMessageDialog(null, "您所使用的表不支持删除行操作", 
											"删除行", JOptionPane.WARNING_MESSAGE);
								}
							}
						}
					}
				}
			}
		});
	}

}
