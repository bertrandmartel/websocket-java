package fr.bmartel.protocol.websocket.client;

/**
 * 
 * Websocket client event listener used to be notified of all websocket client's
 * events
 * 
 * @author Bertrand Martel
 *
 */
public interface IWebsocketClientEventListener {

	/**
	 * notify successfull connection of websocket
	 */
	public void onSocketConnected();

	/**
	 * notify closing of websocket
	 */
	public void onSocketClosed();

	/**
	 * incoming data has arrived from server
	 * 
	 * @param data
	 *            incoming data
	 * @param channel
	 *            websocket client channel object used generally to write back
	 *            to the server (or close the connection)
	 */
	public void onIncomingMessageReceived(byte[] data,
			IWebsocketClientChannel channel);
}
