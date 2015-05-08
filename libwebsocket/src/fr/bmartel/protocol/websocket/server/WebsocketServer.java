package fr.bmartel.protocol.websocket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import fr.bmartel.protocol.websocket.listeners.IClientEventListener;

/**
 * Server socket
 * 
 * @author Bertrand Martel
 */
public class WebsocketServer implements IWebsocketServer,IClientEventListener{

	/** boolean loop control for server instance running */
	private volatile boolean running = true;

	/** define which port we use for connection */
	private int port = 8080;

	/** define server socket object */
	private static ServerSocket serverSocket;

	private ArrayList<IClientEventListener> serverEventListenerList = new ArrayList<IClientEventListener>();
	
	/**
	 * Initialize server
	 */
	public WebsocketServer(int port) {
		this.port=port;
	}

	/**	
	 * main loop for web server running
	 */
	public void start() {
		try {
			/* server will be running while running == true */
			running = true;
			System.out.println("Launching Websocket server on port " + this.port);
			
			WebsocketServer.serverSocket = new ServerSocket(this.port);
			
			/*
			 * server thread main loop : accept a new connect each time
			 * requested by correct client
			 */
			while (running) {
				Socket newSocketConnection = serverSocket.accept();

				newSocketConnection.setKeepAlive(true);
				ServerSocketChannel server = new ServerSocketChannel(
						newSocketConnection,this);
				Thread newSocket = new Thread(server);
				newSocket.start();
			}
			/* close server socket safely */
			serverSocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
			/* stop all thread and server socket */
			stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stop server socket and stop running thread
	 */
	private void stop() {
		/* close socket connection */
		closeServerSocket();
		/* disable loop */
		running = false;
		System.out.println("Stopping server socket");
	}

	/** Stop server socket */
	private void closeServerSocket() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void closeServer() {
		stop();
	}
	
	/**
	 * remove client from list
	 */
	@Override
	public void onClientClose(IWebsocketClient client) {
		for (int i = 0; i < serverEventListenerList.size();i++)
		{
			serverEventListenerList.get(i).onClientClose(client);
		}
	}

	@Override
	public void onClientConnection(IWebsocketClient client) {
		for (int i = 0; i < serverEventListenerList.size();i++)
		{
			serverEventListenerList.get(i).onClientConnection(client);
		}
	}

	@Override
	public void onMessageReceivedFromClient(IWebsocketClient client,
			String message) {
		for (int i = 0; i < serverEventListenerList.size();i++)
		{
			serverEventListenerList.get(i).onMessageReceivedFromClient(client,message);
		}
	}

	@Override
	public void addServerEventListener(IClientEventListener listener) {
		serverEventListenerList.add(listener);
	}
}
