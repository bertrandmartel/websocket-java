package fr.bmartel.protocol.websocket.constants;

/**
 * Define all Websocket header used in websocket handshake protocol
 * 
 * @author Bertrand Martel
 * 
 */
public class WebSocketHeader {

	/**
	 * Accept header for websocket protocol
	 */
	public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";

	/**
	 * Websocket key for websocket protocol handshake
	 */
	public static final String SC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
}
