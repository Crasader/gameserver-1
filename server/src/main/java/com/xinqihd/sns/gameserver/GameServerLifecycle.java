package com.xinqihd.sns.gameserver;

import com.xinqihd.sns.gameserver.bootstrap.ServerLifecycle;

/**
 * The bootstrap will call corresponding methods in different stages.
 * @author wangqi
 *
 */
public class GameServerLifecycle implements ServerLifecycle {

	@Override
	public void init() {
		//Prepare configuration
		GameContext context = GameContext.getInstance();
		context.initContext();
		//Warmup JIT
		Warmup.warmup();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		GameContext context = GameContext.getInstance();
		context.destroyContext();
	}

}
