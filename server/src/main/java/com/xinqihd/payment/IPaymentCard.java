package com.xinqihd.payment;

import com.xinqihd.payment.yeepay.PaymentResult;

public interface IPaymentCard 
{  
  public PaymentResult doPost(String userName);
  
}