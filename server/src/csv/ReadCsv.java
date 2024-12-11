package csv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//read stock.csv file
//store in hash maps

import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.util.Map;
import java.util.Set;
import serverGui.ServerRunningGUI;
import readServerTxtFiles.CompanySubItem;
import readServerTxtFiles.FileHandle;
import readServerTxtFiles.PROupdateHandle;
import readServerTxtFiles.PersonSubItem;
import readServerTxtFiles.SYMfileHandle;
import server.ServerSubHandler;
import server.ServerTimerRunnable;
import server.StoreSubConn;
import serverCore.FullItem;
import serverCore.Item;
import serverGui.ServerRunningGUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class ReadCsv {
	
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
	
	// read stock.csv file and store data in hash maps
	public ReadCsv(ServerRunningGUI thesrg){
		
		srg = thesrg;
		loadDataFromDatabase();
	}
	
	// this constructor can be used to access methods in this class
	public ReadCsv(int emty) {
		
	}
	
	// get symbol list from symPwdmap Hash map
	public List<String> getkeySymList(){
		
		List<String> keyList = new ArrayList<>(symPwdMap.keySet());
		
		return keyList;
	}
	
	// check whether the given symbol is valid 
	//if yes return 1 , else return 0
	public int checkSymble(String symbleN) {
		loadDataFromDatabase();
		int cSflg =  0;
		
		if(symPwdMap.containsKey(symbleN)) {
			cSflg =  1;
		}
		
		return cSflg;
	}
	
	//get details about given symbol list 
	//create Item List and return it
	public List<Item> getItmWuthD(List<String> symlst){
		
		List<Item> symWithD = new ArrayList<>();
		
		for (String symbol : symlst) {
			loadDataFromDatabase();
	            if (symBidMap.containsKey(symbol) && symFunMap.containsKey(symbol)) {
	            	symWithD.add(new Item(symbol, symBidMap.get(symbol), symFunMap.get(symbol)));
	            } else {
	                System.out.println("Data not found for symbol: " + symbol);
	            }
	        }
	    
	    return symWithD;
	}
	private void loadDataFromDatabase() {
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT \"Symbol\", \"Price\", \"Security\", \"Profit\" FROM stocks";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String symbol = rs.getString("Symbol");
                String security = rs.getString("Security");
                float price = rs.getFloat("Price");
                int profit = rs.getInt("Profit");

                symPwdMap.put(symbol, security);
                symBidMap.put(symbol, price);
                symFunMap.put(symbol, profit);
            }
        } catch (Exception e) {
            System.out.printf("%s : Cannot read the csv file \n", time());
        }
    }
	
	// invalid symbol = 0; less bid = 1; bidding time end = 2; success bid = 3
	public synchronized int updateBidPrice(String uNam, String upSym, String price) {
		loadDataFromDatabase();
		int flg = 0;
		float pri = Float.parseFloat(price);
		float prePri;
		
		if(biddStat) {
			if(checkSymble(upSym) == 1) {
				
				prePri = symBidMap.get(upSym);
				System.out.printf("%s : %s : Previous price [%s - %.2f]\n", time(), uNam,upSym, prePri);
			
				if(prePri < pri) {
					symBidMap.put(upSym, pri);
					stro.addTimerMintifLmin(); // ask the server to add a minute to the server running time
					String ct = getCurrentTime(); // get current time
					sentNotfiToSubscribers(upSym,price, 1); // send a notification to subscribed users
					symCusMap.put(upSym, uNam);
					symhBidTim.put(upSym, ct);
					srg.setItemTable(); // server table update
					try (Connection con = DriverManager.getConnection(url, user, password)) {
						con.setAutoCommit(true);
	                    String sql = "UPDATE stocks SET \"Price\" = ? WHERE \"Symbol\" = ?";
	                    PreparedStatement stmt = con.prepareStatement(sql);
	                    stmt.setFloat(1, pri);
	                    stmt.setString(2, upSym);
	                    stmt.executeUpdate();
	     
	                    srg.setItemTable(); // server table update
	                    // Call refreshGUI() method
	                    int rowsAffected = stmt.executeUpdate();
	                    
	                    if (rowsAffected > 0) {
	                        System.out.printf("%s : %s : New price [%s - %.2f]\n", time(), uNam, upSym, pri);
	                    } else {
	                        System.out.printf("%s : %s : No rows updated for symbol [%s]\n", time(), uNam, upSym);
	                    }
	                    
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                }}
				else {
					System.out.printf("%s : %s : [%s - %s] Invalid Bid\n", time(),uNam, upSym, price);
					flg = 1;
				}
			}
			else {
				System.out.printf("%s : Server : [%d] Wrong symbol - Symbol: %s\n", time(), upSym);

				System.out.printf("%s : %s : [%s] Invalid Symbol\n", time(),uNam, upSym);
			}
		}
		else {
			flg = 2;
			System.out.printf("%s : %s : [%s] Bidding time is over\n", time(),uNam, upSym);
		}
		
		return flg;
	}
	
	// invalid symbol = 0; Wrong Security Code = 2; success profit update = 3 
	public synchronized int updateProfit(String uNam, String upSym, String profit, String secCod) {
		
		int flg = 0;
		int prePro;
		int pro = Integer.parseInt(profit);
		
		if(checkSymble(upSym) == 1) {
			loadDataFromDatabase();
			if((symPwdMap.get(upSym)).equals(secCod)) {
				System.out.printf("%s : %s : [%s] Valid security code\n", time(), uNam, secCod);
				
				prePro = symFunMap.get(upSym);
				System.out.printf("%s : %s : Previous profit [%s - %d]\n", time(), uNam,  upSym, prePro);

				symFunMap.put(upSym, pro);
				String ct = getCurrentTime(); // get current time
				sentNotfiToSubscribers(upSym,profit, 2); // send a notification to subscribed users
				srg.setItemTable(); // server table update
				PROupdateHandle profho = new PROupdateHandle();
				flg = profho.updatePro(upSym, uNam, prePro, pro, ct);
				System.out.printf("%s : New Profit Value [%s - %d]\n", time(), upSym ,pro);
			}
			else {
				flg = 2;
				System.out.printf("%s : %s : Invalid Security Code : %s\n" ,time(), uNam, upSym);
			}
		}
		else {
			System.out.printf("%s : %s : Invalid symbol to Profit Update : %s\n", time(),uNam, upSym);
		}
		
		
		return flg;
		
	}
	
	 // issue = 0; Already available symbol = 2; success profit update = 3
	public synchronized int addNewItem(String uNam, String upSym, String price, String profit, String secCod) {
		
		int flg = 0;
		int pro ;
		
		System.out.printf("%s : %s : New symbol Added [%s,%s,%s,%s]\n" ,time(), uNam ,upSym ,price, profit, secCod);
		if(checkSymble(upSym) == 0) {
			pro = Integer.parseInt(profit);
			symPwdMap.put(upSym, secCod);
			symFunMap.put(upSym, pro);
			symBidMap.put(upSym, Float.parseFloat(price));
			sentNotificationtoAll(upSym); // send a notification to subscribed users
			srg.setItemTable(); // server table update
			addItemToStockFile(upSym + "," + price + "," + secCod + "," + profit);
			
			flg = 3;
		}
		else {
			flg = 2;
			System.out.printf("%s : %s : Already available symbol : %s\n" ,time(),uNam, upSym);
		}
		
		
		return flg;
		
	}
	
	// get current time
	public String getCurrentTime() {
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
	
	// send updated notifications through subscribed clients
	// noTy 1 = price Update     // noTy 2 = profit update      // noTy 3 = add new symbol
	// pr - profit or price
	private void sentNotfiToSubscribers(String sym, String pr, int noTy) {
		
		String upd = null; // send message
		
		List<String> NameLst = getNameListGivenSym(sym); // get subscribed user name list for the given symbol
		
		List<ServerSubHandler> sshl = getServerSubList(); // get the currently available publisher-subscriber ServerSubHandler list
		
		if(noTy == 1) {
			upd = "PUPDATE " + sym + " " + pr;
		}
		else if(noTy == 2) {
			upd = "PRUPDATE " + sym + " " + pr; 
		}
		
		for(ServerSubHandler subW : sshl) {
			// send subscribed users who have access to send
			if(NameLst.contains(subW.getSubTUname()) && subW.getSendStatus()) {
				System.out.printf("%s : SEND - [%s]\n",time(), upd);
				subW.subSen(upd); //send message
			}
		}
		
		System.out.printf("%s : UPDATE WAS SENT TO ALL ONLINE SUBSCRIBERS\n", time());
		
	}
	
	private void sentNotificationtoAll(String sym) {
		
		String upd = "NEWITEM " + sym;
		
		List<ServerSubHandler> sshl = getServerSubList();
		
		for(ServerSubHandler subW : sshl) {
			System.out.printf("%s : SEND - [%s]\n", time() ,upd);
			subW.subSen(upd); //send message
		}
		
		System.out.printf("%s : A new item was broadcasted\n", time());
	}
	
	// get subscribed clients and companies for given symbol
	private List<String> getNameListGivenSym(String sym){
		
		List<String> userNLst = new ArrayList();
		
		List<String> uName = getUserList(sym); // get subscribed user name list for the given symbol
		
		List<String> cUname = getComList(sym);
		
		userNLst.addAll(uName);
		userNLst.addAll(cUname);

		return userNLst;
	}
	
	// get the currently available ServerSubHandler list
	private List<ServerSubHandler> getServerSubList(){
		StoreSubConn sscr = new StoreSubConn(1);
		return sscr.getSubWorkers();
	}
	
	// get a list of users who subscribed some item
	// ty = 1, client      // ty = 2, company
	private List<String> getUserList(String sym){
		PersonSubItem psio = new PersonSubItem();
		return psio.addOrRes("EMPTY", sym);
	}
	
	private List<String> getComList(String sym){
		CompanySubItem csio = new CompanySubItem();
		return csio.addOrResCom("EMPTY", sym);
	}
	
	// set the bidding status as stopped
	public void setStopBiddingStates() {
		this.biddStat = false;
	}
	
	public boolean getBidStat() {
		return biddStat;
	}
	
	public List<FullItem> getAllList(){
		
		String cus = null;
		String tim = null;
		
		List<FullItem> symWithD = new ArrayList<>();
		
		List<String> symbols = new ArrayList<>(symBidMap.keySet());
		
		Collections.sort(symbols);
		
		for(String sym : symbols) {
			
			if(symCusMap.containsKey(sym)) {
				cus = symCusMap.get(sym);
				tim = symhBidTim.get(sym);
			}
			else{
				cus = "-";
				tim = "-";
			}
			FullItem tmpFullItem = new FullItem(sym, symBidMap.get(sym), symFunMap.get(sym),cus, tim);
			symWithD.add(tmpFullItem);
		}
		
		return symWithD;
		
	}
	
	private void addItemToStockFile(String ss) {
		FileHandle fho = new FileHandle();
		
		String fPath = "doc//stocks.csv";
		
		fho.addNewToFile(ss, fPath);
	}

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

}
