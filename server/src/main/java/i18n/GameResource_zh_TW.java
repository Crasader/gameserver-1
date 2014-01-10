package i18n;

import java.util.ListResourceBundle;

import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;

public class GameResource_zh_TW extends ListResourceBundle {
	
	public static final String[][] messages = new String[][]{
		//register
		{"register.exist",   "昵稱已經被註冊了"},
		{"register.invalid", "昵稱至少包含3個字符"},
		{"register.toomuch", "昵稱別超過5個字符啊親"},
		{"register.badword", "昵稱中含有不文明用語"},
		{"register.password.less",    "您的密碼應至少包含5個字符"},
		{"register.password.confirm", "您兩次輸入的密碼不相同"},
		
		//login
		{ErrorCode.SUCCESS.desc(), "恭喜您登陸成功！"},
		{ErrorCode.NOTFOUND.desc(), "對不起，用戶名沒有找到"},
		{ErrorCode.WRONGPASS.desc(), "對不起，密碼錯誤"},
		{ErrorCode.MAXRETRY.desc(), "對不起，您已經超過最大重試次數，請15分鐘後再試"},
		{ErrorCode.ALREADY_LOGIN.desc(), "對不起，您已經在其他設備上登陸，請先退出後再登入。"},
		{ErrorCode.VERSION.desc(), "您的客戶端當前版本為{}，需要升級到版本{}後才能繼續遊戲"},
		{ErrorCode.OTHERS.desc(), "對不起，因系統原因無法讓您登陸"},
		{ErrorCode.S_PAUSE.desc(), "您的賬戶{}已經被系統管理員暫停{}"},
		{ErrorCode.S_REMOVED.desc(), "您的賬戶{}已經被系統管理員禁用"},
		{"login.more", "您已經在新的設備{}上登陸，當前連接將斷開"},
		
		//Common
		{"system", "系統"},
		{"gameadmin", "公告"},
		
		{"craft.transfer.level", "#009900‘{}’#e53333裝備與#009900‘{}’#e53333裝備不能跨級轉換啊親"},
		
		//notice
		{"notice.get_item", "#e53333恭喜您，獲得了#009900{}#e53333個#009900{}！"},
		{"notice.open_box", "#e53333玩家'#00d5ff{}#e53333'人品大爆發，從#009900{}#e53333中開出了#009900{}"},
		{"notice.get_war_item", "玩家'#00d5ff{}#e53333'通過戰鬥獲得#9933e5強化石Lv{}"},
		//玩家'{}'在進行水晶球占蔔時，幸運的獲得史詩品質器靈鏡盾
		{"notice.zhanpu", "#e53333玩家'#00d5ff{}#e53333'在進行水晶球占蔔時，幸運的獲得#9933e5{}"},
		//恭喜玩家[某某]成功熔煉出幸運手鐲+4
		{"notice.melt_item", "#e53333恭喜玩家'#00d5ff{}#e53333'成功熔煉出#9933e5幸運手鐲+{}！"},
		//玩家'{}'成功將泡泡手雷強化到9級，戰鬥力又上了一個新臺階
		{"notice.strength", "#e53333玩家'#00d5ff{}'#e53333成功將#009900{}#e53333強化到#337fe5{}#e53333級，戰鬥力又上了一個新臺階。"},
		{"notice.ranking.total.power",   "#e53333玩家'#00d5ff{}#e53333'的戰鬥力全球總排名上升了#9933e5{}#e53333位，現排名第#9933e5{}"},
		{"notice.ranking.total.level",   "#e53333玩家'#00d5ff{}#e53333'的等級全球總排名上升了#9933e5{}#e53333位，現排名第#9933e5{}"},
		{"notice.ranking.total.yuanbao", "#e53333玩家'#00d5ff{}#e53333'的元寶全球總排名上升了#9933e5{}#e53333位，現排名第#9933e5{}"},
		{"notice.ranking.total.medal",   "#e53333玩家'#00d5ff{}#e53333'的勛章全球總排名上升了#9933e5{}#e53333位，現排名第#9933e5{}"},
		{"notice.openItemBox", "#e53333玩家'#00d5ff{}#e53333'人品爆發，獲得了#9933e5{}{}#e53333枚"},
		{"notice.openEquipBox", "#e53333玩家'#00d5ff{}#e53333'人品爆發，獲得了強{}裝備'#9933e5{}'"},
		{"notice.achievement", "#e53333熱烈慶祝玩家'#00d5ff{}#e53333'解鎖了'#9933e5{}#e53333'成就"},
		{"notice.welcome", "#e53333熱烈歡迎玩家'#00d5ff{}#e53333'加入遊戲大家庭，鼓爪!"},
		{"notice.vip", "#e53333恭喜玩家'#00d5ff{}#e53333'成為VIP用戶!"},
		
		//BattleTool
		{"tool.guide",      "引導"},
		{"tool.fly",        "傳送"},
		{"tool.recover",    "生命恢復"},
		{"tool.energy",     "激怒"},
		{"tool.hidden",     "隱身"},
		{"tool.allhidden",  "團隊隱身"},
		{"tool.allrecover", "團隊恢復"},
		{"tool.iced",       "凍結冰彈"},
		{"tool.wind",       "改變風向"},
		
		//Bag
		{"bag.wear.expire",       "#e53333裝備'#9933e5{}#e53333'已經磨損，需要修復後才能使用"},
		{"bag.full",  "您的背包已滿，無法卸載裝備"},
		
		//Shop
		{"shop.error.fullbag",  "您的背包已滿，無法購買"},
		{"shop.error.nogold",   "您的金幣不足，無法購買"},
		{"shop.error.novoucher",   "您的禮券不足，無法購買"},
		{"shop.error.nomedal",   "您的勛章不足，無法購買"},
		{"shop.error.noyuanbao",   "您的元寶不足，無法購買"},
		{"shop.error.noprice",   "未找到商品對應的價格，無法購買"},
		{"shop.error.fullbag",  "您的背包已滿，無法購買"},
		{"shop.success",  		"恭喜您，商品購買成功！"},
		{"shop.equipexpire",  		"您有武器損壞了，請去背包中修理後使用"},
		{"shop.resubscribe",  		"'#9933e5{}#e53333'續費成功！"},
		{"shop.sellbinded",  		"'#9933e5{}#e53333'是綁定物品，無法出售"},
		{"shop.sellconfirm",  		"#000000您確定以#ff33e5{}#000000金幣的價格出售它嗎？"},
		
		//ranking
		{"ranking.total.power",  		"太棒了！您的戰鬥力全球總排名上升了{}位，現排名第{}"},
		{"ranking.total.level",  		"太棒了！您的等級全球總排名上升了{}位，現排名第{}"},
		{"ranking.total.yuanbao",   "太棒了！您的元寶全球總排名上升了{}位，現排名第{}"},
		{"ranking.total.medal",  		"太棒了！您的勛章全球總排名上升了{}位，現排名第{}"},
		
		{"ranking.total.power.down",    "哎呀！您的戰鬥力全球總排名下降了{}位，現排名第{}"},
		{"ranking.total.level.down",  	"哎呀！您的等級全球總排名下降了{}位，現排名第{}"},
		{"ranking.total.yuanbao.down",  "哎呀！您的元寶全球總排名下降了{}位，現排名第{}"},
		{"ranking.total.medal.down",  	"哎呀！您的勛章全球總排名下降了{}位，現排名第{}"},
		
		//prompt
		{"prompt.room.noweapon",      "弱弱的說一下，您還沒有裝備武器呢!"},
		{"prompt.room.weaponexpire",  "您的主武器'{}'已經損壞了，請用金幣或者元寶修復後再開始戰鬥吧!"},
		
		//box
		{"box.levelupbox.fail",  "您的等級需要達到{}級才能開寶箱:-("},
		
		//AI
		{"ai.chat.1",  "哼哼，讓你看看我的厲害"},
		
		//Charge
		{"charge.success",  "恭喜您，{}元寶已經充入您的賬戶！您的元寶總數量為{}"},
		{"charge.failure",  "哦，充值失敗了，請盡快聯系GM！"},
		
		//VIP
		{"vip.already", "您已經是VIP用戶，有效期至:{}"},
		{"vip.buy.success", "恭喜您成功購買了VIP身份，您的VIP有效期至:{}"},
		{"vip.buy.failure", "對不起，VIP購買失敗了，請您稍後再試"},
		{"vip.expire", "您的VIP身份已經到期了，請重新購買"},
		{"vip.nomoney", "您的的元寶{}不足，無法購買VIP"},
		
		//User
		{"user.level.up",   "恭喜，您已經升到了{}級!"},
		{"user.level.up10", "恭喜，您已經升到了{}級!, '青銅'類型武器已解鎖, 快去更新裝備吧"},
		{"user.level.up20", "恭喜，您已經升到了{}級!, '赤鋼'類型武器已解鎖, 快去更新裝備吧"},
		{"user.level.up30", "恭喜，您已經升到了{}級!, '白銀'類型武器已解鎖, 快去更新裝備吧"},
		{"user.level.up40", "恭喜，您已經升到了{}級!, '黃金'類型武器已解鎖, 快去更新裝備吧"},
		{"user.level.up50", "恭喜，您已經升到了{}級!, '琥珀'類型武器已解鎖, 快去更新裝備吧"},
		{"user.level.up60", "恭喜，您已經升到了{}級!, '翡翠'類型武器已解鎖, 快去更新裝備吧"},
		{"user.level.up70", "恭喜，您已經升到了{}級!, '水晶'類型武器已解鎖, 快去更新裝備吧"},
		{"user.level.up80", "恭喜，您已經升到了{}級!, '鉆石'類型武器已解鎖, 快去更新裝備吧"},
		{"user.level.up90", "恭喜，您已經升到了{}級!, '神聖'類型武器已解鎖, 快去更新裝備吧"},
		{"user.level.up100", "恭喜，您已經升到了{}級!, 達到了滿級！"},
		
		//Mail
		{"mail.template", "[{}]於{}給您發送了消息:{}"},
		
		//Gift
		{"gift", "親，禮物'{}'已經發送給您了哦，請去背包或狀態欄查看更新!"},
		{"gift.exp", "經驗值{}"},
		{"gift.golden", "金幣{}"},
		{"gift.yuanbao", "元寶{}"},
		
		//Time
		{"time.second", "{}秒"},
		{"time.minute", "{}分鐘"},
		{"time.hour", "{}小時"},
		{"time.day", "{}天"},
		{"time.week", "{}周"},
		{"time.month", "{}個月"},
		
		//Chat
		{"chat.freq", "您的發言太頻繁了，請升級VIP去除此限制"},
		
		//Confirm
		{"confirm.error", "您的確認失敗了，請再次嘗試該操作"},
		
		//Training
		{"trainer.name", "美女教官"},
		
		//Friend
		{"friend.found",    "已發現玩家'{}'，若要添加好友請按確定按鈕"},
		{"friend.notfound", "玩家'{}'未找到，請用全名並註意區分大小寫"},
		{"friend.add",      "您已經成功添加玩家'{}'作為好友"},
		{"friend.beingadd", "#e53333玩家#00d5ff'{}'#e53333將您添加為好友了"},
		{"friend.notself",  "您不能添加自己做好友"},
		{"friend.delete",   "您已經成功刪除好友{}"},
		{"friend.already",  "#ffffff您和#00d5ff{}#ffffff已經是好友了"},
		{"friend.challenge", "#e53333您的朋友#00d5ff{}#e53333想挑戰您，要不要滅他?"},
		{"friend.cooperate", "#e53333您的朋友#00d5ff{}#e53333想與您合作戰鬥，是否同意呢?"},
		{"friend.challenge.no", "#e53333您的朋友#00d5ff{}#e53333嚇的尿褲了，不敢應戰"},
		{"friend.inroom",   "#e53333您的朋友#00d5ff{}#e53333已經加入對戰了"},
		
		{"battlemap.vip",   "本次戰鬥選擇了VIP玩家{}的地圖"},
		{"battlemap.choose","本次戰鬥選擇了玩家{}的地圖"},
		
		{"room.unsupport",  "您請求的房間類型暫不支持，敬請期待"},
		
		{"strength.max",  "您的裝備已達最大強化上限,無法繼續強化"},
		
		//TODO need extra translation
		
		//Forget password
		{"forget.fail",    "因為系統故障暫時不能發送臨時密碼，請稍後再試"},
		{"forget.noname",  "您需要填寫昵稱才能找回密碼"},
		{"forget.nouser",  "該昵稱未找到"},
		{"forget.noemail", "您註冊時未填寫有效郵箱，無法推送臨時密碼"},
		{"forget.ok",      "臨時密碼已經發送到您的郵箱，請在30分鐘內登陸遊戲，否則密碼失效"},
		{"forget.subject", "小小飛彈忘記密碼找回"},
		{"forget.content", "<html>尊敬的{}，您好,<br><br>  您的遊戲臨時密碼為'{}'，請於30分鐘內登陸遊戲，在'完善信息'面板中修改密碼。<br><br>小小飛彈遊戲團隊</html>"},
		
		//Email
		{"email.invalid",  "您填寫的電子郵件不是有效格式哦"},
		{"email.no",       "如果您填寫了有效的郵件，不僅可以在忘記密碼後找回，還可以領取豐厚獎勵哦親"},
		{"email.subject",  "歡迎您進入小小飛彈的世界"},
		{"email.content",  "<html>尊敬的{}，您好,<br><br>  歡迎您進入小小飛彈的世界，請猛擊鏈接:<a href=\"http://{}\">郵件確認</a>確認您的電子郵件， 可領取豐厚遊戲獎勵，祝您遊戲愉快！<br><br>小小飛彈遊戲團隊</html>"},
		{"email.verified",    "<html><head><meta http-equiv='refresh' content='3; URL={}'></head><body>郵件地址已經確認，請您去遊戲中領取任務獎勵！正在為您跳轉官網...</body></html>"},
		{"email.notverified", "<html><head><meta http-equiv='refresh' content='3; URL={}'></head><body>郵件地址無法確認，請確認您填寫了正確的郵件地址。 正在為您跳轉官網...</body></html>"},
		{"email.reward",   "恭喜您成功綁定了郵箱，禮物已經發送到您的背包"},
		{"email.sent",     "已經向您的郵箱發送了驗證郵件，驗證後可領取豐厚獎勵"},
		
		//Weibo
		{"weibo.levelup.0",  "哈哈，我在#小小飛彈#裏升級啦，現在{}級，升級好快喔！http://xinqihd.com"},
		{"weibo.levelup.1",  "吼吼，升級到{}級了，#小小飛彈#裏升級那是相當容易的一件事，一起來玩吧！http://xinqihd.com"},
		{"weibo.levelup.2",  "升級咯，為了慶祝自己升到{}級，我決定再升一級，嗯，再升一級就好了！http://xinqihd.com"},

		{"weibo.ach.0", "我在#小小飛彈#裏達成成就“{}”拉，太給力了！http://xinqihd.com" },
		{"weibo.ach.1", "達成成就“{}”，#小小飛彈#真好玩，有那麽多獨特的稱號！http://xinqihd.com" },

		{"weibo.str.0", "我在#小小飛彈#中將裝備{}強化到{}級了，吼吼~人品爆發啊…http://xinqihd.com" },
		{"weibo.str.1", "{}強化到{}級了，#小小飛彈#裏我也可以挑戰高富帥。http://xinqihd.com" },

		{"weibo.win.0", "春風吹，戰鼓擂，我在#小小飛彈#裏不怕誰！http://xinqihd.com" },
		{"weibo.win.1", "又輕松戰勝一個對手，證明我不是新手，想要挑戰我，就來#小小飛彈#吧！http://xinqihd.com" },
		{"weibo.win.2", "又贏了，#小小飛彈#裏人稱東方不敗，趕緊來吧，求虐…http://xinqihd.com" },

		{"weibo.rank.0", "嘿~排名又上升了，#小小飛彈#裏可以看到你每一天的成長，真的很好玩！http://xinqihd.com"},
		{"weibo.rank.1", "排名又上升了，浮雲啊浮雲，#小小飛彈#精彩等你來挑戰！http://xinqihd.com"},
		//（排名前10的時調用）
		{"weibo.rank.no10", "偶也！排名進前10了，怎麽說我在#小小飛彈#裏也算個高手了！http://xinqihd.com"},
		//（排名第一時調用）
		{"weibo.rank.no1", "我在#小小飛彈#裏排名第一拉，作為高手我已經了寂寞，哎..沒有對手啊！http://xinqihd.com"},

		/*
		{"hurt.critical", "暴擊"},
		{"hurt.complete", "精確打擊"},
		{"hurt.light", "輕微命中"},
		{"hurt.frozen", "冰凍"},
		{"hurt.unfrozen", "解凍"},
		{"hurt.death", "致命一擊"},
		{"hurt.guide", "引導命中"},
		*/
		
		{"bag.levelhigh", "您選擇的物品需要等級{}及以上才能裝備"},
		
		{"notice.openEquipBox.pink", "#e53333玩家'#00d5ff{}#e53333'人品爆發，獲得了粉色裝備'#e4347e{}'"},
		{"notice.openEquipBox.orange", "#e53333玩家'#00d5ff{}#e53333'人品爆發，獲得了橙色裝備'#fe9f27{}'"},
	};

	@Override
	protected Object[][] getContents() {
		return messages;
	}

}
