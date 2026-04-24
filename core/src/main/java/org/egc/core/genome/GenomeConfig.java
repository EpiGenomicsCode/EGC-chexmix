package org.egc.core.genome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egc.core.genome.location.Region;
import org.egc.core.genome.sequence.SequenceGenerator;
import org.egc.core.gseutils.ArgParser;
import org.egc.core.gseutils.Args;
import org.egc.core.gseutils.NotFoundException;

import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;


/**
 * GenomeConfig:
 * A config parser that loads genome objects from the command-line or config files.
 * You can also use the Args class directly to load Genomes from the command-line.
 * However, GenomeConfig allows convenient loading of cached sequences as well,
 * and fits the schema of the other config parser classes.
 *
 * @author mahony
 *
 */
public class GenomeConfig {
	private Genome gen=null;
	private String genomeSequencePath=null; //Path to sequence data file directories
	private SequenceGenerator<Region> seqgen=null;
	private boolean sequenceAvailable=false;
	private boolean printHelp=false;

	private String[] args;
	public String getArgs(){
		String a="";
		for(int i=0; i<args.length; i++)
			a = a+" "+args[i];
		return a;
	}

	public GenomeConfig(String [] arguments){
		this.args=arguments;
		ArgParser ap = new ArgParser(args);
		seqgen = new SequenceGenerator<Region>();

		if(args.length==0 || ap.hasKey("h")){
			printHelp=true;
		}else{
			try{
				//Test for a config file... if there is concatenate the contents into the args
				if(ap.hasKey("config")){
					ArrayList<String> confArgs = new ArrayList<String>();
					String confName = ap.getKeyValue("config");
					File confFile = new File(confName);
					if(!confFile.isFile())
						System.err.println("\nCannot find configuration file: "+confName);
					BufferedReader reader = new BufferedReader(new FileReader(confFile));
				    String line;
			        while ((line = reader.readLine()) != null) {
			        	line = line.trim();
			        	String[] words = line.split("\\s+");
			        	if(!words[0].startsWith("--"))
			        		words[0] = new String("--"+words[0]);
			        	confArgs.add(words[0]);
			        	if(words.length>1){
				        	String rest=words[1];
				        	for(int w=2; w<words.length; w++)
				        		rest = rest+" "+words[w];
				        	confArgs.add(rest);
			        	}
			        }
			        String [] confArgsArr = confArgs.toArray(new String[confArgs.size()]);
			        String [] newargs =new String[args.length + confArgsArr.length];
			        System.arraycopy(args, 0, newargs, 0, args.length);
			        System.arraycopy(confArgsArr, 0, newargs, args.length, confArgsArr.length);
			        args = newargs;
			        ap = new ArgParser(args);
			        reader.close();
				}

				//Load genome from BAM headers
				gen = buildGenomeFromBAMHeaders(args, ap);

				//Cache genome sequence
				if(ap.hasKey("seq")){
					genomeSequencePath = ap.getKeyValue("seq");
					seqgen.setGenomePath(genomeSequencePath);
					seqgen.useCache(true);
					seqgen.useLocalFiles(true);
					sequenceAvailable=true;
				}

			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Collect BAM file paths from --expt/--ctrl flags and --design file,
	 * open each BAM header, and merge chromosome lengths into a Genome.
	 */
	private static Genome buildGenomeFromBAMHeaders(String[] args, ArgParser ap) throws IOException {
		Set<String> bamPaths = new HashSet<>();

		// Collect paths from --expt / --ctrl style flags
		for(String key : ap.getKeys()){
			if(key.contains("expt") || key.contains("ctrl")){
				Collection<String> names = Args.parseStrings(args, key);
				bamPaths.addAll(names);
			}
		}

		// Collect paths from --design file (filename is in column 0 or 1)
		if(ap.hasKey("design")){
			File df = new File(ap.getKeyValue("design"));
			try(BufferedReader reader = new BufferedReader(new FileReader(df))){
				String line;
				while((line = reader.readLine()) != null){
					if(line.startsWith("#")) continue;
					line = line.trim();
					String[] words = line.split("\\t");
					if(words.length >= 3){
						if(words[0].toUpperCase().equals("SIGNAL") || words[0].toUpperCase().equals("CONTROL"))
							bamPaths.add(words[1]);
						else if(words[1].toUpperCase().equals("SIGNAL") || words[1].toUpperCase().equals("CONTROL"))
							bamPaths.add(words[0]);
					}
				}
			}
		}

		if(bamPaths.isEmpty()) return null;

		HashMap<String, Integer> chrLenMap = new HashMap<>();
		for(String fname : bamPaths){
			File f = new File(fname);
			if(!f.isFile()){
				System.err.println("Warning: BAM file not found, skipping for genome inference: "+fname);
				continue;
			}
			try(SamReader reader = SamReaderFactory.makeDefault()
					.validationStringency(ValidationStringency.SILENT)
					.open(f)){
				for(SAMSequenceRecord rec : reader.getFileHeader().getSequenceDictionary().getSequences()){
					String name = rec.getSequenceName()
							.replaceFirst("^chromosome", "")
							.replaceFirst("^chrom", "")
							.replaceFirst("^chr", "");
					int len = rec.getSequenceLength();
					if(!chrLenMap.containsKey(name) || chrLenMap.get(name) < len)
						chrLenMap.put(name, len);
				}
			}
		}

		if(chrLenMap.isEmpty()) return null;
		return new Genome("Genome", chrLenMap);
	}

	/**
	 * Merge a set of estimated genomes
	 * @param estGenomes
	 * @return
	 */
	public Genome mergeGenomes(List<Genome> estGenomes){
		//Combine the chromosome information
		HashMap<String, Integer> chrLenMap = new HashMap<String, Integer>();
		for(Genome e : estGenomes){
			Map<String, Integer> currMap = e.getChromLengthMap();
			for(String s: currMap.keySet()){
				if(!chrLenMap.containsKey(s) || chrLenMap.get(s)<currMap.get(s))
					chrLenMap.put(s, currMap.get(s));
			}
		}
		gen =new Genome("Genome", chrLenMap);
		return gen;
	}

	//Accessors
	public Genome getGenome(){return gen;}
	public SequenceGenerator getSequenceGenerator(){return seqgen;}
	public String getGenomeSequencePath(){return genomeSequencePath;}
	public boolean sequenceAvailable(){return sequenceAvailable;}
	public boolean helpWanted(){return printHelp;}


	/**
	 * Returns a string describing the arguments handled by this config parser.
	 * @return String
	 */
	public static String getArgsList(){
		return(new String("" +
				"Genome Sequence Caching:" +
				"\t--seq <fasta seq directory>\n" +
				""));
	}
}
