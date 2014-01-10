package com.xinqihd.sns.gameserver.config;


/**
 * The game's tip can be displayed when game content is loading.
 * @author wangqi
 *
 */
public class TipPojo {

	private int _id;
	private String tip;
	private String channel;
	private long startMillis;
	private long endMillis;
	
	/**
	 * @return the id
	 */
	public int getId() {
		return _id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this._id = id;
	}

	/**
	 * @return the tip
	 */
	public String getTip() {
		return tip;
	}

	/**
	 * @param tip the tip to set
	 */
	public void setTip(String tip) {
		this.tip = tip;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * @return the startMillis
	 */
	public long getStartMillis() {
		return startMillis;
	}

	/**
	 * @param startMillis the startMillis to set
	 */
	public void setStartMillis(long startMillis) {
		this.startMillis = startMillis;
	}

	/**
	 * @return the endMillis
	 */
	public long getEndMillis() {
		return endMillis;
	}

	/**
	 * @param endMillis the endMillis to set
	 */
	public void setEndMillis(long endMillis) {
		this.endMillis = endMillis;
	}
	
}
