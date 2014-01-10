package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultRowSorter;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class MongoRowFilterAction  extends AbstractAction {
	
	private final MyTablePanel parent;

	public MongoRowFilterAction(MyTablePanel parent) {
		super("", ImageUtil.createImageSmallIcon("Drive Download.png", "Download"));
		this.parent = parent;
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_export;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				DefaultRowSorter rowSorter = (DefaultRowSorter)parent.getTable().getRowSorter();
				String regex = parent.getFilterText();
				if ( regex == null || regex.length() == 0 ) {
//					JOptionPane.showMessageDialog(parent, "请输入需要过滤的正则表达式", 
//							"数据过滤", JOptionPane.INFORMATION_MESSAGE);
					rowSorter.setRowFilter(null);
				} else {
					rowSorter.setRowFilter(RowFilter.regexFilter(regex));
//					parent.getTable().setRowFilter(RowFilter.regexFilter(regex));
				}
			}
		});
	}
	
}
