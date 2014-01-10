package script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.db.mongo.ServerListManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.ServerRoleList;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 在用户账号登陆后，处理合并服务器的事项.
 * 如果一组服务器需要合并：
 * 1) 
 * 
 * @author wangqi
 *
 */
public class MergeServer {
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		Account account = (Account)parameters[0];

		ArrayList serverRoleList = account.getServerRoles();
		HashMap map = new HashMap();
		for (Iterator iter = serverRoleList.iterator(); iter.hasNext();) {
			ServerRoleList roleList = (ServerRoleList) iter.next();
			map.put(roleList.getServerId(), roleList);
		}
		ArrayList addedList = new ArrayList();
		ArrayList delList = new ArrayList();
		for (Iterator iter = serverRoleList.iterator(); iter.hasNext();) {
			ServerRoleList roleList = (ServerRoleList) iter.next();
			String serverId = roleList.getServerId();
			ServerPojo serverPojo = ServerListManager.getInstance().getServerById(serverId);
			if ( serverPojo != null ) {
				if ( serverPojo.getMergeId() != null ) {
					String targetMergedId = serverPojo.getMergeId();
					ServerPojo targetServer = ServerListManager.getInstance().getServerById(targetMergedId);
					if ( targetServer != null ) {
						//该组服下的所有用户昵称和角色需要移动至指定的其他服中
						ServerRoleList targetRoleList = (ServerRoleList)map.get(targetMergedId);
						if ( targetRoleList == null ) {
							//新注册的玩家没有老服的账号
							targetRoleList = new ServerRoleList();
							targetRoleList.setServerId(targetMergedId);
							addedList.add(targetRoleList);
							map.put(targetMergedId, targetRoleList);
						}
						targetRoleList.getUserIds().addAll(roleList.getUserIds());
						targetRoleList.getRoleNames().addAll(roleList.getRoleNames());
						delList.add(roleList);
					}
				}
			}
		}
		if ( addedList.size() > 0 ) {
			serverRoleList.addAll(addedList);
		}
		if ( delList.size() > 0 ) {
			serverRoleList.removeAll(delList);
		}
		
		ArrayList list = new ArrayList();
		list.add(account);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
