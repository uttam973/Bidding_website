package company;

// this class handle company subscriber connection and process handling

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import companyGUI.CompanyActionGUI;

public class ComSubProcessRunnable implements Runnable{
	
	private Socket subSoc;
	private String name;
	private CompanyActionGUI comacgui;
	private ComMsg comsg;
	private DataInputStream in = null;
	
	public ComSubProcessRunnable(Socket soc, String uNme, CompanyActionGUI thegui) {
		this.subSoc = soc;
		this.name  = uNme;
		this.comacgui  = thegui;
		comsg = new ComMsg();
		
		// create to reserve data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		coSubscriberSocHandle();
	}
	
	// first send company name and get subscriber list with details and set table
	// after reserve server messages and do related action
	private void coSubscriberSocHandle() {
		
		System.out.printf("%s : Subscriber socket handling Process\n", time());
		comsg.sentThSocket(subSoc, name); // send user name
		comacgui.createITMTablemodel(subSoc);
		
		while(true) {
			try {
				String upd = comsg.reserve(in, subSoc); // reserve message
				doCompanySubAction(upd); //call to action
				if(upd.equals("OKC")) {
					break; // end reserve to close connection
				}
			}
			catch(Exception ex) {
				System.out.printf("%s : Server-Subscriber connection was lost\n", time());
				break;
			}
		}
	}
	
	// do action related to message
	private void doCompanySubAction(String cmd) {
		
		try {
			String[] token = cmd.split(" ");
			// update price and refresh table
			if(token[0].equals("PUPDATE")) {
				System.out.printf("%s : SubServer : [%s] - [%s, %s]\n", time(), token[0], token[1], token[2]);
				doPriceUpdateCreTble(token[1],token[2]);
			}
			//update profit and refresh table
			else if(token[0].equals("PRUPDATE")) {
				System.out.printf("%s : SubServer : [%s] - [%s, %s]\n", time(), token[0], token[1], token[2]);
				doProfitUpdateCreTble(token[1],token[2]);
			}
			// add new Symbol and refresh symbol table
			else if(token[0].equals("NEWITEM")) {
				System.out.printf("%s : SubServer : [%s] - [%s]\n", time(), token[0], token[1]);
				doannSymUpdateCreTble(token[1]);
			}
			// close connection
			else if(token[0].equals("OKC")) {
				doCloseSubCompany();
			}
		}
		catch(Exception ex) {
			System.out.printf("%s : Server connection was lost. Subscriber connection was closed\n", time());
			
		}
	}
	
	// do price update and refresh table
	private void doPriceUpdateCreTble(String sym, String prc) {
		System.out.printf("%s : Update Table\n", time());
		comacgui.createPUpdateTable(sym,prc);
	}
	
	// do price update and refresh table
	private void doProfitUpdateCreTble(String sym, String pro) {
		System.out.printf("%s : Update Table\n", time());
		comacgui.createProUpdateTable(sym,pro);
	}
	// do add new symbol and refresh
	private void doannSymUpdateCreTble(String sym) {
		System.out.printf("%s : Update Table\n", time());
		comacgui.newSymbolTable(sym);
		comacgui.createNewSymTable(sym);
	}
	
	// close subscriber connection
	private void doCloseSubCompany() {
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			subSoc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.printf("%s : Subscriber connection was closed successfully\n", time());
	}
	
	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}	
}
