package client_connection_requests;

import java.io.IOException;
import java.net.Socket;
import java.text.NumberFormat;

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
					//progress = 454  , max = 10k
					Thread.sleep(1000);
					
					System.out.println(Thread.currentThread().getName()+" - aqui esta o que quero adicionar a progress bar:" + progress);
					System.out.println(Thread.currentThread().getName()+" - previous progressBar value:" + progressJProgressBar.getValue());
					
					int newVal = progress + progressJProgressBar.getValue();
					int big = newVal/100;//it already rounds down!
					int small = newVal-(big*100);
					System.out.println(Thread.currentThread().getName()+" - value i'm tryng to set:"+newVal);
					
					//progressJProgressBar.setValue(newVal);
					//progressJProgressBar.setValue(progressJProgressBar.getValue()+progress);
					//progressJProgressBar.setString(NumberFormat.getPercentInstance().format(newVal/10));
					progressJProgressBar.setString(big+"."+small+"%");
					progressJProgressBar.setValue(newVal);
				}
				
				if(fileBlock.getBeginning() == lastBlockBeginning) {//we actually need this
					fileBlocksQueue.Done();//only when last part is written in file we can
					//write the full file where it needs to be written;
				}
				
				Thread.sleep(100);//simulate lag

			}
			
			super.getObjectOutputStream().writeObject(null);//condition to stop the while cycle 
			//in server connection
			
		} catch (InterruptedException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		//} finally {
		//	super.closeConnections();
		}
	}

}