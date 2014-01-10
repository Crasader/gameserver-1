package com.xinqihd.sns.gameserver.config;

public class Puzzle {
	
	private String question;
	private String answer;
	/**
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}
	/**
	 * @param question the question to set
	 */
	public void setQuestion(String question) {
		this.question = question;
	}
	/**
	 * @return the answer
	 */
	public String getAnswer() {
		return answer;
	}
	/**
	 * @param answer the answer to set
	 */
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Puzzle [question=");
		builder.append(question);
		builder.append(", answer=");
		builder.append(answer);
		builder.append("]");
		return builder.toString();
	}

}
