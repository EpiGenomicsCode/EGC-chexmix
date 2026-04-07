package org.egc.core.data.motifdb;

import java.util.*;
import java.text.DecimalFormat;



/* weight matrix representation as float[][]:
   the first index is the position in the matrix.
   the second index is the letter.  The size of the second dimension
   is MAXLETTERVAL, but only eight of the indexes are valid: A, C, T, G, and the
   four corresponding lower case letters.  The lower case entries should have the same value
   as the upper case entries.
   This uses more space but makes for quick lookups because you can
   use char values as the index */

public class WeightMatrix {

	public static double LOG2 = Math.log(2);
    public static int MAXLETTERVAL = Math.max(Math.max(Math.max('A','C'),Math.max('T','G')),
                                              Math.max(Math.max('a','c'),Math.max('t','g')))
        + 1;
    public static char[] allLetters = {'A','a','C','c','T','t','G','g'};
    public static char[] letters = {'A','C','T','G'};
    public static char[] revCompLetters = {'T','G','A','C'};

    public float[][] matrix;
    public String consensus;
    public String name, version, type, species;
    public boolean islogodds;
    public int zeroOffset=0;
    
    public WeightMatrix (int length) {
        matrix = new float[length][MAXLETTERVAL];                
    }
    
    /** 
     ** the input matrix must follow the definition of WeightMatrix (see comments at the beginning of the java class)
     **/
    public WeightMatrix (float[][] pwm) {
        matrix = pwm; 
        islogodds = true;
    }

    public int length () {return matrix.length;}
    public String getName(){return name;}
    public String getVersion(){return version;}
    public void setNameVerType(String name, String version, String type){
    	this.name = name;
    	this.version = version;
    	this.type = type;
    }
    
    public void setName(String name){
    	this.name = name;
    }
    
    /**
     * Return column i (indexed by A,C,G,T)
     * @param i
     * @return
     */
    public double[] getColumn(int i){
    	double[] col = new double[4];
    	col[0]=matrix[i]['A'];
    	col[1]=matrix[i]['C'];
    	col[2]=matrix[i]['G'];
    	col[3]=matrix[i]['T'];
    	return col;
    }
    
    /**
     * Return complement of column i (indexed by A,C,G,T)
     * @param i
     * @return
     */
    public double[] getCompColumn(int i){
    	double[] col = new double[4];
    	col[0]=matrix[i]['T'];
    	col[1]=matrix[i]['G'];
    	col[2]=matrix[i]['C'];
    	col[3]=matrix[i]['A'];
    	return col;
    }
    /* returns the maximum possible score that a sequence
       could have against the specified matrix */
    public double getMaxScore() {
        float maxscore = 0;
        for (int i = 0; i < matrix.length; i++) {
            float max = Float.NEGATIVE_INFINITY;
            if (matrix[i]['A'] > max) {
                max = matrix[i]['A'];
            }
            if (matrix[i]['C'] > max) {
                max = matrix[i]['C'];
            }
            if (matrix[i]['G'] > max) {
                max = matrix[i]['G'];
            }
            if (matrix[i]['T'] > max) {
                max = matrix[i]['T'];
            }

            maxscore += max;
        }
        return maxscore;
    }
    public double getMinScore() {
        float minscore = 0;
        for (int i = 0; i < matrix.length; i++) {
            float min = Float.POSITIVE_INFINITY;
            if (matrix[i]['A'] < min) {
                min = matrix[i]['A'];
            }
            if (matrix[i]['C'] < min) {
                min = matrix[i]['C'];
            }
            if (matrix[i]['G'] < min) {
                min = matrix[i]['G'];
            }
            if (matrix[i]['T'] < min) {
                min = matrix[i]['T'];
            }

            minscore += min;
        }
        return minscore;
    }
    public boolean equals(Object other) {
        if (other instanceof WeightMatrix) {
            WeightMatrix o = (WeightMatrix)other;
            return (o.name != null &&
                    this.name != null &&
                    this.name.equals(o.name)
                    && o.version != null
                    && this.version != null
                    && this.version.equals(o.version));
        } else {
            return false;
        }
    }
    public int hashCode() {
        return (name.hashCode() + version.hashCode());
    }
    public String toString() {
        return name + ", " + version + ", " + type;
    }

    public WeightMatrix subMatrix(int start, int length) {
        WeightMatrix out = new WeightMatrix(length);
        for (int i = start; i < start + length; i++) {
            out.matrix[i - start] = matrix[i];
        }
        out.name = name + "(" + start + "-" + (start +length + 0.0) + ")";
        out.version = version;
        out.type = type;
        return out;
    }
    public boolean isSame(WeightMatrix other){
    	if (matrix.length!=other.matrix.length)
    		return false;
    	if (matrix[0].length!=other.matrix[0].length)
    		return false;
    	for (int i=0;i<matrix.length;i++){
    		
    		for (int j=0;j<matrix[0].length;j++){
    			if (matrix[i][j]!=other.matrix[i][j])
    				return false;
    		}
    	}
    	return true;
    }
    /* examines the weight matrix to determine if it's a log-odds matrix or not */
    public boolean setLogOdds() {
        islogodds = false;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < allLetters.length; j++) {
                if (matrix[i][allLetters[j]] < 0) {
                    islogodds = true;
                    return true;
                }
            }
        }
        return false;
    }
    /* convert this matrix to log-odds form using the specified background
       model.  You might want to do this if you're scanning for a matrix 
       in a different genome than the matrix's default background model
    */
    public void toLogOdds(MarkovBackgroundModel bgModel) {
        setLogOdds();  // tests for log-oddsness by looking for weights < 0
        if (islogodds) {return;} 
        islogodds = true;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < allLetters.length; j++) {
                matrix[i][allLetters[j]] = (float)(Math.log(Math.max(matrix[i][allLetters[j]], .000001) / bgModel.getMarkovProb(("" + allLetters[j]).toUpperCase()))/LOG2);
            }
        }                
    }
    
    /* convert this matrix to log-odds form using the specified background freqs
     * freqs Maps from A,C,G,T to frequency
     */
	 public void toLogOdds(Map<String, Double> freqs) {
	     setLogOdds();  // tests for log-oddsness by looking for weights < 0
	     if (islogodds) {return;} 
	     islogodds = true;
	     for (int i = 0; i < matrix.length; i++) {
	         for (int j = 0; j < allLetters.length; j++) {
	             matrix[i][allLetters[j]] = (float)(Math.log(Math.max(matrix[i][allLetters[j]], .000001) / freqs.get(("" + allLetters[j]).toUpperCase()))/LOG2);
	         }
	     }                
	 }
    
    /* converts this matrix to log-odds form.  Uses a uniform .25 background model */
    public void toLogOdds() {
        setLogOdds();  // tests for log-oddsness by looking for weights < 0
        if (islogodds) {return;} 
        islogodds = true;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < allLetters.length; j++) {
                matrix[i][allLetters[j]] = (float)(Math.log(Math.max(matrix[i][allLetters[j]], .000001) / .25)/LOG2);
            }
        }        
    }
    /* converts this matrix to frequencies.
       This conversion is in-exact because it uses a default background
       model */
    public void toFrequency() {
        setLogOdds();
        if (!islogodds) {return;}
        islogodds = false;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < allLetters.length; j++) {
                matrix[i][allLetters[j]] = (float)(Math.pow(2,(double)(matrix[i][allLetters[j]])) * .25);
            }
        }
    }
    public void toFrequency(MarkovBackgroundModel bgModel) {
        setLogOdds();
        if (!islogodds) {return;}
        islogodds = false;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < allLetters.length; j++) {
                matrix[i][allLetters[j]] = (float)(Math.pow(2,(double)(matrix[i][allLetters[j]])) * bgModel.getMarkovProb(("" + allLetters[j]).toUpperCase()));
            }
        }
    }

    /**
     * Modifies this WeightMatrix by normalizing the entries.  The matrix
     * must be a frequency matrix.  The result is that the frequencies at each
     * position in the matrix sum to 1.  This method is useful if you've read
     * in a matrix of counts and need to convert to frequencies.
     */
    public void normalizeFrequencies() {
        setLogOdds();
        if (islogodds) {
            return;
        }
        for (int position = 0; position < matrix.length; position++) {
            double sum = 0;
            for (int j = 0; j < letters.length; j++) {
                sum += matrix[position][letters[j]];
            }
            for (int j = 0; j < letters.length; j++) {
                matrix[position][letters[j]] = (float)(matrix[position][letters[j]] / sum);
            }
        }
    }
    
    /* returns a string showing the full matrx in all its numeric glory */
    public static String printMatrix(WeightMatrix matrix) {
        int length = matrix.length();
        StringBuffer out = new StringBuffer();
        DecimalFormat format = new DecimalFormat("##.##");
        out.append("\t");
        for (int i = 0; i < length;i++) {
            out.append(i + "\t");
        }
        out.append("\n");
        out.append("A\t");
        for (int i = 0; i < length; i++) {            
            out.append(format.format(matrix.matrix[i]['A']) + "\t");
        }
        out.append("\n");
        out.append("C\t");
        for (int i = 0; i < length; i++) {
            out.append(format.format(matrix.matrix[i]['C']) + "\t");
        }
        out.append("\n");
        out.append("G\t");
        for (int i = 0; i < length; i++) {
            out.append(format.format(matrix.matrix[i]['G']) + "\t");
        }
        out.append("\n");
        out.append("T\t");
        for (int i = 0; i < length; i++) {
            out.append(format.format(matrix.matrix[i]['T']) + "\t");
        }
        out.append("\n");
        return out.toString();
    }
    
    /* returns a string showing the full matrix in pseudo-transfac format */
    public static String printTransfacMatrix(WeightMatrix matrix, String name) {
        int length = matrix.length();
        StringBuffer out = new StringBuffer();
        DecimalFormat format = new DecimalFormat("##.##");
        
        out.append("DE\t"+name+"\n");
        for (int i = 0; i < length; i++) {            
            out.append(i+1 +"\t"+ 
            		format.format(matrix.matrix[i]['A']) + "\t" +
            		format.format(matrix.matrix[i]['C']) + "\t" +
            		format.format(matrix.matrix[i]['G']) + "\t" +
            		format.format(matrix.matrix[i]['T']) + "\t" +
            		getConsensusLetter(matrix, i) +"\n");
        }
        out.append("XX\n");
        return out.toString();
    }

    /** returns a string showing a text representation of the motif */
    public static String printMatrixLetters(WeightMatrix matrix) {
        char[][] out = new char[4][matrix.length()];
        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < out[0].length; j++) {
                out[i][j] = ' ';
            }
        }
        Character letters[] = {'A','C','G','T'};
        WMLetterCmp cmp = new WMLetterCmp(matrix);
        double minval = matrix.setLogOdds() ? 0 : .25;
        for (int i = 0; i < matrix.length(); i++) {
            cmp.setIndex(i);
            Arrays.sort(letters,cmp);
            for (int j = 0; j < 4; j++) {
                if (matrix.matrix[i][letters[j]] > minval) {
                    out[j][i] = letters[j];
                } else {
                    break;
                }
            }
        }
        return new String(out[0]) + "\n" +
            new String(out[1]) + "\n" +
            new String(out[2]) + "\n" +
            new String(out[3]);
    }
    /** returns a string that gives the maxScore <br>
     * i.e. the first line of printMatrixLetters()*/
    public static String getMaxLetters(WeightMatrix matrix) {
        char[][] out = new char[4][matrix.length()];
        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < out[0].length; j++) {
                out[i][j] = ' ';
            }
        }
        Character letters[] = {'A','C','G','T'};
        WMLetterCmp cmp = new WMLetterCmp(matrix);
        for (int i = 0; i < matrix.length(); i++) {
        	double minval = matrix.setLogOdds() ? 0 : .25;
            cmp.setIndex(i);
            Arrays.sort(letters,cmp);
            for (int j = 0; j < 4; j++) {
                if (matrix.matrix[i][letters[j]] > minval) {
                    out[j][i] = letters[j];
                } else {
                    break;
                }
            }
        }
        return new String(out[0]);
    }
    
    
    public static List<String> getConsensusKmerList(WeightMatrix matrix, int Kmin, int Kmax ){
    	List<String> ret = new ArrayList<String>();
    	String consensus = WeightMatrix.getConsensus(matrix);
    	// Now trim the edges 
    	// Trim the front chars first
    	while(consensus.startsWith("N")){
    		consensus = consensus.substring(1);
    	}
    	// Now trim the ends
    	while(consensus.endsWith("N")){
    		consensus = consensus.substring(0, consensus.length()-1);
    	}
    	if(consensus.length() < Kmin){ // Return an empty set if Kmin is gt than motif length
    		return ret;
    	}else{
    		int KCap = Math.min(Kmax,consensus.length());
    		for(int k=Kmin; k<=KCap; k++){
    			for (int i = 0; i < (consensus.length() - k + 1); i++) {
    				String sub = consensus.substring(i, i+k);
    				if(sub.contains("N"))
    					continue;
    				if(sub.contains("R") || sub.contains("Y") || sub.contains("S") || sub.contains("W") || sub.contains("K") || sub.contains("M")){
    					List<StringBuilder> mers = new ArrayList<StringBuilder>();
    					StringBuilder sb = new StringBuilder();
    					mers.add(sb);
    					for(int s=0; s<sub.length(); s++){
    						List<StringBuilder> newMers = new ArrayList<StringBuilder>();
    						if(sub.charAt(s) == 'R'){
    							for(StringBuilder tb : mers){
    								newMers.add(new StringBuilder(tb.toString()+"G"));
    								tb.append('A');
    							}
    						}else if(sub.charAt(s) == 'Y'){
    							for(StringBuilder tb : mers){
    								newMers.add(new StringBuilder(tb.toString()+"T"));
    								tb.append('C');
    							}
    						}else if(sub.charAt(s) == 'S'){
    							for(StringBuilder tb : mers){
    								newMers.add(new StringBuilder(tb.toString()+"G"));
    								tb.append('C');
    							}
    						}else if(sub.charAt(s) == 'W'){
    							for(StringBuilder tb : mers){
    								newMers.add(new StringBuilder(tb.toString()+"T"));
    								tb.append('A');
    							}
    						}else if(sub.charAt(s) == 'K'){
    							for(StringBuilder tb : mers){
    								newMers.add(new StringBuilder(tb.toString()+"G"));
    								tb.append('T');
    							}
    						}else if(sub.charAt(s) == 'M'){
    							for(StringBuilder tb : mers){
    								newMers.add(new StringBuilder(tb.toString()+"C"));
    								tb.append('A');
    							}
    						}else{
    							for(StringBuilder tb : mers){
    								tb.append(sub.charAt(s));
    							}
    						}
    						mers.addAll(newMers);
    					}
    					for(StringBuilder tb : mers){
    						ret.add(tb.toString());
    					}
    				}else{
    					ret.add(sub);
    				}
    			}
    		}
    		return ret;
    	}
    }
    
    
    /** returns a simple consensus string <br>
     * i.e. the first line of printMatrixLetters()*/
    public static String getConsensus(WeightMatrix matrix) {
    	char[] out = new char[matrix.length()];
        for (int i = 0; i < out.length; i++) {
            out[i]='N';
        }
        //Single letter consensus
        for (int i = 0; i < matrix.length(); i++) {
        	double maxval = matrix.setLogOdds() ? 0.5 : 0.6;
            for (int j = 0; j < 4; j++) {
                if (matrix.matrix[i][letters[j]] > maxval) {
                    out[i] = letters[j]; 
                    maxval = matrix.matrix[i][letters[j]];
                }
            }
        }
        //Degenerate consensus
        for (int i = 0; i < matrix.length(); i++) {
        	if(out[i]=='N'){
        		double maxval = matrix.setLogOdds() ? 1.2 : 0.8;
        		if(matrix.matrix[i]['A']+matrix.matrix[i]['G'] > maxval) {  out[i]='R';  maxval=matrix.matrix[i]['A']+matrix.matrix[i]['G']; }
        		if(matrix.matrix[i]['C']+matrix.matrix[i]['T'] > maxval) {  out[i]='Y';  maxval=matrix.matrix[i]['C']+matrix.matrix[i]['T']; }
        		if(matrix.matrix[i]['C']+matrix.matrix[i]['G'] > maxval) {  out[i]='S';  maxval=matrix.matrix[i]['C']+matrix.matrix[i]['G']; }
        		if(matrix.matrix[i]['A']+matrix.matrix[i]['T'] > maxval) {  out[i]='W';  maxval=matrix.matrix[i]['A']+matrix.matrix[i]['T']; }
        		if(matrix.matrix[i]['T']+matrix.matrix[i]['G'] > maxval) {  out[i]='K';  maxval=matrix.matrix[i]['T']+matrix.matrix[i]['G']; }
        		if(matrix.matrix[i]['A']+matrix.matrix[i]['C'] > maxval) {  out[i]='M';  maxval=matrix.matrix[i]['A']+matrix.matrix[i]['C']; }
        	}
        }
        
        return new String(out);
    }
    
    /** returns a simple consensus representation at a single position */
    public static char getConsensusLetter(WeightMatrix matrix, int pos) {
    	char out = 'N';

        //Single letter consensus
    	double maxval = matrix.setLogOdds() ? 0.5 : 0.6;
        for (int j = 0; j < 4; j++) {
            if (matrix.matrix[pos][letters[j]] > maxval) {
                out = letters[j]; 
                maxval = matrix.matrix[pos][letters[j]];
            }
        }
    
        //Degenerate consensus
    	if(out=='N'){
    		maxval = matrix.setLogOdds() ? 1.2 : 0.8;
    		if(matrix.matrix[pos]['A']+matrix.matrix[pos]['G'] > maxval) {  out='R';  maxval=matrix.matrix[pos]['A']+matrix.matrix[pos]['G']; }
    		if(matrix.matrix[pos]['C']+matrix.matrix[pos]['T'] > maxval) {  out='Y';  maxval=matrix.matrix[pos]['C']+matrix.matrix[pos]['T']; }
    		if(matrix.matrix[pos]['C']+matrix.matrix[pos]['G'] > maxval) {  out='S';  maxval=matrix.matrix[pos]['C']+matrix.matrix[pos]['G']; }
    		if(matrix.matrix[pos]['A']+matrix.matrix[pos]['T'] > maxval) {  out='W';  maxval=matrix.matrix[pos]['A']+matrix.matrix[pos]['T']; }
    		if(matrix.matrix[pos]['T']+matrix.matrix[pos]['G'] > maxval) {  out='K';  maxval=matrix.matrix[pos]['T']+matrix.matrix[pos]['G']; }
    		if(matrix.matrix[pos]['A']+matrix.matrix[pos]['C'] > maxval) {  out='M';  maxval=matrix.matrix[pos]['A']+matrix.matrix[pos]['C']; }
    	}
        return out;
    }
    /* returns the four letters ordered by their LL at the specified index */
    public static Character[] getLetterOrder(WeightMatrix wm, int index) {
        Character letters[] = {'A','C','G','T'};
        WMLetterCmp cmp = new WMLetterCmp(wm);
        cmp.setIndex(index);
        Arrays.sort(letters,cmp);
        return letters;
    }
    public static WeightMatrix reverseComplement(WeightMatrix input) {
        WeightMatrix output = new WeightMatrix(input.length());
        for (int i = 0; i < input.length(); i++) {
            output.matrix[input.length() - i - 1]['A'] = input.matrix[i]['T'];
            output.matrix[input.length() - i - 1]['C'] = input.matrix[i]['G'];
            output.matrix[input.length() - i - 1]['G'] = input.matrix[i]['C'];
            output.matrix[input.length() - i - 1]['T'] = input.matrix[i]['A'];
            output.matrix[input.length() - i - 1]['a'] = input.matrix[i]['t'];
            output.matrix[input.length() - i - 1]['c'] = input.matrix[i]['g'];
            output.matrix[input.length() - i - 1]['g'] = input.matrix[i]['c'];
            output.matrix[input.length() - i - 1]['t'] = input.matrix[i]['a'];

        }
        output.name = input.name;
        output.version = "revcomp " + input.version;
        output.species = input.species;
        output.type = input.type;
        return output;
    }
    public static WeightMatrix getLogOddsVersion(WeightMatrix input, Map<String, Double> back) {
        WeightMatrix output = new WeightMatrix(input.length());
        //clone
        for (int i = 0; i < input.length(); i++) {
            output.matrix[i]['A'] = input.matrix[i]['A'];
            output.matrix[i]['C'] = input.matrix[i]['C'];
            output.matrix[i]['G'] = input.matrix[i]['G'];
            output.matrix[i]['T'] = input.matrix[i]['T'];
            output.matrix[i]['a'] = input.matrix[i]['a'];
            output.matrix[i]['c'] = input.matrix[i]['c'];
            output.matrix[i]['g'] = input.matrix[i]['g'];
            output.matrix[i]['t'] = input.matrix[i]['t'];
        }
        if (!input.islogodds) { 
        	output.islogodds = true;
        	for (int i = 0; i < output.matrix.length; i++) {
        		for (int j = 0; j < allLetters.length; j++) {
        			output.matrix[i][allLetters[j]] = (float)(Math.log(Math.max(output.matrix[i][allLetters[j]], .000001) / back.get(("" + allLetters[j]).toUpperCase()))/LOG2);
        		}
        	}
        }
        output.name = input.name;
        output.version = "logOdds " + input.version;
        output.species = input.species;
        output.type = input.type;
        return output;
    }
    
    public void setZeroOffset(int z){zeroOffset=z;}
    public int getZeroOffset(){return zeroOffset;}
}
class WMLetterCmp implements Comparator<Character> {
    WeightMatrix matrix;
    int index;
    public WMLetterCmp(WeightMatrix m) {matrix = m;}
    public void setIndex(int i) {index = i;}
    public int compare(Character a, Character b) {
        return Double.compare(matrix.matrix[index][b],matrix.matrix[index][a]);
    }
}
