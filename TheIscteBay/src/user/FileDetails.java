package user;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileDetails implements Serializable{

	private static final long serialVersionUID = -6085430478278137543L;
	private File[] files;
	private String userName;
	private List<String> fileName;
	private List<Integer> fileSize;
	private int port;
	private String ip;
 	
	public FileDetails(File[] files,String userName, int port, String ip) {//surce e um porto
		this.files = files;
		this.userName = userName;
		this.port = port;
		this.ip = ip;
		listFiles(userName, port, ip);
	}
	
	public void listFiles(String userName,int port,String ip) {// o user tem verios files
		this.fileName = new ArrayList<String>();
		this.fileSize = new ArrayList<Integer>();
		for(int i = 0;i<this.files.length;i++) {
			this.fileName.add(files[i].getName());
			this.fileSize.add((int)files[i].length());
			
		}
	}

	public String getUserName() {
		return userName;
	}

	public List<String> getFileName() {
		return fileName;
	}

	public List<Integer> getFileSize() {
		return fileSize;
	}

	public int getPort() {
		return port;
	}

	public String getIp() {
		return ip;
	}

}