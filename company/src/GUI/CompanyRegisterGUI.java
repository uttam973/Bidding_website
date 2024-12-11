package companyGUI;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import company.ComMsg;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class CompanyRegisterGUI extends JFrame {

	private JPanel contentPane;
	private JTextField uNtextField;
	private JTextField emltextField;
	private JTextField pwdtextField;
	private JTextField cpwdtextField;
	
	private DataInputStream in = null;
	private ComMsg commsg = null;
	
	private String uId;

	/**
	 * Create the frame.
	 */
	public CompanyRegisterGUI(Socket soc) {
		setTitle("Company Registration");
		
		commsg = new ComMsg();
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 640, 279);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Name :");
		lblNewLabel.setBounds(10, 41, 134, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Email Address :");
		lblNewLabel_1.setBounds(10, 66, 134, 14);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Password :");
		lblNewLabel_2.setBounds(10, 91, 134, 14);
		contentPane.add(lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("Comfirm Password :");
		lblNewLabel_3.setBounds(10, 116, 134, 14);
		contentPane.add(lblNewLabel_3);
		
		uNtextField = new JTextField();
		uNtextField.setBounds(146, 38, 421, 20);
		contentPane.add(uNtextField);
		uNtextField.setColumns(10);
		
		emltextField = new JTextField();
		emltextField.setBounds(146, 63, 421, 20);
		contentPane.add(emltextField);
		emltextField.setColumns(10);
		
		pwdtextField = new JTextField();
		pwdtextField.setBounds(146, 88, 421, 20);
		contentPane.add(pwdtextField);
		pwdtextField.setColumns(10);
		
		cpwdtextField = new JTextField();
		cpwdtextField.setBounds(146, 113, 421, 20);
		contentPane.add(cpwdtextField);
		cpwdtextField.setColumns(10);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox(" Keep your Password and User Name Safely because you cannot change these given information later ");
		chckbxNewCheckBox.setBounds(10, 155, 608, 23);
		contentPane.add(chckbxNewCheckBox);
		
		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("You cannot change the password later");
		chckbxNewCheckBox_1.setBounds(10, 181, 559, 23);
		contentPane.add(chckbxNewCheckBox_1);
		
		JButton crebtnNewButton = new JButton("Create");
		crebtnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		crebtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					//check text fields are not null and do not contain ","
					if(!(uNtextField.getText().isBlank() || emltextField.getText().isBlank() || pwdtextField.getText().isBlank() || cpwdtextField.getText().isBlank()) && 
							!(uNtextField.getText().contains(",") ||emltextField.getText().contains(",") || pwdtextField.getText().contains(",") || cpwdtextField.getText().contains(","))) {
						
						if(chckbxNewCheckBox.isSelected() && chckbxNewCheckBox_1.isSelected()) {
							
							//check password and confirm password are equal //if return 1 else 0
							if(checkNewPwd(pwdtextField.getText(),cpwdtextField.getText()) == 1) {
								
								//sent type as registration = "5"
								commsg.sentThSocket(soc,"5" );
								System.out.printf("%s : Com : [5] registration\n", time());
								
								//reserve user Id
								uId = commsg.reserve(in,soc);
								System.out.printf("%s : Server : New User ID - %s\n", time(), uId);
								
								//check  registration successful // if 3 else 2
								if(cteateUser(soc, uId, uNtextField.getText(),emltextField.getText(),pwdtextField.getText()) == 3) {
									System.out.printf("%s : Registration successful\n", time());;
									ComRegSussGUI crsob = new ComRegSussGUI(uId);
									crsob.setVisible(true);
									setVisible(false);
									dispose();
								}
								else {
									System.out.printf("%s : Registration Failed\n", time());
									JOptionPane.showMessageDialog(CompanyRegisterGUI.this, "Registration failed", "Registration", JOptionPane.ERROR_MESSAGE);
									CompanyRegisterGUI crob = new CompanyRegisterGUI(soc);
									crob.setVisible(true);
									setVisible(false);
									dispose();
									closeBuff();
								}
								
							}
							else {
								System.out.printf("%s : Wrong Password\n", time());
								JOptionPane.showMessageDialog(CompanyRegisterGUI.this, "Wrong Password", "Password", JOptionPane.ERROR_MESSAGE);
							}
							
						}
						else {
							System.out.printf("%s : Did not agree with the conditions\n", time());
							JOptionPane.showMessageDialog(CompanyRegisterGUI.this, "Please Read and Agree with the Conditions given", "Warning", JOptionPane.ERROR_MESSAGE);
						}
					}
					else {
						System.out.printf("%s : Error in the textfields\n", time());
						JOptionPane.showMessageDialog(CompanyRegisterGUI.this, "Please fill the all fields and do not use ',' in there", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch(Exception ex) {
					System.out.printf("%s : Connection was lost\n", time());
					JOptionPane.showMessageDialog(CompanyRegisterGUI.this, "Connection was lost. Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		crebtnNewButton.setBounds(508, 211, 89, 23);
		contentPane.add(crebtnNewButton);
		
		JButton closbtnNewButton = new JButton("Close");
		closbtnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		closbtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("%s : Registration GUI closed\n", time());
				setVisible(false);
				dispose();
			}
		});
		closbtnNewButton.setBounds(342, 211, 89, 23);
		contentPane.add(closbtnNewButton);
	}
	
	// create user
	private int cteateUser(Socket so, String uid, String nam, String mil, String pwd) {
		
		String rs = "2";
		int val = 2;
		
		String nUData = uid + "," + pwd + "," + nam + "," + mil;
		
		commsg.sentThSocket(so,nUData);
		rs = commsg.reserve(in,so);
		if(rs.equals("SREG")) {
			val = 3;
			System.out.printf("%s : Server : [SREG] Registration Successful\n", time());
		}else {
			System.out.printf("%s : Server : [FREG] Registration Failed\n", time());
		}
		
		return val;
	}
	
	// check password using string compare
	private int checkNewPwd(String pwd, String cpwd) {
		
		int cval = 0;
		
		if(pwd.equals(cpwd)) {
			cval = 1;
		}
		
		return cval;
	}
	
	// close DataInputStream
	private void closeBuff() {
		try {
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}	

}
