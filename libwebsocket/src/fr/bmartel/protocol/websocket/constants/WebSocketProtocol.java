package fr.bmartel.protocol.websocket.constants;

/**
 * Websocket protocol constants used in encapsulation and decapsulation builder
 * 
 * @author Bertrand Martel
 * 
 */
public class WebSocketProtocol {

	/** FIN_FRAME mask */
	public final static byte FIN = (byte) 0b10000000;

	/** RSV_FRAME mask */
	public final static byte RSV = (byte) 0b01110000;

	/** OPCODE_FRAME mask */
	public final static byte OPCODE = (byte) 0b00001111;

	/** MASK_FRAME mask */
	public final static byte MASK = (byte) 0b10000000;

	/** PAYLOAD_LENGTH mask */
	public final static byte PAYLOAD_LENGTH = (byte) 0b01111111;

	/** static size for 2 byte data payload */
	public final static int PAYLOAD_SIZE_2BYTES = 2;

	/** static size for 8 bytes data payload */
	public final static int PAYLOAD_SIZE_8BYTES = 8;

	/** mask key size is always 4 (only when mask is enabled) */
	public final static int MASK_KEY_SIZE = 4;

	/**
	 * if payload size < payload_size_limit1 payload_length_frame is actual
	 * payload length value || if payload size > payload_size_limit1 payload
	 * length value is following two byte after PAYLOAD_LENGTH_FRAME
	 */
	public final static int PAYLOAD_SIZE_LIMIT1 = 126;

	/**
	 * if payload size is PAYLOAD_SIZE_LIMIT2 following 8 bytes are actual
	 * paylad length value
	 */
	public final static int PAYLOAD_SIZE_LIMIT2 = 127;
}
