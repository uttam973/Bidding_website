package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CMsg {
	
	// send message through the given port
	public void sentThSocket(Socket s, String str) {
		
		DataOutputStream out = null;
		
		try {
			out = new DataOutputStream(s.getOutputStream());
			out.writeUTF(str);
		}
		catch(IOException e) {
			System.out.printf("%s : Message Sending Error\n", time());
		}
		
	}
	
	//receive message through given port
	public String reserve(DataInputStream ins,Socket s) {
		
		String str = null;
		
		try {
			
			while(true) {
				str = ins.readUTF();
				
				if(str.equals(null)) {
					sentThSocket(s, "Please input a valid value");
					continue;
				}
				
				break;
			}
		}
		catch(IOException e) {
			System.out.printf("%s : Message Receiving Error\n", time());
		}
		
		return str;
	}

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}	
}
