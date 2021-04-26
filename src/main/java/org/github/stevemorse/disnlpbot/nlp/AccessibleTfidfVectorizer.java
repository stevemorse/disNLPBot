package org.github.stevemorse.disnlpbot.nlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.uima.resource.ResourceInitializationException;
import org.datavec.nlp.tokenization.tokenizerfactory.UimaTokenizerFactory;
import org.deeplearning4j.bagofwords.vectorizer.TfidfVectorizer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.DocumentIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIteratorWrapper;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.documentiterator.interoperability.DocumentIteratorConverter;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.interoperability.SentenceIteratorConverter;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

public class AccessibleTfidfVectorizer extends TfidfVectorizer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AccessibleTfidfVectorizer(){}
	 
	public LabelsSource getLabelsSource(){
		return this.labelsSource;
	}//getLabelsSource	
	public void setLabelsSource(LabelsSource ls){
		this.labelsSource = ls;
	}//setLabelsSource
	public TokenizerFactory getTokenizerFactory(){
		return this.tokenizerFactory;
	}//getLabelsSource	
	public void setTokenizerFactory(TokenizerFactory tf){
		this.tokenizerFactory = tf;
	}//setTokenizerFactory
	/*
	public void setTokenizerFactory(@Nonnull UimaTokenizerFactory uimaTokenizerFactory) {
        this.tokenizerFactory = uimaTokenizerFactory;
    }//setTokenizerFactor*/
	public Collection<String> getStopWords() {
		return this.stopWords;
	}//getStopWords
	public void setStopWords(List<String> sw) {
		this.stopWords = sw;
	}//setStopWords
	public LabelAwareIterator getIterator() {
		return this.iterator;
	}//getIterator
	public void setIterator(@Nonnull DocumentIterator iterator) {
        this.iterator = new DocumentIteratorConverter(iterator, labelsSource);
        this.iterator = (LabelAwareIterator) iterator;
    }//setIterator
	public void setIterator(@Nonnull SentenceIterator  iterator) {
        this.iterator = new SentenceIteratorConverter(iterator, labelsSource);
        this.iterator = (LabelAwareIterator) iterator;
    }//setIterator
	public void setIterator(LabelAwareIterator iterator) {
		this.iterator = iterator;
	}//setIterator
	public void setVocab(@Nonnull VocabCache<VocabWord> vocab) {
		this.vocabCache = vocab;
	}//setVocab
	public VocabCache<VocabWord> getVocab() {
		return this.vocabCache;
	}//setVocab
	public void resetVocabCache() {
    	this.vocabCache = new AbstractCache<VocabWord>();
    	if(this.vocabCache.vocabExists()) {
    		this.vocabCache.vocabWords().stream().forEachOrdered(
    				element -> this.vocabCache.removeElement(element)
    				);
    		this.vocabCache.vocabWords().clear();
    	}//if not empty
    	System.out.println("VECTORIZER CLEAR CALLED vocabCache now has: " + this.vocabCache.numWords() + " elements");
    }
	public static class Builder {
        protected TokenizerFactory tokenizerFactory;
        protected LabelAwareIterator iterator;
        protected int minWordFrequency;
        protected VocabCache<VocabWord> vocabCache;
        protected LabelsSource labelsSource = new LabelsSource();
        protected Collection<String> stopWords = new ArrayList<>();
        protected boolean isParallel = true;

        public Builder() {}

        public Builder allowParallelTokenization(boolean reallyAllow) {
            this.isParallel = reallyAllow;
            return this;
        }

        public Builder setIterator(@Nonnull LabelAwareIterator iterator) {
            this.iterator = new LabelAwareIteratorWrapper(iterator, labelsSource);
            return this;
        }

        public Builder setIterator(@Nonnull DocumentIterator iterator) {
            this.iterator = new DocumentIteratorConverter(iterator, labelsSource);
            return this;
        }

        public Builder setIterator(@Nonnull SentenceIterator iterator) {
            this.iterator = new SentenceIteratorConverter(iterator, labelsSource);
            return this;
        }

        public Builder setVocab(@Nonnull VocabCache<VocabWord> vocab) {
            this.vocabCache = vocab;
            return this;
        }

        public Builder setMinWordFrequency(int minWordFrequency) {
            this.minWordFrequency = minWordFrequency;
            return this;
        }

        public Builder setStopWords(Collection<String> stopWords) {
            this.stopWords = stopWords;
            return this;
        }
        
        public void resetVocabCache() {
        	this.vocabCache = new AbstractCache<VocabWord>();
        	if(this.vocabCache.vocabExists()) {
        		this.vocabCache.vocabWords().stream().forEachOrdered(
        				element -> this.vocabCache.removeElement(element)
        				);
        		this.vocabCache.vocabWords().clear();
        	}//if not empty
        	System.out.println("CLEAR CALLED vocabCache now has: " + this.vocabCache.numWords() + " elements");
        }

        public AccessibleTfidfVectorizer build() {
        	AccessibleTfidfVectorizer vectorizer = new AccessibleTfidfVectorizer();

            vectorizer.setTokenizerFactory(this.tokenizerFactory);
            vectorizer.iterator = this.iterator;
            vectorizer.minWordFrequency = this.minWordFrequency;
            vectorizer.setLabelsSource(this.labelsSource);
            vectorizer.isParallel = this.isParallel;

            if (this.vocabCache == null) {
                this.vocabCache = new AbstractCache.Builder<VocabWord>().build();
            }

            vectorizer.vocabCache = this.vocabCache;
            vectorizer.stopWords = this.stopWords;

            return vectorizer;
        }
        /*
		public Builder setTokenizerFactory(UimaTokenizerFactory uimaTokenizerFactory) {
			try {
				this.tokenizerFactory = new UimaTokenizerFactory();
			} catch (ResourceInitializationException e) {
				e.printStackTrace();
			}
			return this;
		}
		*/

		public void setTokenizerFactory(DefaultTokenizerFactory defaultTokenizerFactory) {
			this.tokenizerFactory = new DefaultTokenizerFactory();
		}
    }
}
