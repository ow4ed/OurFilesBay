package client_connection_requests;

import java.io.IOException;
import java.net.Socket;

import javax.swing.JProgressBar;

import coordination_structures.BlockingQueue;
import serializable_objects.FileBlockRequest;
import serializable_objects.FilePart;

public class FileRequest extends ClientToClient implements Runnable{

	private byte[] file;
	private BlockingQueue<FileBlockRequest> bq;
	private int blocksDownloaded;
	private JProgressBar progressJProgressBar;
	private int totalBlocks;

	public FileRequest(Socket socket, BlockingQueue<FileBlockRequest> bq, byte[] file, int blocksDownloaded, JProgressBar progressJProgressBar) {
		super(socket);
		
		this.bq = bq;
		this.totalBlocks = bq.size();
		this.file = file;
		this.blocksDownloaded = blocksDownloaded;
		this.progressJProgressBar = progressJProgressBar;
	}
	
	@Override
	public void run() {
		super.doConnections();
		
		try {
			while (bq.size() > 0) {
				double kindaInutil;
				double total = totalBlocks;
				double blocksIHave;
				FileBlockRequest fbrm = bq.take();
				System.out.println("Vou escrever a fbrm para o socket do user");
				super.getObjectOutputStream().writeObject(fbrm);
				FilePart fp = (FilePart) super.getObjectInputStream().readObject();
				synchronized (file) {
					//
					byte[] result = new byte[fp.getSize()];
					System.arraycopy(fp.getFileContents(), fp.getStart(), result, 0, fp.getSize());
					//
					System.arraycopy(result, 0, file, fp.getStart(), fp.getSize());
					blocksDownloaded = blocksDownloaded + 1;
					blocksIHave = blocksDownloaded;

					Thread.sleep(10);
				}
				kindaInutil = (blocksIHave / total) * 100;
				progressJProgressBar.setValue((int) kindaInutil);

			}
		} catch (InterruptedException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				super.getScoket().close();
				super.getObjectInputStream().close();
				super.getObjectOutputStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}

	public synchronized int getBlocksDownloaded() {
		return blocksDownloaded;
	}

	public synchronized void setBlocksDownloaded(int blocksDownloaded) {
		this.blocksDownloaded = blocksDownloaded;
	}

}