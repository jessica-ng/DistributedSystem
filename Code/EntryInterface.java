import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface EntryInterface extends Remote {
    ArrayList<String> getRating(String movieid) throws RemoteException, ServersOfflineException, ServersOverloadedException;
    void submitRating(String userid, String movieid, String rating) throws RemoteException, ServersOfflineException, ServersOverloadedException;
    void submitnewRating(String userid, String movieid, String rating) throws RemoteException, ServersOfflineException, ServersOverloadedException;
    void updateRating(String userid, String movieid, String rating) throws RemoteException, ServersOfflineException, ServersOverloadedException;
}