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

import client_connection_requests.FileRequest;
import client_connection_requests.IpsAndPortsOfUsersConnectedRequest;
import client_connection_requests.SearchRequest;
import client_connection_requests.SignUpUserRequest;
import coordination_structures.BlockingQueue;
import coordination_structures.ThreadPool;
import gui.GraphicInterface;
import serializable_objects.FileBlockRequest;
import serializable_objects.UserFilesDetails;
import serializable_objects.WordSearchMessage;
import server.ClientServer;



/*
 * awt.List is a List component used in GUI where as java.util.List is an interface for the lists data structure 
 * care
 * 
 *  5 to 20  lines
 * 
 * 
 */
//CopyOnWriteArrayList
public class Client{

	private String username;// = folder
	private InetAddress userIp;
	private final int userPort;
	private String directoryIp;
	private int directoryPort;
	private ThreadPool pool;
	
	
	//awt.List is a List component used in GUI where as java.util.List is an interface for the lists data structure change this part
	private DefaultListModel<String> userFilesJList = new DefaultListModel<String>();  
	private DefaultListModel<String> searchResultJList = new DefaultListModel<String>();  
	private List<String> searchResultList = new ArrayList<String>();
	
	
	public Client(String username, int portUser,String inetAddressDirectory, int portDirectory,int nCoresToUse) {
		this.username = username;
		try {
			this.userIp = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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
	
	public void signUp() {//first thing need to be done 
		try {
			SignUpUserRequest clientToDirectory = new SignUpUserRequest(new Socket(directoryIp, this.directoryPort));
			if (clientToDirectory.signUpUser("INSC " + username + " " + userIp.getHostAddress() + " " + userPort)) {
				ClientServer server = new ClientServer(userPort, username, pool);
				server.startServing();
			} else {
				System.out.println(username + " - Error during sign up request!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateSearchResult(String searchText) {// make this a thread | 1 task -> 1 Thread every time this thread // stars, of here was another going on it need to be killed
		Runnable searchTask = new Runnable() {
			@Override
			public void run() {
				try {
					IpsAndPortsOfUsersConnectedRequest clientToDirectory = new IpsAndPortsOfUsersConnectedRequest(
							new Socket(directoryIp, directoryPort), userIp.getHostAddress(), userPort);
					updateLists(clientToDirectory.getIpsAndPortsOfUsersConnected(), searchText);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		pool.submit(searchTask);
	}
	
	private void updateLists(List<String> ipsAndPortsOfUsersConnected, String searchText) {
		searchResultJList.clear();
		searchResultList.clear();
		WordSearchMessage wordSearchMessage = new WordSearchMessage(searchText);
		for (String user : ipsAndPortsOfUsersConnected) {
			String[] info = user.split(" ");
			try {
				SearchRequest clientToClient = new SearchRequest(new Socket(info[0], Integer.parseInt(info[1])), wordSearchMessage);
				UserFilesDetails userFilesDetails = clientToClient.getUserFilesDetails();
				for (File file : userFilesDetails.getFiles()) {
					searchResultJList.addElement(
							userFilesDetails.getUserName() + ": " + file.getName() + " ," + file.length() + " bytes");
					searchResultList.add(file.getName() + " " + file.length() + " " + userFilesDetails.getIp() + " "
							+ userFilesDetails.getPort());
					System.out.println(username+" - Added to JList:"+userFilesDetails.getUserName() + ": " + file.getName() + " ," + file.length() + " bytes");
					System.out.println(username+" - Added to List:"+file.getName() + " " + file.length() + " " + userFilesDetails.getIp() + " "
							+ userFilesDetails.getPort());
				}
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void updateUserFilesJList() {
		//Runnable searchTask = new Runnable() {  later
		userFilesJList.clear();
		File[] files = new File(username).listFiles();
		for(File f:files) {
				userFilesJList.addElement(f.getName()+" ,"+f.length()+" bytes");
		}
	}

	public void download(int index, JProgressBar progressJProgressBar) {//progressJProgressBar.setValue(0);
		//Runnable download = new Runnable() {  later
		//int BLOCK_SIZE = 1024;
		int blocksDownloaded = 0;
		//byte[] file;

		
		List<String> ipsAndPorts = getIpsAndPortsOfFile(index);
		//int parts = fileSize/BLOCK_SIZE; 
		//int lastpart = fileSize-(BLOCK_SIZE*parts);
		
		
		//bq
		BlockingQueue<FileBlockRequest> blocks = new BlockingQueue<>();
		//file= new byte[fileSize];
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
		//
		
		
		for(int i = 0; i < ipsAndPorts.size(); i++){
			String user = ipsAndPorts.get(i);
			String[] userFields = user.split(" ");

			try {
				//System.out.println("i'm tryng to open a socket whit IP and Port:"+userFields[0]+","+userFields[1]+";");
				FileRequest dfp = new FileRequest(new Socket(userFields[0], Integer.parseInt(userFields[1])), blocks, file, blocksDownloaded,progressJProgressBar);
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
	
	private List<String> getIpsAndPortsOfFile(int index) {
		List<String> ipsAndPorts = new ArrayList<String>(); // port + Ip of who had the file
		
		String s = searchResultList.get(index);
		String[] info = s.split(" ");
		String fileName = info[0];
		String fileSize = info[1];
		for(String line:searchResultList) {
			String[] lineInfo = line.split(" ");
			if(lineInfo[0].equals(fileName) && lineInfo[1].equals(fileSize)) {
				ipsAndPorts.add(lineInfo[2]+" "+lineInfo[3]);
			}
		}
		return ipsAndPorts;
	}

	public static void main(String[] args) {
		for (int i = 1; i <= 3; i++) {
			Client user = new Client("User_" + i, 8000 + i, "127.0.0.1", 8080, 4);// 127.0.0.1 localhost
			GraphicInterface gui = new GraphicInterface(user);// 8000 port, but could be any port
		}

	}

}