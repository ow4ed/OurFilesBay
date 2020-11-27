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
		System.out.println("ConnectionThread - here is the reference that i have:"+out);
		
		String request;
		try {// non pressistent connections
			request = in.readLine(); // triggers try catch block
			System.out.println("Directory - recived: " + request + "(request)");
			String[] parts = request.split(" ");
			// types of requests a client can make to the Directory:

			if (parts[0].equals("INSC")) {
				Runnable task = new Runnable() {
					@Override
					public void run() {
						synchronized (users) {
							users.add(request.substring(5, request.length()));// shared resource
							out.println("accepted");// critical string, method equals will be used
							System.out.println("Directory - Users Connected List:");
							for (String s : users) {// shared resource
								System.out.println(s);
							}
							System.out.println("Directory - END Users Connected List");
						}
						try {
							socket.close();
							out.close();
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				pool.submit(task);
			} else { //if parts[0] fulfills the first if condition i don't check the second, because of if else block 
				if (parts[0].equals("CLT")) {
					Runnable task = new Runnable() {
						@Override
						public void run() {
							synchronized (users) {
								for (String s : users) {// shared resource
									out.println(s);
								}
								out.println("END");// critical string, method equals will be used
								System.out.println("Directory - List of Users Connected sent");
							}
							try {
								socket.close();
								out.close();
								in.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					pool.submit(task);
				}		
			}
		} catch (IOException e) {
			e.printStackTrace();
		} // need to close socket, in and out 
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
