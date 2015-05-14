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
package fr.bmartel.protocol.websocket.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;

import fr.bmartel.protocol.http.impl.HttpConstants;
import fr.bmartel.protocol.http.impl.HttpHeader;
import fr.bmartel.protocol.http.impl.HttpRequestFrame;
import fr.bmartel.protocol.http.impl.StatusCodeList;
import fr.bmartel.protocol.websocket.WebSocketChannel;
import fr.bmartel.protocol.websocket.WebSocketHandshake;
import fr.bmartel.protocol.websocket.listeners.IClientEventListener;

/**
 * <b>Server socket connection management</b>
 * 
 * @author Bertrand Martel
 */
public class ServerSocketChannel implements Runnable, IWebsocketClient {
	
	/** websocket decoder / encoder object */
	private WebSocketChannel websocketChannel = null;

	/** socket to be used by server */
	private Socket socket;

	/** inputstream to be used for reading */
	private InputStream inputStream;

	/** outputstream to be used for writing */
	private OutputStream outputStream;

	/** http request parser */
	private HttpRequestFrame httpFrameParser;

	private IClientEventListener clientListener = null;

	/**
	 * Initialize socket connection when the connection is available ( socket
	 * parameter wil block until it is opened)
	 * 
	 * @param socket
	 *            the socket opened
	 * @param context
	 *            the current OSGI context
	 */
	public ServerSocketChannel(Socket socket,
			IClientEventListener clientListener) {
		try {
			this.clientListener = clientListener;

			/* give the socket opened to the main class */
			this.socket = socket;
			/* extract the associated input stream */
			this.inputStream = socket.getInputStream();
			/* extract the associated output stream */
			this.outputStream = socket.getOutputStream();

			/*
			 * initialize parsing method for request string and different body
			 * of http request
			 */
			this.httpFrameParser = new HttpRequestFrame();
			/*
			 * intialize response manager for writing data to outputstream
			 * method (headers generation ...)
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write byte array to socket outputstream
	 * 
	 * @param bytes
	 * @throws IOException
	 */
	private void writeToSocket(byte[] bytes) throws IOException {
		synchronized (this.socket.getOutputStream()) {
			this.socket.getOutputStream().write(bytes);
			this.socket.getOutputStream().flush();
		}
	}

	/**
	 * Define if websocket connection has been enables by client and server
	 */
	private boolean websocket = false;

	/**
	 * Main socket thread : parse all datas passing through socket inputstream
	 */
	@Override
	public void run() {
		try {
			do {
				/* clear richRequest object (specially headers) */
				this.httpFrameParser = new HttpRequestFrame();

				/*
				 * define loop if websocket has been enables by client and
				 * server
				 */
				if (websocket == false) {

					int requestLine = this.httpFrameParser
							.parseHttp(inputStream);

					if (requestLine == 200) {

						/* retrieve uri */
						final String uri = this.httpFrameParser.getUri();
						
						/* check if Connection: Upgrade is present in header map */
						if (this.httpFrameParser.getHeaders().containsKey(HttpHeader.CONNECTION.toLowerCase())&& this.httpFrameParser.getHeaders().containsKey(HttpHeader.UPGRADE.toLowerCase())) {
							if (this.httpFrameParser.getHeaders()
									.get(HttpHeader.CONNECTION.toLowerCase())
									.toLowerCase().indexOf("upgrade") != -1
									&& this.httpFrameParser
											.getHeaders()
											.get(HttpHeader.UPGRADE
													.toLowerCase())
											.toLowerCase().indexOf("websocket") != -1) {

								upgradeWebsocketProtocol(this.outputStream,
										this.httpFrameParser, this);

								websocketChannel = new WebSocketChannel();
								notifyConnectionSuccess();
								this.websocket = true;
							}
						} else if (uri.startsWith("/")) {
							System.out.println("API dispatcher ");

						} else if (requestLine == 400) {
							writeToSocket(HttpConstants.BAD_REQUEST_ERROR
									.getBytes("UTF-8"));
							System.out.println("ERROR : => "
									+ StatusCodeList.BAD_REQUEST.code + " "
									+ StatusCodeList.BAD_REQUEST.reasonPhrase);
						} else {
							
							websocket=false;
							closeSocket();
							return;
						}
					}
				} else {

					/* read something on websocket stream */
					String messageRead = this.websocketChannel.decapsulateMessage(this.inputStream);
					
					if (clientListener != null && messageRead != null) {
						clientListener.onMessageReceivedFromClient(this,
								messageRead);
					}

					if (messageRead == null) {
						websocket=false;
						closeSocket();
						return;
					}
					
				}
			} while (websocket == true);
			closeSocket();
		} 
		catch (SocketException e)
		{
		}
		catch (Exception e) {
			e.printStackTrace();
			// TODO : redirect ?
		}
	}

	/**
	 * Switch to Websocket protocol from server socket
	 * 
	 * @param out
	 *            socket outputStream
	 * @param httpFrameParser
	 *            http parser
	 * @param serverThread
	 *            server thread
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void upgradeWebsocketProtocol(OutputStream out,
			HttpRequestFrame httpFrameParser, ServerSocketChannel serverThread)
			throws UnsupportedEncodingException, IOException {

		/* write websocket handshake to client */
		synchronized (out) {
			out.write(WebSocketHandshake.writeWebSocketHandShake(
					httpFrameParser).getBytes());
			out.flush();
		}
	}

	/**
	 * Notify websocket client connection success
	 */
	private void notifyConnectionSuccess() {
		if (clientListener != null) {
			clientListener.onClientConnection(this);
		}
	}

	/**
	 * Close socket inputstream
	 * 
	 * @throws IOException
	 */
	private void closeInputStream() throws IOException {
		this.inputStream.close();
	}

	/**
	 * Close socket inputstream
	 */
	private void closeOutputStream() throws IOException {
		this.outputStream.close();
	}

	private void closeSocket()
	{
		try {
			closeInputStream();
			closeOutputStream();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int close() {
		System.out.println("closing ....");
		
		websocket = false;
		closeSocket();
		
		if (clientListener != null) {
			clientListener.onClientClose(this);
		}
		
		return 0;
	}

	@Override
	public int sendMessage(String message) {
		if (outputStream!=null)
		{
			try {
				websocketChannel.encapsulateMessage(message, outputStream);
				return 0;
			} catch (IOException | InterruptedException e) {
				//e.printStackTrace();
			}
		}
		return -1;
	}
}