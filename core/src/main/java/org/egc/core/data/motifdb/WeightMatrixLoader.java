package org.egc.core.data.motifdb;

import java.sql.*;
import java.util.*;

import org.egc.core.genome.Species;
import org.egc.core.gseutils.NotFoundException;


public class WeightMatrixLoader implements org.egc.core.gseutils.Closeable {

    public WeightMatrixLoader() {}

    private Collection<String> queryWMTable(String field) {
        return null;    
    }
    public Collection<String> getNames() {
        return null;    
    }
    public Collection<String> getVersions() {
        return null;    
    }

    public Collection<String> getTypes() {
        return null;
    }

    public WeightMatrix query(int speciesid,
                              String name,
                              String version) {
        return null;
    }
    
    public Collection<WeightMatrix> loadMatrices(Species species) throws SQLException { 
	return null;
    }

    public Collection<WeightMatrix> query(String name,
                                          String version,
                                          String type) {
	return null;
    }

    public void close() {}
    public boolean isClosed() {return true;}
}
