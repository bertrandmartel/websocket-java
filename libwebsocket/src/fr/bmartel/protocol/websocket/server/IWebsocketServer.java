package fr.bmartel.protocol.websocket.server;

import fr.bmartel.protocol.websocket.listeners.IClientEventListener;

/**
 * Template for websocket server interface featuring closing method and close client connection
 * 
 *
 * @author Bertrand Martel
 *
 */
public interface IWebsocketServer {

	/**
	 * close websocket server
	 */
	public void closeServer();
	
	/**
	 * Add Client event listener to server list for library user to be notified of all server events 
	 * 
	 * @param listener
	 */
	public void addServerEventListener(IClientEventListener listener);
	
}
