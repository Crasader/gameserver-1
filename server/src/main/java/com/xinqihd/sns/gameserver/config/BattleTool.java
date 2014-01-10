package com.xinqihd.sns.gameserver.config;

/**
 * The combating tools:
 * 名称    消耗体力     金币
 * 引导			120       600
 * 传送			150       200
 * 生命恢复		150       600
 * 激怒			120				600
 * 隐身			50				150
 * 团队隐身		150				200
 * 团队恢复		170				500
 * 冻结冰弹		150				500
 * 改变风向		50				150
 * @author wangqi
 *
 */
public class BattleTool {

	//The canonical name of this type
	private String name;
	
	//The human-readable description of it. It is i18n message.
	private String desc;
	
	//The cost of thew
	private int thewCost;
	
	//The price in golden.
	private int golden;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * @return the thewCost
	 */
	public int getThewCost() {
		return thewCost;
	}

	/**
	 * @param thewCost the thewCost to set
	 */
	public void setThewCost(int thewCost) {
		this.thewCost = thewCost;
	}

	/**
	 * @return the golden
	 */
	public int getGolden() {
		return golden;
	}

	/**
	 * @param golden the golden to set
	 */
	public void setGolden(int golden) {
		this.golden = golden;
	}
	
}
