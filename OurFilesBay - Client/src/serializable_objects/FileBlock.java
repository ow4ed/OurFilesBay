package serializable_objects;

import java.io.Serializable;

public class FileBlock implements Serializable{ 

	private static final long serialVersionUID = -8732561272597480081L;
	
	private long beginning;
	private int size;
	private byte[] fileBlock;

	/*
	public FileBlock(long beginning, int size, byte[] fileBlock) {
		this.beginning = beginning;
		this.size = size;
		this.fileBlock = fileBlock;
	}
	*/
	public FileBlock() {
	}

	public void setData(long beginning, int size, byte[] fileBlock) {
		this.beginning = beginning;
		this.size = size;
		this.fileBlock = fileBlock;
	}
	
	public long getBeginning() {
		return beginning;
	}

	public int getSize() {
		return size;
	}		
	
	public byte[] getFileBlock() {
		return fileBlock;
	}
	
}