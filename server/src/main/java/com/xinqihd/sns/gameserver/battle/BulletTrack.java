package com.xinqihd.sns.gameserver.battle;

import com.xinqihd.sns.gameserver.geom.SimplePoint;
import com.xinqihd.sns.gameserver.proto.XinqiBceBulletTrack;
import com.xinqihd.sns.gameserver.proto.XinqiBceBulletTrack.Point;
import com.xinqihd.sns.gameserver.proto.XinqiBseBulletTrack.Bullet;

/**
 * The bullet data in an attacking, including its track, top point's coordinate
 * @author wangqi
 *
 */
public class BulletTrack {

	public int roundNumber = 0;
	public int startX = 0;
	public int startY = 0;
	public int wind = 0;
	public int power = 0;
	public int angle = 0;
	//The bullet hit point's coordinate
	public SimplePoint hitPoint;
	//The top point
	public SimplePoint topPoint;
	//The bullet's flying seconds
	public double flyingSeconds;
	//The bullet's flying speed (pixels/second)
	public double speed;
	//The horizontal speed
	public double speedX;
	//The vertical speed
	public double speedY;
	//The sin value of hitting angle.
	public double sinAttackAngle;
	//The scale ratio for a bullet.
	public int pngNum = 100;
	//The check range of user 
	public int offx = 60;
	public int offy = 60;
	
	//For internal use only
	public double ax, ay;
	public int bx, by;
	//1 爆炸 2 出界
	public int result;
	public String bullet;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BulletTrack [roundNumber=");
		builder.append(roundNumber);
		builder.append(", startX=");
		builder.append(startX);
		builder.append(", startY=");
		builder.append(startY);
		builder.append(", wind=");
		builder.append(wind);
		builder.append(", power=");
		builder.append(power);
		builder.append(", angle=");
		builder.append(angle);
		builder.append(", hitPoint=");
		builder.append(hitPoint);
		builder.append(", topPoint=");
		builder.append(topPoint);
		builder.append(", flyingSeconds=");
		builder.append(flyingSeconds);
		builder.append(", speed=");
		builder.append(speed);
		builder.append(", speedX=");
		builder.append(speedX);
		builder.append(", speedY=");
		builder.append(speedY);
		builder.append(", sinAttackAngle=");
		builder.append(sinAttackAngle);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Convert this to BseBullet object
	 * @return
	 */
	public Bullet toBseBullet() {
		Bullet.Builder builder = Bullet.newBuilder();
		builder.setStartx(startX);
		builder.setStarty(startY);
		builder.setSpeedx((int)(speedX));
		builder.setSpeedy((int)(speedY));
		builder.setWind(wind);
		builder.setPngNum(pngNum);
		builder.setBullet(bullet);
		builder.setOffx(offx);
		builder.setOffy(offy);
		return builder.build(); 
	}
	
	/**
	 * Construct these object from BceBulletTrack
	 * @param track
	 * @return
	 */
	public static BulletTrack fromBceBulletTrack(XinqiBceBulletTrack.BulletTrack track) {
		BulletTrack bulletTrack = new BulletTrack();
		bulletTrack.flyingSeconds = track.getFlyingSeconds()/10000.0;
		Point p = track.getHitpoint();
		if ( p != null ) {
			SimplePoint hitPoint = new SimplePoint(p.getX(), p.getY());
			bulletTrack.hitPoint = hitPoint;
		}
		p = track.getToppoint();
		if ( p != null ) {
			SimplePoint topPoint = new SimplePoint(p.getX(), p.getY());
			bulletTrack.topPoint = topPoint;
		}
		bulletTrack.speedX = track.getSpeedx();
		bulletTrack.speedY = track.getSpeedy();
		bulletTrack.pngNum = track.getPngNum();
		bulletTrack.result = track.getResult();
		return bulletTrack;
	}

}
