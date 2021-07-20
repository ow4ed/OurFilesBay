package client.app;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JProgressBar;
import javax.swing.ListModel;

import client.gui.GraphicInterface;
import client.models.coordination.FileBloksQueue;
import client.models.coordination.ThreadPool;
import client.models.requests.FileBlockRequest;
import client.models.requests.FileRequest;
import client.models.requests.IpsAndPortsOfUsersConnectedRequest;
import client.models.requests.SearchRequest;
import client.models.requests.SignupRequest;
import client.server.Server;
import client.server.SearchResponse;



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

	private String username;
	private InetAddress userIp;
	private final int userPort;
	private String directoryIp;
	private int directoryPort;
	private ThreadPool pool;
	
	
	//awt.List is a List component used in GUI where as java.util.List is an interface for the lists data structure change this part
	private DefaultListModel<String> userFilesJList = new DefaultListModel<String>();  
	private DefaultListModel<String> searchResultJList = new DefaultListModel<String>();  
	private List<String> searchResultList = new ArrayList<String>();
	
	//BLOCK_SIZE*QUEUE_SIZE = +/- max memory usage ? 
	
	private final static int BLOCK_SIZE = 1024000;// * = 1 GB 
	private final static int QUEUE_SIZE = 100000;
//	private final static int BLOCK_SIZE = 1024; 
//	private final static int QUEUE_SIZE = 1000;
	
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
	
	public String getPath() {
		//return new String("C:\\Users\\L3g4c\\git\\OurFilesBay\\OurFilesBay - Client\\"+username+"\\");
		return "D:\\"+username+"\\";
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
			SignupRequest clientToDirectory = new SignupRequest(new Socket(directoryIp, this.directoryPort));
			if (clientToDirectory.signUpUser("INSC " + username + " " + userIp.getHostAddress() + " " + userPort)) {
				Server server = new Server(userPort, username, pool, getPath());
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
		SearchRequest wordSearchMessage = new SearchRequest(searchText);
		for (String user : ipsAndPortsOfUsersConnected) {
			String[] info = user.split(" ");
			try {//SearchRequest -> 1 Connection whit an user, the way i'm doing isn't optimal yet, imagine user_1 takes 1hour to give all his files info...
				SearchRequest clientToClient = new SearchRequest(new Socket(info[0], Integer.parseInt(info[1])));
				SearchRequest userFilesDetails = clientToClient.getUserFilesDetails( wordSearchMessage);
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
		File[] files = new File(getPath().toString()).listFiles();
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
				
				//1024000
				
				long lastBlockBeginning = BLOCK_SIZE * ((Long.parseLong(info[1]) / BLOCK_SIZE));//Round down is done already
				if((Long.parseLong(info[1]) % BLOCK_SIZE)==0) {
					lastBlockBeginning = lastBlockBeginning - 1;
				}
														
				try {
					downloadFileBlocks(ipsAndPorts, progressJProgressBar,
							lastBlockBeginning, info);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
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
	
	private void getFileBloksInfo(FileBloksQueue<FileBlockRequest> fileBlocksQueue, String[] info, int numberOfBlocks, long beginning) {
		fileBlocksQueue.clear();
		
		long lastPartSize = Long.parseLong(info[1]) -
				( beginning  +      
						((long) BLOCK_SIZE * (numberOfBlocks-1)));
			
				                    
		for (int i = 0; i < numberOfBlocks-1; i++) {//not including last block
			fileBlocksQueue.add(new FileBlockRequest(info[0], BLOCK_SIZE,beginning+((long) BLOCK_SIZE * i)));//file begining
		}
		
 		if (lastPartSize <((long) BLOCK_SIZE)) {
			fileBlocksQueue.add(new FileBlockRequest(info[0],(int) lastPartSize,(beginning+((long) BLOCK_SIZE * (numberOfBlocks-1)))));
		}
		else {
			fileBlocksQueue.add(new FileBlockRequest(info[0], BLOCK_SIZE,beginning+((long) BLOCK_SIZE * (numberOfBlocks-1))));
		}
	}
	
	
	/*
	 * I access the same file to send blocks to multiple users
	 * 
	 * But when i try to access the file whit windows explorer, to see its properties for example
	 *  - in users directory that are downloading it, i get an error// need to hide the file or make it inaccessible ?
	 *  - in the user witch is sending the blocks , (haven't been tested); 

	 */
	private void downloadFileBlocks(List<String> ipsAndPortsWhereFileExists,
			JProgressBar progressJProgressBar, long lastBlockBeginning, String[] info) throws IOException {
		
		long numberOfBlocks = Long.parseLong(info[1]) / BLOCK_SIZE;// must round down    31�298,73642 -> 31298

		if((Long.parseLong(info[1]) % BLOCK_SIZE)>0) {
			numberOfBlocks = numberOfBlocks +1 ;
		}
	
		int size = QUEUE_SIZE;
		long beginning = 0;
		
		int progress = (int) ((1 / (double) numberOfBlocks) * 10000000);// progress value of each block
		//floating point is a must!
		// block
		File f = new File(getPath()+info[0]);
		FileBloksQueue<FileBlockRequest> fileBlocksQueue = new FileBloksQueue<FileBlockRequest>();
		
		while (numberOfBlocks>0) {// not working as expected
			System.out.println("There gues 1 queue whit Max size! ("+QUEUE_SIZE*BLOCK_SIZE+" bytes)");
			
			if(numberOfBlocks<QUEUE_SIZE)
				size = (int) numberOfBlocks;
			
			getFileBloksInfo(fileBlocksQueue,info, size, beginning);//avoid large memory usage for large files

			
			//
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
				fileBlocksQueue.resetWaitBlocks();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			beginning = beginning + ((int)(QUEUE_SIZE*BLOCK_SIZE));
			numberOfBlocks = numberOfBlocks-QUEUE_SIZE;

		}

		System.out.println("DOWNLOAD IS COMPLETED!");
		updateUserFilesJList();
		
		
	}

	public static void main(String[] args) {
		for (int i = 1; i <= 3; i++) {
			Client user = new Client("User_" + i, 8000 + i, "127.0.0.1", 8080, 4);// 127.0.0.1 localhost
			GraphicInterface gui = new GraphicInterface(user);// 8000 port, but could be any port
		}

	}

}