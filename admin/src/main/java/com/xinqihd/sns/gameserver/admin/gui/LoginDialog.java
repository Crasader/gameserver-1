package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

import com.xinqihd.sns.gameserver.admin.action.LoginCancelAction;
import com.xinqihd.sns.gameserver.admin.action.LoginOKAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class LoginDialog extends MyDialog {

	private static LoginDialog instance = null;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JPasswordField passwordField;
	private LoginOKAction loginOK = new LoginOKAction();;
	private LoginCancelAction loginCancel = new LoginCancelAction();
	private JButton okButton = null;
	private JLabel resultLabel = new JLabel(" ");
	private JComboBox mongoServer = new JComboBox();

	/**
	 * Create the dialog.
	 */
	public LoginDialog() {
		this.setModal(true);
		this.setBounds(100, 100, 400, 350);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.setDefaultLookAndFeelDecorated(false);
		this.setResizable(false);
		this.addWindowListener(new WindowAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("windowClosing");
				loginCancel.actionPerformed(null);
			}
			
		});
		getContentPane().setLayout(new MigLayout("wrap 2", "[15%][grow,fill]", "[][][][][][][]"));
		{
			JLabel lblNewLabel = new JLabel(ImageUtil.createImageIconFromImg(
					"login_title.png", "Login Title"));
			lblNewLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			getContentPane().add(lblNewLabel, "span 2,gapx 5 5,gapy 5 5");
		}
		{
			resultLabel.setForeground(Color.RED);
			resultLabel.setHorizontalAlignment(JLabel.CENTER);
			getContentPane().add(resultLabel, "span 2, height 15, growx");
		}
		{
			JLabel label = new JLabel("用户名:");
			label.setHorizontalAlignment(JLabel.RIGHT);
			getContentPane().add(label, "alignx trailing");
		}
		{
			textField = new JTextField();
			String username = ConfigManager.getConfigAsString(ConfigKey.adminUsername);
			if ( username != null ) {
				textField.setText(username);
			}
			textField.setColumns(10);
			getContentPane().add(textField, "growx");
		}
		{
			JLabel label = new JLabel("密码:");
			label.setHorizontalAlignment(JLabel.RIGHT);
			getContentPane().add(label, "alignx trailing");
		}
		{
			passwordField = new JPasswordField();
			passwordField.setColumns(10);
			String password = ConfigManager.getConfigAsString(ConfigKey.adminPassword);
			if ( password != null ) {
				passwordField.setText(password);
			}
			getContentPane().add(passwordField, "growx");
		}
		{
			JLabel label = new JLabel("管理数据库:");
			label.setHorizontalAlignment(JLabel.RIGHT);
			getContentPane().add(label, "alignx trailing");
		}
		{
			String mongodb = ConfigManager.getConfigAsString(ConfigKey.adminDatabaseServer);
			if ( mongodb != null ) {
				ComboBoxModel model = new DefaultComboBoxModel(new Object[]{mongodb, "mongocfg.babywar.xinqihd.com"});
				mongoServer.setModel(model);
			}
			mongoServer.setMaximumRowCount(10);
			mongoServer.setEditable(true);
			getContentPane().add(mongoServer, "growx");
		}
		{
			okButton = new JButton(loginOK);
			getRootPane().setDefaultButton(okButton);
			getContentPane().add(okButton, "span 2, split 2, alignx center");
		}
		{
			JButton cancelButton = new JButton(loginCancel);
			cancelButton.setActionCommand("Cancel");
			getContentPane().add(cancelButton, "alignx center");
		}
		this.instance = this;
	}
	
	public static LoginDialog getInstance() {
		return instance;
	}

	/**
	 * Get the login result
	 * @return
	 */
	public boolean getLoginResult() {
		return loginOK.getLoginResult();
	}
	
	public JButton getOKButton() {
		return okButton;
	}
	
	public JLabel getResultLabel() {
		return resultLabel;
	}
	
	public String getUsername() {
		return textField.getText(); 
	}
	
	public String getPassword() {
		return passwordField.getText();
	}
	
	public String getMongoServer() {
		return mongoServer.getSelectedItem().toString();
	}
}
