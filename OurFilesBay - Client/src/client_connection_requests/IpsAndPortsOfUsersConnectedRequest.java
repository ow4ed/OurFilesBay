package client_connection_requests;

import java.net.Socket;
import java.net.UnknownHostException;

public class IpsAndPortsOfUsersConnectedRequest extends ClientToDirectory implements Runnable{

	public IpsAndPortsOfUsersConnectedRequest(Socket socket) throws UnknownHostException {
		super(socket);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
