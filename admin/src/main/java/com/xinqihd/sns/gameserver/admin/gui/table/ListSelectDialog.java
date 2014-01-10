package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;

/**
 * 用于在一个列表中选择指定的项目，用于IconCellEditor
 * @author wangqi
 *
 */
public class ListSelectDialog extends MyDialog implements ActionListener {
	
	private final JXList list = new JXList();
	private DefaultListModel listModel;
	
	public ListSelectDialog(DefaultListModel listModel, ListCellRenderer cellRender) {
		this.listModel = listModel;
		
		list.setModel(listModel);
		list.setCellRenderer(cellRender);
		list.setSortable(true);
		list.setRolloverEnabled(true);
		list.addHighlighter(HighlighterFactory.createAlternateStriping());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane pane = new JScrollPane(list);
		
		this.setMinimumSize(new Dimension(250, 400));
		this.setModal(true);
		this.setLayout(new MigLayout("wrap 1"));
		this.add(pane, "width 100%, height 85%, grow");
		this.add(new JXLabel("用Ctrl-F可以搜索列表"));
		JXButton okButton = new JXButton("确定");
		okButton.addActionListener(this);
		this.add(okButton, "align center");
		this.setLocation((MainFrame.screenWidth-200)/2, (MainFrame.screenHeight-400)/2);
		this.setVisible(true);
	}
	
	public JXList getList() {
		return list;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		this.dispose();
	}

}
