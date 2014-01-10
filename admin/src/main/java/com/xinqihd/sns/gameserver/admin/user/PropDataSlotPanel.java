package com.xinqihd.sns.gameserver.admin.user;

import java.awt.Dimension;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;

import com.xinqihd.sns.gameserver.admin.gui.ext.AddRemoveComboList;
import com.xinqihd.sns.gameserver.admin.model.ArrayListModel;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class PropDataSlotPanel extends JXPanel {

	private PropDataSlot slot = null;
	
	private JXLabel slotTypeLbl = new JXLabel("插槽类型");
	private JXLabel slotIdLbl = new JXLabel("石头ID");
	private JXLabel slotLevelLbl = new JXLabel("石头等级");
	private JXLabel slotValueLbl = new JXLabel("增加数值");
	private JXLabel availableLbl = new JXLabel("类型限定");
	
	private JXComboBox slotTypeFd = new JXComboBox(PropDataEnhanceField.values());
	private JXTextField slotIdFd = new JXTextField();
	private JSpinner slotLevelFd = new JSpinner();
	private JSpinner slotValueFd = new JSpinner();
	private AddRemoveComboList availableFd = new 
			AddRemoveComboList(PropDataEnhanceField.values());
	
	public PropDataSlotPanel(PropDataSlot slot) {
		this.slot = slot;
		if ( this.slot == null ) {
			this.slot = new PropDataSlot();
		}
		init();
	}
	
	public void init() {
		if ( this.slot.getStoneId() != null ) {
			this.slotIdFd.setText(this.slot.getStoneId());
		} else {
			this.slotIdFd.setText("");
		}
		if ( this.slot.getSlotType() != null ) {
			this.slotTypeFd.setSelectedItem(this.slot.getSlotType());
		} else {
			this.slotTypeFd.setSelectedIndex(0);
		}
		if ( this.slot.getValue() > 0  ) {
			this.slotValueFd.setValue(this.slot.getValue());
		} else {
			this.slotValueFd.setValue(0);
		}
		if ( this.slot.getStoneLevel() > 0  ) {
			this.slotLevelFd.setValue(this.slot.getStoneLevel());
		} else {
			this.slotLevelFd.setValue(0);
		}
		ArrayListModel model = this.availableFd.getListModel();
		HashSet<PropDataEnhanceField> set = this.slot.getAvailabeTypes();
		for ( PropDataEnhanceField field : set ) {
			model.insertRow(field.toString());
		}
		
		this.setLayout(new MigLayout("wrap 2"));
		this.add(slotTypeLbl, "");
		this.add(slotTypeFd,  "width 100%");
		this.add(slotIdLbl, "");
		this.add(slotIdFd,  "width 100%");
		this.add(slotLevelLbl, "");
		this.add(slotLevelFd,  "width 100%");
		this.add(slotValueLbl, "");
		this.add(slotValueFd,  "width 100%");
		this.add(availableLbl, "");
		this.add(availableFd,  "width 100%");
		this.setBorder(BorderFactory.createEtchedBorder());
		this.setPreferredSize(new Dimension(200, 200));
	}
	
	public PropDataSlot getPropDataSlot() {
		this.slot.setSlotType((PropDataEnhanceField)slotTypeFd.getSelectedItem());
		if ( StringUtil.checkNotEmpty(this.slotIdFd.getText()) ) {
			this.slot.setStoneId(this.slotIdFd.getText());
		} else {
			this.slot.setStoneId(null);
		}
		this.slot.setSlotType((PropDataEnhanceField)this.slotTypeFd.getSelectedItem());
		this.slot.setStoneLevel((Integer)this.slotLevelFd.getValue());
		this.slot.setValue((Integer)this.slotValueFd.getValue());
		ArrayListModel model = this.availableFd.getListModel();
		HashSet<PropDataEnhanceField> set = new HashSet<PropDataEnhanceField>();
		for ( int i=0; i<model.getSize(); i++) {
			set.add(PropDataEnhanceField.valueOf((String)model.getElementAt(i)));
		}
		this.slot.setAvailabeTypes(set);
		return this.slot;
	}
}
