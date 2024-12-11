package company;

import java.io.IOException;
import java.net.Socket;

public interface Connection {
    void connect(String serverIP, int serverPort) throws IOException;
    Socket getSocket();
    void close() throws IOException;
}