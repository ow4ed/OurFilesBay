package client_connection_requests;

import java.io.IOException;
import java.net.Socket;

import serializable_objects.UserFilesDetails;

public class SearchFileRequest extends ClientToClient implements Runnable{
	
	private Object msg;
	private UserFilesDetails userFilesDetails;
	
	public SearchFileRequest(Socket socket, Object msg,UserFilesDetails userFilesDetails)  {
		super(socket);
		
		this.msg = msg;
		this.userFilesDetails = userFilesDetails;
	}
	
	@Override
	public void run() {
		super.doConnections();

		try {
			
			super.getObjectOutputStream().writeObject(msg);
			Object o = super.getObjectInputStream().readObject();
			if(o instanceof UserFilesDetails) { //se o que recebi for um FileDetails
				
				
				userFilesDetails = (UserFilesDetails)o;
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} finally {
			super.closeConnections();
		}
	}
	
}