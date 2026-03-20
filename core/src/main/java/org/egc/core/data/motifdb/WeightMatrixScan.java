package org.egc.core.data.motifdb;
import java.util.*;
import java.sql.*;

import org.egc.core.gseutils.NotFoundException;


public class WeightMatrixScan {
    public int scandbid;
    public boolean hasscandbid;
    public String scanname;
    public float cutoff;
    public WeightMatrix matrix;

    public static WeightMatrixScan getWeightMatrixScan(int dbid) throws NotFoundException {
        return null;    
    }

    public static Collection<WeightMatrixScan> getScansForMatrix (int matrixid) {
        return null;    
    }

    public static WeightMatrixScan getScanForMatrix(int matrixid, String scanname) {
        return null;    
    }

    public static Collection<WeightMatrixScan> getScansForSpecies (int speciesid, String name, String version, String type, String scanname) {
        return null;    
    }

    public static Collection<WeightMatrixScan> getScansForGenome (int genomeid, String name, String version, String type, String scanname) {
        return null;    
    }

    private static Collection<String> getField(int genomeid, String field) {
        return null;    
    }
    
    public static Collection<String> getNames(int genomeid) {return getField(genomeid,"wm.name");}
    public static Collection<String> getVersions(int genomeid) {return getField(genomeid,"wm.version");}
    public static Collection<String> getTypes(int genomeid) {return getField(genomeid,"wm.type");}
    public static Collection<String> getScanNames(int genomeid) {return getField(genomeid,"wms.name");}



    public String toString() {
        return null;    
    }
    public boolean equals(Object other) {
	return false;
    }
    public int hashCode() {
	return 0;
    }

}
