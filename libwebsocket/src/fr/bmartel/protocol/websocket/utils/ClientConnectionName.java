package fr.bmartel.protocol.websocket.utils;

/**
 * set a name for each websocket client connecting to server
 * 
 * @author Bertrand Martel
 *
 */
public class ClientConnectionName {

	/**
	 * default name for client connection
	 */
	private final static String DEFAULT_NAME ="Client";
	
	/**
	 * set connection name according to index
	 * 
	 * @param previousIndex
	 * @return
	 */
	public static String getConnectionName(int previousIndex)
	{
		return (DEFAULT_NAME + (previousIndex++));
	}
}
