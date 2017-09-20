package edu.berkeley.nlp.assignments.assign1.student;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;

public class KneserNeyLanguageModel implements NgramLanguageModel
{
	static final String STOP = NgramLanguageModel.STOP;
	static final String START = NgramLanguageModel.START;
	long total = 0;
	long[] wordCounter = new long[10];
	int[] x_unigram = new int[10];
	int[] unigram_x = new int[10];
	int[] x_unigram_x = new int[10];
	BigramOpenHashMap bigram_hash = new BigramOpenHashMap();
	TrigramOpenHashMap trigram_hash = new TrigramOpenHashMap();
	int bit_mask = 0xFFFFF;
	int w2_index = 0;
	int w1_index = 0;
	double d = 0.8;
	
	public KneserNeyLanguageModel(Iterable<List<String>> sentenceCollection) {
		System.out.println("Building KneserNeyLanguageModel...");
		int sent = 0;
		for(List<String> sentence : sentenceCollection) {
			sent++;
			if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, START);
			stoppedSentence.add(STOP);
			
			for(int word_ind = 0; word_ind < stoppedSentence.size(); word_ind++)
			{
				int curr_index = EnglishWordIndexer.getIndexer().addAndGetIndex(stoppedSentence.get(word_ind));
				if (curr_index >= wordCounter.length) 
				{
					wordCounter = CollectionUtils.copyOf(wordCounter, wordCounter.length * 2);
					x_unigram = CollectionUtils.copyOf(x_unigram, x_unigram.length * 2);
					unigram_x = CollectionUtils.copyOf(unigram_x, unigram_x.length * 2);
					x_unigram_x = CollectionUtils.copyOf(x_unigram_x, x_unigram_x.length * 2);
					
				}
				wordCounter[curr_index]++;
				
				if(word_ind > 0) // start to record bigram
				{
					// construct 40-bit long bigram_key: w2_index,curr_index
					long bigram_key = ((((long) w2_index) & bit_mask) << 20) | (((long) curr_index) & bit_mask);

					
					// find the pos of key, if it is a new key, return the pos to insert
					int bi_key_pos = bigram_hash.insert(bigram_key); 
					
					// If the bigram is new, then count N[*w] and N[w*]
					if(bigram_hash.getValues()[bi_key_pos] == 1)
					{
						x_unigram[curr_index] += 1;
						unigram_x[w2_index] += 1;
					}
					
					if(word_ind > 1) // start to record trigram
					{
						// construct 60-bit long trigram_key: w1_index,w2_index,curr_index
//						System.out.println(String.valueOf(w1_index)+","+String.valueOf(w2_index)+","+String.valueOf(curr_index));
						long trigram_key = ((((long) w1_index) & bit_mask) << 40) 
								| ((((long) w2_index) & bit_mask) << 20) 
								| (((long) curr_index) & bit_mask);
						
//						System.out.println(trigram_key);
						// find the pos of key, if it is a new key, return the pos to insert
						int tri_key_pos = trigram_hash.insert(trigram_key);

						// If the trigram is new
						if(trigram_hash.getValues()[tri_key_pos] == 1) 
						{
							// count N[*w*]
							x_unigram_x[w2_index] += 1;
							
							// count N[vw*] and N[*vw]
							long vwx_key = ((((long) w1_index) & bit_mask) << 20) | (((long) w2_index) & bit_mask);
							bigram_hash.updateCount(vwx_key, bi_key_pos);
						}		
					}
				}
				if(word_ind > 0) {
					w1_index = w2_index;
				}
				w2_index = curr_index;
			}
		}
		
		
		System.out.println("Done building KneserNeyLanguageModel.");
		
		wordCounter = CollectionUtils.copyOf(wordCounter, EnglishWordIndexer.getIndexer().size());
		x_unigram = CollectionUtils.copyOf(x_unigram, EnglishWordIndexer.getIndexer().size());
		unigram_x = CollectionUtils.copyOf(unigram_x, EnglishWordIndexer.getIndexer().size());
		x_unigram_x = CollectionUtils.copyOf(x_unigram_x, EnglishWordIndexer.getIndexer().size());
		int counter = 0;
		for(int x_ind = 0; x_ind < x_unigram_x.length; x_ind++)
		{
			if(x_unigram_x[x_ind] <= 0)
			{
				counter++;
			}
		}
		System.out.println(counter);
		total = CollectionUtils.sum(wordCounter);
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public double getNgramLogProbability(int[] ngram, int from, int to) {
		// TODO Auto-generated method stub
		// include from, exclude to
		
		// P(w3)
		int w3_index = ngram[to - 1];
		if(w3_index < 0 || w3_index >= wordCounter.length)
		{
			return Math.log(1e-40);
		}
		double prob_w3 = (double)x_unigram[w3_index]/bigram_hash.size();
		
		if(to- from == 1)
		{
			return Math.log(prob_w3);
		}
		
		// P(w3|w2)
		int w2_index = ngram[to - 2];
		if(w2_index < 0 || w2_index >= wordCounter.length || w2_index >= x_unigram_x.length)
		{
			return Math.log(prob_w3);
		}
		if(x_unigram_x[w2_index] <= 0)
		{
			return Math.log(prob_w3);
		}
		double alpha_w2 = d * unigram_x[w2_index] / x_unigram_x[w2_index];
		long x_w2w3_key = ((((long) w2_index) & bit_mask) << 20) | (((long) w3_index) & bit_mask);
		int x_w2w3_per = bigram_hash.getXvw()[bigram_hash.getPosFromKey(x_w2w3_key)];
		double prob_w3_given_w2 = (double)Math.max(x_w2w3_per - d, 0) / x_unigram_x[w2_index] + alpha_w2 * prob_w3;
		
		if(to - from == 2) {
			return Math.log(prob_w3_given_w2);
		}
	
		
		
		int w1_index = ngram[to - 3];
		if(w1_index < 0 || w1_index >= wordCounter.length)
		{
			return Math.log(prob_w3_given_w2);
		}
		long w1w2_key = ((((long) w1_index) & bit_mask) << 20) | (((long) w2_index) & bit_mask);
		int w1w2_pos = bigram_hash.getPosFromKey(w1w2_key);
		int count_w1w2 = bigram_hash.getValues()[w1w2_pos];
		if(count_w1w2 <= 0)
		{
			// the context w1w2 has never occurred before
			return Math.log(prob_w3_given_w2);
		}
		double alpha_w1w2 = d * bigram_hash.getVwx()[w1w2_pos] / count_w1w2;
		long w1w2w3_key = ((((long) w1_index) & bit_mask) << 40) 
				| ((((long) w2_index) & bit_mask) << 20) 
				| (((long) w3_index) & bit_mask);
		double prob_w3_given_w1w2 = (double) Math.max(trigram_hash.getValueFromKey(w1w2w3_key),0) 
				/ bigram_hash.getValues()[w1w2_pos] + alpha_w1w2 * prob_w3_given_w2;

		return Math.log(prob_w3_given_w1w2);
	}

	@Override
	public long getCount(int[] ngram) {
		// TODO Auto-generated method stub
		if(ngram.length == 0)
		{
			return 0;
		}else if(ngram.length == 1) {
			int word_index = ngram[0];
			if(word_index < 0 || word_index >= wordCounter.length)
				return 0;
			return wordCounter[word_index];
		}else if(ngram.length == 2) {
			long key = 0;
			for(int word_index : ngram)
				key = (key << 20) | (word_index & bit_mask);
			return bigram_hash.getValueFromKey(key);
		}else if(ngram.length == 3) {
			long key = 0;
			for(int word_index : ngram)
				key = (key << 20) | (word_index & bit_mask);
			return trigram_hash.getValueFromKey(key);
		}
		return 0;
		
		
		
	}

}