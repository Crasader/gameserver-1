package com.xinqihd.sns.gameserver.admin.action;

import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.LoginDialog;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.service.LoginService;

/**
 * User press 'OK' button when login
 * @author wangqi
 *
 */
public class LoginOKAction extends AbstractAction {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginOKAction.class);
	
	private LoginService loginService = null;
	private String username = null;
	private String password = null;
	
	public LoginOKAction() {
		super("确定", ImageUtil.createImageIcon("ButtonCheck.png", "Button Check"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		LoginDialog.getInstance().getOKButton().setEnabled(false);
		LoginDialog.getInstance().getOKButton().setText("登陆中...");
		username = LoginDialog.getInstance().getUsername();
		password = LoginDialog.getInstance().getPassword();
		loginService = new LoginService();
		loginService.execute();
		logger.debug("Login OK is pressed");
	}

	public boolean getLoginResult() {
		if ( loginService != null ) {
			try {
				return loginService.get();
			} catch (Exception e) {
				logger.warn("LoginService fail. Exception", e);
			}
		}
		return false;
	}
}
