package fr.bmartel.protocol.websocket;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.HttpResponseFrame;
import fr.bmartel.protocol.http.HttpVersion;
import fr.bmartel.protocol.http.StatusCodeObject;
import fr.bmartel.protocol.http.constants.HttpHeader;
import fr.bmartel.protocol.http.constants.MediaType;
import fr.bmartel.protocol.http.constants.StatusCodeList;
import fr.bmartel.protocol.http.states.HttpStates;
import fr.bmartel.protocol.websocket.constants.WebSocketHeader;

/**
 * 
 * Websocket protocol parameters definition / Websocket handshake manager and
 * frame decoder(from browser)
 * 
 * @author Bertrand Martel
 * 
 */
public class WebSocketHandshake {

	/**
	 * Globally Unique Identifier (GUID, [RFC4122]) "258EAFA5-E914-47DA-
	 * 95CA-C5AB0DC85B11"
	 */
	public static String GLOBAL_UID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

	/**
	 * HTTP headers for web socket protocol handshake
	 * 
	 * @param key
	 *            Nonce that has been generated and that must be sent to browser
	 *            to prevent from middle-attack
	 * 
	 * @return http headers for handshake
	 */
	public static String buildWebsocketHandshakeResponse(String key) {
		HttpVersion version = new HttpVersion(1, 1);
		StatusCodeObject returnCode = StatusCodeList.SWITCHING_PROTOCOL;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON_CHARSET);
		headers.put(HttpHeader.UPGRADE, "websocket");
		headers.put(HttpHeader.CONNECTION, "Upgrade");
		headers.put(WebSocketHeader.SEC_WEBSOCKET_ACCEPT, key);
		HttpResponseFrame httpFrame = new HttpResponseFrame(returnCode,
				version, headers, new byte[] {});
		return httpFrame.toString();
	}

	/**
	 * HTTP headers for web socket protocol handshake request
	 * 
	 * @param key
	 *            Nonce that has been generated and that must be sent to browser
	 *            to prevent from middle-attack
	 * 
	 * @return http headers for handshake
	 */
	public static String buildWebsocketHandshakeRequest(String key) {
		HttpVersion version = new HttpVersion(1, 1);
		StatusCodeObject returnCode = StatusCodeList.SWITCHING_PROTOCOL;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON_CHARSET);
		headers.put(HttpHeader.UPGRADE, "websocket");
		headers.put(HttpHeader.CONNECTION, "Upgrade");
		headers.put(WebSocketHeader.SC_WEBSOCKET_KEY, key);

		HttpResponseFrame httpFrame = new HttpResponseFrame(returnCode,
				version, headers, new byte[] {});
		return httpFrame.toString();
	}

	/**
	 * Build a complete new handshake request for websocket protocol
	 * 
	 * @param httpFrameParser
	 *            http request content
	 * @return http handshake request to be sent to browser
	 * @throws UnsupportedEncodingException
	 */
	public static String writeWebSocketHandShake(HttpFrame httpFrameParser)
			throws UnsupportedEncodingException {

		/* build response according to Websocket protocol */
		String websocketKey = httpFrameParser.getHeaders().get(
				WebSocketHeader.SC_WEBSOCKET_KEY.toLowerCase());

		/* concat websocket key and global unique identifier */
		String keysConcat = websocketKey + "" + WebSocketHandshake.GLOBAL_UID;

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] dig1 = new byte[20];

		dig1 = md.digest(keysConcat.getBytes("iso-8859-1"));
		String websocketclientNonce = new String(Base64.encodeBase64(dig1),
				"UTF-8");

		/* send response for websocket handshake */
		return WebSocketHandshake
				.buildWebsocketHandshakeResponse(websocketclientNonce);
	}

	/**
	 * Find websocket accept key from websocket key
	 * 
	 * @param websocketKey
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String retrieveWebsocketAccept(String websocketKey)
			throws UnsupportedEncodingException {

		/* concat websocket key and global unique identifier */
		String keysConcat = websocketKey + "" + WebSocketHandshake.GLOBAL_UID;

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] dig1 = new byte[20];

		dig1 = md.digest(keysConcat.getBytes("iso-8859-1"));

		String websocketclientNonce = new String(Base64.encodeBase64(dig1),
				"UTF-8");

		return websocketclientNonce;
	}

	/**
	 * Determine if handshake response received from server is valid or not
	 * 
	 * @param frame
	 *            http frame received from server
	 * @param expectedKey
	 *            key we expect to find
	 * @return
	 */
	public static boolean isValidHandshakeResponse(HttpFrame frame,
			HttpStates httpStates, String expectedKey) {

		if (httpStates == HttpStates.HTTP_FRAME_OK
				&& frame.getHeaders().containsKey(
						HttpHeader.CONNECTION.toLowerCase())
				&& frame.getHeaders().containsKey(
						HttpHeader.UPGRADE.toLowerCase())) {

			if (frame.getHeaders().get(HttpHeader.CONNECTION.toLowerCase())
					.toLowerCase().indexOf("upgrade") != -1
					&& frame.getHeaders().get(HttpHeader.UPGRADE.toLowerCase())
							.toLowerCase().indexOf("websocket") != -1
					&& frame.getHeaders()
							.get(HttpHeader.WEB_SOCKET_ACCEPT.toLowerCase())
							.toLowerCase().equals(expectedKey.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

}
