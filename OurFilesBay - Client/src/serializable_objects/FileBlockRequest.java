package serializable_objects;

import java.io.Serializable;

public class FileBlockRequest implements Serializable{

	private static final long serialVersionUID = -1059595226808527208L;
	
	private String fileName;
	private int size;
	private int offset;
	
	public FileBlockRequest(String fileName, int size, int offset){
		this.fileName = fileName;
		this.size = size;
		this.offset = offset;
	}

	public String getFileName() {
		return fileName;
	}

	public int getSize() {
		return size;
	}

	public int getOffset() {
		return offset;
	}

}
