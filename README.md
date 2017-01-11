# Websocket Java Server/Client implementation #

http://bertrandmartel.github.io/websocket-java/

Websocket Server/Client implementation

This project uses HTTP endec project : http://bertrandmartel.github.io/http-endec-java/

<i>Last update 16/05/2015</i>

* [16/05/2015] add SSL/TLS support
* [09/05/2015] add Client event listener

You will find : 
* source in ./libwebsocket folder
* secured and unsecured JS websocket client exemples featuring interactions with websocket server in ./exemples/js folder

<hr/>

<b>How to launch Websocket Server ?</b>

```
WebsocketServer server = new WebsocketServer(WEBSOCKET_PORT);
server.start();
```

you specify the port in WEBSOCKET_PORT

<hr/>

<b>How to monitor my clients connected to server ?</b>

Just add a Listener to server object. You have 3 callbacks that will notify you on client connection change and arrival of client messages

```
server.addServerEventListener(new IClientEventListener() {

			@Override
			public void onMessageReceivedFromClient(IWebsocketClient client,
					String message) {
				//all your message received from websocket client will be here
				System.out.println("message received : " + message);
			}
			
			@Override
			public void onClientConnection(IWebsocketClient client) {
				// when a websocket client connect. This will be called (you can store client object)
				System.out.println("Websocket client has connected");
				
				client.sendMessage("Hello,I'm a websocket server");
				
				//client.close(); // this would close the client connection
			}
			
			@Override
			public void onClientClose(IWebsocketClient client) {
				// when a websocket client connection close. This will be called (you can dismiss client object)
				System.out.println("Websocket client has disconnected");
			}
		});
```

<hr/>

<b>How to launch a SSL secured websocket server ?</b>

```
WWebsocketServer server = new WebsocketServer(WEBSOCKET_PORT);

server.setSsl(true); // set SSL to true (default is false)

```

Then you set your kestore, trustore, type of these certificates, filepath and passwords : 

```
server.setSSLParams(String KEYSTORE_DEFAULT_TYPE,
		    String KEYSTORE_FILE_PATH,
		    String TRUSTORE_FILE_PATH,
		    String SSL_PROTOCOL,
		    String KEYSTORE_PASSWORD,
		    String TRUSTORE_PASSWORD);
```

Here is the description of all of these parameters : 

* KEYSTORE_DEFAULT_TYPE : type of certificates used as keystore, it usually contains public and private certificates, common format are PKCS12 and JKS
* TRUSTORE_DEFAULT_TYPE : type of certificates used as trustore, it should contain list of CA cert your server will trust
* KEYSTORE_FILE_PATH : file path to keystore cert file
* TRUSTORE_FILE_PATH: file path to trustore cert file
* SSL_PROTOCOL : ssl protocol used 
* KEYSTORE_PASSWORD : keystore file password
* TRUSTORE_PASSWORD : trustore file password

Eventually add event listener as described above and start websocket server : 

```
server.start();
```

<hr/>

<b>How to connect a websocket client to server ?</b>

new instance of websocket client to connect to server on HOSTNAME:PORT : 

```
WebsocketClient clientSocket = new WebsocketClient(HOSTNAME, PORT);

clientSocket.connect();
```

You can monitor all your event in your websocket client adding a websocket client event listener like this :

```
clientSocket.addClientSocketEventListener(new IWebsocketClientEventListener() {

					@Override
					public void onSocketConnected() {
						System.out.println("[CLIENT] Websocket client successfully connected");
					}

					@Override
					public void onSocketClosed() {
						System.out.println("[CLIENT] Websocket client disconnected");
					}

					@Override
					public void onIncomingMessageReceived(byte[] data,
							IWebsocketClientChannel channel) {
						System.out.println("[CLIENT] Received message from server : "+ new String(data));
					}
				});
```
* onSocketConnected() : happened when websocket client successfully connect to server
* onSocketClosed()    : happened when websocket client disconnect from server
* onIncomingMessageReceived(byte[] data,IWebsocketClientChannel channel) : happen when incoming data arrives from server. You can write a response directly via IWebsocketClientChannel object with ``writeMessage(String message)`` method

Websocket client can be enabled with SSL :


```
// set SSL encryption
clientSocket.setSsl(true);

// set ssl parameters

clientSocket.setSSLParams(KEYSTORE_DEFAULT_TYPE, TRUSTORE_DEFAULT_TYPE,
		CLIENT_KEYSTORE_FILE_PATH, CLIENT_TRUSTORE_FILE_PATH,
		SSL_PROTOCOL, KEYSTORE_PASSWORD, TRUSTORE_PASSWORD);

```
exactly the same as websocket server

<hr/>

<b>Keystore : public and private server certificates</b>

* To convert cert and key certs to p12 : 

``openssl pkcs12 -export -out server.p12 -inkey server.key -in server.crt``

Thus, you will have : ``String KEYSTORE_DEFAULT_TYPE = "PKCS12"``

* To convert your p12 (containing public and private cert) to jks : 

You have to know your alias (name matching your cert entry), if you dont have it retrieve it with : ``keytool -v -list -storetype pkcs12 -keystore server.p12``

``keytool -importkeystore -srckeystore server.p12 -srcstoretype PKCS12 -deststoretype JKS -destkeystore server.jks``

Thus, you will have : ``String KEYSTORE_DEFAULT_TYPE = "JKS"``

<b>Trustore : should contain only CA certificates</b>

convert ca cert to jks : 

```keytool -import -alias ca -file ca.crt -keystore cacerts.jks -storepass 123456```

Thus, you will have : ``String TRUSTORE_DEFAULT_TYPE = "JKS"``

<hr/>

<b>TroubleShooting</b>

<i>Bad certificate | Unknown CA errors</i>

This could mean you didn't import your not-trusted-CA certificate into your browser.

<i>The remote host closed the connection</i>

Usually happen when your browser closed the connection before the end of SSL handshake. If you already added your CA to your browser dont worry.
Both Chrome and Firefox need to MANUALLY add the certificate (in a popup) so putting it in parameter menu don't change anything.

Just load your URL with "https" : https://127.0.0.1:8443 . Browser will prompt you to accept the certificates and it will probably solve your connection error.

<i>CKR_DOMAIN_PARAMS_INVALID error using openjdk</i>

With openjdk-6-jdk and openjdk-7-jdk, I encountered java.security bug described in https://bugs.launchpad.net/ubuntu/+source/openjdk-7/+bug/1006776 triggering a ``CKR_DOMAIN_PARAMS_INVALID`` exception error. Solution was to edit java.security parameters in /etc/java-7-openjdk/security/java.security 

I replaced that : 
```
security.provider.9=sun.security.smartcardio.SunPCSC
security.provider.10=sun.security.pkcs11.SunPKCS11 ${java.home}/lib/security/nss.cfg
```

with that : 
```
#security.provider.9=sun.security.smartcardio.SunPCSC
security.provider.9=sun.security.ec.SunEC
#security.provider.10=sun.security.pkcs11.SunPKCS11 ${java.home}/lib/security/nss.cfg
```

<b>Browser tested</b>

This has been tested on following browser : 
* Chrome
* Chromium
* Firefox

<hr/>

<b>Debugging SSL connection error</b>

I recommmend using openssl command line tool to debug ssl connection : 

``openssl s_client -connect 127.0.0.1:8443``

You can also use vm argument debugging java ssl implementation : ``-Djavax.net.debug=ssl``

<hr/>

<b>Server-Client key/cert generation</b>

Certs are in libwesocket-test/certs folder, you will find server,client and ca cert build with easy-rsa :

https://github.com/OpenVPN/easy-rsa

With last release of easy-rsa, you can build your own key with the following : 

* ``./build-ca`` : generate a new CA for you
* ``./build-server-full myServer`` : will build for you public cert and private cert signed with CA for server
* ``./build-client-full myClient`` : will build for you public cert and private cert signed with CA for client

<b>How to close my websocket server ?</b>

``server.closeServer();``

<hr/>

<b>COMMAND LINE SYNTAX</b> 

The following will open a websocket on port 4343 (default port value for my exemple)

``java -cp ../libs/commons-codec-1.9.jar:../libs/http-endec-1.0.jar:libwebsocket-1.0.jar fr.bmartel.websocket.LaunchServer``

You can change port number by specifying yours

``java -cp ../libs/commons-codec-1.9.jar:../libs/http-endec-1.0.jar:libwebsocket-1.0.jar fr.bmartel.websocket.LaunchServer 4343``

This exemple is launched from /release folder

<hr/>

<b>Exemple with Javascript Client</b>

* Launch the websocket server on port 4242
* Open the javascript client page in ./exemples/js/ folder

=> You have now a complete websocket chat between java server <-> javascript client in websocket 

![client side](https://raw.github.com/bertrandmartel/websocket-java/master/exemples/readme_images/clientSide.png)


![server side](https://raw.github.com/bertrandmartel/websocket-java/master/exemples/readme_images/serverSide.png)


<b>Exemple with Java websocket client <-> Java websocket server</b>

* Launch the LaunchClient main class from folder libwebsocket in fr.bmartel.network package

This will launch one websocket server on 127.0.0.1:8443 and connect a websocket client to it.
You will be able to send message to the server via websocket and server will answer you back :

![wesocket client exemple](https://raw.github.com/bertrandmartel/websocket-java/master/exemples/readme_images/websocket_client.png)

<hr/>

* Project is JRE 1.7 compliant
* You can build it with ant => build.xml
* Development on Eclipse 
* Specification from https://tools.ietf.org/html/rfc6455

Soon : an exemple using this lib as websocket client communicating with a cpp server<br/>
Soon : websocket client exemple
