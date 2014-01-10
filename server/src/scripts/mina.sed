#!/bin/sh
s/^import org.jboss.netty.channel.SimpleChannelHandler;/import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler; \
import org.apache.mina.core.session.IoSession;/
s/^import org.jboss.netty.channel.*//
s/public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)/public void messageReceived(IoSession session, Object message)/
s/BseUserInfo/UserInfo/
s/Channel channel = ctx.getChannel();//
s/channel.write(response);/session.write(response);/
s/(XinqiMessage)e.getMessage();/(XinqiMessage)message;/
