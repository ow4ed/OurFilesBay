package server_client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import coordination_structures_client.ThreadPool;

public class ClientServer { ///still thinking about this part

	private int port;
	private ServerSocket serverSocket = null;
	private String username;
	private ThreadPool pool;

	public ClientServer(int port, String username, ThreadPool pool) {
		this.port = port;
		this.username = username;
		this.pool = pool; 
	}

	public void startServing() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Runnable task = new Runnable() {
			@Override
			public void run() {
				serve();
			}
		};
		Thread thread = new Thread(task);
		thread.start();
		System.out.println(username + " - server started! ");
	}
	
	private void serve() {
		while (!Thread.currentThread().isInterrupted()) {
			System.out.println(username + " - server in standbay...");
			try {
				Socket clientSocket = serverSocket.accept();
				Connection connection = new Connection(clientSocket, pool, username);
				System.out.println(username + " - conection thread created: " + clientSocket);
				pool.submit(connection);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}