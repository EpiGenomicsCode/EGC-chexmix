package org.egc.chexmix.utilities;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.egc.core.deepseq.experiments.ExperimentCondition;
import org.egc.core.deepseq.experiments.ExperimentManager;
import org.egc.core.deepseq.experiments.ExptConfig;
import org.egc.core.genome.Genome;
import org.egc.core.genome.GenomeConfig;
import org.egc.core.genome.location.Point;
import org.egc.chexmix.framework.ChExMixConfig;
import org.egc.core.viz.metaprofile.BinningParameters;
import org.egc.core.viz.metaprofile.MetaConfig;
import org.egc.core.viz.metaprofile.MetaProfileHandler;
import org.egc.core.viz.metaprofile.PointProfiler;
import org.egc.core.viz.metaprofile.swing.MetaFrame;
import org.egc.core.viz.metaprofile.swing.MetaNonFrame;
import org.egc.core.viz.metaprofile.swing.MetaNonFrameMultiSet;


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
							MetaNonFrame nonframe = new MetaNonFrame(gen, params, profiler, normalizeProfile, mconfig.saveSVG);
							nonframe.setColor(mconfig.color);
							nonframe.setDrawColorBar(mconfig.drawColorBar);
							nonframe.setTransparent(mconfig.transparent);
							nonframe.setDrawBorder(mconfig.drawBorder);
							MetaProfileHandler handler = nonframe.getHandler();
							if(mconfig.peakFiles.size()==1){
								System.out.println("Single set mode...");
								File peakFile = new File(mconfig.peakFiles.get(0));
								if (peakFile.exists()){
									Vector<Point> points = nonframe.getUtils().loadPoints(peakFile);
									handler.addPoints(points);
								}
							}else{
								System.out.println("All TSS mode...");
								Iterator<Point> points = nonframe.getUtils().loadTSSs("refGene");
								handler.addPoints(points);
							}
							while(handler.addingPoints()){}
							if(mconfig.cluster)
								nonframe.clusterLinePanel();
							//Set the panel sizes here...
							nonframe.setStyle(mconfig.profileStyle);
							nonframe.setLineMax(mconfig.lineMax);
							nonframe.setLineMin(mconfig.lineMin);
							nonframe.setLineThick(mconfig.lineThick);
							nonframe.saveImages(imagePrefix);
							nonframe.savePointsToFile(intPrefix);
						}else if(mconfig.peakFiles.size()>1){
							System.out.println("Multiple set mode...");
							MetaNonFrameMultiSet multinonframe = new MetaNonFrameMultiSet(mconfig.peakFiles, gen, params, profiler, true);
							for(int x=0; x<mconfig.peakFiles.size(); x++){
								String pf = mconfig.peakFiles.get(x);
								Vector<Point> points = multinonframe.getUtils().loadPoints(new File(pf));
								List<MetaProfileHandler> handlers = multinonframe.getHandlers();
								handlers.get(x).addPoints(points);
								while(handlers.get(x).addingPoints()){}
							}
							multinonframe.saveImage(imagePrefix);
							multinonframe.savePointsToFile(intPrefix);
						}
						System.out.println("Finished");
						if(profiler!=null)
							profiler.cleanup();
					}else{
						System.out.println("Initializing Meta-point frame...");
						MetaFrame frame = new MetaFrame(gen, params, profiler, normalizeProfile);
						frame.setColor(mconfig.color);
						frame.setLineMax(mconfig.lineMax);
						frame.setLineMin(mconfig.lineMin);
						frame.setLineThick(mconfig.lineThick);
						frame.startup();
						if(mconfig.peakFiles.size() > 0){
							MetaProfileHandler handler = frame.getHandler();
							for(String pf : mconfig.peakFiles){
								Vector<Point> points = frame.getUtils().loadPoints(new File(pf));
								handler.addPoints(points);
							}
						}
						frame.setLineMax(mconfig.lineMax);
						frame.setLineMin(mconfig.lineMin);
					}
					
				}
				
				manager.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		GenomeConfig gconfig = new GenomeConfig(args);
		ChExMixConfig cconfig = new ChExMixConfig(gconfig, args);
		ExptConfig econfig = new ExptConfig(gconfig.getGenome(), args);
		ExperimentManager manager = new ExperimentManager(econfig);
		MetaConfig mconfig = new MetaConfig(args);
		
		MetaMaker maker = new MetaMaker(cconfig, gconfig, mconfig, manager);
		
		maker.run();
	}
	
}
