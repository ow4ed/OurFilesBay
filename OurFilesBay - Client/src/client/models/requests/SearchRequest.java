package client.models.requests;

import java.io.IOException;
import java.net.Socket;

import client.models.responses.UserFilesDetails;
import client.models.responses.WordSearchMessage;

public class SearchRequest extends ClientToClient {
	
	public SearchRequest(Socket socket) {
		super(socket);
	}

	public UserFilesDetails getUserFilesDetails(WordSearchMessage wordSearchMessage) {
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