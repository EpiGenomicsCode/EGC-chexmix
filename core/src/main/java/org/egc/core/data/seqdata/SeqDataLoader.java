package org.egc.core.data.seqdata;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.egc.core.data.core.CellLine;
import org.egc.core.data.core.ExptCondition;
import org.egc.core.data.core.ExptTarget;
import org.egc.core.data.core.Lab;
import org.egc.core.data.core.MetadataLoader;
import org.egc.core.data.core.SeqDataUser;
import org.egc.core.genome.Genome;
import org.egc.core.genome.location.Region;
import org.egc.core.genome.location.StrandedRegion;
import org.egc.core.gseutils.NotFoundException;
import org.egc.core.gseutils.Pair;



/**
 * SeqDataLoader serves as a clearinghouse for query interactions with the seqdata database and
 * associated metadata from tables in the core database (via the MetadataLoader). 
 * 
 * Implements a simple access control by checking if the username is in the permissions field
 * in each SeqAlignment. Note that while this is access-control-lite for experiment metadata, 
 * access to the data stored in the underlying readdb entries has more robust access-control.
 * 
 * @author tdanford
 * @author mahony
 * 
 * Created as ChipSeqLoader on May 16, 2007
 */
public class SeqDataLoader implements org.egc.core.gseutils.Closeable {

	public static String role = "seqdata";


	public static void main(String[] args) throws Exception{
		try {
			SeqDataLoader loader = new SeqDataLoader();
			
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private String myusername = "";
	private SeqDataUser myUser=null;
    private MetadataLoader mloader=null;
    private boolean closed=false;
    
    /**
     * Accessor for user
     * @return
     */
    public SeqDataUser getMyUser(){return myUser;}    
    /**
     * Accessor for MetadataLoader
     * @return
     */
    public MetadataLoader getMetadataLoader(){return mloader;}
    
	//Constructors
    public SeqDataLoader() throws SQLException, IOException{this(true, true);}
	public SeqDataLoader(boolean openClient, boolean cacheAllMetadata) throws SQLException, IOException {
	}
    
	/**
	 * Load the genomes that a SeqExpt is aligned to
	 * @param expt
	 * @return
	 * @throws SQLException
	 */
	public Collection<Genome> loadExperimentGenomes(SeqExpt expt) throws SQLException {
        return null;    
	}

	/**
	 * Load all SeqExpts from the database. 
	 * @return
	 * @throws SQLException
	 */
	public Collection<SeqExpt> loadAllExperiments() throws SQLException {
        return null;    
	}

	/**
	 * Find a SeqExpt by name and replicate
	 * (returns null if not found)
	 * @param name
	 * @param rep
	 * @return
	 * @throws SQLException
	 */
	public SeqExpt findExperiment(String name, String rep) throws SQLException {
        return null;    
	}
	
	/**
	 * Load a SeqExpt by name and replicate
	 * (throws exception if not found)
	 * @param name
	 * @param rep
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public SeqExpt loadExperiment(String name, String rep) throws NotFoundException, SQLException {
        return null;    
	}

	/**
	 * Load a collection of SeqExpts by name
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public Collection<SeqExpt> loadExperiments(String name) throws SQLException {
        return null;    
	}
	
	/**
	 * Load a collection of SeqExpts by Lab
	 * @param lab
	 * @return
	 * @throws SQLException
	 */
	public Collection<SeqExpt> loadExperiments(Lab lab) throws SQLException {
        return null;    
	}
	
	/**
	 * Load a collection of SeqExpts by ExptCondition
	 * @param cond
	 * @return
	 * @throws SQLException
	 */
	public Collection<SeqExpt> loadExperiments(ExptCondition cond) throws SQLException {
        return null;    
	}
	
	/**
	 * Load a collection of SeqExpts by ExptTarget
	 * @param target
	 * @return
	 * @throws SQLException
	 */
	public Collection<SeqExpt> loadExperiments(ExptTarget target) throws SQLException {
        return null;    
	}

	/**
	 * Load a collection of SeqExpts by CellLine
	 * @param cell
	 * @return
	 * @throws SQLException
	 */
	public Collection<SeqExpt> loadExperiments(CellLine cell) throws SQLException {
        return null;    
	}

	/**
	 * Load a SeqExpt by ID
	 * @param dbid
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public SeqExpt loadExperiment(int dbid) throws NotFoundException, SQLException {
        return null;    
	}

	/**
	 * Load all SeqAlignments in the database
	 * @param g
	 * @return
	 * @throws SQLException
	 */
    public Collection<SeqAlignment> loadAllAlignments () throws SQLException {
        return null;    
    }
	/**
	 * Load all SeqAlignments for a given Genome
	 * @param g
	 * @return
	 * @throws SQLException
	 */
    public Collection<SeqAlignment> loadAllAlignments (Genome g) throws SQLException {
        return null;    
    }

    /**
     * Load all SeqAlignments for a given SeqExpt
     * @param expt
     * @return
     * @throws SQLException
     */
	public Collection<SeqAlignment> loadAlignmentsBySeqExpt(SeqExpt expt) throws SQLException {
        return null;    
	}
	
	/**
	 * Load SeqAlignment by SeqExpt, name, genome
	 * @param expt
	 * @param n
	 * @param g
	 * @return
	 * @throws SQLException
	 */
	public SeqAlignment loadAlignment(SeqExpt expt, String n, Genome g) throws SQLException {
        return null;    
	}
	
	/**
	 * Load SeqAlignment by ID
	 * @param dbid
	 * @return
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public SeqAlignment loadAlignment(int dbid) throws NotFoundException, SQLException {
        return null;    
	}

	/**
	 * Load a collection of SeqAlignments by SeqLocator
	 * @param locator
	 * @param genome
	 * @return
	 * @throws SQLException
	 * @throws NotFoundException
	 */
	public Collection<SeqAlignment> loadAlignments(SeqLocator locator, Genome genome) throws SQLException, NotFoundException {
		List<SeqAlignment> output = new ArrayList<SeqAlignment>();
        if(locator.getAlignName()!=null){ //Alignment name provided
			if (locator.getReplicates().size() == 0) { //No replicate names provided
	            for (SeqExpt expt : loadExperiments(locator.getExptName())) { 
	                SeqAlignment align = loadAlignment(expt, locator.getAlignName(), genome);
	                if (align != null) {
	                    output.add(align);
	                }
	            }
	        } else {
	            for (String rep : locator.getReplicates()) { //Replicate names provided
	                try {
	                    SeqExpt expt = loadExperiment(locator.getExptName(), rep);
	                    SeqAlignment align = loadAlignment(expt, locator.getAlignName(), genome);
	                    if (align != null) {
	                        output.add(align);
	                    }
	                }
	                catch (IllegalArgumentException e) {
	                    throw new NotFoundException("Couldn't find alignment for " + locator);
	                }
	            }
	        }
        }else{ //No alignment name provided
        	if (locator.getReplicates().size() == 0) { //No alignment or replicate names provided
	            for (SeqExpt expt : loadExperiments(locator.getExptName())) { 
	                Collection<SeqAlignment> aligns = loadAlignmentsBySeqExpt(expt);
	                for(SeqAlignment a : aligns){
	                	if (a.getGenome().equals(genome)) { 
            				output.add(a);
    						break;
    					}
	                }
	            }
	        } else {
	            for (String rep : locator.getReplicates()) { //Replicate names provided, but not alignment name
	                try {
	                    SeqExpt expt = loadExperiment(locator.getExptName(), rep);
	                    Collection<SeqAlignment> aligns = loadAlignmentsBySeqExpt(expt);
		                for(SeqAlignment a : aligns){
		                	if (a.getGenome().equals(genome)) { 
	            				output.add(a);
	    						break;
	    					}
		                }
	                }
	                catch (IllegalArgumentException e) {
	                    throw new NotFoundException("Couldn't find alignment for " + locator);
	                }
	            }
	        }
        }
        Collection<SeqAlignment> filtered = filterAlignmentsByPermission(output);
        if (filtered.size() == 0)
            throw new NotFoundException("Locators were " + toString() + " but didn't get any alignments that you have permission to see.");
        
		return filtered;
	}
     
	/**
	 * Load a collection of SeqAlignments by querying multiple metadata labels 
	 * @param name
	 * @param replicate
	 * @param align
	 * @param exptType
	 * @param lab
	 * @param condition
	 * @param target
	 * @param cells
	 * @param readType
	 * @param genome
	 * @return
	 * @throws SQLException
	 */
	public Collection<SeqAlignment> loadAlignments(String name, String replicate, String align,
                                                       Integer exptType, Integer lab, Integer condition,
                                                       Integer target, Integer cells, Integer readType,
                                                       Genome genome) throws SQLException {
        return null;    
    }
	
	/**
	 * Filter out alignments that the user shouldn't see
	 * @param aligns
	 * @return
	 */
	private Collection<SeqAlignment> filterAlignmentsByPermission(Collection<SeqAlignment> aligns){
		Collection<SeqAlignment> output = new ArrayList<SeqAlignment>();
		for(SeqAlignment a : aligns)
			if(a.getPermissions().contains("public") || a.getPermissions().contains(myusername))
				output.add(a);
		return output;
	}

	/**
	 * Read a set of alignment parameters
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static Map<String,String> readParameters(BufferedReader reader) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			int p = line.indexOf('=');
			String key = line.substring(0, p);
			String value = line.substring(p + 1);
			params.put(key, value);
		}
		reader.close();
        return params;
    }

	/**
	 * Add a set of alignment parameters to a SeqAlignment
	 * @param align
	 * @param paramsfile
	 * @throws SQLException
	 * @throws IOException
	 */
	public void addAlignmentParameters(SeqAlignment align, File paramsfile) throws SQLException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(paramsfile)));
		addAlignmentParameters(align, readParameters(reader));
	}


	public void addAlignmentParameters(SeqAlignment align, Map<String, ? extends Object> params) throws SQLException {
	}


	public Map<String, String> getAlignmentParameters(SeqAlignment align) throws SQLException {
		Map<String, String> output = new HashMap<String, String>();
		Connection cxn=null;
		Statement get = null;
        ResultSet rs = null;
        return null;    
	}


	/*
	 * SeqHit loading  
	 */
	

	/**
	 * Load a chromosome's worth of single hits 
	 * @param a
	 * @param chromid
	 * @return
	 * @throws IOException
	 */
	public List<SeqHit> loadByChrom(SeqAlignment a, int chromid, boolean loadR2) throws IOException {
        return null;    
	}
	
	/**
	 * Load single hits per region
	 * @param align
	 * @param r
	 * @return
	 * @throws IOException
	 */
	public List<SeqHit> loadByRegion(SeqAlignment align, Region r, boolean loadR2) throws IOException {
        return null;    
	}
	
	/**
	 * Load single hits by region
	 * @param alignments
	 * @param r
	 * @return
	 * @throws IOException
	 */
	public Collection<SeqHit> loadByRegion(List<SeqAlignment> alignments, Region r, boolean loadR2) throws IOException {
		if (alignments.size() < 1) {
			throw new IllegalArgumentException("Alignment List must not be empty.");
		}
        Collection<SeqHit> output = null;
        for (SeqAlignment a : alignments) {
            if (output == null) {
                output = loadByRegion(a,r, loadR2);
            } else {
                output.addAll(loadByRegion(a,r, loadR2));
            }
        }
		return output;
	}
	
	/**
	 * Load integer positions of single hits by region.
	 * If Region is a StrandedRegion, then the positions returned are only for that strand
	 * @param alignments
	 * @param r
	 * @return
	 * @throws IOException
	 * @throws ClientException
	 */
    public List<Integer> positionsByRegion(List<SeqAlignment> alignments, Region r, boolean loadR2) throws IOException {
        return null;    
    }
    /**
	 * Load integer positions of single hits by region.
	 * If Region is a StrandedRegion, then the positions returned are only for that strand
	 * @param alignment
	 * @param r
	 * @return
	 * @throws IOException
	 * @throws ClientException
	 */
    public List<Integer> positionsByRegion(SeqAlignment alignment, Region r, boolean loadR2) throws IOException {
        return null;    
    }

    /**
     * Count single hits in a region.
     * If Region is a StrandedRegion, then the counts returned are only for that strand
     * @param align
     * @param r
     * @return
     * @throws IOException
     */
	public int countByRegion(SeqAlignment align, Region r, boolean loadR2) throws IOException {
        return 0;    
    }
    

	/**
	 * Count single hits in a region
	 * @param alignments
	 * @param r
	 * @return
	 * @throws IOException
	 */
	public int countByRegion(List<SeqAlignment> alignments, Region r, boolean loadR2) throws IOException {
        return 0;    
	}

	/**
	 * Weigh single hits in a region.
	 * If Region is a StrandedRegion, then the counts returned are only for that strand
	 * @param alignments
	 * @param r
	 * @return
	 * @throws IOException
	 */
	public double weightByRegion(List<SeqAlignment> alignments, Region r, boolean loadR2) throws IOException {
        return 0;    
	}
	

	/**
	 * Count all single hits in the alignment
	 * @param align
	 * @return
	 * @throws SQLException
	 */
	public int countAllHits(SeqAlignment align, boolean loadR2) throws IOException {
        return 0;    
	}

	/**
	 * Weigh all single hits in the alignment
	 * @param align
	 * @return
	 * @throws IOException
	 */
	public double weighAllHits(SeqAlignment align, boolean loadR2) throws IOException {
        return 0;    
	}

	/** Generates a histogram of the total weight of reads mapped to each bin.
     * Output maps bin center to weight centered around that bin.  Each read
     * is summarized by its start position.
     */
    public Map<Integer,Float> histogramWeight(SeqAlignment align, char strand, Region r, int binWidth, boolean loadR2) throws IOException {
        return null;    
    }
    /** Generates a histogram of the total number of reads mapped to each bin.
     * Output maps bin center to weight centered around that bin.  Each read
     * is summarized by its start position.
     */
    public Map<Integer,Integer> histogramCount(SeqAlignment align, char strand, Region r, int binWidth, boolean loadR2) throws IOException {        
        return null;    
    }
    /**
     * Get the total # of hits and weight for an alignment but only include reads
     * on the specified strand.  
     */
    public Pair<Long,Double> getAlignmentStrandedCountWeight(SeqAlignment align, char strand, boolean loadR2) throws IOException {
        return null;    
    }
    
	public void close() {
	}
	public boolean isClosed() {
        return false;    
	}

}
