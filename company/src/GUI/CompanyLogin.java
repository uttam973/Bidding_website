package companyGUI;
import company.Connection;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import company.ComConn;
import company.ComMsg;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;

/*
 *  when connection established server is waiting for requests
 *  	- [0] = connection close requests                                               (Company send to Server)
 *  	- [4] = company logging request                                                 (Company send to Server )
 *  	- [5] = company registration request                                            (Company send to Server )
 *  
 *  when request type = [0]
 *  	- [0] = server reply "0", to say server accept connection close request        (Server  send to Company)
 *  			- after send server close connection
 *  			- then receiving Company closed socket
 *  
  * when request type = [4] server waiting for company logging information
  *  	- [User_Name] = Company send User Name                                          (Company send to Server )
  *  	- [Password]  = Company send Password                                           (Company send to Server )
  *  	- [SLOG] or [FLOG]  = Server send reply                                         (Server  send to Company)
  *  			- [SLOG] - if logging success, give access to enter server
  *  			- [FLOG] - if login fail, waiting for next request ([0], [4], [5]) 
  * 
  *  when request type = [5] server waiting for registration information of Company
  *  	- [User_Name] = Server send user Name to company                                (Server  send to Company)
  *  	- [User_Name,Password,Name,Email] = send details of Company                     (Company send to Server )
  * 	- [SREG] or [FREG] = send to the client to say result                           (Server  send to Company)
  * 			- [SREG] - if registration success, wait for login or exit([0],[4],[5]) 
  * 				After Registration Company have to log or close connection 
  * 			- [FREG] - if registration failed  , wait for other request ([0],[4],[5]) 
  */

public class CompanyLogin extends JFrame {

	private JPanel contentPane;
	private JTextField uIdtextField;
	private JTextField PwdtextField;
	private JButton ClobtnNewButton;
	private JButton regbtnNewButton;
	private JButton logbtnNewButton;
	
	private Socket rsSoc; // for socket
	private DataInputStream in = null;
	private ComMsg commsg = null;
	private int subPort;            // for subscriber port number 
	
	private ComConn conCon = null;  // to create company side subscriber socket
	private String serIp;           // IP address
	private boolean substat = true; // to check subscriber connection
	private Connection rsConn;
	
	/**
	 * Create the frame.
	 */
	public CompanyLogin(Socket rSoc, int sPort, String seIP) {
		setTitle("Company Log-in");
		
		commsg = new ComMsg();
		subPort = sPort;
		serIp   = seIP;
		rsSoc   = rSoc;
		
		// for receiving messages through socket
		try {
			in  = new DataInputStream(new BufferedInputStream(rsSoc.getInputStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		conCon = new ComConn(); // Instantiate ComConn through the Connection interface
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 220);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("User ID          :");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel.setBounds(10, 38, 123, 24);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Password      :");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_1.setBounds(10, 89, 123, 24);
		contentPane.add(lblNewLabel_1);
		
		uIdtextField = new JTextField();
		uIdtextField.setBounds(128, 40, 275, 20);
		contentPane.add(uIdtextField);
		uIdtextField.setColumns(10);
		
		PwdtextField = new JTextField();
		PwdtextField.setBounds(128, 91, 275, 20);
		contentPane.add(PwdtextField);
		PwdtextField.setColumns(10);
		
		//close button // send server to network close request and after receiving message from server close connection and close program
		ClobtnNewButton = new JButton("Close");
		ClobtnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		ClobtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					commsg.sentThSocket(rsSoc,"0" ); // to close connection and close GUI
					System.out.printf("%s : Com : [0] Ask for closing connection\n", time());
					
					String cStat = commsg.reserve(in,rsSoc);
					System.out.printf("%s : Server : [%s] closed connection\n", time(), cStat);
					if(cStat.equals("0") || cStat.equals("OKC")) {
						System.out.printf("%s : Com : Connection closed successfully\n", time());
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
				catch(Exception ex) {
					setVisible(false);
					dispose();
				}
			}
		});
		ClobtnNewButton.setBounds(10, 140, 97, 32);
		contentPane.add(ClobtnNewButton);
		
		// new Registration button // open registration interface
		regbtnNewButton = new JButton("Create Account");
		regbtnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		regbtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(substat) {
					CompanyRegisterGUI crhob = new CompanyRegisterGUI(rsSoc);
					System.out.printf("%s : Registeration GUI opened\n", time());
					crhob.setVisible(true);
				}
				else {
					System.out.printf("%s : Connection Error. Cannot register\n", time());
					JOptionPane.showMessageDialog(CompanyLogin.this, "Connection Error : " + serIp + ":" + subPort + "  Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		regbtnNewButton.setBounds(128, 140, 123, 32);
		contentPane.add(regbtnNewButton);
		
		logbtnNewButton = new JButton("Log");
		logbtnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		logbtnNewButton.setForeground(new Color(0, 0, 0));
		logbtnNewButton.setBackground(new Color(0, 255, 0));
		logbtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(substat) {
					try {
						//set type as logging and check if it is received = 4
						commsg.sentThSocket(rsSoc,"4" ); // client server
						System.out.printf("%s : Com - [4] logging request \n", time());
						
						//check user name and password
						//if yes go else enter again
						if(chechUsePWd(uIdtextField.getText(),PwdtextField.getText(),rsSoc) == 1){
							try {
								Socket subSoc =  createSubConn(subPort,serIp);// after successfully log-in create publisher Subscriber Connection
								// create Action GUI, open and close this Interface
								CompanyActionGUI cagob = new CompanyActionGUI(rsSoc,uIdtextField.getText(), subSoc);
								System.out.printf("%s : Company Account Interface opened\n", time());
								
								cagob.setVisible(true);
								setVisible(false);
								dispose();
							}
							catch(Exception ex) {
								System.out.printf("%s : Connection Error...\n", time());
								substat = false;
								JOptionPane.showMessageDialog(CompanyLogin.this, "Connection Error : " + serIp + ":" + subPort + "  Please log-in again.",  "Connection Failed", JOptionPane.ERROR_MESSAGE);
							}
						}
						else {
							//wrong password message
							JOptionPane.showMessageDialog(CompanyLogin.this, "Wrong Password or UserName", "Error", JOptionPane.ERROR_MESSAGE);
							System.out.printf("%s : Wrong Password or Username\n", time());
							CompanyLogin clobj = new CompanyLogin(rsSoc, subPort, serIp);
							System.out.printf("%s : Company Interface opened again\n", time());
							
							clobj.setVisible(true);
							setVisible(false);
							dispose();
						}
					}
					catch(Exception ex) {
						JOptionPane.showMessageDialog(CompanyLogin.this, "Connection was lost. Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					JOptionPane.showMessageDialog(CompanyLogin.this, "Connection Error : " + serIp + ":" + subPort + "  Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		logbtnNewButton.setBounds(294, 140, 109, 32);
		contentPane.add(logbtnNewButton);
	}
	
	//receive the result from sever and if yes return 1 else 0
	private int chechUsePWd(String unm, String upwd, Socket ucsoc) {
			
		String rs = "";
		int val = 0;
			
		commsg.sentThSocket(ucsoc,unm);
		System.out.printf("%s : Com - [%s] send the userID \n", time(), unm);
		commsg.sentThSocket(ucsoc,upwd);
		System.out.printf("%s : Com - [%s] send the password \n", time(), upwd);
		
		rs = commsg.reserve(in,ucsoc);
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
			System.out.printf("%s : [%d] Subscriber connection created\n", time(), sPort);
			conCon.connect(ip, sPort);
		}
		catch(Exception ex) {
			System.out.printf("%s :  Cannot create the subscriber connection (IP:%s, Port:%d)\n", time(), ip, sPort);
			substat = false;
		}
		
		subs=conCon.getSocket(); // get created socket
		
		return subs;
	}
	
	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
	
	
}