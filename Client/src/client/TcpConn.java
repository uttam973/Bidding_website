package client;

//get and close socket

import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

public class TcpConn {
	
	private String sIP; //Server IP
	private int port;//server port
	private Socket cs;
	
	//constructor for create client socket
	public TcpConn(String sip, int cPort){
		
		sIP  = sip;
		port =cPort;
		
		try {
			cs = new Socket(sIP, port);
			System.out.printf("%s : IP and the port numbers are confirmed\n", time());
			System.out.printf("%s : Client socket created (IP: %s, Port : %d)\n", time(), sIP, port);
		}
		catch(IOException e) {
			System.out.printf("%s : Cannot find the connection (IP: %s, Port : %d)\n", time(), sIP, port);
		}
	}
	
	//to close socket
	public void tcpClose() {
		
		try {
			cs.close();
			System.out.printf("%s : Port is closed\n", time());
		}
		catch(IOException e){
			System.out.printf("%s : Cannot close the port - %s\n", time(), e);
		}
		
	}
	
	//return socket
	public Socket getSocket() {
		return cs;
	}

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
	
}
