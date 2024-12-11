
package client;

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

import clientCore.Item;

public class SubscribeList {
	
	private DataInputStream in = null;
	CMsg cmsg = new CMsg();
	
	private Map<String, Float> symPrcMap   = new Hashtable<>(); //Symbol with price
	private Map<String, Integer> symProMap = new Hashtable<>(); //Symbol with profit
	private List<String> symLsts = new ArrayList<>(); // to store symbols
	
	/*
	 *  SRS  - client request from server to get Symbol list from server        (client send)
	 *  SRSY - ask the client to be ready to receive the list                   (Server send)
	 *  SRSE - symbol list is end                           				    (Server send)
	 *  
	 *  SUB  - client request subscribed list with profit and base price     	(client send)
	 *  SUBY - server reply that the server accept "SUB" request, ask user name (Server send)
	 *  NEMPTY - server ask the client to be ready to start receiving data		(Server send)
	 *  EMPTY- server send to client saying there is no subscribed items        (Server send)
	 *  SUBC - server inform the client that the process is completed           (Server send)
	 *  
	 *  SYMC - client request to check given symbol is valid and if valid get price and profit (Client send)
	 *  SYNCY- server accept "SYMC" and ask Symbol from client					(Server send)
	 *  S1   - symbol is available and ready to receive       					(Server send)
	 *  S0   - symbol is not available                        					(Server send)
	 *  SYNCE- "SYMC" process is completed                    					(Server send)
	 *  
	 *  BID  - client request, bid on item                                   	(Client send)
	 *  BIDY - "BID" is accepted and ask symbol,user name,bid 					(Server send)
	 *  3    - bidding success and process end                					(Server send)
	 *  2    - bidding time was ended. can not bid            					(Server send)
	 *  0    - symbol wrong and process end                   					(Server send)
	 *  1    - bid price less than current value             					(Server send)
	 *  
	 *  PRFT - client request, subscribe symbol                              	(Client send)
	 *  PRFTY- "PRFT" request accepted                        					(Server send)
	 *  SUB1 - Subscribed fail                                					(Server send)
	 *  SUB0 - subscribed success                             					(Server send)
	 *  
	 *  Close- client request, close connection                              	(Client  send)
	 *  OKC  - accept Close request from client               					(Server  send)
	 */
	
	
	// this is used to get symbol list from the sever and return as List
	public List<String> reqSbsList(Socket soc) {
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		cmsg.sentThSocket(soc, "SRS"); // SRS = to ask to get Symbol list from server 
		System.out.printf("%s : Client : [SRS] ask to get Symbol list from server\n", time());
		
		String strtFlag = cmsg.reserve(in, soc); // receive server acknowledgement
		
		if(strtFlag.equals("SRSY")){
			System.out.printf("%s : Server : [SRSY] Sending Symbol list has started\n", time());
		}
		else {
			System.out.printf("%s : Server : Error...Symbol list cannot send\n", time());
		}
		
		int size = Integer.parseInt(cmsg.reserve(in, soc)); //receive subscribe list size
		System.out.printf("%s : Server : [%d] Symbol List size\n", time(), size);
		
		List<String> sublist = new ArrayList<>(size);
		
		for(int i = 0; i < size; i++ ) {
			sublist.add(cmsg.reserve(in, soc)); //receive and add items to list
		}
		System.out.printf("%s : Server : Sending the Symbol list......\n", time());
		String stopFlag = cmsg.reserve(in, soc); // receive end symbol of Symbol list sending
		
		if(stopFlag.equals("SRSE")){
			System.out.printf("%s : Server : [SRSE] Symbol list is end\n", time());
		}
		else {
			System.out.printf("%s : Server : Have a problem with receiving the list\n", time());
		}
		
		symLsts = sublist; // store in this class
		
		return symLsts;
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
			
		cmsg.sentThSocket(soc, "PRFT"); // client ask to subscribe Symbol
		System.out.printf("%s : Client : [PRFT] ask to subscribe [%s] Symbol\n", time(), sym1);
		
		String strtFlag = cmsg.reserve(in, soc); // receive start statement
		
		if(strtFlag.equals("PRFTY")){
			System.out.printf("%s : Server : [PRFTY] Start updating the subscribe\n", time());
			//receive subscribe list has start
		}
		
		String subFormat = uname + " " + sym1;
		
		cmsg.sentThSocket(soc, subFormat); // send user name with symbol
		System.out.printf("%s : Client : username[%s] , symbol[%s] \n", time(), uname, sym1);
		String condiFlag = cmsg.reserve(in, soc); 
		
		if(condiFlag.equals("SUB0")){
			System.out.printf("%s : Server : [SUB0] subscribe list updated successfully\n", time());
			subflg = 0;
		}
		else {
			System.out.printf("%s : Server : [%s] That symbol is already subscribed\n", time(), condiFlag);
			subflg = 1;
		}
		
		return subflg;
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
		
		cmsg.sentThSocket(soc, "SUB"); // ask subscribed items with details from server
		System.out.printf("%s : Client : [SUB] Request subscribed items with details\n", time());
		
		String strtFlag = cmsg.reserve(in, soc); //receive start statement
		
		if(strtFlag.equals("SUBY")){
			System.out.printf("%s : Server : [SUBY] Start receiving items with details\n", time());
			//receive subscribe list has start
		}
		
		cmsg.sentThSocket(soc, uname); // sent user name
		System.out.printf("%s : Client : Username[%s] \n", time(), uname);
		
		String sizeFlg = cmsg.reserve(in, soc);
		System.out.printf("%s : Server : [%s] \n", time(), sizeFlg);
		
		// if subscribed list not empty create Item List
		if(sizeFlg.equals("NEMPTY")) {
			System.out.printf("%s : Server : [NEMPTY] subscribe list sending......\n", time());
			while(true) {
				symbol = cmsg.reserve(in, soc); //receive Symbol or Stop flag
				if(symbol.equals("SUBC")) {
					System.out.printf("%s : Server : [SUBC] subscribe list is finished\n", time());
					break;
				}
				
				symPrcMap.put(symbol, Float.parseFloat(cmsg.reserve(in, soc))); // receive price and create hash table with symbol
				symProMap.put(symbol, Integer.parseInt(cmsg.reserve(in, soc))); // receive profit and create hash table with symbol
			}
			itmLst = createItemLst();
		}
		// else create List was Item("Empty", 0. 0)
		else {
			symbol = cmsg.reserve(in, soc); // receive "SUBC" end command
			System.out.printf("%s : Server : [EMPTY] subscribe list is empty\n", time());
			Item tempItem = new Item("Empty", 0, 0);
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
	
	// check, get details from server about given item and return price and profit as a Item
	public Item checkItem(Socket soc,  String sym1) {
		
		Item tmpItm = null;
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		cmsg.sentThSocket(soc, "SYMC"); // to ask to check about given symbol and get details
		System.out.printf("%s : Client : [SYMC] send the details about given symbol \n", time());
		
		cmsg.reserve(in, soc); // receive accept message
		
		cmsg.sentThSocket(soc, sym1); // sent Symbol
		System.out.printf("%s : Client : [%s] Symbol\n", time(), sym1);
		
		String symAvailable = cmsg.reserve(in, soc); //to receive available // 0 - not Available, 1 - Available
		
		// if valid receive price and profit and create item
		if(symAvailable.equals("S1")) {
			String sym = cmsg.reserve(in, soc);
			String prc = cmsg.reserve(in, soc);
			String pro = cmsg.reserve(in, soc);
			tmpItm = new Item(sym, Float.parseFloat(prc), Integer.parseInt(pro));
			System.out.printf("%s : Server : [S1] Symbol-[%s] , Price-[%s] , Profit-[%s]\n", time(), sym,prc,pro);
		}
		// else create Empty Item
		else {
			tmpItm = new Item("Empty", 0, 0);
			System.out.printf("%s : Server : [S0] Symbol is not available\n", time());
		}
		
		// stop statement
		if(cmsg.reserve(in, soc).equals("SYNCE")) {
			System.out.printf("%s : Server : [SYNCE] Process completed...\n", time());
		}
		
		return tmpItm;
	}
	
	// bid on item and if success return 3, wrong symbol return 0, less than current max price 1, bidding time end return 2
	public int bidOnItem(Socket soc, String uname, String sym2, String price) {
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		cmsg.sentThSocket(soc, "BID"); // ask to bid
		System.out.printf("%s : Client : [BID]\n", time());
		
		cmsg.reserve(in, soc); //receive start statement
		
		String sStat = uname + " " + sym2 + " " + price;
		
		cmsg.sentThSocket(soc, sStat); // sent details ("userName symbol price)
		System.out.printf("%s : Client : [%s]\n", time(), sStat);
		
		String bsflg = cmsg.reserve(in, soc); // receive result
		return Integer.parseInt(bsflg);
	}
	
	// close client given connection
	public void closeConClient(Socket soc) {
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		cmsg.sentThSocket(soc,"Close" ); // to close connection and close GUI
		System.out.printf("%s : Client : [Close]\n", time());
		
		if(cmsg.reserve(in,soc).equals("OKC")) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				System.out.printf("%s : Server : [OKC] Connection was closed\n", time());
				soc.close();
				System.out.printf("%s : Client : Socket closed...............\n", time());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// update price update and get new list ( for publisher subscriber)
	public List<Item> getPriceUpdateItemWithD(String sym, String npr){
		
		List<Item> itmLst = new ArrayList<>();
		
		symPrcMap.put(sym, Float.parseFloat(npr));
		
		itmLst = createItemLst();
		
		return itmLst;
	}
	
	// update profit update and get new list ( for publisher subscriber)
	public List<Item> getProUpdateItemWithD(String sym, String npro){
		
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
