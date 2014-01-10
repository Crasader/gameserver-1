package com.xinqihd.sns.gameserver.admin.gui.ext;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.MainFrame;

public class SelectDialog extends MyDialog implements ActionListener {

	private JXComboBox list = new JXComboBox();
	private DefaultComboBoxModel listModel = new DefaultComboBoxModel();
	private JButton okButton = new JButton("确定");
	
	public SelectDialog(String title, Object[] values) {
		JLabel titleLbl = new JLabel(title);
		titleLbl.setFont(MainFrame.BIG_FONT);
		for ( int i=0; i<values.length; i++ ) {
			listModel.addElement(values[i]);
		}
		list.setModel(listModel);
		list.setEditable(false);
		
		okButton.setActionCommand(ActionName.OK.name());
		okButton.addActionListener(this);
		
		this.setLayout(new MigLayout("wrap 1, fill"));
		this.add(titleLbl, "width 100%");
		this.add(list, "width 100%, grow");
		this.add(okButton, "align center");
		this.setModal(true);
		this.setSize(300, 150);
		
		Point p = WindowUtils.getPointForCentering(this);
		this.setLocation(p);
	}
 
	/**
	 * 从数组中选择其中一项
	 * @param title
	 * @param values
	 * @return
	 */
	public static Object chooseSingleObject(String title, Object[] values) {
		SelectDialog dialog = new SelectDialog(title, values);
		dialog.setVisible(true);
		return dialog.list.getSelectedItem();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		this.dispose();
	}
	
}
