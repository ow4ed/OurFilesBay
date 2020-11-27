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
		super();

		this.socket = socket;
	}
	
	protected void doConnections() {
		try {
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());//This constructor will block until the corresponding 
			objectInputStream = new ObjectInputStream(socket.getInputStream());//ObjectOutputStream has written and flushed the header
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("User - Did connections Sucessfully!!!!");
	}
	
	protected Socket getScoket() {
		return socket;
	}
	
	protected ObjectInputStream getObjectInputStream() {
		return objectInputStream;
	}
	
	protected ObjectOutputStream getObjectOutputStream() {
		return objectOutputStream;
	}

}
