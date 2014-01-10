package com.xinqihd.sns.gameserver.util;

import static org.easymock.EasyMock.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;

/**
 * The common test utilities.
 * @author wangqi
 *
 */
public class TestUtil {
	
	/**
	 * Get the private field from object.
	 * 
	 * @param fieldName
	 * @param object
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static final Object getPrivateFieldValue(String fieldName, Object object) 
			throws NoSuchFieldException, SecurityException, 
				IllegalArgumentException, IllegalAccessException {
		
		Class clazz = object.getClass();
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		Object value = field.get(object);
		return value;
	}

	public static final void setPrivateFieldValue(String fieldName, Object object, Object value) 
			throws NoSuchFieldException, SecurityException, 
				IllegalArgumentException, IllegalAccessException {
		
		Class clazz = object.getClass();
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(object, value);
	}
	
	/**
	 * Run a task for 'times' loop and output the result.
	 * @param task
	 * @param name
	 * @param times 
	 */
	public static final void doPerform(Runnable task, String name, int times) throws Exception {
		long startM =0l, endM =0l, startHeap =0l, endHeap=0l;
		System.gc();
		Thread.sleep(500);
		startHeap = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		startM = System.nanoTime();
		for ( int i=0; i<times; i++ ) {
			task.run();
		}
		endM = System.nanoTime();
		endHeap = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		System.gc();
		Thread.sleep(500);
		long timeInMillis = (endM-startM)/1000/1000;
		float heapSize = (endHeap-startHeap)/1024f/1024;
		System.out.println("Run " + name + " for " + times + ". Time:"+timeInMillis+
				", Heap:"+heapSize+"M");
	}
	
	/**
	 * Run a task for 'times' loop and output the result.
	 * @param task
	 * @param name
	 * @param times 
	 */
	public static final void doPerformMultiThread(Runnable task, String name, int times, int threadCount) throws Exception {
		long startM =0l, endM =0l, startHeap =0l, endHeap=0l;
		ExecutorService service = Executors.newFixedThreadPool(threadCount);
		
		System.gc();
		Thread.sleep(500);
		startHeap = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		startM = System.nanoTime();
		for ( int i=0; i<times; i++ ) {
			service.execute(task);
		}
		service.shutdown();
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		endM = System.nanoTime();
		endHeap = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		System.gc();
		Thread.sleep(500);
		long timeInMillis = (endM-startM)/1000/1000;
		float heapSize = (endHeap-startHeap)/1024f/1024;
		System.out.println("Run " + name + " for " + times + ". Time:"+timeInMillis+
				", Heap:"+heapSize+"M" + ", Thread:"+threadCount);
	}
	
	/**
	 * Create a fake IoSession.
	 * @return
	 */
	public static final IoSession createIoSession() {
		//Note. It is a temporary solution. The Redis should be cleaned
		final HashMap<Object, Object> attrMap = new HashMap<Object, Object>();
		IoSession session = createNiceMock(IoSession.class);
		expect(session.setAttribute(anyObject(), anyObject())).
			andAnswer(new IAnswer<Object>() {
					@Override
					public Object answer() throws Throwable {
						Object key = getCurrentArguments()[0];
						Object value = getCurrentArguments()[1];
						attrMap.put(key, value);
						return value;
					}
				}).anyTimes();
		expect(session.getAttribute(anyObject())).
			andAnswer(new IAnswer<Object>() {
					public Object answer() throws Throwable {
						Object key = getCurrentArguments()[0];
						return attrMap.get(key);
					}
				}).anyTimes();
		return session;
	}
	
	/**
	 * Create a fake IoSession.
	 * @return
	 */
	public static final IoSession createIoSession(final ArrayList list) {
		//Note. It is a temporary solution. The Redis should be cleaned
		final HashMap<Object, Object> attrMap = new HashMap<Object, Object>();
		IoSession session = createNiceMock(IoSession.class);
		expect(session.write(anyObject()));
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object value = getCurrentArguments()[0];
				list.add(value);
				return null;
			}
		}).anyTimes();
		expect(session.isConnected()).andReturn(Boolean.TRUE).anyTimes();
		expect(session.setAttribute(anyObject(), anyObject())).
			andAnswer(new IAnswer<Object>() {
					@Override
					public Object answer() throws Throwable {
						Object key = getCurrentArguments()[0];
						Object value = getCurrentArguments()[1];
						attrMap.put(key, value);
						return value;
					}
				}).anyTimes();
		expect(session.getAttribute(anyObject())).
			andAnswer(new IAnswer<Object>() {
					public Object answer() throws Throwable {
						Object key = getCurrentArguments()[0];
						return attrMap.get(key);
					}
				}).anyTimes();
		replay(session);
		return session;
	}

	public static final IoSession createIoSessionWithOutReplay(final ArrayList list) {
		//Note. It is a temporary solution. The Redis should be cleaned
		final HashMap<Object, Object> attrMap = new HashMap<Object, Object>();
		IoSession session = createNiceMock(IoSession.class);
		expect(session.write(anyObject()));
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object value = getCurrentArguments()[0];
				list.add(value);
				return null;
			}
		}).anyTimes();
		expect(session.isConnected()).andReturn(Boolean.TRUE).anyTimes();
		expect(session.setAttribute(anyObject(), anyObject())).
			andAnswer(new IAnswer<Object>() {
					@Override
					public Object answer() throws Throwable {
						Object key = getCurrentArguments()[0];
						Object value = getCurrentArguments()[1];
						attrMap.put(key, value);
						return value;
					}
				}).anyTimes();
		expect(session.getAttribute(anyObject())).
			andAnswer(new IAnswer<Object>() {
					public Object answer() throws Throwable {
						Object key = getCurrentArguments()[0];
						return attrMap.get(key);
					}
				}).anyTimes();
		return session;
	}
	
	/**
	 * Get the given class type object from the collection. Return null if not found.
	 * @param coll
	 * @param clazz
	 * @return
	 */
	public static final Object getGivenClassObject(Collection coll, Class clazz) {
		for (Iterator iterator = coll.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			if ( object != null && clazz.isAssignableFrom(object.getClass()) ) {
				return object;
			}
		}
		return null;
	}
}
