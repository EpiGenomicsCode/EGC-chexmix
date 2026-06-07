package org.egc.core.motifs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.egc.core.data.motifdb.CountsBackgroundModel;
import org.egc.core.data.motifdb.MarkovBackgroundModel;
import org.egc.core.data.motifdb.WeightMatrix;
import org.egc.core.genome.sequence.RandomSequenceGenerator;
import org.egc.core.data.motifdb.WeightMatrixScoreProfile;
import org.egc.core.data.motifdb.WeightMatrixScorer;
import org.egc.core.utils.Pair;


public class MarkovMotifThresholdFinder {
	private WeightMatrix motif = null;
	private MarkovBackgroundModel back;
	private ArrayList<String> seqSet = new ArrayList<String>(); 
	private static int numTest=100000;
	private int window=300;
	private boolean ROC=false;
	private boolean scored=false;
	private boolean seqGenerated=false;
	private ArrayList<Double> scores;
	
	//Constructors
	public MarkovMotifThresholdFinder(WeightMatrix wm, MarkovBackgroundModel markov){
		this(wm, markov, numTest);
	}
	public MarkovMotifThresholdFinder(WeightMatrix wm, MarkovBackgroundModel markov, int numSim){
		numTest=numSim;
		motif=wm;
		if(wm==null){throw new IllegalArgumentException("No motif specified");}
		back=markov;		
	}
	
	public void setNumTest(int nt){numTest=nt;}
	public void setWin(int w){window=w;}
	public void setROC(boolean r){ROC = r;}
	public void setRandomSeq(ArrayList<String> rand){if(rand.size()>0){seqSet = rand; seqGenerated=true;}}
	
	//Find the motif-scoring threshold for the given specificity rate
	public double execute(double Sp){
		if(Sp<0 || Sp>1){throw new IllegalArgumentException("Invalid Sp value in MarkovMotifThreshold: " + Sp);}

		WeightMatrixScorer scorer = new WeightMatrixScorer(motif);
		double bestThres=0.0;
		
		//Find the scores for the random sequences
		if(!scored){
			//Generate the sequences first 
			//Simulate sequences using the markov background
			if(!seqGenerated){
				RandomSequenceGenerator gen = new RandomSequenceGenerator(back);
				for(int i=0; i<numTest; i++){
					seqSet.add(gen.execute(window));
				}
				seqGenerated=true;
			}
			
			scores=new ArrayList<Double>();
			for(String s : seqSet){
				WeightMatrixScoreProfile profiler = scorer.execute(s);
				scores.add(Double.valueOf(profiler.getMaxScore(profiler.getMaxIndex())));
			}
			Collections.sort(scores);
			scored=true;
		}
			
		//Find the score which corresponds to the required Specificity rate
		int index = (int)((double)scores.size()*(1-Sp));
		bestThres=scores.get(index);
		
		//Print an ROC if required
		if(ROC){
			System.out.println("i\tThreshold\tPerformance\tSp");
			int count=1;
			for(Double d : scores){
				double currThres = d.doubleValue();
				double currSp =(double)count/(double)scores.size(); 
				System.out.println(count+"\t"+currThres+"\t"+currSp);
				count++;
			}
		}
		return bestThres;
	}

	public Score2Sp getMotifROC(){
		ArrayList<Pair<Double,Double>> scoreVsSp = new ArrayList<Pair<Double,Double>>();
		WeightMatrixScorer scorer = new WeightMatrixScorer(motif);
		
		//Find the scores for the random sequences
		if(!scored){
			//Generate the sequences first 
			//Simulate sequences using the markov background
			if(!seqGenerated){
				RandomSequenceGenerator gen = new RandomSequenceGenerator(back);
				for(int i=0; i<numTest; i++){
					seqSet.add(gen.execute(window));
				}
				seqGenerated=true;
			}
			
			scores=new ArrayList<Double>();
			for(String s : seqSet){
				WeightMatrixScoreProfile profiler = scorer.execute(s);
				scores.add(Double.valueOf(profiler.getMaxScore(profiler.getMaxIndex())));
			}
			Collections.sort(scores);
			scored=true;
		}
			
		int count=1;
		for(Double d : scores){
			double currThres = d.doubleValue();
			double currSp =(double)count/(double)scores.size(); 
			scoreVsSp.add(new Pair<Double,Double>(currThres,currSp));
			count++;
		}
		return(new Score2Sp(scoreVsSp));
	}
}
