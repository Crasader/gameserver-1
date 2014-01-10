package com.xinqihd.sns.gameserver.geom;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.commons.math.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleBitSetMap;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.BulletTrack;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.session.SessionKey;

/**
 * This class will process the map and bullet image to BitSet data.
 * In the combat, the bitset is used to record damange information.
 *  
 * @author wangqi
 *
 */
public class BitmapUtil {
	
	public static final int DEFAULT_SCALE = 256;
	
	public static boolean isDebug = false;
	
	private static final Logger logger = LoggerFactory.getLogger(BitmapUtil.class);

	
	/**
	 * Read the bitmap and put all the pixels into bit data(0 or 1).
	 * If the scaleRatio is greater than 0 and it is not 1, the scale
	 * ratio will be 1/scaleRatio.
	 * 
	 * @param imageFile
	 * @param scaleRatio
	 */
	public static final BitSetImage readBitmapToBitSet(File imageFile, int scaleRatio, int alphaThreshold) {
		try {
			BufferedImage image = ImageIO.read(imageFile);
			int sourceWidth = image.getWidth();
			int sourceHeight = image.getHeight();
			int targetWidth = sourceWidth;
			int targetHeight = sourceHeight;
			int[] pixels = null;
			if ( scaleRatio > 0 && scaleRatio != 1 ) {
				int scaleWidth = (int)((1.0/scaleRatio)*sourceWidth);
				int scaleHeight = (int)((1.0/scaleRatio)*sourceHeight);
				if ( scaleWidth<=0 ) {
					scaleWidth = 1;
				}
				if ( scaleHeight<=0 ) {
					scaleHeight = 1;
				}
				BufferedImage scaleImage = new BufferedImage(
						scaleWidth, scaleHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics g = scaleImage.getGraphics();
				g.drawImage(image, 0, 0, scaleWidth, scaleHeight, null);
							
				pixels = new int[scaleWidth * scaleHeight];
				pixels = scaleImage.getRGB(0, 0, scaleWidth, scaleHeight, pixels, 0, scaleWidth);
				targetWidth = scaleWidth;
				targetHeight = scaleHeight;
			} else {
				pixels = new int[sourceWidth * sourceHeight];
				pixels = image.getRGB(0, 0, sourceWidth, sourceHeight, pixels, 0, sourceWidth);
			}
			BitSet bitSet = new BitSet(pixels.length);
			for ( int i=0; i<pixels.length; i++) {
				int alpha = ((pixels[i] >> 24) & 0xff);
				if ( alpha > alphaThreshold ) {
					bitSet.set(i);
				}
			}
			BitSetImage bitSetImage = new BitSetImage(bitSet, sourceWidth, sourceHeight);
			return bitSetImage;
		} catch (IOException e) {
			logger.warn("Failed to read bitmap file. {}", e.getMessage());
		}
		return null;
	}
	
	/**
	 * Calculate the intersect ratio betweet given bullet bitSetImage 
	 * and the user rectangluar.
	 * 
	 * 将爆炸范围的半径除以20，中心点两侧各分成10等分，从最靠近中线点的位置循环判断哪一个等分
	 * 与玩家的判断范围相符，最终的伤害为: 0.1 + 1/等分序号。在最边缘位置为10%伤害，
	 * 中心点位置为 1.1 倍伤害
	 * 
	 * @param bitSetImage
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	public static final double intersectRatio(
			BitSetImage bitSetImage, int centerX, int centerY, 
			int userCenterX, int userCenterY, int userWidth, int userHeight) {
		
		return intersectRatio(bitSetImage.getWidth(), bitSetImage.getHeight(), 
				centerX, centerY, userCenterX, userCenterY, userWidth, userHeight);
	}
	
	public static final double intersectRatio(
			int bulletWidth, int bulletHeight, int centerX, int centerY, 
			int userCenterX, int userCenterY, int userWidth, int userHeight) {

		int userLeftTopX     = userCenterX-userWidth/2;
		int userLeftTopY     = userCenterY-userHeight/2;
		int userRightBottomX = userCenterX+userWidth/2;
		int userRightBottomY = userCenterY+userHeight/2;
		int userCenterLeftX = userCenterX-userWidth/2;
		int userCenterLeftY = userCenterY;
		int userCenterRightX = userCenterX+userWidth/2;
		int userCenterRightY = userCenterY;

		int unitX = bulletWidth/20;
		int unitY = bulletHeight/20;
		int unitNo = -1;
		
		/* 
		 * debug
		 */
		BufferedImage image = null;
		Graphics g = null;
		if ( isDebug ) {
			image = new BufferedImage(
					Math.max(userCenterX, centerX)+userWidth+bulletWidth+10, 
					Math.max(userCenterY, centerY)+userHeight+bulletHeight+10, 
					BufferedImage.TYPE_INT_ARGB);
			g = image.getGraphics();
		}
				
		for ( int i=1; i<=10; i++ ) {
			int edgeLeftTopX    = centerX-unitX*i;
			int edgeLeftTopY    = centerY-unitY*i;
			int edgeRightBottomX = centerX+unitX*i;
			int edgeRightBottomY = centerY+unitY*i;
			
			if ( isDebug ) {
				g.drawRect(edgeLeftTopX, edgeLeftTopY, edgeRightBottomX-edgeLeftTopX, edgeRightBottomY-edgeLeftTopY);
			}
			
			//首先判断玩家中心点落在矩形的那个区域内
			if ( userCenterX >= edgeLeftTopX && userCenterX <= edgeRightBottomX ) {
				if ( userCenterY >= edgeLeftTopY && userCenterY <= edgeRightBottomY ) {
					unitNo = i;
					break;
				}
			}
			
		  //如果玩家左中心点坐标位于判断范围内
			if ( (userCenterLeftX >= edgeLeftTopX && userCenterLeftX <= edgeRightBottomX) &&
					(userCenterLeftY >= edgeLeftTopY && userCenterLeftY <= edgeRightBottomY) ) {
				unitNo = i;
				break;
			} else 
		  //如果玩家右中心点坐标位于判断范围内
			if ( (userCenterRightX >= edgeLeftTopX && userCenterRightX <= edgeRightBottomX) &&
					(userCenterRightY >= edgeLeftTopY && userCenterRightY <= edgeRightBottomY) ) {
				unitNo = i;
				break;
			}
			
			//如果中心点未命中，再判断玩家边缘落点
			if ( unitNo < 0 ) {
				//如果玩家的左上角坐标位于范围内
				if ( userLeftTopX >= edgeLeftTopX && userLeftTopX <= edgeRightBottomX ) {
					if ( (userLeftTopY >= edgeLeftTopY && userLeftTopY <= edgeRightBottomY) ||
							(userRightBottomY >= edgeLeftTopY && userRightBottomY <= edgeRightBottomY) ) {
						unitNo = i;
						break;
					}
				//如果玩家的右下角坐标位于范围内
				} else if (userRightBottomX >= edgeLeftTopX && userRightBottomX <= edgeRightBottomX ) {
					if ( (userLeftTopY >= edgeLeftTopY && userLeftTopY <= edgeRightBottomY) ||
							(userRightBottomY >= edgeLeftTopY && userRightBottomY <= edgeRightBottomY) ) {
						unitNo = i;
						break;
					}
				}
			}
		}
		
		double ratio = 0.0;
		if ( unitNo < 0 ) {
			ratio = 0.0;
		} else if ( unitNo >= 0 ) {
			ratio = (11.0-unitNo)/10.0;
			if ( ratio < 0.1 ) {
				ratio = 0.1;
			}
		}
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("bulletWidth:{} bulletHeight:{} centerX:{} centerY:{} userCenterX:{} " +
					"userCenterY:{} userWidth:{} userHeight:{}; unitNo:{}; ratio:{}",
					new Object[]{bulletWidth, bulletHeight, centerX, centerY, 
					userCenterX, userCenterY, userWidth, userHeight, unitNo, ratio});
		}

		if ( isDebug ) {
			//draw bullet rect
			g.setColor(Color.BLUE);
			g.drawRect(centerX-bulletWidth/2, centerY-bulletHeight/2, 
					bulletWidth, bulletHeight);
			g.drawRect(centerX, centerY, 2, 2);
			//draw user rect
			g.setColor(Color.RED);
			g.drawRect(userLeftTopX, userLeftTopY, userWidth, userHeight);
			g.drawRect(userCenterX, userCenterY, 2, 2);
			g.drawRect(userCenterLeftX, userCenterLeftY, 2, 2);
			g.drawRect(userCenterRightX, userCenterRightY, 2, 2);
			//draw the final match unit
			g.setColor(Color.GREEN);
			g.drawRect(centerX-unitX*unitNo, centerY-unitY*unitNo, unitX*unitNo*2, 
					unitY*unitNo*2);
			drawBitmap(image, new File("target/bitmap_intersect.png"));
		}
		return ratio;
	}
	
	/**
	 * 
		弹道公式为：								
			Sx=t*V*Cos(a)-风力*t*t/F								
			Sy=t*V*sin(a)-g*t*t/2								
			其中								
			V=力度/k								
			t=V*sin(a)/g								
											
			t为炮弹的飞行时间的一半,即为飞到最高点的时间								
			V为炮弹的初始速度								
			a为炮弹射出时候的角度,取值范围为-90~180度								
			k为常数,k=0.15		力度微调系数						
			F为常数,F=0.7		风力微调系数						
			g为常数,g=120		重力微调系数						
			风力的取值范围为-5~5,正数表示逆风,负数表示顺风								
			力度的取值范围为0~100								
											
			以人物为坐标原点,建立坐标系.								
			则炮弹某一个时刻在空中的位置为(Sx,Sy)								
											
			举例	a=	45	力度=	45		力度微调系数：	k=	0.15
				风力=	0		风力微调系数：	F=	0.7
			求得	sin(a)=	0.707106781				重力微调系数：	g=	120
				cos(a)=	0.707106781						120
				t=	1.767766953	秒					
				V=	300	像素/秒
	 * 
	 * @param scaleRatio
	 * @param startx
	 * @param starty
	 * @param power
	 * @param angle
	 * @param wind
	 * @param battleMap
	 * @param trackDebugList
	 * @return
	 */
	public static final BulletTrack caculateBulletTrack(int scaleRatio, int startx, int starty, 
			int power, int angle, double wind, BitSetImage battleMap, int totalHeight, 
			ArrayList<SimplePoint> trackDebugList) {
		return caculateBulletTrack(scaleRatio, startx, starty, power, angle, wind, battleMap, 
				totalHeight, trackDebugList, null);
	}
	
	/**
	 * 
	 * @param scaleRatio
	 * @param startx
	 * @param starty
	 * @param power
	 * @param angle
	 * @param wind
	 * @param battleMap
	 * @param totalHeight
	 * @param trackDebugList
	 * @param battle
	 * @return
	 */
	public static final BulletTrack caculateBulletTrack(int scaleRatio, int startx, int starty, 
			int power, int angle, double wind, BitSetImage battleMap, int totalHeight, 
			ArrayList<SimplePoint> trackDebugList, Battle battle) {
						
		BulletTrack bullet = calculateBulletTrackSpeed(startx, starty, power, angle, wind);
    
		return caculateBulletTrack(scaleRatio, bullet, battleMap, totalHeight, trackDebugList, battle);
	}
	
	public static final BulletTrack caculateBulletTrack(int scaleRatio, BulletTrack bullet, 
			BitSetImage battleMap, int totalHeight, 
			ArrayList<SimplePoint> trackDebugList, Battle battle) {
		
    //First use 5 pixels unit to check if the the collision occurred
    int realWidth = battleMap.getWidth()*scaleRatio;
    int realHeight = Math.max(battleMap.getHeight() * scaleRatio, totalHeight);
    int baseHeight = totalHeight - realHeight;
    if ( baseHeight < 0 ) baseHeight = 0;
    double t = 0.001;
    bullet.topPoint = new SimplePoint(-1, Integer.MAX_VALUE);
    
    int startx = bullet.startX;
    int starty = bullet.startY;
    int angle  = bullet.angle;
    
    //Try to check the treasure box
    int boxLeft = Integer.MAX_VALUE;
    int boxRight = -1;
    ArrayList<Reward> boxes = null;
    ArrayList<Integer> boxIndexes = null;
    int userLeft = Integer.MAX_VALUE;
    int userRight = -1;
		int userWidth = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.USER_BODY_WIDTH, 60)/BitmapUtil.DEFAULT_SCALE;
		int userHeight = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.USER_BODY_HEIGHT, 60)/BitmapUtil.DEFAULT_SCALE;
		HashMap<SessionKey, BattleUser> battleUsers = null;
		
    if ( battle != null ) {
    	Map<Integer, Reward> boxColl = battle.getTreasureBox();
    	if ( boxColl.size() > 0 ) {
    		boxes = new ArrayList<Reward>();
    		boxIndexes = new ArrayList<Integer>();
    		for ( Entry<Integer,Reward> entry : boxColl.entrySet() ) {
    			boxes.add(entry.getValue());
    			boxIndexes.add(entry.getKey());
    			if ( boxLeft > entry.getValue().getX() ) {
    				boxLeft = entry.getValue().getX();
    			}
    			if ( boxRight < entry.getValue().getX() ) {
    				boxRight = entry.getValue().getX();
    			}
    		}
    	}
    	
    	battleUsers = battle.getBattleUserMap();
    	if ( battleUsers.size() > 0 ) {
    		for ( BattleUser bUser : battleUsers.values() ) {
    			if ( bUser != battle.getRoundOwner() ) {
	    			if ( userLeft > bUser.getPosX()-userWidth ) {
	    				userLeft = bUser.getPosX();
	    			}
	    			if ( userRight < bUser.getPosX()+userWidth ) {
	    				userRight = bUser.getPosX();
	    			}
    			}
    		}
    	}
    }
    
    LOOP:
    for ( ; t<4.0; t+= 0.001 ) {
	    double powerT = t*t;
			double x  = bullet.ax*powerT + bullet.bx*t;
			double y  = bullet.ay*powerT + bullet.by*t;
			
			int realBulletX = startx + (int)x;
			int realBulletY = starty - (int)y;
			int mapBulletX  = (startx + (int)x) / scaleRatio;
			int mapBulletY  = (starty - baseHeight - (int)y) / scaleRatio;
			
			if ( realBulletX < 0 || realBulletX > realWidth || 
					realBulletY > realHeight ) {
				logger.debug("Bullet track reach the bound. break");
				break;
			}
			//Process the special angle = 90 case
			if ( realBulletY < 0 && angle == 90 ) {
				SimplePoint hitPoint = new SimplePoint(startx, starty);
				bullet.hitPoint = hitPoint;
				t = (-bullet.by-bullet.by)/2/bullet.ay;
				break;
			}
			
			if ( realBulletY < bullet.topPoint.getY() ) {
				bullet.topPoint.setX(realBulletX);
				bullet.topPoint.setY(realBulletY);
			}
			
			//Check the treasure box
			if ( realBulletX > boxLeft && realBulletX < boxRight ) {
				for ( int i=0; i<boxes.size(); i++ ) {
					Reward reward = boxes.get(i);
					if ( realBulletX > reward.getX() - 60 && realBulletX < reward.getX() + 60 ) {
						if ( realBulletY > reward.getY() - 60 && realBulletY < reward.getY() + 60 ) {
							Integer index = boxIndexes.get(i);
							battle.getTreasureBox().remove(index);
							logger.debug("BitmapUtil#caculateBulletTrack: pick a reward at index {}", index);
						}
					}
				}
			}
			//Check the users
			if ( realBulletX > userLeft && realBulletX < userRight ) {
				for ( BattleUser bUser : battleUsers.values() ) {
					if ( bUser == null ) continue;
					if ( realBulletX > bUser.getPosX()- userWidth && realBulletX < bUser.getPosX()+ userWidth ) {
						if ( realBulletY > bUser.getPosY()-userHeight && realBulletY < bUser.getPosY()+userHeight) {
							SimplePoint hitPoint = new SimplePoint(realBulletX, realBulletY);
							bullet.hitPoint = hitPoint;
							logger.debug("The bullet hit the user at map ({},{}) to real ({},{})", 
									new Object[]{mapBulletX, mapBulletY, realBulletX, realBulletY});
							break LOOP;
						}
					}
				}
			}
			
			if ( trackDebugList != null ) {
				SimplePoint point = new SimplePoint(realBulletX, realBulletY);
				trackDebugList.add(point);
			}
			
			if ( battleMap.isBitSet(mapBulletX, mapBulletY) ) {
				SimplePoint hitPoint = new SimplePoint(realBulletX, realBulletY);
				bullet.hitPoint = hitPoint;
				logger.debug("The bullet hit the ground at map ({},{}) to real ({},{})", 
						new Object[]{mapBulletX, mapBulletY, realBulletX, realBulletY});
				break;
			}
			
    }
    bullet.flyingSeconds = t;
		return bullet;
	}
	
	
	/**
	 * Calculate the bullet track speed.
	 * @param startx
	 * @param starty
	 * @param power
	 * @param angle
	 * @param wind
	 * @return
	 */
	public static final BulletTrack calculateBulletTrackSpeed(int startx, int starty, 
			int power, int angle, double wind) {							
		BulletTrack bullet = new BulletTrack();
		bullet.startX = startx;
		bullet.startY = starty;
		bullet.power = power;
		bullet.angle = angle;
		bullet.wind = (int)wind;
		
		//Get the constant
		double K = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_K, 0.059081);
		double F = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_F, 0.075);
		int    g = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.BATTLE_ATTACK_G, 760);
		
		//Calculate V and t
		double V = power/K;
		double sinA = FastMath.sin(angle*Math.PI/180);
		double cosA = FastMath.cos(angle*Math.PI/180);
    /*
			X Function:
			 ax = -wind/F
			 bx = (power/K)*cos(angle)
			 x  = a*t^2 + b*t + 0
     */
		bullet.ax = -wind / F;
		bullet.bx = (int)(V*cosA);
    /*
			Y Function:
			 ay = -g/2
			 by = (power/k)*sin(angle)
			 y = ay*t^2 + by*t + 0
     */
		bullet.ay = -g/2;
		bullet.by = (int)(V*sinA);
		
		bullet.speed = V;
		
    double speedx = bullet.bx;
    double speedy = bullet.by;
    bullet.speedX = speedx;
    bullet.speedY = speedy;
    
    if ( logger.isDebugEnabled() ) {
    	logger.debug("ax:{}, ay:{}; bx(speedx):{}, by(speedy):{}, wind:{}, F:{}, K:{}, g:{}",
    			new Object[]{bullet.ax, bullet.ay, bullet.bx, bullet.by, wind, F, K, g});
    }
    
    return bullet;
	}
	
	/**
	 * Check if the position at x can drop out of the bottom of BitSetImage
	 * @param x
	 * @param y
	 * @param battleMap
	 * @return
	 */
	public static final boolean canItDropToBottom(int x, int y, int scaleRatio, BitSetImage battleMap) {
		boolean canDrop = true;
		int mapX  = x / scaleRatio;
		int mapY  = y / scaleRatio;
		int height = battleMap.getHeight();
		for ( int i=mapY; i<height; i++ ) {
			if ( battleMap.isBitSet(mapX, i) ) {
				canDrop = false;
				break;
			}
		}
		return canDrop;
	}
	
	/**
	 * Check the new position for a given user
	 * @param x
	 * @param y
	 * @param battleMap
	 * @return
	 */
	public static final SimplePoint dropOnGround(int x, int y, int scaleRatio, BitSetImage battleMap) {
		int mapX  = x / scaleRatio;
		int mapY  = y / scaleRatio;
		SimplePoint newPoint = new SimplePoint(x, y);
		int height = battleMap.getHeight();
		for ( int i=mapY; i<height; i++ ) {
			if ( battleMap.isBitSet(mapX, i) ) {
				newPoint.setY( i*scaleRatio );
				break;
			}
		}
		return newPoint;
	}
	
	
	/**
	 * Scale the BitSetImage according to the heightRatio. The final ratio will be
	 * 1/heightRatio.
	 * 
	 * @param geometry
	 * @param dim
	 * @param widthRatio
	 * @param heightRatio
	 * @return
	 */
	public static BitSetImage scaleBitSetImage(BitSetImage bitSetImage, double heightRatio) {
		if ( heightRatio >= 1 ) {
			return bitSetImage;
		} else if ( heightRatio <= 0 ) {
			int width = bitSetImage.getWidth();
			BitSet bitSet = new BitSet(width);
			int middleLine = bitSetImage.getHeight()/2;
			for ( int i=0; i<width; i++ ) {
				if ( bitSetImage.isBitSet(i, middleLine) ) {
					bitSet.set(i, true);
				}
			}
			BitSetImage newImage = new BitSetImage(bitSet, width, 1);
			return newImage;
		} else {
			int width = bitSetImage.getWidth();
			int height = bitSetImage.getHeight();
			int mergeLine = (int)(1/heightRatio);
			BitSet bitSet = new BitSet(width*height/mergeLine);
			BitSetImage newImage = new BitSetImage(bitSet, width, height/mergeLine);
			int row = 0;
			for ( int y=0; y<height; y+=mergeLine) {
				for ( int x=0; x<width; x++ ) {
					if ( bitSetImage.isBitSet(x, y) || bitSetImage.isBitSet(x, y+1) ) {
						newImage.setBitAt(x, row, true);
					}
				}
				row++;
			}
			return newImage;
		}
	}
	
	// -------------------------------------------------------------- Debug utilities
	
	
	public static final void drawBitSetImage(BattleBitSetMap map) {
		drawBitSetImage(map.getMapBitSet(), new File(map.getMapId()+"_damage.png"));
	}
	/**
	 * Draw the data from BitSet. If the bit is 1 or true, the pixel is black,
	 * otherwise, the pixel is white.
	 * 
	 * @param bitSet
	 * @param imageFile
	 */
	public static final void drawBitSetImage(BitSetImage bitSetImage, File imageFile) {
		int width = bitSetImage.getWidth();
		int height = bitSetImage.getHeight();
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for ( int y=0; y<height; y++ ) {
			for ( int x=0; x<width; x++ ) {
				if ( bitSetImage.isBitSet(x, y) ) {
					bufferedImage.setRGB(x, y, 0xFF000000);
				} else {
					bufferedImage.setRGB(x, y, 0x00000000);
				}
			}
		}
		drawBitmap(bufferedImage, imageFile);
	}
	
	/**
	 * Draw the java image to an output file.
	 * @param image
	 * @param imageFile
	 */
	public static final void drawBitmap(BufferedImage image, File imageFile) {
		//Test
		try {
			ImageIO.write(image, "png", imageFile);
		} catch (IOException e) {
			logger.warn("Failed to output bitmap file:{}, Exception:{}", imageFile, e.getMessage());
		}
	}
	
	/**
	 * Draw the java image to an output file.
	 * @param image
	 * @param imageFile
	 */
	public static final void drawMapAndBulletBitmap(BufferedImage map, BufferedImage bullet, 
			int bulletCenterX, int bulletCenterY, File imageFile) {
		BufferedImage finalImage = new BufferedImage(map.getWidth(), map.getHeight(), map.getType());
		Graphics g = finalImage.getGraphics();
		g.drawImage(map, 0, 0, null);
		g.drawImage(bullet, bulletCenterX-bullet.getWidth()/2, bulletCenterY-bullet.getHeight()/2, null);
		drawBitmap(finalImage, imageFile);
	}
	
	/**
	 * Draw the bullet track on map
	 * @param mapFile
	 * @param line
	 * @param track
	 * @throws IOException
	 */
	public static final void drawBulletTrack(BattleBitSetMap map,
			ArrayList<SimplePoint> line, BulletTrack track) {
		try {
			//Print image
			BufferedImage debugImage = new BufferedImage(map.getWidth(), map.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = debugImage.getGraphics();
			File outputFile = new File("bitmap_attack.png");
			if ( outputFile.exists() ) {
				BufferedImage originImage = ImageIO.read(outputFile);
				g.drawImage(originImage, 0, 0, null);
			}
			g.setColor(Color.BLUE);
			for ( SimplePoint p : line ) {
				int tx = p.getX();
				int ty = p.getY();
				g.fillRect(tx, ty, 2, 2);
//				logger.debug("tx = " + tx + ", ty = " +ty);
			}
			logger.debug("total bullet loop: " + line.size());
			g.setColor(Color.RED);
			if ( track.hitPoint != null ) {
				int tx = track.hitPoint.getX();
				int ty = track.hitPoint.getY();
				g.fillRect(tx, ty, 4, 4);
			}
			ImageIO.write(debugImage, "png", outputFile);
		} catch (IOException e) {
			logger.debug("Failed to draw bullet track. Exception: {}", e.getMessage());
		}
	}
	
	public static final void drawBulletTrack(String mapDir, BattleBitSetMap map, BitSetImage mapImage,
			ArrayList<SimplePoint> line, BulletTrack track, int scaleRatio) {
		//Print image
		try {
			String mapFileName = map.getMapId();
			if ( mapFileName.length() <= 1 ) {
				mapFileName = "0"+mapFileName;
			}
			File outputFile = new File("bitmap_attack"+mapFileName+".png");
			File mapFile = null;
//			if ( outputFile.exists() ) {
//				mapFile = outputFile; 
//			} else {
				mapFile = new File(mapDir, "map_"+mapFileName+".png");
//			}
			logger.debug("input map file dir: " + mapFile.getAbsolutePath());
			BufferedImage mapBufferedImage = ImageIO.read(mapFile);

			int base = 1000;
			BufferedImage debugImage = new BufferedImage(map.getWidth(), map.getHeight()+base, mapBufferedImage.getType());
			Graphics g = debugImage.getGraphics();
			g.drawImage(mapBufferedImage, 0, base, null);
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
			
			if ( track.hitPoint != null ) {
				g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
				Date d = new Date();
				g.drawString(d.toString(), track.hitPoint.getX(), track.hitPoint.getY());
			}
//			g.drawString("speedx:"+ track.speedX, 100, 140);
//			g.drawString("speedy:"+ track.speedY, 100, 180);
//			g.drawString("hitx:"+ track.hitPoint.getX(), 100, 220);
//			g.drawString("hity:"+ track.hitPoint.getY(), 100, 260);
//			g.drawString("time:"+ track.flyingSeconds, 100, 300);
			
			ImageIO.write(debugImage, "png", outputFile);
		} catch (IOException e) {
			logger.debug("Failed to draw bullet track. Exception: {}", e.getMessage());
		}
	}
}
