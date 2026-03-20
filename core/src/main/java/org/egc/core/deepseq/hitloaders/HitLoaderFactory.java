package org.egc.core.deepseq.hitloaders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.egc.core.deepseq.experiments.ExptConfig;

/**
 * HitLoaderFactory is a simple class that defines the hitloaders. 
 * @author mahony
 *
 */
public class HitLoaderFactory {

	ExptConfig econfig; 
	
	public HitLoaderFactory(ExptConfig e){
		econfig = e;
	}
	
	/**
	 * Add a File HitLoader. File formats accepted include:
	 * SCIDX, NOVO, BOWTIE, BED, SAM, TOPSAM
	 * @param files List of File/String Pairs, where the string is a format descriptor
	 */
	public HitLoader makeFileHitLoader(String filename, String format, boolean useNonUnique){
		HitLoader currReader=null;
		File file = new File(filename);
		if(!file.isFile() && !format.equals("HDF5Cache")){System.err.println("File not found: "+file.getName());System.exit(1);}
		if(format.equals("SAM") || format.equals("BAM")){
			currReader = new SAMFileHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads(), econfig.getLoadRead2(), econfig.getLoadPairs());
		}else if(format.equals("SAMPE") || format.equals("BAMPE")){
			currReader = new SAMFilePEHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads());
		}else if(format.equals("TOPSAM")){
			currReader = new TophatFileHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads(),  econfig.getLoadRead2(), econfig.getLoadPairs());
		}else if(format.equals("NOVO")){
			currReader = new NovoFileHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads(), econfig.getLoadPairs());
		}else if(format.equals("BOWTIE")){
			currReader = new BowtieFileHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads(), econfig.getLoadPairs());
		}else if(format.equals("BED")){
			currReader = new BEDFileHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads(), econfig.getLoadPairs());
		}else if(format.equals("SCIDX") || format.equals("IDX")){
			currReader = new IDXFileHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads(), econfig.getLoadPairs());
		}else{
		    System.err.println("Unknown file format: "+format);
		    System.exit(1);
		}
		return currReader;
	}
}
