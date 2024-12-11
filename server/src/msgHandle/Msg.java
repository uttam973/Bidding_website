package msgHadle;

import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Msg {
	
	//sent a message through given socket
	public synchronized void sentThSocket(Socket s, String str) {
		
		DataOutputStream out = null;
		
		try {
			out = new DataOutputStream(s.getOutputStream());
			out.writeUTF(str);
		}
		catch(IOException e) {
			System.out.printf("%s : Cannot send messages [%s]\n", time(), str);
		}
		
	}
	
	// receive and return a string from the given socket
	public synchronized String reserve(DataInputStream ins,Socket s) {
		
		String str = null;
		
		try {
			
			while(true) {
				str = ins.readUTF();
				
				if(str.equals(null)) {
					sentThSocket(s, "Null Input. Please send a valid message");
					continue;
				}
				
				break;
			}
		}
		catch(IOException e) {
			System.out.printf("%s : Cannot get messages\n", time());
		}
		
		return str;
	}
	
	//check not null
	public int checkString(String cStr, String checker) {
		
		int chek = 1;
		
		if(cStr.equals(null)) {
			chek = -1;
		}
		
		return chek;
	}

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
