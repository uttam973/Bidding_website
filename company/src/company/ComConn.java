package company;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComConn implements Connection {
    
    private String serverIP;
    private int serverPort;
    private Socket socket;
 // Constructor to create a socket connection
    public ComConn() {
        this.socket = null; // Initialize socket to null
    }
//    private Socket cs;
    // Constructor to create a socket connection
    @Override
    public void connect(String serverIP, int serverPort) throws IOException {
    	this.serverIP = serverIP;
        this.serverPort = serverPort;

        try {
            this.socket = new Socket(serverIP, serverPort);
            System.out.printf("%s : Connected to server [IP - %s, Port - %d]\n", getCurrentTime(), serverIP, serverPort);
        } catch (IOException e) {
            System.out.printf("%s : Cannot connect [IP - %s, Port - %d]\n", getCurrentTime(), serverIP, serverPort);
            throw e; // Rethrow the IOException for handling at a higher level
        }
        
		}
    

    // Method to close the socket
    @Override
    public void close() throws IOException {
        socket.close();
        System.out.printf("%s : Socket closed\n", getCurrentTime());
    }

    // Method to get the socket object
    @Override
    public Socket getSocket() {
    	
        return socket;
    }

    // Method to get the current time
    private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}