package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.config.PromotionPojo;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class AddOrEditPromotionDialog extends JDialog implements ActionListener {
	
	private JXTextArea tipFields = new JXTextArea();
	private JSpinner idField = new JSpinner();
	private JLabel channelLabel = new JLabel("渠道");
	private JLabel startLabel = new JLabel("开始日期");
	private JLabel endLabel = new JLabel("结束日期");
	private JXTextField channelField = new JXTextField();
	private JXDatePicker startField = new JXDatePicker();
	private JXDatePicker endField = new JXDatePicker();
	private JXButton okButton = new JXButton();
	private JXButton cancelButton = new JXButton();
	private PromotionPojo pojo = null;
	private boolean createNew = true;
	
	public AddOrEditPromotionDialog(PromotionPojo pojo, boolean createNew) {
		createNew = false;
		if ( pojo != null ) {
			this.pojo = new PromotionPojo();
			this.pojo.setChannel(pojo.getChannel());
			this.pojo.setEndMillis(pojo.getEndMillis());
			this.pojo.setId(pojo.getId());
			this.pojo.setMessage(pojo.getMessage());
			this.pojo.setStartMillis(pojo.getStartMillis());
		}
		init();
	}

	public void init() {
		this.okButton.setText("确定");
		this.okButton.setActionCommand(ActionName.OK.toString());
		this.okButton.addActionListener(this);
		this.cancelButton.setText("取消");
		this.cancelButton.setActionCommand(ActionName.CANCEL.toString());
		this.cancelButton.addActionListener(this);
		this.tipFields.setColumns(20);
		this.tipFields.setRows(20);
		
		if ( this.pojo != null ) {
			this.idField.setValue(this.pojo.getId());
			this.channelField.setText(this.pojo.getChannel());
			this.startField.setDate(new Date(this.pojo.getStartMillis()));
			this.endField.setDate(new Date(this.pojo.getEndMillis()));
			this.tipFields.setText(this.pojo.getMessage());
		}
		
		this.setLayout(new MigLayout("wrap 4"));
		this.setSize(400, 300);
		this.add(new JLabel("ID:"));
		this.add(idField, "width 10%, sg fd");
		this.add(channelLabel, "sg lbl");
		this.add(channelField, "sg fd");
		this.add(startLabel, "");
		this.add(startField, "sg fd");
		this.add(endLabel, "");
		this.add(endField, "sg fd");
		this.add(new JLabel("公告信息:"), "sg lbl");
		JScrollPane pane = new JScrollPane(tipFields, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(pane, "span, growx");
		this.add(okButton, "span, split 2, align center");
		this.add(cancelButton);
		
		this.setResizable(true);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		this.setModal(true);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if ( ActionName.OK.toString().equals(event.getActionCommand()) ) {
			if ( this.pojo == null ) {
				this.pojo = new PromotionPojo();
			}
			if ( StringUtil.checkNotEmpty(channelField.getText()) ) {
				this.pojo.setChannel(channelField.getText());
			} else {
				this.pojo.setChannel(null);	
			}
			this.pojo.setEndMillis(this.endField.getDate().getTime());
			this.pojo.setId((Integer)this.idField.getValue());
			this.pojo.setMessage(this.tipFields.getText());
			this.pojo.setStartMillis(this.startField.getDate().getTime());
			this.dispose();
		} else if ( ActionName.CANCEL.toString().equals(event.getActionCommand()) ) {
			this.pojo = null;
			this.dispose();
		}
	}
	
	public PromotionPojo getPromotionPojo() {
		return pojo;
	}
}
