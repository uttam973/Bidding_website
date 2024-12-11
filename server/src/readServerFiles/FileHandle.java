package readServerTxtFiles;

// this contain file handling methods

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileHandle {
	
	public void CreFile(File fp){
		try {
			if(fp.createNewFile()){
				System.out.printf("%s : [%s] File Created \n", time(),fp.getName());
			}
			else{
				System.out.printf("%s : [%s] File exist\n", time(),fp.getName());
			}
		}
		catch(Exception e) {
			System.out.printf("%s : [%s] File Error... ", time(),fp.getName());
		}
	}
	
	// add string to given file
	public int addNewToFile(String udats, String fpath) {
		
		int stat = 2;
		
		try {
			FileWriter fwu = new FileWriter(fpath,true);
			fwu.write(udats);
			fwu.write(System.getProperty( "line.separator" ));
			stat = 3;
			fwu.close();
		}
		catch(IOException e) {
			System.out.printf("%s : [%s] File Error... ", time(), fpath);
		}
		
		return stat;
	}


	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
