package org.github.stevemorse.disnlpbot.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.github.stevemorse.disnlpbot.bot.AppendingObjectOutputStream;
import org.github.stevemorse.disnlpbot.bot.DisNLPBot;
import org.github.stevemorse.disnlpbot.bot.Post;
import org.deeplearning4j.bagofwords.vectorizer.TfidfVectorizer;
import org.nd4j.linalg.dataset.DataSet;

public class TfIdfDataBuilder {
	List<List<Post>> slices = null;
	List<DataSet> dataSets = new ArrayList<DataSet>();
	int writeFileCounter;
	
	TfIdfDataBuilder(){
		this.writeFileCounter = 0;
	}
	TfIdfDataBuilder(List<List<Post>> slices){
		this.slices = slices;
		this.writeFileCounter = 0;
	}
	
	public List<DataSet> execute() {
		slices.stream().forEachOrdered((Consumer<? super List<Post>>) slice -> {
			String content = getContent(slice);
			DataSet ds = vectorize(content);
			dataSets.add(ds);
		});
		return dataSets;	
	}//execute
	
	private String getContent(List<Post> slice) {
		StringBuilder content = new StringBuilder("");
		slice.stream().forEachOrdered(post -> {
			content.append(post.getContent());
		});
		System.out.println("doc content: " + content.toString());
		return content.toString();
	}//getContent
	
	private DataSet vectorize(String content) {
		TfidfVectorizer vec = new TfidfVectorizer();
		//vec.stopwords = getStopWords();
		DataSet ds = vec.vectorize(content,"tf_idf");
		return ds;
	}//vectorize
	
	private List<String> getStopWords(){
		List<String> stopwords = new ArrayList<String>();
		DisNLPBot bot = new DisNLPBot();
		File stopwordsFile = new File(bot.getResources(bot.getResourceFileName(),"stopwords"));
		try {
			BufferedReader reader = new BufferedReader(new FileReader(stopwordsFile));
			String stopword; 
			while ((stopword = reader.readLine()) != null) {
				stopwords.add(stopword);
				System.out.println(stopword); 
		  	}//while
			reader.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}//catch
		return stopwords;	
	}//getStopWords
	
	public void writeDataSets(List<DataSet> dataSets) {
		dataSets.stream().forEachOrdered(ds -> {	
			try {
				String filename = "timeslice" + this.writeFileCounter++;
				File f= new File(filename);
				FileOutputStream fos = null;
				ObjectOutputStream oos = null;
				if(f.exists()) {
					fos = new FileOutputStream(f);
					oos = new AppendingObjectOutputStream(fos);
				}//if
				else {
					fos = new FileOutputStream(f);
					oos = new ObjectOutputStream(fos);
				}//else
				System.out.println("File: " + f.getName() + " at : " + f.getCanonicalPath());
				oos.writeObject(ds);
				oos.flush();
				oos.close();
				fos.close();
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
				ioe.printStackTrace();
			}//catch
		});
	}//writeDataSets
}
