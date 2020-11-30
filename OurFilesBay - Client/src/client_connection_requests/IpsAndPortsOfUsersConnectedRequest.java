package client_connection_requests;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class IpsAndPortsOfUsersConnectedRequest extends ClientToDirectory {

	public IpsAndPortsOfUsersConnectedRequest(Socket socket)  {
		super(socket);
	
	}

	public List<String> getIpsAndPortsOfUsersConnected(String userIp,int userPort) {
		List<String> connectedUsers = new ArrayList<String>();
		super.doConnections();
		super.getOut().println("CLT");// critical
		String answerCLT;
		try {
			while (!(answerCLT = super.getIn().readLine()).equals("END")) {// works as expected!
				String[] info = answerCLT.split(" ");
				if (!(userIp.equals(info[1])) || (Integer.parseInt(info[2]) != userPort)) {// conditions to add to the list
					connectedUsers.add(info[1] + " " + info[2]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			super.closeConnections();
		}
		return connectedUsers;
	}

}

