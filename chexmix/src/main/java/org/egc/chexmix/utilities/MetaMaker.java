package org.egc.chexmix.utilities;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.egc.core.deepseq.experiments.ExperimentCondition;
import org.egc.core.deepseq.experiments.ExperimentManager;
import org.egc.core.genome.Genome;
import org.egc.core.genome.GenomeConfig;
import org.egc.core.genome.location.Point;
import org.egc.chexmix.framework.ChExMixConfig;


public class MetaMaker {
	
	private ChExMixConfig config;
	private GenomeConfig gconfig;
	private MetaConfig mconfig;
	private ExperimentManager manager;
	
	public MetaMaker(ChExMixConfig con, GenomeConfig g, MetaConfig m, ExperimentManager man){
		config = con;
		gconfig = g;
		mconfig = m;
		manager = man;
	}
	
	public void run(){
		try {
			if(mconfig.printHelp){
				System.err.println("MetaMaker:\n" +
						gconfig.getArgsList()+"\n"+
						mconfig.getArgsList()+"\n");
			}else{
				Genome gen = gconfig.getGenome();
				
				BinningParameters params = new BinningParameters(mconfig.winLen, mconfig.bins);
				System.out.println("Binding Parameters:\tWindow size: "+params.getWindowSize()+"\tBins: "+params.getNumBins());
				
				for (ExperimentCondition cond : manager.getConditions()){
					
					String imagePrefix = config.getOutputImagesDir()+File.separator+config.getOutBase()+"_"+mconfig.outName+"_"+cond.getName()+"_"+mconfig.strand;
					String intPrefix = config.getOutputIntermediateDir()+File.separator+config.getOutBase()+"_"+mconfig.outName+"_"+cond.getName()+"_"+mconfig.strand;
								
					PointProfiler profiler= new Stranded5PrimeProfiler(gconfig, params, cond, mconfig.strand, mconfig.fivePrimeShift, mconfig.baseLimit, mconfig.baseLimitRelPosition);
					boolean normalizeProfile=false;
				
					if(mconfig.batchRun){
						System.out.println("Batch running...");
						System.setProperty("java.awt.headless", "true");
						if(mconfig.peakFiles.size()==1 || mconfig.peakFiles.size()==0){
							MetaProfileRenderer renderer = new MetaProfileRenderer(gen, params, profiler, normalizeProfile, mconfig.saveSVG);
							renderer.setColor(mconfig.color);
							renderer.setDrawColorBar(mconfig.drawColorBar);
							renderer.setTransparent(mconfig.transparent);
							renderer.setDrawBorder(mconfig.drawBorder);
							MetaProfileHandler handler = renderer.getHandler();
							if(mconfig.peakFiles.size()==1){
								System.out.println("Single set mode...");
								File peakFile = new File(mconfig.peakFiles.get(0));
								if (peakFile.exists()){
									Vector<Point> points = renderer.getUtils().loadPoints(peakFile);
									handler.addPoints(points);
								}
							}else{
								System.err.println("All TSS mode requires gene annotation loading, which has been removed. Provide a peak file with --peaks instead.");
							}
							handler.awaitCompletion();
							if(mconfig.cluster)
								renderer.clusterLinePanel();
							//Set the panel sizes here...
							renderer.setStyle(mconfig.profileStyle);
							renderer.setLineMax(mconfig.lineMax);
							renderer.setLineMin(mconfig.lineMin);
							renderer.setLineThick(mconfig.lineThick);
							renderer.saveImages(imagePrefix);
							renderer.savePointsToFile(intPrefix);
						}else if(mconfig.peakFiles.size()>1){
							System.out.println("Multiple set mode...");
							MetaProfileRenderer multirenderer = new MetaProfileRenderer(mconfig.peakFiles, gen, params, profiler, true);
							for(int x=0; x<mconfig.peakFiles.size(); x++){
								String pf = mconfig.peakFiles.get(x);
								Vector<Point> points = multirenderer.getUtils().loadPoints(new File(pf));
								List<MetaProfileHandler> handlers = multirenderer.getHandlers();
								handlers.get(x).addPoints(points);
								handlers.get(x).awaitCompletion();
							}
							multirenderer.saveImage(imagePrefix);
							multirenderer.savePointsToFileMulti(intPrefix);
						}
						System.out.println("Finished");
						if(profiler!=null)
							profiler.cleanup();
					}else{
						System.err.println("Interactive MetaFrame mode is not supported. Use --batch.");
					}
					
				}
				
				manager.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
