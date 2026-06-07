package org.egc.chexmix.stats;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.egc.core.data.io.BackgroundModelIO;
import org.egc.core.data.io.RegionFileUtilities;
import org.egc.core.data.motifdb.CountsBackgroundModel;
import org.egc.core.data.motifdb.MarkovBackgroundModel;
import org.egc.core.data.motifdb.WeightMatrix;
import org.egc.core.data.motifdb.WeightMatrixImport;
import org.egc.core.genome.GenomeConfig;
import org.egc.core.genome.location.StrandedRegion;
import org.egc.core.utils.ArgParser;
import org.egc.core.utils.Args;
import org.egc.core.math.stats.StatUtil;
import org.egc.core.motifs.DrawMotifs;

/**
 * Information content of weight matrix build from aligned a set of sequence
 */
public class InformationContent { 
		
	protected double[] IC;
	protected int maxPos=0;
	protected double maxScore=0.0;
	
	public InformationContent (WeightMatrix fm, MarkovBackgroundModel back){
		
		// Get log odds version from frequency matrix
		WeightMatrix wm = new WeightMatrix(fm.length());
        //clone
        for (int i = 0; i < fm.length(); i++) {
        	wm.matrix[i]['A'] = fm.matrix[i]['A'];
        	wm.matrix[i]['C'] = fm.matrix[i]['C'];
        	wm.matrix[i]['G'] = fm.matrix[i]['G'];
        	wm.matrix[i]['T'] = fm.matrix[i]['T'];
        	wm.matrix[i]['a'] = fm.matrix[i]['a'];
        	wm.matrix[i]['c'] = fm.matrix[i]['c'];
        	wm.matrix[i]['g'] = fm.matrix[i]['g'];
        	wm.matrix[i]['t'] = fm.matrix[i]['t'];
        }
        if (!fm.islogodds) { 
        	wm.islogodds = true;
        	if (back !=null) { wm.toLogOdds(back); }
		else { wm.toLogOdds(); }
        }
        
		IC = new double[fm.length()];
		for (int i=0; i < fm.length(); i++){
			double v = 0.0;
			for (int c=0; c < wm.letters.length; c++){
				char letter = wm.letters[c];				
				double f=fm.matrix[i][letter];
				double p=wm.matrix[i][letter];
				v = v + f*p;
			}
			IC[i]=v;
		}
		for (int i=0; i < IC.length; i++){
			if (IC[i] > maxScore){
				maxScore=IC[i]; maxPos=i;
			}
		}		
	}
	
	public double[] getMotifIC(){return IC;}
	public int getMaxPosition(){return maxPos;}
	public double getMaxScore(){return maxScore;}
		
}
