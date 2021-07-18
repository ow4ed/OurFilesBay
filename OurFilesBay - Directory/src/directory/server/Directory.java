package directory.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import directory.models.ThreadPool;

public class Directory { 
	
	private int port;
	private ServerSocket serverSocket = null;
	
	private List<String> clients = new ArrayList<String>();
	private ThreadPool pool;
	
	public Directory(int port, int threads) {
		this.port = port;
		// initialize an thread pool whit n threads 
		this.pool = new ThreadPool(threads); 
	}
	
	public void startServing() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Runnable task = new Runnable() {
			@Override
			public void run() {
				serve();
			}
		};

		Thread thread = new Thread(task);
		thread.start();
		System.out.println("Directory - server started ...");
	}

	private void serve() {
		while (!Thread.currentThread().isInterrupted()) {
			System.out.println("Directory - server in standbay ...");
			try {
				Socket clientSocket = serverSocket.accept();// clientSocket won't be closed here
				Connection connection = new Connection(clientSocket, pool, clients);
				System.out.println("Directory - conection thread created: " + clientSocket);
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
	
	public static void main(String[] args) {
		
		Directory directory = new Directory(8080,3); 
		
		directory.startServing();

	}

}