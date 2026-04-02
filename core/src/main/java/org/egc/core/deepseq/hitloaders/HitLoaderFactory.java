package org.egc.core.deepseq.hitloaders;

import java.io.File;

import org.egc.core.deepseq.experiments.ExptConfig;

/**
 * HitLoaderFactory is a simple class that defines the hitloaders.
 * Supported formats: SAM, BAM, SAMPE, BAMPE, BED
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
	 * SAM, BAM, SAMPE, BAMPE, BED
	 * @param filename file path
	 * @param format format descriptor string
	 * @param useNonUnique whether to include non-uniquely mapped reads
	 * @return HitLoader for the given format
	 * @throws IllegalArgumentException if the file is not found or format is unknown
	 */
	public HitLoader makeFileHitLoader(String filename, String format, boolean useNonUnique){
		HitLoader currReader=null;
		File file = new File(filename);
		if(!file.isFile()){
			throw new IllegalArgumentException("File not found: "+file.getName());
		}
		if(format.equals("SAM") || format.equals("BAM")){
			currReader = new SAMFileHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads(), econfig.getLoadRead2(), econfig.getLoadPairs());
		}else if(format.equals("SAMPE") || format.equals("BAMPE")){
			currReader = new SAMFilePEHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads());
		}else if(format.equals("BED")){
			currReader = new BEDFileHitLoader(file,useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads(), econfig.getLoadPairs());
		}else{
			throw new IllegalArgumentException("Unknown file format: "+format);
		}
		return currReader;
	}
}
