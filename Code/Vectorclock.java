import java.util.*;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class Vectorclock implements Serializable{
    private ArrayList<Integer> timestamp;
    private int numberofservers;

    public Vectorclock(int servers){
        timestamp = new ArrayList<Integer>();
        numberofservers = servers;
        for(int i=0; i<servers; i++){
            timestamp.add(0);
        }
        

    }

    //Returns values of Vectorclock
    public ArrayList<Integer> returnVectorclock(){
        return timestamp;
    }

    //Update vectorclock by one on the server that operation took place
    public void updateVectorclock(int id){
        if(id<numberofservers){
            timestamp.set(id,timestamp.get(id)+1);
        }
        return;
    }

    //Merge clocks together after operations on server
    public void mergeVectorclocks(Vectorclock New, int id){
        ArrayList<Integer> newVectorclock = New.returnVectorclock();
        if(newVectorclock.get(id)>timestamp.get(id)){
            timestamp.set(id,newVectorclock.get(id));
        }
        return;
    }

    //Checks if two different vectorclocks are equivalent
    public boolean checkVectorclocks(Vectorclock New, int id){
        if(returnVectorclock().equals(New.returnVectorclock())){
            return true;
        }
        else{
            return false;
        }

    }

    public boolean checkVectorclocksstate(Vectorclock New){
        ArrayList<Integer> timestamp = returnVectorclock();
        ArrayList<Integer> fetimestamp = New.returnVectorclock();
        for(int i=0;i<timestamp.size();i++){
            if (fetimestamp.get(i)>timestamp.get(i)){
                return false;
            }
        }
        return true;

    }



}