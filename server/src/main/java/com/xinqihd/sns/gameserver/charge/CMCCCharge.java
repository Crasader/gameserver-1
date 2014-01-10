package com.xinqihd.sns.gameserver.charge;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.ChargePojo;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceCharge.BceCharge;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.http.HttpGameHandler;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.BillingJedis;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MessageFormatter;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 根据中国移动安卓开发规范1.0开发的HTTP计费同步接口:
 * 
		输入参数
		标识符	名称	类型	介质	来源	描述
		userId	用户伪码	String	HTTP	游戏业务平台	userId
		cpServiceId	业务代码	String	HTTP	游戏业务平台	计费业务代码
		consumeCode	消费代码	String	HTTP	游戏业务平台	道具计费代码
		cpParam	CP参数	String	HTTP	游戏业务平台	合作方透传参数
		hRet	状态外码	String	HTTP	游戏业务平台	平台计费结果（状态码外码）0-成功 1-失败
		status	状态内码	String	HTTP	游戏业务平台	状态信息（状态码内码），请参见附录C
		versionId	版本号	String	HTTP	游戏业务平台	版本号100
		transIDO	交易号	String	HTTP	游戏业务平台	事务流水号，长度17。
		返回值
		
		标识符	名称	类型	介质	来源	描述
		transIDO	版本号	String	HTTP	CP服务器	计费事务流水号
		hRet	返回码	String	HTTP	CP服务器	0-通知成功；
		其它-其他错误
		message	消息	String	HTTP	CP服务器	CP响应的消息，比如“Successful”或是合作方自定义的失败原因。
		1.1.1.1.	报文示例
		1、游戏业务平台请求CP服务器的消息
		<?xml version="1.0" encoding="UTF-8"?>
		<request>
			<hRet>0</hRet>
			<status>1800</status>
			<transIDO>12345678901234567</transIDO>
			<versionId>100</versionId>
			<userId>12345678</userId>
			<cpServiceId>120123002000</cpServiceId>
			<consumeCode>120123002001</consumeCode>
			<cpParam>0000000000000000</cpParam>
		</request>
		
		2、CP服务器的应答内容
		<?xml version="1.0" encoding="UTF-8"?>
		<response>
			<transIDO>12345678901234567</transIdo>
			<hRet>0</hRet>
			<message>Successful</message>
		</response>

 * 
 * 
 * @author wangqi
 *
 */
public class CMCCCharge {
	
	private static final String RESPONSE = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
					"<response>"+
					"	<transIDO>{}</transIDO>"+
					"	<hRet>{}</hRet>"+
					"	<message>Successful</message>"+
					"</response>";
	
	private static final Logger logger = LoggerFactory.getLogger(CMCCCharge.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");
	
	/**
	 * 中国移动在充值成功后，会回调这个接口
	 * 
	 * @param httpMessage
	 */
	public static final HttpMessage cmccChargeNotif(HttpMessage httpMessage, String variable) {
		String content = null;
		boolean success = true;
		if ( httpMessage == null ) {
			logger.warn("CMCC Charge HTTP is null");
			success = false;
		} else {
			content = httpMessage.getRequestContent();
			if ( content == null || content.length() <= 0 ) {
				logger.warn("CMCC Charge HTTP post content is null");
				if ( variable != null ) {
					content = variable;
				} else {
					success = false;
				}
			}
		}
		logger.info("CMCC Charge request is coming: {}", content);
		String transIDO = null;
		String rolename = null;
		if ( success ) {
			//success = false;
			String chargeDateStr = DateUtil.formatDateTime(new Date());
			String transId = Constant.EMPTY;
			String device = Constant.EMPTY;
			String billingIdentifier = Constant.EMPTY;

			try {
				BufferedReader br = new BufferedReader(new StringReader(content));
				XMLStreamReader xmlr = XMLInputFactory.newFactory().createXMLStreamReader(br);
				String eleName = null;
				String type = null;
				String hRet = null;
				String status = null;
				String versionId = null;
				String userId = null;
				String cpServiceId = null;
				String consumeCode = null;
				String cpParam = Constant.EMPTY;
				
				while ( xmlr.hasNext() ) {
					switch ( xmlr.next() ) {
						case XMLEvent.START_ELEMENT:
							eleName = xmlr.getLocalName();
							break;
						case XMLEvent.END_ELEMENT:
							eleName = null;
							break;
						case XMLEvent.CHARACTERS:
							if ( "hRet".equals(eleName)  ) {
								//hRet: 平台计费结果（状态码外码）0-成功 1-失败
								hRet = xmlr.getText();
							} else if ( "status".equals(eleName)  ) {
								status = xmlr.getText();
							} else if ( "transIDO".equals(eleName)  ) {
								transIDO = xmlr.getText();
							} else if ( "versionId".equals(eleName)  ) {
								versionId = xmlr.getText();
							} else if ( "userId".equals(eleName)  ) {
								userId = xmlr.getText();
							} else if ( "cpServiceId".equals(eleName)  ) {
								cpServiceId = xmlr.getText();
							} else if ( "consumeCode".equals(eleName)  ) {
								consumeCode = xmlr.getText();
							} else if ( "cpParam".equals(eleName)  ) {
								cpParam = xmlr.getText();
							}
							break;
						default:
							break;
					}
				}

				transId = cpParam.trim();
				/**
				 * Remove the '0'padding on left.
				 */
				int transactionId = StringUtil.toInt(transId, 0);
				transId = String.valueOf(transactionId);
				
				Jedis jedis = JedisFactory.getJedisDB();
				rolename = jedis.get(transId);
				if ( rolename == null ) {
					rolename = transId; 
				}
				if ( rolename == null ) {
					BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
							rolename, device, chargeDateStr, 0, 0, "unknown", 0, 0, 0, 0, "tranid invalid:".concat(cpParam)
					});
				} else {
					device = Constant.EMPTY;
					billingIdentifier = consumeCode.trim();
					ChargePojo chargePojo = ChargeManager.getInstance().getCharePojoByBillingIdentifier(billingIdentifier);
					int origYuanbao = 0;
					int totalYuanbao = 0;
					if ( chargePojo == null ) {
						BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
								rolename, device, chargeDateStr, 0, 0, "unknown", 0, origYuanbao, 0, totalYuanbao, "chargepojo notfound:".concat(billingIdentifier)
						});
					} else {
						if ( Constant.ZERO.equals(hRet) ) {
							String serverAndUserName = jedis.get(transId);
							if ( serverAndUserName == null ) {
								serverAndUserName = BillingJedis.getInstance().getValue(transId);
							}
							if ( serverAndUserName == null ) {
								BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
										//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "cmcc order fail"
										rolename, transId, device, chargeDateStr, 0, chargePojo.getPrice(), "rmb", 0, 0, 0, 0, "cmcc no transid: "+transId
								});
							} else {
								String[] fields = StringUtil.splitMachineId(serverAndUserName);
								String serverId = fields[0];
								String username = fields[1];
								
								User user = UserManager.getInstance().queryUser(username);
								if ( user == null ){
									BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
											rolename, device, chargeDateStr, 0, 0, "unknown", 0, origYuanbao, 0, totalYuanbao, "user notfound:".concat(rolename)
									});
								} else {
									SessionManager manager = GameContext.getInstance().getSessionManager();
									SessionKey userSessionKey = manager.findSessionKeyByUserId(user.get_id());
									if ( userSessionKey == null ) {
										userSessionKey = BillingJedis.getInstance().findSessionKeyByUserId(user.get_id());
									}
									if ( userSessionKey != null ) {
										//The user is online now
										String gameServerId = manager.findUserGameServerId(userSessionKey);
										if ( GameContext.getInstance().getGameServerId().equals(gameServerId) )  {
											ChargeManager.getInstance().doCharge(userSessionKey, user, consumeCode, chargePojo, 0, transId, true);
										} else {
											logger.info("The CMCC charge for user {} is at gameserver {}. orderid:{}", new Object[]{
													user.getRoleName(), gameServerId, transIDO
											});
											BceChargeInternal.Builder bceCharge = BceChargeInternal.newBuilder();
											bceCharge.setChargeid(chargePojo.getId());
											bceCharge.setUserid(user.get_id().toString());
											bceCharge.setFreecharge(false);
											bceCharge.setChannel("cmcc");
											bceCharge.setOrderid(transIDO);
											GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, bceCharge.build());									
										}
									} else {
										ChargeManager.getInstance().doCharge(null, user, transIDO, chargePojo, 0, transId, success);
									}
									success = true;
									logger.debug("CMCC charge success for transIDO:{}, user:{}", transIDO, rolename);
								}
							}
						} else {
							success = false;
							BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
									//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "cmcc order fail"
									rolename, transId, device, chargeDateStr, 0, chargePojo.getPrice(), "rmb", 0, 0, 0, 0, "cmcc hret fail"
							});
						}
					}
				}
				//Send response to client
				HttpMessage response = new HttpMessage();
				response.setResponseContentType(HttpGameHandler.CONTENT_XML_TYPE);
				String msg = null;
				if ( success ) {
					msg = MessageFormatter.format(RESPONSE, transIDO, Constant.ZERO).getMessage();
				} else {
					msg = MessageFormatter.format(RESPONSE, transIDO, Constant.ONE).getMessage();
				}
				//msg = MessageFormatter.format(RESPONSE, transIDO, Constant.ONE).getMessage();
				logger.debug("CMCC response:{}",msg);
				response.setResponseContent(msg.getBytes(Constant.ENC_UTF8));

				return response;
			} catch (Exception e) {
				BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
						rolename, device, chargeDateStr, 0, 0, "unknown", 0, 0, 0, 0, "exception:".concat(e.getMessage())
				});
				logger.warn("#CMCCCharge: fail to process user charge request", e);
			}
		}
		
		//Send fail response to client
		HttpMessage response = new HttpMessage();
		response.setResponseContentType(HttpGameHandler.CONTENT_XML_TYPE);
		String msg = MessageFormatter.format(RESPONSE, transIDO, Constant.ONE).getMessage();
		try {
			response.setResponseContent(msg.getBytes(Constant.ENC_UTF8));
		} catch (UnsupportedEncodingException e) {
		}

		return response;
	}
	
}
