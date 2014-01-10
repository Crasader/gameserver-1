package com.xinqihd.sns.gameserver.transport.rpc;

import java.lang.reflect.Method;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.Service;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.proto.Rpc.Result;
import com.xinqihd.sns.gameserver.proto.Rpc.RpcMessage;
import com.xinqihd.sns.gameserver.proto.Rpc.Type;

/**
 * Process the Rpc request
 * @author wangqi
 *
 */
public class RpcHandler extends IoHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);
	
	private static final String PARSE_FROM_METHOD = "parseFrom";
	
	private static final Class PRASE_FROM_ARG = (new byte[0]).getClass();

	private static final NullRpcController rpcController = new NullRpcController();

	/**
	 * Find the service and process the remote RPC.
	 */
	@SuppressWarnings("unchecked")
	public void messageReceived(final IoSession session, Object message)
			throws Exception {
		RpcMessage request = (RpcMessage) message;
		final int id = request.getId();
		final String className = request.getClassName();
		final String methodName = request.getMethod();
		final String serviceName = request.getService();
		Class reqClass = null;
		try {
			reqClass = Class.forName(className);
		} catch (Exception e) {
			logger.warn("Cannot find " + className);
			sendResponse(session, id, Result.ERR, className, serviceName,
					methodName, null);
			return;
		}
		
		Method parseFromMethod = reqClass.getDeclaredMethod(PARSE_FROM_METHOD, PRASE_FROM_ARG); 
		Message reqMessage = null;
		ByteString payload = request.getPayload();
		if ( payload != null ) {
			reqMessage = (Message)parseFromMethod.invoke(reqClass, payload.toByteArray());
		}
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("messageReceived id:{}, serviceName:{}, methodName:{}", new Object[]{id, serviceName, methodName});
		}
		
		SecureRpcCallback cb = new SecureRpcCallback(session, id, serviceName, methodName);
		
		Service service = GameContext.getInstance().findRpcService(
				serviceName, methodName);
		if ( service != null ) {
			ServiceDescriptor serviceDesc = service.getDescriptorForType();
			MethodDescriptor methodDesc = serviceDesc.findMethodByName(methodName);
			if ( methodDesc != null ) {
				service.callMethod(methodDesc, rpcController, reqMessage, cb);
//				if ( !cb.isCalled() ) {
//					logger.warn("You may forgot to call RpcCallback.run method in {}", serviceName);
//					sendResponse(session, id, Result.ERR, className, serviceName,
//							methodName, null);
//				}
			} else {
				logger.debug("Do not find the remote method: {}", methodName);
				sendResponse(session, id, Result.ERR, className, serviceName,
						methodName, null);
			}
		} else {
			logger.debug("Do not find the remote service: {}", serviceName);
			sendResponse(session, id, Result.ERR, className, serviceName,
					methodName, null);
		}
	}
	
	/**
	 * Construct and write the response to client.
	 * @param session
	 * @param id
	 * @param className
	 * @param serviceName
	 * @param methodName
	 * @param payload
	 * @param result
	 */
	private static void sendResponse(IoSession session, int id, Result result, 
			String className, String serviceName, String methodName, 
			ByteString payload) {
		
		RpcMessage.Builder respBuilder = RpcMessage.newBuilder();
		respBuilder.setId(id);
		respBuilder.setType(Type.RESPONSE);
		respBuilder.setResult(result);
		respBuilder.setClassName(className);
		respBuilder.setService(serviceName);
		respBuilder.setMethod(methodName);
		if ( payload != null ) {
			respBuilder.setPayload(payload);
		}
		
		session.write(respBuilder.build());
	}
	
	// ----------------------------------------------- Interface Methods

	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(cause.getMessage(), cause);
		}
	}

	public void sessionCreated(IoSession session) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("client session created");
		}
	}

	public void sessionClosed(IoSession session) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("client closed");
		}
	}

	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("client idled");
		}
	}
	
	/**
	 * Check if this callback is actually called.
	 * @author wangqi
	 *
	 */
	private static final class SecureRpcCallback implements RpcCallback<Message> {
		private IoSession session;
		private int id;
		private String serviceName;
		private String methodName;
		
		//Whether it is called.
		private boolean isCalled = false;
		
		public SecureRpcCallback(IoSession session, int id, String serviceName, String methodName) {
			this.session = session;
			this.id = id;
			this.serviceName = serviceName;
			this.methodName = methodName;
		}
		
		public boolean isCalled() {
			return isCalled;
		}
		
		@Override
		public void run(Message resp) {					
			sendResponse(session, id, Result.OK, resp.getClass().getName(), serviceName,
					methodName, resp.toByteString());
			isCalled = true;
		}
	}
}
