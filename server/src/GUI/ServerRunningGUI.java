package serverGui;

/* this is the main action interface for Server
 * start timer, 
 * client server connection, 
 * start publisher subscriber connection
 */

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import csv.ReadCsv;
import server.ServerConnRunnable;
import server.ServerSubConnRunnable;
import server.ServerTimerRunnable;
import serverCore.FullItem;
import serverCore.Item;
import serverModel.ItemModels;
import readServerTxtFiles.NextCompanyUname;
import readServerTxtFiles.NextUame;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.SwingConstants;

public class ServerRunningGUI extends JFrame {

	private JPanel contentPane;
	private ReadCsv res; // for csv
	private NextUame NeUObj = new NextUame(); 
	private NextCompanyUname cmobj = new NextCompanyUname();
	private Thread sConThread, subConThread, timerThred;
	private int port = 0; //port number to connect server
	private int subPort = 0; // port number for subscribe server
	private ServerTimerRunnable stronob;
	private  int sRunTime;
	private JTable table;
	private List<FullItem> fItmLst = null;
	private ItemModels itmmdls = null;
	public JLabel lblNewLabel = new JLabel();
	
	public ServerRunningGUI(int sPort, int suPort, int sBisT) {
		setTitle("Server");
		setResizable(false);
		
		port = sPort;
		subPort = suPort;
		sRunTime = sBisT;
		
		res = new ReadCsv(ServerRunningGUI.this); // read file and load
		NeUObj.getLastpin(); // get last user ID
		cmobj.getComLastpin(); // get last company user ID
		
		//create timer thread
		stronob = new ServerTimerRunnable(sRunTime, ServerRunningGUI.this);
		//create server socket and waiting for client requests
		ServerConnRunnable sConR = new ServerConnRunnable(port);
		// create server subscriber connection
		ServerSubConnRunnable subConR = new ServerSubConnRunnable(subPort);
		
		timerThred = new Thread(stronob); // timer thread
		sConThread = new Thread(sConR); // client server Thread
		subConThread = new Thread(subConR); // subscriber Thread
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 692, 385);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(21, 39, 645, 296);
		contentPane.add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		try {
			timerThred.start(); // start timer
			sConThread.start(); // run connection thread
			subConThread.start(); // run subscriber Thread
			
			//set Table model
			fItmLst = res.getAllList();
			itmmdls = new ItemModels(fItmLst);
			// create table
			table.setModel(itmmdls);
			
			lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
			lblNewLabel.setBounds(290, 14, 362, 14);
			contentPane.add(lblNewLabel);
		}
		catch(Exception ex) {
			System.out.printf("%s : Server Port Error...\n", time());
			JOptionPane.showMessageDialog(ServerRunningGUI.this, "Server Port Error.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
		}
	}
	// set Item Table
	public void setItemTable() {
		fItmLst = res.getAllList();
		itmmdls = new ItemModels(fItmLst);
		table.setModel(itmmdls);
	}
	

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
