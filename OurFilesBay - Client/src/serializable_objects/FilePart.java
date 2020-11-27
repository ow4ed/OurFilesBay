package serializable_objects;

import java.io.Serializable;

public class FilePart implements Serializable{ 

	private static final long serialVersionUID = -8732561272597480081L;
	
	private byte[] fileContents;
	private int size;
	private int start;
	
	public FilePart(byte[] fileContents, int size, int start) {
		this.fileContents = fileContents;
		this.size = size;
		this.start = start;
	}

	public int getStart() {
		return start;
	}

	public int getSize() {
		return size;
	}	
	
	public byte[] getFileContents() {
		return fileContents;
	}
	
}