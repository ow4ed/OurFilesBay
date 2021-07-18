package client.models.responses;

import java.io.IOException;
import java.net.Socket;

import client.models.requests.RequestBase;

public class SearchResponse extends RequestBase {
	
	public SearchResponse(Socket socket) {
		super(socket);
	}

	public UserFilesDetails getUserFilesDetails(SearchResponse wordSearchMessage) {
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