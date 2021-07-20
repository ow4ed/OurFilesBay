package client.server;

import java.io.File;
import java.io.Serializable;

public class SearchResponse implements Serializable{

	private static final long serialVersionUID = -6085430478278137543L;
	
	private String userName;
	private String ip;
	private int port;
	
	private File[] files;
 	
	public SearchResponse(String userName, String ip, int port,File[] files) {
		this.files = files;
		this.userName = userName;
		this.ip = ip;
		this.port = port;
	}
	
	public String getUserName() {
		return userName;
	}

	public String getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public File[] getFiles() {
		return files;
	}
	
}

