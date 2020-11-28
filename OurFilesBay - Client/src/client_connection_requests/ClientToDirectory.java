package client_connection_requests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public abstract class ClientToDirectory {
	//always initialize those 3 below
	private Socket socket = null;
	private PrintWriter out = null; 
	private BufferedReader in = null;
	
	public ClientToDirectory(Socket socket) {
		this.socket = socket;
	}
	
	protected PrintWriter getOut() {
		return out;
	}
	
	protected BufferedReader getIn() {
		return in;
	}
	
	protected void doConnections() {
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	protected void closeConnections() {
		out.close();
		try {
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
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