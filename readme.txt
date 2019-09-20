******************************************************************************************************************************************************************************************************************************************************************************************************
Server

	•If the server was assigned over-loaded at the start it would alternate between active and 
 	 overloaded with a set time to simulate how an over-loaded server would work. Offline servers will never come back online.
	•Query 
		- getRating() - retrieve rating for movie
	•Updates 
		-submitRating() - Only allow user to submit a rating for a movie that already exist
		-submitnewRating() - Only allow user to submit a rating for a new movie that doesn't exist yet
		-updateRating()- Only allow user to update a rating submitted by same user. Checks vector clock to make sure 
	 	 current server is up to date with data client has access to before. If not, it'll trigger gossip.
	•Gossip happens every 10 seconds, it's lower to make testing easier
		-Apply received updates, checks with log to make sure it has not been applied. After, updates vector clock

************************************************************************************************************************************************************************************************************************************************************************************************************
Frontend

	•Selects server that is active to connect to
	•If only servers are available are over-loaded, the frontend will wait for 4 seconds before rechecking else user 
	 would have to try again later
	•Has its own vector clock, merges with new vector clock form server if operation was successful

********************************************************************************************************************************************************************************************************************************************************************************************************************************
Instructions
	1. Compile all the .java files and make sure ratings.csv and movies.csv are in the same directory as the compiled files.
	1. Run "java Servercontrol" which initiates all the servers.
	2. Run "java Client" and enter and id when prompt. If you are going to use multiple clients, you must make sure they all have different ids.
	3. Then in the client, you should be given a menu of options to use the distributed system.

