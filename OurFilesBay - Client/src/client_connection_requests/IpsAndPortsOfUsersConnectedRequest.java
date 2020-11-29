package client_connection_requests;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class IpsAndPortsOfUsersConnectedRequest extends ClientToDirectory {
	
	private String userIp;
	private int userPort;

	public IpsAndPortsOfUsersConnectedRequest(Socket socket, String userIp, int userPort)  {
		super(socket);
		
		this.userIp = userIp;
		this.userPort = userPort;
	}

	public List<String> getIpsAndPortsOfUsersConnected() {
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

