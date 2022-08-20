package org.github.stevemorse.disnlpbot.bot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.ListIterator;

import org.github.stevemorse.disnlpbot.nlp.TimeSlicer;

public class MakeXMLSlices {
	private final static Long timeSliceSize = 24L;
	MakeXMLSlices(){}
	public static void main(String[] args) {
		MakeXMLSlices out = new MakeXMLSlices();
		TimeSlicer slicer = new TimeSlicer(timeSliceSize);
		List<List<Post>> slices = slicer.slice();
		out.writeXMLFiles(slices);
	}//main
	
	public void writeXMLFiles(List<List<Post>> slices) {
		ListIterator<List<Post>> slicesIter = slices.listIterator();
		int sliceCounter = 0;
		while(slicesIter.hasNext()) {
			List<Post> currentSlice = slicesIter.next();
			writeOneSliceFile(sliceCounter,currentSlice);
			sliceCounter++;
		}//while slicesIter hasNext
	}//writeXMLFiles
	
	public void writeOneSliceFile(int sliceCounter,List<Post> currentSlice) {
		DisNLPBot bot = new DisNLPBot();
		String xmlOutBaseFileName = bot.getResources(bot.getResourceFileName(), "xmlOutBaseFileName");
		String fileName = xmlOutBaseFileName + sliceCounter + ".xml";
		File file = new File(fileName);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			ListIterator<Post> oneSliceIter = currentSlice.listIterator();
			while(oneSliceIter.hasNext()) {
				Post currentPost = oneSliceIter.next();
				String xmlPostStr = maleXMLPostString(currentPost);
				writer.write(xmlPostStr);
			}//while oneSliceIter hasNext
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}//try/catch	
	}//writeOneSliceFile
	
	public String maleXMLPostString(Post currentPost) {
		String xmlPostStr = "";
		String userName = currentPost.getUserName();
		Long userId = currentPost.getUserId();
		Long channelId = currentPost.getChannelId();
		Instant timeStamp = currentPost.getTimeStamp();
		String content = currentPost.getContent();
		
		xmlPostStr = xmlPostStr + "<POST>" + "\n";
		xmlPostStr = xmlPostStr + "<userName>" + userName + "</userName>" + "\n";
		xmlPostStr = xmlPostStr + "<userId>" + userId + "</userId>" + "\n";
		xmlPostStr = xmlPostStr + "<channelId>" + channelId + "</channelId>" + "\n";
		xmlPostStr = xmlPostStr + "<timeStamp>" + timeStamp + "</timeStamp>" + "\n";
		xmlPostStr = xmlPostStr + "<content>" + content + "</content>" + "\n";
		xmlPostStr = xmlPostStr + "</POST>" + "\n";
		
		return xmlPostStr;
	}//maleXMLPostString
	
	
}//class
