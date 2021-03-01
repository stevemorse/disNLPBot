package org.github.stevemorse.disnlpbot.nlp;

import java.util.List;

import org.github.stevemorse.disnlpbot.bot.Post;
import org.nd4j.linalg.dataset.DataSet;

public class NLPDriver {
	private final static Long timeSliceSize = 24L;
	NLPDriver(){}
	public static void main(String[] args) {
		//NLPDriver driver = new NLPDriver();
		TimeSlicer slicer = new TimeSlicer(timeSliceSize);
		List<List<Post>> slices = slicer.slice();
		TfIdfDataBuilder dataBuilder = new TfIdfDataBuilder(slices);
		List<DataSet> dataSets = dataBuilder.execute();
	}

}
