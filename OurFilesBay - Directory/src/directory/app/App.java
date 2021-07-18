package directory.app;

import directory.server.Server;

public class App {
	
	public static void main(String[] args) {
		
		Server directory = new Server(8080,3); 
		Thread serverThread = new Thread(directory);
		serverThread.start();
		//threaD.start();
	}
}
