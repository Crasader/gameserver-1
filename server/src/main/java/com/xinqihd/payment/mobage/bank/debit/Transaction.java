package com.xinqihd.payment.mobage.bank.debit;

import java.util.List;

public class Transaction {
	private List<BillingItem> items;
	private String comment;
	private String state;
	
	public void setItems(List<BillingItem> items) {
		this.items = items;
	}

	public List<BillingItem> getItems() {
		return items;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}
}
