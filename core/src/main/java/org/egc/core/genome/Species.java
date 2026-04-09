package org.egc.core.genome;


/**
 * Species represents a species.
 * In local-only mode, Species is a simple name holder with no database lookup.
 */
public class Species{

    private String species;
    private int dbid;
    
    /**
     * Constructor: no db lookup needed
     * @param id : use -1 for fake organisms (i.e. not in db)
     * @param species
     */
    public Species(int id, String species){
    	this.dbid = id;
    	this.species = species;
    }
    
    /**
     * Constructor from species name. In local-only mode, dbid defaults to -1.
     * @param species
     * @ (retained for API compatibility; never thrown in local-only mode)
     */
    public Species(String species)  {
    	this.species = species;
    	this.dbid = -1;
    }

    /**
     * Accessor for species name
     */
    public String getName() {
        return species;
    }

    /**
     * Accessor for database ID (always -1 in local-only mode)
     */
    public int getDBID() {
        return dbid;
    }

}
