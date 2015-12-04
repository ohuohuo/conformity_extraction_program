package zichuan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import org.json.*;

public class GTandHomo {
	public static void main(String[] args) throws IOException{
		long enterTime = System.currentTimeMillis(); //record the running time
		
		String pathname = "./Updated.txt"; //the raw JSON objects in txt file
	
		File filename = new File(pathname);
		File writename = new File("./lebronjson_final.json");  //Ground Truth file
		File homophily = new File("./lebronjson_homophily.json"); //Homophily results file
		try {
			writename.createNewFile();
			homophily.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//create the output files
		FileWriter fw= new FileWriter(writename);
		BufferedWriter out = new BufferedWriter(fw);
		FileWriter hm = new FileWriter(homophily);
		BufferedWriter hmout = new BufferedWriter(hm);
		
		FileInputStream fis = new FileInputStream(filename);
		JSONObject users = new JSONObject();
		
		//create object for modify raw input data
		AddValue addvalue = new AddValue(fis,users);
		try {
			addvalue.calvalues();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//create object for finding Ground Truth
		GroundTruth gt = new GroundTruth(users);
		try {
			gt.seekgroundtruth();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//create object for Homophily analysis
		Homo ho = new Homo(users,hmout);
		try {
			ho.tryhomo();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		out.write(users.toString());
		out.flush();
		out.close();
		System.out.println("it's done: /Users/zzc/Downloads/lebronjson_final.json");
		hmout.flush();
		hmout.close();
		System.out.println("it's done: /Users/zzc/Downloads/lebronjson_homophily.json");
		
		//this is for running time recording 
		long leaveTime = System.currentTimeMillis();
		double differencetime = (leaveTime - enterTime)/1000.0;
		System.out.println("running time: "+differencetime + "seconds");
	}
}

/*This part is to calculate the properties of each user, like innate opinion & expressed opinion, 
categorize each tweet into sentiment bucket according to their respective sentiment value and so on so forth
*/
class AddValue{
	
	Scanner sc;
	JSONObject users;
	
	JSONArray tweets;
	String sentimentBucket;
	double innateOpinion, expressedOpinion;
	double[] array;
	int positiveTweetsNum;
	int neutralTweetsNum;
	int negativeTweetsNum;
	int numberOfTweets;
	
	public AddValue(FileInputStream fileinputstream, JSONObject usersforvalue){
		sc = new Scanner(fileinputstream, "UTF-8");
		users = usersforvalue;
	}
	
	public void calvalues() throws JSONException{
		//for each user via while loop
		while(sc.hasNext()){
			String inputStreamString = sc.useDelimiter("\\n").next();
			
			JSONObject user = new JSONObject(inputStreamString); // read one user object per while loop
			String userID = String.valueOf(user.getLong("userID"));		
			addotherval(user);
			users.put(userID,user);	
		}
		sc.close();
	}
	
	public void addotherval(JSONObject user) throws JSONException{
		tweets = user.getJSONArray("tweets");
		
		sentimentBucket = new String();
		//initialize the properties
		innateOpinion = 0;
		expressedOpinion = 0;
		array = new double[3];
		positiveTweetsNum = 0;
		neutralTweetsNum = 0;
		negativeTweetsNum = 0;
		numberOfTweets = tweets.length();
		
		//this is to add sentimentBucket domain(property) for each tweet this user has
		//it will traverse all the tweets this user has
		for(int i=0; i<tweets.length(); i++){
			//add sentimentBucket
			double sentimentValue = tweets.getJSONObject(i).getDouble("sentimentValue");
			if(sentimentValue>=0.0 && sentimentValue <3.5){
				sentimentBucket = "negative";
			}
			else if(sentimentValue>=3.5 && sentimentValue<6.5){
				sentimentBucket = "neutral";
			}
			else if(sentimentValue >=6.5 && sentimentValue <=9.0){
				sentimentBucket = "positive";
			}
			else{
				sentimentBucket = "invalid";
			}
				
			tweets.getJSONObject(i).put("sentiment_bucket", sentimentBucket);
			
			//calculate the innate opinion using the first 3 tweets
			if(i<=2){
				innateOpinion = innateOpinion + tweets.getJSONObject(i).getDouble("sentimentValue");
				if(i==2){
					innateOpinion = innateOpinion/3.0;
				}
			}
			
			
			//calculate the expressed opinion using last 3 tweets
			if(i>=tweets.length()-3){
				
				if(i==tweets.length()-3){
					array[0] = tweets.getJSONObject(i).getDouble("sentimentValue");
				}
				else if(i==tweets.length()-2){
					array[1] = tweets.getJSONObject(i).getDouble("sentimentValue");
				}
				else if(i==tweets.length()-1){
					array[2] = tweets.getJSONObject(i).getDouble("sentimentValue");
					Arrays.sort(array);
					expressedOpinion = array[1];
				}
			}
			
			//calculate how many each kind(namely, negative, neutral and positive tweet) of tweets this user has 
			if(tweets.getJSONObject(i).getString("sentiment_bucket") == "negative"){
				negativeTweetsNum++;
			}
			else if(tweets.getJSONObject(i).getString("sentiment_bucket") == "neutral"){
				neutralTweetsNum++;
			}
			else if(tweets.getJSONObject(i).getString("sentiment_bucket") == "positive"){
				positiveTweetsNum++;
			}
		}//here the traversal of all tweets is done for each user
		
		user.put("innate_opinion", innateOpinion);
		user.put("expressed_opinion", expressedOpinion);
		user.put("num_of_tweet", numberOfTweets);
		user.put("negative_tweet_num", negativeTweetsNum);
		user.put("neutral_tweet_num", neutralTweetsNum);
		user.put("positive_tweet_num", positiveTweetsNum);
		
		addbucket(user);
		
	}
	
	public void addbucket(JSONObject user) throws JSONException{
		//put each user's innate opinion to corresponding bucket
		String innateBucket = new String();
		if(innateOpinion>=0.0 && innateOpinion <3.5){
			innateBucket = "negative";
		}
		else if(innateOpinion>=3.5 && innateOpinion<6.5){
			innateBucket = "neutral";
		}
		else if(innateOpinion >=6.5 && innateOpinion <=9.0){
			innateBucket = "positive";
		}
		else{
			innateBucket = "invalid";
		}
		user.put("innate_bucket", innateBucket);
		
		//put each user's final opinion/expressed opinion to corresponding bucket
		String finalBucket = new String();
		if(expressedOpinion>=0.0 && expressedOpinion <3.5){
			finalBucket = "negative";
		}
		else if(expressedOpinion>=3.5 && expressedOpinion<6.5){
			finalBucket = "neutral";
		}
		else if(expressedOpinion >=6.5 && expressedOpinion <=9.0){
			finalBucket = "positive";
		}
		else{
			finalBucket = "invalid";
		}
		user.put("final_bucket", finalBucket);
	}
	
}

class GroundTruth{
	JSONObject users;
	HashSet<String> result = new HashSet<String>();//only users who act as both users and neighbors in our file
    HashSet<String> userIDset = new HashSet<String>();//store all neighbor IDs in it
	int total_negative;
	int total_positive;
	int total_neutral;
	
	int final_negative;
	int final_positive;
	int final_neutral;
    
	public GroundTruth(JSONObject usersforgt){
		users = usersforgt;
	}
	
	public void seekgroundtruth() throws JSONException{
		//set a iterator to traverse users again to get majority neighboring opinion
		@SuppressWarnings("unchecked")
		Iterator<String> i = users.keys();
		
		//for each user... this part will calculate user's ground truth 
		while(i.hasNext()){
			result = new HashSet<String>();
	        userIDset = new HashSet<String>();
	        
	        
			total_negative = 0;
			total_positive = 0;
			total_neutral = 0;
			
			final_negative = 0;
			final_positive = 0;
			final_neutral = 0;
	        
			//get those IDs who both occur as users and neighbors in this file (only these users have properties to compute).
			String userID = i.next().toString();
			String[] followingIDs = new String[users.getJSONObject(userID).getJSONArray("followingIDs").length()];
			
			//store neighbors in userIDset
			for(int index=0; index < users.getJSONObject(userID).getJSONArray("followingIDs").length(); index++){
				followingIDs[index] = users.getJSONObject(userID).getJSONArray("followingIDs").getString(index);
				userIDset.add(followingIDs[index]);
			}
			
			//take advantage of HashSet's property, store users who are both users and neighbors in the file into result
			Iterator<String> innatei = users.keys();
			for(int index2 = 0; index2 < users.length() && innatei.hasNext(); index2++){
				if(!userIDset.add(innatei.next())){
					result.add(innatei.next());
				}
			}
			
			neiborsent(result);
			majneighopin(userID);
			finmajopin(userID);
			stuorcon(userID);
		}	
	}
	
	// calculate the qualified users in result HashSet 
	// properties calculated below will be used to compute ground truth
	public void neiborsent(HashSet<String> result) throws JSONException{
		Iterator<String> resulti = result.iterator();
		while(resulti.hasNext()){
			String resultis = resulti.next();
			
			//calculate the total number of each kind of tweets of this user's neighbors
			total_negative = total_negative + users.getJSONObject(resultis).getInt("negative_tweet_num");
			total_positive = total_positive + users.getJSONObject(resultis).getInt("negative_tweet_num");
			total_neutral = total_neutral + users.getJSONObject(resultis).getInt("negative_tweet_num");
			
			//calculate the total number of each kind of final opinions of this user's neighbors
			if(users.getJSONObject(resultis).getInt("expressed_opinion")>0.0 && users.getJSONObject(resultis).getInt("expressed_opinion")<3.5){
				final_negative++;
			}
			if(users.getJSONObject(resultis).getInt("expressed_opinion")>=3.5 && users.getJSONObject(resultis).getInt("expressed_opinion")<6.5){
				final_neutral++;
			}
			if(users.getJSONObject(resultis).getInt("expressed_opinion")>=6.5 && users.getJSONObject(resultis).getInt("expressed_opinion")<=9.0){
				final_positive++;
			}
		}
	}
	
	public void majneighopin(String userID) throws JSONException{
		//this is to compute majority neighboring opinion for each user
		int[] majority = {total_negative,total_positive,total_neutral};
		Arrays.sort(majority);
		//3rd element of this array has the most large value according to Array.sort method
		if(majority[2]==total_negative){
			users.getJSONObject(userID).put("majority_neighbor_opinion", "negative");
		}
		else if(majority[2]==total_neutral){
			users.getJSONObject(userID).put("majority_neighbor_opinion", "neutral");
		}
		else if(majority[2]==total_positive){
			users.getJSONObject(userID).put("majority_neighbor_opinion", "positive");
		}
	}
	
	public void finmajopin(String userID) throws JSONException{
		//this is to compute final majority opinion for each user
		int[] final_majority ={final_negative,final_neutral,final_positive};
		Arrays.sort(final_majority);
		if(final_majority[2]==final_negative){
			users.getJSONObject(userID).put("final_majority_opinion", "negative");
		}
		else if(final_majority[2]==final_neutral){
			users.getJSONObject(userID).put("final_majority_opinion", "neutral");
		}
		else if(final_majority[2]==final_positive){
			users.getJSONObject(userID).put("final_majority_opinion", "positive");
		}
	}
	
	public void stuorcon(String userID) throws JSONException{
		//decide if a user is stubborn or conforming or neither
		float neg_fraction = users.getJSONObject(userID).getInt("negative_tweet_num")/users.getJSONObject(userID).getInt("num_of_tweet");
		float neu_fraction = users.getJSONObject(userID).getInt("neutral_tweet_num")/users.getJSONObject(userID).getInt("num_of_tweet");
		float pos_fraction = users.getJSONObject(userID).getInt("positive_tweet_num")/users.getJSONObject(userID).getInt("num_of_tweet");
		if((neg_fraction>=0.7 && (users.getJSONObject(userID).getString("majority_neighbor_opinion") == "neutral" || users.getJSONObject(userID).getString("majority_neighbor_opinion") == "positive")) 
				|| (neu_fraction>=0.7 && (users.getJSONObject(userID).getString("majority_neighbor_opinion") == "negative" || users.getJSONObject(userID).getString("majority_neighbor_opinion") == "positive")) 
				|| (pos_fraction>=0.7 && (users.getJSONObject(userID).getString("majority_neighbor_opinion") == "negative" || users.getJSONObject(userID).getString("majority_neighbor_opinion") == "neutral"))){
			users.getJSONObject(userID).put("ground_truth", "stubborn");
		}
		else if((users.getJSONObject(userID).getString("final_bucket") != users.getJSONObject(userID).getString("innate_bucket"))
				&& (  (neg_fraction>=0.3 && (users.getJSONObject(userID).getString("majority_neighbor_opinion") == "negative")) 
				|| ((neu_fraction>=0.3 && (users.getJSONObject(userID).getString("majority_neighbor_opinion") == "neutral")) ) 
				|| ((pos_fraction>=0.3 && (users.getJSONObject(userID).getString("majority_neighbor_opinion") == "positive")) ))){
			users.getJSONObject(userID).put("ground_truth", "conforming");
		}
		else {
			users.getJSONObject(userID).put("ground_truth", "neither");
		}
	}
}

class Homo{
	JSONObject users;
	int num_homophily = 0;
	ArrayList<Integer> al = new ArrayList<Integer>();// store the qualified users who will be taken into computation
	int innate_fraction = 0;
	int valid_neighbors = 0;
	BufferedWriter hmout;
	
	public Homo(JSONObject usersforhomo,BufferedWriter hmoutforhomo){
		users = usersforhomo;
		hmout = hmoutforhomo;
	}
	
	public void tryhomo() throws JSONException, IOException{
		//traverse all users one by one
		@SuppressWarnings("unchecked")
		Iterator<String> ii = users.keys();
		while(ii.hasNext()){
			/*this is to calculate Total fraction of pairs of users and their neighbors in the JSON file 
			 * for which this difference in innate opinions is less than 1*/
			String userID = ii.next().toString();
			
			String[] followingIDs = new String[users.getJSONObject(userID).getJSONArray("followingIDs").length()];
			obsernei(followingIDs,userID);
		}// traversal of all users is done
		
		//type here next....
		hmout.append("The total fraction of edges in the graph for which this difference in opinions is less than 1: "+String.valueOf((float)innate_fraction/(float)valid_neighbors)+"\\\n");
		
		// here is the equation of the ave difference (namely the ave gap)
		float ave_difference = (float)totdiff(al)/(float)al.size();
		String ave_dif = new String("The average difference in opinions between every pair of users connected by an edge in the Twitter graph of certain topic is: " + Float.toString(ave_difference));
		hmout.append(ave_dif);
	}
	
	public void obsernei(String[] followingIDs, String userID) throws JSONException{
		//observe every neighbor
		int[] neighbors_innate = new int[followingIDs.length];
		int this_user_innate = users.getJSONObject(userID).getInt("innate_opinion");
		int[] difference_between_them = new int[followingIDs.length];
		for(int index=0; index < followingIDs.length; index++){
			followingIDs[index] = users.getJSONObject(userID).getJSONArray("followingIDs").getString(index);
			//only look at those qualified neighbors, namely they are also the users in the file
			if(users.has(followingIDs[index]) && (users.getJSONObject(followingIDs[index]).has("innate_opinion"))){
					neighbors_innate[index] = users.getJSONObject(followingIDs[index]).getInt("innate_opinion");
					
					difference_between_them[index] = Math.abs(this_user_innate - neighbors_innate[index]);
					al.add(difference_between_them[index]);
					valid_neighbors++;
					//if the difference is less than and equal to 1 (since sentiment value only contains integer, < 1.5 means <= 1)
					if(neighbors_innate[index]-(users.getJSONObject(userID).getInt("innate_opinion")) < 1.5 ){
						innate_fraction++;
						}
			}
			else{
				//if this neighbor are not qualified, then go to next neighbor
				continue;
			} 	
		}
	}
	
	public int totdiff(ArrayList<Integer> al){
		//this is to compute the average difference in innate opinions between every pair of users and each of their neighbors
		int total_differences = 0;
		//calculate the total difference
		for(int num_of_differences = 0; num_of_differences < al.size(); num_of_differences++){
			total_differences = total_differences + al.get(num_of_differences);
		}
		return total_differences;
	}
}