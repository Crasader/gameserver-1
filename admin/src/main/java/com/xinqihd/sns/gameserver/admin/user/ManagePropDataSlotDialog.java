package com.xinqihd.sns.gameserver.admin.user;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JDialog;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;

public class ManagePropDataSlotDialog extends JDialog implements ActionListener {
	
	private PropData propData = null;
	private ArrayList<PropDataSlot> slots = new ArrayList<PropDataSlot>();
	private ArrayList<PropDataSlotPanel> panels = new ArrayList<PropDataSlotPanel>();
	
	private JButton okButton = new JButton("确定");
	private JButton cancelButton = new JButton("取消");
	
	public ManagePropDataSlotDialog(PropData propData, int size) {
		this.propData = propData;
		slots.addAll(propData.getSlots());
		if ( size <= 0 ) {
			this.propData.setSlots(slots);
		} else {
			/**
			 * 删除多余的插槽
			 */
			if ( size < slots.size() ) {
				for ( int j=slots.size()-1; j>=size; j-- ) {
					slots.remove(j);
				}
			}
			for ( PropDataSlot slot : slots ) {
				panels.add(new PropDataSlotPanel(slot));
			}
			if ( size > slots.size() ) {
				/**
				 * 添加不足的插槽
				 */
				for ( int j=slots.size(); j<size; j++ ) {
					PropDataSlot slot = new PropDataSlot();
					slots.add(slot);
					panels.add(new PropDataSlotPanel(slot));
				}
			}
		}
		init();
	}
	
	
	public void init() {
		this.setSize(600, 400);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		
		this.setLayout(new MigLayout("wrap 3"));
		for ( PropDataSlotPanel panel : panels ) {
			this.add(panel, "");
		}
		
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		JXPanel btnPanel = new JXPanel();
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);
		
		this.add(btnPanel, "dock south, width 100%");
		this.pack();
		
		this.setModal(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			ArrayList<PropDataSlot> slots = new ArrayList<PropDataSlot>(); 
			for ( PropDataSlotPanel panel : panels ) {
				slots.add(panel.getPropDataSlot());
			}
			this.propData.setSlots(slots);
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.dispose();
		}
	}
	
}
