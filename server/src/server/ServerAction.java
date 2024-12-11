package server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
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

import csv.ReadCsv;
import msgHadle.Msg;
import readServerTxtFiles.PersonSubItem;
import serverCore.Item;
import serverGui.ServerRunningGUI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class ServerAction {
	
	private DataInputStream in = null;
	private Msg msg = new Msg();
	private ReadCsv readcsv = new ReadCsv(1);
	private PersonSubItem persubob = new PersonSubItem();
	private static final String url = "jdbc:postgresql://localhost:5432/test";
	private static final String user = "postgres";
	private static final String password = "1234";
	ServerTimerRunnable stro = new ServerTimerRunnable();
	
	// if this is true a bid can be done, else bidding time is finished
	private static boolean biddStat = true; 
	private static ServerRunningGUI srg;
	
	// to store and map CSV file data and mapping them with symbol
	// Hash table used to thread safety
	private Map<String, String> symPwdMap = new HashMap<>();
	  private Map<String, Float> symBidMap = new HashMap<>();
	  private Map<String, Integer> symFunMap = new HashMap<>();
	  private Map<String, String> symCusMap = new HashMap<>(); // May not be used in this version
	  private Map<String, String> symhBidTim = new HashMap<>(); //Symbol with highest Bid Time
	
	private void loadDataFromDatabase() {
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT \"Symbol\", \"Price\" FROM stocks";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String symbol = rs.getString("Symbol");
                float price = rs.getFloat("Price");

                
                symBidMap.put(symbol, price);
                
            }
        } catch (Exception e) {
            System.out.printf("%s : Cannot read the csv file \n", time());
        }
    }
	/*
	 *  SRS  - client request from server to get Symbol list from server                  (Client  send)
	 *  SRSY - ask the client/company to ready to get the list                            (Server  send)
	 *  SRSE - tell the client/company that the symbol list is end                        (Server  send)
	 *  
	 *  SUB  - client request subscribed list with profit and base price                  (client  send)
	 *  SUBY - server reply to server accept "SUB" request ask user name              	  (Server  send)
	 *  NEMPTY - server ask the client to be ready to receive data             			  (Server  send)
	 *  EMPTY- server send to client there is no subscribed items                         (Server  send)
	 *  SUBC - server send to client that the process is completed                        (Server  send)
	 *  
	 *  SYMC - client request to check given symbol is valid and if valid get price and profit (Client send)
	 *  SYNCY- server accept "SYMC" and ask Symbol from client             				  (Server  send)
	 *  S1   - symbol is available and ready to receive                    				  (Server  send)
	 *  S0   - symbol is not available                                     				  (Server  send)
	 *  SYNCE- "SYMC" process is completed                                 				  (Server  send)
	 *  
	 *  BID  - client request, bid on item                                                (Client  send)
	 *  BIDY - "BID" is accepted and ask symbol,user name,bid            			      (Server  send)
	 *  3    - bidding success and process end                            				  (Server  send)
	 *  2    - bidding time was ended. can not bid                        			  	  (Server  send)
	 *  0    - symbol wrong and process end                                				  (Server  send)
	 *  1    - bid price is less than current value                           			  (Server  send)
	 *  
	 *  PRFT - client request to subscribe a symbol                                       (Client  send)
	 *  PRFTY- "PRFT" request accepted                                   				  (Server  send)
	 *  SUB1 - Subscribed fail                                           				  (Server  send)
	 *  SUB0 - subscribed success                                          				  (Server  send)
	 *  
	 *  Close- client request to close the connection                                     (Client  send)
	 *  OKC  - server tells that it accept Close request from client                      (Server  send)
	 */
	
	// receive about service type from client and return
	public String typOfSevese(Socket s) {
		loadDataFromDatabase();
		
		String sTyp = null;
		
		try {
			in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sTyp = msg.reserve(in, s); // receive service type
		
		return sTyp;
	}
	
	// ask the service  request 
	public int proformServese(Socket s, String flg) {
		loadDataFromDatabase();
		
		int flag = 0;
		
		if(flg.equals("SRS")) {
			flag = releseSubList(s);
		}
		else if(flg.equals("PRFT")){
			flag = SubscribeItem(s);
		}
		else if(flg.equals("SUB")) {
			flag = subItemWithDetails(s);
		}
		else if(flg.equals("SYMC")) {
			flag = checkAvaSym(s);
		}
		else if(flg.equals("BID")) {
			flag = makeBid(s);
		}
		
		return flag;
	}
	
	// send Symbol list to client and return 1 if end function 
	public int releseSubList(Socket s) {
		loadDataFromDatabase();
		int subLstReFlag = 0;
		
		try {
			in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(s,"SRSY"); // ask the client to ready to get the list
		
		List<String> symList = getSymList(); // get symbol list
		int sizeArr = symList.size();
		
		msg.sentThSocket(s,Integer.toString(sizeArr)); // send size of the Symbol list
		
		for(int i = 0; i < sizeArr; i++) {
			msg.sentThSocket(s, symList.get(i)); // sent symbol
		}
		
		msg.sentThSocket(s,"SRSE"); //  Symbol list is end
		
		subLstReFlag = 1;
		
		return subLstReFlag;
		
	}
	
	// get symbol list
	private List<String> getSymList() {
		loadDataFromDatabase();
		List<String> sym = readcsv.getkeySymList(); // get key set of symbols
		
		return sym;
	}
	
	// to subscribe Symbol and send result to client and after process end return 1 // client
	public int SubscribeItem(Socket soc) {
		loadDataFromDatabase();
		int sFlg = 0, upSymflg = 0;
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"PRFTY"); // to start sending
		
		String subItmN = msg.reserve(in, soc); // receive subscribe Item with Name
		
		String[] val = subItmN.split(" ");
		
		// send as wrong symbol
		if(readcsv.checkSymble(val[1]) == 0) {
			msg.sentThSocket(soc,"SUB0");
		}
		else {
			
			try {
				List<String> chek = persubob.addOrRes(val[0], val[1]);
				if(chek.contains("1")){
					upSymflg = 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(upSymflg == 0) {
				sFlg = 1;
				msg.sentThSocket(soc,"SUB0");
			}
			else {
				msg.sentThSocket(soc,"SUB1");
				sFlg = 1;
			}
		}
		
		return sFlg;
	}
	
	// get subscribed item with profit and price and 
	// sent to the client and 
	// after process return 1
	public int subItemWithDetails(Socket soc) {
		loadDataFromDatabase();
		int flg = 0;
		//Item theItem = new Item("Empty", 0, 0);
		List<Item> symDtaList = new ArrayList<>();
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"SUBY"); // server accept request and ask user Name
		
		String uName = msg.reserve(in, soc); //receive User name
		
		symDtaList = getSymWithDataList(uName); // get subscribed item with details
		
		if(symDtaList != null) {
			msg.sentThSocket(soc, "NEMPTY"); // ask to ready to get the subscribe list
		}
		else {
			msg.sentThSocket(soc, "EMPTY"); //  there is no any subscribed item
		}
		
		if(symDtaList != null) {
			
			for(int i = 0; i < symDtaList.size(); i++) {
				Item tmpItem = symDtaList.get(i);
				msg.sentThSocket(soc,tmpItem.getSym()); // to sent symbol
				msg.sentThSocket(soc,Float.toString(tmpItem.getPrice())); // to sent price
				msg.sentThSocket(soc,Integer.toString(tmpItem.getProfit())); // to sent profit
			}
		}
		
		msg.sentThSocket(soc, "SUBC"); // sent that the Item List is finished
		
		flg = 1;
		
		return flg;
	}
	
	// get symbol with price and profit Item List and 
	// return Item list
	private List<Item> getSymWithDataList(String uName) {
		loadDataFromDatabase();
		List<Item> itmWithD = null;
		
		List<String> ItmLst = getSubSymList(uName);
		
		if(!ItmLst.isEmpty()) {
			List<String> uppercaseItmLst = new ArrayList<>();
		    for (String symbol : ItmLst) {
		        uppercaseItmLst.add(symbol.toUpperCase());
		    }
		    itmWithD = readcsv.getItmWuthD(uppercaseItmLst);
		}
		
		return itmWithD;
	}
	
	// get subscribe symbol list with given user name
	private List<String> getSubSymList(String uName) {
		loadDataFromDatabase();
		
		List<String> ItmLst = persubob.addOrRes(uName, "NOSYMBOL"); // get subscribed symbol list
		return ItmLst;
		
	}
	
	// get item details of given Symbol and send to client and 
	//after end process return 1
	public int checkAvaSym(Socket soc) {
		loadDataFromDatabase();
		int sFlg = 0;
		List<String> symlst = new ArrayList<>();
		List<Item> itmlst   = new ArrayList<>();
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"SYNCY"); // to start receive symbol
		
		String symbol = msg.reserve(in, soc); //receive symbol
		
		int chekval = readcsv.checkSymble(symbol); // check Symbol availability // if have 1 else 0
		
		if(chekval == 1) {
			msg.sentThSocket(soc, "S1"); // send as available symbol
			symlst.add(symbol);
			itmlst = readcsv.getItmWuthD(symlst);
			Item tmpItem = itmlst.get(0);
			msg.sentThSocket(soc,tmpItem.getSym()); // to sent symbol
			msg.sentThSocket(soc,Float.toString(tmpItem.getPrice())); // to sent price
			msg.sentThSocket(soc,Integer.toString(tmpItem.getProfit())); // to sent profit
		}
		else {
			msg.sentThSocket(soc, "S0");	// there is no any Symbol like that
		}
		
		msg.sentThSocket(soc, "SYNCE"); // end statement
		
		sFlg = 1;
		
		return sFlg;
	}
	
	// bid price update and keep track of it 
	//send result to the client and 
	//return 1 after process
	public int makeBid(Socket soc) {
		loadDataFromDatabase();
		int bflg = 0;
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"BIDY"); // to start receive bidding details
		
		String bidF = msg.reserve(in, soc); //receive "userName symbol bidPrice"
		System.out.printf("%s : [BID] %s\n",time(), bidF);
		
		String[] val = bidF.split(" ");
		
		int chekval = readcsv.updateBidPrice(val[0], val[1], val[2]); // update value and get result
		
		msg.sentThSocket(soc, Integer.toString(chekval)); // success bid
		
		bflg = 1;
		
		return bflg;
	}
	// close given connection 
	public void closeClientCon(Socket soc) {
		loadDataFromDatabase();
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"OKC"); // accept connection close request
		
		try {
			in.close(); // close DataInputStream
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			soc.close(); // close socket
			System.out.printf("%s : Client disconnected\n", time());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// get user name from client (used in publisher subscriber process)
	public String getClientUname(Socket soc) {
		loadDataFromDatabase();
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String un = msg.reserve(in, soc);
		
		return un;
	}
	
	// get current time
	public String time() {
		loadDataFromDatabase();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

}
