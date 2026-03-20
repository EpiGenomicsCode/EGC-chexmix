package org.egc.core.data.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;


/**
 * MetadataLoader is the interface for interacting with metadata entries in the core database.
 * 
 * Methods in this class make use of a simple caching approach. Use the "forceDatabaseRefresh" flag in each method to 
 * ignore the cache and to re-query the database entries (useful if you expect that the database may have been updated
 * between queries). 
 * 
 * @author mahony
 */

public class MetadataLoader{
    
    public static final String role = "core";

    private boolean allLabsLoaded=false, allCellsLoaded=false, allCondsLoaded=false, allTargetsLoaded=false, 
    		allExptTypesLoaded=false, allReadTypesLoaded=false, allAlignTypesLoaded=false, allSeqDataUsersLoaded=false;
    
    private Map<String,Lab> labNames;
    private Map<String,CellLine> cellNames;
    private Map<String,ExptCondition> condNames;
    private Map<String,ExptTarget> targetNames;
    private Map<String,ExptType> exptTypeNames;
    private Map<String,ReadType> readTypeNames;
    private Map<String,AlignType> alignTypeNames;
    private Map<String,SeqDataUser> seqDataUserNames;
	
    private Map<Integer,Lab> labIDs;
    private Map<Integer,CellLine> cellIDs;
    private Map<Integer,ExptCondition> condIDs;
    private Map<Integer,ExptTarget> targetIDs;
    private Map<Integer,ExptType> exptTypeIDs;
    private Map<Integer,ReadType> readTypeIDs;
    private Map<Integer,AlignType> alignTypeIDs;
    private Map<Integer,SeqDataUser> seqDataUserIDs;
    	
    public MetadataLoader() throws SQLException {this(false);}
    public MetadataLoader(boolean cacheAll) throws SQLException { 
        
        labNames = new HashMap<String,Lab>();
        labIDs = new HashMap<Integer,Lab>();
        cellNames = new HashMap<String,CellLine>();
        cellIDs = new HashMap<Integer,CellLine>();
        condNames = new HashMap<String,ExptCondition>();
        condIDs = new HashMap<Integer,ExptCondition>();
        targetNames = new HashMap<String,ExptTarget>();
        targetIDs = new HashMap<Integer,ExptTarget>();
        exptTypeNames = new HashMap<String,ExptType>();
        exptTypeIDs = new HashMap<Integer,ExptType>();
        readTypeNames = new HashMap<String,ReadType>();
        readTypeIDs = new HashMap<Integer,ReadType>();
        alignTypeNames = new HashMap<String,AlignType>();
        alignTypeIDs = new HashMap<Integer,AlignType>();
        seqDataUserNames = new HashMap<String,SeqDataUser>();
        seqDataUserIDs = new HashMap<Integer,SeqDataUser>();
     
        if(cacheAll)
        	cacheAllMetadata();
    }
	
    /**
     * Load all metadata tables into the cache Maps
     * (forces update of all tables if they already exist).
     * Performs all queries with one connection establishment for efficiency.
     */
    public void cacheAllMetadata() throws SQLException {
    }
    
    
    //////////////////
    // Lab stuff
    //////////////////
    
    /**
     * Load single Lab by name
     * 
     * @param name
     * @param insertIfNone : insert into the database if there is no such entry
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Lab loadLab(String name, boolean insertIfNone, boolean forceDatabaseRefresh) throws SQLException { 
    	return null;
	}
    
    /**
     * Load single Lab by ID
     * @param dbid
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Lab loadLab(int dbid, boolean forceDatabaseRefresh) throws SQLException {
	return null; 
    }
    
    /**
     * Load a collection of Labs by IDs
     * @param dbids
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<Lab> loadLabs(Collection<Integer> dbids, boolean forceDatabaseRefresh) throws SQLException {
        LinkedList<Lab> values = new LinkedList<Lab>();
        for(int dbid : dbids) { values.addLast(loadLab(dbid, forceDatabaseRefresh)); }
        return values;
    }

    /**
     * Load all Labs
     * 
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<Lab> loadAllLabs(boolean forceDatabaseRefresh) throws SQLException {
	return null;
    }	
	
    /**
     * Insert Lab by name
     * @param n
     * @return
     * @throws SQLException
     */
    private int insertLab(String n) throws SQLException {
	return 0;
    }

    
    //////////////////
    // CellLine stuff
    //////////////////

    /**
     * Load single CellLine by name
     * @param name
     * @param insertIfNone : insert into the database if there is no such entry
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public CellLine loadCellLine(String name, boolean insertIfNone, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }
    
    /**
     * Load single CellLine by ID
     * @param dbid
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public CellLine loadCellLine(int dbid, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }
    
    /**
     * Load a collection of CellLines by IDs
     * @param dbids
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<CellLine> loadCellLines(Collection<Integer> dbids, boolean forceDatabaseRefresh) throws SQLException {
        LinkedList<CellLine> values = new LinkedList<CellLine>();
        for(int dbid : dbids) { values.addLast(loadCellLine(dbid, forceDatabaseRefresh)); }
        return values;
    }

    /**
     * Load all CellLines
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<CellLine> loadAllCellLines(boolean forceDatabaseRefresh) throws SQLException {
	return null;
    }	

    /**
     * Insert a CellLine by name
     * @param n
     * @return
     * @throws SQLException
     */
    private int insertCellLine(String n) throws SQLException {
	return 0;
    }

    
    //////////////////
    // ExptCondition stuff
    //////////////////
    
    /**
     * Load a single ExptCondition by name
     * @param name
     * @param insertIfNone : insert into the database if there is no such entry
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public ExptCondition loadExptCondition(String name, boolean insertIfNone, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }        

    /**
     * Load a single ExptCondition by ID
     * @param dbid
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public ExptCondition loadExptCondition(int dbid, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }

    /**
     * Load a collection of ExptConditions by IDs
     * @param dbids
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<ExptCondition> loadExptConditions(Collection<Integer> dbids, boolean forceDatabaseRefresh) throws SQLException {
        LinkedList<ExptCondition> values = new LinkedList<ExptCondition>();
        for(int dbid : dbids) { values.addLast(loadExptCondition(dbid, forceDatabaseRefresh)); }
        return values;
    }

    /**
     * Load all ExptConditions
     * @param forceDatabaseRefresh
     * @return
     * @throws SQLException
     */
    public Collection<ExptCondition> loadAllExptConditions(boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }	

    /**
     * Insert ExptCondition by name
     * @param n
     * @return
     * @throws SQLException
     */
    private int insertExptCondition(String n) throws SQLException {
	return 0;
    }

    
    //////////////////
    // ExptTarget stuff
    //////////////////
    
    /**
     * Load a single ExptTarget by name
     * @param name
     * @param insertIfNone : insert into the database if there is no such entry
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public ExptTarget loadExptTarget(String name, boolean insertIfNone, boolean forceDatabaseRefresh) throws SQLException {
	return null; 
    }
    
    /**
     * Load a single ExptTarget by ID
     * @param dbid
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public ExptTarget loadExptTarget(int dbid, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }
  	
    /**
     * Load a collection of ExptTargets by IDs
     * @param dbids
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<ExptTarget> loadExptTargets(Collection<Integer> dbids, boolean forceDatabaseRefresh) throws SQLException {
        LinkedList<ExptTarget> values = new LinkedList<ExptTarget>();
        for(int dbid : dbids) { values.addLast(loadExptTarget(dbid, forceDatabaseRefresh)); }
        return values;
    }

    /**
     * Load all ExptTargets
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<ExptTarget> loadAllExptTargets(boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }	
	
    /**
     * Insert an ExptTarget by name
     * @param n
     * @return
     * @throws SQLException
     */
    private int insertExptTarget(String n) throws SQLException {
	return 0;
    }

    //////////////////
    // ExptType stuff
    //////////////////
    
    /**
     * Load a single ExptType by name
     * @param name
     * @param insertIfNone : insert into the database if there is no such entry
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public ExptType loadExptType(String name, boolean insertIfNone, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }
    
    /**
     * Load a single ExptType by ID
     * @param dbid
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public ExptType loadExptType(int dbid, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }
    
    /**
     * Load a collection of ExptTypes by IDs
     * @param dbids
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<ExptType> loadExptTypes(Collection<Integer> dbids, boolean forceDatabaseRefresh) throws SQLException {
        LinkedList<ExptType> values = new LinkedList<ExptType>();
        for(int dbid : dbids) { values.addLast(loadExptType(dbid, forceDatabaseRefresh)); }
        return values;
    }

    /**
     * Load all ExptTypes
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<ExptType> loadAllExptTypes(boolean forceDatabaseRefresh) throws SQLException {
	return null;
    }	
	
    /**
     * Insert an ExptType by name
     * @param n
     * @return
     * @throws SQLException
     */
    private int insertExptType(String n) throws SQLException {
	return 0;
    }

    //////////////////
    // ReadType stuff
    //////////////////
    
    /**
     * Load a single ReadType by name
     * @param name
     * @param insertIfNone : insert into the database if there is no such entry
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public ReadType loadReadType(String name, boolean insertIfNone, boolean forceDatabaseRefresh) throws SQLException {
	return null;
    }
        
    /**
     * Load a single ReadType by ID
     * @param dbid
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public ReadType loadReadType(int dbid, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }
    
    /**
     * Load a collection of ReadTypes by IDs
     * @param dbids
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<ReadType> loadReadTypes(Collection<Integer> dbids, boolean forceDatabaseRefresh) throws SQLException {
        LinkedList<ReadType> values = new LinkedList<ReadType>();
        for(int dbid : dbids) { values.addLast(loadReadType(dbid, forceDatabaseRefresh)); }
        return values;
    }

    /**
     * Load all ReadTypes
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<ReadType> loadAllReadTypes(boolean forceDatabaseRefresh) throws SQLException {
	return null;
    }	
	
    /**
     * Insert a ReadType by name
     * @param n
     * @return
     * @throws SQLException
     */
    private int insertReadType(String n) throws SQLException {
	return 0;
    }


    //////////////////
    // AlignType stuff
    //////////////////
    
    /**
     * Load a single AlignType by name
     * @param name
     * @param insertIfNone : insert into the database if there is no such entry
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public AlignType loadAlignType(String name, boolean insertIfNone, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }
    
    /**
     * Load a single AlignType by ID
     * @param dbid
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public AlignType loadAlignType(int dbid, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }
    
    /**
     * Load a collection of AlignTypes by IDs
     * @param dbids
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<AlignType> loadAlignTypes(Collection<Integer> dbids, boolean forceDatabaseRefresh) throws SQLException {
        LinkedList<AlignType> values = new LinkedList<AlignType>();
        for(int dbid : dbids) { values.addLast(loadAlignType(dbid, forceDatabaseRefresh)); }
        return values;
    }

    /**
     * Load all AlignTypes
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public Collection<AlignType> loadAllAlignTypes(boolean forceDatabaseRefresh) throws SQLException {
	return null;
    }	
	
    /**
     * Insert an AlignType by name
     * @param n
     * @return
     * @throws SQLException
     */
    private int insertAlignType(String n) throws SQLException {
	return 0;
    }

	//////////////////
	// SeqDataUser stuff
	//////////////////
    
    /**
     * Load a single SeqDataUser by name
     * @param name
     * @param insertIfNone : insert into the database if there is no such entry
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
    public SeqDataUser loadSeqDataUser(String name, boolean insertIfNone, boolean forceDatabaseRefresh) throws SQLException { 
	return null;
    }
    
    /**
     * Load a single SeqDataUser by ID
     * @param dbid
     * @param forceDatabaseRefresh : ignore cache and query database directly
     * @return
     * @throws SQLException
     */
	public SeqDataUser loadSeqDataUser(int dbid, boolean forceDatabaseRefresh) throws SQLException { 
		return null;
	}
	
	/**
	 * Load a collection of SeqDataUsers by IDs
	 * @param dbids
     * @param forceDatabaseRefresh : ignore cache and query database directly
	 * @return
	 * @throws SQLException
	 */
	public Collection<SeqDataUser> loadSeqDataUsers(Collection<Integer> dbids, boolean forceDatabaseRefresh) throws SQLException {
		LinkedList<SeqDataUser> values = new LinkedList<SeqDataUser>();
		for(int dbid : dbids) { values.addLast(loadSeqDataUser(dbid, forceDatabaseRefresh)); }
		return values;
	}
	
	/**
	 * Load all SeqDataUsers
     * @param forceDatabaseRefresh : ignore cache and query database directly
	 * @return
	 * @throws SQLException
	 */
	public Collection<SeqDataUser> loadAllSeqDataUsers(boolean forceDatabaseRefresh) throws SQLException {
		return null;
	}	
	
	/**
	 * Insert a SeqDataUser by name
	 * @param n
	 * @return
	 * @throws SQLException
	 */
	private int insertSeqDataUser(String n) throws SQLException {
		Statement s = null;
		ResultSet rs = null;
		int id=-1;
		Connection cxn = null;
		return 0;
	}

}
