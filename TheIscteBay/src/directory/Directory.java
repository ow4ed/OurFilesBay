package directory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Directory{
	
	static List<String> users = new LinkedList<String>();
	private ServerSocket serverSocket;
	
	public Directory(int PORT) {
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("Erro ao criar server Socket");
			e.printStackTrace();
		} 
		startServing();
	}
	
	public void startServing() {
		System.out.println("Server Started! ... ");
		try {
			for(;;){
				System.out.println("ServerSocket in Standbay...");
				Socket clientSocket = serverSocket.accept();
				ConnectionThread connection = new ConnectionThread(clientSocket);
				System.out.println("Conection Thread created: " + clientSocket);
				connection.start();
			}
		} catch (IOException e) {
			System.out.println("Erro ao aceitar pedido de coneccao");
			e.printStackTrace();
		} //finally { preguntar se e presiso	
			//serverSocket.close();
		//}
	}
	
	static class ConnectionThread extends Thread {
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
		private String clientPort;
		
		public ConnectionThread(Socket socket){
			this.socket = socket;
			System.out.println("Entrou um user"+socket);
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public synchronized void run() {
			try {
				while(!interrupted()){
					String signUp = in.readLine();
					System.out.println("Recebi : " + signUp);				
					String[] parts = signUp.split(" ");
	
					if(parts[0].equals("INSC")){
						clientPort = parts[3];
						out.println("Coneccao Aceite");
						users.add(signUp.substring(5, signUp.length()));
						System.out.println("----------------Users Connected---------------");
						for(String s: users) {
							System.out.println(s);
						}
						System.out.println("-------------------END------------------");
					}
						
					if(parts[0].equals("CLT")){
						for(String s : users){
							out.println(s);
						}
						out.println("END");
					}
				}
				
			} catch (IOException e) {
				if(clientPort!=null) {
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					for(String s: users) {
						String[] info = s.split(" ");
						if(info[2].equals(clientPort))
							users.remove(s);
					}
				}
				
			}
		}
	}
	
	public static void main(String[] args)  {
		Directory directory = new Directory(8080);
	}
}