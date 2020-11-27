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
		doConnections(socket);
		
		try {
			Object o = objectInputStream.readObject();
			
			if (o instanceof WordSearchMessage) {// if is instance of WrodSearchMessage  is done whit whit the o, dosen't check it again
				WordSearchMessage message = (WordSearchMessage) o;
				Runnable task = new Runnable() {
					@Override
					public void run() {
						File[] files = findFiles(message.getMessage());// findFiles here bad
						UserFilesDetails answer = new UserFilesDetails(username, socket.getLocalAddress().getHostAddress(),socket.getLocalPort(), files);
						try {
							objectOutputStream.writeObject(answer);
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								socket.close();
								objectOutputStream.close();
								objectInputStream.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				};
				pool.submit(task);
			} else {
				while (o instanceof FileBlockRequest) {//i have an while cycle, is good idea to have a thread pool
					FileBlockRequest fileBlock = (FileBlockRequest) o;
					Runnable task = new Runnable() {//imagine 100 connection threads, doing a while cycle at the same time
						@Override
						public void run() {
							File[] files = findFiles(fileBlock.getFileName());
							try {
								byte[] fileContents = Files.readAllBytes(files[0].toPath());
								FilePart filepart = new FilePart(fileContents, fileBlock.getSize(), fileBlock.getOffset());
								objectOutputStream.writeObject(filepart);
							} catch (IOException e) {
								e.printStackTrace();
							} 
						}	
					};
					pool.submit(task);//i'm just adding a task in a list of things to process at a controllable paste.
					//this way i'm like responding at 5 request at the time, and not 100 :)
					o = objectInputStream.readObject();//while cycle condition
					
					//where do i close scket , in and out ?
					
					
				}
			}
		} catch (ClassNotFoundException|IOException e) { //always gives me IOException
			e.printStackTrace();
		}
	}
	
	private void doConnections(Socket socket) {
		System.out.println(username+" - an user has has request an connection:" +socket+"(socket)");
		try {
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());//first we need to create out
			objectInputStream = new ObjectInputStream(socket.getInputStream());//after creating out we can create in
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private File[] findFiles(String keyword) {
		File[] files = new File(username).listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().contains(keyword);
			}
		});
		return files;
	}
}
