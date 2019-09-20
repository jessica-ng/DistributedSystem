import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.InputMismatchException;

public interface ServerInterface extends Remote {
    String returnServerStatus() throws RemoteException;
    ArrayList<String> getRating(String movieid, Vectorclock frontend) throws RemoteException;
    Vectorclock submitRating(String userid, String movieid, String rating, Vectorclock frontend, int serverid,String identifier) throws RemoteException;
    Vectorclock submitnewRating(String userid, String movieid, String rating, Vectorclock frontend, int serverid,String identifier) throws RemoteException;
    void gossip_send(ArrayList<ServerInterface> Servers) throws RemoteException;
    void gossip_receive(ArrayList<ArrayList<String>> updates, int serverid, Vectorclock vectorclock, ArrayList<ServerInterface> activeservers) throws RemoteException;
    Vectorclock updateRating(String userid, String movieid, String rating, Vectorclock frontend, int serverid,String identifier, ArrayList<ServerInterface> activeservers) throws RemoteException;
}
