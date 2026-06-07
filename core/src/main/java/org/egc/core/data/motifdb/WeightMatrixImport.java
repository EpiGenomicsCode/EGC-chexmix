package org.egc.core.data.motifdb;

import java.text.ParseException;
import java.util.*;


public class WeightMatrixImport {

    /**
     * Constructs a matrix from a set of strings.  The strings must all have the same length.
     * The WeightMatrix returned has the frequencies of the bases at each position given.
     */
    public static WeightMatrix buildAlignedSequenceMatrix(Collection<String> strings) throws ParseException {
        WeightMatrix wm = null;

        int[] counts = null;
        for(String line : strings) {
            line = line.trim().toUpperCase();
            if(line.length() > 0) {
                if(wm == null) {
                    wm = new WeightMatrix(line.length());
                    counts = new int[line.length()];
                    for(int i = 0; i < wm.length(); i++) {
                        counts[i] = 0;
                        for(int j = 0; j < wm.matrix[i].length; j++) {
                            wm.matrix[i][j] = (float)0.0;
                        }
                    }
                }

                if(line.length() != wm.length()) {
                    throw new ParseException("Line \"" + line + "\" was of uneven length (" +
                            wm.length() + ")", 0);
                }

                for(int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if(c != 'N' && c != '-') {
                        wm.matrix[i][c] += (float)1.0;
                        counts[i] += 1;
                    }
                }
            }
        }

        for(int i = 0; wm != null && i < wm.length(); i++) {
            if(counts[i] > 0) {
                for(int j = 0; j < wm.matrix[i].length; j++) {
                    wm.matrix[i][j] /= (float)counts[i];
                }
            } else {
                for(int j = 0; j < wm.matrix[i].length; j++) {
                    wm.matrix[i][j] = (float)1.0 / (float)(wm.matrix[i].length);
                }
            }
        }

        return wm;
    }

}
