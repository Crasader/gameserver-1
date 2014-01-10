package com.xinqihd.sns.gameserver.admin.gui.ext;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
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
public class AddRemoveComboList extends MyPanel implements ActionListener {
	
	private JXButton addButton = new JXButton(ImageUtil.createImageSmallIcon("Button Add.png", null));
	private JXButton removeButton = new JXButton(ImageUtil.createImageSmallIcon("Button Remove.png", null));
	private JXList list = new JXList();
	private JXComboBox field = null;
	ArrayListModel listModel = new ArrayListModel();
	
	public AddRemoveComboList(Object[] selectArrays) {
		this(null, selectArrays);
	}
	
	public AddRemoveComboList(Object[] values, Object[] selectArrays) {
		addButton.setSize(36, 36);
		removeButton.setSize(36, 36);
		this.addButton.setActionCommand(ActionName.ADD.name());
		this.addButton.addActionListener(this);
		this.removeButton.setActionCommand(ActionName.DELETE.name());
		this.removeButton.addActionListener(this);
		
		field = new JXComboBox(selectArrays);
		this.field.setActionCommand(ActionName.ADD.name());
		this.field.addActionListener(this);
		
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
			Object text = this.field.getSelectedItem();
			if ( text != null ) {
				listModel.insertRow(text.toString().trim());
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
	
	/**
	 * Change the list content.
	 * @param values
	 */
	public void changeValues(Object[] values) {
		this.listModel.clear();
		for ( int i=0; i<values.length; i++ ) {
			this.listModel.insertRow(values[i]);
		}
	}
	
	/**
	 * Change the list content.
	 * @param values
	 */
	public void changeValues(Collection values) {
		this.listModel.clear();
		for (Iterator iterator = values.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			this.listModel.insertRow(object);
		}
	}
}
