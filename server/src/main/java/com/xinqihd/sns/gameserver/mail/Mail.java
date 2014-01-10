package com.xinqihd.sns.gameserver.mail;

import java.util.List;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.proto.XinqiGift.Gift;
import com.xinqihd.sns.gameserver.proto.XinqiMailData.MailData;
import com.xinqihd.sns.gameserver.reward.Reward;

/**
 * The game mail wrapper
 * 
 * @author wangqi
 *
 */
public class Mail {

	private String fromRoleName = Constant.EMPTY; 
	
	private String subject = Constant.EMPTY;
	
	private String content = Constant.EMPTY;
	
	private String sendDate = Constant.EMPTY;
	
  /**
   * In the future, we may support 
   * send gift to another users.
   * The gift pew is the item pew
   * in sender's bag.
  */
	private List<Integer> giftPews = null;
	
	private List<Reward> gifts = null;

	/**
	 * @return the fromUserName
	 */
	public String getFromRoleName() {
		return fromRoleName;
	}

	/**
	 * @param fromUserName the fromUserName to set
	 */
	public void setFromRoleName(String fromRoleName) {
		this.fromRoleName = fromRoleName;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the sendDate
	 */
	public String getSendDate() {
		return sendDate;
	}

	/**
	 * @param sendDate the sendDate to set
	 */
	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}

	/**
	 * @return the giftPews
	 */
	public List<Integer> getGiftPews() {
		return giftPews;
	}

	/**
	 * @param giftPews the giftPews to set
	 */
	public void setGiftPews(List<Integer> giftPews) {
		this.giftPews = giftPews;
	}
	
	public MailData toMailData() {
		MailData.Builder builder = MailData.newBuilder();
		builder.setFromuser(fromRoleName);
		builder.setSubject(subject);
		builder.setSentdate(sendDate);
		builder.setContent(content);
		if ( giftPews != null ) {
			for ( int giftPew : giftPews ) {
				builder.addGiftpews(giftPew);
			}
		}
		if ( gifts != null ) {
			for ( Reward gift : gifts ) {
				if ( gift != null ) {
					builder.addGifts(gift.toGift());
				}
			}
		}
		return builder.build();
	}
}
