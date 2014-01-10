package com.xinqihd.sns.gameserver.transport.rpc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.xinqihd.sns.gameserver.proto.RpcTest;
import com.xinqihd.sns.gameserver.proto.RpcTest.RpcTestReq;
import com.xinqihd.sns.gameserver.proto.RpcTest.RpcTestResp;

/**
 * For test purpose. Since these service should be registered both in 
 * server and client for tests, they cannot be moved in test-classes path.
 * @author wangqi
 *
 */
public class RpcTestService {
	/**
	 * Blocking test service
	 * @author wangqi
	 *
	 */
	public static class RpcBlockingTestService extends RpcTest.RpcSleep {

		@Override
		public void test(RpcController controller, RpcTestReq request,
				RpcCallback<RpcTestResp> done) {
			int sleep = request.getSleep();
			System.out.println("sleep: " + sleep);
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
			}
			RpcTestResp.Builder respBuilder = RpcTestResp.newBuilder();
			respBuilder.setSleep(sleep);
			done.run(respBuilder.build());
		}
		
	}
	
	/**
	 * Non-blocking test service
	 * @author wangqi
	 *
	 */
	public static class RpcNormalTestService extends RpcTest.RpcSleep {
		
		private ExecutorService service = Executors.newCachedThreadPool();

		@Override
		public void test(RpcController controller, RpcTestReq request,
				final RpcCallback<RpcTestResp> done) {
			final int sleep = request.getSleep();
			service.execute(new Runnable() {
				public void run() {
					System.out.println("sleep: " + sleep);
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
					}
					RpcTestResp.Builder respBuilder = RpcTestResp.newBuilder();
					respBuilder.setSleep(sleep);
					done.run(respBuilder.build());
				}
			});
		}
		
	}
	
	/**
	 * blocking test service
	 * @author wangqi
	 *
	 */
	public static class RpcSpeedService extends RpcTest.RpcSleep {
		
		@Override
		public void test(RpcController controller, RpcTestReq request,
				final RpcCallback<RpcTestResp> done) {
			int sleep = request.getSleep();
			//do nothing.
			RpcTestResp.Builder respBuilder = RpcTestResp.newBuilder();
			respBuilder.setSleep(sleep);
			done.run(respBuilder.build());
		}
		
	}
}
