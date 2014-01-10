package com.xinqihd.sns.gameserver.bootstrap;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestBenchmark {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test the reflection speed
	 * @throws  
	 * @throws SecurityException 
	 */
	@Test
	public void testReflection() throws Exception {
		int times = 10000000;
		Method method = TestBenchmark.class.getMethod("execute", int.class);
		long start = System.currentTimeMillis();
		for ( int i = 0; i<times; i++ ) {
			method.invoke(TestBenchmark.class, Integer.valueOf(i));
		}
		long end = System.currentTimeMillis();
		System.out.println("Reflect: " + (end-start));
	}
	
	/**
	 * Test the reflection speed
	 * @throws  
	 * @throws SecurityException 
	 */
	@Test
	public void testNormal() throws Exception {
		int times = 10000000;		
		long start = System.currentTimeMillis();
		for ( int i = 0; i<times; i++ ) {
			TestBenchmark.execute(i);
		}
		long end = System.currentTimeMillis();
		System.out.println("Normal: " + (end-start));
	}
	
	public static final void execute(int i) {
		double value = Math.sqrt(Math.sin(Math.PI) * Math.cos(Math.PI))*i*i;
	}

}
