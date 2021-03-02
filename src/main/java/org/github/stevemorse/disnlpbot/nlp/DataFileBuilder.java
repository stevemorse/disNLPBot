package org.github.stevemorse.disnlpbot.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.github.stevemorse.disnlpbot.bot.DisNLPBot;
import org.github.stevemorse.disnlpbot.bot.Post;

public class DataFileBuilder {
	
	public static void main (String[] args) {
		DataFileBuilder dfb = new DataFileBuilder();
		List<Post> posts = dfb.makePostList();
		DisNLPBot bot = new DisNLPBot();
		bot.writeToFile(posts);
	}
	
	public DataFileBuilder() {}
	
	private List<Post> makePostList(){
		List<Post> posts = new ArrayList<Post>();
		File testMessageDataFile = new File("testMessageData.txt");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(testMessageDataFile));
			String line; 
			while ((line = reader.readLine()) != null) {
				posts.add(lineToPost(line));
				//System.out.println(line); 
		  	}//while
			reader.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}//catch
		return posts;	
	}//makePostList
	
	private Post lineToPost(String line) {
		Post post = new Post();
		int startContent = line.indexOf(", \"content\": \"");
		int endContent = line.indexOf("\",", startContent);
		String content = line.substring(startContent + ", \"content\": \"".length(), endContent);
		System.out.println("content: " + content);
		post.setContent(content);
		int startUserId = line.indexOf(", \"author\": {\"id\": \"", endContent);
		int endUserId = line.indexOf("\"," , startUserId);
		String userIdStr = line.substring(startUserId + ", \"author\": {\"id\": \"".length(), endUserId);
		Long userId = Long.valueOf(userIdStr);
		System.out.println("id: " + userId);
		post.setUserId(userId);
		int startUserName = line.indexOf("\", \"username\": \"", endUserId);
		int endUserName = line.indexOf("\", ", startUserName + "\", ".length());
		String userName = line.substring(startUserName + "\", \"username\": \"".length(), endUserName);
		System.out.println("author: " + userName);
		post.setUserName(userName);
		int startTimeStamp = line.indexOf(", \"timestamp\": \"", endUserName);
		int endTimeStamp = line.indexOf("\", \"edited_timestamp\"", startTimeStamp);
		String timeStampStr = line.substring(startTimeStamp + ", \"timestamp\": \"".length(), endTimeStamp);
		Instant timestamp = Instant.parse(timeStampStr);
		System.out.println("timeStamp: " + timestamp.toString());
		post.setTimeStamp(timestamp);
		return post;
	}//lineToPost
}
