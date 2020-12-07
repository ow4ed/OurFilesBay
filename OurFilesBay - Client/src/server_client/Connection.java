package server_client;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import coordination_structures_client.ThreadPool;
import serializable_objects.FileBlockRequest;
import serializable_objects.FileBlock;
import serializable_objects.UserFilesDetails;
import serializable_objects.WordSearchMessage;

public class Connection implements Runnable {
	private Socket socket = null;
	private ObjectOutputStream objectOutputStream = null;
	private ObjectInputStream objectInputStream = null;
	private ThreadPool pool;
	private String username;
	private String path;

	public Connection(Socket socket,ThreadPool pool, String username, String path) {
		this.socket = socket;
		this.pool = pool;
		this.username = username;
		this.path = path;
	}
	
	@Override
	public synchronized void run() {
		doConnections();
		try {
			Object o = objectInputStream.readObject();
			//types of requests another client can make to the Client Server
			if (o instanceof WordSearchMessage) {// if is instance of WrodSearchMessage is done whit whit the o, dosen't check it again
				sendFilesInfo((WordSearchMessage) o);
			} else {
				if(o instanceof FileBlockRequest) {
					sendFileBlocks((FileBlockRequest) o);
				}
			}
		} catch (ClassNotFoundException | IOException e) { 
			e.printStackTrace();
		}
	}
	
	private void doConnections() {
		System.out.println(username+" - an user has has request an connection:" +socket+"(socket)");
		try {
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());//first we need to create out
			objectInputStream = new ObjectInputStream(socket.getInputStream());//after creating out we can create in
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeConnections() {
		try {
			objectOutputStream.close();
			objectInputStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendFilesInfo(WordSearchMessage message) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				File[] files = findFiles(message.getMessage());// findFiles here bad
				UserFilesDetails answer = new UserFilesDetails(username, socket.getLocalAddress().getHostAddress(),socket.getLocalPort(), files);
				try {
					try {//delete, lag simulation
						System.out.println("SLEEPY WIPPY TIME");
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					}//delete, lag simulation
					objectOutputStream.writeObject(answer);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					closeConnections();
				}
			}
		};
		pool.submit(task);
	}
	
	private void sendFileBlocks(Object o) {
		while ((o instanceof FileBlockRequest) || o!=null) {//i have an while cycle, is good idea to have a thread pool
			System.out.println(username+" -  Enviei um File Block!");
			sendFileBlock((FileBlockRequest) o);
			
			try {
				o = objectInputStream.readObject();//while "iterator" 
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
		closeConnections();//my only persistent connection!(careful when i close it on the other side)
	}
	
	private void sendFileBlock(FileBlockRequest fileBlockRequest) {
		Runnable task = new Runnable() {//imagine 100 connection threads, doing a while cycle at the same time
			@Override
			public void run() {
				try {//it dosen't look optimal but is what i want, i want to actually check if i'm sending the right block, imagine the client deletes the file
					FileBlock fileBlock = new FileBlock(fileBlockRequest.getBeginning(),fileBlockRequest.getSize(),
							copyBytesToArray(new File(path+fileBlockRequest.getFileName()),fileBlockRequest.getSize(),fileBlockRequest.getBeginning()));
					objectOutputStream.writeObject(fileBlock);
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		};
		pool.submit(task);
	}
	
	private File[] findFiles(String keyword) {// need to thing about, where to place this method
		File[] files = new File(path).listFiles(new FileFilter() {//needs to be changed 
			public boolean accept(File f) {
				return f.getName().contains(keyword);
			}
		});
		return files;
	}
	
	public byte[] copyBytesToArray(File file,int blockSize, long blockBeginning) throws IOException {

		RandomAccessFile aFile = new RandomAccessFile(file, "r");
		FileChannel inChannel = aFile.getChannel();
		ByteBuffer buf = ByteBuffer.allocate(blockSize);
		buf.clear();
		
		inChannel.position(blockBeginning);
		inChannel.read(buf);
		
		byte[] arr = new byte[blockSize];
		System.arraycopy(buf.array(), 0, arr, 0, blockSize);
			
		inChannel.close();
		aFile.close();
		return arr;
	}
    
}
