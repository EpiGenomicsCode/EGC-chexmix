package org.egc.core.genome;


import java.util.*;
import java.io.*;

import org.egc.core.genome.location.ChromosomeInfo;

/**
 * Genome represents one version (or genome build) of some species.
 * <i>Note</i>: We assume 1-based, inclusive coordinate.
 */
public class Genome{
    
    private Species species;
    private String version;
    private Map<String,ChromosomeInfo> chromsByName;
    private Map<Integer,ChromosomeInfo> chromsByID;
    
    
    /**
     * Construct a Genome from a file of chromosome lengths
     * @param tempName
     * @param chrLengths
     * @param inventids
     */
    public Genome(String tempName, File chrLengths, boolean inventids) {
    	species = new Species(-1, "FakeOrganism");
    	version = tempName;
    	chromsByName = new HashMap<String,ChromosomeInfo>();
    	chromsByID = new HashMap<Integer,ChromosomeInfo>();
    	if(!chrLengths.isFile()){throw new RuntimeException("Invalid genome info file: " + chrLengths.getAbsolutePath());}
        BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(chrLengths));
		    String line;
	        int id=0;
	        while ((line = reader.readLine()) != null) {
	            line = line.trim();
	            String[] words = line.split("\\s+");
	            if(words.length>=2){
	            	String chr = words[0].replaceFirst("^chromosome", "");
	            	chr = chr.replaceFirst("^chrom", "");
	            	chr = chr.replaceFirst("^chr", "");
	            	ChromosomeInfo info;
	            	if (inventids) {
	            		info = new ChromosomeInfo(id++, Integer.parseInt(words[1]), chr);
	            	} else {
	            		info = new ChromosomeInfo(Integer.parseInt(words[2]), Integer.parseInt(words[1]), chr);
	            	}
	            	chromsByName.put(info.getName(), info);
	            	chromsByID.put(info.getDBID(), info);
	            }
	    	}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Construct a genome from a Map of names and lengths
     *  (mostly used to merge fake genomes that are data generated)
     * @param tempName
     * @param chrLengthMap
     */
    public Genome(String tempName, Map<String, Integer> chrLengthMap) {
    	species = new Species(-1, "FakeOrganism");
    	version = tempName;
    	chromsByName = new HashMap<String,ChromosomeInfo>();
    	chromsByID = new HashMap<Integer,ChromosomeInfo>();
    	int id=0;
    	for(String s : chrLengthMap.keySet()){
    		ChromosomeInfo info = new ChromosomeInfo(id--, chrLengthMap.get(s), s);
        	chromsByName.put(info.getName(), info);
        	chromsByID.put(info.getDBID(), info);
    	}
    }
    
    //Accessors
    public String getVersion() {return version;}
    public Species getSpecies(){return species;}
    public String getSpeciesName() {return species.getName();}    
    public String toString() { return getSpeciesName() +","+getVersion(); }

    //Chromosome-related accessors
    public Collection<ChromosomeInfo> getChromInfo(){ if(chromsByName!=null){return chromsByName.values();}else{return null;}}
    public List<String> getChromList() { return new LinkedList<String>(chromsByName.keySet()); }
    public ChromosomeInfo getChrom(String name) { return chromsByName.get(name); }
    public boolean containsChromName(String chromName) { return chromsByName.containsKey(chromName); }
    public String getChromName(int chromID) { return chromsByID.get(chromID).getName(); }
    public int getChromID(String chromName) { 
        if (chromsByName.get(chromName) == null) {
            throw new NullPointerException("Null chromosome for " + chromName);
        }
        return chromsByName.get(chromName).getDBID(); 
    }
    public int getChromLength(String chromName) { return chromsByName.get(chromName).getLength(); }
    public Map<String,Integer> getChromLengthMap() { 
        Map<String,Integer> chromLengths = new HashMap<String,Integer>();
        for(String n : chromsByName.keySet()) { chromLengths.put(n, chromsByName.get(n).getLength()); }
        return chromLengths;
    }
    
    /** Returns the genome info string with chromosome name <tab> length format*/
    public String getGenomeInfo(){
    	StringBuilder sb = new StringBuilder();
    	for(String n : chromsByName.keySet()) { sb.append(n).append("\t").append(chromsByName.get(n).getLength()).append("\n"); }
        return sb.toString();
    }

    /**
     * Return total length of all chromosomes
     * @return
     */
    public double getGenomeLength() { 
        double totalLen=0;
        for(String n : chromsByName.keySet()) { totalLen+= (double)chromsByName.get(n).getLength();}
        return totalLen;
    }

    
    //Roman numeral to integer translation helpers
	private static int[] romvals;
    private static String[] intvals;
    public static String convertChromNameToRoman(String c) {
        return convertChromNameToRoman(Integer.parseInt(c));
    }
    public static String convertChromNameToRoman(int chrom) {
        if(intvals == null) { 
            intvals = new String[10];
            intvals[0] = "X";
            intvals[1] = "I";
            intvals[2] = "II";
            intvals[3] = "III";
            intvals[4] = "IV";
            intvals[5] = "V";
            intvals[6] = "VI";
            intvals[7] = "VII";
            intvals[8] = "VIII";
            intvals[9] = "IX";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("chr");
        while(chrom >= 10) { 
            chrom -= 10;
            sb.append(intvals[0]);
        }
        if(chrom > 0)
            sb.append(intvals[chrom]);
        return sb.toString();
    }
    public static String convertChromNameFromRoman(String chrom) {
        if (romvals == null) {
            romvals = new int[Character.getNumericValue('Z')];
            romvals[Character.getNumericValue('X')] = 10;
            romvals[Character.getNumericValue('V')] = 5;
            romvals[Character.getNumericValue('I')] = 1;
        }
        String chr = chrom;
        chr = chr.replaceAll("\\.fa?s?t?a$","");
        if (chr.matches("^[cC][hH][rR].*")) {
            chr = chr.substring(3);
        } 

        if (chr.matches("^[1234567890MmtUnXY]+(_random)?[LRh]?$")) {
            return chr;
        } else {
            throw new NumberFormatException("Can't fix chrom name " + chrom + "," + chr);
        }
    }
    public static String convertYeastChromNameFromRoman(String chrom) {
        if (romvals == null) {
            romvals = new int[Character.getNumericValue('Z')];
            romvals[Character.getNumericValue('X')] = 10;
            romvals[Character.getNumericValue('V')] = 5;
            romvals[Character.getNumericValue('I')] = 1;
        }
        String chr = chrom;
        chr = chr.replaceAll("\\.fa?s?t?a$","");
        if (chr.matches("^[cC][hH][rR].*")) {
            chr = chr.substring(3);
        } 
        if (chr.matches("^[XVI]+$")) {
            int val = 0, pos = 1, curval, lastval, buffer; char cur, last;
            boolean random = false;
            if (chr.matches("_random$")) {
                random = true;
                chr.replaceFirst("_random$","");
            }            
            last = chr.charAt(0);
            lastval = romvals[Character.getNumericValue(last)];
            buffer = lastval;
            //            System.err.println("== " + buffer);
            while (pos < chr.length()) {
                cur = chr.charAt(pos);
                curval = romvals[Character.getNumericValue(cur)];
                if (curval > lastval) {
                    val += curval - lastval;
                    buffer = 0;
                } else if (cur != last) {
                    val += buffer;
                    buffer = curval;
                } else {
                    buffer += curval;
                }
                last = cur;
                lastval = curval;
                pos++;
            }
            val += buffer;
            if (random) {
                return Integer.toString(val) + "_random";
            } else {
                return Integer.toString(val);
            }
        } else 
        if (chr.matches("^[1234567890MUXY]+(_random)?[LRh]?$")) {
            return chr;
        } else if (chr.matches("Mito")) {
            return "mt";
        } else {
            throw new NumberFormatException("Can't fix chrom name " + chrom + "," + chr);
        }
    }

	public int hashCode() {
        return getSpeciesName().hashCode()*37 + getVersion().hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof Genome) {
            Genome other = (Genome)o;
            return (getSpeciesName().equals(other.getSpeciesName()) &&
                    getVersion().equals(other.getVersion()));
        } else {
            return false;
        }
    }

}
