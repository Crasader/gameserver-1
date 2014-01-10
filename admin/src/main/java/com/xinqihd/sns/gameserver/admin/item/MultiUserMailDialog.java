package com.xinqihd.sns.gameserver.admin.item;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.EscapeAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.AddRemoveList;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;

public class MultiUserMailDialog extends JDialog implements ActionListener, ListSelectionListener {
	
	private JXPanel panel = new JXPanel();
	
	private JXLabel rolenameLabel = new JXLabel("玩家昵称");
	private JXLabel subjectLabel = new JXLabel("邮件主题");
	private JXLabel contentLabel = new JXLabel("邮件内容");
	
	private AddRemoveList rolenameField = new AddRemoveList();
	private JXTextArea subjectField = new JXTextArea();
	private JXTextArea contentField = new JXTextArea();
	private JXList rewardField = new JXList();
	
	private JXButton   checkButton = new JXButton("检查昵称");
	private JXButton   okButton = new JXButton("确定");
	private JXButton   cancelButton = new JXButton("取消");
	
	private static final String COMMAND_CHECK = "check";
	private static final String COMMAND_OK = "ok";
	private static final String COMMAND_CANCEL = "cancel";
	

	public MultiUserMailDialog(String title) {
		this(title, null);
	}
	
	public MultiUserMailDialog(String title, Reward reward) {
		setResizable(false);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		init();
	}
	
	public void init() {
		this.setSize(650, 600);
		Point point = WindowUtils.getPointForCentering(this.getOwner());
		this.setLocation(point);
		this.setResizable(true);
		
		this.subjectField.setColumns(100);
		this.contentField.setColumns(100);
		this.contentField.setRows(20);
		this.checkButton.setActionCommand(COMMAND_CHECK);
		this.checkButton.addActionListener(this);
		this.okButton.setActionCommand(COMMAND_OK);
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(COMMAND_CANCEL);
		this.cancelButton.addActionListener(this);
		
		this.panel.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
		this.panel.getActionMap().put("escape", new EscapeAction());
				
	  //layout
		panel.setLayout(new MigLayout("wrap 2, width 100%, gap 10px", "[25%][25%][25%][25%]"));
		panel.setBorder(BorderFactory.createEtchedBorder());

		panel.add(rolenameLabel, "");
		panel.add(rolenameField, "span, growx");
		panel.add(subjectLabel, "");
		panel.add(subjectField, "span, growx");
		panel.add(contentLabel, "");
		panel.add(contentField, "span, growx");
		panel.add(checkButton);
		
		panel.add(okButton, "newline, gaptop 40px, span, split 2, align center");
		panel.add(cancelButton);
		
		JXPanel contentPanel = new JXPanel();
		contentPanel.setLayout(new MigLayout("wrap 2, width 100%, height 100%"));
		contentPanel.add(panel, "width 60%, height 100%, grow");
		
		getContentPane().add(contentPanel);
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand() == COMMAND_OK ) {
			this.dispose();
		} else if ( e.getActionCommand() == COMMAND_CANCEL ) {
			this.dispose();
		} else if ( e.getActionCommand() == COMMAND_CHECK ) {	
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
