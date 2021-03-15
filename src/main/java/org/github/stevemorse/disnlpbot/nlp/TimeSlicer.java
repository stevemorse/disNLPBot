package org.github.stevemorse.disnlpbot.nlp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.github.stevemorse.disnlpbot.bot.DisNLPBot;
import org.github.stevemorse.disnlpbot.bot.Post;

public class TimeSlicer {
	private int sliceIndex;
	private Instant startTime;
	private Instant endTime;
	private Long timeSliceSize;
	List<Post> posts = null;
	List<Post> oneSlice = null;
	List<List<Post>> timeSlices = null;
	DisNLPBot bot = new DisNLPBot();

	TimeSlicer(Long timeSliceSize){
		this.timeSliceSize = timeSliceSize;
		this.timeSlices = new ArrayList<List<Post>>();
		this.oneSlice = new ArrayList<Post>();
		this.sliceIndex = 0;
		this.startTime = null;
		this.endTime = null;
	}
	
	public List<List<Post>> slice() {
		posts = bot.readFromFile();
		System.out.println("first post: " + posts.get(posts.size() -1).toString());
		System.out.println("last post: " + posts.get(0).toString());
		startTime = posts.get(posts.size() -1).getTimeStamp();
		endTime = startTime.plus(timeSliceSize, ChronoUnit.HOURS);
		//iterate thru all posts
		Collections.reverse(posts);
		posts.stream().forEach(post -> {
			//this does one slice
			//System.out.println("slice: " + sliceIndex + " is from: " + startTime + " to: " + endTime);
			if(post.getTimeStamp().isBefore(endTime)) {
				//System.out.println("post time is " + post.getTimeStamp() + " must be after:  " + startTime + " and before: " + endTime);
				oneSlice.add(post);
			} else {
				List<Post> temp = new ArrayList<Post>(oneSlice);
				this.timeSlices.add(temp);
				System.out.println("timeSlices has: " + this.timeSlices.size() + " elements after adding arrayList of: " + this.timeSlices.get(sliceIndex).size() + " posts");
				oneSlice.clear();
				this.startTime = endTime;
				this.endTime = endTime.plus(timeSliceSize, ChronoUnit.HOURS);
				this.sliceIndex++;
			}//else
		});//lamda
		this.timeSlices.add(sliceIndex,oneSlice);
		System.out.println("timeSlices has: " + this.timeSlices.size() + " elements after adding arrayList of: " + this.timeSlices.get(sliceIndex).size() + " posts");
		return timeSlices;
	}//slice	
}
