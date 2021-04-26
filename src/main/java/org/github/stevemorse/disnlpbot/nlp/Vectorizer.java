package org.github.stevemorse.disnlpbot.nlp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.text.documentiterator.BasicLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.documentiterator.BasicLabelAwareIterator.Builder;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.github.stevemorse.disnlpbot.bot.DisNLPBot;
import org.github.stevemorse.disnlpbot.bot.Post;
import org.nd4j.linalg.dataset.DataSet;

public class Vectorizer {
	List<DataSet> dataSets = new ArrayList<DataSet>();
	int writeFileCounter;
	List<String> iterLoadData = null;
	String content = "";
	
	public Vectorizer() {
		iterLoadData = new ArrayList<String>();
	}
	
	public static void main (String[] args) {
		Vectorizer vec = new Vectorizer();
		System.out.println("args[0] is: " + args[0]);
		//System.out.println("args[1] is: " + args[1]);
		vec.process(args);
	}
	
	public void process(String[] args) {
		String filename = args[0]; 
		List<Post> slice = readSliceFromFile(filename);
		content = loadIterFromSlice(slice);
		vectorize(content,slice);
	}
	
	public String loadIterFromSlice(List<Post> slice) {
		StringBuilder content = new StringBuilder("");
		slice.stream().forEachOrdered(post -> {
			iterLoadData.add(content.toString().replaceAll("(,|!|\\?|/|#)", " "));
			content.append(post.getContent().replaceAll("(,|!|\\?|/|#)", " "));
		});
		return content.toString();
	}
	
	public List<Post> readSliceFromFile(String filename){
		ArrayList<Post> posts = new ArrayList<Post>();
		try {
			FileInputStream fis = new FileInputStream(filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Object obj = ois.readObject();
			ois.close();
			posts = (ArrayList<Post>) obj;
			if(posts.size() == 0) {
				System.err.println("failure to obtain any data from input object (<Posts>) file");
			}
		} catch (IOException | ClassNotFoundException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}//catch
		return posts;
	}
	
	public Vectorizer(List<List<Post>> slices){
		this.writeFileCounter = 0;
		this.iterLoadData = new ArrayList<String>();
	}
	
	private DataSet vectorize(String content, List<Post> slice) {
		AccessibleTfidfVectorizer.Builder builder = new AccessibleTfidfVectorizer.Builder();
		builder.setTokenizerFactory(new DefaultTokenizerFactory());
		builder.setStopWords(getStopWords());
		AccessibleTfidfVectorizer vec = builder.build();
		vec.getVocabCache().vocabWords().clear();
		vec.resetVocabCache();
		System.out.println("vocab cache after clear is:\n" + vec.getVocabCache().numWords());
		List<String> sourcesList = new ArrayList<String>();
		sourcesList.add("tf_idf");
		LabelsSource labelSource = new LabelsSource(sourcesList);
		vec.setLabelsSource(labelSource);
		CollectionSentenceIterator iter = new CollectionSentenceIterator(iterLoadData);
		BasicLabelAwareIterator.Builder b = new Builder(iter);
		BasicLabelAwareIterator baIter = b.build();
		vec.setIterator(baIter);
		vec.fit();
		DataSet ds = vec.vectorize(content,"tf_idf");
		System.out.println("data set after vectorize is:\n" + ds.toString());
		VocabCache<VocabWord> vc = vec.getVocabCache();
		System.out.println("vocab cache is:\n" + vc.numWords());
		if(slice.size() != 0) {
			List<String> words = new ArrayList<String>();
			int totalWords = 0;
			for(int count = 0; count < vc.numWords(); count++) {
				totalWords += vc.wordFrequency(vc.wordAtIndex(count));
			}//for
			for(int count = 0; count < vc.numWords(); count++) {
				words.add(vc.wordAtIndex(count) + ", " + vc.wordFrequency(vc.wordAtIndex(count)) + ", " 
						+ vc.wordFrequency(vc.wordAtIndex(count))/(float)totalWords + "\n");
			}//for
			Instant start = slice.get(0).getTimeStamp();
			Instant end = slice.get(slice.size() -1).getTimeStamp();
			printToFile(words,start,end);
		}//if slice size != 0
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
		  	}//while
			reader.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}//catch
		return stopwords;	
	}//getStopWords

	public void printToFile(List<String> words, Instant start, Instant end) {
		String[] temp = start.toString().split("T");
	String startString = temp[0];
	temp = end.toString().split("T");
	String endString = temp[0];
	File f = new File(startString + " - " + endString + ".csv");
	try {
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		System.out.println("print to file: " + f.toString());
			for(String word : words){
				writer.write(word);
				System.out.println(word);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}//printToFile
}//class
