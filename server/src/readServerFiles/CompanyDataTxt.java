package readServerTxtFiles;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class CompanyDataTxt {
	
	private String fpath = "txtFiles/companyDataTxt.txt";
	
	// call to add a new user or confirm the current user identity
	// if val3 = 1 confirm user - dealWithCompanyTxt(user name, password, 1)
	// if val3 = 2 add new user - dealWithCompanyTxt(user name, password, 2)
	
	public synchronized int dealWithCompanyTxt(String val1, String val2, int val3) {
		
		int flag = 0;
		
		if(val3 == 1) {
			flag = readCompanyTxtToUCmf(val1, val2); // check user name and password
		}
		else {
			FileHandle fho = new FileHandle();
			flag = fho.addNewToFile(val1, fpath); // add new user
		}
		
		return flag;
	}
	
	// check user name and password
	private int readCompanyTxtToUCmf(String uName, String uPwd) {
			
		int rety = 0;
			
		String[] values = new String[4];
			
		try{
			File fru = new File(fpath);
			Scanner scn = new Scanner(fru);
				
			while(scn.hasNextLine()){
				String a = scn.nextLine();
				values = a.split(",");
					
				if(values[0].equals(uName)) {
					if(values[1].equals(uPwd)) {
						rety = 1;
						break;
					}
				}
					
			}
				
				scn.close();
			}
			catch(Exception e){
				System.out.printf("%s : username, password checking error...\n", time());
			}
			
			return rety;
			
		}


	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
