package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;

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

import client.app.Client;

public class GraphicInterface {
	
	private Client user;
	private JFrame frame;
	private File backgroundImage;
	
	public GraphicInterface(Client user) {
		this.backgroundImage = getBackgroundImage(System.getProperty("user.dir"),"gui_img"); 
	
		this.user = user;
		this.user.signUp();
		
		this.frame = new JFrame("Our Files - " + user.getUsername());
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.frame.setLayout(new BorderLayout());
		//frame.pack();? not tested on other computers
		this.frame.setSize(600, 850);
		
		addContentToFrame();
		open();
	}	

	

	public void open() {
		frame.setVisible(true);
		frame.setResizable(false);
	}
	
	private void addContentToFrame() {
		JLabel imageJLabel = new JLabel();
		imageJLabel.setHorizontalAlignment(JLabel.CENTER);
		imageJLabel.setIcon(new ImageIcon(backgroundImage.getName()));
		
		frame.add(imageJLabel, BorderLayout.NORTH);
		
		JPanel searchJPanel = new JPanel(new BorderLayout());

		JTextField searchJTextField = new JTextField();
		searchJTextField.setFont(new Font("Calibri", Font.BOLD, 25));
		searchJPanel.add(searchJTextField, BorderLayout.CENTER);
		
		JButton searchJButton = new JButton("Search");
		searchJButton.setFont(new Font("Calibri", Font.BOLD, 25));
		searchJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(user.getUsername()+" - tryed to perform Search action, Search Message:"+ searchJTextField.getText());
				//don't search if there is no input from user
				if(!searchJTextField.getText().isEmpty()) {				
					//list that displays search results
					user.updateSearchResult(searchJTextField.getText());  
				}
				System.out.println(user.getUsername()+" - has performed Search action");
				//user.updateUserFilesJList();//update every time user clicks the search button
			}
		});
		searchJPanel.add(searchJButton, BorderLayout.LINE_END);
		
		JLabel searchJLabel= new JLabel("Search Result:");
		searchJLabel.setFont(new Font("Calibri", Font.BOLD, 30));
		searchJLabel.setHorizontalAlignment(JLabel.CENTER);
		searchJLabel.setVerticalAlignment(JLabel.CENTER);
	    searchJPanel.add(searchJLabel, BorderLayout.SOUTH);
		
		frame.add(searchJPanel, BorderLayout.CENTER);
		// ^ above is the code about search Panel (JextField/input String + Search Button + "Search Result:" Label)
		
		JPanel searchResultListAndDownloadPlusUserFilesJPanel= new JPanel(new BorderLayout());
		
		JPanel searchResultListAndDownloadJPanel = new JPanel(new BorderLayout());
	
		JList<String> searchResultJList = new JList<String>(user.getSearchResultJList());
		searchResultJList.setFont(new Font("Calibri", Font.BOLD, 20));
		searchResultJList.addListSelectionListener(new ListSelectionListener() {
			private int previous = -1;
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (searchResultJList.getSelectedIndex() != -1 && previous != searchResultJList.getSelectedIndex()) {
					System.out.println(searchResultJList.getSelectedValue());
				}
				previous = searchResultJList.getSelectedIndex();
			}
		});
		JScrollPane searchResultListPane = new JScrollPane(searchResultJList);
		searchResultListAndDownloadJPanel.add(searchResultListPane, BorderLayout.CENTER);
		
		JPanel downloadJPanel = new JPanel(new BorderLayout());
		
		JProgressBar progressJProgressBar = new JProgressBar();
		progressJProgressBar.setFont(new Font("Calibri", Font.BOLD, 30));
		progressJProgressBar.setPreferredSize(new Dimension(175, 80));
		//progressJProgressBar.setString("Click Download");
		progressJProgressBar.setStringPainted(true);
		progressJProgressBar.setMinimum(0);
		progressJProgressBar.setMaximum(10000000);
		progressJProgressBar.setValue(0);
		downloadJPanel.add(progressJProgressBar, BorderLayout.PAGE_END);
		
		JButton downloadJButton = new JButton("Download");
		downloadJButton.setFont(new Font("Calibri", Font.BOLD, 30));
		downloadJButton.setPreferredSize(new Dimension(175, 80));
		downloadJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(searchResultJList.getSelectedValue()!=null) {
					user.download(searchResultJList.getSelectedIndex(), progressJProgressBar);
				}
			}
		});
		downloadJPanel.add(downloadJButton, BorderLayout.PAGE_START);
		
		searchResultListAndDownloadJPanel.add(downloadJPanel, BorderLayout.EAST);
		
		searchResultListAndDownloadPlusUserFilesJPanel.add(searchResultListAndDownloadJPanel, BorderLayout.NORTH);
		// ^ above is code about the files list and download (Search Result List + Download Button + Progressbar)
		
		JPanel userFilesJPanel = new JPanel(new BorderLayout());
		
		JLabel userFilesJLabel= new JLabel("Files in my computer:");
		userFilesJLabel.setFont(new Font("Calibri", Font.BOLD, 30));
		userFilesJLabel.setHorizontalAlignment(JLabel.CENTER);
		userFilesJLabel.setVerticalAlignment(JLabel.CENTER);
	    userFilesJPanel.add(userFilesJLabel, BorderLayout.NORTH);
		
		JList<String> userFilesJList = new JList<String>(user.getUserFilesJList());
		userFilesJList.setFont(new Font("Calibri", Font.BOLD, 20));
		userFilesJList.addListSelectionListener(new ListSelectionListener() {
			private int previous = -1;
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (userFilesJList.getSelectedIndex() != -1 && previous != userFilesJList.getSelectedIndex()) {
					System.out.println(userFilesJList.getSelectedValue());
				}
				previous = userFilesJList.getSelectedIndex();
			}
		});
		JScrollPane userFilesJScrollPane = new JScrollPane(userFilesJList);
		user.updateUserFilesJList();		
		userFilesJPanel.add(userFilesJScrollPane, BorderLayout.SOUTH);
		
		searchResultListAndDownloadPlusUserFilesJPanel.add(userFilesJPanel, BorderLayout.SOUTH);
		// ^ above is the code about the user files list (user files list+ JLabel)
		
		frame.add(searchResultListAndDownloadPlusUserFilesJPanel, BorderLayout.SOUTH);
	}
	
	private File getBackgroundImage(String path, String imageName) {
		File[] files = new File(path).listFiles(new FileFilter() {
			public boolean accept(File f){
				return f.getName().contains(imageName);
			}
		});
		return files[0];
	}
	
	
	/*
	frame.addWindowListener(new WindowAdapter(){
        public void windowClosing(WindowEvent e){
            int i=JOptionPane.showConfirmDialog(null, "Are you sure you want to leave ???");
            if(i==0)
                System.exit(0);
        }
    });*/
}