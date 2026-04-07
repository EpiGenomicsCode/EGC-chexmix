package org.egc.core.genome.location;

import java.util.regex.*;

import org.egc.core.genome.Genome;
import org.egc.core.genome.location.Point;


public class PointParser {
    
    private static Pattern pPatt;
    
    static { 
        pPatt = Pattern.compile("(\\w+):(\\d+)");
    }
    
    private Genome genome;
    private int chromIndex, startIndex, minLength;

    public PointParser(Genome g) {
        genome = g;
        chromIndex = 0;
        startIndex = 1;
        minLength = startIndex + 1;
    }

    /* (non-Javadoc)
     */
    public Point execute(String input) {
        String[] array = input.split("\\s+");
        String chrom = array[chromIndex];
        chrom = chrom.replaceFirst("chr", "");
        
        Matcher m = pPatt.matcher(chrom);
        if(m.matches()) { 
            chrom = m.group(1);
            int start = Integer.parseInt(m.group(2));
            return new Point(genome, chrom, start);
        } else { 
            if(array.length >= minLength) { 
                int start = Integer.parseInt(array[startIndex]);
                return new Point(genome, chrom, start);
            } else { 
                System.err.println("Line \"" + input + "\" doesn't have the correct length (" + minLength + ")");
                return null;
            }
        }
    }
}
