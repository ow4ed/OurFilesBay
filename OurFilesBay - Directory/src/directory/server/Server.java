package directory.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import directory.models.ThreadPool;

public class Server implements Runnable{ 
	
	private int port;
	private ThreadPool pool;
	
	// list that stores clients ports
	private List<String> clients;
	
	public Server(int port, int threads) {
		
		this.port = port;
		// initialize an thread pool whit n threads 
		this.pool = new ThreadPool(threads); 
		
		this.clients = new ArrayList<String>();
	}
	
	@Override
	public void run() {
		System.out.println("Directory - Server - start");
		
		ServerSocket serverSocket = null;
		try {
			
			serverSocket = new ServerSocket(port);
			
			while (true) {
				System.out.println("Directory - Server - server in standbay ...");
				
				// actively looking for next client connection to accept
				Socket clientSocket = serverSocket.accept();
				// use one of the threads available in the thread pool to handle client request
				Connection connection = new Connection(clientSocket, clients);
				pool.submit(connection);
				
				System.out.println("Directory - Server - conection request handled: " + clientSocket);
			}
	
		} catch (IOException e1) {
			
			e1.printStackTrace();
		} finally {
			
			try {
				// stop
				serverSocket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}

		System.out.println("Directory - Server - stop");
	}

}