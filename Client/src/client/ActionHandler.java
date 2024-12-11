package client;

import clientGui.ActionGUI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class ActionHandler {
    private ActionGUI acgui;

    public ActionHandler(ActionGUI acgui) {
        this.acgui = acgui;
    }

    // Handle action based on received message
    public void handleAction(String cmd) {
        try {
            String[] token = cmd.split(" ");
            
            // update price and refresh table
            if(token[0].equals("PUPDATE")) {
                System.out.printf("%s : SubServer : [%s] - [%s, %s]\n", time(), token[0], token[1], token[2]);
                doPriceUpdateCreTble(token[1],token[2]);
            }
            // profit update and refresh table
            else if(token[0].equals("PRUPDATE")) {
                System.out.printf("%s : SubServer : [%s] - [%s, %s]\n", time(), token[0], token[1], token[2]);
                doProfitUpdateCreTble(token[1],token[2]);
            }
            // add new Symbol and refresh symbol table
            else if(token[0].equals("NEWITEM")) {
                System.out.printf("%s : SubServer : [%s] - [%s]\n", time(), token[0], token[1]);
                doannSymUpdateCreTble(token[1]);
            }
            // close connection
            else if(token[0].equals("OKC")) {
                doCloseSubClient();
            }
        }
        catch(Exception ex) {
            System.out.printf("%s : Server connection was lost. Subscriber connection was closed\n", time());
        }
    }
    
    // do price update and refresh table
    private void doPriceUpdateCreTble(String sym, String prc) {
        System.out.printf("%s : Update Table\n", time());
        acgui.createPUpdateTable(sym,prc);
    }
    
    // do profit update and refresh table
    private void doProfitUpdateCreTble(String sym, String pro){
        System.out.printf("%s : Update Table\n", time());
        acgui.createProUpdateTable(sym, pro);
    }
    
    // do add new symbol and refresh
    private void doannSymUpdateCreTble(String sym) {
        System.out.printf("%s : Update Table\n", time());
        acgui.newSymbolTable(sym);
        acgui.createNewSymTable(sym);
    }
    
    // close subscriber connection
    private void doCloseSubClient() {
        try {
            // Handle closing subscriber connection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // get current time
    private String time() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}