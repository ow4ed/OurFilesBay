package client_connection_requests;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import javax.swing.JProgressBar;

import coordination_structures_client.FileBloksQueue;
import serializable_objects.FileBlockRequest;
import serializable_objects.FileBlock;

public class FileRequest extends ClientToClient {
	
	public FileRequest(Socket socket) {
		super(socket);

	}

	public void requestFileBlocks(FileBloksQueue<FileBlockRequest> fileBlocksQueue, byte[] file, JProgressBar progressJProgressBar, int progress, int lastBlockBeginning) {//blocks isn't thread safe right now
		super.doConnections();
		try {
			
			FileBlockRequest fileBlockRequest;
			while ((fileBlockRequest = fileBlocksQueue.take()) != null) {//persistent connection
			
				super.getObjectOutputStream().writeObject(fileBlockRequest);
				
				FileBlock fileBlock = (FileBlock) super.getObjectInputStream().readObject();
				
				byte[] block = fileBlock.getFileBlock();
				
				synchronized (file) {	
					//System.arraycopy(Object src,int srcPos,Object dest,int destPos,int length)
					System.arraycopy(block, 0, file, fileBlock.getBeginning(), fileBlock.getSize());
				
				}
				
				synchronized(progressJProgressBar) {
					progressJProgressBar.setValue(progressJProgressBar.getValue()+progress);
				}
				
				if(fileBlock.getBeginning() == lastBlockBeginning) {//we actually need this
					fileBlocksQueue.Done();//only when last part is written in file we can
					//write the full file where it needs to be written;
				}
				
				Thread.sleep(100);//simulate lag

			}
		} catch (InterruptedException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} //finally {
			//super.closeConnections();
	//	}
	}

}