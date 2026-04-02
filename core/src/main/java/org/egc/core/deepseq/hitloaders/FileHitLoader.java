package org.egc.core.deepseq.hitloaders;

import java.io.File;

/**
 * FileHitLoader: Loads reads from alignment files. 
 * Formats supported: SAM, BAM, BED
 * 
 * @author shaun
 *
 */
public abstract class FileHitLoader extends HitLoader{

	protected File file;
	protected boolean useNonUnique=true;
		
	/**
	 * Constructor
	 * @param file alignment file
	 * @param useNonUnique boolean -- load non-uniquely mapping reads
	 * @param loadT1 load Type1 reads
	 * @param loadT2 load Type2 reads
	 * @param loadRead2 load second in pair reads
	 * @param loadPairs load pair information
	 */
	public FileHitLoader(File file, boolean useNonUnique, boolean loadT1, boolean loadT2, boolean loadRead2, boolean loadPairs){
		super(loadT1, loadT2, loadRead2, loadPairs);
		this.file = file;
		this.useNonUnique=useNonUnique;
		this.sourceName = file.getName();
	}
	
	/**
	 * No cleanup for file loaders
	 */
	public void cleanup(){}
}
