package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.stat.Frequency;
import org.apache.commons.math.stat.StatUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MathUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPerformance() throws Exception {
		int max = 10000;
		final int[] ratio1 = new int[10];
		
		final Random javaRandom = new Random();
		TestUtil.doPerform(new Runnable() {
			public void run() {
				int r = (int)(javaRandom.nextDouble() * 10);
				ratio1[r%10]++;
			}
		}, "Java Random", max);
		
		printRatio(ratio1, max);
		
		final int[] ratio2 = new int[10];
		final RandomData commonRandom = new RandomDataImpl();
		TestUtil.doPerform(new Runnable() {
			public void run() {
				int r = Math.abs((int)(commonRandom.nextGaussian(0.0, 1.0)/6*10));
				ratio2[r%10]++;
			}
		}, "Common Random", max);
		
		printRatio(ratio2, max);
		
		assertTrue(true);
	}

	@Test
	public void testNextDouble() throws Exception {
		int max = 10000;
		final int range = 12;
		final int[] ratio = new int[range];
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				double v = MathUtil.nextDouble();
				int r = (int)(v * range);
				ratio[r%range]++;
			}
		}, "Uniform Random", max);
		
		printRatio(ratio, max);
	}
	
	@Test
	public void testNextGuassionDouble() throws Exception {
		int max = 10000;
		final int range = 12;
		final int[] ratio = new int[range];
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				double v = MathUtil.nextGaussionDouble();
				int r = (int)(v * range);
				ratio[r%range]++;
			}
		}, "Normal Random", max);
		
		printRatio(ratio, max);
	}
	
	/**
	 * Run nextInt range for 10000. Time:9, Heap:0.0M
	 	0: 44.8%
		1: 31.42%
		2: 16.7%
		3: 5.54%
		4: 1.31%
		5: 0.2%
		6: 0.03%
		7: 0.0%
		8: 0.0%
		9: 0.0%
		
	 * @throws Exception
	 */
	@Test
	public void testNextInt() throws Exception {
		int max = 10000;
		final int[] ratio1 = new int[10];
		
		final RandomData commonRandom = new RandomDataImpl();
		TestUtil.doPerform(new Runnable() {
			public void run() {
				int r = MathUtil.nextGaussionInt(0, 10);
				ratio1[r]++;
			}
		}, "nextInt range", max);
		
		printRatio(ratio1, max);
	}
	
	/**
		Before
		* Run nextInt range 3.0 for 10000. Time:9, Heap:0.0M
			0: 23.6%
			1: 21.13%
			2: 18.76%
			3: 13.6%
			4: 9.24%
			5: 6.5%
			6: 3.39%
			7: 1.85%
			8: 1.05%
			9: 0.88%
		After
		 Run nextInt range 3.0 for 10000. Time:49, Heap:1.287529M
			0: 14.61%
			1: 13.27%
			2: 12.51%
			3: 11.25%
			4: 9.95%
			5: 9.31%
			6: 8.6%
			7: 7.53%
			8: 7.09%
			9: 5.88%
			total: 100.0%

	
	 * @throws Exception
	 */
	@Test
	public void testNextIntQ() throws Exception {
		int max = 10000;
		final int count = 20;
		final int[] ratio1 = new int[count];
		
		final RandomData commonRandom = new RandomDataImpl();
		TestUtil.doPerform(new Runnable() {
			public void run() {
				int r = MathUtil.nextGaussionInt(1, 5, 3.0);
//				if ( r < ratio1.length ) {
					ratio1[r]++;
//				}
			}
		}, "nextInt range 3.0", max);
		
		printRatio(ratio1, max);
	}
	
	@Test
	public void testNextIntQBigInt() throws Exception {
		int max = 10000;
		final int count = 10;
		final int[] ratio1 = new int[count];
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				int r = MathUtil.nextGaussionInt(2500, 4000, 25.0);
				assertTrue("r="+r, r>=2500 && r<4000);
				System.out.println("r="+(r-2500));
			}
		}, "nextInt range 3.0", max);
		
		printRatio(ratio1, max);
	}
	
	@Test
	public void testNextIntQ1() throws Exception {
		int max = 10000;
		final int count = 16;
		final int[] ratio1 = new int[count];
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				int r = MathUtil.nextGaussionInt(0, 7, 3.0);
				ratio1[r]++;
				//assertTrue("r="+r, r>=0 && r<=5);
				//System.out.println("r="+r);
			}
		}, "nextInt range 3.0", max);
		
		printRatio(ratio1, max);
	}
	
	
	@Test
	public void testNextIntQ2() throws Exception {
		int loop = 10000;
		final int min = 10;
		final int max = 100;
		final double q = 5;
		final HashMap<Integer, Integer> countMap = new
				HashMap<Integer, Integer>();
		
		final RandomData commonRandom = new RandomDataImpl();
		TestUtil.doPerform(new Runnable() {
			public void run() {
				int r = MathUtil.nextGaussionInt(min, max, q);
				Integer count = countMap.get(r);
				if ( count == null ) {
					countMap.put(r, 1);
				} else {
					countMap.put(r, count+1);
				}
			}
		}, "nextInt range 3.0", loop);
		
		ArrayList<Integer> list = new ArrayList<Integer>(countMap.keySet());
		Collections.sort(list);
		for ( Integer in : list ) {
			Integer value = countMap.get(in);
			System.out.println(in+ "->"+value+" : " + (value*100.0/loop) + "%");
			assertTrue(in.intValue() >= min);
		}
	}

	/**
	 * Run Uniform Random for 100000000. Time:29752, Heap:0.2576065M
	 * Normal Distribution [1.9831936257114444E-9, 0.958216890674393]
	 * Run Uniform Random for 100000. Time:25, Heap:0.0M
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNextGaussianDouble() throws Exception {
		int max = 100000;
		
		final RandomData commonRandom = new RandomDataImpl();
		final double[] result = new double[2];
		result[0] = Double.MAX_VALUE;
		result[1] = Double.MIN_NORMAL;
		TestUtil.doPerform(new Runnable() {
			public void run() {
				double r  = MathUtil.nextGaussionDouble();
				if ( result[0]>r ) {
					result[0] = r;
				}
				if ( result[1]<r ) {
					result[1] = r;
				}
			}
		}, "Uniform Random", max);
		
		System.out.println("Normal Distribution ["+result[0]+", "+result[1]+"]");
	}
	
	/**
	 * Run Uniform Random for 100000000. Time:16893, Heap:0.49897003M
			0: 45.152312%
			1: 31.831831%
			2: 15.825302%
			3: 5.549493%
			4: 1.371592%
			5: 0.237706%
			6: 0.029142%
			7: 0.002471%
			8: 1.44E-4%
			9: 7.0E-6%
	 * @throws Exception
	 */
	@Test
	public void testNextGaussianDoubleDistribution() throws Exception {
		int max = 100000;
		final int[] ratio1 = new int[10];
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				int r = (int)(MathUtil.nextGaussionDouble() * 10);
				ratio1[r%ratio1.length]++;
			}
		}, "Uniform Random", max);
		
		printRatio(ratio1, max);
	}
	
	/**
			Run U=0.0, Q=0.5 for 100000000. Time:16990, Heap:0.49897003M
			0: 76.985401%
			1: 21.372181%
			2: 1.610527%
			3: 0.031713%
			4: 1.78E-4%
			5: 0.0%
			6: 0.0%
			7: 0.0%
			8: 0.0%
			9: 0.0%
			
	 * @throws Exception
	 */
	@Test
	public void testNextGaussianDoubleDistribution2() throws Exception {
		int max = 100000;
		final int[] ratio1 = new int[10];
		final double q = 2.0;
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				int r = (int)(MathUtil.nextGaussionDouble(0.0, q) * 10);
				ratio1[r%ratio1.length]++;
			}
		}, "U=0.0, Q="+q, max);
		
		printRatio(ratio1, max);
	}
	
	@Test
	public void testNextSample() throws Exception {
		ArrayList list = new ArrayList();
		for ( int i = 0; i<10; i++ ) {
			list.add(i);
		}
		Object[] ints = MathUtil.randomPick(list, 2);
		for (int i = 0; i < ints.length; i++) {
			System.out.println("ints["+i+"] = " + ints[i]);
		}
	}
	
	@Test
	public void testNextSampleAll() throws Exception {
		ArrayList list = new ArrayList();
		for ( int i = 0; i<10; i++ ) {
			list.add(i);
		}
		Object[] ints = MathUtil.randomPick(list, list.size());
		for (int i = 0; i < ints.length; i++) {
			System.out.println("ints["+i+"] = " + ints[i]);
		}
	}
	
	@Test
	public void testRandomPick() throws Exception {
		ArrayList list = new ArrayList();
		int count = 10;
		for (int i = 0; i < count; i++) {
			Object[] rewards = MathUtil.randomPick(RewardType.TYPES, 1);
			System.out.println("rewards["+i+"] = " + rewards[0]);
		}
	}
	
	@Test
	public void testRandomPickShuffle() throws Exception {
		ArrayList list = new ArrayList();
		int count = 10;
		for (int i = 0; i < count; i++) {
			List<RewardType> rewards = MathUtil.randomPickShuffle(RewardType.TYPES, 3);
			int index = 0;
			for ( RewardType reward: rewards ) {
				System.out.println("rewards["+(index++)+"] = " + reward);
			}
		}
	}
	
	@Test
	public void testRandomPickShuffleMax() throws Exception {
		ArrayList list = new ArrayList();
		int count = 10;
		for (int i = 0; i < count; i++) {
			List<RewardType> rewards = MathUtil.randomPickShuffle(RewardType.TYPES, 
					RewardType.values().length+1);
			int index = 0;
			for ( RewardType reward: rewards ) {
				System.out.println("rewards["+i+"] = " + reward);
			}
		}
	}
	
	/**
	 * Ratio: max=10000;
			0: 10.18%
			1: 9.94%
			2: 10.23%
			3: 9.98%
			4: 10.4%
			5: 10.19%
			6: 9.96%
			7: 9.54%
			8: 9.88%
			9: 9.7%
	 * @throws Exception
	 */
	@Test
	public void testNextSampleRatio() throws Exception {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for ( int i = 0; i<10; i++ ) {
			list.add(i);
		}
		int max = 10000;
		int[] ratio = new int[10];
		for (int i = 0; i < max; i++) {
			Object[] ints = MathUtil.randomPick(list, 1);
			int index = (Integer)ints[0];
			ratio[index]++;
		}
		printRatio(ratio, max);
	}
	
	/**
	   Ratio: q=1.0, max=10000;
			0: 45.53%
			1: 31.07%
			2: 15.81%
			3: 5.82%
			4: 1.43%
			5: 0.31%
			6: 0.03%
			7: 0.0%
			8: 0.0%
			9: 0.0%
			total: 100.0%
			
	   Ratio: q=2.0, max=10000;
			0: 23.17%
			1: 21.53%
			2: 17.99%
			3: 14.43%
			4: 9.7%
			5: 6.46%
			6: 3.55%
			7: 1.74%
			8: 0.93%
			9: 0.5%
			total: 100.0%
			
	   Ratio: q=3.0, max=10000;
			0: 18.35%
			1: 16.2%
			2: 15.09%
			3: 12.15%
			4: 11.4%
			5: 8.54%
			6: 6.71%
			7: 4.99%
			8: 3.9%
			9: 2.67%
			total: 99.99999999999999%
			
		 Ratio: q=4.0, max=10000;
			0: 15.33%
			1: 14.19%
			2: 13.12%
			3: 12.45%
			4: 10.32%
			5: 9.56%
			6: 8.15%
			7: 7.01%
			8: 5.36%
			9: 4.51%
			total: 100.00000000000001%

		 Ratio: q=5.0, max=10000;
			0: 13.48%
			1: 14.18%
			2: 12.8%
			3: 10.85%
			4: 10.84%
			5: 9.61%
			6: 8.38%
			7: 7.65%
			8: 6.63%
			9: 5.58%
			total: 100.0%

		 Ratio: q=10.0, max=10000;
			0: 12.24%
			1: 12.35%
			2: 10.7%
			3: 10.72%
			4: 10.1%
			5: 10.17%
			6: 9.08%
			7: 8.45%
			8: 8.02%
			9: 8.17%
			total: 100.0%

	 * @throws Exception
	 */
	@Test
	public void testNextGuassionSampleRatio() throws Exception {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for ( int i = 0; i<10; i++ ) {
			list.add(i);
		}
		int max = 10000;
		int[] ratio = new int[10];
		
		for (int i = 0; i < max; i++) {
			Collection<Integer> ints = MathUtil.randomPickGaussion(list, 1, 4.0);
			int index = ints.iterator().next();
			ratio[index]++;
		}
		printRatio(ratio, max);
	}
	
	@Test
	public void testNextGuassionInt() throws Exception {
		int low = 0;
		int high = 4;
		int total = 10000;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(); 
		Frequency freq = new Frequency();
		for ( int i=0; i<total; i++ ) {
			int result = MathUtil.nextGaussionInt(low, high, 4.0);
			freq.addValue(result);
			Integer countInt = (Integer)map.get(result);
			int count = 1;
			if ( countInt != null ) {
				count = countInt.intValue();
			}
			map.put(result, count+1);
		}
		for ( Integer key : map.keySet() ) {
			System.out.println(key+":"+map.get(key)*1.0d/total);
		}
		for ( int i=low; i<high; i++ ) {
			System.out.print("i="+i);
			System.out.print(", count: "+freq.getCount(i));
			System.out.print(", comfreq: "+freq.getCumFreq(i));
			System.out.print(", comPct: "+freq.getCumPct(i));
			System.out.print(", pct: "+freq.getPct(i));
			System.out.println();
		}
	}
	
	@Test
	public void testSolveCubicEquation() throws Exception {
		int angle = 30;
		double sin = Math.sin(angle/180.0*Math.PI);
		double cos = Math.cos(angle/180.0*Math.PI);
		int tx = 820/2;
		int ty = 0;

		double a = sin;
		double b = -ty;
		double c = 0;
		double d = tx*tx / (2*cos);
		
		double r = MathUtil.solveCubicEquation(a, b, c, d);
		System.out.println("r="+r);
	}
	
	
	@Test
	public void testFakeRandomInt() {
		int max = 2;
		for ( int i=0; i<1001; i++ ) {
			int n = MathUtil.nextFakeInt(max);
			System.out.println(n);
			if ( n >= max ) {
				System.out.println("error");
				break;
			}
			//System.out.println(n);
		}
	}
	
	@Test
	public void testPermute() {
		int count = 0;
		int min = 5;
		HashSet<String> resultSet = new HashSet<String>();
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			int[] r = MathUtil.nextPermuate(100, 3);
			if ( r[0] < min || r[1] < min || r[2] < min ) continue;
			if ( r[0]%5!=0 || r[1]%5!=0 || r[2]%5!=0 ) continue;
			int sum = (r[0]+r[1]+r[2]);
			if ( sum >=60 && sum <= 75 ) {
				String key = (r[0])+"\t"+(r[1])+"\t"+(r[2]);
				if ( !resultSet.contains(key) ) {
					System.out.println(key);
					count++;
					resultSet.add(key);
					if ( count>26 ) {
						break;
					}
				}
			}
		}
	}
	
	private void printRatio(int[] ratio, int max) {
		double total = 0.0;
		for ( int i=0; i<ratio.length; i++ ) {
			double r = (ratio[i]*100.0/max);
			System.out.println(i + ": " + r + "%");
			total += r;
		}
		System.out.println("total: " + total + "%");
	}
	
	enum RewardType {
		
	  //获奖的道具ID
	  //金币:-1
	  //礼券:-2
	  //元宝:-3
	  //勋章:-4

		//extra experience
		EXP,
		//golden
		GOLDEN,
		//yuanbao
		YUANBAO,
		//
		VOUCHER,
		//
		MEDAL,
		//random combat tool
		TOOL,
		//random item
		ITEM,
		//random weapon
		WEAPON,
		//unknown
		UNKNOWN;
		
		public static final List<RewardType> TYPES = Arrays.asList(RewardType.values());
	}
}
