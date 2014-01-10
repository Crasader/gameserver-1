package com.xinqihd.sns.gameserver.geom;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.apache.commons.math.util.FastMath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.ai.AIAction;
import com.xinqihd.sns.gameserver.battle.BulletTrack;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BitmapUtilTest {

	@Before
	public void setUp() throws Exception {
		BitmapUtil.isDebug = true;
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Run BitSetImage for 10000. Time:913884, Heap:22.909355M
	 * @throws Exception
	 */
	public void testReadBitmapToBitSet() throws Exception {
		final File mapFile = new File("../data/map/map_00.png");
		System.out.println(mapFile.getAbsolutePath());
		BitSetImage bitSetImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		BitmapUtil.drawBitSetImage(bitSetImage, new File("target/bitmap.png"));
		int times = 10000;
		TestUtil.doPerform(new Runnable() {
			public void run() {
				BitSetImage bitSetImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
			}
		}, "BitSetImage", times);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testReadBulletToBitSet() throws Exception {
		final File bulletFile = new File("src/test/data/bullet_sArea.png");
		System.out.println(bulletFile.getAbsolutePath());
		BitSetImage bitSetImage = BitmapUtil.readBitmapToBitSet(bulletFile, 4, 150);
		BitmapUtil.drawBitSetImage(bitSetImage, new File("target/bitmap.png"));
	}
	
	@Test
	public void testSaveBitSetImageToFile() throws Exception {
		final File mapFile = new File("../data/map/map_00.png");
		System.out.println(mapFile.getAbsolutePath());
		BitSetImage bitSetImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		File outFile = new File("target/bitsetimage.dat");
		bitSetImage.toFile(outFile);
		
		BitSetImage actualImage = BitSetImage.fromFile(outFile);
		BitmapUtil.drawBitSetImage(actualImage, new File("target/bitmap.png"));
		
		assertEquals(bitSetImage, actualImage);
	}
	
	@Test
	public void testSubtractBitSetImage() throws Exception {
		final int scaleRatio = 4;
		final File bulletFile = new File("src/test/data/bullet_sArea.png");
		final BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, scaleRatio, 150);
		
		final File mapFile = new File("src/test/data/map_00.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, scaleRatio, 150);
		
		final int centerX = 883;
		final int centerY = 113;
		
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap1.png"));
		
		mapImage.substract(bulletImage, centerX, centerY, scaleRatio);
		
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap2.png"));
		
		int times = 10000;
		TestUtil.doPerform(new Runnable() {
			public void run() {
				mapImage.substract(bulletImage, centerX, centerY, scaleRatio);
			}
		}, "BitSet Substract", times);
	}
	
	@Test
	public void testIntersectRatioFull() throws Exception {
		final File bulletFile = new File("src/test/data/bullet_sArea.png");
		final BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 1, 150);
		int userX = 100, userY = 100, userWidth = 90, userHeight = 60;
		int centerX = 100, centerY = 100;
		double ratio = 0.0;
		ratio = BitmapUtil.intersectRatio(bulletImage, centerX, centerY, userX, userY, userWidth, userHeight);
		System.out.println(ratio);
		assertEquals(1.0, ratio, 0.1);
	}
	
	@Test
	public void testIntersectRatioReal2() throws Exception {
		/**
			bulletWidth:128 bulletHeight:98 centerX:1794 centerY:97 userCenterX:1730 userCenterY:97 userWidth:100 userHeight:90; unitNo:-1; ratio:0.0
	    #intersectRatio: 128, 98, 1794, 97, 1730, 97, 100, 90
		 */
		final File bulletFile = new File("src/test/data/bullet_sArea.png");
		final BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 1, 150);
		int centerX = 1794, centerY = 97;
		int userX = 1730, userY = 97, userWidth = 100, userHeight = 90;
		double ratio = 0.0;
		
		ratio = BitmapUtil.intersectRatio(bulletImage, centerX, centerY, userX, userY, userWidth, userHeight);
		System.out.println(ratio);
		assertEquals(0.8, ratio, 0.1);
	}
	
	/*
	 */
	@Test
	public void testIntersectRatioReal3() throws Exception {
		/**
			bulletWidth:128 bulletHeight:98 centerX:941 centerY:196 userCenterX:870 userCenterY:194 userWidth:100 userHeight:90; unitNo:-1; ratio:0.0
		 */
		final File bulletFile = new File("src/test/data/bullet_sArea.png");
		final BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 1, 150);
		int centerX = 961, centerY = 196;
		int userX = 870, userY = 194, userWidth = 100, userHeight = 90;
		double ratio = 0.0;
		
		ratio = BitmapUtil.intersectRatio(bulletImage, centerX, centerY, userX, userY, userWidth, userHeight);
		System.out.println(ratio);
		assertEquals(0.2, ratio, 0.1);
	}
	
	@Test
	public void testIntersectRatioPartial() throws Exception {
		final File bulletFile = new File("src/test/data/bullet_sArea.png");
		final BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 1, 150);
		System.out.println("bullet width:"+bulletImage.getWidth()+", height:"+bulletImage.getHeight());
		int userWidth = 90, userHeight = 60;
		int userX = 150;
		int userY = 150;
		int centerX = 230;
		int centerY = 150;
		
		double ratio = 0.0;
		
		ratio = BitmapUtil.intersectRatio(bulletImage, centerX, centerY, userX, userY, userWidth, userHeight);
		System.out.println(ratio);
		assertEquals(0.5, ratio, 0.1);
	}
	
	@Test
	public void testIntersectRatioPartial2() throws Exception {
		final File bulletFile = new File("src/test/data/bullet_sArea.png");
		final BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 4, 150);
		int userX = 100, userY = 100, userWidth = 90, userHeight = 60;
		int centerX = 100, centerY = 100;
		double ratio = 0.0;
		
		centerX = 250;
		centerY = 160;
		userX = 200;
		userY = 200; 
		ratio = BitmapUtil.intersectRatio(bulletImage, centerX, centerY, userX, userY, userWidth, userHeight);
		System.out.println(ratio);
		assertEquals(0.8, ratio, 0.1);
	}
		
	@Test
	public void testIntersectRatioReal() throws Exception {
		final File bulletFile = new File("src/test/data/bullet_sArea.png");
		final BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 4, 150);
		int userWidth = 60, userHeight = 60;
		double ratio = 0.0;
		
		int centerX = 1370;
		int centerY = 183;
		int userX = 1370;
		int userY = 183; 
		ratio = BitmapUtil.intersectRatio(
				50, 50, centerX, centerY, userX, userY, userWidth, userHeight);
		System.out.println(ratio);
		assertEquals(1.0, ratio, 0.1);
	}
	
	/**
	 * bulletWidth:135 bulletHeight:75 centerX:430 centerY:21 userCenterX:330 userCenterY:110 userWidth:100 userHeight:90; unitNo:-1; ratio:0.0
	 * @throws Exception
	 */
	@Test
	public void testIntersectRatioReal4() throws Exception {
		final File bulletFile = new File("src/test/data/bullet_sArea.png");
		final BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 4, 150);
		int userWidth = 100, userHeight = 90;
		double ratio = 0.0;
		
		int centerX = 430;
		int centerY = 21;
		int userX = 330;
		int userY = 110; 
		ratio = BitmapUtil.intersectRatio(
				50, 50, centerX, centerY, userX, userY, userWidth, userHeight);
		System.out.println(ratio);
		assertEquals(0, ratio, 0.1);
	}
	
	@Test
	public void testCaculateBulletTrack() throws Exception {
		final File mapFile = new File("src/test/data/map_00.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		int startx = 1354;
		int starty = 162;
		int power  = 100;
		int angle  = 150;
		double wind= 1;
		ArrayList<SimplePoint> line = new ArrayList<SimplePoint>();
		BulletTrack track = BitmapUtil.caculateBulletTrack(4, startx, starty, power, angle, wind, mapImage, mapImage.getHeight(), line);
		assertEquals(null, track.hitPoint);
		System.out.println("track size: " + line.size());
		printBulletTrack(mapFile, line, track);
//		assertEquals(6, line.get(line.size()-1).getX());
//		assertEquals(-294, line.get(line.size()-1).getY());
	}
	
	@Test
	public void testCaculateBulletTrackAngle30() throws Exception {
		final File mapFile = new File("src/test/data/map_00.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		int startx = 354;
		int starty = 199;
		int power  = 100;
		int angle  = 30;
		double wind= 1;
		ArrayList<SimplePoint> line = new ArrayList<SimplePoint>();
		BulletTrack track = BitmapUtil.caculateBulletTrack(4, startx, starty, power, angle, wind, mapImage, mapImage.getHeight(), line);
//		assertEquals(null, track.hitPoint);
//		assertEquals(6, line.get(line.size()-1).getX());
//		assertEquals(-294, line.get(line.size()-1).getY());
		System.out.println("track size: " + line.size());
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		printBulletTrack(mapFile, line, track);
	}
	
	@Test
	public void testCaculateBulletTrackAngle90() throws Exception {
		final File mapFile = new File("src/test/data/map_00.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		int startx = 432;
		int starty = 238;
		int power  = 100;
		int angle  = 90;
		double wind= 1;
		ArrayList<SimplePoint> line = new ArrayList<SimplePoint>();
		BulletTrack track = BitmapUtil.caculateBulletTrack(4, startx, starty, power, angle, wind, mapImage, mapImage.getHeight(), line);
		System.out.println("track size: " + line.size());
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		printBulletTrack(mapFile, line, track);
	}
	
	@Test
	public void testCanItDrop() throws Exception {
		final File mapFile = new File("src/test/data/map_00.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		int startx = 988;
		int starty = 246;
		boolean canDrop = BitmapUtil.canItDropToBottom(startx, starty, 4, mapImage);
		assertEquals(false, canDrop);
	}
	
	@Test
	public void testCanItDropTrue() throws Exception {
		final File mapFile = new File("src/test/data/map_00.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		int startx = 1030;
		int starty = 246;
		boolean canDrop = BitmapUtil.canItDropToBottom(startx, starty, 4, mapImage);
		assertEquals(true, canDrop);
	}
	
	@Test
	public void testCanItDropMap1001() throws Exception {
		final File mapFile = new File("../data/map/map_1001.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		int startx = 120;
		int starty = 154;
		boolean canDrop = BitmapUtil.canItDropToBottom(startx, starty, 4, mapImage);
		assertEquals(false, canDrop);
	}
	
	@Test
	public void testDropOnGround1001() throws Exception {
		final File mapFile = new File("../data/map/map_1001.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		int startx = 120;
		int starty = 120;
		SimplePoint point = BitmapUtil.dropOnGround(startx, starty, 4, mapImage);
		System.out.println(point);
		assertEquals(152, point.getY());
	}
	
	@Test
	public void testScaleBitSetImage0() throws Exception {
		File bulletFile = new File("src/test/data/bullet_sArea.png");
		BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 4, 150);
		bulletImage = BitmapUtil.scaleBitSetImage(bulletImage, 0);
		BitmapUtil.drawBitSetImage(bulletImage, new File("target/bitmap_bullet.png"));
		double ratio = 0.0;
	}
	
	@Test
	public void testScaleBitSetImage1() throws Exception {
		File bulletFile = new File("src/test/data/bullet_sArea.png");
		BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 1, 150);
		bulletImage = BitmapUtil.scaleBitSetImage(bulletImage, 1);
		BitmapUtil.drawBitSetImage(bulletImage, new File("target/bitmap_bullet_10.png"));
		double ratio = 0.0;
	}
	
	@Test
	public void testScaleBitSetImage0_5() throws Exception {
		File bulletFile = new File("src/test/data/bullet_sArea.png");
		BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 1, 150);
		bulletImage = BitmapUtil.scaleBitSetImage(bulletImage, 0.5);
		BitmapUtil.drawBitSetImage(bulletImage, new File("target/bitmap_bullet_05.png"));
		double ratio = 0.0;
	}
	
	@Test
	public void testScaleBitSetImage2_5() throws Exception {
		File bulletFile = new File("src/test/data/bullet_sArea.png");
		BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, 1, 150);
		bulletImage = BitmapUtil.scaleBitSetImage(bulletImage, 0.25);
		BitmapUtil.drawBitSetImage(bulletImage, new File("target/bitmap_bullet_25.png"));
		double ratio = 0.0;
	}
	
	@Test
	public void testRoleAttack() throws Exception {
		final File mapFile = new File("src/test/data/map_00.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, 4, 150);
		int startx = 354;
		int starty = 199;
		int power  = 30;
		int angle  = 30;
		double wind= 1;
		ArrayList<SimplePoint> line = new ArrayList<SimplePoint>();
		BulletTrack track = BitmapUtil.caculateBulletTrack(4, startx, starty, power, angle, wind, mapImage, mapImage.getHeight(), line);
		System.out.println("track size: " + line.size());
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		printBulletTrack(mapFile, line, track);
		double ratio = BitmapUtil.intersectRatio(mapImage, track.hitPoint.getX(), 
				track.hitPoint.getY(), startx, starty, 90, 60);
		System.out.println("ratio: " + ratio);
		boolean canUserDrop = BitmapUtil.canItDropToBottom(startx, starty, 4, mapImage);
		System.out.println("canUserDrop: " + canUserDrop);
		assertEquals(false, canUserDrop);
	}
	
	@Test
	public void testRoleAttackMap17() throws Exception {
		int scaleRatio = 4;
		final File mapFile = new File("../data/map/map_17.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, scaleRatio, 150);
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		//RoleAttack: userPos: (780, 3), angle: 31, power: 40, dir: 1
		int startx = 780;
		int starty = 3;
		int angle  = 31;
		int power  = 40;
		double wind= 0;
		ArrayList<SimplePoint> line = new ArrayList<SimplePoint>();
		BulletTrack track = BitmapUtil.caculateBulletTrack(scaleRatio, 
				startx, starty, power, angle, wind, mapImage, mapImage.getHeight(), line);
		System.out.println("track size: " + line.size());
		System.out.println("hit point: " +  track.hitPoint);
		System.out.println("top point: " +  track.topPoint);
		double ratio = BitmapUtil.intersectRatio(mapImage, track.hitPoint.getX(), 
				track.hitPoint.getY(), startx, starty, 90, 60);
		System.out.println("ratio: " + ratio);
		boolean canUserDrop = BitmapUtil.canItDropToBottom(startx, starty, scaleRatio, mapImage);
		System.out.println("canUserDrop: " + canUserDrop);
		assertEquals(false, canUserDrop);
		
		//Test damage
		File bulletFile = new File("/Users/wangqi/disk/projects/snsgames/babywar/client/game/Build/Prefab/bullet/bullet_black/area.png");
		BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, scaleRatio, 150);
		mapImage.substract(bulletImage, track.hitPoint.getX(), 
				track.hitPoint.getY(), scaleRatio);
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap_damage.png"));
		
		printBulletTrack(mapFile, mapImage, line, track, scaleRatio);
	}
	
	@Test
	public void testRoleAttackMap13() throws Exception {
		int scaleRatio = 1;
		final File mapFile = new File("../data/map/map_13.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, scaleRatio, 150);
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		//RoleAttack: userPos: (780, 3), angle: 31, power: 40, dir: 1
		int startx = 688;
		int starty = 177;
		int angle  = 150;
		int targetx = 260;
		int targety = 32;
		int wind= 0;
		int power  = AIAction.calculatePower(angle, targetx-startx, targety-starty, wind);
		ArrayList<SimplePoint> line = new ArrayList<SimplePoint>();
		BulletTrack track = BitmapUtil.caculateBulletTrack(scaleRatio, 
				startx, starty, power, angle, wind, mapImage, mapImage.getHeight(), line);
		System.out.println("hit point: " +  track.hitPoint+", power="+power);
		System.out.println("sin 150 = " + FastMath.sin(150/180.0*Math.PI)/10);
		
		//Test damage
		File bulletFile = new File("/Users/wangqi/disk/projects/snsgames/babywar/client/game/Build/Prefab/bullet/bullet_black/area.png");
		BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, scaleRatio, 150);
		if ( track.hitPoint != null ) {
			mapImage.substract(bulletImage, track.hitPoint.getX(), 
				track.hitPoint.getY(), scaleRatio);
		}
		
		printBulletTrack(mapFile, mapImage, line, track, scaleRatio);
		
	}
	
	@Test
	public void testRoleAttackMap16() throws Exception {
		int scaleRatio = 1;
		final File mapFile = new File("../data/map/map_16.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, scaleRatio, 150);
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		//RoleAttack: userPos: (780, 3), angle: 31, power: 40, dir: 1
		int startx = 1580;
		int starty = 34;
		int angle  = 160;
		int targetx = 760;
		int targety = -6;
		int wind= 0;
		int power  = AIAction.calculatePower(angle, targetx-startx, targety-starty, wind);
		ArrayList<SimplePoint> line = new ArrayList<SimplePoint>();
		BulletTrack track = BitmapUtil.caculateBulletTrack(scaleRatio, 
				startx, starty, power, angle, wind, mapImage, mapImage.getHeight(), line);
		System.out.println("hit point: " +  track.hitPoint+", power="+power);
		
		//Test damage
		File bulletFile = new File("/Users/wangqi/disk/projects/snsgames/babywar/client/game/Build/Prefab/bullet/bullet_black/area.png");
		BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, scaleRatio, 150);
		if ( track.hitPoint != null ) {
			mapImage.substract(bulletImage, track.hitPoint.getX(), 
				track.hitPoint.getY(), scaleRatio);
		}
		
		printBulletTrack(mapFile, mapImage, line, track, scaleRatio, targetx, targety);
		
	}
	
	@Test
	public void testRoleAttackMap17AfterAttack() throws Exception {
		int scaleRatio = 2;
		final File mapFile = new File("../data/map/map_09.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, scaleRatio, 150);
		//BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		
		//Test damage
		File bulletFile = new File("/Users/wangqi/disk/projects/snsgames/babywar/client/game/Build/Prefab/bullet/bullet_cannon/area.png");
		BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, scaleRatio, 150);
		mapImage.substract(bulletImage, 1520, 220, scaleRatio);
		
		SimplePoint sp = BitmapUtil.dropOnGround(1520, 220, scaleRatio, mapImage);
		
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap_damage.png"));
		
		System.out.println(sp);
	}
	
	
	@Test
	public void testRoleAttackWithScale() throws Exception {
		int scaleRatio = 2;
		final File mapFile = new File("../data/map/map_09.png");
		final BitSetImage mapImage = BitmapUtil.readBitmapToBitSet(mapFile, scaleRatio, 150);
		//BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap.png"));
		
		//Test damage
		File bulletFile = new File("/Users/wangqi/disk/projects/snsgames/babywar/client/game/Build/Prefab/bullet/bullet_cannon/area.png");
		BitSetImage bulletImage = BitmapUtil.readBitmapToBitSet(bulletFile, scaleRatio, 150);
		BitmapUtil.scaleBitSetImage(bulletImage, 0.5);
		mapImage.substract(bulletImage, 1520, 220, scaleRatio);
		
		SimplePoint sp = BitmapUtil.dropOnGround(1520, 220, scaleRatio, mapImage);
		
		BitmapUtil.drawBitSetImage(mapImage, new File("target/bitmap_scale_damage.png"));
		
		System.out.println(sp);
	}

	private void printBulletTrack(final File mapFile,
			ArrayList<SimplePoint> line, BulletTrack track) throws IOException {
		//Print image
		BufferedImage map = ImageIO.read(mapFile);
		int base = 500;
		BufferedImage debugImage = new BufferedImage(map.getWidth(), map.getHeight()+base, map.getType());
		Graphics g = debugImage.getGraphics();
		g.drawImage(map, 0, base, null);
		g.setColor(Color.BLUE);
		for ( SimplePoint p : line ) {
			int tx = p.getX();
			int ty = p.getY() + base;
			g.fillRect(tx, ty, 2, 2);
//			System.out.println("tx = " + tx + ", ty = " +ty);
		}
		g.setColor(Color.RED);
		if ( track.hitPoint != null ) {
			int tx = track.hitPoint.getX();
			int ty = track.hitPoint.getY() + base;
			g.fillRect(tx, ty, 4, 4);
		}
		ImageIO.write(debugImage, "png", new File("target/bitmap_attack.png"));
		System.out.println(track);
	}
	
	private void printBulletTrack(final File mapFile, BitSetImage mapImage,
			ArrayList<SimplePoint> line, BulletTrack track, int scaleRatio) throws IOException {
		//Print image
		BufferedImage map = ImageIO.read(mapFile);
		int base = 500;
		BufferedImage debugImage = new BufferedImage(map.getWidth(), map.getHeight()+base, map.getType());
		Graphics g = debugImage.getGraphics();
		g.drawImage(map, 0, base, null);
		int width= map.getWidth();
		int height=map.getHeight();
		for ( int y=0; y<height; y++) {
			for ( int x=0; x<width; x++) {
				if ( !mapImage.isBitSet(x/scaleRatio, y/scaleRatio)) {
					debugImage.setRGB(x, y+base, 0x00000000);
				}
			}
		}
		g.setColor(Color.BLUE);
		for ( SimplePoint p : line ) {
			int tx = p.getX();
			int ty = p.getY() + base;
			g.fillRect(tx, ty, 2, 2);
//			System.out.println("tx = " + tx + ", ty = " +ty);
		}
		g.setColor(Color.RED);
		if ( track.hitPoint != null ) {
			int tx = track.hitPoint.getX();
			int ty = track.hitPoint.getY() + base;
			g.fillRect(tx, ty, 4, 4);
		}
		ImageIO.write(debugImage, "png", new File("target/bitmap_attack.png"));
		System.out.println(track);
	}
	
	private void printBulletTrack(final File mapFile, BitSetImage mapImage,
			ArrayList<SimplePoint> line, BulletTrack track, int scaleRatio, int targetx, int targety) throws IOException {
		//Print image
		BufferedImage map = ImageIO.read(mapFile);
		int base = 500;
		BufferedImage debugImage = new BufferedImage(map.getWidth(), map.getHeight()+base, map.getType());
		Graphics g = debugImage.getGraphics();
		g.drawImage(map, 0, base, null);
		int width= map.getWidth();
		int height=map.getHeight();
		for ( int y=0; y<height; y++) {
			for ( int x=0; x<width; x++) {
				if ( !mapImage.isBitSet(x/scaleRatio, y/scaleRatio)) {
					debugImage.setRGB(x, y+base, 0x00000000);
				}
			}
		}
		g.setColor(Color.BLUE);
		for ( SimplePoint p : line ) {
			int tx = p.getX();
			int ty = p.getY() + base;
			g.fillRect(tx, ty, 2, 2);
//			System.out.println("tx = " + tx + ", ty = " +ty);
		}
		g.setColor(Color.RED);
		if ( track.hitPoint != null ) {
			int tx = track.hitPoint.getX();
			int ty = track.hitPoint.getY() + base;
			g.fillRect(tx, ty, 4, 4);
		}
		g.setColor(Color.green);
		g.fillRect(targetx, targety+base, 90, 60);
		ImageIO.write(debugImage, "png", new File("target/bitmap_attack.png"));
		System.out.println(track);
	}
}
