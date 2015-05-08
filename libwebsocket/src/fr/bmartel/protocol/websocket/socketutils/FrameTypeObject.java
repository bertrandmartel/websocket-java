package fr.bmartel.protocol.websocket.socketutils;

/**
 * Define a pattern for a frame associated with a sungle byte value
 * 
 * @author Bertrand Martel
 * 
 */
public class FrameTypeObject {

	/** frame type value */
	public byte frameTypeValue;

	/** frame type name */
	public String frameTypeName;

	/**
	 * Frame type builder with value and name
	 * 
	 * @param frameTypeValue
	 *            frame type value
	 * @param frameTypeName
	 *            frame type name
	 */
	public FrameTypeObject(byte frameTypeValue, String frameTypeName) {
		this.frameTypeName = frameTypeName;
		this.frameTypeValue = frameTypeValue;
	}

	/**
	 * frame string
	 */
	public String toString() {
		return frameTypeName;
	}
}
