package com.xinqihd.payment.mobage;

public abstract class MbgaProvider {

	public abstract String getRequestTokenEndpoint();

	public abstract String getAccessTokenEndpoint();

	public abstract String getRestfulHost();
}
