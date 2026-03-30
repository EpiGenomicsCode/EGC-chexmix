package org.egc.core.viz.metaprofile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.egc.core.genome.Genome;
import org.egc.core.genome.location.Point;
import org.egc.core.genome.location.StrandedPoint;


/**
 * MetaUtils: loaders from various sources
 * 
 * @author tdanford
 * @author mahony
 *
 */
public class MetaUtils {

	private Genome genome;
	
	public MetaUtils(Genome g){
		genome = g;
	}
	
	/**
	 * Load a set of (stranded) points from a file
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public Vector<Point> loadPoints(File f) throws IOException {
		System.err.println("Loading points");
		Vector<Point> pts = new Vector<Point>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while((line = br.readLine()) != null) {
			String [] curr = line.split("\\s+");
			String coord = curr[0];
			if(!line.startsWith("#")){
				if(curr.length>=3 && curr[2].contains(":")){coord = curr[2];}
				if(coord.contains(":")) {
					String [] currB = coord.split(":");
					String chrom = currB[0].replaceAll("chr", "");
					char strand = '?';
					if(currB.length==3)
						strand = currB[2].charAt(0);
					Point pt = null;
					int location=-1;
					if(currB[1].contains("-")){
						String [] currC = currB[1].split("-");
						int start = Integer.valueOf(currC[0]);
						int stop = Integer.valueOf(currC[1]);
						location = (start+stop)/2;
						if(strand=='-' && (stop-start)%2==1)
							location+=1;
					}else{
						location = Integer.valueOf(currB[1]);
					}
					if(strand!='?')
						pt = new StrandedPoint(genome, chrom, location, strand);
					else
						pt = new Point(genome, chrom, location);
				
					pts.add(pt);
				} else { 
					System.err.println(String.format("Couldn't find point in line \"%s\"", line));
				}
			}
		}
		br.close();
		System.err.println(pts.size()+" points loaded");
		return pts;
	}
}
