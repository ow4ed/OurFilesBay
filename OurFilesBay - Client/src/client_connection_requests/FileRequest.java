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

	public void requestFileBlocks(FileBloksQueue<FileBlockRequest> fileBlocksQueue, File file, JProgressBar progressJProgressBar, int progress, long lastBlockBeginning) {//blocks isn't thread safe right now
		super.doConnections();
		try {
			
			int newVal;
			int big;
			int small;
			
			byte[] blockData;
			FileBlock fileBlock;
			FileBlockRequest fileBlockRequest;
			
			while ((fileBlockRequest = fileBlocksQueue.take()) != null) {//persistent connection
			//	Thread.sleep(10);//simulate lag
				super.getObjectOutputStream().writeObject(fileBlockRequest);
				
				fileBlock = (FileBlock) super.getObjectInputStream().readObject();
				//
				
				
				blockData = fileBlock.getFileBlock();
				
				synchronized (file) {
					writeBytesArrayToFile(file,blockData,fileBlock.getBeginning());
				}
				
				
				
				
				
				
				
				
				
				
				
				////
				synchronized(progressJProgressBar) {
					newVal = progress + progressJProgressBar.getValue();
					big = newVal/100000;//it already rounds down!
					small = newVal-(big*100000);

					progressJProgressBar.setString(big+"."+small+"%");
					progressJProgressBar.setValue(newVal);
				}
				
				
				if(fileBlocksQueue.getSize() == 0){// is already sync method 
					fileBlocksQueue.Done();//queue is empty
				}
				
				if(fileBlock.getBeginning() == lastBlockBeginning) {//we actually need this //only 1 thread enters this if statement
						progressJProgressBar.setString(100+"%");//visual update
						progressJProgressBar.setString(0+"%");
						progressJProgressBar.setValue(0);
						System.out.println("@Is donee");
						System.out.println("last block beginning was:"+lastBlockBeginning);
				}

				

			}
			
			super.getObjectOutputStream().writeObject(null);//condition to stop the while cycle 
			//in server connection
			
		} catch (/*InterruptedException |*/ IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			super.closeConnections();
		}
	}

		       		       
	public void writeBytesArrayToFile(File f, byte[] data, long off) throws IOException {
		
		RandomAccessFile aFile = new RandomAccessFile(f, "rw");
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