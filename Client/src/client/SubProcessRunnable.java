package client;

// this class is used to handle subscriber client

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import clientGui.ActionGUI;

public class SubProcessRunnable implements Runnable{
	
	private Socket subSoc;
	private String name;
	private ActionGUI acgui;
	private CMsg cmsg;
	private DataInputStream in = null;
	
	public SubProcessRunnable(Socket soc, String uNme, ActionGUI agui) {
		this.subSoc = soc;
		this.name  = uNme;
		this.acgui  = agui;
		cmsg = new CMsg();
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		subscriberSocHandle();
	}
	
	// first send user name and get subscriber list with details and set table
	// after receiving server messages, do related action
	private void subscriberSocHandle() {
		
		System.out.printf("%s : Subscriber socket handling Process\n", time());
		cmsg.sentThSocket(subSoc, name); // send user name
		acgui.createITMTablemodel(subSoc); // create table
		while(true) {
			try {
				String upd = cmsg.reserve(in, subSoc); // receive message
				//System.out.println("Update Reserved From BID : " + upd);
				
				doClientSubAction(upd); //call to action
				if(upd.equals("OKC")) {
					break; // end receive to close connection
				}
			}
			catch(Exception ex) {
				System.out.printf("%s : Server connection was lost. Subscriber connection was closed\n", time());
				break;
			}
		}
	}
	
	// do action related to message
	private void doClientSubAction(String cmd) {
		
		try {
			String[] token = cmd.split(" ");
			
			// update price and refresh table
			if(token[0].equals("PUPDATE")) {
				System.out.printf("%s : SubServer : [%s] - [%s, %s]\n", time(), token[0], token[1], token[2]);
				doPriceUpdateCreTble(token[1],token[2]);
			}
			// profit update and refresh table
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
				doCloseSubClient();
			}
		}
		catch(Exception ex) {
			System.out.printf("%s : Server connection was lost. Subscriber connection was closed\n", time());
		}
	}
	
	// do price update and refresh table
	private void doPriceUpdateCreTble(String sym, String prc) {
		System.out.printf("%s : Update Table\n", time());
		acgui.createPUpdateTable(sym,prc);
	}
	
	// do profit update and refresh table
	private void doProfitUpdateCreTble(String sym, String pro){
		System.out.printf("%s : Update Table\n", time());
		acgui.createProUpdateTable(sym, pro);
	}
	
	// do add new symbol and refresh
	private void doannSymUpdateCreTble(String sym) {
		System.out.printf("%s : Update Table\n", time());
		acgui.newSymbolTable(sym);
		acgui.createNewSymTable(sym);
	}
	
	// close subscriber connection
	private void doCloseSubClient() {
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
