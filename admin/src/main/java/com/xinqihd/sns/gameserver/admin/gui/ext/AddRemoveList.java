package com.xinqihd.sns.gameserver.admin.gui.ext;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.model.ArrayListModel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

/**
 * 可以向List中添加或者删除或者修改。
 * @author wangqi
 *
 */
public class AddRemoveList extends MyPanel implements ActionListener {
	
	private JXButton addButton = new JXButton(ImageUtil.createImageSmallIcon("Button Add.png", null));
	private JXButton removeButton = new JXButton(ImageUtil.createImageSmallIcon("Button Remove.png", null));
	private JXList list = new JXList();
	private JTextField field = new JTextField(15);
	ArrayListModel listModel = new ArrayListModel();
	
	public AddRemoveList() {
		this(null);
	}
	
	public AddRemoveList(Object[] values) {
		addButton.setSize(36, 36);
		removeButton.setSize(36, 36);
		this.addButton.setActionCommand(ActionName.ADD.name());
		this.addButton.addActionListener(this);
		this.removeButton.setActionCommand(ActionName.DELETE.name());
		this.removeButton.addActionListener(this);
		this.field.setActionCommand(ActionName.ADD.name());
		this.field.addActionListener(this);
		this.field.setToolTipText("可直接输入逗号或者顿号分割的多项内容");
		
		list.setHighlighters(HighlighterFactory.createAlternateStriping());
		if ( values != null ) {
			for ( Object o : values ) {
				listModel.insertRow(o);
			}
		}
		list.setModel(listModel);
		
		JScrollPane pane = new JScrollPane(list);
		this.setLayout(new MigLayout("wrap 2"));
		this.add(pane, "spany 2, width 80%");
		this.add(addButton, "top");
		this.add(removeButton, "top");
		this.add(field, "newline, span, width 100%");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.ADD.name().equals(e.getActionCommand()) ) {
			String text = this.field.getText();
			if ( text != null ) {
				String[] fields = text.split(",|、");
				if ( text.indexOf(',') >= 0 ) {
					for ( String f : fields ) {
						listModel.insertRow(f.trim());
					}
				} else {
					listModel.insertRow(text.trim());
				}
				this.field.selectAll();
			}
		} else if ( ActionName.DELETE.name().equals(e.getActionCommand()) ) {
			int[] indices = this.list.getSelectedIndices();
			for ( int i=indices.length-1; i>=0; i-- ) {
				listModel.deleteRow(indices[i]);
			}
		}
	}
	
	public ArrayListModel getListModel() {
		return this.listModel;
	}
}
