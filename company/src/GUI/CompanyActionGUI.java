package companyGUI;

import javax.swing.JFrame;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import company.ComSubProcessRunnable;
import company.ComSubscriberList;
import companyCore.Item;
import companySupMethod.CompanySupMethods;
import comModels.ComSubModels;
import comModels.ItemModel;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.event.ActionEvent;

public class CompanyActionGUI extends JFrame {

	private JPanel contentPane;
	private JTable table;
	private JTable table_1;
	private JTextField chPriProextField;
	private JTextField upProSymtextField;
	private JTextField protextField;
	private JTextField ssCodtextField;
	private JTable table_3;
	
	private Socket cSoc, subSoc;
	private String uName;
	private ItemModel itmmodl = null;
	private ComSubModels submod = null;
	private ComSubscriberList comSubL;
	private List<String> symboles = null; // create list for symbols
	private List<Item> itmlst = null;
	private List<String> newSymbol = new ArrayList<>(); // new symbol list
	private ComSubModels nsymMod = null; // to new symbol table
	private CompanySupMethods csupIb = new CompanySupMethods();
	private static final String url = "jdbc:postgresql://localhost:5432/test";
	private static final String user = "postgres";
	private static final String password = "1234";	
	// if this is true a bid can be done, else bidding time is finished
	private static boolean biddStat = true; 	
	// to store and map CSV file data and mapping them with symbol
	// Hash table used to thread safety
	private Map<String, String> symPwdMap = new HashMap<>();
	  private Map<String, Float> symBidMap = new HashMap<>();
	  private Map<String, Integer> symFunMap = new HashMap<>();
	  private Map<String, String> symCusMap = new HashMap<>(); // May not be used in this version
	  private Map<String, String> symhBidTim = new HashMap<>(); //Symbol with highest Bid Time
	
	private int guiSynCntrl; // avoid interruption when same time call to methods
	private JTextField newSymtextField;
	private JTextField newProtextField_1;
	private JTextField newPritextField_2;
	private JTextField newSeqtextField_3;
	private JTable table_2;

	/**
	 * Create the frame.
	 */
	public CompanyActionGUI(Socket soc, String uname, Socket sSoc) {
		
		this.cSoc = soc;
		this.uName = uname;
		this.subSoc = sSoc;
		
		comSubL = new ComSubscriberList(); // create object of ComSubscribeList class
		
		createSymbolTable(cSoc); // receive symbol list and create symbol model
		
		setTitle("Company Account - "+ uName);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 10, 664, 707);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Subsctibed Items");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel.setBounds(10, 3, 307, 14);
		contentPane.add(lblNewLabel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 28, 457, 207);
		contentPane.add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		JLabel lblNewLabel_1 = new JLabel("Check Item Price And Profit");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_1.setBounds(10, 265, 272, 14);
		contentPane.add(lblNewLabel_1);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 318, 457, 52);
		contentPane.add(scrollPane_1);
		
		table_1 = new JTable();
		scrollPane_1.setViewportView(table_1);
		
		JLabel lblNewLabel_2 = new JLabel("Symbol :");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_2.setBounds(31, 293, 68, 14);
		contentPane.add(lblNewLabel_2);
		
		chPriProextField = new JTextField();
		chPriProextField.setBounds(98, 290, 122, 20);
		contentPane.add(chPriProextField);
		chPriProextField.setColumns(10);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(485, 209, 153, 420);
		contentPane.add(scrollPane_3);
		
		table_3 = new JTable();
		scrollPane_3.setViewportView(table_3);
		
		setSymTable(submod);
		
		// check details of given symbol
		JButton chkDtlsbtnNewButton = new JButton("Check Item Details");
		chkDtlsbtnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		chkDtlsbtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("\n%s : -------------------Check Item Details---------------------\n", time());
				if(guiSynCntrl == 0) {
					if(!chPriProextField.getText().isBlank()) {
						guiSynCntrl = 1;
						List<Item> chkItmLst = new ArrayList<>(); // to store given item
						
						try {
							Item tmpItm = comSubL.checkItem(cSoc, chPriProextField.getText()); // get given Item Details
							
							// create table if valid symbol
							if(!tmpItm.getSym().equals("Empty")) {
								chkItmLst.add(tmpItm);
								ItemModel chkItmmodl = new ItemModel(chkItmLst);
								table_1.setModel(chkItmmodl);
							}
							else {
								JOptionPane.showMessageDialog(CompanyActionGUI.this, "Invalid Symbol : " + chPriProextField.getText(), "Invalid Symbol", JOptionPane.ERROR_MESSAGE);
							}
						}
						// if connection loss happen
						catch(Exception ex) {
							JOptionPane.showMessageDialog(CompanyActionGUI.this, "Connection is lost. Please log-in again.", "Connection failed", JOptionPane.ERROR_MESSAGE);
							guiSynCntrl = 0;
						}
					}
					else {
						JOptionPane.showMessageDialog(CompanyActionGUI.this, "Plese Enter the Symbol", "Error", JOptionPane.ERROR_MESSAGE);
					}
					guiSynCntrl = 0;
				}
				
			}
		});
		chkDtlsbtnNewButton.setBounds(298, 284, 156, 23);
		contentPane.add(chkDtlsbtnNewButton);
		
		JLabel lblNewLabel_3 = new JLabel("Update Profit");
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_3.setBounds(10, 397, 176, 14);
		contentPane.add(lblNewLabel_3);
		
		JLabel lblNewLabel_4 = new JLabel("Symbol :");
		lblNewLabel_4.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_4.setBounds(31, 422, 68, 14);
		contentPane.add(lblNewLabel_4);
		
		upProSymtextField = new JTextField();
		upProSymtextField.setBounds(128, 422, 122, 20);
		contentPane.add(upProSymtextField);
		upProSymtextField.setColumns(10);
		
		JLabel lblNewLabel_5 = new JLabel("Profit :");
		lblNewLabel_5.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_5.setBounds(277, 422, 40, 14);
		contentPane.add(lblNewLabel_5);
		
		protextField = new JTextField();
		protextField.setBounds(336, 419, 118, 20);
		contentPane.add(protextField);
		protextField.setColumns(10);
		
		JLabel lblNewLabel_6 = new JLabel("Security Code :");
		lblNewLabel_6.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_6.setBounds(31, 454, 104, 14);
		contentPane.add(lblNewLabel_6);
		
		ssCodtextField = new JTextField();
		ssCodtextField.setBounds(128, 451, 122, 20);
		contentPane.add(ssCodtextField);
		ssCodtextField.setColumns(10);
		
		//profit update
		JButton upProbtnNewButton_1 = new JButton("Update Profit");
		upProbtnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("\n%s : -------------------Update Profit---------------------\n", time());
				if(guiSynCntrl == 0) { // check access
					
					// check blank text fields
					if(!(upProSymtextField.getText().isBlank() || protextField.getText().isBlank() || ssCodtextField.getText().isBlank())) {
						
						// check profit data type is Integer
						if(csupIb.isInteger(protextField.getText())) {
							guiSynCntrl = 1; // close access for other process
							
							try {
								// call to profit update
								int bidflg = comSubL.profitUpdate(cSoc, uName, upProSymtextField.getText(), protextField.getText(), ssCodtextField.getText());
								
								if(bidflg == 0) {
									System.out.printf("\n%s : Invalid Symbol\n", time());
									JOptionPane.showMessageDialog(CompanyActionGUI.this, "Invalid Symbol : " + upProSymtextField.getText(), "Invalid Symbol", JOptionPane.ERROR_MESSAGE);
								}
								else if(bidflg == 2) {
									System.out.printf("\n%s : Invalid Security Code\n", time());
									JOptionPane.showMessageDialog(CompanyActionGUI.this, "Invalid Security Code", "Security Code", JOptionPane.ERROR_MESSAGE);
								}
								else if(bidflg == 3) {
									System.out.printf("\n%s : Profit Updated\n", time());
									JOptionPane.showMessageDialog(CompanyActionGUI.this, "Profit Updated : " +protextField.getText(), "Successful", JOptionPane.INFORMATION_MESSAGE);
								}
							}
							// if connection loss happen
							catch(Exception ex) {
								System.out.printf("\n%s : Connection was lost\n", time());
								JOptionPane.showMessageDialog(CompanyActionGUI.this, "Connection was lost. Please log-in again.", "Connection failed", JOptionPane.ERROR_MESSAGE);
								guiSynCntrl = 0;
							}
						}
						else {
							JOptionPane.showMessageDialog(CompanyActionGUI.this, "Profit should be a Positive Integer Value", "Error", JOptionPane.ERROR_MESSAGE);
						}	
					}
					else {
						JOptionPane.showMessageDialog(CompanyActionGUI.this, "Please Enter Symbol and Price with the security code", "Error", JOptionPane.ERROR_MESSAGE);
					}
					guiSynCntrl = 0; // give access to other buttons
					
				}
				
			}
		});
		upProbtnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		upProbtnNewButton_1.setBounds(298, 450, 156, 23);
		contentPane.add(upProbtnNewButton_1);
		
		// close 
		JButton closbtnNewButton = new JButton("Close");
		closbtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("\n%s : -------------------Close---------------------\n", time());
				try {
					comSubL.closeConCompany(cSoc); // close connection
				}
				catch(Exception ex) {
					System.out.println("Connection was lost with the Server");
					setVisible(false);
					dispose();
				}
				setVisible(false);
				dispose();
			}
		});
		closbtnNewButton.setBounds(10, 640, 89, 23);
		contentPane.add(closbtnNewButton);
		
		// subscribed item
		JButton btnNewButton = new JButton("Subscribe Item");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("\n%s : -------------------Subscribe Item---------------------\n", time());
				if(guiSynCntrl == 0) {
					guiSynCntrl = 1;
					int row1 = table_3.getSelectedRow(); // select row number
					
					if(row1 < 0 ) { 
						JOptionPane.showMessageDialog(CompanyActionGUI.this, "You must select a Symbol", "Error",
								JOptionPane.ERROR_MESSAGE);
						guiSynCntrl = 0;
						return;
					}
					
					String selSym1 = (String) table_3.getValueAt(row1,ComSubModels.ITEM); // get symbol
					System.out.printf("%s : Com : [%s] symbol selected to subscribe\n", time(), selSym1);
					try {
						int subSus = comSubL.SubscribeItem(cSoc, uName, selSym1); // call to subscribe
						guiSynCntrl = 0;
						
						 // refresh table with subscribed Item
						if(subSus == 0) {
							createITMTablemodel(cSoc);
							
							JOptionPane.showMessageDialog(CompanyActionGUI.this, "Subscribed Successfully : " + selSym1, "Subscribed",
									JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						else {
							JOptionPane.showMessageDialog(CompanyActionGUI.this, "Already Subscribed : " + selSym1, "Subscribed",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					// if connection loss happen
					catch(Exception ex) {
						JOptionPane.showMessageDialog(CompanyActionGUI.this, "Connection was lost. Please log-in again.", "Connection failed", JOptionPane.ERROR_MESSAGE);
						guiSynCntrl = 0;
					}
				}
				
			}
		});
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton.setBounds(485, 640, 153, 23);
		contentPane.add(btnNewButton);
		
		JLabel lblNewLabel_7 = new JLabel("Add New Item");
		lblNewLabel_7.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_7.setBounds(10, 512, 109, 14);
		contentPane.add(lblNewLabel_7);
		
		JLabel lblNewLabel_8 = new JLabel("Symbol :");
		lblNewLabel_8.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_8.setBounds(31, 537, 89, 14);
		contentPane.add(lblNewLabel_8);
		
		newSymtextField = new JTextField();
		newSymtextField.setBounds(98, 534, 122, 20);
		contentPane.add(newSymtextField);
		newSymtextField.setColumns(10);
		
		JLabel lblNewLabel_9 = new JLabel("Profit :");
		lblNewLabel_9.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_9.setBounds(248, 537, 61, 14);
		contentPane.add(lblNewLabel_9);
		
		newProtextField_1 = new JTextField();
		newProtextField_1.setBounds(336, 534, 118, 20);
		contentPane.add(newProtextField_1);
		newProtextField_1.setColumns(10);
		
		JLabel lblNewLabel_10 = new JLabel("Price :");
		lblNewLabel_10.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_10.setBounds(31, 575, 68, 14);
		contentPane.add(lblNewLabel_10);
		
		newPritextField_2 = new JTextField();
		newPritextField_2.setBounds(98, 572, 122, 20);
		contentPane.add(newPritextField_2);
		newPritextField_2.setColumns(10);
		
		JLabel lblNewLabel_11 = new JLabel("Security Code :");
		lblNewLabel_11.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_11.setBounds(248, 575, 89, 14);
		contentPane.add(lblNewLabel_11);
		
		newSeqtextField_3 = new JTextField();
		newSeqtextField_3.setBounds(336, 572, 118, 20);
		contentPane.add(newSeqtextField_3);
		newSeqtextField_3.setColumns(10);
		
		// add new Item
		JButton btnNewButton_1 = new JButton("Add new Item");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("\n%s : -------------------Add new Item---------------------\n", time());
				if(guiSynCntrl == 0) { // check access
					
					// check blank text fields
					if(!(newSymtextField.getText().isBlank() ||newProtextField_1.getText().isBlank() || newPritextField_2.getText().isBlank() || newSeqtextField_3.getText().isBlank())) {
						
						// check profit data type is Integer
						if(csupIb.isInteger(newProtextField_1.getText())) {
							guiSynCntrl = 1; // close access for other process
							
							try {
								int addflg = comSubL.addNewItem(cSoc, uName, newSymtextField.getText(), newPritextField_2.getText(), newProtextField_1.getText(), newSeqtextField_3.getText());
								
								if(addflg == 3) {
									JOptionPane.showMessageDialog(CompanyActionGUI.this, "Success : " +newSymtextField.getText(), "success", JOptionPane.INFORMATION_MESSAGE);
								}
								else if(addflg == 2) {
									JOptionPane.showMessageDialog(CompanyActionGUI.this, "Available Symbol : " + newSymtextField.getText(), "Symbol", JOptionPane.ERROR_MESSAGE);
								}
								else if(addflg == 0) {
									JOptionPane.showMessageDialog(CompanyActionGUI.this, "Adding failed : " +newSymtextField.getText(), "Fail", JOptionPane.ERROR_MESSAGE);
								}
							}
							// if connection loss happen
							catch(Exception ex) {
								JOptionPane.showMessageDialog(CompanyActionGUI.this, "Connection was lost. Please log-in again.", "Connection failed", JOptionPane.ERROR_MESSAGE);
								guiSynCntrl = 0;
							}
						}
						else {
							JOptionPane.showMessageDialog(CompanyActionGUI.this, "Profit should be a Positive Integer Value", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else {
						JOptionPane.showMessageDialog(CompanyActionGUI.this, "Plese Enter Symbol, Price, profit with the security code", "Error", JOptionPane.ERROR_MESSAGE);
					}
					
					guiSynCntrl = 0; // give access to other buttons
					
				}
				
			}
		});
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton_1.setBounds(298, 612, 156, 23);
		contentPane.add(btnNewButton_1);
		
		JLabel lblNewLabel_12 = new JLabel("New Item");
		lblNewLabel_12.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_12.setBounds(529, 3, 68, 14);
		contentPane.add(lblNewLabel_12);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(485, 28, 153, 122);
		contentPane.add(scrollPane_2);
		
		table_2 = new JTable();
		scrollPane_2.setViewportView(table_2);
		
		JLabel lblNewLabel_13 = new JLabel("Item List");
		lblNewLabel_13.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_13.setBounds(529, 184, 61, 14);
		contentPane.add(lblNewLabel_13);
		
		// start subscriber process
		ComSubProcessRunnable cspro = new ComSubProcessRunnable(subSoc,uName,CompanyActionGUI.this);
		Thread csTr = new Thread(cspro);
		csTr.start();
	}
	
	private void createSymbolTable(Socket pSoc) {
		symboles = loadDataFromDatabase();
		Collections.sort(symboles); // sort symbol list
		submod = new ComSubModels(symboles); //create model of symbols for creating table
		
	}
	
	public synchronized void createITMTablemodel(Socket pSoc) {
		
		itmlst = comSubL.getAlrdySubItemWithDitail(pSoc, uName); // get already subscribed Symbol list with it's details from server
		itmmodl = new ItemModel(itmlst);
		setSubTable(itmmodl);
	}
	
	// update table for price update
	public void createPUpdateTable(String sym, String npr) {
		List<Item> itLst = comSubL.getPriceUpdateItemWithD(sym, npr);
		ItemModel tl = new ItemModel(itLst);
		setSubTable(tl);
	}
	
	// update table for profit update
	public void createProUpdateTable(String sym, String npro) {
		List<Item> itLst = comSubL.getProfitUpdateItemWithD(sym, npro);
		ItemModel tl = new ItemModel(itLst);
		setSubTable(tl);
	}
	
	// update symbol table
	public void createNewSymTable(String sym) {
		symboles = comSubL.getUpdateStringList(sym);
		submod = new ComSubModels(symboles); //create model of symbols for creating table
		setSymTable(submod);
	}
	
	// create new symbol and refresh
	public void newSymbolTable(String symbol) {
		newSymbol.add(symbol);
		nsymMod = new ComSubModels(newSymbol);
		setNewSymTable(nsymMod);
	}
	
	// set subscribed item table
	private synchronized void setSubTable(ItemModel itm) {
		table.setModel(itm);
	}
	
	// set symbol table
	private synchronized void setSymTable(ComSubModels ssubmod) {
		table_3.setModel(ssubmod);	
	}
	
	// create new symbol table
	private synchronized void setNewSymTable(ComSubModels ssubmod) {
		table_2.setModel(ssubmod);
	}
	
	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}	
	private List<String> loadDataFromDatabase() {
		List<String> symbolList = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT \"Symbol\" FROM stocks";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String symbol = rs.getString("Symbol");
                symbolList.add(symbol);
                
            }
        } catch (Exception e) {
            System.out.printf("%s : Cannot read the csv file \n", time());
        }
        return symbolList;
    }
}
