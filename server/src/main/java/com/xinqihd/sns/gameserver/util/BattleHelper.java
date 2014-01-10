package com.xinqihd.sns.gameserver.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiAtkBltInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleAttack.BseRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiHurtUserInfo;

/**
 * BattleHelper根据BceRoleAttack的数据，根据当前地图，计算出正确的子弹运行时间及各子弹破坏效果
 * 
 * x = t * speedx + wind * t * t / 0.075
 * y = -t * speedy + 380 * t * t
 * 
 * 
 * @author jsding
 *
 */
public class BattleHelper {
  private static final int[] allmapIds = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 1001, 1002, 2000};
  
  private static final HashMap<Integer, byte[][]> mapImageDatas = new HashMap<Integer, byte[][]>();

  /**
   * convert image to data  map_00.png --> map_00.dat 仅保留alpha值
   * @param args
   * @throws Throwable
   */
  public static final void main(String[] args) throws Throwable {
    String basepath = "/Users/jsding/Documents/workspace/newenginegames/snsgames/babywar/server/data/map";
    for (int mapId : allmapIds) {
      
      String filename = null;
      if (mapId < 10) {
        filename = "map_0"+mapId;
      } else {
        filename = "map_"+mapId;
      }
      BufferedImage image = ImageIO.read(new File(basepath, filename + ".png"));
      int width = image.getWidth();
      int height = image.getHeight();
      
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(basepath, filename + ".dat")));
      dos.writeInt(width);
      dos.writeInt(height);
      for (int j = 0; j< height; j++) {
        for (int i = 0; i < width; i++) {
          int color = image.getRGB(i, j);
          dos.writeByte((color >> 24) & 0xff);
        }
      }
      
      dos.flush();
      dos.close();
      
    }

  }
  
  private static final String bulletpath = "/Users/jsding/Documents/workspace/newenginegames/snsgames/babywar/server/data/bullet/bullet_missile";
  public static byte[][] getMapData(int mapId) {
    if (mapImageDatas.containsKey(mapId)) {
      return mapImageDatas.get(mapId);
    }
    try {
      String basepath = "/Users/jsding/Documents/workspace/newenginegames/snsgames/babywar/server/data/map";
      String filename = null;
        if (mapId < 10) {
          filename = "map_0"+mapId+".dat";
        } else {
          filename = "map_"+mapId+".dat";
        }
        
        
        DataInputStream din = new DataInputStream(new FileInputStream(new File(basepath, filename)));
        int width = din.readInt();
        int height = din.readInt();
        byte[][] data = new byte[height][width];
        for (int i = 0; i < height; i++) {
          for(int j = 0; j < width; j++) {
            data[i][j] = din.readByte();
          }
        }
        
        mapImageDatas.put(mapId, data);

      return data;
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  
  private byte[][] floorImage = null;
  public BattleHelper(byte[][] floorData) {
    this.floorImage = floorData;
  }
  public BseRoleAttack attack(IoSession session, BceRoleAttack roleAttack) throws IOException {
    // 地图ID
    float wind = 0.2f;
    float scale = 0.4f;
    int direction = roleAttack.getDirection();
    int startx = roleAttack.getUserx() + 30 * (direction == 4 ? 1 : -1);
    int starty = roleAttack.getUsery() - 50;
    
    int power = roleAttack.getPower();
    float angle = roleAttack.getAtkAngle() / 1000;
    
    int speed = power * 20;
    
    double radian = Math.PI * angle / 180;
    double speedx = speed * Math.cos(radian);
    double speedy = speed * Math.sin(radian);
    
    int width = floorImage[0].length;
    int height = floorImage.length;
    // 根据上述运动轨迹计算公式，每次增加0.2, 直到碰到a > 155的地方爆炸
    int i = 1;
    float t = 0;
    int bltx = 0;
    int blty = 0;
    int result = 0;
    while(true) {
      t = i * 0.01f;
      bltx = (int)(startx + t * speedx + wind * t * t);
      blty = (int)(starty - t * speedy + 380 * t * t);
      
      if (bltx > 0 && blty > 0) {
        if (bltx >= width || blty >= height) {
          result = 2;
          break;
        }
        
        int argb = floorImage[blty][bltx];
        
        if (((argb >> 24) & 0xff) > 155) {
          // 需要将位置纠正到最上面的Floor点
          result = 1;
          break;
        }
      }
      i++;
    }
    
    /**
     * 服务器端需要保持已经被破坏的地图
     */
    if (result == 1) {
      BufferedImage areaImage = ImageIO.read(new File(bulletpath, "area.png"));
      BufferedImage edgeImage = ImageIO.read(new File(bulletpath, "edge.png"));
      
      BufferedImage scaledAreaImage = new BufferedImage(areaImage.getWidth(), (int)(areaImage.getHeight() * scale), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = (Graphics2D)scaledAreaImage.getGraphics();
      g.drawImage(areaImage, 0, 0, scaledAreaImage.getWidth(), scaledAreaImage.getHeight(), null);
      g.dispose();
      
      BufferedImage scaledEdgeImage = new BufferedImage(edgeImage.getWidth(), (int)(edgeImage.getHeight() * scale), BufferedImage.TYPE_INT_ARGB);
      g = (Graphics2D)scaledEdgeImage.getGraphics();
      g.drawImage(edgeImage, 0, 0, scaledEdgeImage.getWidth(), scaledEdgeImage.getHeight(), null);
      g.dispose();
      
      int x = bltx - edgeImage.getWidth() / 2;
      int y = (int)(blty - edgeImage.getHeight() * (1 - scale) / 2);
      
      int srcWidth = scaledAreaImage.getWidth();
      int srcHeight = scaledAreaImage.getHeight();
      
      int dstx = x; 
      int dsty = y; 
      for (int j = 0; j < srcHeight; j++) {
        for (i = 0; i < srcWidth; i++) {
          if (dstx + i >= 0 && dsty + j >= 0 &&dstx + i < width && dsty + j < height) {
            int color = scaledAreaImage.getRGB(i, j);
            if (((color >> 24) & 0xff) == 255) {
              // floorImage.setRGB(dstx + i, dsty + j, 0);
              floorImage[dsty + j][dstx + i ] = 0;
            }
          }
        }
      }
      
      dstx = x - (scaledEdgeImage.getWidth() - scaledAreaImage.getWidth() ) /2 ;
      dsty = y - (scaledEdgeImage.getHeight() - scaledAreaImage.getHeight()) / 2;
      
      srcWidth = scaledEdgeImage.getWidth();
      srcHeight = scaledEdgeImage.getHeight();
      
      for (int j = 0; j < srcHeight; j++) {
        for (i = 0; i < srcWidth; i++) {
          if (dstx + i >= 0 && dsty + j >= 0 &&dstx + i < width && dsty + j < height) {
            int srcColor = scaledEdgeImage.getRGB(i, j);
            int alpha1 = (srcColor >> 24) & 0xff;
            
            int alpha2 = floorImage[dsty + j][dstx + i];
            if (alpha1 != 0 && alpha2 != 0) {
               // floorImage.setRGB(dstx + i, dsty + j, srcColor);
              floorImage[dsty + j][dstx + i] = (byte)alpha1;
            }
          }
        }
      }
                  
      
    }
    
    XinqiBseRoleAttack.BseRoleAttack.Builder builder = XinqiBseRoleAttack.BseRoleAttack.newBuilder();
    /**
     * for test, 用户发送基本的攻击数据，服务器需要完成所有的计算并将计算结果发送给房间客户端。
     * 客户端发出BceRoleAttack包后，只有收到BseRoleAttack才会有下一步的动作
     * 
     * RoleAttack包含
     */
    // 1. 基本信息
    builder.setSessionId("BBBBBBBBBB");
    builder.setAngle(roleAttack.getAngle());
    builder.setUserx(startx);
    builder.setUsery(starty);
    builder.setDirection(roleAttack.getDirection());
    builder.setBltMode(0);
    builder.setBltQuantity(1);
    builder.setBltCount(1);
    builder.setBltAtkTimes(1);
    
    // 2. 攻击子弹信息 (repeated)
    XinqiAtkBltInfo.AtkBltInfo.Builder atkBltInfo = XinqiAtkBltInfo.AtkBltInfo.newBuilder();
    atkBltInfo.setBltIdx(1);
    atkBltInfo.setSpeedX((int)(speedx * 1000));
    atkBltInfo.setSpeedY((int)(speedy * 1000));
    atkBltInfo.setResult(result);
    atkBltInfo.setTime((int)(t * 1000));
    atkBltInfo.setBltX(bltx);
    atkBltInfo.setBltY(blty);
    atkBltInfo.setPngNum((int)(scale * 10));
    
    
    // 2.1. 子弹攻击到的玩家信息 (repeated)
    XinqiHurtUserInfo.HurtUserInfo.Builder hurtUserInfoBuilder = XinqiHurtUserInfo.HurtUserInfo.newBuilder();
    hurtUserInfoBuilder.setUserId("CCCCCCCCC");
    hurtUserInfoBuilder.setBlood(30);
    hurtUserInfoBuilder.setEnergy(40);
    hurtUserInfoBuilder.setUserMode(1);
    atkBltInfo.addHurtUser(hurtUserInfoBuilder.build());
    
    builder.addBltInfo(atkBltInfo.build());

    // 2.2. 子弹攻击到的PickBoxInfo
    // TODO
    
  
    return  builder.build();

  }
}
