package user;

import java.io.Serializable;

public class FilePart implements Serializable{ 

	private static final long serialVersionUID = -8732561272597480081L;
	private byte[] fileContents;
	private int size;
	private int start;
	
	public FilePart(byte[] fileContents, int size, int start) {
		this.size = size;
		this.start = start;
		this.fileContents = fileContents;
	}

	public byte[] getFileContents() {
        byte[] result = new byte[this.size];
        System.out.println("start"+start);
        System.arraycopy(fileContents, start, result, 0, size);
        return result;
    }

	public int getStart() {
		return start;
	}

	public int getSize() {
		return size;
	}	
}