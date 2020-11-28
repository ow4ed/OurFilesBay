package user;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientToClient extends Thread{
	
	private Socket socket;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	private FileDetails wsmResponse;
	private FilePart filePart;
	
	public ClientToClient(Socket socket, Object msg) throws IOException {
		this.socket = socket;
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
		oos.writeObject(msg);
	}
	@Override
	public synchronized void run() {
		System.out.println("Resposta do Client Server:");
		try {
			Object o = ois.readObject();
			if(o instanceof FileDetails) { //se o que recebi for um FileDetails
				wsmResponse = (FileDetails)o;
				socket.close();
				System.out.println("Conecção encerrada!");
			}
			
		} catch (ClassNotFoundException | IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}	
	}
	
	public FileDetails getFileDetails(){
		return wsmResponse;
	}
	public FilePart getFilePart() {
		return filePart;
	}	
}