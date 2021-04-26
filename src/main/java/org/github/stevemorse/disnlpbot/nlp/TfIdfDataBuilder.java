package org.github.stevemorse.disnlpbot.nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.uima.resource.ResourceInitializationException;
import org.datavec.nlp.tokenization.tokenizerfactory.UimaTokenizerFactory;
import org.deeplearning4j.bagofwords.vectorizer.BaseTextVectorizer;
import org.deeplearning4j.bagofwords.vectorizer.TfidfVectorizer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.VocabConstructor;
import org.deeplearning4j.text.documentiterator.BasicLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.BasicLabelAwareIterator.Builder;
import org.deeplearning4j.text.documentiterator.DocumentIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.github.stevemorse.disnlpbot.bot.AppendingObjectOutputStream;
import org.github.stevemorse.disnlpbot.bot.DisNLPBot;
import org.github.stevemorse.disnlpbot.bot.Post;
import org.nd4j.linalg.dataset.DataSet;

public class TfIdfDataBuilder {
	List<List<Post>> slices = null;
	List<DataSet> dataSets = new ArrayList<DataSet>();
	int writeFileCounter;
	List<String> iterLoadData = null;
	
	TfIdfDataBuilder(){
		this.writeFileCounter = 0;
		this.iterLoadData = new ArrayList<String>();
	}
	TfIdfDataBuilder(List<List<Post>> slices){
		this.slices = slices;
		this.writeFileCounter = 0;
		this.iterLoadData = new ArrayList<String>();
	}
	
	public List<DataSet> execute() {
		Collections.reverse(this.slices);
		this.slices.stream().forEach(slice -> {
			System.out.println("slice size is:\n" + slice.size());
			String content = getContent(slice,++writeFileCounter);
			System.out.println("content returned from getContent is:\n" + content);
			String contentFile = "slice" + writeFileCounter; 
			//DataSet ds = vectorize(content,slice);
			//dataSets.add(ds);
			String[] args = new String[1];
			args[0] = contentFile;
			System.out.println("args size is: " + args.length);
			Vectorizer.main(args);
		});
		return dataSets;	
	}//execute
	
	private String getContent(List<Post> slice, int writeFileCounter) {
		StringBuilder content = new StringBuilder("");
		slice.stream().forEachOrdered(post -> {
			iterLoadData.add(content.toString().replaceAll("(,|!|\\?|/|#)", " "));
			content.append(post.getContent().replaceAll("(,|!|\\?|/|#)", " "));
		});
		//System.out.println("doc content: " + content.toString().trim());
		writeSliceToFile(slice, writeFileCounter);
		return content.toString().trim();
	}//getContent
	
	private void writeSliceToFile(List<Post> slice, int writeFileCounter) {
		try {
			File f= new File("slice" + writeFileCounter);
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
			oos.writeObject(slice);
			oos.flush();
			oos.close();
			fos.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}//catch
	}//writeSliceToFile
	
/*	
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
*/	
	private List<String> getStopWords(){
		List<String> stopwords = new ArrayList<String>();
		DisNLPBot bot = new DisNLPBot();
		File stopwordsFile = new File(bot.getResources(bot.getResourceFileName(),"stopwords"));
		try {
			BufferedReader reader = new BufferedReader(new FileReader(stopwordsFile));
			String stopword; 
			while ((stopword = reader.readLine()) != null) {
				stopwords.add(stopword);
				//System.out.println(stopword); 
		  	}//while
			reader.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}//catch
		return stopwords;	
	}//getStopWords
	
	public void writeDataSets(List<DataSet> dataSets) {
		System.out.println("number of datasets: " + dataSets.size());
		dataSets.stream().forEachOrdered(ds -> {	
			try {
				String filename = "timeslice" + this.writeFileCounter++ + ".txt";
				File f= new File(filename);		
				ObjectOutputStream oos = null;
				oos = new ObjectOutputStream( new FileOutputStream(f));
				System.out.println("File: " + f.getName() + " at : " + f.getCanonicalPath());
				System.out.println("dataset is: " + ds.toString());
				oos.writeObject(ds);
				oos.flush();
				oos.close();
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
				ioe.printStackTrace();
			}//catch
		});
	}//writeDataSets
	
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
}
