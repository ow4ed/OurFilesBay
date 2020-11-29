package client_connection_requests;

import java.io.IOException;
import java.net.Socket;

import serializable_objects.UserFilesDetails;
import serializable_objects.WordSearchMessage;

public class SearchRequest extends ClientToClient {

	private WordSearchMessage wordSearchMessage;
	
	public SearchRequest(Socket socket, WordSearchMessage wordSearchMessage) {
		super(socket);
		
		this.wordSearchMessage = wordSearchMessage;
	}

	public UserFilesDetails getUserFilesDetails() {
		super.doConnections();

		try {
			super.getObjectOutputStream().writeObject(wordSearchMessage);
			
			Object o = super.getObjectInputStream().readObject();
			if(o instanceof UserFilesDetails) {
				return (UserFilesDetails) o;
			}
			
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} finally {
			super.closeConnections();
		}
		
		return null;
	}
	
}