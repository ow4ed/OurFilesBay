package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import coordination_structures.ThreadPool;


/*
 * Directory Server Connection
 * (the same ideas apply to Client Server Connection)
 * 
 * 1. (Thread) queue the connection request -> offer it to pool 
 * 
 * 2. (Thread) execute the connection request queued -> take it from the pool
 * 
 * This way:
 * 
 * - i can handle multiple connection requests at the same time; (controllable how many at the same time)
 * important to control because i can have many request at the same time;
 * 
 * - respond to multiple requests at the same time; (controllable how many at the time)
 * important to control even the simples type because even the simplest type of request may take its time (connection delays, shared resource)
 * 
 * 
 * If want to separate 1. and 2. just need to create another Thread Pool
 * 
 */


public class Connection implements Runnable {// if this isn't a thread, then server will do only 1 thing: acept 1 connection then process it,
	private Socket socket = null;    
	private PrintWriter out = null;// meanwhile another user may request an connection, but the server will be buisy processing the previous one
	private BufferedReader in = null;
	private ThreadPool pool;
	private List<String> users;
	
	public Connection(Socket socket, ThreadPool pool, List<String> users){
		this.socket = socket;
		this.pool = pool;
		this.users = users;
	}
	
	@Override
	public void run() {
		doConnections();
		try {// non pressistent connections
			String request = in.readLine(); // triggers try catch block
			String[] info = request.split(" ");
			System.out.println("Directory - recived: " + request + "(request)");
			// types of requests a client can make to the Directory: 
			if (info[0].equals("INSC")) {
				signUpClient(request.substring(5, request.length())); //change to parts
			} else { //if parts[0] fulfills the first if condition i don't check the second, because of if else block 
				if (info[0].equals("CLT")) {
					sendUsersConnectedToClient();
				}		
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private void doConnections() {
		System.out.println("Directory - an user has request an connection: "+socket+"(socket)");
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
	
	private void signUpClient(String user) {
		Runnable task = new Runnable() { //if i have like 2k users connected// and like 400 connections asking for the uses //if divide this in parts i won't have 400 active threads
			@Override					//10 more asking to sing up
			public void run() {			//20 more asking to leave
				synchronized (users) {	// this task can take time to execute no ?, like i have 430 threads competing for acess to the users list
					users.add(user);// shared resource
					out.println("accepted");// critical string, method equals will be used
					System.out.println("Directory - Users Connected List:");//i'll do something similar like this in directory gui 
					for (String s : users) {// shared resource
						System.out.println(s);
					}
					System.out.println("Directory - END Users Connected List");
				}
				closeConnections();
			}
		};
		pool.submit(task);
	}
	
	private void sendUsersConnectedToClient() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				synchronized (users) {
					for (String s : users) {// shared resource
						out.println(s);
					}
					out.println("END");// critical string, method equals will be used
				}
				closeConnections();
			}
		};
		pool.submit(task);
	}

}
/* remove a client when it exists the make this trigger when he presses X
 * if(clientPort!=null) {//user is always delted if some error happened
			System.out.println("Directory - removing: " +clientPort);
			for(String s: users) {//shared resource
				String[] info = s.split(" ");
				if(info[2].equals(clientPort))
					users.remove(s);//shared resource
			}
		}
 */
