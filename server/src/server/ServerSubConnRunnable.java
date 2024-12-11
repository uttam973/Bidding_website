package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//create server subscriber socket and waiting for client requests
//ask for user authentication

public class ServerSubConnRunnable implements Runnable{
	
	private ServerSocket subSoc ;
	private int subPort;
	private StoreSubConn ssc;
	
	// this constructor is used to access methods in this class
	public ServerSubConnRunnable() {
		
	}
	
	// to create thread
	public ServerSubConnRunnable(int sport) {
		subPort = sport;
		System.out.printf("%s : publisher subscriber connection is started\n", time());
		ssc = new StoreSubConn();
	}
	
	
	
	@Override
	public void run() {
		
		try {
			subSoc = new ServerSocket(subPort, 300); // create publisher subscriber server socket
			System.out.printf("%s : Wait for a subscriber client....\n", time());
			
			while(true) {
				Socket sub = subSoc.accept(); // wait for client connection request
				System.out.printf("%s : Subscriber Client was connected\n", time());
			
				ServerSubHandler scho = new ServerSubHandler(this,sub); // create thread
				ssc.setWorker(scho); // add ServerSubHandler objects to list
				scho.start(); // start thread
			}
		}
		catch(IOException e) {
			System.out.printf("%s : Subscriber connection Error...\n", time());
		}
	}
	

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

}
