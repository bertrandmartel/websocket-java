package fr.bmartel.protocol.websocket.listeners;

import fr.bmartel.protocol.websocket.server.IWebsocketClient;

/**
 * Close listener for websocket server to know exactly when a client will log off 
 * 
 * @author Bertrand Martel
 *
 */
public interface IClientEventListener {

	/**
	 * called when a websocket client connection closes
	 * 
	 * @param clientRef
	 */
	public void onClientClose(IWebsocketClient client);
	
	/**
	 * called when a websocket client has successfully connected to server
	 * 
	 * @param client
	 */
	public void onClientConnection(IWebsocketClient client);
	
	/**
	 * called when a message has been received from client
	 * 
	 * @param client
	 * 		client object
	 * @param message
	 * 		message delivered
	 */
	public void onMessageReceivedFromClient(IWebsocketClient client,String message);
	
}
