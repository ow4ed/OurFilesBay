package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
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
	private final static int QUEUE_SIZE = 100000;
	
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
		Runnable download = new Runnable() {
			@Override
			public void run() {
				String selectedFile = searchResultList.get(index);
				String[] info = selectedFile.split(" ");
				// info[0] == file name
				// info[1] == file size

			//	byte[] file = new byte[Integer.parseInt(info[1])];// accessed by multiple Threads

				List<String> ipsAndPorts = getIpsAndPortsWhereFileExists(info[0], info[1]);// who i'm going ask for
																							// fileBlocks

				//have multiple of those!
			//	int numberOfQueues = (int) (Long.parseLong(info[1])/QUEUE_SIZE);//queueMaxSize = 100k
			//	int lastQueue = (int) (Long.parseLong(info[1])%QUEUE_SIZE);
				//this way i can manage the total memory spend for very large files
				
				
				
				//i have to limit the size of this ?
				//for very big files yes! (> 500 GB ? )
				
				
				try {
					downloadFileBlocks(ipsAndPorts, progressJProgressBar,
							BLOCK_SIZE *((int) (Long.parseLong(info[1]) / BLOCK_SIZE)), info);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		pool.submit((download));
	}

	private List<String> getIpsAndPortsWhereFileExists(String fileName, String fileSize) {//String string, good ideia
		List<String> ipsAndPorts = new ArrayList<String>(); // port + Ip of who had the file

		for (String line : searchResultList) {
			String[] lineInfo = line.split(" ");
			if (lineInfo[0].equals(fileName) && lineInfo[1].equals(fileSize)) {//condition to see if 2 files are the same
				ipsAndPorts.add(lineInfo[2] + " " + lineInfo[3]);//maybe change this in the future
			}
		}
		return ipsAndPorts;
	}
	
	private FileBloksQueue<FileBlockRequest> getFileBloksInfo(String[] info, int numberOfBlocks, long beginning) {
		FileBloksQueue<FileBlockRequest> fileBlocksQueue = new FileBloksQueue<FileBlockRequest>();
		long lastPartSize = Long.parseLong(info[1]) -
				( beginning  +      
						((long) BLOCK_SIZE * (numberOfBlocks-1)));
			
				  
                           
		for (int i = 0; i < numberOfBlocks-1; i++) {//not including last block
			fileBlocksQueue.add(new FileBlockRequest(info[0], BLOCK_SIZE,beginning+((long) BLOCK_SIZE * i)));//file begining
		}
		
 		if (lastPartSize < BLOCK_SIZE) {
			fileBlocksQueue.add(new FileBlockRequest(info[0],(int) lastPartSize,(beginning+((long) BLOCK_SIZE * (numberOfBlocks-1)))));
		}
		else {
			fileBlocksQueue.add(new FileBlockRequest(info[0], BLOCK_SIZE,beginning+((long) BLOCK_SIZE * (numberOfBlocks-1))));
		}
		

		return fileBlocksQueue;
	}
	
	
	/*
	 * Barrier:
	 * all tasks write on the file
	 * 
	 * another big boy task has to wait to write the file
	 * 32 049 152
	 */
	private void downloadFileBlocks(List<String> ipsAndPortsWhereFileExists,
			JProgressBar progressJProgressBar, int lastBlockBeginning, String[] info) throws IOException {
		
		long numberOfBlocks = Long.parseLong(info[1]) / BLOCK_SIZE;// must round down    31 298,73642 -> 31298

		if((Long.parseLong(info[1]) % BLOCK_SIZE)>0) {
			numberOfBlocks = numberOfBlocks +1 ;
		}
		
		
		
		
		
		int size = QUEUE_SIZE;
		long beginning = 0;
		while (numberOfBlocks>0) {// lastQueue included
			if(numberOfBlocks<QUEUE_SIZE)
				size = (int) numberOfBlocks;
			
			FileBloksQueue<FileBlockRequest> fileBlocksQueue = getFileBloksInfo(info, size, beginning);// accessed
			
			beginning = beginning + ((int)(QUEUE_SIZE*BLOCK_SIZE));
		
																													// multiple
																													// Threads

			int progress = ((int) ((1 / ((double) fileBlocksQueue.getSize())) * 10000000));// progress value of each
																							// block
			File f = new File(
					"C:\\Users\\L3g4c\\git\\OurFilesBay\\OurFilesBay - Client\\" + username + "\\" + info[0]);
			for (String client : ipsAndPortsWhereFileExists) {// ask all users(witch have the file) for the file blocks
				Runnable task = new Runnable() {// starting a thread per connection -> kinda good idea
					@Override
					public void run() {
						String[] clientFields = client.split(" ");
						try {
							FileRequest fileRequest = new FileRequest(
									new Socket(clientFields[0], Integer.parseInt(clientFields[1])));
							fileRequest.requestFileBlocks(fileBlocksQueue, f, progressJProgressBar, progress,
									lastBlockBeginning);
						} catch (NumberFormatException | IOException e) {
							e.printStackTrace();
						}
					}
				};
				pool.submit(task);
			}

			// when is done!

			try {
				fileBlocksQueue.waitBlocks(); // 1 queue at the time!!!
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			numberOfBlocks = numberOfBlocks-QUEUE_SIZE;

		}
		
		
		
		updateUserFilesJList();
		
		
	}

	public static void main(String[] args) {
		for (int i = 1; i <= 3; i++) {
			Client user = new Client("User_" + i, 8000 + i, "127.0.0.1", 8080, 4);// 127.0.0.1 localhost
			GraphicInterface gui = new GraphicInterface(user);// 8000 port, but could be any port
		}

	}

}