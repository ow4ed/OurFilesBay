package client.client;

import java.io.IOException;
import java.net.Socket;

import client.models.requests.RequestBase;

public class SearchRequest extends RequestBase {
	
	public SearchRequest(Socket socket) {
		super(socket);
	}

	public SearchRequest getUserFilesDetails(SearchRequest wordSearchMessage) {
		super.doConnections();

		try {
			super.getObjectOutputStream().writeObject(wordSearchMessage);
			
			Object o = super.getObjectInputStream().readObject();
			if(o instanceof SearchRequest) {
				return (SearchRequest) o;
			}
			
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} finally {
			super.closeConnections();
		}
		
		return null;
	}
	
}