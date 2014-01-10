package com.xinqihd.sns.gameserver.config;

import java.util.HashMap;

import com.xinqihd.sns.gameserver.config.equip.EquipType;

/**
 * The shop will categorize all good, including weapons and items.
 * So there should be a mapping relation between the shop category and property's type.
 * It is used in BceShoppingHandler
 * 
 * 商品类别(推荐0, 热买1, 武器2, 服饰3, 道具4, 形象5, 礼包6)
 * 
 * @author wangqi
 *
 */
public enum ShopCatalog {

	RECOMMEND(0),
	HOT(1),
	// default itemType
	WEAPON(2),
	// itemType: 3; 服饰： 衣服 帽子，眼镜 饰品 套装
	SUITE(3),
  //itemType:  4;
	ITEM(4),
	// itemType: 5; 形象： 头发 脸饰 五官 翅膀
	DECORATION(5),
	GIFTPACK(6),
	NEW(7),
	//公会商城
	GUILD_LV1(10),
	GUILD_LV2(11),
	GUILD_LV3(12),
	GUILD_LV4(13),
	GUILD_LV5(14);
	
	
	private int catalogId;
	private static final HashMap<Integer, ShopCatalog> ID_MAP = 
			new HashMap<Integer, ShopCatalog>();
	static {
		for ( ShopCatalog catalog : ShopCatalog.values() ) {
			ID_MAP.put(catalog.catalogId, catalog);
		}
	}
	
	/**
	 * Constructor for ShopType
	 * @param catalogId
	 * @param equipTypes
	 */
	ShopCatalog(int catalogId) {
		this.catalogId = catalogId;
	}

	/**
	 * @return the itemType
	 */
	public int getCatalogId() {
		return catalogId;
	}

	/**
	 * Get the ShopCatalog from the catalogId
	 * @param catalogId
	 * @return
	 */
	public static ShopCatalog fromCatalogId(int catalogId) {
		return ID_MAP.get(catalogId);
	}
	/**
	 * Get the ShopCatalog by given EquipType
	 * @param type
	 * @return
	 */
	public static ShopCatalog getShopCatalogByEquipType(EquipType type) {
		switch ( type ) {
			case WEAPON:
				return ShopCatalog.WEAPON;
			case CLOTHES:
			case HAT:
			case GLASSES:
			case DECORATION:
			case SUIT:
				return ShopCatalog.SUITE;
			case HAIR:
			case FACE:
			case EXPRESSION:
			case WING:
				return ShopCatalog.DECORATION;
			case ITEM:
				return ShopCatalog.ITEM;
			case GIFT_PACK:
				return ShopCatalog.GIFTPACK;
			default:
				return ShopCatalog.WEAPON;	
		}
	}
}
