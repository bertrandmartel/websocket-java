package fr.bmartel.protocol.websocket.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.states.HttpStates;
import fr.bmartel.protocol.websocket.WebSocketChannel;
import fr.bmartel.protocol.websocket.WebSocketHandshake;

public class WebsocketClient implements IWebsocketClientChannel {
	/**
	 * socket server hostname
	 */
	private String hostname = "";

	/**
	 * socket server port
	 */
	private int port = 0;

	/** set ssl encryption or not */
	private boolean ssl = false;

	private static String websocketResponseExpected = "";

	/**
	 * keystore certificate type
	 */
	private String keystoreDefaultType = "";

	/**
	 * trustore certificate type
	 */
	private String trustoreDefaultType = "";

	/**
	 * keystore file path
	 */
	private String keystoreFile = "";

	/**
	 * trustore file path
	 */
	private String trustoreFile = "";

	/**
	 * ssl protocol used
	 */
	private String sslProtocol = "";

	/**
	 * keystore file password
	 */
	private String keystorePassword = "";

	/**
	 * trustore file password
	 */
	private String trustorePassword = "";

	/**
	 * define socket timeout (-1 if no timeout defined)
	 */
	private int socketTimeout = -1;

	private WebSocketChannel websocketChannel = new WebSocketChannel();

	/**
	 * socket object
	 */
	private Socket socket = null;

	/** client event listener list */
	private ArrayList<IWebsocketClientEventListener> clientListenerList = new ArrayList<IWebsocketClientEventListener>();

	/**
	 * thread used to read http inputstream data
	 */
	private Thread readingThread = null;

	private volatile boolean websocket = false;

	/**
	 * Build Client socket
	 * 
	 * @param hostname
	 * @param port
	 */
	public WebsocketClient(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	/**
	 * Create and connect socket
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	public void connect() {

		// close socket before recreating it
		if (socket != null) {
			closeSocket();
		}
		try {

			if (ssl) {
				/* initial server keystore instance */
				KeyStore ks = KeyStore.getInstance(keystoreDefaultType);

				/* load keystore from file */
				ks.load(new FileInputStream(keystoreFile),
						keystorePassword.toCharArray());

				/*
				 * assign a new keystore containing all certificated to be
				 * trusted
				 */
				KeyStore tks = KeyStore.getInstance(trustoreDefaultType);

				/* load this keystore from file */
				tks.load(new FileInputStream(trustoreFile),
						trustorePassword.toCharArray());

				/* initialize key manager factory with chosen algorithm */
				KeyManagerFactory kmf = KeyManagerFactory
						.getInstance(KeyManagerFactory.getDefaultAlgorithm());

				/* initialize trust manager factory with chosen algorithm */
				TrustManagerFactory tmf;

				tmf = TrustManagerFactory.getInstance(TrustManagerFactory
						.getDefaultAlgorithm());

				/* initialize key manager factory with initial keystore */
				kmf.init(ks, keystorePassword.toCharArray());

				/*
				 * initialize trust manager factory with keystore containing
				 * certificates to be trusted
				 */
				tmf.init(tks);

				/* get SSL context chosen algorithm */
				SSLContext ctx = SSLContext.getInstance(sslProtocol);

				/*
				 * initialize SSL context with key manager and trust managers
				 */
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

				SSLSocketFactory sslserversocketfactory = ctx
						.getSocketFactory();

				/* create a SSL socket connection */
				socket = new Socket();
				socket = sslserversocketfactory.createSocket();

			} else {

				/* create a basic socket connection */
				socket = new Socket();
			}

			/* establish socket parameters */
			socket.setReuseAddress(true);

			socket.setKeepAlive(true);

			if (socketTimeout != -1) {
				socket.setSoTimeout(socketTimeout);
			}

			socket.connect(new InetSocketAddress(hostname, port));

			if (readingThread != null) {
				websocket = false;
				readingThread.join();
			}

			websocket = false;
			readingThread = new Thread(new Runnable() {

				@Override
				public void run() {
					do {
						if (!websocket) {
							try {
								HttpFrame frame = new HttpFrame();

								HttpStates httpStates = frame.parseHttp(socket
										.getInputStream());

								// check handshake response from websocket
								// server
								if (WebSocketHandshake
										.isValidHandshakeResponse(frame,
												httpStates,
												websocketResponseExpected)) {

									websocket = true;

									for (int i = 0; i < clientListenerList
											.size(); i++) {
										clientListenerList.get(i)
												.onSocketConnected();
									}
								} else {
									websocket = false;
									closeSocket();
								}
							} catch (SocketException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							// here we can read data coming from websocket
							// server

							try {
								/* read something on websocket stream */
								byte[] data = websocketChannel
										.decapsulateMessage(socket
												.getInputStream());

								if (data == null) {
									closeSocket();
									websocket = false;
								} else {
									// incoming data message received
									for (int i = 0; i < clientListenerList
											.size(); i++) {
										clientListenerList.get(i)
												.onIncomingMessageReceived(
														data,
														WebsocketClient.this);
									}
								}
							} catch (Exception e) {
								closeSocket();
								websocket = false;
							}
						}
					} while (websocket == true);

					// socket is closed
					for (int i = 0; i < clientListenerList.size(); i++) {
						clientListenerList.get(i).onSocketClosed();
					}

				}
			});
			readingThread.start();

			String websocketKey = new BigInteger(130, new Random(42424242))
					.toString(32);

			websocketResponseExpected = WebSocketHandshake
					.retrieveWebsocketAccept(websocketKey);

			write(WebSocketHandshake.buildWebsocketHandshakeRequest(
					websocketKey).getBytes("UTF-8"));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Set timeout for this socket
	 * 
	 * @param socketTimeout
	 */
	@Override
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	private int write(final byte[] data) {
		try {
			synchronized (socket.getOutputStream()) {
				socket.getOutputStream().write(data);
				socket.getOutputStream().flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	public int writeMessage(String message) {
		try {
			this.websocketChannel.encapsulateMessage(message,
					socket.getOutputStream());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	public void closeSocket() {
		if (socket != null) {
			try {
				socket.getOutputStream().close();
				socket.getInputStream().close();
				socket.close();
			} catch (IOException e) {
			}
		}
		socket = null;
	}

	@Override
	public void closeSocketJoinRead() {
		closeSocket();
		if (readingThread != null) {
			websocket = false;
			try {
				readingThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isConnected() {
		if (socket != null && socket.isConnected())
			return true;
		return false;
	}

	@Override
	public void addClientSocketEventListener(
			IWebsocketClientEventListener eventListener) {
		clientListenerList.add(eventListener);
	}

	@Override
	public void cleanEventListeners() {
		clientListenerList.clear();
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	/**
	 * Set ssl parameters
	 * 
	 * @param keystoreDefaultType
	 *            keystore certificates type
	 * @param trustoreDefaultType
	 *            trustore certificates type
	 * @param keystoreFile
	 *            keystore file path
	 * @param trustoreFile
	 *            trustore file path
	 * @param sslProtocol
	 *            ssl protocol used
	 * @param keystorePassword
	 *            keystore password
	 * @param trustorePassword
	 *            trustore password
	 */
	public void setSSLParams(String keystoreDefaultType,
			String trustoreDefaultType, String keystoreFile,
			String trustoreFile, String sslProtocol, String keystorePassword,
			String trustorePassword) {
		this.keystoreDefaultType = keystoreDefaultType;
		this.trustoreDefaultType = trustoreDefaultType;
		this.keystoreFile = keystoreFile;
		this.trustoreFile = trustoreFile;
		this.sslProtocol = sslProtocol;
		this.keystorePassword = keystorePassword;
		this.trustorePassword = trustorePassword;
	}

}
