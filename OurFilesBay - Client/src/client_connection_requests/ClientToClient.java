package client_connection_requests;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
