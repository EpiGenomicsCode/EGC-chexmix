package org.egc.core.viz.paintable;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.Color;
import java.io.File;
import java.io.Writer;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;


public abstract class AbstractPaintable {
    
    public static int sImageWidth, sImageHeight;

    static { 
        sImageWidth = 1000;
        sImageHeight = 750;
    }
    
    protected int siw, sih;
    
    public AbstractPaintable() {
        sih = sImageHeight;
        siw = sImageWidth;
    }

    public AbstractPaintable(int h, int w) {
        sih = h;
        siw = w;
    }
    
    public void setImageWidth(int iw) { siw = iw; }
    public void setImageHeight(int ih) { sih = ih; }

    public abstract void paintItem(Graphics g, int x1, int y1, int x2, int y2);
    
    public Image createImage(int w, int h) { 
        BufferedImage im = 
            new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = im.getGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        paintItem(g, 0, 0, w, h);
        return im;
    }
    
    public void saveImage(File f, int w, int h, boolean raster) 
    throws IOException { 

        if (raster) {
            BufferedImage im = 
                new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics g = im.getGraphics();
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            paintItem(g, 0, 0, w, h);
            ImageIO.write(im, "png", f);
        } else {
            DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument(null, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
            svgGenerator.setSVGCanvasSize(new Dimension(w,h));
            svgGenerator.setColor(Color.white);        
            svgGenerator.fillRect(0,0,w,h);
            paintItem(svgGenerator,25,25,w-50,h-50);

            boolean useCSS = true;
            Writer out = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            svgGenerator.stream(out, useCSS);
        }
    }
}
