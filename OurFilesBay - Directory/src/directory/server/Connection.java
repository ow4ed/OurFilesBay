package directory.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class Connection implements Runnable {
	
	private Socket socket;   
	private List<String> clients;
	
	public Connection(Socket socket, List<String> clients){
		this.socket = socket;
		this.clients = clients;
	}
	
	@Override
	public void run() {
		System.out.println("Directory - Connection - start");
		
		PrintWriter out = null;
		BufferedReader in = null;
		
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String request = in.readLine();
			System.out.println("Directory - Connection - recived Request: " + request);
			
			// analyze request and send back a response
			String[] requerstInfo = request.split(" ");
			if ("INSC".equals(requerstInfo[0])) {
				
				// get client port from request
				String clientPort = request.substring(5, request.length());
		
				// add client port to clients ports list
				synchronized (clients) {
					clients.add(clientPort);
				}
				
				// send back an response
				out.println("accepted");
				
			} else if ("CLT".equals(requerstInfo[0])) {
				
				synchronized (clients) {
					// send back multiple responses ( other clients ports)
					for (String s : clients) {
						out.println(s);
					}
					// client interprets this "END" as the end of the list
					out.println("END");
				}

			}
		} catch (IOException e) {
			
			e.printStackTrace();
		} finally {

			try {
				// stop
				out.close();
				in.close();
				socket.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		System.out.println("Directory - Connection - stop");
	}

}
