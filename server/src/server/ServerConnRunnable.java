package server;

//create server socket and waiting for client requests
//ask for user authentication

import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.io.IOException;

public class ServerConnRunnable implements Runnable{
	
	private ServerSocket ss;
	private int sPort;
	
	public ServerConnRunnable() {
		
	}
	
	public ServerConnRunnable(int sport) {
		
		this.sPort = sport;
		System.out.printf("%s : Server is started\n", time());
		
	}
	
	@Override
	public void run() {
		
		try {
			ss = new ServerSocket(sPort,300);
			System.out.printf("%s : Wait for clients....\n", time());
			while(true) {
				Socket s = ss.accept();
				System.out.print(time() + " : " + s);
				System.out.print(" - A client is connected\n");
				
				Thread ct = new Thread() {
					@Override
					public void run() {
						handleClientSocket(s);
					}
				};
				ct.start();
			}
			
		}
		catch(IOException e) {
			System.out.printf("%s : Port Error...\n", time());
		}
	}
	
	public void handleClientSocket(Socket soc) {
		
		ServerAction saobj = new ServerAction();
		String userName = null;
		
		//call to log or create new account to user
		ServerTask stob = new ServerTask();
		int logflag = stob.uselogingtyp(soc);
		System.out.printf("%s : Client logged-in successfully - [%d]\n", time(), logflag);
		
		if(logflag == 1) {
			userName = stob.getUname();
			System.out.printf("%s : Got username after login - [%s]\n", time(), userName);
			
			while(true) {
				String serTyp = saobj.typOfSevese(soc);
				
				if(serTyp.equals("Close")) {
					getSubClientandClose(userName); // sub client close
					saobj.closeClientCon(soc); // close client server client
					System.out.printf("%s : Client was disconnected - [%s]\n", time(), userName);
					break;
				}
				else if(serTyp.equals("0")) {
					saobj.closeClientCon(soc); // close client server client
					System.out.printf("%s : Client was disconnected Without cresting subscriber connection\n", time());
					break;
				}
				saobj.proformServese(soc, serTyp); // do service
			}
		}
		if(logflag == 2) {
			ServerComAction scaobj = new ServerComAction();
			userName = stob.getUname();
			System.out.printf("%s : Got username after login - [%s]\n", time(), userName);
			while(true) {
				String serTyp = scaobj.cTypOfSevese(soc);
				if(serTyp.equals("Close")) {
					getSubClientandClose(userName); // sub client close
					scaobj.closeCompanyCon(soc); // close client server client
					System.out.printf("%s : Company was disconnected - [%s]\n", time(), userName);
					break;
				}
				else if(serTyp.equals("0")) {
					saobj.closeClientCon(soc); // close client server client
					System.out.printf("%s : Client was disconnected Without cresting subscriber connection\n", time());
					break;
				}
				scaobj.proformCServese(soc, serTyp); // do service
			}
		}
		else {
			try {
				soc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	// close socket when needed
	public int socClose(Socket s) {
		if(s != null) {
			try {
				s.close();
				System.out.printf("%s : Socket is not closed yet\n", time());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.printf("%s : Null Socket \n", time());
		}
		
		return 1;
	}
	
	// close server socket
	public int serverSocClose() {
		
		if(ss != null ) {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.printf("%s : Server socket is closed\n", time());
		}
		else {
			System.out.printf("%s : Server Socket is NULL\n", time());
		}
		
		return 1;
	}
	
	// close subscriber connection
	public void getSubClientandClose(String uName) {
		ServerSubHandler ssh = getWithName(uName);
		ssh.closeSubConnection();
	}
	
	// get subscriber client ServerSubHandler to close
	private ServerSubHandler getWithName(String userNam) {
		
		ServerSubHandler ressh = null;
		
		StoreSubConn sco = new StoreSubConn(1);
		
		List<ServerSubHandler> workersList = sco.getSubWorkers();

		for(ServerSubHandler swo : workersList) {
			System.out.printf("%s : [%s] Successfully closed the subscribed connection\n", time(), userNam);

			// check with user name
			if(swo.getSubTUname().equals(userNam)) {
				ressh = swo;
			}
		}
		
		return ressh;
	}
	

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

}
