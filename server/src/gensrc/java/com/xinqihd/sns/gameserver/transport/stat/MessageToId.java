package com.xinqihd.sns.gameserver.transport.stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.proto.stat.JavaCStatAction;
import com.xinqihd.sns.gameserver.proto.stat.JavaCStatLogin;

/**
 * GENERATED SOURCE CODE DO NOT MODIFY!
 * Translate the given message to its corresponding id. 
 * @author wangqi 
 */ 
public class MessageToId {

  private static Logger logger = LoggerFactory.getLogger(MessageToId.class); 

  public static int messageToId(MessageLite msg) { 
    if (msg instanceof JavaCStatAction.CStatAction ) {
      return 20001; 
    }
    else if (msg instanceof JavaCStatLogin.CStatLogin ) {
      return 20002; 
    }
    else {
      logger.error("No id for message: "+msg.getClass().getName());
    }
    return -1;
  }
}
