package client_connection_requests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.swing.JProgressBar;

import coordination_structures_client.FileBloksQueue;
import serializable_objects.FileBlockRequest;
import serializable_objects.FileBlock;

public class FileRequest extends ClientToClient {
	
	public FileRequest(Socket socket) {
		super(socket);

	}

	public void requestFileBlocks(FileBloksQueue<FileBlockRequest> fileBlocksQueue, File file, JProgressBar progressJProgressBar, int progress, int lastBlockBeginning) {//blocks isn't thread safe right now
		super.doConnections();
		try {
			
			FileBlockRequest fileBlockRequest;
			while ((fileBlockRequest = fileBlocksQueue.take()) != null) {//persistent connection
				Thread.sleep(10);//simulate lag
				super.getObjectOutputStream().writeObject(fileBlockRequest);
				
				FileBlock fileBlock = (FileBlock) super.getObjectInputStream().readObject();
				
				byte[] block = fileBlock.getFileBlock();
	
				synchronized (file) {
					writeBytesArrayToFile(file,block,fileBlock.getBeginning());
				}
				
				synchronized(progressJProgressBar) {
			
					
					int newVal = progress + progressJProgressBar.getValue();
					int big = newVal/100000;//it already rounds down!
					int small = newVal-(big*100000);

					progressJProgressBar.setString(big+"."+small+"%");
					progressJProgressBar.setValue(newVal);
				}
				
				if(fileBlock.getBeginning() == lastBlockBeginning) {//we actually need this
					fileBlocksQueue.Done();//guy needs to update files in user folder!
					progressJProgressBar.setString(0+"%");
					progressJProgressBar.setValue(0);
				}
				


			}
			
			super.getObjectOutputStream().writeObject(null);//condition to stop the while cycle 
			//in server connection
			
		} catch (InterruptedException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			super.closeConnections();
		}
	}

		       		       
	public void writeBytesArrayToFile(File file, byte[] data, long off) throws IOException {
		RandomAccessFile aFile = new RandomAccessFile(file, "rw");
		FileChannel ch = aFile.getChannel();
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(data.length);		
		byteBuffer.clear();
		byteBuffer.put(data);
		byteBuffer.flip();

		ch.position(off);
		while (byteBuffer.hasRemaining()) {
			ch.write(byteBuffer);
		}

		ch.close();
		aFile.close();

	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}