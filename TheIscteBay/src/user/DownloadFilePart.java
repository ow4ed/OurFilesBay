package user;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DownloadFilePart extends Thread{

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private byte[] file;
	private BlockingQueue<FileBlockRequestMessage> bq;
	private int blocksDownloaded;
	private Socket socket;

	public DownloadFilePart(Socket socket, BlockingQueue<FileBlockRequestMessage> bq, byte[] file, int blocksDownloaded) throws IOException{
		this.bq = bq;
		this.file = file;
		this.setBlocksDownloaded(blocksDownloaded);
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
		this.socket = socket;
	}
	
	@Override
	public void run() {
		while(bq.size() > 0){
			FileBlockRequestMessage fbrm;
			try {
				fbrm = bq.poll();
				System.out.println("Vou escrever a fbrm para o socket do user");
				oos.writeObject(fbrm);
				FilePart fp = (FilePart) ois.readObject();
				synchronized (file) {	
					System.arraycopy(fp.getFileContents(), 0, file, fp.getStart(), fp.getSize());
					setBlocksDownloaded(getBlocksDownloaded() + 1);
			//		notifyAll();
				}
			} catch (InterruptedException | IOException | ClassNotFoundException e) {
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