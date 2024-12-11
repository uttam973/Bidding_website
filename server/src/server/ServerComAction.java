package server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import csv.ReadCsv;
import msgHadle.Msg;
import readServerTxtFiles.CompanySubItem;
import serverCore.Item;

public class ServerComAction {
	
	private DataInputStream in = null;
	private Msg msg = new Msg();
	
	private ReadCsv readcsv = new ReadCsv(1);
	private CompanySubItem comSubO = new CompanySubItem();
	
	/*
	 *  CRS  - company request from server to get Symbol list from server                 (company send)
	 *  CRSY - ask the company to ready to receive the list                               (Server  send)
	 *  CRSE - tell the company that the symbol list is end                               (Server  send)
	 *  
	 *  CRSI - company request, subscribe symbol                                          (Company send)
	 *  CRSIY- "RSIY" request accepted                                  				  (Server  send)
	 *  CSUB0- Subscribed fail                                          				  (Server  send)
	 *  CSUB1- subscribed success                                       				  (Server  send)
	 *  
	 *  CSYM - company request to check given symbol is valid and if valid get price and profit (Company send)
	 *  CSYMY- server accept "CSYM" and ask Symbol from company          				  (Server  send)
	 *  S1   - symbol is available and ready to receive                  				  (Server  send)
	 *  S0   - symbol is not available                                  				  (Server  send)
	 *  CSYME- "CSYM" process is completed                             				 	  (Server  send)
	 *  
	 *  CPUR - company request to update profit of Item                                   (Company send)
	 *  CPURY- server accept "CPUR" request and ask details for update    				  (Server  send)
	 *  0    -  invalid symbol                                           				  (Server  send)
	 *  2    - wrong Security code                                     					  (Server  send)
	 *  3    - Success profit update                                   				      (Server  send)
	 *  
	 *  CSUB - company request subscribed list with profit and base price                 (company send)
	 *  CSUBY- server reply to the server accept, "CSUB" request ask user name            (Server  send)
	 *  NEMPTY- server ask the company to be ready to start receive data               	  (Server  send)
	 *  EMPTY- server tells the company, there is no subscribed items                     (Server  send)
	 *  CSUBE- server tells the company, process is completed                       	  (Server  send)
	 *  
	 *  CADD - company request to add a new Item                                          (Company send)
	 *  CADDY- server accept "CADD" request ask Details                 				  (Server  send)
	 *  0    - can not add new Item                                     				  (Server  send)
	 *  2    - already available symbol                                 				  (Server  send)
	 *  3    - adding new item is success                                  				  (Server  send)
	 *  
	 *  Close- company request to close the connection                                    (Company send)
	 *  OKC - server accept connection close request                     				  (Server  send)
	 */
	
	// receive about service type from company and return
	
	public String cTypOfSevese(Socket s) {
			
		String sTyp = null;
			
		try {
			in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sTyp = msg.reserve(in, s); // receive service type
		
		return sTyp;
	}
	
	// send the service  request 
	public int proformCServese(Socket s, String flg) {
		
		int flag = 0;
		
		if(flg.equals("CRS")) {
			flag = releseCSubList(s);
		}
		else if(flg.equals("CRSI")) {
			flag = SubscribeCItem(s);
		}
		else if(flg.equals("CSYM")){
			flag = checkCAvaSym(s);
		}
		else if(flg.equals("CPUR")) {
			flag = updateProfit(s);
		}
		else if(flg.equals("CSUB")) {
			flag = cSubItemWithDetails(s);
		}
		else if(flg.equals("CADD")) {
			flag = cAddNewItemWithDetails(s);
		}
		
		return flag;
	}
	
	// send Symbol list to company and return 1 if end function 
	public int releseCSubList(Socket s) {
		
		int subLstReFlag = 0;
		
		try {
			in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(s,"CRSY"); // ask the company to be ready to receive list
		
		List<String> symList = getSymList(); // get symbol list
		int sizeArr = symList.size();
		
		msg.sentThSocket(s,Integer.toString(sizeArr)); // send size of the Symbol list
		
		for(int i = 0; i < sizeArr; i++) {
			msg.sentThSocket(s, symList.get(i)); // sent symbol
		}
		
		msg.sentThSocket(s,"CRSE"); //tell the company, Symbol list is end
		
		subLstReFlag = 1;
		
		return subLstReFlag;
		
	}
	
	// get symbol list
	private List<String> getSymList() {
		
		List<String> sym = readcsv.getkeySymList(); // get key set of symbols
		
		return sym;
	}
	
	// to subscribe Symbol and send result to company and after process end return 1 // company
	public int SubscribeCItem(Socket soc) {
		
		int sFlg = 0, upSymflg = 0;
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"CRSIY"); // to start send
		
		String subItmN = msg.reserve(in, soc); // receive subscribe Item with Name
		
		String[] val = subItmN.split(" ");
		
		// send as wrong symbol
		if(readcsv.checkSymble(val[1]) == 0) {
			msg.sentThSocket(soc,"CSUB0");
		}
		else {
			
			try {
				List<String> chek = comSubO.addOrResCom(val[0], val[1]);
				if(chek.contains("1")){
					upSymflg = 1;
				}
				else {
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(upSymflg == 0) {
				sFlg = 1;
				msg.sentThSocket(soc,"CSUB0");
			}
			else {
				msg.sentThSocket(soc,"CSUB1");
				sFlg = 1;
			}
		}
		
		return sFlg;
	}
	
	// get item details of given Symbol and send to client and after end process return 1
	public int checkCAvaSym(Socket soc) {
		
		int sFlg = 0;
		List<String> symlst = new ArrayList<>();
		List<Item> itmlst   = new ArrayList<>();
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"CSYMY"); // to start receive symbol
		
		String symbol = msg.reserve(in, soc); //receive symbol
		
		int chekval = readcsv.checkSymble(symbol); // check Symbol availability
		
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
			msg.sentThSocket(soc, "S0");	// no Symbol like that
		}
		
		msg.sentThSocket(soc, "CSYME"); // end statement
		
		sFlg = 1;
		
		return sFlg;
	}
	
	// profit update and keep track of it 
	//send result to the client and return 1 after process
	public int updateProfit(Socket soc) {
		
		int bflg = 0;
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"CPUR"); // to start receive profit update details
		
		String pUD = msg.reserve(in, soc); //receive "userName symbol Profit Security_Code"
		
		String[] val = pUD.split(" ");
		
		int chekval = readcsv.updateProfit(val[0], val[1], val[2], val[3]); // update value and get result
		
		String chval = Integer.toString(chekval);
		
		msg.sentThSocket(soc, chval); 
		// invalid symbol = 0
		//Wrong Security Code = 2
		//success profit update = 3
		
		bflg = 1;
		return bflg;
		
	}
	
	// profit update and keep track of it 
	//send result to the client and return 1 after process
	public int cAddNewItemWithDetails(Socket soc) {
		
		int bflg = 0;
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"CADDY"); // to start receive profit update details
		
		String pUD = msg.reserve(in, soc); //receive "userName symbol Profit Security_Code"
		
		String[] val1 = pUD.split(" ");
		
		int chekval = readcsv.addNewItem(val1[0], val1[1], val1[2], val1[3], val1[4]); // update value and get result
		
		String chval = Integer.toString(chekval);
		
		msg.sentThSocket(soc, chval); 
		// problem = 0
		//Already available symbol = 2
		//success profit update = 3
		
		bflg = 1;
		return bflg;
		
	}
	
	// get subscribed item with profit and price and 
	//sent to company and after process return 1
	public int cSubItemWithDetails(Socket soc) {
		
		int flg = 0;
		//Item theItem = new Item("Empty", 0, 0);
		List<Item> symDtaList = new ArrayList<>();
		
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msg.sentThSocket(soc,"CSUBY"); // tell the company accept requested and ask user Name
		
		String uName = msg.reserve(in, soc); //receive User name
		
		symDtaList = getSymWithDataList(uName); // get subscribed item with details
		
		if(symDtaList != null) {
			msg.sentThSocket(soc, "NEMPTY"); // ask to be ready to receive subscribe list
		}
		else {
			msg.sentThSocket(soc, "EMPTY"); //  no subscribed item
		}
		
		if(symDtaList != null) {
			
			for(int i = 0; i < symDtaList.size(); i++) {
				Item tmpItem = symDtaList.get(i);
				msg.sentThSocket(soc,tmpItem.getSym()); // to sent symbol
				msg.sentThSocket(soc,Float.toString(tmpItem.getPrice())); // to sent price
				msg.sentThSocket(soc,Integer.toString(tmpItem.getProfit())); // to sent profit
			}
		}
		
		msg.sentThSocket(soc, "CSUBE"); // sent as finish Item List
		
		flg = 1;
		
		return flg;
	}
	
	// get symbol with price and profit Item List and return Item list
	private List<Item> getSymWithDataList(String uName) {
		
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
	
	// get subscribe symbol list with given company name
	private List<String> getSubSymList(String uName) {
		
		List<String> ItmLst = comSubO.addOrResCom(uName, "NOSYMBOL"); // get subscribed symbol list
		return ItmLst;
		
	}
	
	// close given connection 
	public void closeCompanyCon(Socket soc) {
		
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

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
