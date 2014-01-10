package com.xinqihd.sns.gameserver.entity.user;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.proto.XinqiFriendInfoLite.FriendInfoLite;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.JsonUtil;

/**
 * User's relationship
 * 
 * @author wangqi
 *
 */
public class Relation {
	
	//Our customized userid (shardkey)
	private UserId _id = null;
  
  // Relation's type
  private RelationType type;
  
  private LinkedHashMap<String, People> peoples = new LinkedHashMap<String, People>();
  
  public static final int MAX_RELATION = 50;
    
  // -------------------------------- Internal use fields
  
  //For internal use only
  private transient User parentUser;
  
  private transient Set<String> changedFlag = new HashSet<String>(); 
    
	// -------------------------------- Properties method

	/**
	 * @return the _id
	 */
	public UserId get_id() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void set_id(UserId _id) {
		this._id = _id;
	}

	/**
	 * @return the type
	 */
	public RelationType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(RelationType type) {
		this.type = type;
	}

	/**
	 * @return the people
	 */
	public People removePeople(People p) {
		People people = peoples.remove(p.getUsername());
		synchronized ( changedFlag ) {
			changedFlag.add(p.username);
		}
		return people;
	}

	/**
	 * @param people the people to set
	 */
	public void addPeople(People p) {
		if ( this.peoples.size() >= MAX_RELATION ) {
			String oldestName = this.peoples.keySet().iterator().next();
			People oldest = findPeopleByUserName(oldestName);
			this.removePeople(oldest);
		}
		this.peoples.put(p.username, p);
		synchronized ( changedFlag ) {
			changedFlag.add(p.username);
		}
	}
	
	/**
	 * Mark that the people is modified
	 * @param p
	 */
	public void modifyPeople(People p) {
		synchronized ( changedFlag ) {
			changedFlag.add(p.username);
		}
	}
	
	/**
	 * Find the people by its name.
	 * @param name
	 * @return
	 */
	public People findPeopleByUserName(String name) {
		return this.peoples.get(name);
	}
	
	/**
	 * Return the list of people in random order.
	 * @return
	 */
	public Collection<People> listPeople() {
		return this.peoples.values();
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
	}
	
	/**
	 * Get the changed flag and clear the internal marks
	 * @return
	 */
	public Set<String> clearChangeMark() {
		Set<String> flags = new HashSet<String>();
		synchronized ( changedFlag ) {
			flags.addAll(changedFlag);
			changedFlag.clear();
		}
		return flags;
	}
	
	/**
	 * Debug for string
	 */
	public String toString() {
		return JsonUtil.serialize(this);
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<FriendInfoLite> toBseFriendList() {
		Collection<People> friends = this.listPeople();
		LinkedList<FriendInfoLite> friendList = new LinkedList<FriendInfoLite>();
		for (Iterator iter = friends.iterator(); iter.hasNext();) {
			People people = (People) iter.next();
			BasicUser basicUser = GameContext.getInstance().getUserManager()
					.queryBasicUserByRoleName(people.getRolename());
			if ( basicUser != null ) {
				people.setBasicUser(basicUser);
				FriendInfoLite friendInfo = people.toFriendInfoLite(type, basicUser);
				friendList.add(0, friendInfo);
			}
		}
		return friendList;
	}
	
	/**
	 * 
	 * @author wangqi
	 *
	 */
  public static class People implements Serializable {
		
		private static final long serialVersionUID = 1409045895216741967L;
		
  	//User's id
		private UserId id;
		private UserId myId;
		//User's name
		private String username;
		private String rolename;
  	//The winning time between him and me
  	private int win;
  	//The losing time between him and me
  	private int lose;
  	//The user's level
  	private int level;
  	//The basic user. It is a temporary value
  	//and will never be stored in database.
  	private transient BasicUser basicUser;
  	
  	/**
		 * @return the userId
		 */
		public UserId getId() {
			return id;
		}
		
		/**
		 * @param userId the userId to set
		 */
		public void setId(UserId userId) {
			this.id = userId;
		}
		
		/**
		 * @return the myId
		 */
		public UserId getMyId() {
			return myId;
		}

		/**
		 * @param myId the myId to set
		 */
		public void setMyId(UserId myId) {
			this.myId = myId;
		}

		/**
		 * @return the username
		 */
		public String getUsername() {
			return username;
		}
		/**
		 * @param username the username to set
		 */
		public void setUsername(String username) {
			this.username = username;
		}
		/**
		 * @return the rolename
		 */
		public String getRolename() {
			return rolename;
		}
		/**
		 * @param rolename the rolename to set
		 */
		public void setRolename(String rolename) {
			this.rolename = rolename;
		}

		/**
		 * @return the winCount
		 */
		public int getWin() {
			return win;
		}
		/**
		 * @param winCount the winCount to set
		 */
		public void setWin(int winCount) {
			this.win = winCount;
		}
		/**
		 * @return the loseCount
		 */
		public int getLose() {
			return lose;
		}
		/**
		 * @param loseCount the loseCount to set
		 */
		public void setLose(int loseCount) {
			this.lose = loseCount;
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
		
		public BasicUser getBasicUser() {
			return this.basicUser;
		}
		
		public void setBasicUser(BasicUser basicUser) {
			this.basicUser = basicUser;
		}

		public String toString() {
			return JsonUtil.serialize(username);
		}
				
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((myId == null) ? 0 : myId.hashCode());
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
			People other = (People) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (myId == null) {
				if (other.myId != null)
					return false;
			} else if (!myId.equals(other.myId))
				return false;
			return true;
		}

		/**
		 * 
		 * @param type
		 * @param basicUser
		 * @return
		 */
		public FriendInfoLite toFriendInfoLite(RelationType type, BasicUser basicUser) {
			return toFriendInfoLite(type, basicUser, true);
		}
		/**
		 * Convert the people to FriendInfoLite
		 * @param type
		 * @param basicUser
		 * @return
		 */
		public FriendInfoLite toFriendInfoLite(RelationType type, BasicUser basicUser, boolean updateOnline) {
			FriendInfoLite.Builder fiBuilder = FriendInfoLite.newBuilder();
			fiBuilder.setFriendtype(type.ordinal());
			String roleName = UserManager.getDisplayRoleName(basicUser.getRoleName());
			fiBuilder.setNickName(roleName);
			fiBuilder.setIsYellowDmd(basicUser.isVip());
			fiBuilder.setWins(win);
			fiBuilder.setFails(lose);
			fiBuilder.setLevel(basicUser.getLevel());
			// TODO check session
			if ( updateOnline ) {
				SessionKey sessionKey = GameContext.getInstance().findSessionKeyByUserId(myId);
				fiBuilder.setOnline( sessionKey != null );
			} else {
				fiBuilder.setOnline( false );
			}
			fiBuilder.setUid(basicUser.get_id().toString());
			float total = win + lose;
			if ( total == 0 ) total = 1;
			fiBuilder.setWinOdds(Math.round(win * 100 / total));
			if ( basicUser.getIconurl() != null ) {
				fiBuilder.setHeadurl(basicUser.getIconurl());
			} else {
				fiBuilder.setHeadurl(Constant.EMPTY);
			}
			fiBuilder.setOpenid(Constant.EMPTY);
			return fiBuilder.build();
		}
  }
}
