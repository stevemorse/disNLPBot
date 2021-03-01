package org.github.stevemorse.disnlpbot.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.deeplearning4j.core.datasets.vectorizer.Vectorizer;
import org.github.stevemorse.disnlpbot.bot.Post;
import org.deeplearning4j.bagofwords.vectorizer.TfidfVectorizer;
import org.nd4j.linalg.dataset.DataSet;

public class TfIdfDataBuilder {
	List<List<Post>> slices = null;
	List<DataSet> dataSets = new ArrayList<DataSet>();
	
	TfIdfDataBuilder(List<List<Post>> slices){
		this.slices = slices;
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
		Vectorizer vec = new TfidfVectorizer();
		vec.vectorize();
		return null;
	}//vectorize
}
