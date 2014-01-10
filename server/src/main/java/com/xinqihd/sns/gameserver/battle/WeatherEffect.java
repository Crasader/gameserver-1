package com.xinqihd.sns.gameserver.battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Every battle will have different weather effects
 * @author wangqi
 *
 */
public enum WeatherEffect {

  /**
   * 0: sunny
   * 1: rainy
   * 2: snowy
   * 3: cloudy
   * 4: foggy 
   * 5: windy
   */
	sunny,
	rainy,
	snowy,
	cloudy,
	foggy,
	windy;
	
	private static final List<WeatherEffect> valueList = new ArrayList<WeatherEffect>();
	
	public static final List<WeatherEffect> getValueList() {
		if ( valueList.size() <= 0 ) {
			valueList.addAll(Arrays.asList(WeatherEffect.values()));
		}
		return valueList;
	}
}
