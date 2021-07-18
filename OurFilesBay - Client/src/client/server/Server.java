package client.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import client.models.coordination.ThreadPool;

public class Server implements Runnable{

	private int port;
	private ThreadPool pool;
	private String userName;
	private String userFileSystemPath;

	public Server(int port, int threads, String userName, String userFileSystemPath) {
		this.port = port;
		// initialize an thread pool whit n threads 
		this.pool = new ThreadPool(threads); 
		this.userName = userName; 
		this.userFileSystemPath = userFileSystemPath;
	}

	@Override
	public void run() {
		System.out.println("Client "+ userName +" - Server - start");
		
		ServerSocket serverSocket = null;
		try {
			
			serverSocket = new ServerSocket(port);
			
			while (true) {
				System.out.println("Client "+ userName +" - Server - server in standbay ...");
				
				// actively looking for next client connection to accept
				Socket clientSocket = serverSocket.accept();
				// use one of the threads available in the thread pool to handle request
				Connection connection = new Connection(clientSocket, userName, userFileSystemPath);
				pool.submit(connection);
				
				System.out.println("Client "+ userName +" - Server - conection request handled: " + clientSocket);
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

		System.out.println("Client "+ userName +" - Server - stop");
	}

}