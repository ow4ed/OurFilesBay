package client_connection_requests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

public abstract class ClientToDirectory {
	//always initialize those 3 below
	private Socket socket = null;
	private PrintWriter out = null; 
	private BufferedReader in = null;
	
	public ClientToDirectory(Socket socket) throws UnknownHostException {
		this.socket = socket;
	}
	
	protected void doConnections() {
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	//public abstract boolean signUpUser(String request);
	
	public class SignUpUserRequest extends ClientToDirectory{
		
		public SignUpUserRequest(Socket socket) throws UnknownHostException {
			super(socket);
			
		}
		
		public boolean signUpUser(String request) { // a interface e que vai mandar o user fazer sungup
			doConnections();
			
			out.println(request);
			try {
				String answer = in.readLine();
				System.out.println("here is my ANSWER BRUH:"+answer);
				if (answer.equals("accepted")) {// critical equals
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
					in.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}

	}

	
	
}
/*
public List<String> geIpsAndPortsOfUsersConnected(String userIp, int userPort) { //ConnectionThtread in Directory whit the User will be created
	doConnections();
	
	List<String> connectedUsers = new LinkedList<String>();
	
	out.println("CLT");// critical
	String answerCLT;
	try {
		while(!(answerCLT = in.readLine()).equals("END")) {// works as expected!
			String[] info = answerCLT.split(" ");
			if (!(userIp.equals(info[1])) || (Integer.parseInt(info[2]) != userPort)) {//conditions to add to the list
				connectedUsers.add(info[1] + " " + info[2]);
			}
		}
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		try {
			socket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	return connectedUsers;
}*/