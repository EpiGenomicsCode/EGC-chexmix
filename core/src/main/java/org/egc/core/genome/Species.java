package org.egc.core.genome;

import java.util.*;


import java.sql.*;

import org.egc.core.gseutils.*;

/**
 * Species represents a species in our database
 */
public class Species{

    //static cache of all Species
    private static Map<String,Species> organisms = new HashMap<String,Species>();
    private static Map<Integer, Species> organismsids = new HashMap<Integer,Species>();

    //Variables that define this Species
    private String species;
    private int dbid;
    
    /**
     * Constructor: no db lookup needed
     * @param id : use -1 for fake organisms (i.e. not in db)
     * @param species
     * @throws NotFoundException
     */
    public Species(int id, String species){
    	this.dbid = id;
    	this.species = species;
    	
    }
    
    /**
     * Constructor: look up species name in core database
     * @param species
     * @throws NotFoundException
     */
    public Species(String species) throws NotFoundException {
    	this.species = species;
    	if(organisms.isEmpty()){
    		getAllSpecies(false);
        }
        if (organisms.containsKey(species)) {
            this.dbid = organisms.get(species).getDBID();
        }
    }


    /**
     * Constructor: look up dbid in core database
     * @param speciesID
     * @throws NotFoundException
     */
    public Species(int speciesID) throws NotFoundException {
        this.dbid = speciesID;
        if(organisms.isEmpty()){
        	getAllSpecies(false);
        }
        if (organisms.containsKey(dbid)) {
            this.species = organisms.get(dbid).getName();
        }
        
    }



    /**
     * Accessor for species name
     */
    public String getName() {
        return species;
    }


    /**
     * Accessor for database ID
     */
    public int getDBID() {
        return dbid;
    }

	/**
	 * Returns a list of genomes for this species
	 * @return
	 */
    public Collection<Genome> getGenomes(){
    	try {
			return Genome.getAllGenomesBySpecies(this);
		} catch (NotFoundException e) {
			e.printStackTrace();			
		}
    	return null;
    }
    
    /**
	 * Returns a list of genome names for this species
	 * @return
	 */
    public Collection<String> getGenomeNames(){
    	List<String> names = new ArrayList<String>();
    	for(Genome g : getGenomes()){
    		names.add(g.getVersion()); 
    	}
    	return names;
    }
    
	/**
	 * Load all Organisms from database 
	 * @return
	 */
    public static Collection<Species> getAllSpecies(boolean forceRefreshFromDB){
    	List<Species> orgs = new ArrayList<Species>();
    	
    		orgs.addAll(organisms.values());
	    return orgs;
    }

    /**
     * Get a specific Species by name
     * @param species
     * @return
     * @throws NotFoundException
     */
    public static Species getSpecies(String species) throws NotFoundException {
        if(organisms.isEmpty()){
        	getAllSpecies(false);
        }
    	if (organisms.containsKey(species)) {
            return organisms.get(species);
        } else {
            Species o = new Species(species);
            organisms.put(species, o);
            organismsids.put(o.getDBID(), o);
            return o;
        }
    }


    /**
     * Get a specific Species by ID
     * @param species
     * @return
     * @throws NotFoundException
     */
    public static Species getSpecies(int species) throws NotFoundException {
    	if(organisms.isEmpty()){
        	getAllSpecies(false);
        }
        if (organismsids.containsKey(species)) {
            return organismsids.get(species);
        } else {
            Species o = new Species(species);
            organisms.put(o.getName(), o);
            organismsids.put(o.getDBID(), o);
            return o;
        }
    }
    
    /**
     * Get all Species names from the database
     * @return
     */
    public static Collection<String> getAllSpeciesNames(boolean forceRefreshFromDB) {
    	if(organisms.isEmpty() || forceRefreshFromDB){
        	getAllSpecies(forceRefreshFromDB);
        }
    	return organisms.keySet();
    }

    /**
     * Insert a new Species into the database
     * @param species
     * @throws SQLException
     */
    public static void insertSpecies(String species) throws SQLException {
        java.sql.Connection cxn = null;
        Statement stmt = null;
        
    }


}
