package org.egc.chexmix.motifs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egc.core.data.io.StreamGobbler;
import org.egc.core.data.motifdb.WeightMatrix;
import org.egc.core.deepseq.experiments.ExperimentManager;
import org.egc.core.genome.Genome;
import org.egc.core.gseutils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egc.chexmix.framework.ChExMixConfig;


public class MEMERunner {
	private static final Logger logger = LogManager.getLogger(MEMERunner.class);

	protected ExperimentManager manager; 
	protected ChExMixConfig config;
	protected Genome gen;
	protected String MEMEpath;
	protected String MEMEargs;
	protected Float pseudo = (float)0.001; 
	
	//Constructor
	public MEMERunner(ChExMixConfig c, ExperimentManager man){
		config=c;
		manager=man;
		
		MEMEpath = config.getMEMEpath();
		MEMEargs = config.getMEMEargs(); 
	}
	
	/**
	 * Run MEME on the sequences, and store the best motifs (aligned with each other) in the ExperimentConditions
	 * @param sequences: List of Lists of sequences. Indexed by condition
	 */
	public Pair<List<WeightMatrix>,List<WeightMatrix>> execute(List<String> sequences, String memeOutDirName, boolean bestOnly){
		List<WeightMatrix> wm = new ArrayList<WeightMatrix>();
		List<WeightMatrix> fm = new ArrayList<WeightMatrix>();
		File workingDir = config.getOutputIntermediateDir();
		String memeOutDir = workingDir+"/meme_"+memeOutDirName;
		try {
			//Set up the input file
			File seqFile= File.createTempFile("seq", ".fa", workingDir);
			String seqFilename = seqFile.getCanonicalPath();
			FileWriter fout = new FileWriter(seqFile);
			int sCount=1;
			for(String seq : sequences){
				fout.write(">Seq"+sCount+"\n"+seq+"\n");
				sCount++;
			}
			fout.close();
			
			//Test if meme directory exists. If it does, recursively delete contents
			File memeOutPath = new File(memeOutDir);
			if(memeOutPath.exists())
				config.deleteDirectory(memeOutPath);
			
			//Call MEME
			String MEMEcmd = MEMEpath+"meme ";
			Process proc;
			if(config.getMaxThreads()==1 || config.getMEMEnonparallel()){
				proc = Runtime.getRuntime().exec(MEMEcmd+" "+seqFilename+" "+MEMEargs +" -o "+memeOutDir);				
			}else{
				proc = Runtime.getRuntime().exec(MEMEcmd+" "+seqFilename+" "+MEMEargs +" -p "+config.getMaxThreads()+" -o "+memeOutDir);				
			}		
			// any error message?
			Thread errorGobbler = new Thread(new StreamGobbler(proc.getErrorStream(), "MEME_ERR", true));
			// any output?
			Thread outputGobbler = new Thread(new StreamGobbler(proc.getInputStream(), "MEME_OUT", false));
			// kick them off
			errorGobbler.start();
			outputGobbler.start();
			// any error??? 
			int exitVal = proc.waitFor(); 
			System.err.println("MEME ExitValue: " + exitVal);

            File memeOutFile = new File(memeOutDir+"/meme.txt");
            if (!memeOutFile.exists()) {
            	//Clean up intermediate files (fasta, etc)
                if(seqFile.exists())
                	seqFile.delete();
            	throw new FileNotFoundException("Can't find file " + memeOutFile.getName());
            }else{
	            BufferedReader memeReader = new BufferedReader(new FileReader(memeOutFile));
	            Map<String,Double> back = parseMEMEResultsForBackground(memeReader);
	            memeReader.close();
	            BufferedReader memeReader2 = new BufferedReader(new FileReader(memeOutFile));
	            List<Pair<WeightMatrix,Double>> currFM = parseMEMEResultsForFreqMatries(memeReader2);
	            memeReader2.close();
	            
	            //Extract best motif?
				if(bestOnly){
					double minScore = Double.MAX_VALUE; 
					WeightMatrix bestMotif=null; 
					for(Pair<WeightMatrix,Double> m : currFM)
						if(m.cdr()<minScore){
							minScore = m.cdr();
							bestMotif = m.car();
						}
					fm.add(bestMotif);
					WeightMatrix wMatrix = WeightMatrix.getLogOddsVersion(bestMotif, back);
					wm.add(wMatrix);
					//System.out.println(bestMotif.getName()+"\t"+minScore);
					//System.out.println(WeightMatrix.printMatrix(bestMotif));
				}else{
					for(Pair<WeightMatrix,Double> m : currFM){
						fm.add(m.car());
						WeightMatrix wMatrix = WeightMatrix.getLogOddsVersion(m.car(), back);
						wm.add(wMatrix);
					}
				}
            }
            //Clean up intermediate files (fasta, etc)
            if(seqFile.exists())
            	seqFile.delete();
            proc.destroy();
		} catch (IOException e) {
			logger.error("MEME execution failed", e);
		} catch (InterruptedException e) {
			logger.error("MEME process interrupted", e);
			Thread.currentThread().interrupt();
		}
		return new Pair<List<WeightMatrix>,List<WeightMatrix>>(wm,fm);
	}
	
	protected Map<String,Double> parseMEMEResultsForBackground(BufferedReader memeOut){
		Map<String,Double> back = new HashMap<String,Double>();
		try {
			String line=memeOut.readLine();
			int lineno = 1; 
			while (line!=null && !line.matches(".*Background letter frequencies.*")) {
		      line = memeOut.readLine();
		      lineno++;
		    }
			line = memeOut.readLine();
			if(line!=null){
		    	try {
		    		String[] pieces = line.split("\\s+");
		    		double A = Double.parseDouble(pieces[1]);
		    		double C = Double.parseDouble(pieces[3]);
		    		double G = Double.parseDouble(pieces[5]);
		    		double T = Double.parseDouble(pieces[7]);
		    		back.put("A", A);
		    		back.put("C", C);
		    		back.put("G", G);
		    		back.put("T", T);
		    	}
		    	catch (NumberFormatException ex) {
		    		logger.error("Parse error at line {}: {}", lineno, line);
		    		throw ex;
		    	}
		    	catch (ArrayIndexOutOfBoundsException ex) {
		    		logger.error("Index error at line {}: {}", lineno, line);
		    		throw ex;
		    	}
			}
		} catch (NumberFormatException e) {
			logger.error("Failed to parse MEME background frequencies", e);
		} catch (IOException e) {
			logger.error("Error reading MEME output for background", e);
		}
		return back;
	}
	
	protected List<Pair<WeightMatrix,Double>> parseMEMEResultsForFreqMatries(BufferedReader memeOut){
		List<Pair<WeightMatrix,Double>> parsed = new ArrayList<Pair<WeightMatrix,Double>>();
		String line;
		int lineno = 1; 
		int motifCount=0;
		try {
			while((line = memeOut.readLine()) != null){
				while (line!=null && !line.matches(".*letter-probability matrix.*")) {
			      line = memeOut.readLine();
			      lineno++;
			    }
				if(line!=null){
					motifCount++;
				    String lenStr = line.replaceFirst("^.*w=\\s*", "");
				    lenStr = lenStr.replaceFirst("\\s*nsites=.*", "");
				    int length = Integer.parseInt(lenStr);
				    String EStr = line.replaceFirst("^.*E=\\s*", "");
				    double Eval = Double.parseDouble(EStr);
				    WeightMatrix matrix = new WeightMatrix(length);
				    matrix.setNameVerType("Motif"+motifCount, "freq", "MEME");
				    for (int i = 0; i < length; i++) {
				    	line = memeOut.readLine().replaceFirst("^\\s*", "");
				    	lineno++;
				    	try {
				    		String[] pieces = line.split("\\s+");
				    		float A = Float.parseFloat(pieces[0])+pseudo;
				    		float C = Float.parseFloat(pieces[1])+pseudo;
				    		float G = Float.parseFloat(pieces[2])+pseudo;
				    		float T = Float.parseFloat(pieces[3])+pseudo;
				    		float total = A+C+G+T;
				    		matrix.matrix[i]['A'] = A/total;
				    		matrix.matrix[i]['C'] = C/total;
				    		matrix.matrix[i]['G'] = G/total;
				    		matrix.matrix[i]['T'] = T/total;
				    	}
				    	catch (NumberFormatException ex) {
				    		System.err.println("At line " + lineno + ": " + line);
				    		ex.printStackTrace();
				    		throw ex;
				    	}
				    	catch (ArrayIndexOutOfBoundsException ex) {
				    		System.err.println("At line " + lineno + ": " + line);
				    		ex.printStackTrace();
				    		throw ex;
				    	}
				    }
				    matrix.setLogOdds();
				    parsed.add(new Pair<WeightMatrix,Double>(matrix, Eval));
				}
			}
		} catch (NumberFormatException e) {
			logger.error("Failed to parse MEME frequency matrix", e);
		} catch (IOException e) {
			logger.error("Error reading MEME output for frequency matrices", e);
		}
		return parsed;
	}
	
}
	
