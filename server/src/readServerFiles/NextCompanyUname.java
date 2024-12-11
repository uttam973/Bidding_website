package readServerTxtFiles;

import java.io.File;
import java.util.Scanner;

/*
 * this class is used to 
 * read last userName in lastUserId.txt file
 * store that in a variable
 * calculate the next user name and return
 * store the last user in lastUserId.txt at end of the program
*/
public class NextCompanyUname {
	
	String ProDataF = "txtFiles/companyDataTxt.txt";
	FileHandle fobj = new FileHandle();
	
	private String ppin= "";
	private static int copin = 0 ;
	private static Scanner x;
	
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
		public void getComLastpin() {
			 String lID = getPPin(ProDataF);
			 copin = Integer.parseInt(lID.substring(1,7));
		}
		
		//Generate the next user name and keep it for the next calculations
		public synchronized String genComPin(){
			copin = copin + 1;
			String p = "C" + String.format("%06d", copin);
			return p;
		}

}
