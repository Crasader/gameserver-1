package com.xinqihd.sns.gameserver.proto;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.ProtobufDecoder;
import com.xinqihd.sns.gameserver.transport.ProtobufEncoder;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It will assemble and test all Protocol Buffer in system to make sure 
 * they are work.
 * 
 * @author wangqi
 *
 */
public class Client extends IoHandlerAdapter implements Runnable {
	
	public static final int CONNECT_TIMEOUT = 8000;
	
	private static final Logger logger = LoggerFactory.getLogger(Client.class);
	
	//Since our request will have several response, the client will
	//wait for timeout to check if there is new message arriving.
	private static final int CLIENT_WAIT_TIMEOUT = 2000000;
	
	//Keep error information.
	private static Map<String, AtomicInteger> errorMap = new HashMap<String, AtomicInteger>();
	private static AtomicInteger successCount = new AtomicInteger(0);
	private static AtomicInteger failureCount = new AtomicInteger(0);
	private static AtomicInteger requestCount = new AtomicInteger(0);

	private static int stressRunTotalSeconds = 0;
	
	private SocketConnector connector;
	
	protected IoSession session;
	
	private EnumMap<ContextKey, Object> context;
	
	private ArrayList<Object> testcases;
	
	private ArrayList<Class> testcaseClasses;
	
	private Semaphore sema = new Semaphore(0);
	
	private int currentIndex = 0;
	
	private long currentMillis = 0l;
	
	private long stressBeginSeconds = 0l;
	
	private boolean longRunning = false;
	
	public Client(ArrayList<Class> testCases, String host, int port, boolean longRunning) 
				throws Exception {
		
		// Set up
		this.longRunning = longRunning;
		this.testcaseClasses = testCases;
		this.testcases = new ArrayList(this.testcaseClasses.size());
		for ( int i=0; i<this.testcaseClasses.size(); i++ ) {
			this.testcases.add(this.testcaseClasses.get(i).newInstance());
			logger.info("testcase: " + this.testcaseClasses.get(i));
		}
		this.context = new EnumMap<ContextKey, Object>(ContextKey.class);
		
		connector = new NioSocketConnector();
		connector.getFilterChain().addLast("codec", 
				new ProtocolCodecFilter(new ProtobufEncoder(), new ProtobufDecoder()));
		connector.setHandler(this);
		
		// Make a new connection
    ConnectFuture connectFuture = connector.connect(new InetSocketAddress(host, port));
    // Wait until the connection is make successfully.
    connectFuture.awaitUninterruptibly(CONNECT_TIMEOUT);
    try {
        session = connectFuture.getSession();
        logger.info("client connected");
    }
    catch (RuntimeIoException e) {
    	e.printStackTrace();
    	if ( session != null ) {
    		session.close();
    	}
    }
	}
	
	/**
	 * An exception occurred when testing
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		try {
			String msg = cause.getMessage();
			if ( msg == null ) {
				msg = cause.toString();
			}
			if ( msg.length() > 20 ) {
				msg = msg.substring(0, 20);
			}
			if ( errorMap.containsKey(msg) ) {
				errorMap.get(msg).incrementAndGet(); 
			} else {
				errorMap.put(msg, new AtomicInteger(1));
			}
			if ( logger.isDebugEnabled() ) {
				logger.debug(cause.getMessage(), cause);
			}
		} finally {
			failureCount.incrementAndGet();
			sema.release();
		}
	}

	/**
	 * A message received when testing.
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {

		logger.info("Message received: " + message );
		try {
			Class clazz = this.testcaseClasses.get(currentIndex);
			Object object = this.testcases.get(currentIndex);
			logger.info("client message received for " + clazz.getName());

			Method method = clazz.getDeclaredMethod("assertResult", Map.class, XinqiMessage.class);
			AssertResultType result = (AssertResultType)method.invoke(object, context, message);
			if ( result == AssertResultType.SUCCESS ) {
				successCount.incrementAndGet();
				sema.release();
			} else if ( result == AssertResultType.CONTINUE ) {
				//continue;
			} else {
				failureCount.incrementAndGet();
				sema.release();
			}
			logger.info("message " + clazz.getName() + " processed ok.");
		} catch (InvocationTargetException ite ) {
			logger.error(ite.getCause().getMessage(), ite.getCause());
			failureCount.incrementAndGet();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			failureCount.incrementAndGet();
		}
	}
	
	/**
	 * Run the test suites.
	 */
	public void run() {
		logger.info("client runs");
		try {
			for ( currentIndex = 0; currentIndex < this.testcases.size(); currentIndex++ ) {
				Class clazz = this.testcaseClasses.get(currentIndex);
				Object object = this.testcases.get(currentIndex);
				
				ProtoTest protoTest = (ProtoTest) clazz.getAnnotation(ProtoTest.class);
				int times = protoTest.times();
				
				logger.info("run the " + this.testcaseClasses.get(currentIndex) + " cases for " + times + " times.");
				
				for ( int i=0; i<times; i++ ) {
					Method method = clazz.getDeclaredMethod("generateMessge", Map.class);
					MessageLite request = (MessageLite)method.invoke(object, context);
					sendRequest(request);

					sema.acquire();
				}
			}
			
			if ( !longRunning ) {
				CloseFuture future = session.close(false);
				future.await();
				logger.info("client exit");
			}
			
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Send a message to server.
	 * @param msg
	 */
	private void sendRequest(MessageLite msg) {
		requestCount.incrementAndGet();
		XinqiMessage request = new XinqiMessage();
		request.index = 0;
		request.payload = msg;
		request.type = MessageToId.messageToId(msg);
		WriteFuture future = session.write(request);
		future.awaitUninterruptibly();
		if ( !future.isWritten() ) {
			future.getException().printStackTrace();
			fail("testBceLogin failed");
		}
	}
	
	/**
	 * Get all classes under a package.
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static ArrayList<Class> getClasses(String packageName) 
			throws ClassNotFoundException, IOException {
		
	    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	    String path = packageName.replace('.', '/');
	    Enumeration<URL> resources = classLoader.getResources(path);
	    List<File> dirs = new ArrayList<File>();
	    while (resources.hasMoreElements()) {
	        URL resource = resources.nextElement();
	        dirs.add(new File(resource.getFile()));
	    }
	    ArrayList<Class> classes = new ArrayList<Class>();
	    for (File directory : dirs) {
	        classes.addAll(findClasses(directory, packageName));
	    }
	    

	    return classes;
	}
	
	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs.
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class> findClasses(File directory, String packageName) 
				throws ClassNotFoundException {
		
	    List<Class> classes = new ArrayList<Class>();
	    if (!directory.exists())
	    {
	        return classes;
	    }
	    File[] files = directory.listFiles();
	    for (File file : files) {
	        if (file.isDirectory()) {
	            classes.addAll(findClasses(file, packageName + "." + file.getName()));
	        }
	        else if (file.getName().endsWith(".class")) {
	        	Class clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
	        	if ( clazz.isAnnotationPresent(ProtoTest.class) ) {
	        		classes.add(clazz);
	        		Collections.sort(classes, new Comparator<Class>(){
								@Override
								public int compare(Class o1, Class o2) {
									ProtoTest proto1 = (ProtoTest) o1.getAnnotation(ProtoTest.class);
									ProtoTest proto2 = (ProtoTest) o2.getAnnotation(ProtoTest.class);
									return proto1.order() - proto2.order();
								}
	        		});
	        	}
	        }
	    }
	    return classes;
	}
	
	public static void main(String[] args) throws Exception {
		String packages = "com.xinqihd.sns.gameserver.proto.cases";
		
		System.out.println("java.version: " + System.getProperty("java.version"));
		
//		String host = "test.babywar.xinqihd.com";
		String host = "localhost";
		
		int port = 3443;
		int threads = 1;
		int seconds = Integer.MAX_VALUE;
		boolean longRunning = true;

		if ( args.length >= 4 ) {
			host = args[0];
			port = Integer.parseInt(args[1]);
			threads = Integer.parseInt(args[2]);
//			seconds = Integer.parseInt(args[3]);
		}
		
		Client.stressRunTotalSeconds = seconds*1000;
		
		logger.info("Test (" + host + ":" + port + ") workers #"+threads
				+" longRunning: " + longRunning + ", seconds: " + seconds );
		
		//Find all testcases
		ArrayList<Class> testClasses = Client.getClasses(packages);
		
		ExecutorService service = Executors.newFixedThreadPool(threads);
		
		Date startDate = new Date();
		long startM = System.currentTimeMillis();
		for ( int i=0; i<threads; i++ ) {
			Client client = new Client(testClasses, host, port, longRunning);
			service.submit(client);
		}
		
		if ( longRunning ) {
			Thread.sleep(Integer.MAX_VALUE);
		}
		System.out.println("Shutdown service...");
		//Wait to process all requests
		service.shutdown();
		service.awaitTermination(seconds, TimeUnit.SECONDS);
		long endM = System.currentTimeMillis();
		
		//Print the result
		//Use reflection
		StringBuilder buf = new StringBuilder(300);
		buf.append("\nStat from: ").append(startDate).append('\n');
		buf.append("=======================================\n");
		//output
		Set<String> keySet = errorMap.keySet();
		int maxNameSize = 0;
		for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
			String msg = (String) iterator.next();
			if (maxNameSize < msg.length()) {
				maxNameSize = msg.length();
			}
		}
		maxNameSize += 5;
		float runSeconds = (int)((endM-startM)/1000.0f);
		StringUtil.padStringRight(buf, "Run:", maxNameSize);
		buf.append(runSeconds).append('\n');
		StringUtil.padStringRight(buf, "Total requests:", maxNameSize);
		buf.append(requestCount).append('\n');
		StringUtil.padStringRight(buf, "Requests/sec:", maxNameSize);
		buf.append((requestCount.intValue()/runSeconds)).append('\n');
		StringUtil.padStringRight(buf, "Success:", maxNameSize);
		buf.append(successCount).append('\n');
		StringUtil.padStringRight(buf, "Failure:", maxNameSize);
		buf.append(failureCount).append('\n');
		for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
			String msg = (String) iterator.next();
			StringUtil.padStringRight(buf, msg, maxNameSize);
			buf.append(':').append(' ');
			buf.append(errorMap.get(msg));
			buf.append('\n');
		}
		System.out.println(buf.toString());
		
		System.exit(0);
	}


}
