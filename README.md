# Websocket Client / Server implementation in Java #

http://akinaru.github.io/websocket-java/

<i>Last update 09/05/2015</i>

Websocket Client & Server implementation

* Very easy to use
* A sample is provided to launch a Websocket server and test with a Javascript Client in exemples/js folder

<hr/>

<b>How to launch Websocket Server ?</b>

``WebsocketServer server = new WebsocketServer(WEBSOCKET_PORT);``
``server.start();``

you specify the port in WEBSOCKET_PORT

<hr/>

<b>How to monitor my clients connected to server ?</b>

Just add a Listener to server object. You have 3 callbacks that will notify you on client connection change and arrival of client messages

``server.addServerEventListener(new IClientEventListener() {
			
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
		});``

<hr/>

<b>How to close my websocket server ?</b>

``server.closeServer();``

<hr/>

<b>COMMAND LINE SYNTAX</b> 

The following will open a websocket on port 4242 (default port value for my exemple)

``java -cp ../libs/commons-codec-1.9.jar:../libs/http-endec-1.0.jar:wlandecoder-1.0.jar fr.bmartel.websocket.LaunchServer``

You can change port number by specifying yours

``java -cp ../libs/commons-codec-1.9.jar:../libs/http-endec-1.0.jar:wlandecoder-1.0.jar fr.bmartel.websocket.LaunchServer 4343``

This exemple is launched from /release folder

<hr/>

<b>Exemple with Javascript Client</b>

* Launch the websocket server on port 4242
* Open the javascript client page in ./exemples/js/ folder

=> You have now a complete websocket chat between java server <-> javascript client in websocket 

![alt tag](https://raw.github.com/akinaru/websocket-java/master/exemples/clientSide.png)


![alt tag](https://raw.github.com/akinaru/websocket-java/master/exemples/serverSide.png)
<hr/>

* Project is JRE 1.7 compliant
* You can build it with ant => build.xml
* Development on Eclipse 
* Specification from https://tools.ietf.org/html/rfc6455

Soon : an exemple using this lib as websocket client communicating with a cpp server
