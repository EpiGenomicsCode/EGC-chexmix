package org.egc.core.deepseq.hitloaders;

import java.io.File;

import org.egc.core.deepseq.experiments.ExptConfig;

/**
 * HitLoaderFactory is a simple class that defines the hitloaders.
 * Supported formats: SAM, BAM
 * @author mahony
 *
 */
public class HitLoaderFactory {

	ExptConfig econfig;

	public HitLoaderFactory(ExptConfig e){
		econfig = e;
	}

	/**
	 * Add a File HitLoader. Only SAM/BAM format is supported.
	 * @param filename file path
	 * @param format format descriptor string (SAM or BAM)
	 * @param useNonUnique whether to include non-uniquely mapped reads
	 * @return HitLoader for the given format
	 * @throws IllegalArgumentException if the file is not found or format is unknown
	 */
	public HitLoader makeFileHitLoader(String filename, String format, boolean useNonUnique){
		File file = new File(filename);
		if(!file.isFile()){
			throw new IllegalArgumentException("File not found: "+file.getName());
		}
		if(format.equals("SAM") || format.equals("BAM")){
			return new SAMFileHitLoader(file, useNonUnique, econfig.getLoadType1Reads(), econfig.getLoadType2Reads(), econfig.getLoadRead2(), econfig.getLoadPairs());
		}else{
			throw new IllegalArgumentException("Unsupported file format: "+format+". Only SAM/BAM is supported.");
		}
	}
}
