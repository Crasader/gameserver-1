package com.xinqihd.payment;

import com.xinqihd.payment.yeepay.PaymentResult;

public class main 
{
	public static void main(String[] args)
	{
		IPaymentCard payment =  new PaymentCardYeepay("591001006736431","5907417563563260202",30);
		((PaymentCardYeepay)payment).setAmt(80);
		((PaymentCardYeepay)payment).setChannelID("JUNNET");
		((PaymentCardYeepay)payment).setOrderID(String.valueOf(System.currentTimeMillis()));
		PaymentResult result = payment.doPost("test");
		System.out.println("statusCode = " + result.statusCode);
		System.out.println("code = " + result.code);
		System.out.println("msg = " + result.msg);
	}
}
