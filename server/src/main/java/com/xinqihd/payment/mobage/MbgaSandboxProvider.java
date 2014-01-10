package com.xinqihd.payment.mobage;

public class MbgaSandboxProvider extends MbgaProvider {
	
	private static final String requestTokenEndpoint = "http://sp.sb.mobage-platform.cn/social/api/oauth/v2/request_temporary_credential";
	private static final String accessTokenEndpoint = "http://sp.sb.mobage-platform.cn/social/api/oauth/v2/request_token";
	private static final String restfulHost = "http://sp.sb.mobage-platform.cn/social/api/restful/v2";

	@Override
	public String getRequestTokenEndpoint() {
		return requestTokenEndpoint;
	}
	
	@Override
	public String getAccessTokenEndpoint() {
		return accessTokenEndpoint;
	}

	@Override
	public String getRestfulHost() {
		return restfulHost;
	}
}
