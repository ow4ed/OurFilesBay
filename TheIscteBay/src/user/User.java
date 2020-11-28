package user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

public class User{

	private String folder;
	private InetAddress address;
	private final int PORT;
	private String path;
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out; 
	
	public User(String InetAddressDirectory, int PORTDirectory,int PORTUser, String folder) throws UnknownHostException, IOException  {
		this.folder = folder;
		this.address = InetAddress.getLocalHost();//address do user ....
		this.PORT = PORTUser; 
		this.path = this.folder;
		socket = new Socket(InetAddressDirectory, PORTDirectory);//A  connectionToDirecotry e criada pelo ClientDirectory assim que ele se liga com sucesso ao directorio
		doConnections(socket);
		signUpUser();

	}
	
	private void doConnections(Socket socket) throws IOException {
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		
	}
	
	public void signUpUser() throws IOException{ // a interface e que vai mandar o user fazer sungup
		out.println("INSC " + folder + " " + address.getHostAddress() + " " + PORT);
		System.out.println("Passei a inscricao: " + folder+ " " + address.getHostAddress() + " " + PORT);
		String answerINSC = in.readLine();
		System.out.println("Resposta do Servidor: " + answerINSC);
		if(answerINSC.equals("Coneccao Aceite")) {
			ClientServer server = new ClientServer(PORT, folder, path);
			server.start();
		}	
	}
	
	public List<String> getUsersIpsPORTs() throws IOException { // vai pedilos a connectionToDirecotry
		System.out.println("-------------getUsersIpsPORTs---------------");
		List<String> connectedUsers = new LinkedList<String>();
		out.println("CLT");
		for(;;){
			String answerCLT = in.readLine();
			if(answerCLT.equals("END")){
				break;
			}
			String[] info = answerCLT.split(" ");
			for(String s:info) {//3 linhas pa apagar
				System.out.println(s);
			}
			if(!this.address.getHostAddress().equals(info[1]) || Integer.parseInt(info[2])!=this.PORT) {
				connectedUsers.add(info[1]+" "+info[2]);
			}
		}
		System.out.println("----------------end getUsersIpsPORTs------------------");
		return connectedUsers;
	}
	
	public String getFolder() {
		return this.folder;
	}
	public int getUserPORT() {
		return this.PORT;
	}
	public String getUserPath() {
		return path;
	}	
}