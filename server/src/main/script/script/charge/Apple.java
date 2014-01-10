package script.charge;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.util.Base64;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.ChargePojo;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.JSON;
import com.xinqihd.sns.gameserver.util.MessageFormatter;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class Apple {
	
	private static final String IAP_SANDBOX_VERIFY = "https://sandbox.itunes.apple.com/verifyReceipt";
	private static final String IAP_VERIFY = "https://buy.itunes.apple.com/verifyReceipt";

	private static final String IAP_JSON = "{ \"receipt-data\" : \"{}\"}";
	
	private static URL iapVerifyURL = null;
	
	static {
		try {
			iapVerifyURL = new URL(IAP_VERIFY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(Apple.class);
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		
		final User user = (User)parameters[0];
		final String receipt = (String)parameters[1];
		final String token = (String)parameters[2];
		
		boolean success = false;
		ChargePojo chargePojo;
		//Apple IAP
		String billingId = receipt;
		chargePojo = ChargeManager.getInstance().getCharePojoByBillingIdentifier(billingId);
		final ChargePojo finalChargePojo = chargePojo; 
		if ( finalChargePojo != null ) {
			success = true;
			//Check if the appstore use server authentication method.
			boolean serverAuthMethod = false;
			if ( StringUtil.checkNotEmpty(token) ) {
				serverAuthMethod = true;
				/*
				if ( token.length() > 500 ) {
					/**
					 * 苹果升级1.8.1后，用token存储了苹果发送的验证字符串，所以
					 * 需要进行判断
					 * 
					 * 2013-2-4
					 * /
					serverAuthMethod = true;
				} else {
					serverAuthMethod = false;
					if ( token.startsWith("4743487") ) {
						success = false;
					} else if ( token.startsWith("com.urus.iap.") ) {
						success = false;
					} else if ( token.charAt(0) < '0' ||token.charAt(0) > '9' ) {
						success = false;
					} else if ( token.length()<11 || token.length() > 18 ) {
						success = false;
					}
				}
				*/
			}
			if ( !success ) {
				SysMessageManager.getInstance().sendClientInfoMessage(user.getSessionKey(), "apple.invalid.iap", Type.NORMAL);
				AccountManager.getInstance().forbiddenAccount(user.getAccountName(), null);
			}
			if ( serverAuthMethod ) {
				/**
				 * 2013-2-1升级的苹果1.8.1启动了服务器验证的充值方式
				 */
				final String appleInvoice = token;
				if ( !checkIAPReceipt(
						user, chargePojo.getBillingIdentifier(), appleInvoice) ) {
					SysMessageManager.getInstance().sendClientInfoMessage(user.getSessionKey(), "apple.invalid.iap", Type.NORMAL);
					AccountManager.getInstance().forbiddenAccount(user.getAccountName(), null);
				} else {
					ChargeManager.getInstance().submitTask(new Runnable(){
						public void run() {
							String response = verifyIAP(appleInvoice);
							DBObject json = (DBObject)JSON.parse(response);
							String value = String.valueOf(json.get("status"));
							String orderId = StringUtil.concat(new Object[]{
									user.getRoleName(), Constant.COLON, System.currentTimeMillis()	
							});
							logger.info("Apple check result code:{} for user:{}", value, user.getRoleName());
							if ( Constant.ZERO.equals(value) ) {
								ChargeManager.getInstance().doCharge(user.getSessionKey(), user, orderId, finalChargePojo, 0, value, true);
							} else {
								if ( "21007".equals(value) ) {
									//21007 收据信息是测试用（sandbox），但却被发送到产品环境中验证
									//sandbox默认成功
									ChargeManager.getInstance().doCharge(user.getSessionKey(), user, orderId, finalChargePojo, 0, value, true);	
								}
							}
						}
					});
				}
			} else {
				/**
				 * 为保持兼容，之前的1.8.0苹果仍然采用直冲方式
				 */
				ChargeManager.getInstance().doCharge(user.getSessionKey(), user, token, chargePojo, 0, "test_iap", success);
			}
		} else {
			logger.warn("Apple IAP has not found billingId:{}", receipt);
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
	/**
	 * Receive iOS client's receipt data got from Apple Server
	 * and verify it before adding money to users.
	 *  
	 * @param receiptData
	 * @return
	 */
	public static String verifyIAP(String receiptData) {
		if ( StringUtil.checkNotEmpty(receiptData) ) {
			String json = MessageFormatter.format(IAP_JSON, receiptData).getMessage();
			logger.info("IAP json: {}", json);
			if ( iapVerifyURL != null ) {
				try {
					HttpURLConnection conn = (HttpURLConnection)iapVerifyURL.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);
					OutputStream os = conn.getOutputStream();
					os.write(json.getBytes());
					os.flush();
					int code = conn.getResponseCode();
					if ( code == 200 ) {
						BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
						ByteArrayOutputStream baos = new ByteArrayOutputStream(30);
						int b = is.read();
						while ( b >= 0 ) {
							baos.write(b);
							b = is.read();
						}
						String response = new String(baos.toByteArray());
						return response;
					} else {
						logger.debug("Failed to verify iap receipt data. Http code: {}", code);
					}
				} catch (Exception e) {
					logger.debug("Failed to verify iap receipt data because of {}", e.toString());
				}
			}
		} else {
			logger.debug("#verifyIAP: receipt data is empty");
		}
		return null;
	}

	/**
	 * Check if the receipt sent by client is for us and valid.
	 * 
	 * @param json
	 * @return
	 */
	public static boolean checkIAPReceipt(User user, String productId, String json) {
		try {
			String roleName = Constant.EMPTY;
			if ( user != null ) {
				roleName = user.getRoleName();
			}
			String decodeJson = new String(Base64.decodeFast(json));

			decodeJson = decodeJson.replaceAll("=", ":");
			decodeJson = decodeJson.replaceAll(";", ",");
			Object obj = com.alibaba.fastjson.JSON.parse(decodeJson);
//			String receipt = ((Map)obj).get("receipt-data").toString();
//			/**
//			 * receipt:
//			 * {
//					"signature" = "ApdxJdtNwPU2rA5/cn3kIO1OTk25feDKa0aagyyRveWlcFlglv6RF6znkiBS3um9Uc7pVob+PqZR2T8wyVrHNplof3DX3IqDOlWq+90a7Yl+qrR7A7jWwviw708PS+67PyHRnhO/G7bVqgRpEr6EuFybiU1FXAiXJc6ls1YAssQxAAADVzCCA1MwggI7oAMCAQICCGUUkU3ZWAS1MA0GCSqGSIb3DQEBBQUAMH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQKDApBcHBsZSBJbmMuMSYwJAYDVQQLDB1BcHBsZSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEzMDEGA1UEAwwqQXBwbGUgaVR1bmVzIFN0b3JlIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MB4XDTA5MDYxNTIyMDU1NloXDTE0MDYxNDIyMDU1NlowZDEjMCEGA1UEAwwaUHVyY2hhc2VSZWNlaXB0Q2VydGlmaWNhdGUxGzAZBgNVBAsMEkFwcGxlIGlUdW5lcyBTdG9yZTETMBEGA1UECgwKQXBwbGUgSW5jLjELMAkGA1UEBhMCVVMwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMrRjF2ct4IrSdiTChaI0g8pwv/cmHs8p/RwV/rt/91XKVhNl4XIBimKjQQNfgHsDs6yju++DrKJE7uKsphMddKYfFE5rGXsAdBEjBwRIxexTevx3HLEFGAt1moKx509dhxtiIdDgJv2YaVs49B0uJvNdy6SMqNNLHsDLzDS9oZHAgMBAAGjcjBwMAwGA1UdEwEB/wQCMAAwHwYDVR0jBBgwFoAUNh3o4p2C0gEYtTJrDtdDC5FYQzowDgYDVR0PAQH/BAQDAgeAMB0GA1UdDgQWBBSpg4PyGUjFPhJXCBTMzaN+mV8k9TAQBgoqhkiG92NkBgUBBAIFADANBgkqhkiG9w0BAQUFAAOCAQEAEaSbPjtmN4C/IB3QEpK32RxacCDXdVXAeVReS5FaZxc+t88pQP93BiAxvdW/3eTSMGY5FbeAYL3etqP5gm8wrFojX0ikyVRStQ+/AQ0KEjtqB07kLs9QUe8czR8UGfdM1EumV/UgvDd4NwNYxLQMg4WTQfgkQQVy8GXZwVHgbE/UC6Y7053pGXBk51NPM3woxhd3gSRLvXj+loHsStcTEqe9pBDpmG5+sk4tw+GK3GMeEN5/+e1QT9np/Kl1nj+aBw7C0xsy0bFnaAd1cSS6xdory/CUvM6gtKsmnOOdqTesbp0bs8sn6Wqs0C9dgcxRHuOMZ2tm8npLUm7argOSzQ==";
//					"purchase-info" = "ewoJIm9yaWdpbmFsLXB1cmNoYXNlLWRhdGUtcHN0IiA9ICIyMDEyLTA3LTEyIDA1OjU0OjM1IEFtZXJpY2EvTG9zX0FuZ2VsZXMiOwoJInB1cmNoYXNlLWRhdGUtbXMiID0gIjEzNDIwOTc2NzU4ODIiOwoJIm9yaWdpbmFsLXRyYW5zYWN0aW9uLWlkIiA9ICIxNzAwMDAwMjk0NDk0MjAiOwoJImJ2cnMiID0gIjEuNCI7CgkiYXBwLWl0ZW0taWQiID0gIjQ1MDU0MjIzMyI7CgkidHJhbnNhY3Rpb24taWQiID0gIjE3MDAwMDAyOTQ0OTQyMCI7CgkicXVhbnRpdHkiID0gIjEiOwoJIm9yaWdpbmFsLXB1cmNoYXNlLWRhdGUtbXMiID0gIjEzNDIwOTc2NzU4ODIiOwoJIml0ZW0taWQiID0gIjUzNDE4NTA0MiI7CgkidmVyc2lvbi1leHRlcm5hbC1pZGVudGlmaWVyIiA9ICI5MDUxMjM2IjsKCSJwcm9kdWN0LWlkIiA9ICJjb20uemVwdG9sYWIuY3RyYm9udXMuc3VwZXJwb3dlcjEiOwoJInB1cmNoYXNlLWRhdGUiID0gIjIwMTItMDctMTIgMTI6NTQ6MzUgRXRjL0dNVCI7Cgkib3JpZ2luYWwtcHVyY2hhc2UtZGF0ZSIgPSAiMjAxMi0wNy0xMiAxMjo1NDozNSBFdGMvR01UIjsKCSJiaWQiID0gImNvbS56ZXB0b2xhYi5jdHJleHBlcmltZW50cyI7CgkicHVyY2hhc2UtZGF0ZS1wc3QiID0gIjIwMTItMDctMTIgMDU6NTQ6MzUgQW1lcmljYS9Mb3NfQW5nZWxlcyI7Cn0=";
//					"pod" = "17";
//					"signing-status" = "0";
//				}
//			 */
//			String decodeReceipt = new String(Base64.decodeFast(receipt));
//			decodeReceipt = decodeReceipt.replaceAll("=", ":");
//			decodeReceipt = decodeReceipt.replaceAll(";", ",");
//			obj = com.alibaba.fastjson.JSON.parse(decodeReceipt);
			String purchaseInfo = ((Map)obj).get("purchase-info").toString();
			
			String decodePurchaseInfo = new String(Base64.decodeFast(purchaseInfo));
			/*
				{
					"original-purchase-date-pst" = "2012-07-12 05:54:35 America/Los_Angeles";
					"purchase-date-ms" = "1342097675882";
					"original-transaction-id" = "170000029449420";
					"bvrs" = "1.4";
					"app-item-id" = "450542233";
					"transaction-id" = "170000029449420";
					"quantity" = "1";
					"original-purchase-date-ms" = "1342097675882";
					"item-id" = "534185042";
					"version-external-identifier" = "9051236";
					"product-id" = "com.zeptolab.ctrbonus.superpower1";
					"purchase-date" = "2012-07-12 12:54:35 Etc/GMT";
					"original-purchase-date" = "2012-07-12 12:54:35 Etc/GMT";
					"bid" = "com.zeptolab.ctrexperiments";
					"purchase-date-pst" = "2012-07-12 05:54:35 America/Los_Angeles";
				}
			 */
			decodePurchaseInfo = decodePurchaseInfo.replaceAll("=", ":");
			decodePurchaseInfo = decodePurchaseInfo.replaceAll(";", ",");
			logger.info("User {} decode purchase-info: {}", roleName, decodePurchaseInfo);
			obj = com.alibaba.fastjson.JSON.parse(decodePurchaseInfo);
			String pid = ((Map)obj).get("product-id").toString();
			String purchaseDate = ((Map)obj).get("purchase-date-pst").toString();
			Date date = DateUtil.parseApplePurchaseDate(purchaseDate);
			if ( productId.equals(pid) ) {
				long currentMillis = System.currentTimeMillis();
				int timeDiff = (int)(currentMillis - date.getTime());
				if ( timeDiff > 0 && timeDiff < 86400000 ) {
					return true;
				} else {
					logger.info("The {} use a fake purchaseDate:{}", roleName, purchaseDate);
				}
			} else {
				logger.info("The {} use a fake productId:{}", roleName, pid);
			}
		} catch (Exception e) {
			logger.warn("Failed to check IAP receipt json:{}, exception:{}", json, e.getMessage());
			logger.info("Parse IAP receipt exception: ", e);
		}
		return false;
	}
}
