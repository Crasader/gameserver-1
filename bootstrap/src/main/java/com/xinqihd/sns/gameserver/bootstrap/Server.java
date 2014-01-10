package com.xinqihd.sns.gameserver.bootstrap;

public interface Server {

	/**
	 * Reload the pipeline factory.
	 */
	public abstract void reload();

	/**
	 * Start the http server at given address and port.
	 * @param bindAddr
	 * @param port
	 */
	public abstract void startServer(String bindAddr, int port);

	/**
	 * Stop the SimpleHttpServer
	 */
	public abstract void stopServer();

}