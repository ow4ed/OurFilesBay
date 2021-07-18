package client.models.requests;

import java.io.Serializable;

public class FileBlockRequest implements Serializable{

	private static final long serialVersionUID = -1059595226808527208L;
	
	// fileName and size are used for file identification
	private String fileName; 
	private int size; 
	private long beginning;
	
	public FileBlockRequest(String fileName, int size, long beginning){
		this.fileName = fileName;
		this.size = size;
		this.beginning = beginning;
	}

	public String getFileName() {
		return fileName;
	}

	public int getSize() {
		return size;
	}

	public long getBeginning() {
		return beginning;
	}

}
