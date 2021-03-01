package org.github.stevemorse.disnlpbot.nlp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.github.stevemorse.disnlpbot.bot.DisNLPBot;
import org.github.stevemorse.disnlpbot.bot.Post;

public class TimeSlicer {
	private int sliceIndex;
	private Instant startTime;
	private Instant endTime;
	private Long timeSliceSize;
	List<Post> posts = null;
	List<List<Post>> timeSlices = new ArrayList<List<Post>>();
	DisNLPBot bot = new DisNLPBot();

	TimeSlicer(Long timeSliceSize){
		this.timeSliceSize = timeSliceSize;
		this.sliceIndex = 0;
		this.startTime = null;
		this.endTime = null;
	}
	
	public List<List<Post>> slice() {
		posts = bot.readFromFile();
		System.out.println("first post: " + posts.get(0).toString());
		System.out.println("last post: " + posts.get(posts.size() -1).toString());
		startTime = posts.get(0).getTimeStamp();
		endTime = startTime.plus(timeSliceSize, ChronoUnit.HOURS);
		//iterate thru all posts
		posts.stream().forEach(post -> {
			//this does one slice
			timeSlices.add(new ArrayList<Post>());
			if(post.getTimeStamp().isBefore(endTime)) {
				timeSlices.get(sliceIndex).add(post);		
			} else {
				this.sliceIndex++;
				this.startTime = endTime;
				this.endTime = endTime.plus(timeSliceSize, ChronoUnit.HOURS);
			}//else		
		});//lamda
		return timeSlices;
	}//slice
	
}
