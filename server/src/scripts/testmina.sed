#!/bin/sh
s/^import org.jboss.netty.buffer.ChannelBuffer;/import org.apache.mina.core.future.WriteFuture;/
s/^import org.jboss.netty.channel.ChannelFuture;/import org.apache.mina.core.session.IoSession;/
s/^import org.jboss.netty.*//
s/^import com.xinqihd.sns.gameserver.transport.client.MessageToId/import com.xinqihd.sns.gameserver.transport.MessageToId/
s/ChannelHandlerContext ctx, MessageEvent e)/IoSession session, Object message)/
s/ChannelFuture future = channel.write(request);/WriteFuture future = session.write(request);/
s/if ( !future.isSuccess() ) {/if ( !future.isWritten() ) {/
s/future.getCause().printStackTrace();/future.getException().printStackTrace();/
s/XinqiMessage response = (XinqiMessage)e.getMessage();/XinqiMessage response = (XinqiMessage)message;/
