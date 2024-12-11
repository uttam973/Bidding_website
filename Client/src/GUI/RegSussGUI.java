package clientGui;

// this is used to show registration no 
// this appear when registration success

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class RegSussGUI extends JFrame {

	private JPanel contentPane;

	public RegSussGUI(String regNo) {
		setTitle("Registration Successful");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 460, 171);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Welcome to our auction System ......");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblNewLabel.setBounds(10, 11, 301, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("User ID :");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_1.setBounds(10, 46, 64, 14);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel(regNo); // register number
		lblNewLabel_2.setBounds(75, 46, 161, 14);
		contentPane.add(lblNewLabel_2);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("  This is your User ID. Keep it Safely. ");
		chckbxNewCheckBox.setBounds(6, 71, 508, 23);
		contentPane.add(chckbxNewCheckBox);
		
		JButton btnNewButton = new JButton("OK");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxNewCheckBox.isSelected()) {
					setVisible(false);
					dispose();
				}
			}
		});
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton.setBounds(349, 101, 89, 23);
		contentPane.add(btnNewButton);
		
		JLabel lblNewLabel_3 = new JLabel("Everytime you need this ID to log-in to your account");
		lblNewLabel_3.setBounds(33, 93, 306, 14);
		contentPane.add(lblNewLabel_3);
	}
}
