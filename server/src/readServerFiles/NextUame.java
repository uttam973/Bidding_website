package readServerTxtFiles;

/*
 * this class is used to get the last company ID
 * stored it in a variable
 * calculate the next user name and return it
 * store the last user in lastUserId.txt at end of the program
*/
import java.io.File;
import java.util.Scanner;

//read useDetTxt.txt file and store the last Id

public class NextUame {
	
	String ProDataF = "txtFiles/useDetTxt.txt";
	FileHandle fobj = new FileHandle();
	
	private String ppin= "";
	private static int cpin = 0 ;
	
	//read last value in string file
	private String getPPin(String pdf){
		
		String[] values = new String[4];
		
		try{
			File fru = new File(pdf);
			Scanner scn = new Scanner(fru);
			
			String a = "";
			
			while(scn.hasNextLine()){
				a = scn.nextLine();
			}
			values = a.split(",");
			
			scn.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		ppin = values[0];
		
		return ppin;
	}
	
	//store last user name
	public void getLastpin() {
		 cpin = Integer.parseInt(getPPin(ProDataF));
	}
	
	//Generate the next user name and keep it for the next calculations
	public synchronized String genPin(){
		cpin = cpin + 1;
		return String.format("%06d", cpin);
	}

}
