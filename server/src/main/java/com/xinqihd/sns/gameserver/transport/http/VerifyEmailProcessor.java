package com.xinqihd.sns.gameserver.transport.http;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.mongo.EmailManager;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

public class VerifyEmailProcessor implements HttpProcessor {
	
	private static String locationHeader = "Location:";

	private static VerifyEmailProcessor instance = new VerifyEmailProcessor();
	
	public static VerifyEmailProcessor getInstance() {
		return instance;
	}

	@Override
	public void process(String uri, String variable, HttpMessage httpMessage) {
		String userIdStr = variable;
		boolean verified = EmailManager.getInstance().verifyEmail(userIdStr);
		String officialSite = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.official_site);
		if ( !StringUtil.checkNotEmpty(officialSite) ) {
			officialSite = "http://www.xinqihd.com";
		}
		if ( verified ) {
			String text = Text.text("email.verified", officialSite);
			httpMessage.setResponseContentType(HttpGameHandler.CONTENT_TYPE);
			httpMessage.setResponseContent(text.getBytes(Constant.charset));
			httpMessage.addHeader(StringUtil.concat(locationHeader, officialSite).getBytes());
		} else {
			String text = Text.text("email.notverified", officialSite);
			httpMessage.setResponseContentType(HttpGameHandler.CONTENT_TYPE);
			httpMessage.setResponseContent(text.getBytes(Constant.charset));
			httpMessage.addHeader(StringUtil.concat(locationHeader, officialSite).getBytes());
		}
	}

}
