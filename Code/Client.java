import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Client{
    private Frontend frontend;
    private String frontendid;
    private String userid;

    private Client(String clientid) {
        frontendid = "frontend" + clientid;
        frontend = new Frontend(frontendid);
        userid = clientid;
    }


    public static void main(String[] args) {

        try {
            System.out.println("Please enter user id.");
            Scanner reader = new Scanner(System.in);
            String userid = reader.nextLine();
            Client client = new Client(userid);
            
            // Get registry
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 8801);

            // Lookup the frontend from registry
            // and create a stub for it
            EntryInterface stub = (EntryInterface) registry.lookup(client.returnFrontendid());

            //While loop to keep printing menu asking for command from user
            while(true){
                try{
                    printMenu();
                    String command = reader.nextLine();
                    // Command 1 for get rating
                    if(command.equals("1")){
                        System.out.println("What movie's rating would you like to retrieve?");
                        String input = reader.nextLine();
                        try{
                            ArrayList<String> ratings = stub.getRating(input);
                            System.out.println("Here are the list of ratings for movie " + input);
                            System.out.println(ratings); 
                        }
                    
                        catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                        
                    }
                    // Command 2 for submit rating
                    if(command.equals("2")){
                        System.out.println("What movie's rating would you like to submit?");
                        String movieid = reader.nextLine();
                        System.out.println("What rating would you like to submit?");
                        String rating = reader.nextLine();
                        try{
                            stub.submitRating(userid,movieid,rating);
                            System.out.println("Sucesss");
                        }
                        catch(Exception e){
                            System.out.println(e.getMessage());
                        }

                    }
                    // Command 3 for submit rating for new movie
                    if(command.equals("3")){
                        System.out.println("What new movie's rating would you like to submit?");
                        String movieid = reader.nextLine();
                        System.out.println("What rating would you like to submit?");
                        String rating = reader.nextLine();
                        try{
                            stub.submitnewRating(userid,movieid,rating);
                            System.out.println("Sucesss");
                        }
                        catch(Exception e){
                            System.out.println(e.getMessage());
                        }

                    }
                    // Command 4 for update existing rating
                    if(command.equals("4")){
                        try{
                            System.out.println("Which movie's rating would you like to update?");
                            String movieid = reader.nextLine();
                            System.out.println("What is the new rating?");
                            String rating = reader.nextLine();
                            stub.updateRating(userid, movieid, rating);
                            System.out.println("Sucesss");
                        }
                        catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                    // Command 5 for exciting program
                    if(command.equals("5")){
                        reader.close();
                        System.exit(0);
                        return;
                    }
                }
                catch(Exception e){
                    
                }
                
            }
                
        

            /*// Invoke a remote method
            String response = stub.sayHello();
            System.out.println("response: " + response);*/      

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
    public static void printMenu(){
        System.out.println("1......Retrieve");
        System.out.println("2......Submit rating to an existing movie");
        System.out.println("3......Submit rating to a new movie");
        System.out.println("4......Update movie ratings");
        System.out.println("5......End");
    }

    public String returnFrontendid(){
        return frontendid;
    }

}
