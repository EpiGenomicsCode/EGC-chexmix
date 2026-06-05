package org.egc.chexmix.utilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;


public class SequenceAlignmentFigure {

	private static Color AColor = Color.RED;
	private static Color CColor = Color.BLUE;
	private static Color GColor = Color.ORANGE;
	private static Color TColor = Color.GREEN;
	private static Color GapColor = Color.WHITE;
	private static Color NColor = Color.GRAY;


	public SequenceAlignmentFigure(){}

	public static void setColors(Color a, Color c, Color g, Color t){
		AColor = a;
		CColor = c;
		GColor = g;
		TColor = t;
	}
	/**
	 * Visualize sequences as color pixels.
	 * Note that if the sequences are of differing lengths, this code assumes they should be left-aligned
	 * @param seqs, raw sequences or FASTA sequences
	 * @param width, width of each base, in pixel
	 * @param height, height of each base, in pixel
	 * @param f, output file
	 */
	public static void visualizeSequences(List<String> seqs, int width, int height, File f){
		if (seqs.size()==0)
			return;

		int pixheight = 0;
		int maxLen = 0;
		for (String s:seqs){
        	if (s.length()!=0 && s.charAt(0)!='>')	{		// ignore header line of FASTA file
        		pixheight += height;
        		if (maxLen < s.length())
        			maxLen = s.length();
        	}
		}
		int pixwidth = maxLen*width;

		System.setProperty("java.awt.headless", "true");
		BufferedImage im = new BufferedImage(pixwidth, pixheight,BufferedImage.TYPE_INT_ARGB);
        Graphics g = im.getGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(NColor);
        g2.fillRect(0,0,pixwidth, pixheight);

        int count = 0;
        for (String s:seqs){
        	if (s.charAt(0)=='>')			// ignore header line of FASTA file
        		continue;
        	char[] letters = s.toCharArray();
        	for (int j=0;j<letters.length;j++){
        		switch(letters[j]){
        		case 'A':
        		case 'a':
        			g.setColor(AColor);
        			break;
        		case 'C':
        		case 'c':
                    g.setColor(CColor);
        			break;
        		case 'G':
        		case 'g':
                    g.setColor(GColor);
        			break;
        		case 'T':
        		case 't':
                    g.setColor(TColor);
        			break;
        		case '-':
                    g.setColor(GapColor);
        			break;
                default:
                	g.setColor(NColor);
        		}
                g.fillRect(j*width, count*height, width, height);
        	}
            count++;
        }
        try {
            ImageIO.write(im,"png",f);
        }  catch (IOException ex) {
            ex.printStackTrace();
        }
	}
}
