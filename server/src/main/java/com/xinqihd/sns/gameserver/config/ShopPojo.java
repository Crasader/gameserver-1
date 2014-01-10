package com.xinqihd.sns.gameserver.config;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.proto.XinqiBseShop;
import com.xinqihd.sns.gameserver.proto.XinqiGoodsInfo;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class ShopPojo implements Pojo, Comparable<ShopPojo> {

	//The item id
	private String _id;
	
	/**
	 * The item id
	 * 1: all weapons
	 * 4: 水神石
	 * 5: 土神石
	 * 6: 风神石
	 * 7: 火神石
	 * 8: 强化石
	 * 12: 神恩符
	 * ...
	 */
	private int type;
	
	//The item's name
	private String info;
	
	//The item's prop id
	private String propInfoId;
	
	//The minimum required level
	private int level = -1;
	
	/**
	 * The goldtype:
	 * 0: 
	 * 1: 礼券
	 * 2: 勋章
	 * 3:
	 * 4: 元宝
	 */
	private MoneyType moneyType = MoneyType.GOLDEN;
	
	/**
	 * The price and valid times for the item
	 */
	private List<BuyPrice> buyPrices; 
	
	/**
	 * 1： The item is banded to users.
	 * 0:  The item can transfer between users.
	 */
	private int banded = 1;
	
	/**
	 * 0 - 100. The discount percent
	 */
	private int discount = 100;
	
	/**
	 * If the item is showed on shop
	 * 1: sell
	 * 0: not sell
	 */
	private int sell = 0;
	
	/**
	 * 是否是 新品或者热门商品 0为都不是 1 为新 2为热门 
	 * 0: normal item
	 * 1: new sell item
	 * 2: hot sell item
	 */
//	private int hot;
	
	//TODO unknown field
	private int limitCount;
	
	//TODO unknown field
	private int limitGroup;
	
	//TODO un
	private int shopId;
	
	private boolean isItem = false;
	
	//The catalog
	private Set<ShopCatalog> catalogs = EnumSet.noneOf(ShopCatalog.class);
	
	/**
	 * @return the id
	 */
	public String getId() {
		return _id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this._id = id;
	}


	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}


	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}


	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}


	/**
	 * @return the propInfoId
	 */
	public String getPropInfoId() {
		return propInfoId;
	}


	/**
	 * @param propInfoId the propInfoId to set
	 */
	public void setPropInfoId(String propInfoId) {
		this.propInfoId = propInfoId;
	}


	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}


	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}


	/**
	 * @return the goldType
	 */
	public MoneyType getMoneyType() {
		return moneyType;
	}


	/**
	 * @param goldType the goldType to set
	 */
	public void setMoneyType(MoneyType goldType) {
		this.moneyType = goldType;
	}


	/**
	 * @return the buyPrices
	 */
	public List<BuyPrice> getBuyPrices() {
		return buyPrices;
	}


	/**
	 * @param buyPrices the buyPrices to set
	 */
	public void setBuyPrices(List<BuyPrice> buyPrices) {
		this.buyPrices = buyPrices;
	}


	/**
	 * @return the banded
	 */
	public int getBanded() {
		return banded;
	}


	/**
	 * @param banded the banded to set
	 */
	public void setBanded(int banded) {
		this.banded = banded;
	}


	/**
	 * @return the discount
	 */
	public int getDiscount() {
		return discount;
	}


	/**
	 * @param discount the discount to set
	 */
	public void setDiscount(int discount) {
		this.discount = discount;
	}


	/**
	 * @return the sell
	 */
	public int getSell() {
		return sell;
	}


	/**
	 * @param sell the sell to set
	 */
	public void setSell(int sell) {
		this.sell = sell;
	}

	/**
	 * @return the limitCount
	 */
	public int getLimitCount() {
		return limitCount;
	}


	/**
	 * @param limitCount the limitCount to set
	 */
	public void setLimitCount(int limitCount) {
		this.limitCount = limitCount;
	}


	/**
	 * @return the limitGroup
	 */
	public int getLimitGroup() {
		return limitGroup;
	}


	/**
	 * @param limitGroup the limitGroup to set
	 */
	public void setLimitGroup(int limitGroup) {
		this.limitGroup = limitGroup;
	}


	/**
	 * @return the shopId
	 */
	public int getShopId() {
		return shopId;
	}


	/**
	 * @param shopId the shopId to set
	 */
	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
	
	/**
	 * Add the ShopPojo to a new catalog
	 * @param catalog
	 */
	public void addCatalog(ShopCatalog catalog) {
		this.catalogs.add(catalog);
	}
	
	/**
	 * Remove the given catalog.
	 * @param catalog
	 */
	public void removeCatalog(ShopCatalog catalog) {
		if ( catalog != null ) {
			this.catalogs.remove(catalog);
		}
	}
	
	/**
	 * @param catalogs the catalogs to set
	 */
	public void setCatalogs(Collection<ShopCatalog> catalogs) {
		this.catalogs.addAll(catalogs);
	}


	/**
	 * Return all the catalogs of this shop data.
	 * @return
	 */
	public Set<ShopCatalog> getCatalogs() {
		return this.catalogs;
	}

	/**
	 * @return the isItem
	 */
	public boolean isItem() {
		return isItem;
	}


	/**
	 * @param isItem the isItem to set
	 */
	public void setItem(boolean isItem) {
		this.isItem = isItem;
	}

	/**
	 * Convert this method to ProtoBuf's ShopData
	 * @return
	 */
	public XinqiBseShop.ShopData toShopData() {
		return toShopData(discount);
	}
	
	/**
	 * Convert this method to ProtoBuf's ShopData
	 * @return
	 */
	public XinqiBseShop.ShopData toShopData(int newDiscount) {
		XinqiBseShop.ShopData.Builder builder = XinqiBseShop.ShopData.newBuilder();
		if ( !StringUtil.checkNotEmpty(_id) ) {
			int propId = StringUtil.toInt(propInfoId, 0);
			int fakeId = 100000 + propId;
			builder.setId(String.valueOf(fakeId));
		} else {
			builder.setId(_id);
		}
		builder.setType(type);
		builder.setInfo(info);
		builder.setPropInfoId(propInfoId);
		builder.setLevel(level);
		builder.setGoldTye(moneyType.type());
		for ( BuyPrice price : buyPrices ) {
			XinqiBseShop.BuyPrice pbPrice = XinqiBseShop.BuyPrice.newBuilder()
					.setPrice(price.price).setValidTimes(price.validTimes).build();
			builder.addBuyPrices(pbPrice);
		}
		builder.setBanded(banded==1);
		builder.setDiscount(newDiscount);
		builder.setSell(sell==1);
		if ( this.catalogs.contains(ShopCatalog.HOT) ) {
			builder.setHot(1);
		} else {
			builder.setHot(0);
		}
		builder.setLimitCount(limitCount);
		builder.setLimitGroup(limitGroup);
		builder.setShopId(shopId);
		return builder.build();
	}
	
	/**
	 * Convert this object to ProtoBuf's GoodsInfo
	 * @return
	 */
	public XinqiGoodsInfo.GoodsInfo toGoodsInfo() {
		XinqiGoodsInfo.GoodsInfo.Builder goodsInfoBuilder = XinqiGoodsInfo.GoodsInfo.newBuilder();
		if ( !StringUtil.checkNotEmpty(_id) ) {
			int propId = StringUtil.toInt(propInfoId, 0);
			int fakeId = 100000 + propId;
			goodsInfoBuilder.setId(fakeId);
		} else {
			goodsInfoBuilder.setId(StringUtil.toInt(_id, 0));
		}
		goodsInfoBuilder.setPropInfo(propInfoId);
		WeaponPojo weaponPojo = GameContext.getInstance().getEquipManager().getWeaponById(propInfoId);
		if ( weaponPojo == null ) {
	    goodsInfoBuilder.setAgilityLev(0);
	    goodsInfoBuilder.setAttackLev(0);
	    goodsInfoBuilder.setDefendLev(0);
	    goodsInfoBuilder.setLuckLev(0);
	    
			ItemPojo itemPojo = GameContext.getInstance().getItemManager().getItemById(propInfoId);
			if ( itemPojo != null ) {
		    goodsInfoBuilder.setLevel(itemPojo.getLevel());
			} else {
		    goodsInfoBuilder.setLevel(0);
			}
		} else {
	    goodsInfoBuilder.setAgilityLev(weaponPojo.getAddAgility());
	    goodsInfoBuilder.setAttackLev(weaponPojo.getAddAttack());
	    goodsInfoBuilder.setDefendLev(weaponPojo.getAddDefend());
	    goodsInfoBuilder.setLuckLev(weaponPojo.getAddLuck());
	    goodsInfoBuilder.setLevel(weaponPojo.getLv());
		}
		//sign: 是否是 新品或者热门商品 0为都不是 1 为新 2为热门 
		//0: normal; 1<<1: new; 1<<2: hot; 1<<3: recommend
		int sign = 0;
		for ( ShopCatalog catalog : this.catalogs ) {
			switch ( catalog ) {
				case HOT:
					sign |= 1<<2;
					break;
				case NEW:
					sign |= 1<<1;
					break;
				case RECOMMEND:
					sign |= 1<<3;
					break;
			}
		}
		goodsInfoBuilder.setSign(sign);
    goodsInfoBuilder.setGoldtype(moneyType.type());
    goodsInfoBuilder.setDiscount(discount);
    for ( BuyPrice price : buyPrices ) {
    	goodsInfoBuilder.addPrice((int)price.price);
    	goodsInfoBuilder.addIndate(price.validTimes);
    }
    return goodsInfoBuilder.build();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ShopPojo o) {
		int result = 0;
		if ( this.propInfoId != null && o.propInfoId != null) {
			result = o.propInfoId.compareTo( this.propInfoId );
		}
		if ( result == 0 ) {
			if ( this.info != null && o.info != null ){
				result = this.info.compareTo(o.info);
			}
		}
		if ( result == 0 ) {
			if ( this._id != null && o._id != null ){
				result = this._id.compareTo(o._id);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result
				+ ((propInfoId == null) ? 0 : propInfoId.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShopPojo other = (ShopPojo) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (info == null) {
			if (other.info != null)
				return false;
		} else if (!info.equals(other.info))
			return false;
		if (propInfoId == null) {
			if (other.propInfoId != null)
				return false;
		} else if (!propInfoId.equals(other.propInfoId))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShopPojo [_id=");
		builder.append(_id);
		builder.append(", type=");
		builder.append(type);
		builder.append(", info=");
		builder.append(info);
		builder.append(", propInfoId=");
		builder.append(propInfoId);
		builder.append(", level=");
		builder.append(level);
		builder.append(", goldType=");
		builder.append(moneyType);
		builder.append(", buyPrices=");
		builder.append(buyPrices);
		builder.append(", banded=");
		builder.append(banded);
		builder.append(", discount=");
		builder.append(discount);
		builder.append(", sell=");
		builder.append(sell);
		builder.append(", limitCount=");
		builder.append(limitCount);
		builder.append(", limitGroup=");
		builder.append(limitGroup);
		builder.append(", shopId=");
		builder.append(shopId);
		builder.append(", catalogs=");
		builder.append(catalogs);
		builder.append("]");
		return builder.toString();
	}

	public static class BuyPrice {
		//Price
		public int price;
		//Hours 
		public int validTimes;
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("[price=");
			builder.append(price);
			builder.append(", times=");
			builder.append(validTimes);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
	/**
	 * A wrapper class for ShopManager#buyGoodFromShop
	 * Note: the count is not used in equals() method.
	 * @author wangqi
	 *
	 */
	public static class BuyInfo {
		public String goodId;
		public int count;
		public int indateIndex;
		public int color;
		//Level is preserved for future use.
		public int level;
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + color;
			result = prime * result + ((goodId == null) ? 0 : goodId.hashCode());
			result = prime * result + indateIndex;
			result = prime * result + level;
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BuyInfo other = (BuyInfo) obj;
			if (color != other.color)
				return false;
			if (goodId == null) {
				if (other.goodId != null)
					return false;
			} else if (!goodId.equals(other.goodId))
				return false;
			if (indateIndex != other.indateIndex)
				return false;
			if (level != other.level)
				return false;
			return true;
		}
		
	}
}
