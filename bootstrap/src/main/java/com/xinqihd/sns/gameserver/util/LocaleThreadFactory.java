package com.xinqihd.sns.gameserver.util;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Create a new thread that can inherit the locale setting 
 * from parent thread.
 * 
 * @author wangqi
 *
 */
public class LocaleThreadFactory {

	static final AtomicInteger poolNumber = new AtomicInteger(1);
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	LocaleThreadFactory() {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
				.getThreadGroup();
		namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
	}

	public Thread newThread(Runnable r) {

		LocaleThread t = new LocaleThread(group, r,
				namePrefix + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon())
			t.setDaemon(false);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}

	/**
	 * Locale Thread
	 * @author wangqi
	 *
	 */
	public static class LocaleThread extends Thread {
		private Locale locale = Locale.SIMPLIFIED_CHINESE;
		private ThreadLocal<Locale> threadLocal = new ThreadLocal<Locale>();
		
		public LocaleThread(ThreadGroup group, Runnable target, 
				String name, int stackSize) {
			super(group, target, name, stackSize);
			Thread parent = Thread.currentThread();
			if ( parent instanceof LocaleThread ) {
				this.setLocale(((LocaleThread)parent).getLocale());
			} else {
				this.setLocale(locale);
			}
		}
		
		public void setLocale(Locale locale) {
			threadLocal.set(locale);
		}
		
		public Locale getLocale() {
			return threadLocal.get();
		}
	}
}
