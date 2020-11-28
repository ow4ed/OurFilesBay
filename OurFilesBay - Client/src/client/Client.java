package client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JProgressBar;
import javax.swing.ListModel;

import client_connection_requests.ClientToDirectory;
import client_connection_requests.FileRequest;
import client_connection_requests.SearchFileRequest;
import client_connection_requests.SignUpUserRequest;
import coordination_structures.BlockingQueue;
import coordination_structures.ThreadPool;
import gui.GraphicInterface;
import serializable_objects.FileBlockRequest;
import serializable_objects.UserFilesDetails;
import serializable_objects.WordSearchMessage;
import server.ClientServer;

public class Client{

	private String username;// = folder
	private InetAddress userIp;
	private final int userPort;
	private String directoryIp;
	private int directoryPort;
	private ThreadPool pool;
	
	private DefaultListModel<String> userFilesJList = new DefaultListModel<String>();  
	private DefaultListModel<String> searchResultJList = new DefaultListModel<String>();  
	private List<String> searchResultList = new ArrayList<String>();
	
	private static final int BLOCK_SIZE = 1024;
	private int blocksDownloaded;
	private byte[] file;
	
	public Client(String username, int portUser,String inetAddressDirectory, int portDirectory,int nCoresToUse) throws UnknownHostException {
		this.username = username;
		this.userIp = InetAddress.getLocalHost();//address of user, thrws Exception
		this.userPort = portUser;
		this.directoryIp = inetAddressDirectory;
		this.directoryPort = portDirectory;
		this.pool = new ThreadPool(nCoresToUse);
	}
	
	public String getUsername() {
		return username;
	}
	
	public ListModel<String> getSearchResultJList() {
		return searchResultJList;
	}
	
	
	public DefaultListModel<String> getUserFilesJList(){
		return userFilesJList;
		
	}
	
	public void signUp() {
		try {
			SignUpUserRequest connection = new SignUpUserRequest(new Socket(directoryIp, this.directoryPort));
			if (connection.signUpUser("INSC " + username + " " + userIp.getHostAddress() + " " + userPort)) {
				ClientServer server = new ClientServer(userPort, username, pool);
				server.startServing();
			} else {
				System.out.println(username + " - Error during sign up request!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	public void updateSearchResultJList(String searchText) {
		try {
			ClientToDirectory clientToDirectoryConnection = new ClientToDirectory(new Socket(directoryIp, this.directoryPort));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
			searchResultJList.clear();
			searchResultList.clear(); 
			for (UserFilesDetails fd : getUsersDetailsAndFilesList(searchText)) {
				for (int i = 0; i < fd.getFiles().length; i++) {
					searchResultJList.addElement(fd.getUserName() + ": " + fd.getFiles()[i].getName() + " ,"+ fd.getFiles()[i].length() + " bytes");
					searchResultList.add(fd.getFiles()[i].getName() +" "+ fd.getFiles()[i].length() + " " + fd.getIp() + " " + fd.getPort());
				}
			}
	}
	
	private List<UserFilesDetails> getUsersDetailsAndFilesList(String searchString) {//faria setnido ser um runnable porque fica em wait
		WordSearchMessage search = new WordSearchMessage(searchString);
		List<UserFilesDetails> usersDetailsAndFilesList = new ArrayList<UserFilesDetails>();
		try {
			ClientToDirectory clientToDirectoryConnection = new SignUpUserRequest(new Socket(directoryIp, this.directoryPort));
		
			for (String ipAndPort : clientToDirectoryConnection.geIpsAndPortsOfUsersConnected(userIp.getHostAddress(), userPort)) {
				String[] info = ipAndPort.split(" ");

				SearchRequest clientToClientConnection = new SearchRequest(new Socket(info[0], Integer.parseInt(info[1])), search);
				pool.submit(clientToClientConnection);
				
				usersDetailsAndFilesList.add(userFilesDetails);
					
					Thread thread = new Thread(clientToClientConnection);
					thread.start();
					try {
						thread.join();  /// not sure why tho yet
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					usersDetailsAndFilesList.add(clientToClientConnection.getFileDetails());
					
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return usersDetailsAndFilesList;
	}

	public void updateUserFilesJList() {
		userFilesJList.clear();
		File[] files = new File(username).listFiles();
		for(File f:files) {
				userFilesJList.addElement(f.getName()+" ,"+f.length()+" bytes");
		}
	}

	public void download(int index, JProgressBar progressJProgressBar) {//progressJProgressBar.setValue(0);
		blocksDownloaded = 0;  

		String s = searchResultList.get(index);
		String[] info = s.split(" ");
		String fileName = info[0];
		int fileSize = Integer.parseInt(info[1]);
		List<String> ipsAndPorts = new ArrayList<String>(); // porto + ip de quem tem
		
		for(String line:searchResultList) {
			String[] lineInfo = line.split(" ");
			if(lineInfo[0].equals(fileName) && Integer.parseInt(lineInfo[1])==fileSize) {
				ipsAndPorts.add(lineInfo[2]+" "+lineInfo[3]);
			}
		}

		
		int parts = fileSize/BLOCK_SIZE; 
		int lastpart = fileSize-(BLOCK_SIZE*parts);
		BlockingQueue<FileBlockRequest> blocks = new BlockingQueue<>();
		
		file= new byte[fileSize];
		for(int i = 0;i<parts;i++) {
			try {
				blocks.offer(new FileBlockRequest(fileName, BLOCK_SIZE, BLOCK_SIZE*i));
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		if(lastpart>0) {
			try {
				blocks.offer(new FileBlockRequest(fileName, lastpart, BLOCK_SIZE*(parts)));
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	
		for(int i = 0; i < ipsAndPorts.size(); i++){
			String user = ipsAndPorts.get(i);
			String[] userFields = user.split(" ");
			FileRequest dfp;
			try {
				System.out.println("i'm tryng to open a socket whit IP and Port:"+userFields[0]+","+userFields[1]+";");
				dfp = new FileRequest(new Socket(userFields[0], Integer.parseInt(userFields[1])), blocks, file, blocksDownloaded,progressJProgressBar);
				//i have en error here :(
				
				Thread thread = new Thread(dfp);
				thread.start();
				
			} catch (NumberFormatException | IOException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			Files.write(Paths.get(username+"/" + fileName), file);
		} catch ( IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		for (int i = 1; i <= 3; i++) {
			try { 
				Client user = new Client("User_"+i,8080+i,"127.0.0.1", 8080, 4);// 127.0.0.1 localhost 
				GraphicInterface gui = new GraphicInterface(user);//8080 port, but could be any port
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}

	}

}