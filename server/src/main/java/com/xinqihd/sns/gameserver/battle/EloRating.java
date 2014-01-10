package com.xinqihd.sns.gameserver.battle;

/**
 * 
 * @author wangqi
 *
 */
public class EloRating {

	/**
	 * 
	 * Updates the scores in the passed matchup.
	 * 
	 * The Matchup to update Whether User 1 was the winner (false if User 2 is the
	 * winner) The desired Diff The desired KFactor
	 * 
	 * @param matchup
	 * @param user1WonMatch
	 * @param diff
	 * @param kFactor
	 */
	public static void UpdateScores(Matchup matchup, boolean user1WonMatch,
			int diff, int kFactor) {

		double est1 = 1.0 / (1 + 10 ^ (matchup.User2Score - matchup.User1Score) / diff);
		double est2 = 1.0 / (1 + 10 ^ (matchup.User1Score - matchup.User2Score) / diff);

		int sc1 = 0;
		int sc2 = 0;

		if (user1WonMatch) {
			sc1 = 1;
		} else {
			sc2 = 1;
		}
		
		matchup.User1Score = (int) (Math.round(matchup.User1Score + kFactor
				* (sc1 - est1)));
		matchup.User2Score = (int) (Math.round(matchup.User2Score + kFactor
				* (sc2 - est2)));
	}

	/**
	 * Updates the scores in the match, using default Diff and KFactors (400, 100)
	 * The Matchup to update
	 * Whether User 1 was the winner (false if User 2 is the winner)
	 * 
	 * @param matchup
	 * @param user1WonMatch
	 */
	public static void UpdateScores(Matchup matchup, boolean user1WonMatch) {
		UpdateScores(matchup, user1WonMatch, 400, 10);
	}

	public class Matchup {

		public int User1Score;

		public int User2Score;

		/**
		 * @return the user1Score
		 */
		public int getUser1Score() {
			return User1Score;
		}

		/**
		 * @param user1Score
		 *          the user1Score to set
		 */
		public void setUser1Score(int user1Score) {
			User1Score = user1Score;
		}

		/**
		 * @return the user2Score
		 */
		public int getUser2Score() {
			return User2Score;
		}

		/**
		 * @param user2Score
		 *          the user2Score to set
		 */
		public void setUser2Score(int user2Score) {
			User2Score = user2Score;
		}
	}

}