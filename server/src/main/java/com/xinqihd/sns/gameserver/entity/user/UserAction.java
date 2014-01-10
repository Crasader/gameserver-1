package com.xinqihd.sns.gameserver.entity.user;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The user's action in game
 * @author wangqi
 *
 */
public class UserAction {

	//The i18n key
	private UserActionKey textKey = null;
	
	private String roleName = null;
	
	private String[] params = null;
	

	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * @param roleName the roleName to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * @return the textKey
	 */
	public UserActionKey getTextKey() {
		return textKey;
	}

	/**
	 * @param textKey the textKey to set
	 */
	public void setTextKey(UserActionKey textKey) {
		this.textKey = textKey;
	}

	/**
	 * @return the params
	 */
	public String[] getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(String[] params) {
		this.params = params;
	}
	
	/**
	 * Return the localized action message
	 * @return
	 */
	public String getLocalizedMessage() {
		String message = null;
		if ( params != null ) {
			Object[] totalParams = new String[params.length+1];
			totalParams[0] = roleName;
			System.arraycopy(params, 0, totalParams, 1, params.length);
			message = Text.text(textKey.toString(), totalParams);
		} else {
			Object[] totalParams = new String[1];
			totalParams[0] = roleName;
			message = Text.text(textKey.toString(), totalParams);
		}
		return message;
	}
	
	/**
	 * Convert this object to string
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(textKey).append(Constant.TAB);
		buf.append(roleName);
		if ( params != null ) {
			buf.append(Constant.TAB);
			for ( String param : params ) {
				buf.append(param).append(Constant.TAB);
			}
			buf.deleteCharAt(buf.length()-1);
		}
		return buf.toString();
	}
	
	/**
	 * Convert the string format to UserAction
	 * @param str
	 * @return
	 */
	public static UserAction fromString(String str) {
		if ( StringUtil.checkNotEmpty(str) ) {
			String[] fields = str.split(Constant.TAB);
			if ( fields.length>1 ) {
				UserAction userAction = new UserAction();
				userAction.setTextKey(UserActionKey.valueOf(fields[0]));
				userAction.setRoleName(fields[1]);
				if ( fields.length>2 ) {
					String[] params = new String[fields.length-1];
					for ( int i=1; i<fields.length; i++ ) {
						params[i-1] = fields[i];
					}
					userAction.setParams(params);
				}
				return userAction;
			}
		}
		return null;
	}
}
