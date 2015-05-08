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

import fr.bmartel.protocol.websocket.listeners.IClientEventListener;
import fr.bmartel.protocol.websocket.server.IWebsocketClient;
import fr.bmartel.protocol.websocket.server.WebsocketServer;

/**
* @mainpage  Websocket Server / Client implementation in Java
* 
* 
* 
*/
/**
 * Launch a websocket Server on localhost port 4242
 * 
 * @author Bertrand Martel
 *
 */
public class LaunchServer {

	private static int WEBSOCKET_PORT = 4242;

	public static void main(String[] args) {
		
		if (args.length > 0) {
			// see if arg[0] is int if not choose port 4242
			if (isInteger(args[0])) {
				WEBSOCKET_PORT=Integer.parseInt(args[0]);
			}
		}

		WebsocketServer server = new WebsocketServer(WEBSOCKET_PORT); // intiate websocket server

		server.addServerEventListener(new IClientEventListener() {

			@Override
			public void onMessageReceivedFromClient(IWebsocketClient client,
					String message) {
				// all your message received from websocket client will be here
				System.out.println("message received in main : " + message);
			}

			@Override
			public void onClientConnection(IWebsocketClient client) {
				// when a websocket client connect. This will be called (you can
				// store client object)
				System.out.println("Websocket client has connected in main");

				client.sendMessage("Hello,I'm a websocket server");

				// client.close();
			}

			@Override
			public void onClientClose(IWebsocketClient client) {
				// when a websocket client connection close. This will be called
				// (you can dismiss client object)
				System.out.println("Websocket client has disconnected in main");
			}
		});

		server.start(); // start Websocket server => this method will block

		// server.closeServer(); //close websocket server
	}

	public static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c <= '/' || c >= ':') {
				return false;
			}
		}
		return true;
	}
}
