package clientGui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import client.CMsg;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ClientRegisterGUI1 extends JFrame {

	private JPanel contentPane;
	private JTextField uNamtextField;
	private JTextField mailtextField;
	private JTextField pwdtextField;
	private JTextField cPwdtextField;
	
	private DataInputStream in = null;
	private CMsg cmsgo = null;
	
	private String uId;

	public ClientRegisterGUI1(Socket soc) {
		setTitle("Client Registration");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		cmsgo = new CMsg();
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		setBounds(100, 100, 644, 279);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Create Your Account");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		lblNewLabel.setBounds(225, 11, 146, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1_1 = new JLabel("Name :");
		lblNewLabel_1_1.setBounds(10, 41, 134, 14);
		contentPane.add(lblNewLabel_1_1);
		
		JLabel lblNewLabel_1_2 = new JLabel("Email Address :");
		lblNewLabel_1_2.setBounds(10, 66, 134, 14);
		contentPane.add(lblNewLabel_1_2);
		
		JLabel lblNewLabel_1_2_1 = new JLabel("Password :");
		lblNewLabel_1_2_1.setBounds(10, 91, 134, 14);
		contentPane.add(lblNewLabel_1_2_1);
		
		JLabel lblNewLabel_1_2_1_1 = new JLabel("Confirm Password :");
		lblNewLabel_1_2_1_1.setBounds(10, 116, 134, 14);
		contentPane.add(lblNewLabel_1_2_1_1);
		
		uNamtextField = new JTextField();
		uNamtextField.setBounds(146, 38, 421, 20);
		contentPane.add(uNamtextField);
		uNamtextField.setColumns(10);
		
		mailtextField = new JTextField();
		mailtextField.setBounds(146, 63, 421, 20);
		contentPane.add(mailtextField);
		mailtextField.setColumns(10);
		
		pwdtextField = new JTextField();
		pwdtextField.setBounds(146, 88, 421, 20);
		contentPane.add(pwdtextField);
		pwdtextField.setColumns(10);
		
		cPwdtextField = new JTextField();
		cPwdtextField.setBounds(146, 113, 421, 20);
		contentPane.add(cPwdtextField);
		cPwdtextField.setColumns(10);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox(" Keep your Password and User Name Safely because you cannot change the given information later");
		chckbxNewCheckBox.setBounds(10, 155, 612, 23);
		contentPane.add(chckbxNewCheckBox);
		
		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("You cannot change the password later");
		chckbxNewCheckBox_1.setBounds(10, 181, 559, 23);
		contentPane.add(chckbxNewCheckBox_1);
		
		// create client account
		JButton btnNewButton = new JButton("Create");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
				
					//check text fields are not null and does not contain ","
					if(!(uNamtextField.getText().isBlank() || mailtextField.getText().isBlank() || pwdtextField.getText().isBlank() || cPwdtextField.getText().isBlank()) && 
							!(uNamtextField.getText().contains(",") || mailtextField.getText().contains(",") || pwdtextField.getText().contains(",") || cPwdtextField.getText().contains(","))) {
						
						// check CheckBox 
						if(chckbxNewCheckBox.isSelected() && chckbxNewCheckBox_1.isSelected()) {
							
							//check password and confirm password are equal //if return 1 else 0
							if(checkNewPwd(pwdtextField.getText(),cPwdtextField.getText()) == 1) {
								
								//sent type as registration = "2"
								cmsgo.sentThSocket(soc,"2" );
								System.out.printf("%s : Client : [2] registration\n", time());
								
								//reserve user Id
								uId = cmsgo.reserve(in,soc);
								System.out.printf("%s : Server : New User ID - %s\n", time(), uId);
								
								//check  registration successful // if 3 else 2
								if(createUser(soc, uId, uNamtextField.getText(),mailtextField.getText(),pwdtextField.getText()) == 3) {
									System.out.printf("%s : Registration successful\n", time());
									RegSussGUI objsus = new RegSussGUI(uId);
									objsus.setVisible(true);
									setVisible(false);
									dispose();
								}
								else {
									System.out.printf("%s : Registration Failed\n", time());
									JOptionPane.showMessageDialog(ClientRegisterGUI1.this, "Registration failed", "Registration", JOptionPane.ERROR_MESSAGE);
									ClientRegisterGUI1 objreg = new ClientRegisterGUI1(soc);
									objreg.setVisible(true);
									setVisible(false);
									dispose();
									closeBuff();
								}
							}
							else {
								System.out.printf("%s : Wrong Password\n", time());
								JOptionPane.showMessageDialog(ClientRegisterGUI1.this, "Wrong Password", "Password", JOptionPane.ERROR_MESSAGE);
							}
						}
						else {
							System.out.printf("%s : Did not agree with the conditions\n", time());
							JOptionPane.showMessageDialog(ClientRegisterGUI1.this, "Please Read and Agree with the given Conditions", "Warning", JOptionPane.ERROR_MESSAGE);
						}
					}
					else {
						System.out.printf("%s : Error in the textfields\n", time());
						JOptionPane.showMessageDialog(ClientRegisterGUI1.this, "Please fill all the fields and do not use ',' in there", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch(Exception ex) {
					System.out.printf("%s : Connection was lost\n", time());
					JOptionPane.showMessageDialog(ClientRegisterGUI1.this, "Connection was lost. Please log-in again", "Connection Failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton.setBounds(509, 211, 89, 23);
		contentPane.add(btnNewButton);
		
		//close
		JButton btnNewButton_1 = new JButton("Close");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("%s : Registration GUI closed\n", time());
				setVisible(false);
				dispose();
			}
		});
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton_1.setBounds(342, 211, 89, 23);
		contentPane.add(btnNewButton_1);
	}
	
	// compare given 2 strings // if same return 1 else 0
	private int checkNewPwd(String pwd, String cpwd) {
		
		int cval = 0;
		
		if(pwd.equals(cpwd)) {
			cval = 1;
		}
		
		return cval;
	}
	
	// create user
	private int createUser(Socket so, String uid, String nam, String mil, String pwd) {
		
		String rs = "2";
		int val = 2;
		
		String nUData = uid + "," + pwd + "," + nam + "," + mil;
		
		cmsgo.sentThSocket(so,nUData);
		rs = cmsgo.reserve(in,so);
		
		if(rs.equals("SREG")) {
			System.out.printf("%s : Server : [SREG] Registration Successful\n", time());
			val = 3;
		}else {
			System.out.printf("%s : Server : [FREG] Registration Failed\n", time());
		}
		
		return val;
	}
	
	//close  DataInputStream
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
