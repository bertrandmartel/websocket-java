package fr.bmartel.protocol.websocket.constants;

import fr.bmartel.protocol.websocket.socketutils.FrameTypeObject;

/**
 * Websocket opcode type list
 * 
 * @author Bertrand Martel
 * 
 */
public class WebSocketOpcode {

	/** continuation frame type */
	public final static FrameTypeObject CONTINUATION_FRAME = new FrameTypeObject(
			(byte) 0x00, "CONTINUATION_FRAME");

	/** text frame type */
	public final static FrameTypeObject TEXT_FRAME = new FrameTypeObject(
			(byte) 0x01, "TEXT_FRAME");

	/** binary frame type */
	public final static FrameTypeObject BINARY_FRAME = new FrameTypeObject(
			(byte) 0x02, "BINARY_FRAME");

	/** connection close frame type */
	public final static FrameTypeObject CONNECTION_CLOSE_FRAME = new FrameTypeObject(
			(byte) 0x08, "CONNECTION_CLOSE_FRAME");

	/** ping frame type */
	public final static FrameTypeObject PING_FRAME = new FrameTypeObject(
			(byte) 0x09, "PING_FRAME");

	/** pong frame type */
	public final static FrameTypeObject PONG_FRAME = new FrameTypeObject(
			(byte) 0x0A, "PONG_FRAME");

	/** non control frame type (frame value is fictive => between 0x03 and 0x07) */
	public final static FrameTypeObject NON_CONTROL_FRAME = new FrameTypeObject(
			(byte) 0x00, "NON_CONTROL_FRAME");

	/** control frames type (frame value is fictive => between 0x0B and 0x0F */
	public final static FrameTypeObject CONTROL_FRAME = new FrameTypeObject(
			(byte) 0x00, "CONTROL_FRAME");

	/**
	 * return opcode object type according to opcode byte code
	 * 
	 * @param code
	 *            byte code we are looking for
	 * @return frame type object of opcode type
	 */
	public static FrameTypeObject getOpcode(byte code) {
		if (code == 0x00)
			return CONTINUATION_FRAME;
		else if (code == 0x01)
			return TEXT_FRAME;
		else if (code == 0x02)
			return BINARY_FRAME;
		else if (code == 0x08)
			return CONNECTION_CLOSE_FRAME;
		else if (code == 0x09)
			return PING_FRAME;
		else if (code == 0x0A)
			return PONG_FRAME;
		else if (code > 0x02 && code < 0x08)
			return NON_CONTROL_FRAME;
		else if (code > 0x0A)
			return CONTROL_FRAME;
		return null;
	}
}
