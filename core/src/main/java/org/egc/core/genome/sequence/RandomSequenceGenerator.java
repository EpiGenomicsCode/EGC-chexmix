package org.egc.core.genome.sequence;

import java.io.IOException;
import java.text.ParseException;
import java.util.Random;

import org.egc.core.data.io.BackgroundModelIO;
import org.egc.core.data.motifdb.MarkovBackgroundModel;
import org.egc.core.genome.Genome;
import org.egc.core.genome.Species;
import org.egc.core.gseutils.ArgParser;
import org.egc.core.gseutils.Args;
import org.egc.core.gseutils.Pair;


public class RandomSequenceGenerator {

	private MarkovBackgroundModel markov;
	private int modelLen;
//	private Random rand = new Random();
	private Random rand; 
	
	public RandomSequenceGenerator(MarkovBackgroundModel m){
		markov=m;
		modelLen = markov.getMaxKmerLen();
		rand = new Random();
	}
	
	public RandomSequenceGenerator(MarkovBackgroundModel m, long seed){
		markov=m;
		modelLen = markov.getMaxKmerLen();
		rand = new Random(seed);
	}
	
	public String execute(int len){
		String seq = new String();
		
		//Preliminary bases
		for(int i=1; i<modelLen && i<=len; i++){
			double prob = rand.nextDouble();
			double sum=0; int j=0;
			while(sum<prob){
				String test = seq.concat(int2base(j));
				sum += markov.getMarkovProb(test);
				if(sum>=prob){
					seq = test;
					break;
				}
				j++;
			}
		}
		//Remaining bases
		for(int i=modelLen; i<=len; i++){
			String lmer = seq.substring(seq.length()-(modelLen-1));
			double prob = rand.nextDouble();
			double sum=0; int j=0;
			while(sum<prob){
				String test = lmer.concat(int2base(j));
				sum += markov.getMarkovProb(test);
				if(sum>=prob){
					seq =seq.concat(int2base(j));
					break;
				}
				j++;
			}
		}
		
		return seq;
	}
	
	protected String int2base(int x){
		String c;
		switch(x){
			case 0:
				c="A"; break;
			case 1:
				c="C"; break;
			case 2:
				c="G"; break;
			case 3:
				c="T"; break;
			default:
				c="N";
		}
		return(c);
	}
}
