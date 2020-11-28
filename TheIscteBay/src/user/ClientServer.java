package user;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class ClientServer extends Thread {

	private int port;
	private String folder;
	private String path;
	private ServerSocket serverSocket;
	private ThreadPool pool;

	public ClientServer(int PORT, String folder, String path) {
		this.port = PORT;
		this.folder = folder;
		this.path = path;
		pool = new ThreadPool(5);
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println(folder + " Says Server Started ");

			while (!interrupted()) {
				System.out.println(folder + " Says server in Standbay...");
				Socket clientSocket = serverSocket.accept();
				ConnectionThread connection = new ConnectionThread(clientSocket, path, folder, pool);
				connection.start();
			}
		} catch (IOException e) {// um pouco mal , porque se houver uma coneção mal sucedida o servidor fecha a
									// sua soket
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Server Closed ;(");
				e.printStackTrace();
			}
		}
	}

	static class ConnectionThread extends Thread {
		private Socket socket;
		private ThreadPool pool;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private String userPath;
		private String userName;

		public ConnectionThread(Socket socket, String userPath, String userName, ThreadPool pool) {
			this.socket = socket;
			this.pool = pool;
			this.userPath = userPath;
			this.userName = userName;
			System.out.println("Entrou um user" + socket);
			try {
				oos = new ObjectOutputStream(socket.getOutputStream());
				ois = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public synchronized void run() {
			System.out.println("Inicio de conecção");

			try {
				Object o = ois.readObject();
				if (o instanceof WordSearchMessage) {
					WordSearchMessage message;
					message = (WordSearchMessage) o;
					File[] files = findFiles(userPath, message.getMessage());
					FileDetails answer = new FileDetails(files, userName, socket.getLocalPort(),
							socket.getInetAddress().getHostAddress());// alterar
					oos.writeObject(answer);
					socket.close();
					System.out.println("Connection Closed");
				}
				if (o instanceof FileBlockRequestMessage) {
					FileBlockRequestMessage fbqm = (FileBlockRequestMessage) o;
					String name = fbqm.getFileName();
					int size = fbqm.getSize();
					int offset = fbqm.getOffset();
					Runnable r = new Runnable() {
						@Override
						public void run() {
							File[] files = findFiles(userPath, name); 
							try {
								byte[] fileContents = Files.readAllBytes(files[0].toPath());
								FilePart filepart = new FilePart(fileContents, size, offset);
								System.out.println("debug 47 o tamanho da FilePart que envio "
										+ filepart.getFileContents().length);
								oos.writeObject(filepart);
								System.out.println("Processo de resposta Ao Download Completo");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					};
					pool.submit(r);
					
					while (true) {
						 o = ois.readObject();
						fbqm = (FileBlockRequestMessage) o;
						String name2 = fbqm.getFileName();
						int size2 = fbqm.getSize();
						int offset2 = fbqm.getOffset();
						Runnable rr = new Runnable() {
							@Override
							public void run() {
								File[] files = findFiles(userPath, name2); 
								try {
									byte[] fileContents = Files.readAllBytes(files[0].toPath());
									FilePart filepart = new FilePart(fileContents, size2, offset2);
									System.out.println("debug 47 o tamanho da FilePart que envio "
											+ filepart.getFileContents().length);
									oos.writeObject(filepart);
									System.out.println("Processo de resposta Ao Download Completo");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						};
						pool.submit(rr);
						
					}
				}
			} catch (ClassNotFoundException e) {
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (IOException e) {
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}

		}

		public Socket getSocket() {
			return this.socket;
		}

		public ObjectOutputStream getOOS() {
			return this.oos;
		}

		public String getUserPath() {
			return this.userPath;
		}
	}

	private static File[] findFiles(String path, String keyword) {
		File[] files = new File(path).listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().contains(keyword);
			}
		});
		return files;
	}
}