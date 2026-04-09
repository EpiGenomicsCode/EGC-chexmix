package org.egc.core.viz.metaprofile.swing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.egc.core.viz.metaprofile.*;
import org.egc.core.viz.paintable.PaintableScale;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;


public class ProfileLinePanel { 
	
	private BinningParameters params;
	private PaintableScale scale;
	private int width = 500;
	private int height = 300;
	private int lineWeight=1;
	private ProfileClusteringHandler clusteringHandler;
	private int numAxisTicks=5;
	private int fontSize=20;
	private Color lineColor =Color.blue;
	private int colorbarHeight=50;
	private boolean addPercentages=false;	
	private Vector<ProfileLinePaintable> linePainters;
	private boolean colorQuantized=false;
	private double [] colorQuantaLimits=null;
	private boolean drawColorBar=true;
	private boolean drawBorder = true; 
	private boolean transparent = false;

	public ProfileLinePanel(BinningParameters bps, PaintableScale s) { 
		params = bps;
		scale = s;
		linePainters = new Vector<ProfileLinePaintable>();
		clusteringHandler = new ProfileClusteringHandler(params);
		width = params.getNumBins();
	}
	
	public void cluster() { 
		System.out.println("Getting Profiles...");
		Vector<Profile> profs = getAllProfiles();
		System.out.println("Clustering...");
		Vector<Integer> permutation = clusteringHandler.runClustering(profs);
		System.out.println("Reordering...");
		reorder(permutation);
		System.out.println("Done.");
	}
	
	public synchronized Vector<Profile> getAllProfiles() { 
		Vector<Profile> profs = new Vector<Profile>();
		for(int i = 0; i < linePainters.size(); i++) { 
			profs.add(linePainters.get(i).getProfile());
		}
		return profs;
	}

	public synchronized void reorder(Vector<Integer> indices) { 
		Vector<ProfileLinePaintable> newLinePainters = new Vector<ProfileLinePaintable>();
		for(Integer idx : indices) { 
			newLinePainters.add(linePainters.get(idx));
		}
		linePainters = newLinePainters;
	}

	public synchronized void addProfileLinePaintable(ProfileLinePaintable plp) { 
		if(colorQuantized)
			plp.setQuanta(colorQuantaLimits);
		linePainters.add(plp);
	}
	
	/**
	 * Build line paintables from all profiles in a MetaProfile.
	 * Call after all points have been added and processing is complete.
	 * Idempotent — safe to call multiple times, only builds once.
	 * Note: does not modify the scale — scale bounds are managed externally
	 * via setMinColorVal/setMaxColorVal (called from MetaNonFrame.setLineMin/setLineMax).
	 */
	public synchronized void buildFromProfiles(MetaProfile mp) {
		if(!linePainters.isEmpty()) return;
		for(int i = 0; i < mp.size(); i++) {
			Profile p = mp.profile(i);
			ProfileLinePaintable plp = new ProfileLinePaintable(scale, p);
			addProfileLinePaintable(plp);
		}
		updateSize();
	}
	
	public int getPanelWidth(){
		return(width);
	}
	public int getPanelLength(){
		return(colorbarHeight+(linePainters.size()*lineWeight)+lineWeight+1);
	}
	private void updateSize() { 
		height = colorbarHeight+(linePainters.size()*lineWeight)+lineWeight+1;
	}
	
	public void updateFontSize(int size) { fontSize = size; }
	public void updateLineWeight(int w) { lineWeight = w; updateSize(); }
	public void updateColor(Color c) { lineColor=c; }
	public double getMaxColorVal(){return scale.getMax();}
	public void setMaxColorVal(double v){ scale.setScale(scale.getMin(), v); }
	public double getMinColorVal(){return scale.getMin();}
	public void setMinColorVal(double v){ scale.setScale(v, scale.getMax()); }
	public void setDrawColorBar(boolean c){
		drawColorBar = c;
		if(!c)
			colorbarHeight=0;
		else
			colorbarHeight=50;
	}
	public void setTransparent(boolean c){ transparent = c; }
	public void setLineColorQuanta(double[] q){
		if(q!=null){
			colorQuantaLimits=q;
			colorQuantized=true;
		}
	}
	public void setDrawBorder(boolean b){ drawBorder=b; }
	
	private void paintComponent(Graphics g) { 
		int w = width, h = height;
		
		if(!transparent){
			g.setColor(Color.white);
			g.fillRect(0, 0, w, h);
		}

		//Colorbar
		if(drawColorBar)
			drawSiteColorBar((Graphics2D)g, 0, 0);
		
		//Lines
		for(int i = 0; i < linePainters.size(); i++) { 
			linePainters.get(i).setColor(lineColor);
			linePainters.get(i).paintItem(g, 0, colorbarHeight+i*lineWeight, w, colorbarHeight+(i*lineWeight)+lineWeight+1);
		}
		
		//Labels, axes, etc 
		g.setColor(Color.DARK_GRAY);
		if(linePainters.size()>0){
			g.drawLine(0, colorbarHeight-1, w, colorbarHeight-1);
			int lastLine = colorbarHeight+(linePainters.size()*lineWeight)+lineWeight+1;
			g.drawLine(0, lastLine, w, lastLine);
		}
		
		//Axis
		g.setFont(new Font("Arial", Font.PLAIN, fontSize));
		FontMetrics metrics = g.getFontMetrics();
		g.setColor(Color.black);
		if(addPercentages){
			if(linePainters.size()>(numAxisTicks*10)){
				for(int i=1; i<=numAxisTicks; i++){
					int l = (100/numAxisTicks)*i;
					String s = String.format("%d%c", l, '%');
					g.drawString(s, w-5-metrics.stringWidth(s), colorbarHeight+(i*((linePainters.size()*lineWeight)/numAxisTicks)));
				}	
			}
		}
		
		//Border
		if(drawBorder){
			g.setColor(Color.black);
			g.drawRect(0, 0, w, h);
		}
	}
	
	public void saveImage(File f, int w, int h, boolean raster) 
    throws IOException { 
		this.width = w;
		this.height = h;
		if(raster){
	        BufferedImage im = 
	            new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D graphics = im.createGraphics();
	        graphics.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
	        paintComponent(graphics);
	        graphics.dispose();
	        ImageIO.write(im, "png", f);
		}else{
	        DOMImplementation domImpl =
	            GenericDOMImplementation.getDOMImplementation();
	        Document document = domImpl.createDocument(null, "svg", null);
	        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
	        svgGenerator.setSVGCanvasSize(new Dimension(w,h));
	        if(!transparent){
	        	svgGenerator.setColor(Color.white);
	        	svgGenerator.fillRect(0,0,w,h);
	        }
	        paintComponent(svgGenerator);
	        boolean useCSS = true;
	        FileOutputStream outStream = new FileOutputStream(f);
	        Writer out = new OutputStreamWriter(outStream, "UTF-8");
	        svgGenerator.stream(out, useCSS);
	        outStream.flush();
	        outStream.close();
		}
	}
   
	private void drawSiteColorBar(Graphics2D g2d, int x, int y){
		int cWidth = width;
		int cHeight = colorbarHeight-5;
		
		//Draw colors 
		if(!colorQuantized){
			GradientPaint colorbar = new GradientPaint(x, y, Color.white, x+cWidth, y, lineColor, false);
			g2d.setPaint(colorbar);
			g2d.fillRect(x, y, cWidth, cHeight);
		}else{
			for(int i=0; i<colorQuantaLimits.length; i++){
				int off =(int)x+(i*(int)(cWidth/(double)colorQuantaLimits.length)); 
				g2d.setColor(calcFracColor(lineColor, (double)i/(double)(colorQuantaLimits.length-1)));
				g2d.fillRect(off, y, (int)(cWidth/(double)colorQuantaLimits.length), cHeight);
			}
		}
		
		g2d.setPaint(Color.black);
		g2d.setColor(Color.black);
		
		//Zero tick if needed
		if(scale.getMin()<0){
			int zeroOff = (int)((double)cWidth*(0-scale.getMin())/(scale.getMax()-scale.getMin()));
			g2d.setStroke(new BasicStroke(1.0f));
			g2d.drawLine(zeroOff, 0,zeroOff, cHeight);
		}
		//Draw border
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.drawRect(x, y, cWidth, cHeight);
		
		//Legend
		g2d.setColor(Color.white);
		g2d.setFont(new Font("Ariel", Font.BOLD, 20));
		FontMetrics metrics = g2d.getFontMetrics();
		String maxVal;
		if(scale.getMax()>=1 || scale.getMax()==0)
			maxVal = String.format("%.0f",scale.getMax());
		else
			maxVal = String.format("%.2e",scale.getMax());
		g2d.drawString(maxVal, x+cWidth-(metrics.stringWidth(maxVal)), cHeight-2);
		
	}
	private Color calcFracColor(Color col, double v){
		Color c;
		Color maxColor = col;
		Color minColor = Color.white;
		
		double sVal = v>1 ? 1 : (v<0 ? 0 : v);
		int red = (int)(maxColor.getRed() * sVal + minColor.getRed() * (1 - sVal));
	    int green = (int)(maxColor.getGreen() * sVal + minColor.getGreen() * (1 - sVal));
	    int blue = (int)(maxColor.getBlue() *sVal + minColor.getBlue() * (1 - sVal));
	    c = new Color(red, green, blue);
		return(c);
	}
}
