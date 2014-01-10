package script.charge;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.charge.CMCCCharge;
import com.xinqihd.sns.gameserver.config.ChargePojo;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.http.HttpGameHandler;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.BillingJedis;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MessageFormatter;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class CMCC {
	
	private static final String RESPONSE = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
					"<response>"+
					"	<transIDO>{}</transIDO>"+
					"	<hRet>{}</hRet>"+
					"	<message>Successful</message>"+
					"</response>";
	
	private static final Logger logger = LoggerFactory.getLogger(CMCCCharge.class);
	private static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING");

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		String content = (String)parameters[0];
		logger.info("CMCC Charge request is coming: {}", content);
		String transIDO = null;
		String rolename = null;
		boolean success = true;
		ArrayList list = new ArrayList();
		if ( success ) {
			success = false;
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
						if ( true ) { //Constant.ZERO.equals(hRet) ) {
							logger.info("CMCC is sucessfully charged.");
							String serverAndUserName = jedis.get(transId);
							if ( serverAndUserName == null ) {
								BILLING_LOGGER.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", new Object[]{
										//orderId, username, device, chargeDateStr, amount, rmb_amount, currency, discount, origYuanbao, boughtYuanbao, 0, "cmcc order fail"
										rolename, transId, device, chargeDateStr, 0, chargePojo.getPrice(), "rmb", 0, 0, 0, 0, "cmcc order fail"
								});
							} else {
								String[] fields = StringUtil.splitMachineId(serverAndUserName);
								String serverId = fields[0];
								String username = fields[1];
								
								User user = UserManager.getInstance().queryUser(username);
								if ( user == null ){
									logger.info("The CMCC user is null {}", username);
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
											logger.info("The CMCC charge gameServerId {}", gameServerId);
											ChargeManager.getInstance().doCharge(userSessionKey, user, consumeCode, chargePojo, 0, transId, success);
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
										logger.info("The CMCC charge userSessionKey is null");
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
				logger.info("CMCC response:{}",msg);
				response.setResponseContent(msg.getBytes(Constant.ENC_UTF8));

				list.add(response);

				result = new ScriptResult();
				result.setType(ScriptResult.Type.SUCCESS_RETURN);
				result.setResult(list);
				return result;
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
		
		list.add(response);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
