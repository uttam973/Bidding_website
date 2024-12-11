package clientGui;

// This is client logging interface
// Get user Name and Password

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.CMsg;
import client.TcpConn;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.Font;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.Color;

/*
 *  when connection establish server is waiting for requests
 *  	- [0] = connection close requests                                               (Client  send to Server)
 *  	- [1] = client logging request                                                  (Client  send to Server)
 *  	- [2] = client registration request                                             (Client  send to Server)
 *  
 *  when request type = [0]
 *  	- [0] = server ripely "0", to say server accept connection close request        (Server  send to Client)
 *  			- after send server close connection
 *  			- after reserving Client closed socket
 *  
 *  when request type = [1] server waiting for logging information
 *  	- [User_Name] = Client send User Name                                           (Client  send to server)
 *  	- [Password]  = Client send Password                                            (Client  send to server)
 *  	- [SLOG] or [FLOG]  = Server send reply                                        (Server  send to Client)
 *  			- [SLOG] - if logging success, give access to enter server
 *  			- [FLOG] - if login fail, waiting for next request ([0], [1], [2])
 *  
 *  when request type = [2] server waiting for registration information
 *  	- [User_Name] = Server send user Name to client                                 (Server  send to Client)
 *  	- [User_Name,Password,Name,Email] = send details of client to server            (Client  send to Server)
 * 		- [SREG] or [FREG] = send to client to say result                               (Server  send to Client)
 * 			- [SREG] - if registration success, wait for login or exit([0],[1],[2]) 
 * 				After Registration client have to log or close connection 
 * 			- [FREG] - if registration false  , wait for other request ([0],[1],[2])
 */

public class ClientLogin extends JFrame {

	private JPanel contentPane;
	private JTextField usNametextField;
	private JTextField PwdtextField_1;
	
	private Socket rsSoc; // for socket
	private DataInputStream in = null;
	private CMsg cmsg = null;
	private int subPort;            // for subscriber port number 
	private TcpConn subCon = null;  // to create client side subscriber socket
	private String serIp;           // IP address
	private boolean substat = true; // to check subscriber connection

	public ClientLogin(Socket rSoc, int sPort, String seIP) {
		setTitle("Client Log-in");
		
		cmsg = new CMsg();
		subPort = sPort;
		serIp   = seIP;
		rsSoc   = rSoc;
		
		// to receive messages through socket
		try {
			in  = new DataInputStream(new BufferedInputStream(rsSoc.getInputStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 220);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("User ID          :");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel.setBounds(10, 38, 123, 24);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Password      :");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel_1.setBounds(10, 89, 123, 24);
		contentPane.add(lblNewLabel_1);
		
		usNametextField = new JTextField();
		usNametextField.setBounds(128, 40, 275, 20);
		contentPane.add(usNametextField);
		usNametextField.setColumns(10);
		
		PwdtextField_1 = new JTextField();
		PwdtextField_1.setBounds(128, 91, 275, 20);
		contentPane.add(PwdtextField_1);
		PwdtextField_1.setColumns(10);
		
		JButton btnNewButton = new JButton("Log-in");
		btnNewButton.setForeground(new Color(0, 0, 0));
		btnNewButton.setBackground(new Color(0, 255, 0));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(substat) {
					try {
						//set type as logging and check it is received = 1
						cmsg.sentThSocket(rsSoc,"1" ); // client server
						System.out.printf("%s : Client - [1] logging request \n", time());
						
						//check user name and password
						//if yes go else enter again
						if(chechUsePWd(usNametextField.getText(),PwdtextField_1.getText(),rsSoc) == 1){
							try {
								Socket subSoc =  createSubConn(subPort,serIp); // after successfully log-in create publisher Subscriber Connection
								// create Action GUI, open and close this Interface
								ActionGUI actObj = new ActionGUI(rsSoc,usNametextField.getText(), subSoc);
								actObj.setVisible(true);
								System.out.printf("%s : Client Account Interface opened\n", time());
								setVisible(false);
								dispose();
								}
							catch(Exception ex){
								substat = false;
								JOptionPane.showMessageDialog(ClientLogin.this, "Connection Error : " + serIp + ":" + subPort + "  Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
								System.out.printf("%s : Connection Error...\n", time());
							}
						}
						else {
							//wrong password message
							JOptionPane.showMessageDialog(ClientLogin.this, "Wrong Password or UserName", "Error", JOptionPane.ERROR_MESSAGE);
							System.out.printf("%s : Wrong Password or Username\n", time());
							ClientLogin obj = new ClientLogin(rsSoc, subPort, serIp);
							obj.setVisible(true);
							System.out.printf("%s : Client Interface opened again\n", time());
							setVisible(false);
							dispose();
						}
					}
					catch(Exception ex) {
						System.out.printf("%s : Connection was lost\n", time());
						JOptionPane.showMessageDialog(ClientLogin.this, "Connection was lost. Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					System.out.printf("%s : Connection Error\n", time());
					JOptionPane.showMessageDialog(ClientLogin.this, "Connection Error : " + serIp + ":" + subPort + "  Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton.setBounds(294, 140, 109, 32);
		contentPane.add(btnNewButton);
		
		// new Registration button 
		// open registration interface
		JButton btnNewButton_1 = new JButton("Create Account");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(substat) {
					ClientRegisterGUI1 objClg = new ClientRegisterGUI1(rsSoc);
					objClg.setVisible(true);
					System.out.printf("%s : Registeration GUI opened\n", time());
				}
				else {
					System.out.printf("%s : Connection Error. Cannot register\n", time());
					JOptionPane.showMessageDialog(ClientLogin.this, "Connection Error : " + serIp + ":" + subPort + "  Please log-in again.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton_1.setBounds(128, 140, 123, 32);
		contentPane.add(btnNewButton_1);
		
		//close button // send server to close request and after receiving message from server close connection and close program
		JButton btnNewButton_2 = new JButton("Close");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					cmsg.sentThSocket(rsSoc,"0" ); // for close connection and close GUI
					System.out.printf("%s : Client : [0] Ask for closing connection\n", time());
					String cStat = cmsg.reserve(in,rsSoc);
					System.out.printf("%s : Server : [%s] closed connection\n", time(), cStat);
					
					if(cStat.equals("0") || cStat.equals("OKC")) {
						System.out.printf("%s : Client : Connection closed successfully\n", time());
						setVisible(false);
						dispose();
						closeBuff();
						try {
							rsSoc.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				catch(Exception ex){
					setVisible(false);
					dispose();
				}
			}
		});
		btnNewButton_2.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton_2.setBounds(10, 140, 97, 32);
		contentPane.add(btnNewButton_2);
	}
	
	//receive result from sever and if yes return 1 else 0
	private int chechUsePWd(String unm, String upwd, Socket ucsoc) {
		
		String rs = "";
		int val = 0;
		
		cmsg.sentThSocket(ucsoc,unm);
		System.out.printf("%s : Client - [%s] send the userID \n", time(), unm);
		cmsg.sentThSocket(ucsoc,upwd);
		System.out.printf("%s : Client - [%s] send the password \n", time(), upwd);
		
		rs = cmsg.reserve(in,ucsoc);
		if(rs.equals("SLOG")) {
			val = 1;
			System.out.printf("%s : Server - [SLOG] Login successful... \n", time());
		}
		else {
			System.out.printf("%s : Server - [FLOG] Login Failed...\n", time());
		}
		
		return val;
	}
	
	// close DataInputStream
	private void closeBuff() {
		try {
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	// create connection through subscriber Port and return socket
	private Socket createSubConn(int sPort, String ip) {
		
		Socket subs = null;
		
		try {
			subCon = new TcpConn(ip, sPort); // call to subCon set connection
			System.out.printf("%s : [%d] Subscriber connection created\n", time(), sPort);
		}
		catch(Exception ex) {
			System.out.printf("%s :  Cannot create the subscriber connection (IP:%s, Port:%d)\n", time(), ip, sPort);
			substat = false;
		}
		
		subs = subCon.getSocket(); // get created socket
		
		return subs;
	}

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

}
