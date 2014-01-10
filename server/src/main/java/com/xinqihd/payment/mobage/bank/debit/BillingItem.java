package com.xinqihd.payment.mobage.bank.debit;

import com.xinqihd.payment.mobage.bank.ItemData;

public class BillingItem {
	private ItemData item;
	private Integer quantity;
	
	public void setItem(ItemData item) {
		this.item = item;
	}
	public ItemData getItem() {
		return item;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getQuantity() {
		return quantity;
	}
	
}