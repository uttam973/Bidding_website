package company;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import companyCore.Item;

public class ComSubscriberList {
	
	private DataInputStream in = null;
	ComMsg commsg = new ComMsg();
	
	private Map<String, Float> symPrcMap   = new Hashtable<>(); //Symbol with price
	private Map<String, Integer> symProMap = new Hashtable<>(); //Symbol with profit
	private List<String> symLsts = new ArrayList<>();// to store symbols
	
	/*
	 *  CRS  - company request from server to get Symbol list from server                 	(company send)
	 *  CRSY - ask the client/company to be ready to receive the list                     	(Server  send)
	 *  CRSE - tell the client/company that the symbol list is end                        	(Server  send)
	 *  
	 *  CRSI - company request, subscribe symbol                                          	(Company send)
	 *  CRSIY- "RSIY" request accepted                                    					(Server  send)
	 *  CSUB0- Subscribed fail                                            					(Server  send)
	 *  CSUB1- subscribed success                                         					(Server  send)
	 *  
	 *  CSYM - company request to check given symbol is valid and if valid get price and profit (Company send)
	 *  CSYMY- server accept "CSYM" and ask Symbol from company           					(Server  send)
	 *  S1   - symbol is available and ready to receive                   					(Server  send)
	 *  S0   - symbol is not available                                   					(Server  send)
	 *  CSYME- "CSYM" process is completed                                					(Server  send)
	 *  
	 *  CPUR - company request to update profit of Item                                  	(Company send)
	 *  CPURY- server accept "CPUR" request and ask details for update    					(Server  send)
	 *  0    - invalid symbol                                             					(Server  send)
	 *  2    - wrong Security code                                        					(Server  send)
	 *  3    - Success profit update                                      					(Server  send)
	 *  
	 *  CSUB - company request subscribed list with profit and base price                 	(company send)
	 *  CSUBY- server accept "CSUB" request ask user name                 					(Server  send)
	 *  NEMPTY- server ask the company to be ready to start receive data             		(Server  send)
	 *  EMPTY- server send to company that there is no subscribed items                     (Server  send)
	 *  CSUBE- server send to company that the process is completed                        	(Server  send)
	 *  
	 *  CADD - company request to add new Item                                            	(Company send)
	 *  CADDY- server accept "CADD" request Details                   						(Server  send)
	 *  0    - can not add new Item                                       					(Server  send)
	 *  2    - already available symbol                                   					(Server  send)
	 *  3    - add new item is success                                    					(Server  send)
	 *  
	 *  Close- company request, for close request                                         	(Company send)
	 *  OKC  - server accept connection close request                     					(Server  send)
	 *  
	 */
	
	// this is used to get symbol list from the sever and return as List
	public List<String> reqSbsList(Socket soc) {
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		commsg.sentThSocket(soc, "CRS"); // CRS = to ask to get Symbol list from server 
		System.out.printf("%s : Com : [CRS] ask to get Symbol list from server\n", time());
		
		String strtFlag = commsg.reserve(in, soc); // receive server acknowledgement
		
		if(strtFlag.equals("CRSY")){
			System.out.printf("%s : Server : [CRSY] Sending Symbol list has started\n", time());
			//receiving subscribe list has started
		}
		else {
			System.out.printf("%s : Server : Error...Symbol list cannot send\n", time());
		}
		
		int size = Integer.parseInt(commsg.reserve(in, soc)); //receive subscribe list size
		System.out.printf("%s : Server : [%d] Symbol List size\n", time(), size);
		
		List<String> sublist = new ArrayList<>(size);
		
		for(int i = 0; i < size; i++ ) {
			sublist.add(commsg.reserve(in, soc)); //receive and add items to list
		}
		System.out.printf("%s : Server : Sending the Symbol list......\n", time());
		String stopFlag = commsg.reserve(in, soc); // receive end symbol of Symbol list sending
		
		if(stopFlag.equals("CRSE")){
			System.out.printf("%s : Server : [CRSE] Symbol list is end\n", time());
		}
		else {
			System.out.printf("%s : Server : Have a problem with receiving the list\n", time());
		}
		
		symLsts = sublist;
		
		return sublist;
	}
	
	// subscribe Item
	public int SubscribeItem(Socket soc, String uname, String sym1) {
		
		int subflg = 3;
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		commsg.sentThSocket(soc, "CRSI"); // company ask to subscribe Symbol
		System.out.printf("%s : Com : [CRSI] ask to subscribe [%s] Symbol\n", time(), sym1);
		
		String strtFlag = commsg.reserve(in, soc); // receive start statement
		
		if(strtFlag.equals("CRSIY")){
			System.out.printf("%s : Server : [CRSIY] Start updating the subscribe\n", time());
			//receive subscribe list has start
		}
		
		String subFormat = uname + " " + sym1;
		
		commsg.sentThSocket(soc, subFormat); // send user name with symbol
		System.out.printf("%s : Com : username[%s] , symbol[%s] \n", time(), uname, sym1);
		String condiFlag = commsg.reserve(in, soc); 
		
		if(condiFlag.equals("CSUB0")){
			System.out.printf("%s : Server : [SUB0] subscribe list updated successfully\n", time());
			subflg = 0;
		}
		else {
			System.out.printf("%s : Server : [%s] That symbol is already subscribed\n", time(), condiFlag);
			subflg = 1;
		}
		
		return subflg;
	}
	
	// check, get details from server about given item and 
	//return price and profit as a Item
	public Item checkItem(Socket soc,  String sym1) {
		
		Item tmpItm = null;
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		commsg.sentThSocket(soc, "CSYM"); // to check about given symbol and get details
		System.out.printf("%s : Con : [CSYM] send the details about given symbol \n", time());
		
		commsg.reserve(in, soc); // receive accept message
		
		commsg.sentThSocket(soc, sym1); // sent Symbol
		System.out.printf("%s : Com : [%s] Symbol\n", time(), sym1);
		
		String symAvailable = commsg.reserve(in, soc); //to receive available // 0 - not Available, 1 - Available
		
		// if valid receive price and profit and create item
		if(symAvailable.equals("S1")) {
			String sym = commsg.reserve(in, soc);
			String prc = commsg.reserve(in, soc);
			String pro = commsg.reserve(in, soc);
			tmpItm = new Item(sym, Float.parseFloat(prc), Integer.parseInt(pro));
			System.out.printf("%s : Server : [S1] Symbol-[%s] , Price-[%s] , Profit-[%s]\n", time(), sym,prc,pro);
		}
		// else create Empty Item
		else {
			tmpItm = new Item("Empty", 0, 0);
			System.out.printf("%s : Server : [S0] Symbol is not available\n", time());
		}
		
		// stop statement
		if(commsg.reserve(in, soc).equals("CSYME")) {
			System.out.printf("%s : Server : [CSYME] Process completed...\n", time());
		}
		
		return tmpItm;
	}
	
	// profit update
	public int profitUpdate(Socket soc, String uname, String sym2, String profit, String spwd) {
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		commsg.sentThSocket(soc, "CPUR"); // ask to update profit
		System.out.printf("%s : Com : [CPUR] update profit\n", time());
		commsg.reserve(in, soc); //receive start statement
		
		String sStat = uname + " " + sym2 + " " + profit + " " + spwd;
		
		commsg.sentThSocket(soc, sStat); // sent details ("userName symbol price Security_Code")
		System.out.printf("%s : Com : [%s]\n", time(), sStat);
		
		String bsflg = commsg.reserve(in, soc); // invalid symbol = 0; invalid Security Code = 2; success profit update = 3
		
		return Integer.parseInt(bsflg);
		
	}
	
	// add new Item
	public int addNewItem(Socket soc, String uname, String sym2, String price, String profit, String spwd) {
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		commsg.sentThSocket(soc, "CADD"); // ask to add a new item
		System.out.printf("%s : Com : [CADD] add new item\n", time());
		commsg.reserve(in, soc); //receive start statement
		
		String sStat = uname + " " + sym2 + " " + price + " "+ profit + " " + spwd;
		
		commsg.sentThSocket(soc, sStat); // sent details ("userName symbol price Security_Code")
		System.out.printf("%s : Com : [%s]\n", time(), sStat);
		
		String bsflg = commsg.reserve(in, soc); // problem = 0; Already available symbol = 2; successful profit update = 3
		
		return Integer.parseInt(bsflg);
		
	}
	
	// close client given connection
	public void closeConCompany(Socket soc) {
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		commsg.sentThSocket(soc,"Close" ); // to close connection and close GUI
		System.out.printf("%s : Com : [Close]\n", time());
		if(commsg.reserve(in,soc).equals("OKC")) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				System.out.printf("%s : Server : [OKC] Connection was closed\n", time());
				soc.close();
				System.out.printf("%s : Com : Socket closed...............\n", time());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// get already subscribed Symbol list and it's details and return that Item list
	public List<Item> getAlrdySubItemWithDitail(Socket soc, String uname){
		
		String symbol;
		List<Item> itmLst = new ArrayList<>();
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		commsg.sentThSocket(soc, "CSUB"); // ask subscribed items with details from server
		System.out.printf("%s : Com : [CSUB] Request subscribed items with details\n", time());
		String strtFlag = commsg.reserve(in, soc); //receive start statement
		
		if(strtFlag.equals("CSUBY")){
			System.out.printf("%s : Server : [CSUBY] Start receiving items with details\n", time());
			//receive subscribe list has start
		}
		
		commsg.sentThSocket(soc, uname); // sent user name
		System.out.printf("%s : Com : Username[%s] \n", time(), uname);
		
		String sizeFlg = commsg.reserve(in, soc);
		System.out.printf("%s : Server : [%s] \n", time(), sizeFlg);
		
		// if subscribed list not empty create Item List
		if(sizeFlg.equals("NEMPTY")) {
			System.out.printf("%s : Server : [NEMPTY] subscribe list sending......\n", time());
			while(true) {
				symbol = commsg.reserve(in, soc); //receive Symbol or Stop flag
				if(symbol.equals("CSUBE")) {
					System.out.printf("%s : Server : [CSUBE] subscribe list is finished\n", time());
					break;
				}
				
				symPrcMap.put(symbol, Float.parseFloat(commsg.reserve(in, soc))); // receive price and create hash table with symbol
				symProMap.put(symbol, Integer.parseInt(commsg.reserve(in, soc))); // receive profit and create hash table with symbol
			}
			itmLst = createItemLst();
		}
		// else create List was Item("Empty", 0. 0)
		else {
			symbol = commsg.reserve(in, soc); // receive "CSUBE" end command
			System.out.printf("%s : Server : [EMPTY] subscribe list is empty\n", time());
			Item tempItem = new Item("Epmty", 0, 0);
			itmLst.add(tempItem);
		}
		
		return itmLst;
	}
	
	// create Item list using hash maps
	private List<Item> createItemLst(){
		
		List<Item> tmpItemLst = new ArrayList<>();
		
		Set<String> keys = symPrcMap.keySet();
		
		for(String key : keys) {
			Item tempItem = new Item(key, symPrcMap.get(key), symProMap.get(key));
			tmpItemLst.add(tempItem);
		}
		
		return tmpItemLst;
	}
	
	// update price update and get new list ( for publisher subscriber)
	public List<Item> getPriceUpdateItemWithD(String sym, String npr){
		
		List<Item> itmLst = new ArrayList<>();
		
		symPrcMap.put(sym, Float.parseFloat(npr));
		
		itmLst = createItemLst();
		
		return itmLst;
	}
	
	// update profit update and get new list ( for publisher subscriber)
	public List<Item> getProfitUpdateItemWithD(String sym, String npro){
		
		List<Item> itmLst = new ArrayList<>();
		
		symProMap.put(sym, Integer.parseInt(npro));
		
		itmLst = createItemLst();
		
		return itmLst;
	}
	
	// add new symbol and get list
	public List<String> getUpdateStringList(String sym){
		
		List<String> sublist = new ArrayList<>();
		
		symLsts.add(sym); // add new Item
		sublist = symLsts;
		Collections.sort(sublist); // sort symbol list
		
		return sublist;
		
		
	}
	
	
	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}	

}
