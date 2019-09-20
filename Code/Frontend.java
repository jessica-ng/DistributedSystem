import java.util.*;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Frontend implements EntryInterface{
    public ArrayList<ServerInterface> Serverstubs;
    private int numberofserver;
    private Vectorclock vectorclock;
    private int numberofoperations;
    private String frontendname;

    public Frontend(String stubname){
        frontendname = stubname;
        numberofoperations = -1;
        numberofserver = 3;
        vectorclock = new Vectorclock(numberofserver);
        Serverstubs = new ArrayList<ServerInterface>();
       
        try{
            // Get registry
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 8801);
            ServerInterface stub = (ServerInterface) registry.lookup("server1");
            ServerInterface stub2 = (ServerInterface) registry.lookup("server2");
            ServerInterface stub3 = (ServerInterface) registry.lookup("server3");
            Serverstubs.add(stub);
            Serverstubs.add(stub2);
            Serverstubs.add(stub3);

            // Create remote object stub from server object
            EntryInterface festub = (EntryInterface) UnicastRemoteObject.exportObject(this, 0);

            // Bind the remote object's stub in the registry
            registry.bind(stubname, festub);

            // Write ready message to console
            System.err.println("Entry ready");
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    public static void main(String args[]) {
        Frontend frontend = new Frontend("frontend1");
    }

    public ArrayList<ServerInterface> checkServerStatus() throws ServersOfflineException{
        try{
            String Status = new String();
            ArrayList<ServerInterface> activeServers = new ArrayList<ServerInterface>();
            ArrayList<ServerInterface> offlineServers = new ArrayList<ServerInterface>();
            ArrayList<ServerInterface> overworkedServers = new ArrayList<ServerInterface>();
            for(ServerInterface server : Serverstubs){
                Status = server.returnServerStatus();
                if(Status.equals("active")){
                    activeServers.add(server);
                }
                if(Status.equals("offline")){
                    offlineServers.add(server);
                }
                if(Status.equals("over-worked")){
                    overworkedServers.add(server);
                }
            }
            /*ArrayList<ArrayList<Integer>> Statuses = new ArrayList<ArrayList<Integer>>();
            Statuses.add(activeServers);
            Statuses.add(offlineServers);
            Statuses.add(overworkedServers);*/
            if(activeServers.size()>0){
                return activeServers;
            }
            if(offlineServers.size()==numberofserver){
                throw new ServersOfflineException("All servers are offline. Please try again later.");
            }
            else{
                return null;
            }
        }
        catch (RemoteException e){
            System.out.println(e);
            return null;
        }
    }

    public ArrayList<String> getRating(String movieid) throws ServersOfflineException, ServersOverloadedException{
        ArrayList<ServerInterface> statuses = checkServerStatus();
        if(statuses.equals(null)){
            try{
                Thread.sleep(4000);
                statuses = checkServerStatus();
                if(statuses.equals(null)){
                    throw new ServersOverloadedException("oof");
                }
            }
            catch (InterruptedException e){

            }
        }

        try{
            ArrayList<String> rating = statuses.get(0).getRating(movieid,vectorclock);
            if(rating.size()==0){
                throw new InputMismatchException("Movie has not ratings yet.");
            }
            return rating;
        }
        catch(InputMismatchException e){
            throw new InputMismatchException("Movie has not ratings yet.");
        }  
        catch(RemoteException e){

        }
        throw new ServersOfflineException("All servers are offline. Please try again later.");       
    }

    public void submitRating(String userid, String movieid, String rating) throws ServersOfflineException, ServersOverloadedException{
        ArrayList<ServerInterface> statuses = checkServerStatus();
        if(statuses.equals(null)){
            try{
                Thread.sleep(4000);
                statuses = checkServerStatus();
                if(statuses.equals(null)){
                    throw new ServersOverloadedException("Servers are overloaded please wait and try again");
                }
            }
            catch (InterruptedException e){
            }            
        }
        try{
            numberofoperations = numberofoperations +1;
            String identifier = frontendname + numberofoperations;
            Vectorclock newVectorclock = statuses.get(0).submitRating(userid,movieid,rating,vectorclock,Serverstubs.indexOf(statuses.get(0)),identifier);
            vectorclock.mergeVectorclocks(newVectorclock, Serverstubs.indexOf(statuses.get(0)));
            return;
        }
        catch(RemoteException e){
            System.out.print(e);

        }
        /*catch(InputMismatchException e ){
            throw new InputMismatchException("Movie doesn't exist");
        }*/
        throw new ServersOfflineException("Servers offline");
    }

    public void submitnewRating(String userid, String movie, String rating) throws ServersOfflineException, ServersOverloadedException{
        ArrayList<ServerInterface> statuses = checkServerStatus();
        if(statuses.equals(null)){
            try{
                Thread.sleep(4000);
                statuses = checkServerStatus();
                if(statuses.equals(null)){
                    throw new ServersOverloadedException("Servers are overloaded please wait and try again");
                }
            }
            catch (InterruptedException e){

            }
            
        }
        if(statuses.size()!=0){
            try{
                numberofoperations = numberofoperations +1;
                String identifier = frontendname + numberofoperations;
                Vectorclock newVectorclock = statuses.get(0).submitnewRating(userid,movie,rating,vectorclock,Serverstubs.indexOf(statuses.get(0)),identifier);
                vectorclock.mergeVectorclocks(newVectorclock, Serverstubs.indexOf(statuses.get(0)));
                return;
            }
            catch(RemoteException e){
                System.out.println(e.getMessage());
            }       
            catch(InputMismatchException e){
                throw new InputMismatchException(e.getMessage());
            }
        }
        throw new ServersOfflineException("Servers offline");
    }

    public void updateRating(String userid, String movieid, String rating) throws ServersOfflineException, ServersOverloadedException, InputMismatchException{
        //Update Servers before updating a rating that might exist in the server that it's not connected to 
        //gossip();
        ArrayList<ServerInterface> statuses = checkServerStatus();
        if(statuses.equals(null)){
            try{
                Thread.sleep(4000);
                statuses = checkServerStatus();
                if(statuses.equals(null)){
                    throw new ServersOverloadedException("Servers are overloaded please wait and try again");
                }
            }
            catch (InterruptedException e){
            }
            
        }
        
        //Select an active server to perform update
        try{
            numberofoperations = numberofoperations +1;
            String identifier = frontendname + numberofoperations;
            Vectorclock newVectorclock = statuses.get(0).updateRating(userid,movieid,rating,vectorclock,Serverstubs.indexOf(statuses.get(0)), identifier,statuses);
            numberofoperations =+ 1;
            vectorclock.mergeVectorclocks(newVectorclock, Serverstubs.indexOf(statuses.get(0)));
            return;
        }
        catch(InputMismatchException e){
            throw new InputMismatchException("You need to submit a rating first");
        }
        catch(RemoteException e){

        }
        throw new ServersOfflineException("All servers offline, please wait and try again.");            
    }

    //Function to trigger gossip (only when online or both offline?)
    public void gossip() throws ServersOverloadedException{
        try{
            ArrayList<ServerInterface> statuses = checkServerStatus();
            
            if(statuses.equals(null)){
                    Thread.sleep(4000);
                    statuses = checkServerStatus();
                    if(statuses.equals(null)){
                        throw new ServersOverloadedException("Servers are overloaded please wait and try again");
                    }
            }
            else{
                for (ServerInterface server: statuses){
                    try{
                    server.gossip_send(Serverstubs); 
                    }
                    catch (Exception e){
                    }            
                }      
            }
        }
        catch (InterruptedException e){
        }
        catch(ServersOfflineException e){

        }  
    }

}