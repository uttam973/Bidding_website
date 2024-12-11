package serverGui;

//This is the server starting class with interface
//get client-server port number and publisher-subscriber port number as inputs

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import supMethod.SupMethods;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;

public class GetRunDetailsGUI extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private SupMethods supob = new SupMethods();
	private JTextField subPorttextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GetRunDetailsGUI frame = new GetRunDetailsGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GetRunDetailsGUI() {
		setTitle("Server");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 288, 175);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Client Server Port :");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel.setBounds(22, 22, 121, 21);
		contentPane.add(lblNewLabel);
		
		textField = new JTextField();
		textField.setBounds(153, 22, 94, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		// get the port numbers as input and call to ServerTimeGUI
		JButton btnNewButton = new JButton("OK");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(supob.isInteger(textField.getText()) && supob.isInteger(subPorttextField.getText())) { // check empty text fields
					
					int response = JOptionPane.showConfirmDialog(
							 GetRunDetailsGUI.this, "Check again whether the enterd port numbers are correct"
									, "Port Confirm", 
									JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					
					if (response != JOptionPane.YES_OPTION) {
						return;
					}
					
					int port = Integer.parseInt(textField.getText()); // get client-server port number
					int subPort = Integer.parseInt(subPorttextField.getText()); // get publisher-subscriber port number
					
					System.out.printf("%s : Client Server Port - %d\n", time(), port);
					System.out.printf("%s : Publisher-subscriber Port - %d\n", time(), subPort);
					
					//close this interface and open ServerTimeGUI
					setVisible(false);
					dispose();
					ServerTimeGUI stobj = new ServerTimeGUI(port, subPort);
					stobj.setVisible(true);
				}
				else {
					System.out.printf("%s : Invalid port number\n", time());
					JOptionPane.showMessageDialog(GetRunDetailsGUI.this, "Enter a valid Port Number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton.setBounds(158, 102, 89, 23);
		contentPane.add(btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Subscriber Port     :");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_1.setBounds(22, 64, 121, 14);
		contentPane.add(lblNewLabel_1);
		
		subPorttextField = new JTextField();
		subPorttextField.setBounds(153, 61, 94, 20);
		contentPane.add(subPorttextField);
		subPorttextField.setColumns(10);
	}
	

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

}