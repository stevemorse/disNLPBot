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

	public TimeSlicer(Long timeSliceSize){
		this.timeSliceSize = timeSliceSize;
		this.timeSlices = new ArrayList<List<Post>>();
		this.oneSlice = new ArrayList<Post>();
		this.sliceIndex = 0;
		this.startTime = null;
		this.endTime = null;
	}
	
	public List<List<Post>> slice() {
		posts = bot.readFromFile();
		Collections.sort(posts);
		System.out.println("first post: " + posts.get(0).toString());
		System.out.println("last post: " + posts.get(posts.size() -1).toString());
		startTime = posts.get(0).getTimeStamp();
		endTime = startTime.plus(timeSliceSize, ChronoUnit.HOURS);
		System.out.println(" 1stslice: " + sliceIndex + " is from: " + startTime + " to: " + endTime);
		//iterate thru all posts
		posts.stream().forEach(post -> {
			//this does one slice
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
		System.out.println("timeSlices has: " + this.timeSlices.size() + " elements after adding last arrayList of: " + this.timeSlices.get(sliceIndex).size() + " posts");
		return timeSlices;
	}//slice	
}
