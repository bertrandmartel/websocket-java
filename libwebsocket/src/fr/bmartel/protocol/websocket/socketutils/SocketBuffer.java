package fr.bmartel.protocol.websocket.socketutils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Socket bufferized functions
 * 
 * @author Bertrand Martel
 * 
 */
public class SocketBuffer {

	/**
	 * Separate body content of http request into blocks of BLOCK_SIZE to match
	 * buffer size of JVM
	 * 
	 * @param arg
	 *            body content to send to the socket
	 * @param out
	 *            printwriter to write to the socket
	 */
	public static void separateBlock(String arg, PrintWriter out) {
		int numberOfBlockToWrite = arg.length()
				% DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
		// 4096 = 2.6
		int numberOfBlock = 0;
		if (numberOfBlockToWrite == 0) {
			numberOfBlock = arg.length()
					/ DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
		} else {
			numberOfBlock = (arg.length() / DataBufferConst.DATA_BLOCK_SIZE_LIMIT) + 1;
		}
		int blockInit = 0;
		int blockEnd = 0;
		if (numberOfBlock == 1) {
			blockEnd = arg.length();
		} else {
			blockEnd = DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
		}
		for (int i = 0; i < numberOfBlock; i++) {

			out.print(arg.substring(blockInit, blockEnd));
			if (i == (numberOfBlock - 2) && numberOfBlockToWrite != 0) {
				blockInit = blockInit + DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
				blockEnd = blockEnd + numberOfBlockToWrite;
			} else {
				blockInit = blockInit + DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
				blockEnd = blockEnd + DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
			}
		}
		out.flush();
	}

	/**
	 * Separate body content of http request into blocks of BLOCK_SIZE to match
	 * buffer size of JVM
	 * 
	 * @param arg
	 *            body content to send to the socket
	 * @param out
	 *            printwriter to write to the socket
	 * @throws IOException
	 */
	public void separateBlock(byte[] arg, OutputStream out) throws IOException {
		int numberOfBlockToWrite = arg.length
				% DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
		// 4096 = 2.6
		int numberOfBlock = 0;
		if (numberOfBlockToWrite == 0) {
			numberOfBlock = arg.length / DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
		} else {
			numberOfBlock = (arg.length / DataBufferConst.DATA_BLOCK_SIZE_LIMIT) + 1;
		}
		int blockInit = 0;
		int blockEnd = 0;
		if (numberOfBlock == 1) {
			blockEnd = arg.length;
		} else {
			blockEnd = DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
		}
		for (int i = 0; i < numberOfBlock; i++) {
			byte[] arrayToWrite = new byte[blockEnd - blockInit];
			for (int j = blockInit; j < blockEnd; j++) {
				arrayToWrite[j - blockInit] = arg[j];
			}
			synchronized (out) {
				out.write(arrayToWrite);
			}

			if (i == (numberOfBlock - 2) && numberOfBlockToWrite != 0) {
				blockInit = blockInit + DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
				blockEnd = blockEnd + numberOfBlockToWrite;
			} else {
				blockInit = blockInit + DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
				blockEnd = blockEnd + DataBufferConst.DATA_BLOCK_SIZE_LIMIT;
			}
		}
		synchronized (out) {
			out.flush();
		}
	}
}
