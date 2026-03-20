package org.egc.core.data.seqdata;

import java.util.*;
import java.io.*;
import java.sql.*;

import org.egc.core.genome.Genome;
import org.egc.core.genome.location.Region;
import org.egc.core.gseutils.NotFoundException;


/**
 * A SeqAnalysis represents the results of running some binding-call or 
 * peak finding program on a set of SeqAlignments.  The name and version of 
 * the analysis are independent of the name and version of the alignments, though
 * in practice they should be related; the name and version are
 * used as they DB key.
 *
 * The analysis stores a set of parameters (Map<String,String>) that can be 
 * any relevant data about how the binding calls were generated.
 */

public class SeqAnalysis implements Comparable<SeqAnalysis> {

    private Map<String,String> params;
    private Set<SeqAlignment> foreground, background;
    private String name, version, program;
    private Integer dbid;
    private List<SeqAnalysisResult> results;
    private boolean active;

    /* these methods (through store()) are primarily for constructing a 
       SeqAnalysis and saving it to the DB */
    public SeqAnalysis (String name, String version, String program) {
        this.name = name;
        this.version = version;
        this.program = program;
        dbid = null;
        params = null;
        foreground = null;
        background = null;
        results = new ArrayList<SeqAnalysisResult>();
        this.active = true;
    }
    public SeqAnalysis (String name, String version, String program, boolean active) {
        this.name = name;
        this.version = version;
        this.program = program;
        dbid = null;
        params = null;
        foreground = null;
        background = null;
        results = new ArrayList<SeqAnalysisResult>();
        this.active = active;
    }
    public void setActive(boolean b) {active = b;}
    public void setParameters(Map<String,String> params) {
        this.params = params;
    }
    public void setInputs(Set<SeqAlignment> foreground,
                          Set<SeqAlignment> background) {
        this.foreground = foreground;
        this.background = background;
    }
    public void addResult(SeqAnalysisResult result) {
        results.add(result);
    }
    private void storeinputs(PreparedStatement ps,
                             String type,
                             int analysis,
                             int alignment) throws SQLException {
        ps.setInt(1,analysis);
        ps.setInt(2,alignment);
        ps.setString(3, type);
        ps.execute();
    }
                             
    public void store() throws SQLException {
    }
    /* stores the active flag to the database.  Must be done
       on an object that has already been stored such that it has a DBID */
    public void storeActiveDB() throws SQLException {
    }

    /* these methods are primarily for querying an object that you've gotten back
       from the database */
    public String toString() {
        return name + ";" + version + ";" + program;
    }
    public Integer getDBID() {return dbid;}
    public String getName() {return name;}
    public String getVersion() {return version;}
    public String getProgramName() {return program;}
    public boolean isActive() {return active;}
    public Map<String,String> getParams() {
        if (params == null) {
        }
        return params;
    }
    public Set<SeqAlignment> getForeground() {
        if (foreground == null) {
        }
        return foreground;
    }
    public Set<SeqAlignment> getBackground() {
        if (background == null) {
        }
        return background;
    }
    /** fills in the parameters from the database */
    private void loadParams() throws SQLException {
    }
    /** fills in the input experiment fields from the database */
    private void loadInputs() throws SQLException {
    }
    public List<SeqAnalysisResult> getResults(Genome g) throws SQLException {
        return getResults(g,null);
    }
    public List<SeqAnalysisResult> getResults(Region queryRegion) throws SQLException {
        return getResults(queryRegion.getGenome(), queryRegion);
    }
    private Integer isnullint(ResultSet r, int index) throws SQLException {
        Integer i = r.getInt(index);
        if (i == 0 && r.wasNull()) {
            return null;
        } else {
            return i;
        }
    }
    private Double isnulldouble(ResultSet r, int index) throws SQLException {
        Double i = r.getDouble(index);
        if (i == 0 && r.wasNull()) {
            return null;
        } else {
            return i;
        }
    }
    public List<SeqAnalysisResult> getResults(Genome genome, Region queryRegion) throws SQLException {
        return null;
       
    }
   

    /** retrieves all ChipSeqAnalysis objects from the database */
    public static Collection<SeqAnalysis> getAll() throws SQLException {
        return getAll(true);
    }
    public static Collection<SeqAnalysis> getAll(Boolean active) throws SQLException {
        return null;
    }
    /** Retrieves the ChipSeqAnalysis with the specified name and version */
    public static SeqAnalysis get(SeqDataLoader loader, String name, String version) throws NotFoundException, SQLException {
        return get(loader,name,version,true);
    }
    public static SeqAnalysis get(SeqDataLoader loader, String name, String version, Boolean active) throws NotFoundException, SQLException {
        return null;
    }
    /** returns the collection of active analyses that have a result in the specified region
     */
    public static Collection<SeqAnalysis> withResultsIn(SeqDataLoader loader, Region r) throws SQLException {
	return null;
    }
 
    public int compareTo(SeqAnalysis other) {
        int c = name.compareTo(other.name);
        if (c == 0) {
            c = version.compareTo(other.version);
            if (c == 0) {
                c = program.compareTo(other.program);
            }
        }
        return c;
    }


}
