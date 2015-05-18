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
package fr.bmartel.websocket;

import java.util.Scanner;

import fr.bmartel.protocol.websocket.client.IWebsocketClientChannel;
import fr.bmartel.protocol.websocket.client.IWebsocketClientEventListener;
import fr.bmartel.protocol.websocket.client.WebsocketClient;
import fr.bmartel.protocol.websocket.listeners.IClientEventListener;
import fr.bmartel.protocol.websocket.server.IWebsocketClient;
import fr.bmartel.protocol.websocket.server.WebsocketServer;

/**
 * Launch a websocket client connecting to localhost 127.0.0.1:8443 websocket
 * server
 * 
 * @author Bertrand Martel
 *
 */
public class LaunchClient {

	/**
	 * server hostname
	 */
	private final static String HOSTNAME = "127.0.0.1";

	/**
	 * server port
	 */
	private final static int PORT = 8443;

	private final static String KEYSTORE_DEFAULT_TYPE = "PKCS12";
	private final static String TRUSTORE_DEFAULT_TYPE = "JKS";
	
	private final static String CLIENT_KEYSTORE_FILE_PATH = "~/websocket-java/certs/client/client.p12";
	private final static String CLIENT_TRUSTORE_FILE_PATH = "~/websocket-java/certs/ca.jks";
	private final static String SERVER_KEYSTORE_FILE_PATH = "~/websocket-java/certs/server/server.p12";
	private final static String SERVER_TRUSTORE_FILE_PATH = "~/websocket-java/certs/ca.jks";
	
	private final static String SSL_PROTOCOL = "TLS";
	private final static String KEYSTORE_PASSWORD = "123456";
	private final static String TRUSTORE_PASSWORD = "123456";

	private static WebsocketServer serverTest = null;

	public static void main(String[] args) {

		// start testing server
		startTestServer(PORT);

		// new instance of client socket
		WebsocketClient clientSocket = new WebsocketClient(HOSTNAME, PORT);

		// set SSL encryption
		//clientSocket.setSsl(true);

		// set ssl parameters
		/*
		clientSocket.setSSLParams(KEYSTORE_DEFAULT_TYPE, TRUSTORE_DEFAULT_TYPE,
				CLIENT_KEYSTORE_FILE_PATH, CLIENT_TRUSTORE_FILE_PATH,
				SSL_PROTOCOL, KEYSTORE_PASSWORD, TRUSTORE_PASSWORD);
		*/
		
		// add a client event listener to be notified for incoming http frames
		clientSocket
				.addClientSocketEventListener(new IWebsocketClientEventListener() {

					@Override
					public void onSocketConnected() {
						System.out
								.println("[CLIENT] Websocket client successfully connected");
					}

					@Override
					public void onSocketClosed() {
						System.out
								.println("[CLIENT] Websocket client disconnected");
					}

					@Override
					public void onIncomingMessageReceived(byte[] data,
							IWebsocketClientChannel channel) {
						System.out
								.println("[CLIENT] Received message from server : "
										+ new String(data));
					}
				});

		clientSocket.connect();

		// you can choose here which command to send to the server
		Scanner scan = new Scanner(System.in);

		System.out.println("------------------------------------------------");
		System.out.println("Started Websocket chat with server " + HOSTNAME
				+ ":" + PORT);
		System.out.println("------------------------------------------------");
		System.out.println("List of chat command :");
		System.out.println("HELLO");
		System.out.println("GOODBYE");
		System.out.println("T_T");
		System.out.println("EXIT");
		System.out.println("------------------------------------------------");

		String command = "";

		while (!command.equals("EXIT")) {

			command = scan.nextLine();

			switch (command) {
			case "HELLO":
				clientSocket.writeMessage("HELLO");
				break;
			case "GOODBYE":
				clientSocket.writeMessage("GOODBYE");
				break;
			case "T_T":
				clientSocket.writeMessage("T_T");
				break;
			case "EXIT":
				break;
			default:
				System.out.println("Unknown command");
			}
		}

		System.out.println("Exiting Websocket chat ...");

		// socket will be closed and reading thread will die if it exists
		clientSocket.closeSocketJoinRead();

		// clean event listener list
		clientSocket.cleanEventListeners();

		if (serverTest != null)
			serverTest.closeServer();
	}

	public static void startTestServer(int port) {

		// initiate websocket server
		serverTest = new WebsocketServer(port);

		// set ssl encryption
		//serverTest.setSsl(true);

		// set ssl parameters
		/*
		serverTest.setSSLParams(KEYSTORE_DEFAULT_TYPE, TRUSTORE_DEFAULT_TYPE,
				SERVER_KEYSTORE_FILE_PATH, SERVER_TRUSTORE_FILE_PATH,
				SSL_PROTOCOL, KEYSTORE_PASSWORD, TRUSTORE_PASSWORD);
		*/
		
		serverTest.addServerEventListener(new IClientEventListener() {

			@Override
			public void onMessageReceivedFromClient(IWebsocketClient client,
					String message) {
				// all your message received from websocket client will be here
				switch (message) {
				case "HELLO":
					client.sendMessage("Hello from websocket server");
					break;
				case "GOODBYE":
					client.sendMessage("Goodbye, See you from websocket server");
					break;
				case "T_T":
					client.sendMessage("O_O from websocket server");
					break;
				}
			}

			@Override
			public void onClientConnection(IWebsocketClient client) {
				// when a websocket client connect. This will be called (you can
				// store client object)
				System.out
						.println("[SERVER] Websocket client has connected to server");

			}

			@Override
			public void onClientClose(IWebsocketClient client) {
				// when a websocket client connection close. This will be called
				// (you can dismiss client object)
				System.out
						.println("[SERVER] Websocket client has disconnected from server");
			}
		});

		// start server in another thread not to block socket client
		// interactions
		Thread serverThread = new Thread(new Runnable() {

			@Override
			public void run() {
				serverTest.start();
			}
		});
		serverThread.start();
	}
}
