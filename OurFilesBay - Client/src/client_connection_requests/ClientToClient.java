package client_connection_requests;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * A client can have many FlieRequest instances and SearchRequest instances,
 * is a good idea to separate ClientToClient connection into those 2, 
 * this way for the many FileRquest  needed i don't have also to load the
 * SearchRequest... 
 * 
 * Imagine i file whit 1Gb:
 * i'll have like 1 million FileRequest instances
 *
 * if i merge FileRequest + SearchRequest into ClientToClient class 
 * i'll have to load SearchRequest method(part) 1 million times for no reason
 * 
 */

public abstract class ClientToClient {
	
	private Socket socket = null;
	private ObjectOutputStream objectOutputStream = null;
	private ObjectInputStream objectInputStream = null;

	public ClientToClient(Socket socket) {
		this.socket = socket;
	}
	
	protected ObjectInputStream getObjectInputStream() {
		return objectInputStream;
	}
	
	protected ObjectOutputStream getObjectOutputStream() {
		return objectOutputStream;
	}
	
	protected void doConnections() {
		try {
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectInputStream = new ObjectInputStream(socket.getInputStream());//This constructor will block until the corresponding 
			//ObjectOutputStream has written and flushed the header
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void closeConnections() {
		try {
			objectOutputStream.close();
			objectInputStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
