package fr.bmartel.protocol.websocket.client;


public interface IWebsocketClientChannel {

	/**
	 * close client socket
	 */
	public void closeSocket();

	/**
	 * Write data to client socket outputstream
	 * 
	 * @param data
	 *            data to write
	 */
	public int writeMessage(String message);

	/**
	 * connect to client socket
	 * 
	 * @return socket created
	 */
	public void connect();

	/**
	 * to know if socket is connected or not
	 * 
	 * @return
	 */
	public boolean isConnected();

	/**
	 * Add client event listener to list
	 * 
	 * @param eventListener
	 */
	public void addClientSocketEventListener(
			IWebsocketClientEventListener eventListener);

	/**
	 * clean event listener list
	 */
	public void cleanEventListeners();

	/**
	 * close socket and wait for socket reading thread to die
	 */
	public void closeSocketJoinRead();

	/**
	 * Set timeout for this socket
	 * 
	 * @param socketTimeout
	 */
	public void setSocketTimeout(int socketTimeout);
}
