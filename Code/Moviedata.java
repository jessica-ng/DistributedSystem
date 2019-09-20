import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

	
public class Moviedata {
    private List<List<String>> records;
    private List<List<String>> movies;

    public Moviedata() {
        records = new ArrayList<>();
        movies = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader("ratings.csv"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    records.add(Arrays.asList(values));
            }
            }
            catch(Exception e){
                System.out.print(e);
            }
            try (BufferedReader br = new BufferedReader(new FileReader("movies.csv"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    movies.add(Arrays.asList(values));
                }
                for(List<String> movie : movies){
                    if(movie.get(1).contains("(")){
                        String[] values = movie.get(1).split(" \\(");
                        movie.set(1,values[0]);
                    }
                    
                }
                return;
            }
            
            catch(Exception e){
                System.out.print(e);
            }
        }
    
		
        public static void main(String args[]) {
            Moviedata moviedata = new Moviedata();
        }

        public ArrayList<String> getRating(String moviename){
            String movieid = checkMovierecords(moviename);
            ArrayList<String> movierating = new ArrayList<String>();
            if(movieid.equals("0")==false){
                
                for(List<String> rating : records){
                    if(movieid.equals(rating.get(1))){
                        movierating.add(rating.get(2));
                    }
                }
                return movierating;
            }
            return movierating;
        }

        public String checkMovierecords(String moviename){
            for(List<String> movie : movies){
                if(moviename.equals(movie.get(1))){
                    return movie.get(0);
                }
            }
            return "0";
        }

        public boolean checkRatings(String movieid, String userid){
            for(List<String> rating : records){
                if(movieid.equals(rating.get(1)) && userid.equals(rating.get(0))){
                    return true;
                }
            }
            return false;
        }

        public void submitRating(String userid, String moviename, String rating) throws InputMismatchException{
            String movieid = checkMovierecords(moviename);
            if(movieid.equals("0")==false){
                if(checkRatings(movieid, userid)){
                    throw new InputMismatchException("Rating for this movie already exists, maybe you want to update instead");
                } 
                List<String> input = new ArrayList<>();
                input.add(userid);
                input.add(movieid);
                input.add(rating);
                records.add(input);
                return;
                
            }
            throw new InputMismatchException("Movie doesn't exist");
            
        }

        public void submitnewRating(String userid, String moviename, String rating) throws InputMismatchException{
            String movieid = checkMovierecords(moviename);
            if(movieid.equals("0")){
                movieid = ""+movies.size();
                List<String> newmovie = new ArrayList<>();
                newmovie.add(movieid);
                newmovie.add(moviename);
                movies.add(newmovie);
                List<String> input = new ArrayList<>();
                input.add(userid);
                input.add(movieid);
                input.add(rating);
                records.add(input);
                return;
            }
            throw new InputMismatchException("Movie is not new");
        }

        public boolean updateRating(String userid, String moviename, String rating){
            String movieid = checkMovierecords(moviename);
            if(!movieid.equals("0")){
                for(List<String> ratings : records){
                    if(movieid.equals(ratings.get(1)) && userid.equals(ratings.get(0))){
                        ratings.set(2,rating);
                        return true;
                    }
                }
            }
            return false;
        }
	 

  
}
