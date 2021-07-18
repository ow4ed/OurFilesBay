package client.models.requests;

import java.io.IOException;
import java.net.Socket;

public class SignUpUserRequest extends ClientToDirectory{
	
	public SignUpUserRequest(Socket socket) {
		super(socket);
		
	}
	
	public boolean signUpUser(String request) { // this request comes from the gui ...
		super.doConnections();
		
		super.getOut().println(request);
		try {
			String answer = super.getIn().readLine();

			if (answer.equals("accepted")) {// critical equals
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			super.closeConnections();
		}
		return false;
	}

}
