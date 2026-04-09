package org.egc.core.deepseq.experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cern.jet.random.Poisson;
import cern.jet.random.engine.DRand;
import org.egc.core.deepseq.ExtReadHit;
import org.egc.core.deepseq.HitPair;
import org.egc.core.deepseq.ReadHit;
import org.egc.core.deepseq.StrandedBaseCount;
import org.egc.core.deepseq.StrandedPair;
import org.egc.core.deepseq.hitloaders.HitLoader;
import org.egc.core.deepseq.stats.BackgroundCollection;
import org.egc.core.deepseq.stats.PoissonBackgroundModel;
import org.egc.core.genome.Genome;
import org.egc.core.genome.location.Region;
import org.egc.core.math.stats.StatUtil;


/**
 * HitCache stores all alignment hits for a Sample in memory.
 * 
 * All hits are loaded at initialization into primitive arrays for efficient access.
 * After potential binding regions are identified, call subsetArrays() to free memory
 * for reads outside the analysis regions.
 * 
 * Hit alignments are stored in two primitive type 3D arrays -- fivePrimePos and fivePrimeCounts.
 * In each array:
 * - first dimension: chromosome ID (via chrom2ID map)
 * - second dimension: strand (0 for Watson/'+', 1 for Crick/'-')
 * - third dimension: hit positions or counts
 * 
 * After initialization, data is read-only and all access is thread-safe
 * without synchronization.
 * 
 * @author mahony
 */
public class HitCache implements HitCacheInterface{

	private Collection<HitLoader> loaders; //Source of reads
	private Genome gen;
	private ExptConfig econfig;
	private int numChroms=0;
	protected double totalHits=0; //totalHits is the sum of alignment weights
	protected double totalHitsPos=0; //totalHitsPos is the sum of alignment weights on the plus strand
	protected double totalHitsNeg=0; //totalHitsNeg is the sum of alignment weights on the minus strand
	protected double uniqueHits=0; //count of unique mapped positions (just counts the number of bases with non-zero counts - does not treat non-uniquely mapped positions differently)
	protected double totalPairs=0; //count of the total number of paired hits
	protected double uniquePairs=0; //count of the total number of unique paired hits
	protected boolean loadPairs; //Load them if they exist
	protected boolean hasPairs; //Some pairs have been loaded
	protected boolean sortMid; //Sort pairs according to midpoint
	protected BackgroundCollection perBaseBack=new BackgroundCollection();
	protected float maxReadsPerBP=-1;
	
	/**
	 * Five prime ends of the read hits. <br>
	 * First dimension represents the corresponding chromosome ID. <br>
	 * Second dimension represents the strand. 0 for '+', 1 for '-' <br>
	 * Third dimension contains the coordinates of the hits
	 */
	private int[][][] fivePrimePos=null;
	/**
	 * Sum of read hit weights that corresponds to the 5' position
	 * First dimension represents the corresponding chromosome ID. <br>
	 * Second dimension represents the strand. 0 for '+', 1 for '-' <br>
	 * Third dimension contains the number of hits at corresponding start position 
	 * Third dimension index is that of the corresponding hit location in fivePrimePos
	 */
	private float[][][] fivePrimeCounts=null;
	/**
	 * Five prime ends of the R1 read hits in paired hits. <br>
	 * First dimension represents the R1 read chromosome ID. <br>
	 * Second dimension represents the R1 read strand. 0 for '+', 1 for '-' <br>
	 * Third dimension contains the coordinates of the R1 read hit <br>
	 */
	private int[][][] pairR1Pos=null;
	/**
	 * Five prime ends of the R2 read hits in paired hits. <br>
	 * First dimension represents the R1 read chromosome ID. <br>
	 * Second dimension represents the R1 read strand. 0 for '+', 1 for '-' <br>
	 * Third dimension contains the coordinates of the R2 read hit <br>
	 * Third dimension index is that of the corresponding R1 read in pairR1Pos
	 */
	private int[][][] pairR2Pos=null;
	/**
	 * Chromosome IDs of the R2 read hits in paired hits. <br>
	 * First dimension represents the R1 read chromosome ID. <br>
	 * Second dimension represents the R1 read strand. 0 for '+', 1 for '-' <br>
	 * Third dimension contains the coordinates of the R2 read hit <br>
	 * Third dimension index is that of the corresponding R1 read in pairR1Pos
	 */
	private int[][][] pairR2Chrom=null;
	/**
	 * Strands of the R2 read hits in paired hits. <br>
	 * First dimension represents the R1 read chromosome ID. <br>
	 * Second dimension represents the R1 read strand. 0 for '+', 1 for '-' <br>
	 * Third dimension contains the strands of the R2 read hit <br>
	 * Third dimension index is that of the corresponding R1 read in pairR1Pos
	 */
	private int[][][] pairR2Strand=null;
	/**
	 * Weights of the paired hits. <br>
	 * First dimension represents the R1 read chromosome ID. <br>
	 * Second dimension represents the R1 read strand. 0 for '+', 1 for '-' <br>
	 * Third dimension contains the weights <br>
	 * Third dimension index is that of the corresponding R1 read in pairR1Pos
	 */
	private float[][][] pairWeight=null;
	/**
	 * Midpoints of the paired hits. <br>
	 * First dimension represents the R1 read chromosome ID. <br>
	 * Second dimension represents the R1 read strand. 0 for '+', 1 for '-' <br>
	 * Third dimension contains the midpoints <br>
	 * Third dimension index is that of the corresponding R1 read in pairR1Pos
	 */
	private int[][][] pairMid=null;
	
	private HashMap<String, Integer> chrom2ID=new HashMap<String,Integer>();
	private HashMap<String, Integer> chrom2DBID=new HashMap<String,Integer>();
	private HashMap<Integer,String> id2Chrom=new HashMap<Integer,String>();
	private HashMap<Integer,Integer> id2DBID=new HashMap<Integer,Integer>();
	
	/**
	 * Constructor
	 * @param loadPairs : boolean flag to load the read pairs (if they exist)
	 * @param ec
	 * @param hloaders
	 * @param perBaseReadMax
	 */
	public HitCache(boolean loadPairs, ExptConfig ec, Collection<HitLoader> hloaders, float perBaseReadMax){
		econfig = ec;
		gen = econfig.getGenome();
		this.loaders = hloaders;
		maxReadsPerBP= perBaseReadMax;
		this.loadPairs = loadPairs;
		this.sortMid = ec.sortMid;
		initialize();
	}
	
	//Accessors
	public double getHitCount(){return(totalHits);}
	public double getHitCountPos(){return(totalHitsPos);}
	public double getHitCountNeg(){return(totalHitsNeg);}
	public double getHitPositionCount(){return(uniqueHits);}
	public double getPairCount(){return(totalPairs);}
	public double getUniquePairCount(){return(uniquePairs);}
	public Genome getGenome(){return gen;}
	public void setGenome(Genome g){gen=g;}

	
	/**
	 * Initialize needs to be called by the constructor.
	 * 
	 * If all hits are being cached, initialize sets up the primitive arrays. 
	 * Unfortunately, even if all hits are not being cached we still need to 
	 * set up the primitive arrays for now. This is so that the total and unique
	 * hit counts can be accurately calculated given the per base limits.   
	 *  
	 * @param loadType1 : boolean flag to load the left reads
	 * @param loadType2 : boolean flag to load the right reads (if they exist)
	 * @param loadPairs : boolean flag to load the read pairs (if they exist)
	 * @param cacheEverything : boolean flag to cache all hits (if false, local file caching is activated)
	 * @param initialCacheRegions : list of regions to cache first (can be null)
	 */
	private void initialize(){
		
		//These lists are temporary stores while collecting reads from all sources
		HashMap<String, ArrayList<Integer>[]> posList = new HashMap<String, ArrayList<Integer>[]>();
		HashMap<String, ArrayList<Float>[]> countsList = new HashMap<String, ArrayList<Float>[]>();
		HashMap<String, ArrayList<HitPair>[]> pairsList = new HashMap<String, ArrayList<HitPair>[]>();
		
		for(HitLoader currLoader : loaders){
			try{
				//Get all read hits (necessary here to correct per-base counts appropriately)
				currLoader.sourceAllHits();
			
				//Add the reads to the temporary stores
				for(String chr: currLoader.getFivePrimePositions().keySet()){
					if(!posList.containsKey(chr)){
						ArrayList<Integer>[] currIArrayList = new ArrayList[2];
						currIArrayList[0]=new ArrayList<Integer>();
						currIArrayList[1]=new ArrayList<Integer>();
						posList.put(chr, currIArrayList);
					}
					posList.get(chr)[0].addAll(currLoader.getFivePrimePositions().get(chr)[0]);
					posList.get(chr)[1].addAll(currLoader.getFivePrimePositions().get(chr)[1]);
				}
				for(String chr: currLoader.getFivePrimeCounts().keySet()){
					if(!countsList.containsKey(chr)){
						ArrayList<Float>[] currFArrayList = new ArrayList[2];
						currFArrayList[0]=new ArrayList<Float>();
						currFArrayList[1]=new ArrayList<Float>();
						countsList.put(chr, currFArrayList);
					}
					countsList.get(chr)[0].addAll(currLoader.getFivePrimeCounts().get(chr)[0]);
					countsList.get(chr)[1].addAll(currLoader.getFivePrimeCounts().get(chr)[1]);
				}
				
				//Add the pairs to the temporary stores (if requested & exist)
				//Also sort the pairs (required for telling uniques apart)
				if(loadPairs && currLoader.hasPairedReads()){
					hasPairs=true;
					for(String chr: currLoader.getPairs().keySet()){
						if(!pairsList.containsKey(chr)){
							ArrayList<HitPair>[] currLRPArrayList = new ArrayList[2];
							currLRPArrayList[0]=new ArrayList<HitPair>();
							currLRPArrayList[1]=new ArrayList<HitPair>();
							pairsList.put(chr, currLRPArrayList);
						}
						pairsList.get(chr)[0].addAll(currLoader.getPairs().get(chr)[0]);
						pairsList.get(chr)[1].addAll(currLoader.getPairs().get(chr)[1]);
						Collections.sort(pairsList.get(chr)[0]);
						Collections.sort(pairsList.get(chr)[1]);
					}
				}
				
				//Reset loader to free memory
				currLoader.resetLoader();
			}catch(OutOfMemoryError e){
				e.printStackTrace();
				throw new RuntimeException("Ran out of memory during hit loading; try re-running with increased -Xmx option.", e);
			}
		}
		
		//null genome is estimated here if necessary
		if(gen==null)
			gen = estimateGenome(posList);
		
		//Make the primitive arrays 
		populateArrays(posList, countsList, pairsList);
		updateTotalHits();
		
		//Initialize a per-base background model
		initializeBackground();
		
		//Enforce per-base read limits 
		//maxReadsPerBP = 0 : poisson/gauss
		//maxReadsPerBP = -1 : global poisson
		//maxReadsPerBP > 0 : fixed
		if(econfig.doPerBaseFiltering()){
			if(econfig.doPoissonGaussWinPerBaseFiltering() || maxReadsPerBP==0){ //global poisson/gauss model
				capPerBaseCountWithPoissonGaussianFilter(10e-3, 20);
			}else{
				if(maxReadsPerBP == -1)
					maxReadsPerBP = perBaseBack.getMaxThreshold('.');
				capPerBaseCount(maxReadsPerBP);
			}
			updateTotalHits();
			initializeBackground(); //Reinitialize given updated hit count (again - just the per-base background model)
		}
		
		//Free memory
		for(String chr: posList.keySet()){
			posList.get(chr)[0].clear();
			posList.get(chr)[1].clear();
			countsList.get(chr)[0].clear();
			countsList.get(chr)[1].clear();
			if(loadPairs && hasPairs && pairsList!=null){
				pairsList.get(chr)[0].clear();
				pairsList.get(chr)[1].clear();
			}
		}
		posList.clear();
		countsList.clear();
		if(loadPairs && hasPairs && pairsList!=null)
			pairsList.clear();
		System.gc();
	}
	
	/**
	 * Generate a hashmap containing the frequencies of each fragment size,
	 * @return
	 * @author Jianyu Yang
	 */
	public HashMap<Integer, Integer> getFragSizeFrequency(){
		HashMap<Integer, Integer> frequency = new HashMap<Integer, Integer>();
		for(int chrID=0; chrID<pairR1Pos.length; chrID++) if(pairR1Pos[chrID]!=null) {
			for(int strand=0; strand<pairR1Pos[chrID].length; strand++) {
				if(pairR1Pos[chrID][strand]!=null) {
					for(int index=0; index<pairR1Pos[chrID][strand].length; index++)
						if(chrID==pairR2Chrom[chrID][strand][index]) { 
							int size = Math.abs(pairR1Pos[chrID][strand][index]-pairR2Pos[chrID][strand][index])+1;
							if(frequency.containsKey(size)) {
								frequency.put(size, frequency.get(size)+(int)pairWeight[chrID][strand][index]);
							} else {
								frequency.put(size, (int)pairWeight[chrID][strand][index]);
							}
						} 
				}
			}
		}
		return frequency;
	}
	
	
	/**
	 * Load all base counts in a region, regardless of strand.
	 * If file caching is being used, it's more efficient to group calls to this method by chromosome.  
	 * @param r Region
	 * @return List of StrandedBaseCounts
	 */
	public List<StrandedBaseCount> getBases(Region r) {
		List<StrandedBaseCount> bases = new ArrayList<StrandedBaseCount>();
		bases.addAll(getStrandedBases(r,'+'));
		bases.addAll(getStrandedBases(r,'-'));
		return bases;
	}
	/**
	 * Loads hits in the region.
	 * If file caching is being used, it's more efficient to group calls to this method by chromosome.
	 * Unfortunately, I think the easiest way to ensure thread-safety is to allow only one thread to call this at a time. 
	 * @param r Region
	 * @return List of StrandedBaseCounts
	 */
	public List<StrandedBaseCount> getStrandedBases(Region r, char strand) {
		List<StrandedBaseCount> bases = new ArrayList<StrandedBaseCount>();

		
		String chr = r.getChrom();
		if(chrom2ID.containsKey(chr)){
			int chrID = chrom2ID.get(chr);
			int j = (strand=='+') ? 0 : 1;
			if(fivePrimePos[chrID][j] != null){
				int[] tempStarts = fivePrimePos[chrID][j];		
				if(tempStarts.length != 0) {
					int start_ind = Arrays.binarySearch(tempStarts, r.getStart());
					int end_ind   = Arrays.binarySearch(tempStarts, r.getEnd());
					
					if( start_ind < 0 ) { start_ind = -start_ind - 1; }
					if( end_ind < 0 )   { end_ind   = -end_ind - 1; }
					
		            while (start_ind > 0 && tempStarts[start_ind - 1] >= r.getStart() ) {
		                start_ind--;
		            }
		            while (end_ind < tempStarts.length && tempStarts[end_ind] <= r.getEnd()) {
		                end_ind++;
		            }
					for(int k = start_ind; k < end_ind; k++) {
						bases.add(new StrandedBaseCount(strand, tempStarts[k], fivePrimeCounts[chrID][j][k]));
					}	
				}
			}
		}
		return bases;
	}//end of getStrandedBases method
	
	/**
	 * Load all paired hits that have an R1 read in a region.
	 * If file caching is being used, it's more efficient to group calls to this method by chromosome.  
	 * @param r Region
	 * @return List of StrandedPair
	 */
	public List<StrandedPair> getPairs(Region r) {
		List<StrandedPair> pairs = new ArrayList<StrandedPair>();
		pairs.addAll(getPairsOnStrand(r,'+'));
		pairs.addAll(getPairsOnStrand(r,'-'));
		return pairs;
	}
	/**
	 * Loads paired hits that have an R1 read in the region and on the requested strand.
	 * If file caching is being used, it's more efficient to group calls to this method by chromosome.
	 * Unfortunately, I think the easiest way to ensure thread-safety is to allow only one thread to call this at a time. 
	 * @param r Region
	 * @return List of StrandedPair
	 */
	public List<StrandedPair> getPairsOnStrand(Region r, char strand) {
		List<StrandedPair> pairs = new ArrayList<StrandedPair>();

		if(loadPairs && hasPairs && pairR1Pos!=null){
			String chr = r.getChrom();
			if(chrom2ID.containsKey(chr)){
				int chrID = chrom2ID.get(chr);
				int chrDBID = chrom2DBID.get(chr);
				int j = (strand=='+') ? 0 : 1;
				if(pairR1Pos[chrID][j] != null){
					int[] tempStarts = pairR1Pos[chrID][j];		
					if(tempStarts.length != 0) {
						int start_ind = Arrays.binarySearch(tempStarts, r.getStart());
						int end_ind   = Arrays.binarySearch(tempStarts, r.getEnd());
						
						if( start_ind < 0 ) { start_ind = -start_ind - 1; }
						if( end_ind < 0 )   { end_ind   = -end_ind - 1; }
						
			            while (start_ind > 0 && tempStarts[start_ind - 1] >= r.getStart() ) {
			                start_ind--;
			            }
			            while (end_ind < tempStarts.length && tempStarts[end_ind] <= r.getEnd()) {
			                end_ind++;
			            }
						for(int k = start_ind; k < end_ind; k++) {
							pairs.add(new StrandedPair(gen, chrDBID, tempStarts[k], strand, id2DBID.get(pairR2Chrom[chrID][j][k]), pairR2Pos[chrID][j][k], pairR2Strand[chrID][j][k]==0?'+':'-', pairWeight[chrID][j][k] ));
						}	
					}
				}
			}
		
		}
		return pairs;
	}
	
	public List<StrandedPair> getPairsByMid(Region r) {
		List<StrandedPair> pairs = new ArrayList<StrandedPair>();
		
		if(loadPairs && hasPairs && pairR1Pos!=null) {
			String chr = r.getChrom();
			if(chrom2ID.containsKey(chr)) {
				int chrID = chrom2ID.get(chr);
				int chrDBID = chrom2DBID.get(chr);
				for(int j=0;j<2;j++) {
					if(pairMid[chrID][j]!=null) {
						int[] tempMids = pairMid[chrID][j];
						if(tempMids.length !=0 ) {
							int start_ind = Arrays.binarySearch(tempMids, r.getStart());
							int end_ind = Arrays.binarySearch(tempMids, r.getEnd());
							
							if( start_ind < 0 ) { start_ind = -start_ind - 1; }
							if( end_ind < 0 )   { end_ind = -end_ind - 1; }
							
							while (start_ind > 0 && tempMids[start_ind - 1] >= r.getStart() ) {
								start_ind--;
							}
							while (end_ind < tempMids.length && tempMids[end_ind] <= r.getEnd()) {
								end_ind++;
							}
							for(int k = start_ind; k < end_ind; k++) {
								pairs.add(new StrandedPair(gen, chrDBID, pairR1Pos[chrID][j][k], j==0?'+':'-', id2DBID.get(pairR2Chrom[chrID][j][k]), pairR2Pos[chrID][j][k], pairR2Strand[chrID][j][k]==0?'+':'-', pairWeight[chrID][j][k]));							}
						}
					}
				}
			}
		}
		return pairs;
	}
	
	
	/**
	 * Sum of all hit weights in a region
	 * If file caching is being used, it's more efficient to group calls to this method by chromosome.
	 * @param r Region
	 * @return float 
	 */
	public float countHits(Region r) {
		float count = 0;
		count += countStrandedBases(r, '+');
		count += countStrandedBases(r, '-');
		return count;
	}
	/**
	 * Sum of hit weights in one strand of a region.
	 * If file caching is being used, it's more efficient to group calls to this method by chromosome.
	 * Unfortunately, I think the easiest way to ensure thread-safety is to allow only one thread to call this at a time.
	 * @param r Region
	 * @return float 
	 */
    public float countStrandedBases(Region r, char strand) {
    	float count = 0;
		
    	String chr = r.getChrom();
		if(chrom2ID.containsKey(chr)){
			int chrID = chrom2ID.get(chr);
			int j = (strand=='+') ? 0 : 1;
			if(fivePrimePos[chrID][j] != null){
				int[] tempStarts = fivePrimePos[chrID][j];		
		        if(tempStarts.length != 0) {
					int start_ind = Arrays.binarySearch(tempStarts, r.getStart());
					int end_ind   = Arrays.binarySearch(tempStarts, r.getEnd());
					if( start_ind < 0 ) { start_ind = -start_ind - 1; }
					if( end_ind < 0 )   { end_ind   = -end_ind - 1; }
					
		            while (start_ind > 0 && tempStarts[start_ind - 1] >= r.getStart() ) {
		                start_ind--;
		            }
		            while (end_ind < tempStarts.length && tempStarts[end_ind] <= r.getEnd()) {
		                end_ind++;
		            }
					for(int k = start_ind; k < end_ind; k++) {
		                count += fivePrimeCounts[chrID][j][k];
		            }
		        }
			}
		}
	    return count;
    }
    
    
    
    public List<ExtReadHit> exportExtReadHits(Region r, int readLen, int startShift, int fivePrimeExt, int threePrimeExt){
    	List<ReadHit> readHits = exportReadHits(r,readLen);
    	List<ExtReadHit> extReadHits = new ArrayList<ExtReadHit>();
    	for(ReadHit rh : readHits){
    		extReadHits.add(new ExtReadHit(gen, rh, startShift, fivePrimeExt, threePrimeExt));
    	}
    	return extReadHits;
    }
    
    
    /**
     * Exports readhits in a given region
     * If file caching is being used, it's more efficient to group calls to this method by chromosome.
     */
    public List<ReadHit> exportReadHits(Region r, int readLen){
    	List<ReadHit> reghits = new ArrayList<ReadHit>();
    	for(StrandedBaseCount sbc : getBases(r)){
    		int start = sbc.getStrand() == '+' ? sbc.getCoordinate() : sbc.getCoordinate()-readLen+1;
    		int end = sbc.getStrand() == '+' ? sbc.getCoordinate()+readLen-1 : sbc.getCoordinate();
    		reghits.add(new ReadHit(r.getChrom(),start,end,sbc.getStrand(),sbc.getCount()));
    	}
    	return(reghits);
    	
    }
    
    /**
     * Export all single-end hits to ReadHit objects
     * @param readLen
     * @return
     */
    public List<ReadHit> exportReadHits(int readLen){
		List<ReadHit> allhits = new ArrayList<ReadHit>();
		
		for(String chr : chrom2ID.keySet()){
			Region chrom = new Region(gen, chr, 1, gen.getChromLength(chr));
			for(StrandedBaseCount sbc : getBases(chrom)){
				int start = sbc.getStrand()=='+' ? sbc.getCoordinate() : sbc.getCoordinate()-readLen+1;
				int end = sbc.getStrand()=='+' ? sbc.getCoordinate()+readLen-1 : sbc.getCoordinate();
				allhits.add(new ReadHit(chr, start, end, sbc.getStrand(), sbc.getCount()));
			}
		}
		return(allhits);
	}
    /**
	 * Check that a given region is in the cache. 
	 * Should be called by all methods that query hits in this class 
	 * @param r
	 * @return
	 */
	private void populateArrays(HashMap<String, ArrayList<Integer>[]> posList, HashMap<String, ArrayList<Float>[]> countsList, HashMap<String, ArrayList<HitPair>[]> pairsList) {
		//Initialize chromosome name to id maps
		numChroms=0;
		for(String chr : gen.getChromList()){
			chrom2ID.put(chr, numChroms);
			chrom2DBID.put(chr, gen.getChromID(chr));
			id2DBID.put(numChroms, gen.getChromID(chr));
			id2Chrom.put(numChroms, chr);
			numChroms++;
		}
		
		//Initialize the data structures
		fivePrimePos  = new int[numChroms][2][];
		fivePrimeCounts = new float[numChroms][2][];
		if(hasPairs){
			pairR1Pos = new int[numChroms][2][];
			pairR2Pos = new int[numChroms][2][];
			pairR2Chrom = new int[numChroms][2][];
			pairR2Strand = new int[numChroms][2][];
			pairWeight = new float[numChroms][2][];
			pairMid = new int[numChroms][2][];
		}
		
		//Copy over the 5' position data
		for(String chr : gen.getChromList()){
			if(posList.containsKey(chr)){
				for(int j = 0; j < posList.get(chr).length; j++)
					fivePrimePos[chrom2ID.get(chr)][j] = list2int(posList.get(chr)[j]);
			}else{
				fivePrimePos[chrom2ID.get(chr)][0]=null;
				fivePrimePos[chrom2ID.get(chr)][1]=null;
			}
		}//Copy over the count data
		for(String chr : gen.getChromList()){
			if(countsList.containsKey(chr)){
				for(int j = 0; j < countsList.get(chr).length; j++)
					fivePrimeCounts[chrom2ID.get(chr)][j] = list2float(countsList.get(chr)[j]);
			}else{
				fivePrimeCounts[chrom2ID.get(chr)][0]=null;
				fivePrimeCounts[chrom2ID.get(chr)][1]=null;
			}
		}
		if(loadPairs && hasPairs){ //Copy over the paired data
			for(String chr : gen.getChromList()){
				int c = chrom2ID.get(chr);
				if(pairsList.containsKey(chr)){
					for(int j = 0; j < pairsList.get(chr).length; j++){
						pairR1Pos[c][j] = new int[pairsList.get(chr)[j].size()];
						pairR2Pos[c][j] = new int[pairsList.get(chr)[j].size()];
						pairR2Chrom[c][j] = new int[pairsList.get(chr)[j].size()];
						pairR2Strand[c][j] = new int[pairsList.get(chr)[j].size()];
						pairWeight[c][j] = new float[pairsList.get(chr)[j].size()];
						pairMid[c][j] = new int[pairsList.get(chr)[j].size()];
						int p=0;
						for(HitPair lrp : pairsList.get(chr)[j]){
							pairR1Pos[c][j][p] = lrp.r1Pos;
							pairR2Pos[c][j][p] = lrp.r2Pos;
							pairR2Chrom[c][j][p] = chrom2ID.get(lrp.r2Chr);
							pairR2Strand[c][j][p] = lrp.r2Strand;
							pairWeight[c][j][p] = lrp.pairWeight;
							pairMid[c][j][p] = (lrp.r1Pos+lrp.r2Pos)/2;
							p++;
						}
					}
				}else{
					pairR1Pos[chrom2ID.get(chr)][0]=null;
					pairR1Pos[chrom2ID.get(chr)][1]=null;
					pairR2Pos[chrom2ID.get(chr)][0]=null;
					pairR2Pos[chrom2ID.get(chr)][1]=null;
					pairR2Chrom[chrom2ID.get(chr)][0]=null;
					pairR2Chrom[chrom2ID.get(chr)][1]=null;
					pairR2Strand[chrom2ID.get(chr)][0]=null;
					pairR2Strand[chrom2ID.get(chr)][1]=null;
					pairWeight[chrom2ID.get(chr)][0]=null;
					pairWeight[chrom2ID.get(chr)][1]=null;
					pairMid[chrom2ID.get(chr)][0]=null;
					pairMid[chrom2ID.get(chr)][1]=null;
				}
			}
		}
		
		//Sort the single-end arrays 
		for(int i = 0; i < fivePrimePos.length; i++) {  // chr
			for(int j = 0; j < fivePrimePos[i].length; j++) { // strand
				if(fivePrimePos[i][j]!=null && fivePrimeCounts[i][j]!=null){
					int[] inds = StatUtil.findSort(fivePrimePos[i][j]);
					fivePrimeCounts[i][j] = StatUtil.permute(fivePrimeCounts[i][j], inds);
				}
			}
		}
		//Sort the paired-end arrays
		if(loadPairs && hasPairs){ 
			for(int i = 0; i < pairR1Pos.length; i++) {  // chr
				for(int j = 0; j < pairR1Pos[i].length; j++) { // strand
					if(pairR1Pos[i][j]!=null && pairR2Pos[i][j]!=null && pairR2Chrom[i][j]!=null && pairR2Strand[i][j]!=null){
						int[] inds;
						if (sortMid)
							inds = StatUtil.findSort(pairMid[i][j]);
						else
							inds = StatUtil.findSort(pairR1Pos[i][j]);
						pairR1Pos[i][j] = StatUtil.permute(pairR1Pos[i][j], inds);
						pairR2Pos[i][j] = StatUtil.permute(pairR2Pos[i][j], inds);
						pairR2Chrom[i][j] = StatUtil.permute(pairR2Chrom[i][j], inds);
						pairR2Strand[i][j] = StatUtil.permute(pairR2Strand[i][j], inds);
						pairWeight[i][j] = StatUtil.permute(pairWeight[i][j], inds);
					}
				}
			}
		}
		
		//Collapse duplicate positions (single-end arrays)
		for(int i = 0; i < fivePrimePos.length; i++){
			for(int j = 0; j < fivePrimePos[i].length; j++){
				if(fivePrimePos[i][j]!=null && fivePrimePos[i][j].length>0){
					int uniquePos=1;
					for(int k = 0; k < fivePrimePos[i][j].length-1; k++)
						if(fivePrimePos[i][j][k+1]!=fivePrimePos[i][j][k]){uniquePos++;}
							
					int[] tmpPos = new int[uniquePos];
					float[] tmpCnt = new float[uniquePos];
					for(int x=0; x<uniquePos; x++){tmpCnt[x]=0;}
					int x=0;
					tmpPos[x] = fivePrimePos[i][j][0];
					tmpCnt[x] += fivePrimeCounts[i][j][0];
					for(int k = 1; k < fivePrimePos[i][j].length; k++){
						if(fivePrimePos[i][j][k]!=fivePrimePos[i][j][k-1]){x++;}
						tmpPos[x] = fivePrimePos[i][j][k];
						tmpCnt[x] += fivePrimeCounts[i][j][k];
					}
					fivePrimeCounts[i][j] = tmpCnt;
					fivePrimePos[i][j] = tmpPos;
				}
			}
		}
		
		//Collapse duplicate positions (paired-end arrays)
		if(loadPairs && hasPairs){
			for(int i = 0; i < pairR1Pos.length; i++){
				for(int j = 0; j < pairR1Pos[i].length; j++){
					if(pairR1Pos[i][j]!=null && pairR1Pos[i][j].length>0){
						int uniquePos=1;
						for(int k = 0; k < pairR1Pos[i][j].length-1; k++)
							if(pairR1Pos[i][j][k+1]!=pairR1Pos[i][j][k] || 
									pairR2Pos[i][j][k+1]!=pairR2Pos[i][j][k] || 
									pairR2Chrom[i][j][k+1]!=pairR2Chrom[i][j][k] || 
									pairR2Strand[i][j][k+1]!=pairR2Strand[i][j][k]){
								uniquePos++;
							}
								
						int[] tmpR1Pos = new int[uniquePos];
						int[] tmpR2Pos = new int[uniquePos];
						int[] tmpR2Chrom = new int[uniquePos];
						int[] tmpR2Str = new int[uniquePos];
						float[] tmpW = new float[uniquePos];
						int[] tmpMid = new int[uniquePos];
						for(int x=0; x<uniquePos; x++){tmpW[x]=0;}
						int x=0;
						tmpR1Pos[x] = pairR1Pos[i][j][0];
						tmpR2Pos[x] = pairR2Pos[i][j][0];
						tmpR2Chrom[x] = pairR2Chrom[i][j][0];
						tmpR2Str[x] = pairR2Strand[i][j][0];
						tmpW[x] += pairWeight[i][j][0];
						tmpMid[x] = pairMid[i][j][0];
						for(int k = 1; k < pairR1Pos[i][j].length; k++){
							if(pairR1Pos[i][j][k-1]!=pairR1Pos[i][j][k] || 
									pairR2Pos[i][j][k-1]!=pairR2Pos[i][j][k] || 
									pairR2Chrom[i][j][k-1]!=pairR2Chrom[i][j][k] || 
									pairR2Strand[i][j][k-1]!=pairR2Strand[i][j][k]){
								x++;
							}
							tmpR1Pos[x] = pairR1Pos[i][j][k];
							tmpR2Pos[x] = pairR2Pos[i][j][k];
							tmpR2Chrom[x] = pairR2Chrom[i][j][k];
							tmpR2Str[x] = pairR2Strand[i][j][k];
							tmpW[x] += pairWeight[i][j][k];
							tmpMid[x] = pairMid[i][j][k];
						}
						pairR1Pos[i][j] = tmpR1Pos;
						pairR2Pos[i][j] = tmpR2Pos;
						pairR2Chrom[i][j] = tmpR2Chrom;
						pairR2Strand[i][j] = tmpR2Str;
						pairWeight[i][j] = tmpW;
						pairMid[i][j] = tmpMid;
					}
				}
			}
		}
	}//end of populateArrays method
		
	/**
	 * Extract hits overlapping a subset of regions and redefine the cache using just these hits.
	 * Call after initialization and scaling to free memory for reads outside the analysis regions.
	 * @param regs
	 */
	public void subsetArrays(List<Region> regs){
		//merge overlapping regions so that hits aren't loaded twice
		List<Region> mregs = Region.mergeRegions(regs);
		//TODO: There's probably a more efficient way to do the below without needing to use the HashMaps 
		//Extract the relevant hits
		HashMap<String, ArrayList<Integer>[]> posList = new HashMap<String, ArrayList<Integer>[]>();
		HashMap<String, ArrayList<Float>[]> countsList = new HashMap<String, ArrayList<Float>[]>();
		HashMap<String, ArrayList<HitPair>[]> pairsList = new HashMap<String, ArrayList<HitPair>[]>();
		for(Region r : mregs){
			if(!posList.containsKey(r.getChrom())){
				ArrayList<Integer>[] currIArrayList = new ArrayList[2];
				currIArrayList[0]=new ArrayList<Integer>();
				currIArrayList[1]=new ArrayList<Integer>();
				posList.put(r.getChrom(), currIArrayList);
				ArrayList<Float>[] currFArrayList = new ArrayList[2];
				currFArrayList[0]=new ArrayList<Float>();
				currFArrayList[1]=new ArrayList<Float>();
				countsList.put(r.getChrom(), currFArrayList);
			}
			ArrayList<Integer>[] chrIArrayList = posList.get(r.getChrom());
			ArrayList<Float>[] chrFArrayList = countsList.get(r.getChrom());
			for(StrandedBaseCount sbc : getStrandedBases(r, '+')){
				chrIArrayList[0].add(sbc.getCoordinate());
				chrFArrayList[0].add(sbc.getCount());
			}
			for(StrandedBaseCount sbc : getStrandedBases(r, '-')){
				chrIArrayList[1].add(sbc.getCoordinate());
				chrFArrayList[1].add(sbc.getCount());
			}
			
			//Add pairs in if they exist
			if(loadPairs && hasPairs){
				if(!pairsList.containsKey(r.getChrom())){
					ArrayList<HitPair>[] currLRPArrayList = new ArrayList[2];
					currLRPArrayList[0]=new ArrayList<HitPair>();
					currLRPArrayList[1]=new ArrayList<HitPair>();
					pairsList.put(r.getChrom(), currLRPArrayList);
				}
				ArrayList<HitPair>[] currLRPArrayList = pairsList.get(r.getChrom());
				for(StrandedPair sp : getPairsOnStrand(r, '+')){
					currLRPArrayList[0].add(new HitPair(sp.getR1Coordinate(), sp.getR2Chrom(), sp.getR2Coordinate(), sp.getR2Strand()=='+'?0:1, (float)1.0));
				}
				for(StrandedPair sp : getPairsOnStrand(r, '-')){
					currLRPArrayList[1].add(new HitPair(sp.getR1Coordinate(), sp.getR2Chrom(), sp.getR2Coordinate(), sp.getR2Strand()=='+'?0:1, (float)1.0));
				}
			}
		}
		//Repopulate cache
		populateArrays(posList, countsList, pairsList);
		//Free memory
		for(String chr: posList.keySet()){
			posList.get(chr)[0].clear();
			posList.get(chr)[1].clear();
			countsList.get(chr)[0].clear();
			countsList.get(chr)[1].clear();
			if(loadPairs && hasPairs){
				pairsList.get(chr)[0].clear();
				pairsList.get(chr)[1].clear();
			}
		}
		posList.clear();
		countsList.clear();
		if(loadPairs && hasPairs)
			pairsList.clear();
		System.gc();
	}
	
	/**
	 * Save the contents of the hit arrays to local binary files
	 */
	private int[] list2int(List<Integer> list) {
		int[] out = new int[list.size()];
		for(int i = 0; i < out.length; i++)
			out[i] = list.get(i);
		return out;
	}
	/**
	 * Simple convertor
	 * @param list
	 * @return
	 */
	private float[] list2float(List<Float> list) {
		float[] out = new float[list.size()];
		for(int i = 0; i < out.length; i++)
			out[i] = list.get(i);
		return out;
	}
	
	/**
	 * Enforces a per-base weight threshold
	 * @param maxReadperBP float threshold
	 */
	private void capPerBaseCount(float maxReadperBP){
		for(int i = 0; i < fivePrimeCounts.length; i++)
			for(int j = 0; j < fivePrimeCounts[i].length; j++)
				if(fivePrimeCounts[i][j]!=null)
					for(int k = 0; k < fivePrimeCounts[i][j].length; k++)
						if (fivePrimeCounts[i][j][k] > maxReadperBP){
							//System.err.println("Capping "+fivePrimeCounts[i][j][k]+" to "+maxReadperBP);
							fivePrimeCounts[i][j][k] = maxReadperBP;
						}
	}
	
	/**
	 * Reset duplicate reads that pass Poisson threshold. 
	 * The Poisson lambda parameter is calculated by an Gaussian average
	 * that puts more weight for nearby bases (same chrom, same strand)
	 */
	private void capPerBaseCountWithPoissonGaussianFilter(double threshold, int width){
        double g[] = new double[width*4+1];
		double variance = (double)(width*width);
		double gaussCoeff = 1.0 / Math.sqrt(2.0 * Math.PI * variance);
		for (int i=0;i<g.length;i++)
			g[i]=gaussCoeff * Math.exp(-((double)i*(double)i) / (2.0 * variance));
		
		DRand re = new DRand();
		Poisson P = new Poisson(0, re);
			
		for(int i = 0; i < fivePrimeCounts.length; i++)
			for(int j = 0; j < fivePrimeCounts[i].length; j++){
				float counts[] = fivePrimeCounts[i][j];
				int pos[] = fivePrimePos[i][j]; 
				if(counts!=null){
					for(int k = 0; k < counts.length; k++){
						int posK = pos[k]; 
						double sum = 0;
						for (int x=1;x<=width*4;x++){		// at most extend out 250 idx
							if (k+x>=counts.length|| pos[k+x]-posK>width*4)
								break;
							sum += counts[k+x]*g[pos[k+x]-posK];
						}
						for (int x=1;x<=width*4;x++){		// at most extend out 250 idx
							if (k-x<0 || posK-pos[k-x]>width*4)
								break;
							sum += counts[k-x]*g[posK-pos[k-x]];
						}
						sum = sum/(1-g[0]);				// exclude this position for evaluation
						
						double countThres=0;
						P.setMean(sum);
						double pvalue=1;
						for(int b=1; pvalue>threshold; b++){
							pvalue=1-P.cdf(b);	//p-value as the tail of Poisson
							countThres=b;
						}
						if (counts[k] > Math.max(1,countThres))
							counts[k] = (float) Math.max(1,countThres);					
					}
				}
			}
	}

	/**
	 * Simple count correction with a scaling factor and a floor of one. 
	 * Beware: only works if all reads are loaded.
	 * @param perBaseScaling float threshold
	 */
	public void linearCountCorrection(float perBaseScaling){
		if(perBaseScaling<1)
			System.err.println("linearCountCorrection: perBaseScaling is less than 1 - makes no sense to scale");
		else{
			for(int i = 0; i < fivePrimeCounts.length; i++)
				for(int j = 0; j < fivePrimeCounts[i].length; j++)
					if(fivePrimeCounts[i][j]!=null)
						for(int k = 0; k < fivePrimeCounts[i][j].length; k++)
							if (fivePrimeCounts[i][j][k] > 0){
								fivePrimeCounts[i][j][k] = fivePrimeCounts[i][j][k]/perBaseScaling;
								if(fivePrimeCounts[i][j][k]<1)
									fivePrimeCounts[i][j][k]=1;
							}
		}
	}
	
	/**
	 * Recount hit weights
	 */
	private void updateTotalHits(){
		totalHits = 0.0;
		totalHitsPos=0.0;
		totalHitsNeg=0.0;
		uniqueHits = 0.0;
		for(int i = 0; i < fivePrimeCounts.length; i++)
			for(int j = 0; j < fivePrimeCounts[i].length; j++)
				if(fivePrimeCounts[i][j]!=null)
					for(int k = 0; k < fivePrimeCounts[i][j].length; k++){
						totalHits += fivePrimeCounts[i][j][k];
						if(j==0){totalHitsPos+= fivePrimeCounts[i][j][k];}
						else{totalHitsNeg+= fivePrimeCounts[i][j][k];}
						if(fivePrimeCounts[i][j][k]>0)
							uniqueHits++;
					}
		totalPairs =0;
		uniquePairs = 0;
		if(loadPairs && hasPairs){
			for(int i = 0; i < pairWeight.length; i++)
				for(int j = 0; j < pairWeight[i].length; j++)
					if(pairWeight[i][j]!=null)
						for(int k = 0; k < pairWeight[i][j].length; k++){
							totalPairs += pairWeight[i][j][k];
							if(pairWeight[i][j][k]>0)
								uniquePairs++;
						}
		}
	}
	
	/**
	 * Estimate a genome from the observed read positions that are collected into the list
	 * @param posList HashMap indexed by chr containing ArrayLists of hit positions
	 * @return Genome
	 */
	private Genome estimateGenome(HashMap<String, ArrayList<Integer>[]> posList){
		HashMap<String, Integer> chrLenMap = new HashMap<String, Integer>();
		for(String c : posList.keySet()){
			int max = 0;
			for(int j=0; j<posList.get(c).length; j++){
				for(Integer i : posList.get(c)[j]){
					if(i>max)
						max=i;
				}
			}
			chrLenMap.put(c, max);
		}
		Genome g =new Genome("Genome", chrLenMap);
		return(g);
	}
	/**
	 * Initialize the per-base background model
	 *  
	 */
	private void initializeBackground(){
		perBaseBack=new BackgroundCollection();
		if(econfig.getGenome()==null){
			throw new RuntimeException("Genome chromosome lengths not specified. Please define using --geninfo.");
		}
		perBaseBack.addBackgroundModel(
				new PoissonBackgroundModel(-1, econfig.getPerBaseLogConf(), getHitCount(), econfig.getGenome().getGenomeLength(), econfig.getMappableGenomeProp(), 1, '.', 1, true));
	}
	
	/**
	 * Tidy up the local read caches
	 */
	public void close(){
	}
}
