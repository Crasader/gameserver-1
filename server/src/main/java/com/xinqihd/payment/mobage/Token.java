package com.xinqihd.payment.mobage;

public class Token {
	private String token;
	private String secret;
	private String status;
	private String errorMessage;
	
	public void setToken(String token) {
		this.token = token;
	}
	public String getToken() {
		return token;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public String getSecret() {
		return secret;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
}
