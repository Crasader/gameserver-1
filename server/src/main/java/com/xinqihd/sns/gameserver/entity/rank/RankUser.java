package com.xinqihd.sns.gameserver.entity.rank;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiArrangeInfo.ArrangeInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseMyRankInfo.BseMyRankInfo;

/**
 * The rank wrapper object
 * 
 * @author wangqi
 *
 */
public class RankUser implements Comparable {
	
	//The rank of this user
	private int rank;
	
	//The score of this user
	private int score;
	
	// if it is > 0, it means user's level is down.
	// If it is < 0, it means user's level is up.
	private int rankChange;
	
	//The user's information
	private BasicUser basicUser;

	/**
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}

	/**
	 * @return the rankChange
	 */
	public int getRankChange() {
		return rankChange;
	}

	/**
	 * @param rankChange the rankChange to set
	 */
	public void setRankChange(int rankChange) {
		this.rankChange = rankChange;
	}

	/**
	 * @return the basicUser
	 */
	public BasicUser getBasicUser() {
		return basicUser;
	}

	/**
	 * @param basicUser the basicUser to set
	 */
	public void setBasicUser(BasicUser basicUser) {
		this.basicUser = basicUser;
	}
	
	/**
	 * 
	 * @param scoreType
	 * @return
	 */
	public ArrangeInfo toArrangeInfo(RankScoreType scoreType) {
		ArrangeInfo.Builder builder = ArrangeInfo.newBuilder();
		builder.setRank(rank);
		builder.setScore(score);
		builder.setType(scoreType.ordinal());
		BasicUser user = this.basicUser;
		if ( user != null ) {
			if ( user.get_id() != null ) {
				String userId = user.get_id().toString();
				builder.setUserid(userId);
			}
			String roleName = UserManager.getDisplayRoleName(user.getRoleName());
			if ( roleName != null ) {
				builder.setName(roleName);
			} else {
				builder.setName(Constant.EMPTY);
			}
			builder.setLevel(user.getLevel());
			builder.setPower(user.getPower());
			if ( user.isVip() ) {
				int vipLevel = user.getViplevel();
				builder.setViplevel(vipLevel);
			} else {
				builder.setViplevel(0);
			}
		} else {
			builder.setUserid(Constant.EMPTY);
			builder.setName(Constant.EMPTY);
			builder.setLevel(0);
		}
		return builder.build();
	}
	
	/**
	 * 
	 * @return
	 */
	public BseMyRankInfo toBseMyRankInfo() {
		BseMyRankInfo.Builder builder = BseMyRankInfo.newBuilder();
		builder.setMyRank(rank);
		builder.setRankChange(rankChange);
		if ( this.basicUser != null ) {
			builder.setMyLevel(this.basicUser.getLevel());
			User user = null;
			if ( this.basicUser instanceof User ) {
				user = (User)this.basicUser;
				builder.setMyPower(user.getPower());
				builder.setMyWealth(user.getYuanbao());
				builder.setMyMedal(user.getMedal());
				builder.setMyAchievement(user.getAchievement());
			}
		} else {
			builder.setMyLevel(0);
		}
		return builder.build();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RankUser [basicUser=");
		if ( basicUser == null ) {
			builder.append("null");
		} else {
			builder.append(basicUser.getRoleName());
		}
		builder.append(", rank=");
		builder.append(rank);
		builder.append(", score=");
		builder.append(score);
		builder.append(", rankChange=");
		builder.append(rankChange);
		builder.append("]");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((basicUser == null) ? 0 : basicUser.hashCode());
		result = prime * result + rank;
		result = prime * result + rankChange;
		result = prime * result + score;
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
		RankUser other = (RankUser) obj;
		if (basicUser == null) {
			if (other.basicUser != null)
				return false;
		} else if (!basicUser.equals(other.basicUser))
			return false;
		if (rank != other.rank)
			return false;
		if (rankChange != other.rankChange)
			return false;
		if (score != other.score)
			return false;
		return true;
	}

	@Override
	public int compareTo(Object o) {
		if ( o instanceof RankUser ) {
			RankUser other = (RankUser)o;
			if ( this.rank != other.rank ) {
				return this.rank - other.rank;
			} else {
				if ( this.score != other.score ) {
					return other.score - this.score;
				} else {
					if ( this.rankChange != other.rankChange ) {
						return other.rankChange - this.rankChange;
					} else {
						if ( this.basicUser != null 
									&& this.basicUser.getUsername() != null 
									&& other.basicUser != null ) {
							return this.basicUser.getUsername().compareTo(
								other.basicUser.getUsername());
						} else {
							return 0;
						}
					}
				}
			}
		}
		return -1;
	}
	
}
