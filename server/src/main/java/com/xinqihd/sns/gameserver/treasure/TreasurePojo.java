package com.xinqihd.sns.gameserver.treasure;

import java.util.List;

import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.proto.XinqiBseTreasureHuntQuery.TreasureHunt;
import com.xinqihd.sns.gameserver.reward.Reward;

/**
 * The object wraps a treaure for the treasure hunting function
 * @author wangqi
 *
 */
public class TreasurePojo {
  /**
  * 对应的抽奖模式
  * 0: 普通寻宝
  * 1: 高级寻宝
  * 2: 专家寻宝
  */
	private int mode = 0;
	
  /**
  * 购买一次的元宝价格
  */
	private int price = 0;
	
	/**
	 * 抽奖可能获得的八项最有价值的道具
	 */
	private List<Reward> gifts = null;
	
	/**
	 * 抽奖的成功率
	 */
	private List<Float> ratios = null;

	/**
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	/**
	 * @return the price
	 */
	public int getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(int price) {
		this.price = price;
	}

	/**
	 * @return the gifts
	 */
	public List<Reward> getGifts() {
		return gifts;
	}

	/**
	 * @param gifts the gifts to set
	 */
	public void setGifts(List<Reward> gifts) {
		this.gifts = gifts;
	}

	/**
	 * @return the ratios
	 */
	public List<Float> getRatios() {
		return ratios;
	}

	/**
	 * @param ratios the ratios to set
	 */
	public void setRatios(List<Float> ratios) {
		this.ratios = ratios;
	}
	
	/**
	 * Convert this object to Protobuf TreasureHunt
	 * @return
	 */
	public TreasureHunt toTreaureHunt(boolean isFree) {
		TreasureHunt.Builder builder = TreasureHunt.newBuilder();
		builder.setMode(mode);
		if ( isFree && mode == 0) {
			builder.setPrice(0);
		} else {
			builder.setPrice(price);
		}
		if ( this.gifts != null ) {
			for ( Reward reward : this.gifts ) {
				builder.addGifts(reward.toGift());
			}
		}
		if ( this.ratios != null ) {
			for ( Float ratio : ratios ) {
				builder.addRatios(Math.round(ratio*1000));
			}
		}
		return builder.build();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"TreasurePojo [mode=%s, price=%s, gifts=%s, ratios=%s]", mode, price,
				gifts, ratios);
	}
	
}
