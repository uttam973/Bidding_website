package server;

//check user identity or create new user

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import msgHadle.Msg;
import readServerTxtFiles.TraderDataTxt;
import readServerTxtFiles.CompanyDataTxt;
import readServerTxtFiles.NextCompanyUname;
import readServerTxtFiles.NextUame;


 /*
  *  when connection establish server is waiting for requests
  *  	- [0] = connection close request                                                (Client/Company send to Server)
  *  	- [1] = client logging request                                                  (Client  send to Server )
  *  	- [2] = client registration request                                             (Client  send to Server )
  *  	- [4] = company logging request                                                 (Company send to Server )
  *  	- [5] = company registration request                                            (Company send to Server )
  *  
  *  when request type = [0]
  *  	- [0] = server reply "0", to say server accepted the connection close request   (Server  send to Client/Company)
  *  			- then send server close connection
  *  			- then receive Client/ Company closed socket
  *  
  *  when request type = [1] server waiting for customer logging information
  *  	- [User_Name] = Client send User Name                                           (Client  send to Server )
  *  	- [Password]  = Client send Password                                            (Client  send to Server )
  *  	- [SLOG] or [FLOG]  = Server send reply                                         (Server  send to Client )
  *  			- [SLOG] - if logging success, give access to communicate with the server
  *  			- [FLOG] - if login failed, waiting for next request ([0], [1], [2])
  *  
  *  when request type = [2] server waiting for registration information of Client
  *  	- [User_Name] = Server send user Name to client                                 (Server  send to Client )
  *  	- [User_Name,Password,Name,Email] = send details of client to server            (Client  send to Server )
  * 	- [SREG] or [FREG] = send to the client to say the result                       (Server  send to Client )
  * 			- [SREG] - if registration success, wait for login or exit([0],[1],[2]) 
  * 				After Registration, client have to log or close connection 
  * 			- [FREG] - if registration failed  , wait for other request ([0],[1],[2])
  * 
  * when request type = [4] server waiting for company logging information
  *  	- [User_Name] = Company send User Name                                          (Company send to Server )
  *  	- [Password]  = Company send Password                                           (Company send to Server )
  *  	- [SLOG] or [FLOG]  = Server send reply                                         (Server  send to Company)
  *  			- [SLOG] - if logging success, give access to communicate with the server
  *  			- [FLOG] - if login failed, wait for next request ([0], [4], [5]) 
  * 
  *  when request type = [5] server waiting for registration information of Company
  *  	- [User_Name] = Server send user Name to company                                (Server  send to Company)
  *  	- [User_Name,Password,Name,Email] = send details of Company                     (Company send to Server )
  * 	- [SREG] or [FREG] = send to the client to say the result                       (Server  send to Company)
  * 			- [SREG] - if registration success, wait for login or exit([0],[4],[5]) 
  * 				After Registration, Company have to login or close the connection 
  * 			- [FREG] - if registration failed  , wait for other request ([0],[4],[5])
  */

public class ServerTask {
	
	private NextUame nUmOb = new NextUame(); //get next user name
	private NextCompanyUname ncun = new NextCompanyUname();
	private DataInputStream in = null;
	private String uName = null;
	
	//if client login success return 1 else 0 
	// if company log success return 2 else 0
	public int uselogingtyp(Socket s){
		Msg msg = new Msg();
		int typ = 10, sockflag = 10;
		String rs = null;
		
		// create to receive data from client
		try {
			in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true) {
			
			//receive type 
			// 1 - client logging // 0 - close // 2 - client registration // 4 - company logging // 5 - company registration
			rs = msg.reserve(in,s);
			typ = Integer.parseInt(rs);
			
			// current user logging
			if(typ == 1) {
				
				//Check user authentication
				if(confirmCurrentUser(s) == 1) {
					msg.sentThSocket( s, "SLOG");
					sockflag = 1;
					break;
				}
					
				msg.sentThSocket(s,"FLOG");
				continue;
			}
			// current company logging
			else if(typ == 4) {
				if(confirmCurrentCompany(s) == 1) {
					msg.sentThSocket( s, "SLOG");
					sockflag = 2;
					break;
				}
				
				msg.sentThSocket(s,"FLOG");
				continue;
			}
			//close connection
			else if(typ == 0) {
				msg.sentThSocket( s, "0"); //close socket
				sockflag = 0;
				break;
			}
			
			//new client registration
			else if(typ == 2){
				
				if(addNewUser(s) == 3) {
					msg.sentThSocket( s, "SREG"); //success
					continue; // after go to logging
				}
				
				msg.sentThSocket(s,"FREG"); //error
			}
			else if(typ == 5) {
				if(addNewCompany(s) == 3) {
					msg.sentThSocket( s, "SREG"); //success
					continue; // after go to logging
				}
				
				msg.sentThSocket(s,"FREG"); //error
				
			}
			
		}
		
		return sockflag;
	}
	
	//confirm current user 
	// if success return 1 else 0
	private int confirmCurrentUser(Socket cs) {
		
		//System.out.println("Running oo");
		
		String uNam = getUserName(cs);
		String uPwd = getUserPwd(cs);
		
		TraderDataTxt trobj = new TraderDataTxt();
		int syn = trobj.dealWithTraderTxt(uNam, uPwd, 1);
		
		if(syn == 1) {
			this.uName = uNam;
		}
		
		return syn;
	}
	
	private int confirmCurrentCompany(Socket cs) {
		
		String uNam = getUserName(cs);
		String uPwd = getUserPwd(cs);
		
		CompanyDataTxt coobj = new CompanyDataTxt();
		int syn = coobj.dealWithCompanyTxt(uNam, uPwd, 1);
		
		if(syn == 1) {
			this.uName = uNam;
		}
		
		return syn;
	}
	
	//add new user 
	private int addNewUser(Socket cs) {
		
		int flag = 2;
		
		String newUseName = nUmOb.genPin(); // get next available user ID
		System.out.printf("%s : [%s] new client added\n", time(),newUseName);
		seeUserName(cs ,newUseName );
		
		String newUser = getUserDetails(cs);
		System.out.printf("%s : [%s] new client details\n", time(),newUser);
		System.out.println(newUser);
		
		// if success return 3 else 2
		while(true) {
			flag = addUserToFile(newUser);
			if(flag == 3) {
				break;
			}
		}
		
		return flag;
	}
	
	// add new Company
	private int addNewCompany(Socket cs) {
		
		int flag = 2;
		
		String newUseName = ncun.genComPin(); // get next available user ID
		System.out.printf("%s : [%s] new company added\n", time(),newUseName);
		
		seeUserName(cs ,newUseName );
		String newUser = getUserDetails(cs);
		System.out.printf("%s : [%s] new company details\n", time(),newUser);
		
		// if success return 3 else 2
		while(true) {
			flag = addCompanyToFile(newUser);
			if(flag == 3) {
				break;
			}
		}
		
		return flag;
	}
	
	//get user name from client
	private String getUserName(Socket cs) {
		Msg msg = new Msg();
		String uN = msg.reserve(in,cs);
		return uN;
	}
	
	//get password from client
	private String getUserPwd(Socket cs) {
		Msg msg = new Msg();
		String uP = msg.reserve(in,cs);
		return uP;
	}
	
	// get registration details from client and server
	private String getUserDetails(Socket cs) {
		Msg msg = new Msg();
		String uN = msg.reserve(in,cs);
		return uN;
	}
	
	// to send user name to client or company
	private void seeUserName(Socket cs, String msgusNm) {
		Msg msg = new Msg();
		msg.sentThSocket(cs, msgusNm);
		
	}
	
	// add new client to file
	private int addUserToFile(String useD) {
		
		TraderDataTxt trUobj = new TraderDataTxt();
		int stat = trUobj.dealWithTraderTxt(useD, "", 2);
		
		return stat;
	}
	
	// add new company to file
	private int addCompanyToFile(String useD) {
		
		CompanyDataTxt coUobj = new CompanyDataTxt();
		int stat = coUobj.dealWithCompanyTxt(useD, "", 2);
		
		return stat;
	}
	
	// get user name
	public String getUname() {
		return uName;
	}
	

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

}
