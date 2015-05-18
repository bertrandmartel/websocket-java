/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Bertrand Martel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.protocol.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import fr.bmartel.protocol.websocket.constants.WebSocketOpcode;
import fr.bmartel.protocol.websocket.constants.WebSocketProtocol;
import fr.bmartel.protocol.websocket.constants.WebSocketStates;
import fr.bmartel.protocol.websocket.socketutils.SocketBuffer;

/**
 * Encapsulation and Decapsulation of data according to websocket protocol
 * 
 * @author Bertrand Martel
 * 
 */
public class WebSocketChannel {

	private boolean DEBUG = false;

	private SocketBuffer socketBuffer = null;

	public WebSocketChannel() {
		this.socketBuffer = new SocketBuffer();
	}

	/**
	 * Decapsulate a websocket message from a basic inputstream
	 * 
	 * @param in
	 *            inputstream
	 * @return string data to be read
	 * @throws IOException
	 */
	public byte[] decapsulateMessage(InputStream in) throws IOException,
			SocketException {
		/* initialize state machine */
		int currentState = WebSocketStates.NONE;
		int sizeCounter = 0;
		int maskKeyCounter = 0;
		int payloadLengthCounter = 0;

		/* initialize a brand new wedsocket message object */
		WebSocketMessage message = new WebSocketMessage();

		/* read one byte from inputstream (block if nothing to be read) */
		int currentByte;
		try {
			while ((currentByte = in.read()) != -1)
				switch (currentState) {
				case WebSocketStates.NONE:
					sizeCounter = 0;
					currentState = WebSocketStates.FIN_STATE;
				case WebSocketStates.FIN_STATE:
					message.setFIN(currentByte & WebSocketProtocol.FIN);
					currentState = WebSocketStates.RSV_STATE;
				case WebSocketStates.RSV_STATE:
					message.setRSV(currentByte & WebSocketProtocol.RSV);
					currentState = WebSocketStates.OPCODE_STATE;
				case WebSocketStates.OPCODE_STATE:
					message.setOPCODE(currentByte & WebSocketProtocol.OPCODE);
					message.setOpcodeType(WebSocketOpcode
							.getOpcode((byte) message.getOPCODE()));
					if (message.getOpcodeType().frameTypeValue == WebSocketOpcode.CONNECTION_CLOSE_FRAME.frameTypeValue) {
						return null;
					} else {
						if (DEBUG)
							System.out
									.println("Received websocket message with type : "
											+ message.getOpcodeType().frameTypeName);
					}
					currentState = WebSocketStates.MASK_STATE;
					break;
				case WebSocketStates.MASK_STATE:
					message.setMASK(currentByte & WebSocketProtocol.MASK);
					currentState = WebSocketStates.PAYLOAD_LENGTH;
				case WebSocketStates.PAYLOAD_LENGTH:
					message.setPAYLOAD_LENGTH_FRAME(currentByte
							& WebSocketProtocol.PAYLOAD_LENGTH);
					if (message.getPAYLOAD_LENGTH_FRAME() < WebSocketProtocol.PAYLOAD_SIZE_LIMIT1) {
						message.setPayload_length(message
								.getPAYLOAD_LENGTH_FRAME());
						if (message.getMASK() == 0x01) {
							currentState = WebSocketStates.MASKING_KEY;
							message.maskKey = new byte[WebSocketProtocol.MASK_KEY_SIZE];
						} else {
							message.payloadData = new byte[message
									.getPayload_length()];
							currentState = WebSocketStates.PAYLOAD_DATA;
						}
					} else if (message.getPAYLOAD_LENGTH_FRAME() == WebSocketProtocol.PAYLOAD_SIZE_LIMIT1) {
						message.payloadLength = new byte[WebSocketProtocol.PAYLOAD_SIZE_2BYTES];
						currentState = WebSocketStates.PAYLOAD_LENGTH_2_BYTES;
					} else {
						message.payloadLength = new byte[WebSocketProtocol.PAYLOAD_SIZE_8BYTES];
						currentState = WebSocketStates.PAYLOAD_LENGTH_8_BYTES;
					}
					break;
				case WebSocketStates.PAYLOAD_LENGTH_2_BYTES:
					message.payloadLength[sizeCounter] = (byte) currentByte;
					sizeCounter++;
					if (sizeCounter == WebSocketProtocol.PAYLOAD_SIZE_2BYTES) {
						message.setPayload_length(message.payloadLength);
						if (message.getMASK() == 0x01) {
							currentState = WebSocketStates.MASKING_KEY;
							message.maskKey = new byte[WebSocketProtocol.MASK_KEY_SIZE];
						} else {
							message.payloadData = new byte[message
									.getPayload_length()];
							currentState = WebSocketStates.PAYLOAD_DATA;
						}
					}
					break;
				case WebSocketStates.PAYLOAD_LENGTH_8_BYTES:
					message.payloadLength[sizeCounter] = (byte) currentByte;
					sizeCounter++;
					if (sizeCounter == WebSocketProtocol.PAYLOAD_SIZE_8BYTES) {
						message.setPayload_length(message.payloadLength);
						if (message.getMASK() == 0x01) {
							currentState = WebSocketStates.MASKING_KEY;
							message.maskKey = new byte[WebSocketProtocol.MASK_KEY_SIZE];
						} else {
							message.payloadData = new byte[message
									.getPayload_length()];
							currentState = WebSocketStates.PAYLOAD_DATA;
						}
					}
					break;
				case WebSocketStates.MASKING_KEY:
					message.maskKey[maskKeyCounter] = (byte) currentByte;
					maskKeyCounter++;
					if (maskKeyCounter == WebSocketProtocol.MASK_KEY_SIZE) {
						message.payloadData = new byte[message
								.getPayload_length()];
						currentState = WebSocketStates.PAYLOAD_DATA;
					}
					break;
				case WebSocketStates.PAYLOAD_DATA:
					message.payloadData[payloadLengthCounter] = (byte) currentByte;
					payloadLengthCounter++;
					if (payloadLengthCounter == message.getPayload_length()) {
						currentState = WebSocketStates.FINISHED_LOADING;
					} else {
						break;
					}
				case WebSocketStates.FINISHED_LOADING:
					byte[] data = new byte[] {};
					if (message.getMASK() == 0x01) {
						data = unmask(message.payloadData, message.maskKey);
					} else {
						data = message.payloadData;
					}
					return data;
				}
		} catch (SocketException e) {

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Unmask data payload according to algorithm described in websocket
	 * protocol rfc <br/>
	 * <br/>
	 * Octet i of the transformed data ("transformed-octet-i") is the XOR of
	 * octet i of the original data ("original-octet-i") with octet at index i
	 * modulo 4 of the masking key ("masking-key-octet-j"):
	 * 
	 * j = i MOD 4 transformed-octet-i = original-octet-i XOR
	 * masking-key-octet-j
	 * 
	 * @param maskedData
	 *            data masked (from client)
	 * @param mask
	 *            mask id byte array
	 * @return unmasked data payload
	 */
	private byte[] unmask(byte[] maskedData, byte[] mask) {
		byte[] unmaskedData = new byte[maskedData.length];
		for (int i = 0; i < maskedData.length; i++) {
			unmaskedData[i] = (byte) ((maskedData[i] & 0xFF) ^ (mask[i % 4] & 0xFF) & 0xFF);
		}
		return unmaskedData;
	}

	/**
	 * Send a message according to websocket protocol
	 * 
	 * @param message
	 *            message string to be sent
	 * @param out
	 *            outputstream to which it will be sent
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void encapsulateMessage(String message, OutputStream out)
			throws IOException, InterruptedException, SocketException {

		WebSocketMessage websocketMessage = new WebSocketMessage(1, 0,
				WebSocketOpcode.TEXT_FRAME.frameTypeValue, 0, null,
				message.getBytes("UTF-8"));

		if (DEBUG) {
			System.out.println("Message sent to websocket : " + message);
		}
		this.socketBuffer.separateBlock(websocketMessage.buildMessage(), out);
	}
}
