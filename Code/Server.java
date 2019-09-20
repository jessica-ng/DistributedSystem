import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

	
public class Server implements ServerInterface {
	private String ServerStatus;
	private String StubName;
	private int connections; 
	private Moviedata moviedata;
	private ArrayList<ArrayList<String>> updates;
	private Vectorclock vectorclock;
	private ArrayList<String> log; 
	private int id;
	private ArrayList<String> PossibleStatus;

	public Server(String stub, int serverid){
		PossibleStatus = new ArrayList<String>();
		PossibleStatus.add("active");
		PossibleStatus.add("active");
		PossibleStatus.add("over-loaded");
		PossibleStatus.add("offline");
		log = new ArrayList<String>();
		id = serverid;
		vectorclock = new Vectorclock(3);
		updates = new ArrayList<ArrayList<String>>();
		moviedata = new Moviedata();
		ServerStatus = "offline";
		connections = 0;	
		StubName = stub;	
	}
	
	public String returnServerStatus(){
		//System.out.println(StubName+ServerStatus);
		return ServerStatus;
	}


	public void updateServerStatus(){
		//Function to arbitarily set server status
		Random rand = new Random(); 
		ServerStatus = PossibleStatus.get(rand.nextInt(2));
		return;
	}

	public ArrayList<String> getRating(String movie, Vectorclock frontend) throws RemoteException{
		ArrayList<String> rating = moviedata.getRating(movie);
		System.out.println("Getting rating...");
		if (rating!=null){
			return rating;
		}
		throw new RemoteException("Movie does not exist.");

	}

	//Submit rating for existing movie
  public Vectorclock submitRating(String userid, String movie, String rating, Vectorclock frontend, int serverid, String identifier) throws RemoteException{
		if(checklog(identifier)){
			System.out.println("Submitting rating...");
			moviedata.submitRating(userid, movie, rating);
			ArrayList<String> ratinginfo = new ArrayList<String>();
			ratinginfo.add("2");
			ratinginfo.add(userid);
			ratinginfo.add(movie);
			ratinginfo.add(rating);
			ratinginfo.add(identifier);
			updates.add(ratinginfo);
			log.add(identifier);
			vectorclock.updateVectorclock(serverid);
			if(serverid==id){
				frontend.updateVectorclock(serverid);
			}			
		}		
		return frontend;
	}

	//Submit rating for new movie
	public Vectorclock submitnewRating(String userid, String movie, String rating, Vectorclock frontend, int serverid, String identifier) throws RemoteException, InputMismatchException{
		if(checklog(identifier)){
			try{
				System.out.println("Submitting rating for new movie...");
				moviedata.submitnewRating(userid, movie, rating);
				ArrayList<String> ratinginfo = new ArrayList<String>();
				ratinginfo.add("3");
				ratinginfo.add(userid);
				ratinginfo.add(movie);
				ratinginfo.add(rating);
				ratinginfo.add(identifier);
				updates.add(ratinginfo);
				log.add(identifier);
				vectorclock.updateVectorclock(serverid);
				if(serverid==id){
					frontend.updateVectorclock(serverid);
				}	
			}
			catch(InputMismatchException e){
				throw new InputMismatchException("Movie is not new");
			}		
		}		
		return frontend;
	}

	//Update existing rating
	public Vectorclock updateRating(String userid, String movieid, String rating, Vectorclock frontend,int serverid,String identifier, ArrayList<ServerInterface> activeservers) throws RemoteException{
		if (vectorclock.checkVectorclocksstate(frontend)==false){
			for (ServerInterface server : activeservers){
				server.gossip_send(activeservers);
			}	
		}
		if (vectorclock.checkVectorclocksstate(frontend)==true){
			System.out.println("Updating rating...");
			if (moviedata.updateRating(userid, movieid, rating)==true){
				ArrayList<String> updateratinginfo = new ArrayList<String>();
				updateratinginfo.add("4");
				updateratinginfo.add(userid);
				updateratinginfo.add(movieid);
				updateratinginfo.add(rating);
				updateratinginfo.add(identifier);
				updates.add(updateratinginfo);
				log.add(identifier);
				vectorclock.updateVectorclock(serverid);
				if(serverid==id){
					frontend.updateVectorclock(serverid);
				}
				return frontend;
			}
			else{
				throw new RemoteException("You need to submit a rating before updating it.");
			}
		}
		return frontend;
	}

	public boolean checklog(String identifier){
		//Going through log to check whether the operation has occured already using identifier 
		//which is unique to each operation generated by the frontend
		for(ArrayList<String> update : updates){
			if(update.get(4).equals(identifier)){
				return false;
			}
		}
		return true;
	}

	
  public void init() {
	try {
		// Create remote object stub from server object
		ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);

		// Get registry
		Registry registry = LocateRegistry.getRegistry("127.0.0.1", 8801);

		// Bind the remote object's stub in the registry
		registry.bind(StubName, stub);

		// Write ready message to console
		System.out.println(StubName + " ready");

		//Select random server status
		//it's set to more likely to get an active status for testing purposes
		Random rand = new Random(); 
		ServerStatus = PossibleStatus.get(rand.nextInt(PossibleStatus.size()));
		System.out.println(StubName+" "+ServerStatus);
		
		//Timer for alternating between over-loaded and active
		if(ServerStatus.equals("over-loaded")){
			TimerTask repeatedTask = new TimerTask() {
				public void run() {
					alternateStatus();	
				}
		};
		Timer timer = new Timer("Timer");
		long delay  = 3000L;
		long period = 3000L;
		timer.scheduleAtFixedRate(repeatedTask, delay, period);
		}
			
	} 
	catch (Exception e) {
		System.err.println("Server exception: " + e.toString());
		e.printStackTrace();
	}
	}
	
	//Setting Status to over-loaded or active
	public void alternateStatus(){
		if(ServerStatus.equals("over-loaded")){
			ServerStatus = "active";
		}
		else{
			ServerStatus = "over-loaded";
		}
		return;
	}
		
	public void gossip_send(ArrayList<ServerInterface> Servers){
		//Sends other servers the updates it had, with types of update, info in update etc.
		//Sends its vector clock for checking with the other server that they are not up to date		
		try{
			for(ServerInterface server: Servers){
				if(Servers.indexOf(server)!=id){
					server.gossip_receive(updates,id,vectorclock,Servers);
				}
			}
		}
		catch (Exception e) {			
		}
		if(Servers.size()==3){
			updates.clear();
		}
	}

	public void gossip_receive(ArrayList<ArrayList<String>> updates,int serverid, Vectorclock vc, ArrayList<ServerInterface> Servers){
		ArrayList<ArrayList<String>> updateratings = new ArrayList<ArrayList<String>>();
		for(ArrayList<String> update : updates){
			//Check if operation has been done already
			if(vectorclock.checkVectorclocks(vc,serverid)==false && log.contains(update.get(4))==false){
				//Only does submit first
				if(update.get(0).equals("2")){
					try{
						submitRating(update.get(1),update.get(2),update.get(3),this.vectorclock,serverid,update.get(4));
					}
					catch(RemoteException e){
						System.out.println(e.getMessage());
					}
				}
				if(update.get(0).equals("3")){
					try{
						submitnewRating(update.get(1),update.get(2),update.get(3),this.vectorclock,serverid,update.get(4));
					}
					catch(RemoteException e){
						System.out.println(e.getMessage());
					}
				
				}
				if(update.get(0).equals("4")){
					updateratings.add(update);
				}
			}
			
		}
		//Only after all submits are done then updates are proceeded
		for(ArrayList<String> updaterating : updateratings){
			try{
				updateRating(updaterating.get(1),updaterating.get(2),updaterating.get(3),this.vectorclock,serverid,updaterating.get(4),Servers);
			}
			catch(RemoteException e){
				System.out.println(e.getMessage());
			}
						
		}
		//System.out.println(StubName + vectorclock.returnVectorclock());
	}


}
