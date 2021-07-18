package client.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

import client.models.coordination.ThreadPool;

public class ClientServer { ///still thinking about this part

	private int port;
	private ServerSocket serverSocket = null;
	private String username;
	private ThreadPool pool;
	private String path;

	public ClientServer(int port, String username, ThreadPool pool, String path) {
		this.port = port;
		this.username = username;
		this.pool = pool; 
		this.path = path;
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
				Connection connection = new Connection(clientSocket, pool, username, path);
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