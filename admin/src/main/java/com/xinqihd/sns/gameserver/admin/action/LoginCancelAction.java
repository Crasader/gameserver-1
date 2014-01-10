package com.xinqihd.sns.gameserver.admin.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

/**
 * User press 'Cancel' button when login.
 * @author wangqi
 *
 */
public class LoginCancelAction extends AbstractAction {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginCancelAction.class);

	public LoginCancelAction() {
		super("取消", ImageUtil.createImageIcon("ButtonDelete.png", "Button Delete"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		logger.debug("Login Cancel is pressed");
		System.exit(0);
	}

}
