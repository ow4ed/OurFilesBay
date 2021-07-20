package directory.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import directory.models.ErrorResponseType;
import directory.models.GetClientsRequestType;
import directory.models.GetClientsResponseType;
import directory.models.SignupRequestType;
import directory.models.SingupResponseType;

public class Connection implements Runnable {
	
	private Socket socket;   
	private List<String> clients;
	
	public Connection(Socket socket, List<String> clients){
		this.socket = socket;
		this.clients = clients;
	}
	
	@Override
	public void run() {
		System.out.println("Directory - Connection - thread start");
		
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
			handleRequest(out,in);
			
						
		// exceptions during client connection only shut down connection
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
		
		System.out.println("Directory - Connection - thread stop");
	}

	private void handleRequest(ObjectOutputStream out, ObjectInputStream in) throws IOException {
		System.out.println("Directory - handleRequest - Start");
		
		try {
			// get request Object
			Object o = in.readObject();
			
			// parse request
			if (o instanceof SignupRequestType) {
		
				SignupRequestType request = (SignupRequestType) o;
				
				SingupResponseType response = new SingupResponseType();
				
				// add client port to singed up clients data list
				synchronized (clients) {
					// TODO: validations
					if(!clients.contains(request.getClientPort())) {
						clients.add(request.getClientPort());
						response.setSignedUp(true);
					} else {
						response.setSignedUp(false);
					}
				}
				
				// send back an response
				out.writeObject(response);
				
			} else if (o instanceof GetClientsRequestType) {
				
				GetClientsRequestType request = (GetClientsRequestType) o;
				
				GetClientsResponseType response = new GetClientsResponseType();
			
				synchronized (clients) {
					response.setNewClients(clients);
				}
				
				// do not sent already known clients
				response.getNewClients().removeAll(request.getCurrentClients());
				
				// do not send more than the number of clients asked
				response.setNewClients(response.getNewClients().subList(0, request.getNumberOfClients()));

				
				// send back an response
				out.writeObject(response);
			
			// if request class object is found but is invalid
			} else {
				
				throw new ClassNotFoundException();
			}

		} catch (ClassNotFoundException e) {
			// send back an response ( bad request, invalid type )
			ErrorResponseType response = new ErrorResponseType();
			response.setReason("Invalid request type");
			out.writeObject(response);
		}
		
		System.out.println("Directory - handleRequest - End");
		
	}

}
