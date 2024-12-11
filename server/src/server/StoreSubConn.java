package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// this class used to store publisher subscriber clients, ServerSubHandler objects and 
//add, remove and get objects as we want

import java.util.ArrayList;
import java.util.List;

public class StoreSubConn {
	
	private static ArrayList<ServerSubHandler> workerList; // create ServerSubHandler list
	
	// create ServerSubHandle
	public StoreSubConn() {
		this.workerList = new ArrayList<>();
	}
	
	// this constructor used to access this class methods
	public StoreSubConn(int i) {
		
	}
	
	// get list of ServerSubHandle
	public List<ServerSubHandler> getSubWorkers(){
		return workerList;
	}
	
	// add ServerSubHandle to list
	public void setWorker(ServerSubHandler scho) {
		workerList.add(scho);
	}
	
	// remove given ServerSubHandle from list
	public void removeWorker(ServerSubHandler swo) {
		workerList.remove(swo);
		System.out.printf("%s : Sucessfully Removed Subscriber from the List\n", time());
	}

	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
