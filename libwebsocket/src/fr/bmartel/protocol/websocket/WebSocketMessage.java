package fr.bmartel.protocol.websocket;

import fr.bmartel.protocol.websocket.constants.WebSocketProtocol;
import fr.bmartel.protocol.websocket.socketutils.FrameTypeObject;
import fr.bmartel.protocol.websocket.utils.ByteUtils;


/**
 * Build a websocket message accroding to websocket framing protocol
 * 
 * @author Bertrand Martel
 * 
 */
public class WebSocketMessage {

	/**
	 * FIN frame (determine the final message)
	 */
	private int FIN_FRAME;

	/**
	 * MUST be 0 unless an extension is negotiated that defines meanings for
	 * non-zero values. If a nonzero value is received and none of the
	 * negotiated extensions defines the meaning of such a nonzero value, the
	 * receiving endpoint MUST _Fail the WebSocket Connection_.
	 */
	private int RSV_FRAME;

	/**
	 * Determine the meaning of data payload see different opcode type in
	 * Websocket opcode class
	 */
	private int OPCODE_FRAME;

	/**
	 * Determine if data payload is being masked (1) or not (0)
	 */
	private int MASK_FRAME;

	/**
	 * payload length frame value (<126 ; ==126 ; ==127)
	 */
	private int PAYLOAD_LENGTH_FRAME;

	/**
	 * payload length actual value (in number of octet)
	 */
	private int payloadLengthValue;

	/**
	 * mask key for masking data payload (only if MASK_FRAME == 0x01)
	 */
	public byte[] maskKey;

	/**
	 * payload data
	 */
	public byte[] payloadData;

	/** byte array for payload size on 2 or 8 bytes */
	public byte[] payloadLength;

	/**
	 * opcode type object (See opcode class for full list of opcode)
	 */
	private FrameTypeObject opcodeType;

	/**
	 * Default constructor for websocket message object
	 */
	public WebSocketMessage() {
	}

	/**
	 * Websocket builder for building a new websocket message
	 * 
	 * @param FIN_FRAME
	 *            fin frame value
	 * @param RSV_FRAME
	 *            rsv frame value
	 * @param OPCODE_FRAME
	 *            opcode frame value
	 * @param MASK_FRAME
	 *            mask frame value
	 * @param maskKey
	 *            maskey value (can be null)
	 * @param payloadData
	 *            payload data
	 */
	public WebSocketMessage(int FIN_FRAME, int RSV_FRAME, int OPCODE_FRAME,
			int MASK_FRAME, byte[] maskKey, byte[] payloadData) {
		this.FIN_FRAME = FIN_FRAME;
		this.RSV_FRAME = RSV_FRAME;
		this.OPCODE_FRAME = OPCODE_FRAME;
		this.MASK_FRAME = MASK_FRAME;
		if (maskKey != null) {
			this.maskKey = maskKey;
		}
		this.payloadData = payloadData;
	}

	/**
	 * Build a message according to all information gathered in websocket
	 * message object
	 * 
	 * @return byte array to be sent to outputstream
	 * */
	public byte[] buildMessage() {
		int mask_size = 0;
		int lengthIndicator = 0;
		byte[] lengthSize;

		if (this.MASK_FRAME == 0x01) {
			mask_size = WebSocketProtocol.MASK_KEY_SIZE;
		}

		/* offset for number of bytes defining length */

		/* FFFF = 65536 => 8 bytes for payload data length */
		if (this.payloadData.length > 65535) {

			byte[] array = new byte[8];
			lengthIndicator = WebSocketProtocol.PAYLOAD_SIZE_LIMIT2;
			int left = this.payloadData.length;
			int unit = 256;
			for (int i = 8; i > 0; i--) {
				array[i - 1] = (byte) (left % unit);
				left = left / unit;

				if (left == 0)
					break;
			}
			lengthSize = new byte[] { array[0], array[1], array[2], array[3],
					array[4], array[5], array[6], array[7] };
		} else if (this.payloadData.length > 125) {
			/* 01111111 == 127 max */
			byte[] array = ByteUtils
					.convertIntToByte2Array(this.payloadData.length);
			lengthIndicator = WebSocketProtocol.PAYLOAD_SIZE_LIMIT1;
			lengthSize = new byte[] { array[0], array[1] };
		} else {
			lengthIndicator = this.payloadData.length;
			lengthSize = new byte[] {};
		}
		byte[] message = new byte[1 + 1 + lengthSize.length + mask_size
				+ this.payloadData.length];
		message[0] = (byte) ((this.FIN_FRAME << 7) + (this.RSV_FRAME << 4) + (this.OPCODE_FRAME));
		message[1] = (byte) ((this.MASK_FRAME << 7) + lengthIndicator);
		for (int i = 0; i < lengthSize.length; i++) {
			message[i + 2] = lengthSize[i];
		}
		if (mask_size != 0) {
			message[2 + lengthSize.length] = this.maskKey[0];
			message[2 + lengthSize.length + 1] = this.maskKey[1];
			message[2 + lengthSize.length + 2] = this.maskKey[2];
			message[2 + lengthSize.length + 3] = this.maskKey[3];
			System.arraycopy(this.payloadData, 0, message,
					2 + lengthSize.length + 4, this.payloadData.length);
		} else {
			System.arraycopy(this.payloadData, 0, message,
					2 + lengthSize.length, this.payloadData.length);
		}
		return message;
	}

	/**
	 * Display some information in console
	 */
	public String toString() {
		String ret = FIN_FRAME + " message(s) - " + opcodeType.toString()
				+ " - ";
		if (MASK_FRAME == 1) {
			ret += " PAYLOAD DATA IS MASKED";
		} else {
			ret += " PAYLOAD DATA IS NOT MASKED";
		}
		ret += " - PAYLOAD LENGTH : " + this.payloadLengthValue;
		return ret;
	}

	/**
	 * Getter for FIN_FRAME value
	 * 
	 * @return FIN_FRAME value
	 */
	public int getFIN() {
		return FIN_FRAME;
	}

	/**
	 * Set FIN_FRAME value
	 * 
	 * @param fIN
	 *            fin byte value
	 */
	public void setFIN(int fIN) {
		FIN_FRAME = (fIN >> 7);
	}

	/**
	 * Retrieve RSV byte array value
	 * 
	 * @return RSV_FRAME
	 */
	public int getRSV() {
		return RSV_FRAME;
	}

	/**
	 * Set RSV byte array value
	 * 
	 * @param rSV
	 *            RSV_FRAME
	 */
	public void setRSV(int rSV) {
		RSV_FRAME = (rSV >> 4);
	}

	/**
	 * Retrieve Opcode value
	 * 
	 * @return opcode frame value
	 */
	public int getOPCODE() {
		return OPCODE_FRAME;
	}

	/**
	 * Set opcode value ofr OPCODE_FRAME
	 * 
	 * @param oPCODE
	 *            opcode byte integer value
	 */
	public void setOPCODE(int oPCODE) {
		OPCODE_FRAME = oPCODE;
	}

	/**
	 * Opcode frame type
	 * 
	 * @return opcode type according to opcode type class
	 */
	public FrameTypeObject getOpcodeType() {
		return opcodeType;
	}

	public void setOpcodeType(FrameTypeObject opcodeType) {
		this.opcodeType = opcodeType;
	}

	/**
	 * Get mask value (1 for data payload masked and 0 for not being masked)
	 * 
	 * @return MASK_FRAME value
	 */
	public int getMASK() {
		return MASK_FRAME;
	}

	/**
	 * Set mask value
	 * 
	 * @param mASK
	 *            MASK8FRAME value
	 */
	public void setMASK(int mASK) {
		MASK_FRAME = (mASK >> 7);
	}

	/**
	 * Retrieve PAYLOAD_LENTH_FRAME value (actual byte being read but not the
	 * ACTUAL length of data payload (only if PAYLOAD_LENGTH_FRAME <126 cf
	 * protocol))
	 * 
	 * @return Data payload frame value
	 */
	public int getPAYLOAD_LENGTH_FRAME() {
		return PAYLOAD_LENGTH_FRAME;
	}

	/**
	 * Set value for data payload frame
	 * 
	 * @param PAYLOAD_LENGTH_FRAME
	 *            data payload length value
	 */
	public void setPAYLOAD_LENGTH_FRAME(int PAYLOAD_LENGTH_FRAME) {
		this.PAYLOAD_LENGTH_FRAME = PAYLOAD_LENGTH_FRAME;
	}

	/**
	 * Retrieve actual length of data payload (in number of octets)
	 * 
	 * @return number of octet of data payload
	 */
	public int getPayload_length() {
		return payloadLengthValue;
	}

	/**
	 * Set data payload length value with integer parameter
	 * 
	 * @param payload_length
	 *            payload length actual value
	 */
	public void setPayload_length(int payload_length) {
		this.payloadLengthValue = payload_length;
	}

	/**
	 * Set data payload length with a byte array as parameters
	 * 
	 * @param payload_length
	 *            data payload length byte array
	 */
	public void setPayload_length(byte[] payload_length) {
		int length = 0;
		for (int i = 0; i < payload_length.length; i++) {
			length += (payload_length[i] & 0xFF) << (8 * (payload_length.length
					- i - 1));
		}
		this.payloadLengthValue = length;
	}
}
