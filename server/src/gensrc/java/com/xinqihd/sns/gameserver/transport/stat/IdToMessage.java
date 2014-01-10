package com.xinqihd.sns.gameserver.transport.stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.proto.stat.JavaCStatAction;
import com.xinqihd.sns.gameserver.proto.stat.JavaCStatLogin;

/**
 * GENERATED SOURCE CODE DO NOT MODIFY!
 * Translate the given int id to its coresponding message. 
 * @author wangqi 
 */ 
public class IdToMessage {

  private static Logger logger = LoggerFactory.getLogger(IdToMessage.class); 

  public static MessageLite idToMessage(int id) { 
    MessageLite message = null;
    switch(id) {
    case 20001: 
      message = JavaCStatAction.CStatAction.getDefaultInstance(); 
      break;
    case 20002: 
      message = JavaCStatLogin.CStatLogin.getDefaultInstance(); 
      break;
    default:
      logger.error("No message type for id: {}", id);
    }
    return message;
  }
}
