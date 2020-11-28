package graphicinterface;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import user.BlockingQueue;
import user.ClientToClient;
import user.DownloadFilePart;
import user.FileBlockRequestMessage;
import user.FileDetails;
import user.User;
import user.WordSearchMessage;

public class GraphicInterface {
	
	private User user;
	private JFrame frame;
	private JTextField textField;
	private DefaultListModel<String> resultList;
	private JList<String> result;
	private JProgressBar progress;
	private static final int BLOCK_SIZE = 1024;
	private List<String> yellowPages;
	private List<String> yellowFileNames;
	private List<FileDetails> allInfo;
	private File image;
	private int blocksDownloaded;
	private int totalBloks;
	private byte[] file;
	
	public GraphicInterface(User user) throws IOException {
		
		this.image = findImageFiles(System.getProperty("user.dir"));

		this.user = user;

		this.allInfo = new ArrayList<FileDetails>();
		
		yellowPages = new ArrayList<String>();
		
		yellowFileNames = new ArrayList<String>();
		
		frame = new JFrame("Our Files" + user.getFolder());
		
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		frame.setLayout(new BorderLayout());
		
		addContent();
		
		frame.pack();
		
		open();
		
	}
	
	public void open() {
		frame.setVisible(true);
		frame.setResizable(false);
	}
	
	private void addContent() {
		
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel panelImage = new JPanel(new BorderLayout());
		JLabel  label = new JLabel();
		label.setHorizontalAlignment(JLabel.CENTER);
		ImageIcon icon = new ImageIcon(image.getName());
		label.setIcon(icon);
		panelImage.add(label);
		frame.add(panelImage, BorderLayout.NORTH);
		
		JLabel texto_a_procurar = new JLabel("Texto a procurar:");
		panel.add(texto_a_procurar, BorderLayout.LINE_START);
		
		textField = new JTextField();
		panel.add(textField, BorderLayout.CENTER);
		
		JButton procurar = new JButton("Procurar");
		panel.add(procurar, BorderLayout.LINE_END);
		procurar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {//user notify obersers o user vai observar esta clsse , depois quando receber o sinal esta mensagem criar uma WSM
				if(!textField.getText().isEmpty()) { // não fazer nada caso seija um espaço vazio
					resultList.clear();
					yellowPages.clear();
					yellowFileNames.clear();
					allInfo.clear();
					System.out.println("------------------- inicio procurar ---------------");
					WordSearchMessage search = new WordSearchMessage(textField.getText());
					try {
						List<String> conectedUsers = user.getUsersIpsPORTs();
						for(int i = 0; i < conectedUsers.size(); i++) {
							String[] info = conectedUsers.get(i).split(" ");
							System.out.println("Mensagem a Transmitir:"+ textField.getText());
							ClientToClient cTc = new ClientToClient(new Socket(info[0], Integer.parseInt(info[1])), search);
							cTc.start();
							try {
								cTc.join();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							allInfo.add(cTc.getFileDetails());
							
						}	
						addInfo();
						System.out.println("--------------------- end procurar --------------------");
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
				}
				
			}
		});
		
		frame.add(panel, BorderLayout.CENTER);
		
		JPanel panelListaDescaregarBarra = new JPanel(new BorderLayout());
		
		resultList = new DefaultListModel<String>();  
		result = new JList<String>(resultList);
		JScrollPane list = new JScrollPane(result);
		panelListaDescaregarBarra.add(list, BorderLayout.CENTER);
		result.addListSelectionListener(new ListSelectionListener() {
			private int previous = -1;

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (result.getSelectedIndex() != -1
						&& previous != result.getSelectedIndex()) {
					
					System.out.println(result.getSelectedValue());
				}
				previous = result.getSelectedIndex();
			}
		});
		
		JPanel panel2 = new JPanel(new BorderLayout());
		
		JButton descarregar = new JButton("Descarregar");
		descarregar.setPreferredSize(new Dimension(175, 80));
		panel2.add(descarregar, BorderLayout.PAGE_START);
		descarregar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(result.getSelectedValue()!=null) {
					blocksDownloaded = 0;
					int indice = result.getSelectedIndex();
					String s = yellowPages.get(indice);
					String[] info = s.split(" ");
					String fileName = yellowFileNames.get(indice);
					int fileSize = Integer.parseInt(info[0]);
					List<String> yellowPagesWithContent = new ArrayList<String>(); // porto + ip de quem tem
					for(FileDetails fd:allInfo) {
						for(int i = 0;i<fd.getFileName().size();i++) {
							if(fd.getFileName().get(i).equals(fileName) && fd.getFileSize().get(i)==fileSize) {
								yellowPagesWithContent.add(fd.getIp()+" "+fd.getPort());
							}
						}
					}
					System.out.println("ips + portos que me interessam:");
					for(String sick : yellowPagesWithContent) {
						System.out.println(sick);
					}
					System.out.println("nome do ficheiro selecionado e tamaho do mesmo: "+fileName+" "+fileSize );
					
					int parts = fileSize/BLOCK_SIZE; 
					int lastpart = fileSize-(BLOCK_SIZE*parts);
					BlockingQueue<FileBlockRequestMessage> blocks = new BlockingQueue<>();
					file= new byte[fileSize];
					for(int i = 0;i<parts;i++) {
						try {
							blocks.offer(new FileBlockRequestMessage(fileName, BLOCK_SIZE, BLOCK_SIZE*i));
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					
					if(lastpart>0) {
						try {
							blocks.offer(new FileBlockRequestMessage(fileName, lastpart, BLOCK_SIZE*(parts)));
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					
					
					totalBloks = blocks.size();
					Thread thread = new Thread(new Runnable() {
						
						@Override
						public void run() {
						//nao sei se e suposto ser assim, o blocksDownloaded e incrementado na classe do DownloadFilePart depois de ele escrever uma parte
						try {
							waitForDownload();
									
							Files.write(Paths.get(user.getFolder()+"/" + fileName), file);
						} catch (InterruptedException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							
							
						}

			
					});
					for(int i = 0; i < yellowPagesWithContent.size(); i++){
						String user = yellowPagesWithContent.get(i);
						String[] userFields = user.split(" ");
						
						DownloadFilePart dfp;
						try {
							dfp = new DownloadFilePart(new Socket(userFields[0], Integer.parseInt(userFields[1])), blocks, file, blocksDownloaded);
							dfp.start();
						} catch (NumberFormatException | IOException e1) {
							e1.printStackTrace();
						}
					}
					
					try {
						Thread.sleep(7000);
						Files.write(Paths.get(user.getFolder()+"/" + fileName), file);
					} catch (InterruptedException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					
					
					
				}
			}
		});
		
		progress = new JProgressBar();
		panel2.add(progress, BorderLayout.PAGE_END);
		progress.setPreferredSize(new Dimension(175, 80));
		
		panelListaDescaregarBarra.add(panel2, BorderLayout.EAST);
		frame.add(panelListaDescaregarBarra, BorderLayout.SOUTH);
		//depois adicionar ao frame mais um panel onde esta uma lista dos ficheioros disponiveis na pasta do utilizador
	}
	private void waitForDownload() throws InterruptedException {
		while(blocksDownloaded<totalBloks) {
			wait();
		}
		notifyAll();
		
	}

	public void addInfo() {
		System.out.println(allInfo);
		for(FileDetails fd:allInfo) {
			for(int i = 0;i<fd.getFileName().size();i++) {
				resultList.addElement(fd.getUserName()+": "+fd.getFileName().get(i)+" ,"+fd.getFileSize().get(i)+" bytes;");
				yellowPages.add(fd.getFileSize().get(i)+" "+fd.getIp()+" "+fd.getPort());// "  " careter especial
				yellowFileNames.add(fd.getFileName().get(i));
			}
		}
		System.out.println("aqui estao as yello pages");
		for(String s: yellowPages) {
			System.out.println(s);
		}
		for(String ss:yellowFileNames) {
			System.out.println(ss);
		}
	}
	
	private File findImageFiles(String path) {
		File[] files = new File(path).listFiles(new FileFilter() {
			public boolean accept(File f){
				return f.getName().contains("trabalho_img");
			}
		});
		return files[0];
	}
	
	public static void main(String[] args){
        User user;
        try {
            user = new User(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
            GraphicInterface gp = new GraphicInterface(user);
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }
}