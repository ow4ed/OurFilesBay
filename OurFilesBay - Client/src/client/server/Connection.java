package client.server;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import client.models.requests.FileBlockRequest;
import client.models.requests.SearchRequest;

public class Connection implements Runnable {

	private Socket socket;
	private String userName;
	private String userFileSystemPath;

	public Connection(Socket socket, String userName, String userFileSystemPath) {
		this.socket = socket;
		this.userName = userName;
		this.userFileSystemPath = userFileSystemPath;
	}
	
	@Override
	public void run() {
		System.out.println("Client "+ userName +" - Connection - start");
		
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		RandomAccessFile aFile = null;
		FileChannel inChannel = null;
		
		try {
			
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
			// get request Object
			Object o = in.readObject();
			
			// parse request
			if (o instanceof SearchRequest) {
				
				parseSearchRequest((SearchRequest) o, out);
				
			} else if (o instanceof FileBlockRequest){
				
				// get file channel for requested file
				try {
					aFile = new RandomAccessFile(userFileSystemPath + ((FileBlockRequest) o).getFileName(), "r");
					inChannel = aFile.getChannel();
					
				} catch (FileNotFoundException e) {
					// TODO:file dosen't exit anymore, //TODO generate a special response Type
					System.out.println("Client " + userName + " - parseFileBlockRequest - got an request for file '"
							+ ((FileBlockRequest) o).getFileName() + "' witch dosen't exits anymore");
					return;
				}

				parseFileBlockRequest((FileBlockRequest) o, out, in, inChannel);
				
			}
		} catch (ClassNotFoundException | IOException | InterruptedException e) { 
			e.printStackTrace();
		} finally {

			try {
				// stop
				out.close();
				in.close();
				socket.close();
				aFile.close();
				inChannel.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		System.out.println("Client "+ userName +" - Connection - stop");
	}

	private void parseSearchRequest(SearchRequest request, ObjectOutputStream out) throws InterruptedException, IOException {
		
		File[] files = findFiles(request.getKeyWord());
	
		SearchResponse response = new SearchResponse(userName, socket.getLocalAddress().getHostAddress(),socket.getLocalPort(), files);
				
		//TODO:delete > lag simulation
		System.out.println("DIzYYY");
		Thread.sleep(2000);
					
		out.writeObject(response);
	
	}
	
	private File[] findFiles(String keyWord) {
		File[] files = new File(userFileSystemPath).listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().contains(keyWord);
			}
		});
		return files;
	}

	private void parseFileBlockRequest(FileBlockRequest request, ObjectOutputStream out, ObjectInputStream in, FileChannel inChannel)
			throws IOException, ClassNotFoundException {

		int blockSize = request.getSize();
		ByteBuffer buf = ByteBuffer.allocate(blockSize);
		byte[] data = new byte[blockSize];

		// TODO:confirm if this is acting as an while or an if
		while (null != request && (request instanceof FileBlockRequest)) {

			// check if we are parsing the last block of the file
			if (request.getSize() != blockSize) {
				blockSize = request.getSize();
				// allocate memory
				buf = ByteBuffer.allocate(blockSize);
				data = new byte[blockSize];
			}

			long beginning = request.getBeginning();

			// copy file block bytes to array
			buf.clear();
			inChannel.position(beginning);
			inChannel.read(buf);
			System.arraycopy(buf.array(), 0, data, 0, blockSize);

			// generate response object
			FileBlockResponse fileBlock = new FileBlockResponse(beginning, blockSize, data);

			// send response
			out.writeObject(fileBlock);
			out.reset();

			System.out.println("Client " + userName + " - parseFileBlockRequest - Sent file block [" + beginning + "-"
					+ (beginning + blockSize) + "] for file:" + request.getFileName());

			// while "iterator"
			request = (FileBlockRequest) in.readObject();
		}

	}

}
