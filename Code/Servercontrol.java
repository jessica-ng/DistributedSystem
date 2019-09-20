import java.util.*;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Servercontrol{
    private Server server1;
    private Server server2;
    private Server server3;
    private ArrayList<Server> Servers;
    private ArrayList<ServerInterface> Serverstubs;
    private int numberofserver;


    public Servercontrol(){
        /*if (System.getSecurityManager() == null){
            System.setSecurityManager ( new RMISecurityManager() );
        }*/
        numberofserver = 3;
        Serverstubs = new ArrayList<ServerInterface>();
        Servers = new ArrayList<Server>();
        server1 = new Server("server1",0);
        server2 = new Server("server2",1);
        server3 = new Server("server3",2);
        Servers.add(server1);
        Servers.add(server2);
        Servers.add(server3);
       
        try{
            LocateRegistry.createRegistry(8801);
            server1.init();
            server2.init();
            server3.init();
            // Get registry
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 8801);
            ServerInterface stub = (ServerInterface) registry.lookup("server1");
            ServerInterface stub2 = (ServerInterface) registry.lookup("server2");
            ServerInterface stub3 = (ServerInterface) registry.lookup("server3");
            Serverstubs.add(stub);
            Serverstubs.add(stub2);
            Serverstubs.add(stub3);

            //Timer to schedule gossip between all servers every 10 seconds
            TimerTask repeatedTask = new TimerTask() {
                public void run() {
                    ArrayList<ServerInterface> servers = new ArrayList<ServerInterface>();
                    servers = checkServerStatus();
                    //Gossip is only triggered if there is more than 1 active server
                    if(servers!=null&&servers.size()>1){
                        System.out.println("Gossiping...");
                        for(ServerInterface server : servers){
                            try{
                                server.gossip_send(servers);
                            }
                            catch(Exception e){
                                
                            }
                        }
                    }
                }
            };
            Timer timer = new Timer("Timer");
            long delay  = 10000L;
            long period = 10000L;
            timer.scheduleAtFixedRate(repeatedTask, delay, period);
        }
        catch(Exception e){
            System.out.println(e);
        }

    }

    public ArrayList<ServerInterface> checkServerStatus(){
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
            if(activeServers.size()>0){
                return activeServers;
            }
            if(offlineServers.size()==numberofserver){
                throw new ServersOfflineException("All servers are offline.");
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }


    public static void main(String args[]) {
        Servercontrol servercontrol = new Servercontrol();
    }


}