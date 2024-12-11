package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//timer Runnable class - this used to count server running time

import java.util.Timer;
import java.util.TimerTask;

import csv.ReadCsv;
import serverGui.ServerRunningGUI;

public class ServerTimerRunnable implements Runnable{
	
	private ServerRunningGUI srgo;
	private int ssTime = 0;
	private int spTime = 1000;
	private static int slTime;
	Timer sTimer = new Timer();
	private static int sPass;
	
	TimerTask sTimerTask = new TimerTask() {
		@Override
		public void run() {
			sPass++;
			// stop timer
			if(sPass == slTime/1000) {
				sTimer.cancel(); // stop timer
				ReadCsv reo = new ReadCsv(1);
				reo.setStopBiddingStates(); // to set as bidding time is end
				
				System.out.printf("%s : Server bidding time is over\n", time());
			}
			String tl = "Bidding Time End After : " + Integer.toString((slTime/1000 - sPass)/60) + "min " + Integer.toString((slTime/1000 - sPass)%60) + "s";
			srgo.lblNewLabel.setText(tl);
		}
	};;
	
	//constructor to get input
	public ServerTimerRunnable(int lt, ServerRunningGUI thsrg) {
		slTime = lt*60*1000;
		srgo = thsrg;
	}
	
	//Constructor without parameters
	public ServerTimerRunnable() {
		
	}
	
	@Override
	public void run() {
		start();
	}
	
	private void start() {
		sTimer.scheduleAtFixedRate(sTimerTask,ssTime,spTime);
	}
	
	// add another minute to the server runtime if the server is running on the last minute  
	public synchronized void addTimerMintifLmin() {
		if(sPass*1000 > slTime - 1000*60) {
			slTime = slTime + 60*1000;
			System.out.printf("%s : Another minute was added to the bidding time\n", time());
		}
	}
	
	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

}
