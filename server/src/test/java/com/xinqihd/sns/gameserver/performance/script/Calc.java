package com.xinqihd.sns.gameserver.performance.script;

class Calc implements Function {

	@Override
	public double calc(double x) {
		return Math.sin(Math.sqrt(Math.pow(x, 3) * Math.random()*100));
	}
}
