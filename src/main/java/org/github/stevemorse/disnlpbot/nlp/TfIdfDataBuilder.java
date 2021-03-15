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

import org.deeplearning4j.bagofwords.vectorizer.BaseTextVectorizer;
import org.deeplearning4j.bagofwords.vectorizer.TfidfVectorizer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.text.documentiterator.BasicLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.BasicLabelAwareIterator.Builder;
import org.deeplearning4j.text.documentiterator.DocumentIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
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
			String content = getContent(slice);
			System.out.println("content returned from getContent is:\n" + content);
			DataSet ds = vectorize(content,slice);
			dataSets.add(ds);
		});
		return dataSets;	
	}//execute
	
	private String getContent(List<Post> slice) {
		StringBuilder content = new StringBuilder("");
		slice.stream().forEachOrdered(post -> {
			content.append(". ");
			iterLoadData.add(content.toString());
			content.append(post.getContent());
		});
		//System.out.println("doc content: " + content.toString().trim());
		return content.toString().trim();
	}//getContent
	
	private DataSet vectorize(String content, List<Post> slice) {
		/*
		Builder builder = new Builder();
		builder.setTokenizerFactory(new DefaultTokenizerFactory());
		builder.setStopWords(getStopWords());
		TfidfVectorizer vec = builder.build();
		List<String> sourcesList = new ArrayList<String>();
		sourcesList.add("tf_idf");
		LabelsSource labelSource = new LabelsSource(sourcesList);
		Class<?> c = null;
		Field labelsSourceFeild = null;
		try {
			c = vec.getClass();
			System.out.println("class is: " + c.toString());
			labelsSourceFeild = vec.getClass().getDeclaredField("labelsSource");
			System.out.println("labelsSource is: " + labelsSourceFeild.toString());
			labelsSourceFeild.setAccessible(true);
			List<String> sourcesList = new ArrayList<String>();
			sourcesList.add("tf_idf");
			LabelsSource labelSource = new LabelsSource(sourcesList);
			//labelsSourceFeild.set(vec, labelSource);
			labelsSourceFeild.set(vec, sourcesList);
			//vec.getLabelsSource().storeLabel("tf_idf");
		} catch (NoSuchFieldException e) {
			System.out.println("Could not find field named: " + labelsSourceFeild + " in class: " + c.toString() + "\nAll fields: " +  Arrays.asList(c.getDeclaredFields()));
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        */
		/*
		Field labelsSourceFeild = null;
		Class<?> c = null;
		CollectionSentenceIterator iter = new CollectionSentenceIterator(iterLoadData);
		List<String> sourcesList = new ArrayList<String>();
		sourcesList.add("tf_idf");
		LabelsSource labelSource = new LabelsSource(sourcesList);
		TfidfVectorizer vec =  new TfidfVectorizer.Builder()
            .allowParallelTokenization(false)
            .setStopWords(getStopWords())
            .setTokenizerFactory(new DefaultTokenizerFactory())
            .setIterator(iter)
            .build();
		c = vec.getSuperClass();
		System.out.println("super class is: " + c.toString());
		try {
			labelsSourceFeild = vec.getSuperClass().getDeclaredField("labelsSource");
			System.out.println("labelsSource is: " + labelsSourceFeild.toString());
			labelsSourceFeild.setAccessible(true);
			labelsSourceFeild.set(vec, labelSource);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		AccessibleTfidfVectorizer.Builder builder = new AccessibleTfidfVectorizer.Builder();
		builder.setTokenizerFactory(new DefaultTokenizerFactory());
		builder.setStopWords(getStopWords());
		builder.resetVocabCache();
		AccessibleTfidfVectorizer vec = builder.build();
		vec.getVocabCache().vocabWords().clear();
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
