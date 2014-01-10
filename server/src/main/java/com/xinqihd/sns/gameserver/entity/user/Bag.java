package com.xinqihd.sns.gameserver.entity.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.BiblioManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.proto.XinqiBseAddProp.BseAddProp;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The user's bag. Because it will be frequently added and removed items,
 * these data should be in a sperate collection in database.
 * 
 * Note: This object is not thread-safe. I assume only one user will modify its
 * Bag, so the synchronization is not needed.
 * 
 * 
 * @author wangqi
 *
 */
public class Bag implements Serializable {
  
	private static final long serialVersionUID = 3975829426165286204L;
	
	private static final Logger logger = LoggerFactory.getLogger(Bag.class);
	
	public static final int BAG_WEAR_COUNT = PropDataEquipIndex.values().length;
	
	//Our customized userid (shardkey)
	protected UserId _id = null;
	
  /**
   * The maximun items that this user can save in the bag.
   */
  private int maxCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_BAG_MAX, 70);
  
  private int currentCount = 0;
  
	//Use the PropDataEquipIndex as index
  private final ArrayList<PropData> wearedPropDatas = new ArrayList<PropData>(BAG_WEAR_COUNT);
  
  //The whole bag package.
  private final ArrayList<PropData> otherPropDataList = new ArrayList<PropData>(maxCount);
      
  //It is for internal use only.
  private transient User parentUser;
    
  /**
   * Used for tracking the genernal change.
   */
  private transient boolean changeFlag = false;
    
  /**
   * Used for tracking the wearedPropDatas change and otherPropDatas change.
   */
  private transient Set<Integer> changedFieldSet = new HashSet<Integer>(
  		(otherPropDataList.size()+BAG_WEAR_COUNT)<<2);
  
  /**
   * Create an empty bag with at least 'BAG_WEAR_COUNT' 
   */
  public Bag() {
  	for ( int i=0; i<BAG_WEAR_COUNT; i++ ) {
  		this.wearedPropDatas.add(null);
  	}
  }
  
	/**
	 * @return the _id
	 */
	public UserId getUserid() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void setUserd(UserId _id) {
		this._id = _id;
		this.changeFlag = true;
	}

	/**
	 * @return the count
	 */
	public int getMaxCount() {
		synchronized ( changedFieldSet ) {
			return maxCount;
		}
	}

	/**
	 * @param maxCount the count to set
	 */
	public void setMaxCount(int maxCount) {
		synchronized ( changedFieldSet ) {
			this.maxCount = maxCount;
			this.changeFlag = true;			
		}
	}

	/**
	 * @return the currentCount
	 */
	public int getCurrentCount() {
		synchronized ( changedFieldSet ) {
			return currentCount;
		}
	}

	/**
	 * @param currentCount the currentCount to set
	 */
	public void setCurrentCount(int currentCount) {
		synchronized ( changedFieldSet ) {
			this.currentCount = currentCount;
			this.changeFlag = true;
		}
	}

	/**
	 * @return the wearedPropDatas
	 */
	public List<PropData> getWearPropDatas() {
		return wearedPropDatas;
	}

	/**
	 * @return the otherPropDatas
	 */
	public List<PropData> getOtherPropDatas() {
		return otherPropDataList;
	}
	
	/**
	 * Get the PropData in bag at given pew
	 * @param pew
	 * @return
	 */
	public PropData getOtherPropData(final int pew) {
		synchronized ( changedFieldSet ) {
			int index = pew;
			if ( pew >= BAG_WEAR_COUNT ) {
				index = pew - BAG_WEAR_COUNT;
			}
			if ( index >= 0 && index < this.otherPropDataList.size() ) {
				return this.otherPropDataList.get(index);
			}
			return null;
		}
	}
	
	/**
	 * Sort all items in user bag. Merge all the same 
	 * items into one position to save space.
	 * 
	 * Note: After calling this method, the saveBag should be
	 * called with 'true'
	 */
	public void tidyUserBag() {
		//TODO
		synchronized ( changedFieldSet ) {
			ArrayList<PropData> weapons = new ArrayList<PropData>();
			LinkedHashMap<String, PropData> tmpSet = new LinkedHashMap<String, PropData>();
			for ( PropData propData : this.otherPropDataList ) {
				if ( propData != null ) {
					if ( propData.isWeapon() ) {
						weapons.add(propData);
					} else {
						ItemPojo item = ItemManager.getInstance().getItemById(propData.getItemId());
						PropData existPropData = tmpSet.get(propData.getItemId());
						if ( existPropData != null && item != null ) {
							existPropData.setCount( existPropData.getCount() + propData.getCount() );
						} else {
							tmpSet.put(propData.getItemId(), propData);
						}
					}
				}
			}
			TreeSet<PropData> tmpList = new TreeSet<PropData>();
			for ( PropData propData : weapons ) {
				tmpList.add(propData);
			}
			Collection<PropData> tideCollection = tmpSet.values();
			for ( PropData propData : tideCollection ) {
				tmpList.add(propData);
			}
			//Check the collect task
			TaskManager.getInstance().processUserTasks(parentUser, TaskHook.COLLECT, null);
			
			//adjust the pew
			int i = 0;
			for ( PropData propData : tmpList ) {
				propData.setPew(BAG_WEAR_COUNT+(i++));
			}
			this.otherPropDataList.clear();
			this.otherPropDataList.addAll(tmpList);
			this.setCurrentCount(this.otherPropDataList.size());
		}
	}

	/**
	 * Add a new PropData in bag. If there is empty space,
	 * the new item will be inserted into empty space first.
	 * Otherwise, it will be appended to the last space.
	 * 
	 * Note: If the total count of bag items exceed maximun
	 * count, nothing will happend and the method will return 
	 * false.
	 * 
	 * @param otherPropDatas the otherPropDatas to set
	 */
	public boolean addOtherPropDatas(PropData newPropData) {
		return addOtherPropDatas(newPropData, true);
	}

	/**
	 * Add a new PropData in bag. If there is empty space,
	 * the new item will be inserted into empty space first.
	 * Otherwise, it will be appended to the last space.
	 * 
	 * Note: If the total count of bag items exceed maximun
	 * count, nothing will happend and the method will return 
	 * false.
	 * 
	 * @param otherPropDatas the otherPropDatas to set
	 */
	public boolean addOtherPropDatas(PropData newPropData, boolean notify) {
		//Try to find if there is an empty position
		synchronized ( changedFieldSet ) {
			int emptyIndex = -1;
			int length = this.otherPropDataList.size();
			for ( int i=0; i<length; i++ ) {
				if ( this.otherPropDataList.get(i) == null ) {
					emptyIndex = i;
					break;
				}
			}
			if ( emptyIndex == -1 ) {
				this.otherPropDataList.add(null);
				emptyIndex = length;
			}
			if ( emptyIndex >= this.maxCount ) {
				//There is too many items.
				if ( parentUser != null ) {
					StatClient.getIntance().sendDataToStatServer(parentUser, StatAction.BagFull, this.maxCount, parentUser.isVip());
				}
				return false;
			} else {
				int pew = emptyIndex + BAG_WEAR_COUNT;
				this.otherPropDataList.set(emptyIndex, newPropData);
				if ( newPropData != null ) {
					newPropData.setPew(pew);
					newPropData.setAddTimestamp(System.currentTimeMillis());
					if ( newPropData.isBanded() ) {
						if ( newPropData.getBandUserName()==null && parentUser != null ) {
							newPropData.setBandUserName(parentUser.getUsername());
						}
					}
					this.currentCount++;
				  //Mark the change
				}
				markChangeFlag(pew);
				this.changeFlag = true;
				
				if ( newPropData != null && notify && this.parentUser != null && 
						!this.parentUser.isAI() && !this.parentUser.isProxy() ) {
					//Notify client
					//Send the data back to client
					if ( this.parentUser.getSessionKey() != null ) {
					  //Call task script here
						TaskManager.getInstance().processUserTasks(parentUser, TaskHook.ADD_BAG, newPropData);
						//Bag task
						TaskManager.getInstance().processUserTasks(parentUser, TaskHook.ADD_BAG_COUNT, this.currentCount);
						//Check the collect task
						TaskManager.getInstance().processUserTasks(parentUser, TaskHook.COLLECT, newPropData);

						
						BseAddProp.Builder addPropBuilder = BseAddProp.newBuilder();
						addPropBuilder.setAddedProp(newPropData.toXinqiPropData(this.parentUser));
						GameContext.getInstance().writeResponse(this.parentUser.getSessionKey(), addPropBuilder.build());
						logger.debug("The {} is added to the user {}'s bag. The pew is {}", 
								new Object[]{newPropData.getName(), this.parentUser.getRoleName(), newPropData.getPew()});
						
						SysMessageManager.getInstance().sendClientInfoRawMessage(parentUser.getSessionKey(), 
								Text.text("bag.add", newPropData.getName()), 3000);
						
						StatClient.getIntance().sendDataToStatServer(parentUser, StatAction.BagAdd, 
							newPropData.getName(), newPropData.getWeaponColor(), 
							newPropData.getLevel(), newPropData.getPew(), newPropData.hashCode());
						
						int left = this.maxCount - this.currentCount;
						if ( left <= 0 ) {
							SysMessageManager.getInstance().sendClientInfoRawMessage(
									parentUser.getSessionKey(), Text.text("bag.add.full"), Action.NOOP, XinqiSysMessage.Type.NORMAL);
						} else if (  left <= 5 ) {
							/**
							 * If the left empty cell is less than 5, prompt the user
							 */
							SysMessageManager.getInstance().sendClientInfoRawMessage(
									parentUser.getSessionKey(), Text.text("bag.near.full", left), Action.NOOP, XinqiSysMessage.Type.NORMAL);
						}
						
						if ( newPropData.isWeapon() ) {
							UserBiblio userBiblio = parentUser.getBiblio();
							if ( userBiblio != null ) {
								BiblioManager.getInstance().
									addBiblio(parentUser, newPropData);
							}
						}
						StatClient.getIntance().sendDataToStatServer(parentUser, StatAction.BiblioAdd, newPropData.getName());
					} else {
						logger.debug("Bag's parent user or session key is null. It cannot send BseAddProp {} to client", newPropData);
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Set the PropData at given pew. If the pew exceeds total available
	 * item count, it will return false and do nothing.
	 * 
	 * @param newPropData
	 * @param pew
	 * @return
	 */
	public boolean setOtherPropDataAtPew(PropData newPropData, int pew) {
		synchronized ( changedFieldSet ) {
			int index = pew - BAG_WEAR_COUNT;
			if ( index < 0 || index >= this.maxCount ) {
				return false;
			} else {
				if ( index >= this.otherPropDataList.size() ) {
					for ( int i=this.otherPropDataList.size(); i<=index; i++ ) {
						this.otherPropDataList.add(null);
						markChangeFlag(i+BAG_WEAR_COUNT);
					}
				}
				int originalHashCode = 0;
				PropData original = this.otherPropDataList.get(index);
				if ( original != null ) {
					originalHashCode = original.hashCode();
				}
				if ( original == null && newPropData != null ) {
					this.currentCount++;
				}
				this.otherPropDataList.set(index, newPropData);
				if ( newPropData != null ) {
					newPropData.setPew(pew);
					if ( newPropData.getCount() == 0 ) {
						newPropData.setCount(1);
					}
					if ( this.parentUser != null && !this.parentUser.isAI() && !this.parentUser.isProxy() ) {
						StatClient.getIntance().sendDataToStatServer(parentUser, StatAction.BagSet, 
							newPropData.getName(), newPropData.getColor(), 
							newPropData.getLevel(), newPropData.hashCode(), originalHashCode);
					}
				}
				markChangeFlag(pew);
				this.changeFlag = true;
			}
		}
		return true;
	}
	
	/**
	 * Set the PropData to WearPropData at given index.
	 * 
	 * Note: It is only used when reading from database.
	 * For normal use, call the wearPropData(int,int) method instead.
	 * 
	 * Note: This method will never set the change bit.
	 * 
	 * @param newPropData
	 * @param index
	 */
	public void setWearPropData(PropData newPropData, PropDataEquipIndex index) {
		this.setWearPropData(newPropData, index.index());
	}
	
	/**
	 * Set the PropData to WearPropData at given index.
	 * 
	 * Note: It is only used when reading from database.
	 * For normal use, call the wearPropData(int,int) method instead.
	 * 
	 * Note: This method will never set the change bit.
	 * 
	 * @param newPropData
	 * @param pew
	 */
	public void setWearPropData(PropData newPropData, int pew) {
		synchronized ( changedFieldSet ) {
			if ( pew >= 0 && pew < Bag.BAG_WEAR_COUNT ) {
				this.wearedPropDatas.set(pew, newPropData);
				newPropData.setPew(pew);
				markChangeFlag(pew);
				this.changeFlag = true;
			}
		}
	}
	
	/**
	 * If we modify the PropData outside, call this method to remember the change.
	 * Otherwise, the change will not be saved into database.
	 * 
	 * @param propData
	 */
	public void setChangeFlagOnItem(PropData propData) {
		synchronized ( changedFieldSet ) {
			int pew = propData.getPew();
			if ( pew >= 0 && pew < BAG_WEAR_COUNT + this.otherPropDataList.size() ) {
				markChangeFlag(pew);
				this.changeFlag = true;
			}
		}
	}
	
	/**
	 * Remove a PropData from Bag. It is a costly operation.
	 * @param otherPropDatas the otherPropDatas to set
	 */
	public PropData removeWearPropDatas(PropDataEquipIndex index) {
		synchronized ( changedFieldSet ) {
			int itemIndex = index.index();
			PropData wearedPropData = this.wearedPropDatas.get(itemIndex);
			if ( wearedPropData != null ) {
				//Remove it.
				this.wearedPropDatas.set(itemIndex, null);
				this.markChangeFlag(itemIndex);
				this.changeFlag = true;
			}
			return wearedPropData;
		}
	}
	
	
	/**
	 * Remove a PropData from Bag. It is a costly operation.
	 * 
	 * Note: The pew = realListIndex + BAG_WEAR_COUNT;
	 * 
	 * @param pew The PropData position in user bag. It is the real index+BAG_WEAR_COUNT.
	 */
	public void removeOtherPropDatas(int pew) {
		synchronized ( changedFieldSet ) {
			int index = pew - BAG_WEAR_COUNT;
			if ( index < 0 || index >= this.otherPropDataList.size() ) {
				logger.warn("removeOtherPropDatas: The pew {} in bag is not found.", pew);
				return;
			}
			
			PropData oldPropData = this.otherPropDataList.get(index);
			if (oldPropData == null ) { 
				return;
			}
			
			oldPropData.subCount();
			
			if ( oldPropData.getCount() <= 0 ) {
				this.otherPropDataList.set(index, null);
				oldPropData.setPew(-1);
				this.currentCount--;
			}
			if ( parentUser != null && !this.parentUser.isAI() && !this.parentUser.isProxy() ) {
				StatClient.getIntance().sendDataToStatServer(parentUser, StatAction.BagRemove, 
					oldPropData.getName(), oldPropData.getWeaponColor(), 
					oldPropData.getLevel(), oldPropData.getPew(), oldPropData.hashCode());
			}
			markChangeFlag(pew);
			this.changeFlag = true;
		}
	}
	
	/**
	 * Remove all propDatas at given pew
	 * @param pew
	 */
	public int removeOtherPropDatasCount(int pew, int count) {
		synchronized ( changedFieldSet ) {
			int index = pew - BAG_WEAR_COUNT;
			if ( index < 0 || index >= this.otherPropDataList.size() ) {
				logger.warn("removeOtherPropDatas: The pew {} in bag is not found.", pew);
				return 0;
			}
			
			PropData oldPropData = this.otherPropDataList.get(index);
			if (oldPropData == null ) { 
				return 0;
			}
			
			int hasCount = oldPropData.getCount();
			int delCount = 0;
			if ( hasCount > count ) {
				oldPropData.setCount(hasCount-count);
				delCount = count;
			} else {
				oldPropData.setCount(0);
				this.otherPropDataList.set(index, null);
				oldPropData.setPew(-1);
				this.currentCount--;
				delCount = hasCount;
			}

			if ( parentUser != null && !this.parentUser.isAI() && !this.parentUser.isProxy() ) {
				StatClient.getIntance().sendDataToStatServer(parentUser, StatAction.BagRemove, 
					oldPropData.getName(), oldPropData.getWeaponColor(), 
					oldPropData.getLevel(), oldPropData.getPew(), oldPropData.hashCode());
			}
			markChangeFlag(pew);
			this.changeFlag = true;
			
			return delCount;
		}
	}
	
	/**
	 * Move a propData from the 'fromIndex' to 'toIndex'.
	 * If the 'toIndex' is in wearing position, check if there
	 * is item on that position. If ture, move the item into
	 * bag first.
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public boolean movePropData(int fromIndex, int toIndex) {
		synchronized ( changedFieldSet ) {
			boolean result = true;
			PropData fromPropData = null;
			if ( fromIndex < BAG_WEAR_COUNT ) {
				fromPropData = this.wearedPropDatas.get(fromIndex);
			} else {
				fromPropData = this.getOtherPropData(fromIndex);
				//Check if it can be wore
				if ( fromPropData == null ) {
					result = false;
				} else if ( parentUser!= null ) {
					if ( !fromPropData.isWeapon() ) {
						result = false;
					} else {
						WeaponPojo weapon = (WeaponPojo)fromPropData.getPojo();
						if ( toIndex < Bag.BAG_WEAR_COUNT ) {
							EquipType givenType = PropDataEquipIndex.values()[toIndex].getPropEquipType();
							if ( givenType != null && givenType != weapon.getSlot() ) {
								result = false;								
							}
						}
						
						if ( result ) {
							if ( fromPropData.getUserLevel() > parentUser.getLevel() ) {
								result = false;
								SysMessageManager.getInstance().sendClientInfoMessage(parentUser, 
										"bag.levelhigh", Action.NOOP, new Object[]{fromPropData.getUserLevel()});
							}
						}
						if ( result ) {
							if ( parentUser != null ) {
								if ( weapon.getSex() != Gender.ALL && weapon.getSex() != parentUser.getGender() ) {
									result = false;
									String message = Text.text("bag.sexwrong", weapon.getSex().getTitle());
									SysMessageManager.getInstance().sendClientInfoRawMessage(parentUser.getSessionKey(), 
											message, 3000);
								}
							}
						}
						if ( result ) {
							/**
							 * Check if the device is banded
							 */
							if ( fromPropData.isBanded() ) {
								if ( fromPropData.getBandUserName() != null ) {
									if ( !fromPropData.getBandUserName().equals(this.parentUser.getUsername()) ) {
										String message = Text.text("bag.wear.banded", new Object[]{fromPropData.getName()});
										SysMessageManager.getInstance().sendClientInfoRawMessage(
												this.parentUser.getSessionKey(), message, 3000);
										result = false;
									}
								} else {
									fromPropData.setBandUserName(this.parentUser.getUsername());
								}
							} else {
								fromPropData.setBandUserName(null);
							}
						}
					}
				}
			}
			if ( result ) {
				PropData toPropData = null;
				if ( toIndex >= 0 ) {
					if ( toIndex < BAG_WEAR_COUNT ) {
						toPropData = this.wearedPropDatas.get(toIndex);
						/**
						 * remove the toPropData first
						 * wangqi 2012-09-20
						 */
						this.removeWearPropDatas(PropDataEquipIndex.values()[toIndex]);
						if ( toPropData != null ) {
							this.recalculateUserProperties(toPropData, false);
						}
						//Wear the new weapon
						result = this.wearPropData(fromIndex, toIndex);
						//add the removed propData back to bag.
						this.addOtherPropDatas(toPropData, false);
					} else {
						toPropData = this.getOtherPropData(toIndex);
						/**
						 * remove the toPropData first
						 * wangqi 2012-09-20
						 */
						this.removeOtherPropDatas(toIndex);
						//Wear the new weapon
						result = this.wearPropData(fromIndex, toIndex);
						this.setWearPropData(toPropData, fromIndex);
					  //The removeOtherPropDatas will subtract 1 from propData's count
						//It is a bug
						//wangqi 2012-2-16
						toPropData.addCount();
						/**
						 * When user wear a new weapon or unwear a weapon, his/her properties such as
						 * power, attack, defend etc.. should be recalculated
						 */
						recalculateUserProperties(toPropData, true);
						
						//Call task script here
						TaskManager.getInstance().processUserTasks(parentUser, TaskHook.WEAR, toPropData);
					}
				} else {
					//toIndex == -1
					result = this.wearPropData(fromPropData.getPew(), -1);
				}
			}
			return result;
		}
	}
	
	/**
	 * The user wear or unwear a specific PropData.
	 * If the "fromIndex" > BAG_WEAR_COUNT, then the user wants to wear a propdata.
	 * If the "toIndex" < BAG_WEAR_COUNT, then the user wants to unwear a propdata.
	 * In this case, the secondIndex is ignored.
	 * 
	 * @param fromIndex 
	 * @param toIndex 
	 */
	public boolean wearPropData(int fromIndex, int toIndex) {
		synchronized ( changedFieldSet ) {
			boolean success = false;
			if ( fromIndex >= BAG_WEAR_COUNT ) {
				//User wants to wear something.
				int otherPropDataIndex = fromIndex - BAG_WEAR_COUNT;
				if ( (toIndex >= 0 && toIndex < BAG_WEAR_COUNT) &&
						(otherPropDataIndex >= 0 && otherPropDataIndex < this.otherPropDataList.size() ) ) {
					PropData propData = this.otherPropDataList.get(otherPropDataIndex);
					if ( propData != null && !propData.isExpire() ) {
						this.wearedPropDatas.set(toIndex, propData);
						this.removeOtherPropDatas(fromIndex);
						//The removeOtherPropDatas will subtract 1 from propData's count
						//It is a bug
						//wangqi 2012-2-16
						propData.addCount();
						propData.setPew(toIndex);
						
						this.changeFlag = true;
						markChangeFlag(toIndex);
						markChangeFlag(fromIndex);
						/**
						 * When user wear a new weapon or unwear a weapon, his/her properties such as
						 * power, attack, defend etc.. should be recalculated
						 */
						recalculateUserProperties(propData, true);
						
						//Call task script here
						TaskManager.getInstance().processUserTasks(parentUser, TaskHook.WEAR, propData);
						return true;
					} else {
						if ( propData.isExpire() ) {
							logger.debug("propData {} expired and cannot be wore.", propData.getName());
							if ( parentUser != null ) {
								SysMessageManager.getInstance().sendClientInfoMessage(
										parentUser, "bag.wear.expire", Type.NORMAL, new Object[]{propData.getName()});
							}
						}
					}
					return false;
				} else {
					return false;
				}
			} else {
				//User wants unwear something.
				if ( fromIndex >= 0 && fromIndex < BAG_WEAR_COUNT ) {
					
					//Check if the bag is full
					if ( this.getCurrentCount()>=this.getMaxCount() ) {
						success = false;
						//do nothing
						if ( parentUser != null ) {
							logger.debug("User '{}' bag is full. It cannot unwear an equip into bag.",
									parentUser.getRoleName());
						  //Send notify
							SysMessageManager.getInstance().sendClientInfoMessage(parentUser, "bag.full", Type.NORMAL);
						}
						
						return success;
					}
					
					PropData wearedPropData = this.removeWearPropDatas(
							PropDataEquipIndex.values()[fromIndex]);
					
					if ( wearedPropData != null ) {
						/**
						 * When user wear a new weapon or unwear a weapon, his/her properties such as
						 * power, attack, defend etc.. should be recalculated
						 */
						/**
						 * If the propData is expired, do not calculate its value
						 */
						if ( !wearedPropData.isExpire() ) {
							recalculateUserProperties(wearedPropData, false);
						}
						
						if ( toIndex < BAG_WEAR_COUNT 
								|| toIndex >= this.otherPropDataList.size() + BAG_WEAR_COUNT ) {
							success = this.addOtherPropDatas(wearedPropData, false);
						} else {
							this.setOtherPropDataAtPew(wearedPropData, toIndex);
							/*
							this.otherPropDataList.set(toIndex-BAG_WEAR_COUNT, wearedPropData);
							wearedPropData.setPew(toIndex);
							*/
							success = true;
						}
						return success;
					} else {
						logger.warn("User does not wear pew: {}", fromIndex);
						return false;
					}
				} else {
					logger.warn("wearpropData invalid firstIndex {}", fromIndex);
					return false;
				}
			}
		}
	}
	
	/**
	 * When user wear a new weapon or unwear a weapon, his/her properties such as
	 * power, attack, defend etc.. should be recalculated
	 */
	public void recalculateUserProperties(PropData propData, boolean wearEquip) {
		/**
		 * TODO Remove corresponding properties to user.
		 * 
		 * 伤害=武器的伤害+其他伤害（附加属性，镶嵌的宝珠等）
		 * 护甲=衣服的护甲+帽子的护甲+其他护甲（附加属性，镶嵌的宝珠等）
		 * 攻击=全身装备的攻击总和
		 * 防御=全身装备的防御总和
		 * 敏捷=全身装备的敏捷总和
		 * 幸运=全身装备的幸运总和
		 */
		if ( parentUser != null && !parentUser.isDefaultUser() ) {
			ScriptManager.getInstance().runScript(
					ScriptHook.USER_PROP_CALCULATE, parentUser, propData, wearEquip);
		} else {
			logger.warn("Bag's parentUser should not be null!");
		}
	}

	/**
	 * @return the parentUser
	 */
	public User getParentUser() {
		return parentUser;
	}

	/**
	 * @param parentUser the parentUser to set
	 */
	public void setParentUser(User parentUser) {
		this.parentUser = parentUser;
		this.changeFlag = true;
		
//		parentUser.setBag(this);
	}

	/**
	 * Remove all items in other propData list
	 */
	public void cleanOtherPropDatas() {
		synchronized ( changedFieldSet ) {
			this.otherPropDataList.clear();
			this.currentCount = 0;
			this.changeFlag = true;
		}
	}
	
	/**
	 * Clear the change flag.
	 * @return
	 */
	public boolean clearGeneralChangeFlag() {
		synchronized ( changedFieldSet ) {
			boolean result = this.changeFlag;
			this.changeFlag = false;
			return result;
		}
	}
	
	/**
	 * Get and clear the prop data list changed flag.
	 */
	public Set<Integer> clearMarkedChangeFlag() {
		synchronized ( changedFieldSet ) {
			Set<Integer> flags = new HashSet<Integer>();
			synchronized ( this ) {
				flags.addAll(changedFieldSet);
				changedFieldSet.clear();
			}
			return flags;
		}
	}
		
	/**
	 * Mark the change behaviour
	 * @param index
	 * @param isWearedPropDataChange
	 */
	public void markChangeFlag(int index) {
		synchronized (changedFieldSet) {
			changedFieldSet.add(index);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result
				+ ((otherPropDataList == null) ? 0 : otherPropDataList.hashCode());
		result = prime * result
				+ ((wearedPropDatas == null) ? 0 : wearedPropDatas.hashCode());
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
		Bag other = (Bag) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (otherPropDataList == null) {
			if (other.otherPropDataList != null)
				return false;
		} else if (!otherPropDataList.equals(other.otherPropDataList))
			return false;
		if (wearedPropDatas == null) {
			if (other.wearedPropDatas != null)
				return false;
		} else if (!wearedPropDatas.equals(other.wearedPropDatas))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Bag [_id=");
		builder.append(_id);
		builder.append(", count=");
		builder.append(maxCount);
		builder.append(", wearedPropDatas=");
		builder.append(wearedPropDatas);
		builder.append(", otherPropDataList=");
		builder.append(otherPropDataList);
		builder.append("]");
		return builder.toString();
	}

	
}
