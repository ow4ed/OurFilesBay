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
import coordination_structures_client.FileBloksQueue;
import coordination_structures_client.ThreadPool;
import gui.GraphicInterface;
import serializable_objects.FileBlockRequest;
import serializable_objects.UserFilesDetails;
import serializable_objects.WordSearchMessage;
import server_client.ClientServer;



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
	
	private final static int BLOCK_SIZE = 1024;
	
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
	
	/*
	 * signUp method - First Thing needed to be done, this method is a priority. 
	 * 
	 * If this method is successful:
	 * 1. Client is visible to Other Clients;
	 * 2. client knows for sure that directory is accessible for requests
	 * 
	*/
	public void signUp() {//first thing needed to be done, method is a priority when user launches the program
		try {//if this method sucess i hav
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
							new Socket(directoryIp, directoryPort));
					searchResultJList.clear();
					searchResultList.clear();
					updateLists(clientToDirectory.getIpsAndPortsOfUsersConnected(userIp.getHostAddress(), userPort), searchText);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		pool.submit(searchTask);
	}
	
	/*
	 * updateLists method maybe needs to create a thread per user, this way, for example Client
	 * dosen't have to wait for the user_1 1hour to give all his files ... (if it takes him 1 hour)
	 * 
	 *i can have synchronized(defaultList) 
	 * 
	 * or i can implement a new coordination_structure: barrier
	 */
	private void updateLists(List<String> ipsAndPortsOfUsersConnected, String searchText) {
		WordSearchMessage wordSearchMessage = new WordSearchMessage(searchText);
		for (String user : ipsAndPortsOfUsersConnected) {
			String[] info = user.split(" ");
			try {//SearchRequest -> 1 Connection whit an user, the way i'm doing isn't optimal yet, imagine user_1 takes 1hour to give all his files info...
				SearchRequest clientToClient = new SearchRequest(new Socket(info[0], Integer.parseInt(info[1])));
				UserFilesDetails userFilesDetails = clientToClient.getUserFilesDetails( wordSearchMessage);
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

	public void download(int index, JProgressBar progressJProgressBar) {//accessed by multiple Threads
		//Runnable download = new Runnable() {  ?
		String selectedFile = searchResultList.get(index);
		String[] info = selectedFile.split(" ");
		// info[0] == file name
		//info[1] == file size
		
		byte[] file = new byte[Integer.parseInt(info[1])];//accessed by multiple Threads
		
		List<String> ipsAndPorts = getIpsAndPortsWhereFileExists(info[0], info[1]);// who i'm going ask for fileBlocks
		
		FileBloksQueue<FileBlockRequest> fileBlocksQueue = getFileBloksInfo(info[0],Integer.parseInt(info[1]));//accessed by multiple Threads
		
		downloadFileBlocks(ipsAndPorts,fileBlocksQueue, file, progressJProgressBar, BLOCK_SIZE* (Integer.parseInt(info[1]) / BLOCK_SIZE),info[0]);		
	}

	private List<String> getIpsAndPortsWhereFileExists(String fileName, String fileSize) {
		List<String> ipsAndPorts = new ArrayList<String>(); // port + Ip of who had the file

		for (String line : searchResultList) {
			String[] lineInfo = line.split(" ");
			if (lineInfo[0].equals(fileName) && lineInfo[1].equals(fileSize)) {//condition to see if 2 files are the same
				ipsAndPorts.add(lineInfo[2] + " " + lineInfo[3]);//maybe change this in the future
			}
		}
		return ipsAndPorts;
	}
	
	private FileBloksQueue<FileBlockRequest> getFileBloksInfo(String fileName, int fileSize) {
		FileBloksQueue<FileBlockRequest> fileBlocksQueue = new FileBloksQueue<FileBlockRequest>();
		int parts = fileSize / BLOCK_SIZE;// must round down
		int lastpart = fileSize - (BLOCK_SIZE * parts); // int  type does that round down thing 13.86 -> 13 blocks

		for (int i = 0; i < parts; i++) {
			fileBlocksQueue.add(new FileBlockRequest(fileName, BLOCK_SIZE, BLOCK_SIZE * i));
		}
		if (lastpart > 0) {
			fileBlocksQueue.add(new FileBlockRequest(fileName, lastpart, BLOCK_SIZE * (parts)));
		}

		return fileBlocksQueue;
	}
	
	
	/*
	 * Barrier:
	 * all tasks write on the file
	 * 
	 * another big boy task has to wait to write the file
	 * 
	 */
	private void downloadFileBlocks(List<String> ipsAndPortsWhereFileExists, FileBloksQueue<FileBlockRequest> fileBlocksQueue, byte[] file,
			JProgressBar progressJProgressBar, int lastBlockBeginning, String fileName) {
		int progress = ((1/fileBlocksQueue.getSize())*100);//progress value of each block
		for (String client:ipsAndPortsWhereFileExists) {// ask all users(witch have the file) for the file blocks
			Runnable task = new Runnable() {// starting a thread per connection -> kinda good idea
				@Override
				public void run() {
					String[] clientFields = client.split(" ");
					try {
						FileRequest fileRequest = new FileRequest(
								new Socket(clientFields[0], Integer.parseInt(clientFields[1])));
						fileRequest.requestFileBlocks(fileBlocksQueue, file, progressJProgressBar,progress, lastBlockBeginning);
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
			};
			pool.submit(task);
		}
		
		
		//when is done!
		try {
			fileBlocksQueue.waitBlocks();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			Files.write(Paths.get(username + "/" + fileName), file); // 1
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	public static void main(String[] args) {
		for (int i = 1; i <= 3; i++) {
			Client user = new Client("User_" + i, 8000 + i, "127.0.0.1", 8080, 4);// 127.0.0.1 localhost
			GraphicInterface gui = new GraphicInterface(user);// 8000 port, but could be any port
		}

	}

}