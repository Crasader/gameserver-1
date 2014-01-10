package i18n;

import java.awt.Color;
import java.util.ListResourceBundle;

import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.util.Text;

public class GameResource extends ListResourceBundle {

	public static final String[][] messages = new String[][] {
			// register
			{ "register.exist", "昵称已经被注册了" },
			{ "register.invalid", "名称应包含3-8个字符,别含有空格和##哟" },
			{ "register.toomuch", "昵称别超过5个字符啊亲" },
			{ "register.badword", "昵称中含有不文明用语" },
			{ "register.password.less", "您的密码应至少包含5个字符" },
			{ "register.password.confirm", "您两次输入的密码不相同" },
			{ "register.login.invalid", "您的登陆已经过期了,请重新登陆后创建角色" },

			// login
			{ ErrorCode.SUCCESS.desc(), "恭喜您登陆成功！" },
			{ ErrorCode.NOTFOUND.desc(), "对不起,用户名没有找到" },
			{ ErrorCode.WRONGPASS.desc(), "对不起,密码错误" },
			{ ErrorCode.MAXRETRY.desc(), "对不起,您已经超过最大重试次数,请15分钟后再试" },
			{ ErrorCode.ALREADY_LOGIN.desc(), "对不起,您已经在其他设备上登陆,请先退出后再登入。" },
			{ ErrorCode.VERSION.desc(), "您的客户端当前版本为{},需要升级到版本{}后才能继续游戏" },
			{ ErrorCode.OTHERS.desc(), "对不起,因系统原因无法让您登陆" },
			{ ErrorCode.S_PAUSE.desc(), "您的账户{}已经被系统管理员暂停{}" },
			{ ErrorCode.S_REMOVED.desc(), "您的账户已经被系统管理员禁用" },
			{ ErrorCode.THIRD_REG.desc(), "您需要先创建角色再进行游戏" },
			{ "login.more", "您已经在新的设备{}上登陆,当前连接将断开" },

			{ "account.forbidden", "您的登陆账号已经被禁用,如有问题请联系GM(QQ:418313138)" },
			{ "account.deleterole", "您真的确定要删除角色'{}'吗？角色删除后所有的数据会被清除并且无法恢复,请再次确认！" },

			// Common
			{ "system", "#ff0000系统" },
			{ "gameadmin", "公告" },
			{ "notice", "#ff0000通知" },

			// notice
			{ "notice.get_item", "#ffffff恭喜您,获得了#009900{}#ffffff个#009900{}！" },
			{ "notice.open_box",
					"#e53333玩家'#00d5ff{}#e53333'人品大爆发,从#009900{}#e53333中开出了#009900{}" },
			{ "notice.get_war_item", "玩家'#00d5ff{}#e53333'通过战斗获得#9933e5强化石Lv{}" },
			// 玩家'{}'在进行水晶球占卜时,幸运的获得史诗品质器灵镜盾
			{ "notice.zhanpu", "#e53333玩家'#00d5ff{}#e53333'在进行水晶球占卜时,幸运的获得#9933e5{}" },
			// 恭喜玩家[某某]成功熔炼出幸运手镯+4
			{ "notice.melt_item", "#e53333恭喜玩家'#00d5ff{}#e53333'成功熔炼出#9933e5幸运手镯+{}！" },
			// 玩家'{}'成功将泡泡手雷强化到9级,战斗力又上了一个新台阶
			{
					"notice.strength",
					"#e53333玩家'#00d5ff{}'#e53333成功将#009900{}#e53333强化到#337fe5{}#e53333级,战斗力又上了一个新台阶。" },
			{ "notice.ranking.total.power",
					"#e53333玩家'#00d5ff{}#e53333'的战斗力全球总排名上升了#9933e5{}#e53333位,现排名第#9933e5{}" },
			{ "notice.ranking.total.level",
					"#e53333玩家'#00d5ff{}#e53333'的杀敌全球总排名上升了#9933e5{}#e53333位,现排名第#9933e5{}" },
			{ "notice.ranking.total.yuanbao",
					"#e53333玩家'#00d5ff{}#e53333'的元宝全球总排名上升了#9933e5{}#e53333位,现排名第#9933e5{}" },
			{ "notice.ranking.total.medal",
					"#e53333玩家'#00d5ff{}#e53333'的功勋全球总排名上升了#9933e5{}#e53333位,现排名第#9933e5{}" },
			{ "notice.openItemBox",
					"#e53333玩家'#00d5ff{}#e53333'人品爆发,获得了#9933e5{}#e53333{}枚" },
			{ "notice.openEquipBox",
					"#e53333玩家'#00d5ff{}#e53333'人品爆发,获得了强{}装备'#9933e5{}'" },
			{ "notice.achievement",
					"#e53333玩家'#00d5ff{}#e53333'解锁了'#9933e5{}#e53333'成就" },
			{ "notice.task", "#e53333玩家'#00d5ff{}#e53333'完成了'#9933e5{}#e53333'任务" },
			{ "notice.welcome", "#e53333热烈欢迎玩家'#00d5ff{}#e53333'加入游戏大家庭,鼓爪!" },
			{ "notice.vip", "#e53333恭喜玩家'#00d5ff{}#e53333'成为尊贵的VIP{}用户!" },
			{ "notice.vip.login",
					"#e53333欢迎尊贵的#9933e5VIP{}#e53333级玩家'#00d5ff{}#e53333'登陆游戏!" },

			// BattleTool
			{ "tool.guide", "引导" },
			{ "tool.fly", "传送" },
			{ "tool.recover", "生命恢复" },
			{ "tool.energy", "激怒" },
			{ "tool.hidden", "隐身" },
			{ "tool.allhidden", "团队隐身" },
			{ "tool.allrecover", "团队恢复" },
			{ "tool.iced", "冻结冰弹" },
			{ "tool.wind", "改变风向" },

			// Bag
			{ "bag.add", "新的装备'{}'已经加入您的背包" },
			{ "bag.wear.expire", "#e53333装备'#9933e5{}#e53333'已经磨损,需要修复后才能使用" },
			{ "bag.full", "您的背包已满,无法卸载装备" },
			{ "bag.near.full", "您的背包还有{}个空间就要满了,背包装满后新获取的装备可能会丢失,请您清理背包或者升级VIP获得更大空间" },
			{ "bag.add.full", "您的背包已满了,新的装备不能再添加进去,请立即整理或者升级VIP获得更大空间" },
			{ "bag.pickreward.full", "您的背包满了,无法放入新的装备,请尽快清理。升级VIP可获得更大容量！" },
			{ "bag.wear.banded", "装备{}已经绑定到其他玩家,您无法使用" },

			// Shop
			{ "shop.error.fullbag", "您的背包已满" },
			{ "shop.error.nogold", "您的金币不足啦,可以通过每日祈福增加金币哟" },
			{ "shop.error.novoucher", "您的礼券不足" },
			{ "shop.error.nomedal", "您的功勋不足" },
			{ "shop.error.noyuanbao", "您的元宝不足" },
			{ "shop.error.noguildcredit", "您的公会财富" },
			{ "shop.error.noprice", "未找到商品对应的价格,无法购买" },
			{ "shop.error.fullbag", "您的背包已满,无法购买" },
			{ "shop.error.wrongcount", "您选择的卖出数量{}不正确" },
			{ "shop.success", "恭喜您,商品购买成功！" },
			{ "shop.equipexpire", "您有武器损坏了,请去背包中修理后使用" },
			{ "shop.resubscribe", "'#9933e5{}#e53333'续费成功！" },
			{ "shop.sellbinded", "'#9933e5{}#e53333'是绑定物品,无法出售" },
			{ "shop.sellconfirm",
					"#FFFFFF您确定以#ff33e5{}#FFFFFF金币的价格出售'#9933e5{}#FFFFFF'吗？" },
			{
					"shop.sellconfirm.strength",
					"#FFFFFF您准备卖掉的武器是强化{}级的装备,建议先把强化等级通过铁匠铺转移到其他任意装备上。您确定以#ff33e5{}#FFFFFF金币的价格出售'#9933e5{}#FFFFFF'吗？" },

			// ranking
			{ "ranking.total.power", "太棒了！您的战斗力全球总排名上升了{}位,现排名第{}" },
			{ "ranking.total.level", "太棒了！您的杀敌数全球总排名上升了{}位,现排名第{}" },
			{ "ranking.total.yuanbao", "太棒了！您的元宝全球总排名上升了{}位,现排名第{}" },
			{ "ranking.total.medal", "太棒了！您的功勋全球总排名上升了{}位,现排名第{}" },

			{ "ranking.total.power.down", "哎呀！您的战斗力全球总排名下降了{}位,现排名第{}" },
			{ "ranking.total.level.down", "哎呀！您的杀敌数全球总排名下降了{}位,现排名第{}" },
			{ "ranking.total.yuanbao.down", "哎呀！您的元宝全球总排名下降了{}位,现排名第{}" },
			{ "ranking.total.medal.down", "哎呀！您的功勋全球总排名下降了{}位,现排名第{}" },

			// prompt
			{ "prompt.room.noweapon", "弱弱的说一下,您还没有装备武器呢!" },
			{ "prompt.room.notexist", "您的房间出现了问题，请退出房间后后重新进入一次" },
			{ "prompt.room.weaponexpire", "您的主武器'{}'已经损坏了,请用金币或者元宝修复后再开始战斗吧!" },

			// box
			{ "box.levelupbox.fail", "您的等级需要达到{}级才能开宝箱:-(" },
			{ "box.not.roleaction", "您选中的道具不是体力卡" },

			// AI
			{ "ai.chat.1", "哼哼,让你看看我的厉害" },

			// Charge
			{ "charge.success", "恭喜您,{}元宝已经充入您的账户！您的元宝总数量为{}" },
			{ "charge.failure", "充值失败了,请尽快联系GM！" },

			// User
			{ "user.level.up.unlock", "恭喜,您已经升到了{}级,装备{}已经解锁！" },
			{ "user.level.up", "恭喜,您已经升到了{}级!" },
			{ "user.level.up10", "恭喜,您已经升到了{}级!, '青铜'类型武器已解锁, 快去更新装备吧" },
			{ "user.level.up20", "恭喜,您已经升到了{}级!, '赤钢'类型武器已解锁, 快去更新装备吧" },
			{ "user.level.up30", "恭喜,您已经升到了{}级!, '白银'类型武器已解锁, 快去更新装备吧" },
			{ "user.level.up40", "恭喜,您已经升到了{}级!, '黄金'类型武器已解锁, 快去更新装备吧" },
			{ "user.level.up50", "恭喜,您已经升到了{}级!, '琥珀'类型武器已解锁, 快去更新装备吧" },
			{ "user.level.up60", "恭喜,您已经升到了{}级!, '翡翠'类型武器已解锁, 快去更新装备吧" },
			{ "user.level.up70", "恭喜,您已经升到了{}级!, '水晶'类型武器已解锁, 快去更新装备吧" },
			{ "user.level.up80", "恭喜,您已经升到了{}级!, '钻石'类型武器已解锁, 快去更新装备吧" },
			{ "user.level.up90", "恭喜,您已经升到了{}级!, '神圣'类型武器已解锁, 快去更新装备吧" },
			{ "user.level.up100", "恭喜,您已经升到了{}级!, 达到了满级！" },

			// Mail
			{ "mail.template", "[{}]于{}给您发送了消息:{}" },

			// Gift
			{ "gift", "亲,礼物'{}'已经发送给您了哦,请去背包或状态栏查看更新!" },
			{ "gift.exp", "经验值{}" },
			{ "gift.golden", "金币{}" },
			{ "gift.yuanbao", "元宝{}" },

			// Time
			{ "time.second", "{}秒" },
			{ "time.minute", "{}分钟" },
			{ "time.hour", "{}小时" },
			{ "time.day", "{}天" },
			{ "time.week", "{}周" },
			{ "time.month", "{}个月" },

			// Cooldown time
			{ "chat.freq", "您的发言太频繁了,请升级VIP去除此限制" },

			// Confirm
			{ "confirm.error", "您的确认失败了,请再次尝试该操作" },

			// Training
			{ "trainer.name", "美女教官" },

			// Friend
			{ "friend.found", "已发现玩家'{}',若要添加好友请按确定按钮" },
			{ "friend.notfound", "玩家'{}'未找到,请用全名并注意区分大小写" },
			// {"friend.add", "您已经成功添加玩家'{}'作为好友"},
			{ "friend.beingadd", "#e53333玩家#00d5ff'{}'#e53333将您添加为好友了" },
			{ "friend.notself", "您不能添加自己做好友" },
			{ "friend.delete", "您已经成功删除好友{}" },
			{ "friend.already", "#ffffff您和#00d5ff{}#ffffff已经是好友了" },
			{ "friend.challenge", "#e53333您的朋友#00d5ff{}#e53333想挑战您,要不要灭他?" },
			{ "friend.self", "#e53333您不能向自己发起挑战" },
			{ "friend.cooperate", "#e53333您的朋友#00d5ff{}#e53333想与您合作战斗,是否同意呢?" },
			{ "friend.challenge.no", "#e53333您的朋友#00d5ff{}#e53333吓的尿裤了,不敢应战" },
			// {"friend.inroom", "#e53333您的朋友#00d5ff{}#e53333已经加入对战了"},
			{ "friend.kickfirst", "您要邀请的玩家已经加入队伍了,如果需要变更位置需要先将他踢出房间后再次邀请" },
			{ "friend.offline.confirm", "您选择的好友已经下线了,是否发起离线挑战？" },

			{ "battlemap.vip", "本次战斗选择了VIP玩家{}的地图" },
			{ "battlemap.choose", "本次战斗选择了玩家{}的地图" },

			{ "room.denyauto", "当前的战斗类型不支持自动战斗模式" },
			{ "room.unsupport", "您请求的房间类型暂不支持,敬请期待" },
			{ "room.kick.notowner", "您不是房主，不能踢出其他玩家" },
			{ "room.kick.otherroom", "您不能踢出对方玩家,可以退出单人对战再进入一次。" },

			{ "strength.nogodstone", "您使用的神恩符Lv{}只能用于强化{}级及以下装备" },
			{ "strength.max", "您的装备已达最大强化上限,无法继续强化" },
			{ "strength.outrange", "该装备的等级超过了{}级强化石的强化范围,需要使用{}级及以上等级强化石" },
			{ "strength.transfer.outrange", "装备'{}'的最大强化等级为'强{}级', 而待转移的等级为'强{}级', 如果强制转移则最终等级为'强{}级，是否确定转换？" },

			// TODO need extra translation

			{ "lottery.pve.noreward", "您选取的副本奖励已经过期，请在1小时内领取副本通关奖励" },
			{ "change.rolename", "恭喜,昵称修改成功！" },
			{ "change.password", "恭喜,密码修改成功！" },
			{ "change.email", "恭喜,邮箱修改成功！" },
			{ "change.email.verified", "您的邮箱已经被绑定了,无法修改" },

			{ "charge.invalid", "对不起,您的充值密钥认证失败,为了保证账户安全,请重新登陆或立即联系客服" },
			{ "charge.first.reward", "#FFFFFF首次充值充多少返还多少！返还的元宝会通过邮件发送#FFFFFF"},

			{ "challenge.freq", "您对这个玩家的挑战请求太频繁了,请间隔15秒发起一次挑战请求" },
			// {"challenge.chat", "您的挑战请求太频繁了,请间隔15秒发起一次挑战请求"},

			{ "WHITE", "白色" },
			{ "GREEN", "绿色" },
			{ "BLUE", "蓝色" },
			{ "PINK", "粉色" },
			{ "ORGANCE", "橙色" },
			{ "PURPLE", "紫色" },
			
			{"weapon.level.0", "黑铁"}, 
			{"weapon.level.10", "青铜"},
			{"weapon.level.20", "赤钢"},
			{"weapon.level.30", "白银"},
			{"weapon.level.40", "黄金"},
			{"weapon.level.50", "琥珀"},
			{"weapon.level.60", "翡翠"},
			{"weapon.level.70", "水晶"},
			{"weapon.level.80", "钻石"},
			{"weapon.level.90", "神圣"},

			{
					"craft.transfer.level",
					"#e53333您要转换装备‘#009900{}#e53333’的强化属性"
							+ "到装备‘#009900{}#e53333’,需要额外支付'#009900{}#e53333'金币手续费" },
			{
					"craft.transfer.color",
					"#e53333您要将{}装备‘#009900{}#e53333’的属性"
							+ "转换到{}装备‘#009900{}#e53333’,需要额外支付'#009900{}#e53333'金币手续费" },
			{ "craft.transfer.notlevel", "您当前的VIP等级无法转移'{}'级装备,详情请查看VIP会员" },
			{ "craft.transfer.notcolor", "您当前的VIP等级无法转移'{}'装备,详情请查看VIP会员" },

			{ "craft.compose.equip.level", "您需要四把相同等级的装备(比如黑铁、青铜等)才能熔炼" },
			{ "craft.compose.equip.notenough", "您需要放入四把装备才能熔炼新的装备或武器" },
			{ "craft.compose.color", "您需要一至四把'{}'的装备才能使用'{}'熔炼符" },
			{ "craft.compose.color.notsame", "使用颜色熔炼符时需要使用四把相同武器" },
			{ "craft.compose.color.str.warning",
					"您准备熔炼颜色的武器带有强化等级,熔炼后强化等级会消失,建议您先将强化等级转移到其他装备上再强化,还要继续强化吗？" },
			{ "craft.compose.color.purple.warning", "注意：熔炼紫色装备失败时装备会消失,您确定要继续熔炼吗？" },

			// Forget
			{ "forget.fail", "因为系统故障暂时不能发送临时密码,请稍后再试" },
			{ "forget.noname", "您需要填写昵称才能找回密码" },
			{ "forget.nouser", "该昵称未找到" },
			{ "forget.noemail", "您注册时未填写有效邮箱,无法推送临时密码" },
			{ "forget.ok", "临时密码已经发送到您的邮箱,请在30分钟内登陆游戏,否则密码失效" },
			{ "forget.subject", "《打你妹》忘记密码找回" },
			{
					"forget.content",
					"<html>尊敬的{},您好,<br><br>  您的游戏临时密码为'{}',请于30分钟内登陆游戏,在'完善信息'面板中修改密码。<br><br>《打你妹》游戏团队</html>" },

			// Compose
			{ "compose.fail", "因为系统故障暂时不能发送临时密码,请稍后再试" },

			// Email
			{ "email.invalid", "您填写的电子邮件不是有效格式哦" },
			{ "email.no", "如果您填写了有效的邮件,不仅可以在忘记密码后找回,还可以领取丰厚奖励哦亲" },
			{ "email.subject", "欢迎您进入《打你妹》的世界" },
			{
					"email.content",
					"<html>尊敬的{},您好,<br><br>  欢迎您进入《打你妹》的世界,请勐击链接:<a href=\"http://{}\">邮件确认</a>确认您的电子邮件, 可领取丰厚游戏奖励,祝您游戏愉快！<br><br>《打你妹》游戏团队</html>" },
			{
					"email.verified",
					"<html><head><meta http-equiv='refresh' content='3; URL={}'></head><body>邮件地址已经确认,请您去游戏中领取任务奖励！正在为您跳转官网...</body></html>" },
			{
					"email.notverified",
					"<html><head><meta http-equiv='refresh' content='3; URL={}'></head><body>邮件地址无法确认,请确认您填写了正确的邮件地址。 正在为您跳转官网...</body></html>" },
			{ "email.reward", "恭喜您成功绑定了邮箱,礼物已经发送到您的背包" },
			{ "email.sent", "已经向您的邮箱发送了验证邮件,验证后可领取丰厚奖励" },
			{ "email.gift.subject", "邮件验证的奖励" },

			// Weibo
			{ "weibo.levelup.0", "哈哈,我在#打你妹#里升级啦,现在{}级,升级好快喔！[{}]" },
			{ "weibo.levelup.1", "吼吼,升级到{}级了,#打你妹#里升级那是相当容易的一件事,一起来玩吧！[{}]" },
			{ "weibo.levelup.2", "升级咯,为了庆祝自己升到{}级,我决定再升一级,嗯,再升一级就好了！[{}]" },

			{ "weibo.ach.0", "我在#打你妹#里达成成就“{}”拉,太给力了！[{}]" },
			{ "weibo.ach.1", "达成成就“{}”,#打你妹#真好玩,有那么多独特的称号！[{}]" },

			{ "weibo.str.0", "我在#打你妹#中将装备{}强化到{}级了,吼吼~人品爆发啊…[{}]" },
			{ "weibo.str.1", "{}强化到{}级了,#打你妹#里我也可以挑战高富帅。[{}]" },

			{ "weibo.win.0", "春风吹,战鼓擂,我在#打你妹#里不怕谁！[{}]" },
			{ "weibo.win.1", "又轻松战胜一个对手,证明我不是新手,想要挑战我,就来#打你妹#吧！[{}]" },
			{ "weibo.win.2", "又赢了,#打你妹#里人称东方不败,赶紧来吧,求虐…[{}]" },

			{ "weibo.rank.0", "嘿~排名又上升了,#打你妹#里可以看到你每一天的成长,真的很好玩！[{}]" },
			{ "weibo.rank.1", "排名又上升了,浮云啊浮云,#打你妹#精彩等你来挑战！[{}]" },
			// （排名前10的时调用）
			{ "weibo.rank.no10", "偶也！排名进前10了,怎么说我在#打你妹#里也算个高手了！[{}]" },
			// （排名第一时调用）
			{ "weibo.rank.no1", "我在#打你妹#里排名第一拉,作为高手我已经了寂寞,哎..没有对手啊！[{}]" },

			// VIP
			{ "vip.already", "您已经是VIP用户,有效期至:{}" },
			{ "vip.buy.success", "恭喜您成功购买了VIP身份,您的VIP有效期至:{}" },
			{ "vip.buy.failure", "对不起,VIP购买失败了,请您稍后再试" },
			{ "vip.expire", "您的VIP身份已经到期了,请重新购买" },
			{ "vip.nomoney", "您的的元宝{}不足,无法购买VIP" },
			{ "vip.tobeexpired",
					"#e53333您的VIP身份将于#00d5ff{}#e53333日后(有效期至#00d5ff{}#e53333)过期,请及时续费" },
			{ "vip.subscribe", "您确定要购买{}天的VIP身份（有效期至{}）吗?" },
			{ "vip.resubscribe", "您现在已经是VIP了,有效期至{},您确定要续订VIP身份至{}吗?" },
			{ "vip.offlineexp.subject", "VIP{}离线经验奖励" },
			{ "vip.offlineexp.content", "尊贵的VIP{}级用户,在您离线的时候共产生了{}经验,快点击附件收取吧！" },
			{ "vip.gift.subject", "恭喜您获得了VIP{}大礼包！" },
			{ "vip.gift.content", "尊贵的VIP{}级用户,请点击邮件的获得按钮收取VIP{}大礼包！" },
			/*
			 * {"hurt.critical", "暴击"}, {"hurt.complete", "精确打击"}, {"hurt.light",
			 * "轻微命中"}, {"hurt.frozen", "冰冻"}, {"hurt.unfrozen", "解冻"}, {"hurt.death",
			 * "致命一击"}, {"hurt.guide", "引导命中"},
			 */

			{ "bag.openvip.box",
					"提示：这个宝箱会开出和您当前等级匹配的装备,比如10级开出黑铁,20级开出青铜,如果您希望获得更高级别装备,可以考虑升级后再打开。" },
			{ "bag.levelhigh", "您选择的物品需要等级{}级以上才能装备" },
			{ "bag.sexwrong", "您选择的是{}专用装备,与您的角色性别不符" },
			{ "bag.pick.full.sub", "您刚刚抽到的物品" },
			{ "bag.pick.full.content", "您的背包满了,物品会暂时存放在邮件中,请尽快清理背包,否则会造成物品丢失" },

			{ "notice.openEquipBox.pink",
					"#e53333玩家'#00d5ff{}#e53333'人品爆发,获得了粉色装备'#e4347e{}'" },
			{ "notice.openEquipBox.orange",
					"#e53333玩家'#00d5ff{}#e53333'人品爆发,获得了橙色装备'#fe9f27{}'" },
			{ "notice.vip.tobeexpired",
					"#e53333您的VIP身份将于'#00d5ff{}#e53333'日后过期,过期后背包将会缩减到70项物品,多余的物品会被系统自动卖出,请您尽快处理。" },
			{ "notice.treasure.strength.pink",
					"#e53333玩家'#00d5ff{}#e53333'人品爆发,寻宝获得了强{}级粉色装备'#e4347e{}#e53333', 感谢CCTV！" },
			{ "notice.treasure.strength.orange",
					"#e53333玩家'#00d5ff{}#e53333'人品爆发,寻宝获得了强{}级橙色装备'#fe9f27{}#e53333', 感谢CCTV！" },
			{ "notice.treasure.pink",
					"#e53333玩家'#00d5ff{}#e53333'人品爆发,寻宝获得了粉色装备'#e4347e{}', 感谢CCTV！" },
			{ "notice.treasure.orange",
					"#e53333玩家'#00d5ff{}#e53333'人品爆发,寻宝获得了橙色装备'#fe9f27{}#e53333', 感谢CCTV！" },
			{ "notice.treasure.strength",
					"#e53333玩家'#00d5ff{}#e53333'人品爆发,寻宝获得了强{}级装备'{}', 感谢CCTV！" },
			{ "notice.treasure.refresh",
					"您每日的{}次立即刷新机会用完了,需要等待{}秒后刷新,或者支付1元宝立即刷新。点击“取消”则使用上次刷新的结果" },

			{ "task.reward.notfinished", "任务'{}'还没有完成呢" },
			{ "task.reward.notfound", "任务ID'{}'没有找到完成" },
			{ "task.reward.taken", "任务'{}'奖励已经被领取过了" },
			{ "task.reward.outoftime", "您需要在规定的时间内领取任务奖励" },

			{ ErrorCode.TIMEOUT.desc(), "您的连接已经超时了,需要重新登陆" },

			{ "friend.add", "您和玩家'{}'已经成为好友" },
			{ "friend.offline", "您要挑战的好友已经下线了" },
			{ "box.not_a_box", "您选择的道具{}不是一个宝箱,无法使用" },
			{ "box.not_enough_key", "您要打开的宝箱'{}'需要{}个‘{}‘才能开启" },

			{ "friend.incombat",
					"#e53333您邀请的玩家#00d5ff{}#e53333已经加入对战了,需要等战斗结束后才能收到您的请求" },
			{ "friend.chall.note", "#e53333您的朋友#00d5ff{}#e53333希望和你进行一场战斗" },

			{ "battle.pickbox.subject", "恭喜您在战斗中获得了{}" },
			{ "battle.pickbox.content", "这是您在战斗中击中钻石获得的奖品,请点击获取" },

			// 战斗结算时服务器给予玩家的动态评价
			{ "battle.remark.fail.agility", "对手的敏捷是您的{}倍,所以他会先发而且连续攻击,赶紧加强敏捷吧" },
			{ "battle.remark.fail.level", "对手使用了'{}'等级的武器,而您的武器还停留在'{}'等级,赶紧升级吧" },
			{ "battle.remark.fail.strength",
					"对手的武器强化到了'{}'级,您的武器是'{}'级,请您先去铁匠铺强化您的装备再战不迟" },
			{ "battle.remark.fail.strong", "对手的武器拥有更高的战斗力,请您先去铁匠铺强化合成您的装备吧" },
			{ "battle.remark.fail.suit", "对手装备'{}'超强套装,您也来一套吧！" },
			{ "battle.remark.fail.hitratio", "对手的命中率比您高啊,先去训练场找找感觉吧" },
			{ "battle.remark.fail.vip", "对手是个VIP,打不赢也是正常的说..." },

			{ "battle.remark.succ.perfect", "您的战斗太完美了,地球人已经无法阻止了" },
			{ "battle.remark.succ.beatstrong", "哈,真是以弱胜强的经典战例!" },
			{ "battle.remark.succ.beatweak", "恃强凌弱胜之不武啊啊啊!" },
			{ "battle.remark.succ", "恭喜您获得了战斗的胜利！" },
			{ "battle.remark.fail", "大侠请重新来过..." },
			{ "battle.remark.leave", "敌人逃跑啦..." },
			{ "battle.remark.boss.succ", "恭喜您成功挑战了BOSS！" },
			{ "battle.remark.boss.fail", "挑战BOSS需要强化的武器,精良的装备,和密切的合作哟" },

			{ ErrorCode.MAINTANCE.desc(), "目前服务器正在维护中..." },

			// Boss
			{ "boss.simple", "简单" },
			{ "boss.normal", "#00FF00普通" },
			{ "boss.hard",   "#FF0000困难" },
			{ "boss.veryhard", "极难" },
			{ "boss.nothing", "世界副本开放时间为每日13:00及20:00及23:00开启,请准时进入" },
			{ "boss.condition.level", "1).{}<=等级<={}" },
			{ "boss.condition.level.desc", "您的等级需要在{}级到{}级之间方可挑战此副本" },
			{ "boss.condition.golden", "2).支付金币{}" },
			{ "boss.challenge.runout", "您今天挑战该Boss的机会已经用完了" },
			{ "boss.challenge.timeout", "副本{}的挑战时间已经截止, 挑战失败了" },
			{ "boss.challenge.new", "对副本{}的挑战尚未开始,敬请期待" },
			{ "boss.challenge.success", "副本{}已经挑战成功,请挑战其他副本" },
			{ "boss.reward.notsuccess", "副本现在不能领取奖励" },
			{ "boss.reward.timeout", "副本领取奖励的时间已经截止了" },
			{ "boss.reward.notwin", "您未能成功挑战该副本,无法领取奖励" },
			{ "boss.reward.taken", "您已经领取过该副本的奖励了" },
			{ "boss.reward.take", "恭喜您成功领取了副本的奖励！" },
			{ "boss.win.notify", "#ffffff副本{}挑战成功啦！贡献排名如下:\n" },
			{ "boss.win.notify.user", "#ffffff{}.  玩家#e53333{}#ffffff, 获得{}金币奖励\n" },
			{ "boss.win.notify.user2", "#ffffff{}.  玩家#e53333{}#ffffff\n" },
			{ "boss.win.mail.sub", "恭喜您在副本{}战斗中贡献度排名第{},获得额外奖励" },
			{ "boss.win.mail.content", "您在副本{}战斗中贡献度排名第{},将获得额外奖励{}金币" },
			{ "boss.invite.friend", "您邀请的好友暂时不能参加此副本" },
			{ "boss.single.invite.friend", "单人副本暂时不能邀请好友参与" },
			{ "boss.challenge.already.success", "副本{}已经挑战成功了" },
			{ "boss.reward.noaction", "您的体力耗尽了,暂时不能领取副本奖励,需要等体力恢复后领取" },
			{ "boss.challenge.noaction", "您的体力耗尽了,暂时不能挑战副本,请体力恢复后再试" },
			{ "boss.invite.unsyncprogress", "您邀请的好友副本已经达到第{}关,关卡不同不能组队" },
			{ "boss.invite.unsyncstatus", "您邀请的好友副本已经通关,无法组队" },
			{ "boss.invite.leveldiff", "您和您的好友等级相差超过20级无法组队" },

			// Secure
			{ "secure.limit.exp",
					"您今天获得的累计总经验达到了惊人的'{}',已经超过安全上限,新的经验值将不再添加到您的账户,建议您明天再来游戏" },
			{ "secure.limit.golden",
					"您今天获得的累计总金币数达到了惊人的'{}',已经超过安全上限,新的金币将不再添加到您的账户,建议您明天再来游戏" },
			{ "secure.limit.yuanbao",
					"您今天获得的累计总元宝数达到了惊人的'{}',已经超过安全上限,新的元宝将不再添加到您的账户,建议您明天再来游戏" },

			// 体力
			{ "action.grow", "#FFFFFF每隔1小时体力会自动恢复#FF0000{}#FFFFFF点" },
			{ "action.buy.success", "您成功购买了体力" },
			{ "action.limit.nochance", "您不能购买额外的体力值,升级VIP可获得更多购买机会！" },
			{ "action.exhausted", "您今天的体力值的已经耗尽,经验值和战斗奖励会降低,建议您等待体力值自动恢复或者购买体力后再进行游戏" },
			// 祈福
			{ "pray.caishen.success", "恭喜您祈福成功！" },
			{ "pray.caishen.nochance", "您今天祈福的机会已经用完,请明天再试,成为VIP可多次祈福" },

			{ "prop.bag.limit", "'{}'最多能将您的背包扩充到{}个位置,您的背包已经达到了这个上限,无法继续扩充了" },

			{ "room.lock.desk", "您选择的同机对战模式将在{}级开启" },
			{ "room.lock.friend", "您选择的好友对战模式将在{}级开启" },
			{ "room.lock.multi", "您选择的多人对战模式将在{}级开启" },
			{ "room.lock.pve", "您选择的副本对战模式将在{}级开启" },
			{ "room.lock.single", "您选择的单人对战模式将在完成一次训练场挑战或者升到2级后开启" },
			{ "room.ready.editseat", "您的房间已经进入准备状态不可编辑坐位" },
			{ "room.guild.match.timeout", "您的公会房间长时间匹配不到其他公会,可以尝试增减房间人数后,再次匹配" },

			// Boss
			{ "boss.nochall", "对不起,您的挑战次数已经用完了, 下个小时自动恢复{}次机会" },

			// treasure.hunt.notreasure
			{ "treasure.hunt.bag.full",
					"您的背包已经满了(共{}格),再次寻宝如果抽到装备道具可能会丢失,建议您先整理下背包再寻宝。升级VIP可获得更大背包空间" },
			{ "treasure.hunt.fail", "对不起,寻宝失败了" },
			{ "treasure.hunt.success", "恭喜您寻到了宝藏" },
			{ "treasure.hunt.card", "您今天可用的免费抽奖次数增加为{}次,赶紧寻宝去吧,明天会消失哟" },
			{ "treasure.cannot.buy", "您今天的免费寻宝机会已经用完啦,升级VIP即获得无限寻宝能力哟" },
			{ "treasure.cannot.buy.advance", "高级及专家寻宝需要VIP1以上级别方可使用" },

			// Offline challenge
			{ "offline.chall.noweapon", "您要挑战的玩家没有装备武器,暂时不能应战" },
			{ "offline.chall.subject", "您离线的时候接受了{}次挑战,获胜了{}次,失败了{}次" },
			{ "offline.chall.user.win", "'#00d5ff{}#FFFFFF'于{}挑战了您,您大获全胜,打的对方满地找牙" },
			{ "offline.chall.user.fail", "'#00d5ff{}#FFFFFF'于{}挑战了您,您不幸战败,接受了胯下之辱" },

			{ "levelup.subject", "恭喜您升级到了{}级" },
			{ "levelup.content", "系统赠送给您{}级升级大礼包" },

			// 360 gift
			{ "360.gift.subject", "欢迎尊贵的360玩家,我们为您准备了丰厚的游戏大礼包" },
			{ "360.gift.content", "感谢您的访问,我们为您准备了游戏大礼包一份,请点击领取。" },
			{ "dangle.gift.subject", "欢迎尊贵的当乐玩家,我们为您准备了丰厚的游戏大礼包" },
			{ "dangle.gift.content", "感谢您的访问,我们为您准备了游戏大礼包一份,请点击领取。" },

			//
			{ "fillprofile.nonamecard", "您的背包中没有改名卡,无法修改昵称,可以去商城购买" },

			// User Action key
			{ "ActionLimitBuy", "玩家{}正在购买体力" },
			{ "ActionLimitQuery", "玩家{}正在查看体力剩余" },
			{ "Weibo", "玩家{}正在发出微博消息" },
			// double
			{ "UseProp", "玩家{}开启了{}" },
			{ "TreasureHuntPick", "玩家{}正在进行寻宝" },
			{ "TaskReward", "玩家{}正在领取任务奖励" },
			{ "Shopping", "玩家{}正在查看商城物品" },
			{ "ProduceSellTool", "玩家{}正在出售便携道具" },
			{ "ProduceSellProp", "玩家{}正在出售装备" },
			{ "MailRead", "玩家{}正在阅读邮件" },
			// double
			{ "Levelup", "玩家{}成功升级到了{}级" },
			// double
			{ "Forge", "玩家{}正在对{}进行强化" },
			{ "FindFriend", "玩家{}正在查找好友" },
			{ "EnterRoomTraining", "玩家{}正在进行训练场训练" },
			{ "EnterRoomSingle", "玩家{}正在进行单人挑战" },
			{ "EnterRoomRank", "玩家{}正在进行擂台赛" },
			{ "EnterRoomPVE", "玩家{}正在进行副本挑战" },
			{ "EnterRoomMulti", "玩家{}正在进行多人挑战" },
			{ "EnterRoomGuild", "玩家{}正在进行公会对战" },
			{ "EnterRoomFriend", "玩家{}正在进行好友对战" },
			{ "EnterRoomDesk", "玩家{}正在进行同机对战" },
			{ "EnterRoomChallenge", "玩家{}正在进行好友挑战" },
			{ "CraftComposeWeaponPro", "玩家{}正在熔炼精良武器" },
			{ "CraftComposeWeapon", "玩家{}正在熔炼普通武器" },
			{ "CraftComposeStoneWind", "玩家{}正在熔炼风神石" },
			{ "CraftComposeStoneWater", "玩家{}正在熔炼水神石" },
			{ "CraftComposeStoneStrength", "玩家{}正在熔炼强化石" },
			{ "CraftComposeStoneFire", "玩家{}正在熔炼火神石" },
			{ "CraftComposeStoneEarth", "玩家{}正在熔炼土神石" },
			{ "CraftComposeEquipPro", "玩家{}正在熔炼精良装备" },
			{ "CraftComposeEquip", "玩家{}正在熔炼普通装备" },
			{ "CraftComposeColorPink", "玩家{}正在熔炼粉色装备" },
			{ "CraftComposeColorOrange", "玩家{}正在熔炼橙色装备" },
			{ "CraftComposeColorGreen", "玩家{}正在熔炼绿色装备" },
			{ "CraftComposeColorBlue", "玩家{}正在熔炼蓝色装备" },
			// three
			{ "Charge", "玩家{}充值了{}元宝成为了VIP{}" },
			{ "CaishenPrayBuy", "玩家{}正在进行祈福" },
			{ "BuyTool", "玩家{}购买了战斗的便携道具" },
			// double
			{ "BuyProp", "玩家{}从商城中购买了{}" },
			{ "BossBeaten", "玩家{}打败了BOSS" },
			{ "BattleWin", "玩家{}获得了战斗的胜利" },
			{ "BattleReward", "玩家{}正在进行战斗抽奖" },
			{ "BattlePickBox", "玩家{}获取了战斗宝箱" },
			{ "BattleFail", "玩家{}战斗失败" },
			{ "BagTidy", "玩家{}正在整理背包" },
			{ "ArrangeList", "玩家{}正在查看排行榜" },
			// double
			{ "AddFriend", "玩家{}与{}成为了好友" },

			// 使用改名卡
			{ "card.changename", "您点击游戏右上侧的齿轮图标,进入'完善信息',然后修改昵称,改名卡会自动扣除" },
			{ "cdkey.notfoundid", "没有找到指定的CDKEY" },
			{ "cdkey.nochannel", "您输入的CDKEY是属于其他游戏版本的" },
			{ "cdkey.begintime", "您输入的CDKEY还未生效" },
			{ "cdkey.endtime", "您输入的CDKEY已经过期了" },
			{ "cdkey.used", "您输入的CDKEY已经使用过了" },
			{ "cdkey.invalid", "您输入的CDKEY无效" },
			{ "cdkey.empty", "您需要输入有效的CDKEY" },

			{ "pickreward.exp", "您获得了经验{}" },
			{ "pickreward.golden", "您获得了金币{}" },
			{ "pickreward.yuanbao", "您获得了元宝{}" },
			{ "pickreward.stone", "您获得了{}" },
			{ "pickreward.item", "您获得了道具{}" },
			{ "pickreward.weapon", "您获得了装备{}" },

			{ "deleterole.success", "成功的删除了角色'{}'" },
			{ "deleterole.cancel", "您取消了删除动作" },
			{ "deleterole.failure", "删除角色失败" },

			{ "charge.first.subject", "首次充值充多少返多少" },
			{ "charge.first.content",
					"欢迎您参加首次充值充多少返多少活动,您共充值元宝{},活动再赠送您{}元宝,感谢您对游戏的支持" },
			{ "charge.card.fail", "您输入的卡号或密码有误,无法充值,请核对后重新输入" },

			{ "remark.firstbattle", "太棒了,您在{}秒时间内结束了战斗,精准度和命中率等多项指标击败了全球{}的玩家,再接再厉！" },
			{ "exit.1", "*. 我看您天资非凡,必有过人之处,如果您明日再次登陆游戏,将获得稀有物品作为奖励,这可是商城都没得卖的哟！\n\n" },
			{ "exit.hour", "您{}小时后再次登陆即可领取稀有的神秘奖励了" },
			{ "exit.minute", "您{}分钟后再次登陆即可领取稀有的神秘奖励了" },
			{ "exit.success.sub", "恭喜您如约登陆游戏" },
			{ "exit.success.cont", "邮件的附件是系统给予您的奖励,请点击'获取',感谢您的支持" },

			{ "activity.exprate", "双倍经验时间已经开启,将于{}时间结束" },

			// 表示用户活跃度
			{ "active.roleaction", "*. 您今日的体力还剩余{}点\n" },
			{ "active.roleaction.done", "*. 恭喜,您今日的体力已经全部用完（每小时会自动恢复）\n" },
			{ "active.treasure", "*. 您今日的免费寻宝机会还剩余{}次\n" },
			{ "active.treasure.done", "*. 恭喜,您今日的免费寻宝机会已经全部用完\n" },
			{ "active.pray", "*. 您今日的祈福还剩余{}次机会\n" },
			{ "active.pray.done", "*. 恭喜,您今日的祈福已经完成\n" },
			{ "active.general", "您确定要退出游戏吗?" },

			{ "roleaction.add", "恭喜您体力恢复了{}点" },
			{ "roleaction.max", "您的体力已经达到最大上限,无法累加体力" },

			{ "chargecard.no.type", "您选择的卡类型{}尚不支持" },
			{ "chargecard.inprogress", "提交的信息正在处理中..." },
			{ "chargecard.error", "您输入的信息有误,请核对后重新输入" },

			{
					"chat.charge.reply",
					"充值未到账问题请在聊天中发送: @充值@<RMB金额>@<充值时间>,例如: @充值@50元@18:30,这条信息会自动发送给GM。或者您在QQ中添加418313139客服号做好友,标明充值问题,谢谢" },
			{ "chat.charge.recevied", "您反馈的充值问题已经受理,请等待处理,如果没有答复,也可以加QQ:418313139询问" },

			{ "battle.secondkill", "苍天啊大地啊,玩家#00d5ff{}#ffffff华丽丽滴秒杀了玩家#00d5ff{}" },
			{ "battle.diaosi",
					"屌丝的逆袭,免费玩家#00d5ff{}#ffffff击败了#00d5ffVIP{}#ffffff级玩家#00d5ff{}" },
			{ "battle.load.ready", "#00d5ff{}#ffffff: 加载完毕" },
			{ "battle.load.loading", "#00d5ff{}#e53333: 正在载入" },

			{ "friend.mutaladd", "#FFFFFF玩家#00d5ff{}#FFFFFF已经添加您为好友了,您是否也愿意添加他做好友呢？" },
			{ "bag.check.vipcount", "您的背包尺寸已经调整为{}格" },

			{ "newserver.open", "您选择的新服预计在{}正式开放注册,敬请期待" },
			{ "server.register.disable", "该服已经关闭注册，请访问其他服" },
			{ "server.versionurl", "您需要更新版本才能访问本服，点击'确定'按钮将自动下载更新，点击'取消'则退出游戏。游戏包大约50M左右，请确定您在WIFI环境下更新。" },
			{ "server.ip.wrong", "您选择的服不是您角色'{}'所在的服,请选择正确的服" },

			{ "apple.invalid.iap", "您的充值无效,使用破解的IAP充值将被我们封号,如有疑问请加QQ:418313139咨询" },

			{ "doubleexp.confirm", "双倍经验卡将在{}场战斗中使您的经验翻倍,但是需要在{}小时内用完,过期失效,是否要现在激活？" },
			{ "doubleexp.enable", "您已经激活了双倍经验卡" },

			{ "struggle.usedup", "限量礼品已经发放完毕" },

			{ "online.promotion.subject", "在线工资礼包奖励" },
			{ "online.promotion.content", "恭喜您！{}当日累计在线时长为{}分钟,可领取今日的工资礼包" },

			{ "biblio.power", "战斗力是对角色综合战斗能力的评定" },
			{ "biblio.attack", "攻击可以提升战斗中对敌人的伤害程度" },
			{ "biblio.defend", "防御可以抵消战斗中敌人对角色的伤害" },
			{ "biblio.agility", "敏捷值决定了战斗的出手顺序,并影响战斗中的体力值" },
			{ "biblio.luck", "幸运可以提升暴击的概率和暴击的伤害程度" },
			{ "biblio.reward.desc", "收集齐所有图鉴可赢得稀有珍宝！完成度:{}%" },
			{ "biblio.reward.taken", "您已经领取了图鉴的奖励" },
			{ "biblio.reward.unfinished", "您还没有收集齐所有的图鉴的内容" },
			{ "biblio.reward.success", "恭喜您成功的领取了图鉴的奖励" },

			{ "gender.MALE", "男性" },
			{ "gender.FEMALE", "女性" },
			{ "gender.NONE", "未知" },
			{ "gender.ALL", "通用" },

			{ "quality.NONE", "简陋" },
			{ "quality.SIMPLE", "普通" },
			{ "quality.NORMAL", "精良" },
			{ "quality.PRO", "稀有" },
			{ "quality.LEGEND", "传说" },

			// Guild
			{ "guild.create.success", "恭喜您成功创建了公会'{}'" },
			{ "guild.create.namedup", "您选择的公会名'{}'已经存在了,请更换名称" },
			{ "guild.create.noname", "待创建的公会名不能为空" },
			{ "guild.create.namesize", "待创建的公会名应该在1到5个字符之间" },
			{ "guild.create.nolevel", "您的等级未达到{}级,无法创建公会" },
			{ "guild.create.nogolden", "您的金币少于{},无法创建公会" },
			{ "guild.create.alreadyin", "您已经加入了公会,无法创建新的公会了" },
			{ "guild.owner.apply.success", "您已经成功处理了玩家的申请" },
			{ "guild.join.success", "恭喜您成功加入公会" },
			{ "guild.apply.noid", "没有找到您申请的公会" },
			{ "guild.apply.sent", "您曾经提交过申请,请耐心等待处理" },
			{ "guild.apply.success", "您的申请已经发给该公会" },
			{ "guild.apply.full", "该公会人数已满,请选择其他公会" },
			{ "guild.apply.maxapply", "您已经向{}个公会提交了申请,不能再提交新的申请了" },
			{ "guild.apply.noguild", "您还没有加入公会,无法处理入会申请" },
			{ "guild.apply.nopriv", "您当前的职位没有权利处理入会申请" },
			{ "guild.apply.approve.full", "公会'{}'成员已达到该等级的最大数量,您暂时无法加入该公会。" },
			{ "guild.apply.approve", "恭喜,您申请的公会'{}'正式批准您的加入" },
			{ "guild.apply.deny", "公会'{}'拒绝了您的加入申请" },
			{ "guild.apply.exist", "您已经加入了公会'{}',不能再申请其他公会了" },
			{ "guild.apply.submited", "您已经向公会'{}'提交过申请了,请等候回复" },
			{ "guild.opfee.payed.subject", "公会维护费扣除通知" },
			{ "guild.opfee.payed.content",
					"您的公会维护费(从{}至{})共计{},已经从公会财富账户中扣除,当前公会财富余额为{}" },
			{ "guild.opfee.unpayed.subject", "公会维护费不足解散通知" },
			{ "guild.opfee.unpayed.content",
					"您的公会维护费(从{}至{})共计{},而公会财富余额为{},无法完成续费,公会已经解散。" },
			{ "guild.opfee.payed.subject", "公会维护费扣除通知" },
			{ "guild.opfee.notify.subject", "您的公会维护费不足" },
			{ "guild.opfee.notify.content",
					"您的公会需在{}支付{}维持费,当前公会财富余额为{},已经不足以支付,请尽快增加公会财富值,否则在{}日公会将会解散" },
			{ "guild.opfee.notify.dismiss", "您的公会'{}'因维护费不足已被解散" },
			{ "guild.shop.zero", "您需要将公会商城升级到1级方可使用" },
			{ "guild.shop.overlevel", "您不能查看超过商城等级的商品列表,请尽快升级公会商城" },
			{ "guild.facility.unmeetcredit", "您的贡献度为{},不能使用{}级公会设施" },
			{ "guild.bag.progress", "玩家'{}'正在保存物品到公会仓库,请您稍后再试" },
			{ "guild.bag.obsolete", "其他玩家修改了公会仓库的物品,您需要先刷新后再次操作" },
			{ "guild.bag.put.success", "您的物品'{}'已经放入公会仓库" },
			{ "guild.bag.put.full", "公会仓库已满,无法放入新的物品了" },
			{ "guild.bag.put.null", "您放入的物品无效" },
			{ "guild.bag.put.nopriv", "精英以上的职位才可以使用公会仓库" },
			{ "guild.bag.take.nopriv", "精英以上的职位才可以使用公会仓库" },
			{ "guild.bag.take.success", "您成功的提取了公会仓库的物品{}" },
			{ "guild.bag.take.fail", "无法提取该公会仓库的物品" },
			{ "guild.bag.take.taken", "您要提取的物品已经被其他玩家取走了,请查看'存取日志'" },
			{ "guild.fire.nopriv", "您在公会的职位无法开除其他会员" },
			{ "guild.fire.fail", "未能成功开除该会员" },
			{ "guild.fire.success", "您已成功开除该会员" },
			{ "guild.fire.self", "您不能开除自己,如果要退出公会,请点击'退出'按钮" },
			{ "guild.fire.mail", "您已经被公会管理员'{}'开除出公会" },
			{ "guild.fire.owner", "会长不能被开除出公会" },
			{ "guild.exit.owner", "会长不可以退出公会" },
			{ "guild.exit.notinguild", "您已经退出了公会" },
			{ "guild.exit.success", "您成功退出了公会{}" },
			{ "guild.enter.success", "您还没有加入任何公会呢" },
			{ "guild.facility.caifu", "消耗公会财富{}" },
			{ "guild.facility.credit", "消耗个人贡献{}" },
			{ "guild.facility.guild.level", "公会等级{}" },
			{ "guild.facility.guild.people", "公会人数上限{}" },
			{ "guild.facility.craft.level", "公会铁匠铺等级{}" },
			{ "guild.facility.craft.people", "公会铁匠铺提升强化等级{}%" },
			{ "guild.facility.shop.level", "公会商城等级{}" },
			{ "guild.facility.shop.people", "公会商城可用Lv{}" },
			{ "guild.facility.bag.level", "公会仓库等级{}" },
			{ "guild.facility.bag.people", "公会仓库数量上限{}" },
			{ "guild.facility.shop.lv1", "可以使用1级公会商城" },
			{ "guild.facility.shop.lv2", "可以使用2级公会商城" },
			{ "guild.facility.shop.lv3", "可以使用3级公会商城" },
			{ "guild.facility.shop.lv4", "可以使用4级公会商城" },
			{ "guild.facility.shop.lv5", "可以使用5级公会商城" },
			{ "guild.facility.craft.lv1", "强化可提升2%成功率" },
			{ "guild.facility.craft.lv2", "强化可提升4%成功率" },
			{ "guild.facility.craft.lv3", "强化可提升6%成功率" },
			{ "guild.facility.craft.lv4", "强化可提升8%成功率" },
			{ "guild.facility.craft.lv5", "强化可提升10%成功率" },
			{ "guild.facility.storage.lv1", "仓库空间扩展到40格" },// 40, 100, 160, 220, 280
			{ "guild.facility.storage.lv2", "仓库空间扩展到80格" },
			{ "guild.facility.storage.lv3", "仓库空间扩展到140格" },
			{ "guild.facility.storage.lv4", "仓库空间扩展到220格" },
			{ "guild.facility.storage.lv5", "仓库空间扩展到320格" },
			{ "guild.facility.ability.lv1", "可学习技能：防御" },
			{ "guild.facility.ability.lv2", "可学习技能：防御、幸运" },
			{ "guild.facility.ability.lv3", "可学习技能：防御、幸运、攻击" },
			{ "guild.facility.ability.lv4", "可学习技能：防御、幸运、攻击、敏捷" },
			{ "guild.facility.ability.lv5", "可学习技能：防御、幸运、攻击、敏捷、生命、寻宝、祈福" },
			{ "guild.facility.ab_attack.lv1", "提高自身攻击1%" },
			{ "guild.facility.ab_attack.lv2", "提高自身攻击2%" },
			{ "guild.facility.ab_attack.lv3", "提高自身攻击3%" },
			{ "guild.facility.ab_attack.lv4", "提高自身攻击4%" },
			{ "guild.facility.ab_attack.lv5", "提高自身攻击5%" },
			{ "guild.facility.ab_agility.lv1", "提高自身敏捷1%" },
			{ "guild.facility.ab_agility.lv2", "提高自身敏捷2%" },
			{ "guild.facility.ab_agility.lv3", "提高自身敏捷3%" },
			{ "guild.facility.ab_agility.lv4", "提高自身敏捷4%" },
			{ "guild.facility.ab_agility.lv5", "提高自身敏捷5%" },
			{ "guild.facility.ab_lucky.lv1", "提高自身幸运1%" },
			{ "guild.facility.ab_lucky.lv2", "提高自身幸运2%" },
			{ "guild.facility.ab_lucky.lv3", "提高自身幸运3%" },
			{ "guild.facility.ab_lucky.lv4", "提高自身幸运4%" },
			{ "guild.facility.ab_lucky.lv5", "提高自身幸运5%" },
			{ "guild.facility.ab_defend.lv1", "提高自身防御1%" },
			{ "guild.facility.ab_defend.lv2", "提高自身防御2%" },
			{ "guild.facility.ab_defend.lv3", "提高自身防御3%" },
			{ "guild.facility.ab_defend.lv4", "提高自身防御4%" },
			{ "guild.facility.ab_defend.lv5", "提高自身防御5%" },
			{ "guild.facility.ab_blood.lv1", "提高自身血量1%" },
			{ "guild.facility.ab_blood.lv2", "提高自身血量2%" },
			{ "guild.facility.ab_blood.lv3", "提高自身血量3%" },
			{ "guild.facility.ab_blood.lv4", "提高自身血量4%" },
			{ "guild.facility.ab_blood.lv5", "提高自身血量5%" },
			{ "guild.facility.ab_treasure.lv1", "提高寻宝成功率1%" },
			{ "guild.facility.ab_treasure.lv2", "提高寻宝成功率2%" },
			{ "guild.facility.ab_treasure.lv3", "提高寻宝成功率3%" },
			{ "guild.facility.ab_treasure.lv4", "提高寻宝成功率4%" },
			{ "guild.facility.ab_treasure.lv5", "提高寻宝成功率5%" },
			{ "guild.facility.ab_pray.lv1", "提高祈福获得金币数量至60K" },
			{ "guild.facility.ab_pray.lv2", "提高祈福获得金币数量至70K" },
			{ "guild.facility.ab_pray.lv3", "提高祈福获得金币数量至80K" },
			{ "guild.facility.ab_pray.lv4", "提高祈福获得金币数量至90K" },
			{ "guild.facility.ab_pray.lv5", "提高祈福获得金币数量至100K" },
			{ "guild.levelup.ab.nocredit", "您的公会贡献度不足,通过捐献可以提升贡献度" },
			{ "guild.levelup.ab.prompt", "您升级个人技能'{}'需要消耗贡献度{},是否确定？" },
			{ "guild.levelup.nopriv", "您的职位不能升级公会设施" },
			{ "guild.levelup.guild.noyuanbao", "您的元宝不足,无法立即冷却并升级" },
			{ "guild.levelup.guild.ability.higher", "您要升级的子技能不能超过公会技能的等级" },
			{ "guild.levelup.guild.higher", "该公会设施等级不能超过公会等级{}" },
			{ "guild.levelup.guild.max", "该公会设施已经升级到最高等级" },
			{ "guild.levelup.guild.success", "恭喜您公会设施已经成功升级" },
			{ "guild.levelup.guild.failure", "公会设施升级失败,请稍后再试" },
			{ "guild.levelup.guild.cancel", "公会设施升级取消" },
			{ "guild.levelup.guild.nowealth", "公会财富不足,无法升级设施" },
			{ "guild.levelup.guild.prompt", "您升级公会设施'{}'需要消耗公会财富{},是否确定？" },
			{ "guild.levelup.guild.prompt.cool",
					"您需要等冷却时间结束后再升级,但是花费元宝{}可以立即冷却,是否同意？" },
			{ "guild.announce.noguild", "您当前未加入公会,无法修改宣言" },
			{ "guild.announce.nopriv", "您的职位不能修改公会宣言" },
			{ "guild.announce.success", "恭喜您修改成功！" },
			{ "guild.changerole.success", "恭喜您成功修改了职位" },
			{ "guild.changerole.nopriv", "您没有权限修改成员的职位" },
			{ "guild.changerole.owner", "会长的职位不能修改" },
			{ "guild.changerole.max", "{}级公会的{}职位最多只能指派{}名会员" },
			{ "guild.recruit.full", "您的公会人数已经达到这个等级的最大值,无法邀请新的玩家了" },
			{ "guild.recruit.success", "已经成功的发出了邀请" },
			{ "guild.recruit.agree", "已经成功的发出了邀请" },
			{ "guild.recruit.nopriv", "您没有邀请新成员的权限" },
			{ "guild.recruit.cancel", "玩家'{}'拒绝了您的公会邀请" },
			{ "guild.recruit.backmsg", "您邀请的玩家'{}'同意加入您的公会" },
			{ "guild.recruit.already", "您邀请的玩家'{}'已经加入其他公会了" },
			{ "guild.recruit.prompt", "玩家'{}'邀请您加入他的公会'{}',是否同意？" },
			{ "guild.recruit.self",   "您不能邀请自己加入公会"},
			{ "guild.transfer.notowner", "您不是会长,无法转让公会" },
			{ "guild.transfer.notmember", "玩家'{}'还没有加入公会" },
			{ "guild.transfer.notdays", "会长需要任职满7天才可转让公会" },
			{ "guild.transfer.success", "恭喜您成功转让了公会" },
			{ "guild.transfer.offline", "要转让的玩家未登陆游戏,必须双方在线方可转让" },
			{ "guild.transfer.prompt", "公会会长'{}'希望将公会'{}'转让给你,是否同意？" },
			{ "guild.transfer.cancel", "玩家'{}'拒绝了您转让公会的要求" },
			{ "guild.transfer.backmsg", "公会已经成功转让给玩家'{}'" },
			{ "guild.transfer.fail", "公会转让失败" },
			{ "guild.recruit.pending", "转让请求已经发出,等待对方确认" },
			{ "guild.offline.confirm",  "您要邀请的公会成员不在线"},
			{ "guild.no.challenge",   "您不能把自己公会的玩家邀请到对手房间去"},
			{ "guild.combat.mincount",  "公会战至少需要2个人组队对战，单人无法发起公会战"},
			
			// 公会的设施
			{ "guild.shop", "公会商城" },
			{ "guild.craft", "公会铁匠铺" },
			{ "guild.storage", "公会仓库" },
			{ "guild.guild", "公会" },
			{ "guild.ability", "公会技能" },
			{ "guild.ab_attack", "攻击技能" },
			{ "guild.ab_agility", "敏捷技能" },
			{ "guild.ab_lucky", "幸运技能" },
			{ "guild.ab_defend", "防御技能" },
			{ "guild.ab_blood", "血量技能" },
			{ "guild.ab_treasure", "寻宝技能" },
			{ "guild.ab_pray", "祈福技能" },

			// 公会的职位名称
			{ "chief", "会长" },
			{ "director", "副会长" },
			{ "manager", "官员" },
			{ "elite", "精英" },
			{ "member", "会员" },
			// 权限列表
			{ "announce", "修改公告" },
			{ "guildrole", "职位调整" },
			{ "recruit", "招收成员" },
			{ "firememeber", "开除成员" },
			{ "levelup", "建筑升级" },
			{ "combat", "公会战斗" },
			{ "takebag", "使用仓库" },
			// 公会背包行为类型
			{ "PUT", "放入" },
			{ "TAKE", "取出" },

			// 装备类型输出
			{ "slot.weapon", "武器" },
			{ "slot.expression", "表情" },
			{ "slot.face", "脸饰" },
			{ "slot.decoration", "装饰" },
			{ "slot.hair", "头发" },
			{ "slot.wing", "翅膀" },
			{ "slot.clothes", "衣服" },
			{ "slot.hat", "帽子" },
			{ "slot.glasses", "眼睛" },
			{ "slot.jewelry", "珠宝" },
			{ "slot.ring", "戒指" },
			{ "slot.weddingring", "婚戒" },
			{ "slot.necklace", "项链" },
			{ "slot.bracelet", "手镯" },
			{ "slot.bubble", "气泡" },
			{ "slot.suit", "套装" },
			{ "slot.offhandweapon", "副手武器" },
			{ "slot.other", "其他" },
			{ "slot.item", "物品" },
			{ "slot.gift_pack", "礼包" },

			// 武器装备新的属性输出
			{ "propdata.quality", "品质：{}" }, { "propdata.level", "等级：{}" },
			{ "propdata.maxlevel","最高强化:{}级" },
			{ "propdata.noslot", "(装备无熔炼插槽)" },
			{ "propdata.slot","(熔炼插槽数：{})" },
			{ "propdata.type",    "类型：{}" }, { "propdata.duration", "耐久：{}" },
			{ "propdata.count",   "数量：{}" }, { "propdata.binded", "已绑定" },
			{ "propdata.unbind",  "未绑定" }, { "propdata.radius", "攻击范围：{}" },
			{ "propdata.sradius", "大招攻击范围：{}" }, { "propdata.gender", "性别：{}" },
			{ "propdata.damage",  "伤害：{}" }, { "propdata.skin", "护甲：{}" },
			{ "propdata.attack",  "攻击：{}" }, { "propdata.defend", "防御：{}" },
			{ "propdata.agility", "敏捷：{}" }, { "propdata.lucky", "幸运：{}" },
			{ "propdata.str.damage", "强化加成伤害：{}" },
			{ "propdata.str.attack", "强化加成攻击：{}" },
			{ "propdata.str.damage", "强化加成护甲：{}" },
			{ "propdata.str.attack", "强化加成防御：{}" },
			{ "propdata.guild.attack", "公会加成攻击：{}" },
			{ "propdata.guild.defend", "公会加成防御：{}" },
			{ "propdata.guild.agility", "公会加成敏捷：{}" },
			{ "propdata.guild.lucky", "公会加成幸运：{}" }, 
			{ "propdata.noslot", "无插槽" },
			{ "propdata.slot", "插槽{}" }, 
			{ "propdata.power", "战斗力：{}" }, 
			{ "propdata.skill", "熟练度：{}" }, 
			{ "propdata.golden.payed", "累计花费:{}"},
			{ "propdata.golden.price", "金币价格:{}" },
			{ "propdata.rare", "稀有系数:{}" }, 
			{ "propdata.slot.pending", "插槽内容待定"},
			{ "propdata.diamond", "升级'{}'需水晶: {}颗" },
			{ "propdata.diamond.embed", "已经嵌入的水晶: {}颗" },
			
			{ "slot.fire",  "#ff0000火"},
			{ "slot.water", "#0099cc水"},
			{ "slot.earth", "#ffff00土"},
			{ "slot.wind",  "#00ff00风"},
			{ "slot.attack",  "攻"},
			{ "slot.lucky",   "幸"},
			{ "slot.defend",  "防"},
			{ "slot.agility", "敏"},
			{ "slot.title",  "{}#ffffff插槽:{}"},
			{ "slot.empty",  " 空白"}
		};

	@Override
	protected Object[][] getContents() {
		return messages;
	}

	public static void main(String args[]) throws Exception {
		for ( String[] content : GameResource.messages ) {
			System.out.println(content[0]+"\t"+content[1]);
		}
	}
}
