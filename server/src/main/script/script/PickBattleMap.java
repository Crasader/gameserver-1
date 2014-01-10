package script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleBitSetMap;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * Pickup a random map for battle.
 * 
 * @author wangqi
 *
 */
public class PickBattleMap {
	
	private static final String[] INIT_MAPS = new String[]{"13", "3"};
	
	private static final Random R = new Random();

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		//We can assign a map 
		/*
		for ( int i=0; i<parameters.length; i++ ) {
			BattleBitSetMap battleMap = (BattleBitSetMap)parameters[i];
			if ( "6".equals(battleMap.getMapId()) ) {
				index = i;
				break;
			}
		}
		*/
		Battle battle = (Battle)parameters[0];
		Object[] array = (Object[])parameters[1];
		
		String mapId = null;
		
		/**
		 * Find the real user and the min level.
		 */
		User realUser = null;
		int minLevel = Integer.MAX_VALUE;
		Collection bUserCol = battle.getBattleUserMap().values();
		for (Iterator iterator = bUserCol.iterator(); iterator.hasNext();) {
			BattleUser bUser = (BattleUser) iterator.next();
			User tmpUser = bUser.getUser();
			if ( !tmpUser.isAI() && minLevel > bUser.getUser().getLevel() ) {
				minLevel = bUser.getUser().getLevel();
				realUser = tmpUser;
			}
		}
		/**
		 * 5级以下的训练场，绑定地图ID: 20
		 */
		if ( realUser.getLevel() < 5 ) {
			mapId = INIT_MAPS[(int)(MathUtil.nextDouble()*INIT_MAPS.length)];
		}
		
		BattleBitSetMap battleMap = null;
		if ( mapId == null ) {
			while ( battleMap == null ) {
				int index = R.nextInt(array.length);
				BattleBitSetMap tmpMap = (BattleBitSetMap)array[index];
				if ( !tmpMap.getMapPojo().isHidden() && tmpMap.getMapPojo().getReqlv() <= minLevel ) {
					battleMap = tmpMap; 
				}
			}
		} else {
			for (int i = 0; i < array.length; i++) {
				BattleBitSetMap map = (BattleBitSetMap)array[i];
				if ( map.getMapId().equals(mapId) ) {
					battleMap = map;
					break;
				}
			}
		}

		ArrayList list = new ArrayList();
		list.add(battleMap.clone());
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
