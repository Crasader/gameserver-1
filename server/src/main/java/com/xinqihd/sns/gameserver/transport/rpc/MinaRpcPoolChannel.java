package com.xinqihd.sns.gameserver.transport.rpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.proto.Rpc.Result;
import com.xinqihd.sns.gameserver.proto.Rpc.RpcMessage;
import com.xinqihd.sns.gameserver.proto.Rpc.Type;
import com.xinqihd.sns.gameserver.util.ClientPool;

/**
 * Rpc client tool
 * @author wangqi
 *
 */
public class MinaRpcPoolChannel implements RpcChannel, BlockingRpcChannel {

	private static final Logger logger = LoggerFactory.getLogger(MinaRpcPoolChannel.class);
	
	private static final RpcStatListener statListener = new RpcStatListener();
	
	//This is used to track all rpc request id.
	private static AtomicInteger rpcIdCounter = new AtomicInteger();
	
	private RpcProtocolCodecFilter filter = new RpcProtocolCodecFilter();
	
	private IoHandler[] handlers = null;
	
	private ClientPool clientPool = null;
	
	/**
	 * This RpcClient may be called by multiple threads, if some threads sent the same
	 * RPC (same remote Service.method), there should be a way to distinguish them.
	 * I put an ID in every RPC request. The same ID will be sent back in RpcResponse.
	 * I use this ID to find their original callee.
	 */
	private ConcurrentHashMap<Integer, Callback> resultMap = new ConcurrentHashMap<Integer, Callback>();
	
	/**
	 * Create a RpcClient connected to remote host & port
	 * Core-duo 2.4G CPU: (2-connection)
	 * 	Run RPC test for 10000. Time:4310, Heap:30.40319M
	 *  Run RPC Pool test for 10000. Time:1915, Heap:69.22287M
	 *  
	 * Core-duo 2.4G CPU: (2-connection)
	 *  Run RPC test for 10000. Time:3330, Heap:31.707764M, Thread:2
	 *  Run RPC Pool test for 10000. Time:1877, Heap:27.134613M, Thread:2
	 *  
	 * Core-duo 2.4G CPU: (5-connection)
	 *  Run RPC test for 10000. Time:3564, Heap:9.313782M, Thread:2
	 *  Run RPC Pool test for 10000. Time:1912, Heap:8.885384M, Thread:2
	 *  
	 * @param remoteHost
	 * @param remotePort
	 */
	public MinaRpcPoolChannel(String remoteHost, int remotePort) {
		this(remoteHost, remotePort, Constant.CPU_CORES);
	}
	
	/**
	 * Create a RpcClient connected to remote host & port
	 * @param remoteHost
	 * @param remotePort
	 */
	public MinaRpcPoolChannel(String remoteHost, int remotePort, int count) {
		handlers = new RpcClientHandler[count];
		for ( int i=0; i<handlers.length; i++ ) {
			handlers[i] = new RpcClientHandler();
		}
		clientPool = new ClientPool(filter, handlers, remoteHost, remotePort, count);
		clientPool.setStatListener(statListener);
		clientPool.initQueue();
	}

	/**
	 * This is the Google ProtocolBuf RPC remote call method.
	 * 
	 * Call the given method of the remote service. This method is similar to 
	 * Service.callMethod() with one important difference: the caller decides 
	 * the types of the Message objects, not the callee. The request may be 
	 * of any type as long as request.getDescriptor() == method.getInputType(). 
	 * The response passed to the callback will be of the same type as 
	 * responsePrototype (which must have getDescriptor() == method.getOutputType()).
	 */
	@Override
	public void callMethod(MethodDescriptor method, RpcController controller,
			Message request, Message responsePrototype, RpcCallback<Message> done) {
		
		doCallMethod(method, controller, request, responsePrototype, done);
		
	}

	/**
	 * It is the blocking version of RPC.
	 */
	@Override
	public Message callBlockingMethod(MethodDescriptor method,
			RpcController controller, Message request, Message responsePrototype)
			throws ServiceException {

		Callback callback = doCallMethod(method, controller, request, responsePrototype, null);
		return callback.getResponse();
	}
	
	/**
	 * Call the actual RPC logic.
	 * @return
	 */
	private final Callback doCallMethod(MethodDescriptor method, RpcController controller,
			Message request, Message responsePrototype, RpcCallback<Message> done) {
		
		RpcMessage.Builder rpcReqBuilder = RpcMessage.newBuilder();
		rpcReqBuilder.setId(rpcIdCounter.getAndIncrement());
		String serviceName = method.getService().getFullName();
		String methodName = method.getName();
		String className = void.class.getName();
		byte[] content = Constant.EMPTY_BYTES;
		if ( request != null ) {
			className = request.getClass().getName();
			content = request.toByteArray();
		}
		logger.debug("callMethod className: {}, methodName: {}", className, methodName);
		rpcReqBuilder.setClassName(className);
		rpcReqBuilder.setType(Type.REQUEST);
		rpcReqBuilder.setService(serviceName);
		rpcReqBuilder.setMethod(methodName);
		rpcReqBuilder.setPayload(ByteString.copyFrom(content));
		RpcMessage req = rpcReqBuilder.build();
		
		//Save the callback
		Callback callback = new Callback(done, responsePrototype);
		resultMap.put(req.getId(), callback);
		
		//Send this RpcRequest method to server.
		clientPool.sendMessageToServer(req);
		
		return callback;
	}
	
	/**
	 * A inner impl for the Rpc handler
	 * @author wangqi
	 *
	 */
	private class RpcClientHandler extends IoHandlerAdapter {

		/**
		 * Get a remote RPC method response, get their original caller,
		 * call the done method.
		 */
		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
			if ( message instanceof RpcMessage ) {
				RpcMessage response = (RpcMessage)message;
				int id = response.getId();
				logger.debug("RpcClient receive result. id:{}, message:{}", id, response);
				Callback done = resultMap.get(id);
				if ( done != null ) {
					done.run(response);
				}
			}
		}

		@Override
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {
			Stat.getInstance().rpcClientSentFail++;
		}

	}
	
	private static class Callback implements RpcCallback<Message> {
		
		private RpcCallback<Message> delegate = null;
		private Message responsePrototype = null;
		private Message response;
		private Semaphore semaphore = new Semaphore(0);
		
		public Callback(RpcCallback<Message> delegate, Message responsePrototype) {
			this.delegate = delegate;
			this.responsePrototype = responsePrototype;
		}

		/**
		 * It is blocking until we got the server's response.
		 * @return
		 */
		public Message getResponse() {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
			}
			return response;
		}
		
		/**
		 * Callback the original caller.
		 */
		@Override
		public void run(Message parameter) {
			try {
				RpcMessage resp = (RpcMessage)parameter;
				byte[] array = resp.getPayload().toByteArray();
				try {
					if ( this.responsePrototype != null ) {	
						if ( resp.getResult() == Result.OK ) {
							this.response = this.responsePrototype.newBuilderForType().
									mergeFrom(array).build();
						} else {
							logger.debug("RPC remote has ERR response");
						}
					} else {
						logger.warn("No responsePrototype exist.");
					}
				} catch (InvalidProtocolBufferException e) {
					logger.debug("Failed to merge RpcResponse.bytes", e);
				}
				if ( this.delegate != null ) {
					this.delegate.run(this.response);
				}
			} finally {
				semaphore.release();
			}
		}
		
	}
	
	/**
	 * Collect statistic information about RpcClient
	 * @author wangqi
	 *
	 */
	private static class RpcStatListener implements IoFutureListener<IoFuture> {

		/* (non-Javadoc)
		 * @see org.apache.mina.core.future.IoFutureListener#operationComplete(org.apache.mina.core.future.IoFuture)
		 */
		@Override
		public void operationComplete(IoFuture future) {
			Stat.getInstance().rpcClientSent++;
		}
		
	}

}
