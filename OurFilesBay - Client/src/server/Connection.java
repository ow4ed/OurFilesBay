package server;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;

import coordination_structures.ThreadPool;
import serializable_objects.FileBlockRequest;
import serializable_objects.FilePart;
import serializable_objects.UserFilesDetails;
import serializable_objects.WordSearchMessage;

public class Connection implements Runnable {
	private Socket socket = null;
	private ObjectOutputStream objectOutputStream = null;
	private ObjectInputStream objectInputStream = null;
	private ThreadPool pool;
	private String username;

	public Connection(Socket socket,ThreadPool pool, String username) {
		this.socket = socket;
		this.pool = pool;
		this.username = username;
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
					try {//delete lag simulation
						System.out.println("SLEEPY WIPPY TIME");
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					}
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
		while (o instanceof FileBlockRequest) {//i have an while cycle, is good idea to have a thread pool
			sendFileBlock((FileBlockRequest) o);
			
			try {
				o = objectInputStream.readObject();//while "iterator" - may be dangerous!
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
		closeConnections();//my only persistent connection!(careful when i close it on the other side)
	}
	
	private void sendFileBlock(FileBlockRequest fileBlock) {
		Runnable task = new Runnable() {//imagine 100 connection threads, doing a while cycle at the same time
			@Override
			public void run() {
				File[] files = findFiles(fileBlock.getFileName());//maybe this isn't optimal, view the function for changes
				try {//it dosen't look optimal but is what i want, i want to actually check if i'm sending the right block, imagine the client deletes the file
					byte[] fileContents = Files.readAllBytes(files[0].toPath());//while his server is sending file blocks 
					FilePart filepart = new FilePart(fileContents, fileBlock.getSize(), fileBlock.getOffset());
					objectOutputStream.writeObject(filepart);
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}	
		};
		pool.submit(task);
	}
	
	private File[] findFiles(String keyword) {// need to thing about, where to place this method
		File[] files = new File(username).listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().contains(keyword);
			}
		});
		return files;
	}
}
