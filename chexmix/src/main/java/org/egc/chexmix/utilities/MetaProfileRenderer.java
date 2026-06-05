package org.egc.chexmix.utilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.egc.core.genome.Genome;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * MetaProfileRenderer: generates metaprofile images (PNG/SVG) from read data.
 * Consolidates MetaNonFrame, MetaNonFrameMultiSet, ProfileLinePanel, ProfilePanel,
 * MultiProfilePanel, ProfilePaintable, ProfileLinePaintable, AbstractPaintable,
 * PaintableScale, and MetaUtils.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MetaProfileRenderer {

    // ---- Shared scale state -----------------------------------------------

    private double scaleMin = 0.0, scaleMax = 1.0;

    private double getScaleMin() { return scaleMin; }
    private double getScaleMax() { return scaleMax; }
    private void setScaleMin(double v) { scaleMin = v; }
    private void setScaleMax(double v) { scaleMax = v; }
    private void setScale(double mn, double mx) { scaleMin = mn; scaleMax = mx; }
    private double fractionalOffset(double value) {
        double range = scaleMax - scaleMin;
        return range == 0.0 ? 0.0 : (value - scaleMin) / range;
    }

    // ---- Single-condition renderer (MetaNonFrame) -------------------------

    private final BinningParameters params;
    private final MetaProfile profile;
    private final MetaProfileHandler handler;
    private final MetaUtils utils;
    private final boolean saveSVG;

    // line-panel state
    private int lineWidth;
    private int lineWeight = 1;
    private int colorbarHeight = 50;
    private boolean drawColorBar = true;
    private boolean drawBorder = true;
    private boolean transparent = false;
    private Color lineColor = Color.blue;
    private boolean colorQuantized = false;
    private double[] colorQuantaLimits = null;
    private Vector<LineEntry> linePainters = new Vector<>();

    // profile-panel state
    private Color peakColor = Color.blue;
    private String profileStyle = "line";

    // multi-set renderer (only populated by the multi-set constructor)
    private List<Profile> multiProfiles = null;
    private static final Color[] MULTI_COLORS = {
        Color.blue, Color.red, Color.gray, Color.green,
        Color.cyan, Color.orange, Color.magenta
    };

    /** Single-condition constructor (replaces MetaNonFrame). */
    public MetaProfileRenderer(Genome g, BinningParameters bps,
            PointProfiler profiler, boolean normalizedMeta, boolean svg) {
        params = bps;
        handler = new MetaProfileHandler("MetaProfile", bps, profiler, normalizedMeta);
        profile = handler.getProfile();
        utils = new MetaUtils(g);
        saveSVG = svg;
        lineWidth = params.getNumBins();
    }

    /** Multi-set constructor (replaces MetaNonFrameMultiSet). */
    public MetaProfileRenderer(List<String> setNames, Genome g, BinningParameters bps,
            PointProfiler profiler, boolean normalizedMeta) {
        params = bps;
        saveSVG = false;
        multiProfiles = new ArrayList<>();
        List<MetaProfileHandler> handlers = new ArrayList<>();
        for (String name : setNames) {
            MetaProfileHandler h = new MetaProfileHandler(name, bps, profiler, normalizedMeta);
            handlers.add(h);
            multiProfiles.add(h.getProfile());
        }
        // expose handlers list via a holder so MetaMaker can call getHandlers()
        multiHandlers = handlers;
        handler = null;
        profile = null;
        utils = new MetaUtils(g);
        lineWidth = params.getNumBins();
    }

    private List<MetaProfileHandler> multiHandlers = null;

    // ---- Public API (matches what MetaMaker calls) ------------------------

    public MetaProfileHandler getHandler() { return handler; }
    public List<MetaProfileHandler> getHandlers() { return multiHandlers; }
    public MetaUtils getUtils() { return utils; }

    public void setStyle(String s) { profileStyle = s; }
    public void setColor(Color c) { peakColor = c; lineColor = c; }
    public void setDrawColorBar(boolean c) {
        drawColorBar = c;
        colorbarHeight = c ? 50 : 0;
    }
    public void setTransparent(boolean c) { transparent = c; }
    public void setDrawBorder(boolean c) { drawBorder = c; }
    public void setLinePanelColorQuanta(double[] q) {
        if (q != null) { colorQuantaLimits = q; colorQuantized = true; }
    }
    public void setLineMin(double m) { setScaleMin(m); }
    public void setLineMax(double m) { setScaleMax(m); }
    public void setLineThick(int t) { lineWeight = t; }

    // ---- Image save (single-condition) ------------------------------------

    public void saveImages(String root) throws IOException {
        buildLineEntries(profile);
        int panelH = colorbarHeight + (linePainters.size() * lineWeight) + lineWeight + 1;
        String profileExt = saveSVG ? "_profile.svg" : "_profile.png";
        String linesExt   = saveSVG ? "_lines.svg"   : "_lines.png";
        // drawProfileCurve auto-expands the shared scale to profile.max() (the aggregate
        // sum across all peaks).  Save and restore so saveLineImage uses the user-set
        // lineMax/lineMin, not the inflated aggregate value.
        double savedMin = scaleMin, savedMax = scaleMax;
        saveProfileImage(new File(root + profileExt), 1200, 700, !saveSVG);
        scaleMin = savedMin; scaleMax = savedMax;
        saveLineImage(new File(root + linesExt), lineWidth, panelH, !saveSVG);
    }

    /** Multi-set image save. */
    public void saveImage(String root) throws IOException {
        saveMultiImage(new File(root + "_profile.png"), 1200, 700);
    }

    /** Save tab-separated profile values for single-condition. */
    public void savePointsToFile(String root) {
        String fileName = String.format("%s.points.txt", root);
        profile.saveToFile(fileName);
        fileName = String.format("%s.profiles.txt", root);
        profile.saveProfilesToFile(fileName);
    }

    /** Save tab-separated profile values for multi-set. */
    public void savePointsToFileMulti(String root) {
        if (multiProfiles == null) return;
        String fileName = String.format("%s.points.txt", root);
        try (java.io.FileWriter fout = new java.io.FileWriter(fileName)) {
            int start = (-1 * (params.getWindowSize() / 2)) + params.getBinSize() / 2;
            int step  = params.getWindowSize() / params.getNumBins();
            fout.write("Pos");
            for (Profile q : multiProfiles) fout.write("\t" + q.getName());
            fout.write("\n");
            int k = start;
            for (int i = 0; i < params.getNumBins(); i++) {
                fout.write(String.valueOf(k));
                for (Profile q : multiProfiles) fout.write("\t" + q.value(i));
                fout.write("\n");
                k += step;
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void clusterLinePanel() {
        buildLineEntries(profile);
        Vector<Profile> profs = new Vector<>();
        for (LineEntry e : linePainters) profs.add(e.profile);
        MetaProfileClustering clustering = new MetaProfileClustering(params);
        Vector<Integer> perm = clustering.cluster(profs);
        Vector<LineEntry> reordered = new Vector<>();
        for (Integer idx : perm) reordered.add(linePainters.get(idx));
        linePainters = reordered;
    }

    // ---- Line-entry building (replaces ProfileLinePanel.buildFromProfiles) -

    private synchronized void buildLineEntries(MetaProfile mp) {
        if (!linePainters.isEmpty()) return;
        for (int i = 0; i < mp.size(); i++)
            linePainters.add(new LineEntry(mp.profile(i)));
    }

    // ---- Profile image rendering (replaces ProfilePanel) ------------------

    private void saveProfileImage(File f, int w, int h, boolean raster) throws IOException {
        if (raster) {
            BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = im.createGraphics();
            g2.setRenderingHints(new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            paintProfile(g2, w, h);
            g2.dispose();
            ImageIO.write(im, "png", f);
        } else {
            svgSave(f, w, h, false, (g) -> paintProfile(g, w, h));
        }
    }

    private void paintProfile(Graphics g, int w, int h) {
        int border = 20;
        Graphics2D g2 = (Graphics2D) g;
        if (!transparent) { g2.setColor(Color.white); g2.fillRect(0, 0, w, h); }

        // draw the profile curve
        drawProfileCurve(g2, profile, peakColor, profileStyle, border, 0, w, h - border);

        int binPix = w / (profile.length() + 1);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics metrics = g2.getFontMetrics();
        g2.setColor(Color.black);
        if (profile.max() < 10 && profile.max() > 1)
            g2.drawString(String.format("%.2f", getScaleMax()), border / 2, 12);
        else if (profile.max() >= 1 || profile.max() == 0)
            g2.drawString(String.format("%.0f", getScaleMax()), border / 2, 12);
        else
            g2.drawString(String.format("%.2e", getScaleMax()), border / 2, 12);
        if (profile.min() == 0 || profile.min() <= -1)
            g2.drawString(String.format("%.0f", getScaleMin()), border / 2, h - border - 1);
        else
            g2.drawString(String.format("%.2e", getScaleMin()), border / 2, h - border - 1);
        String counter = String.format("%d datapoints", profile.getNumProfiles());
        g2.drawString(counter, w - border - metrics.stringWidth(counter), 12);

        int xaxispos = h - border;
        if (profile.min() < 0) {
            double frac = getScaleMax() / (getScaleMax() - getScaleMin());
            xaxispos = (int) ((double) (h - border - 1) * frac);
            g2.drawString("0", border / 2, xaxispos);
        }
        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(border, h - border, (binPix * profile.length()) + border, h - border);
        BinningParameters bps = profile.getBinningParameters();
        String minVal = String.format("%d", -1 * (bps.getWindowSize() / 2));
        String maxVal = String.format("%d", bps.getWindowSize() / 2);
        g2.drawString(maxVal, border + (binPix * profile.length()) - metrics.stringWidth(maxVal), h);
        g2.drawString(minVal, border, h);

        int lineHeight = 20, lineWidthPx = 4;
        g2.setColor(Color.black);
        if (profile.isStranded()) {
            g2.setStroke(new BasicStroke((float) lineWidthPx));
            int[] a = new int[7], b = new int[7];
            arrangeArrow(a, b, lineHeight,
                    border + (binPix * (profile.length() / 2)),
                    border + (binPix * (profile.length() / 2)) + 150,
                    h - border);
            g2.drawPolyline(a, b, 7);
        } else {
            g2.fillRect(border + (binPix * (profile.length() / 2)) - lineWidthPx / 2,
                    h - lineHeight - (border / 2), lineWidthPx, lineHeight);
        }
    }

    // ---- Profile curve painter (replaces ProfilePaintable) ----------------

    private void drawProfileCurve(Graphics2D g2, Profile p, Color col, String style,
            int x1, int y1, int x2, int y2) {
        int w = x2 - x1, h = y2 - y1;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.5f));
        int binPix = w / (params.getNumBins() + 1);
        int[] xs = new int[params.getNumBins()];
        int[] ys = new int[params.getNumBins()];

        synchronized (p) {
            if (p.min() < getScaleMin()) setScale(p.min(), getScaleMax());
            if (p.max() > getScaleMax()) setScale(getScaleMin(), p.max());
            for (int i = 0; i < params.getNumBins(); i++) {
                xs[i] = x1 + (i + 1) * binPix;
                ys[i] = y2 - (int) Math.round(fractionalOffset(p.value(i)) * (double) h);
            }
            g2.setColor(col);
            if (style.equalsIgnoreCase("histogram")) {
                for (int i = 0; i < xs.length; i++)
                    g2.fillRect(xs[i], ys[i], binPix, h - ys[i]);
            } else {
                for (int i = 1; i < xs.length; i++)
                    g2.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i]);
                int rad = 2, diam = 4;
                for (int i = 0; i < xs.length; i++) {
                    g2.setColor(Color.white);
                    g2.fillOval(xs[i] - rad, ys[i] - rad, diam, diam);
                    g2.setColor(col);
                    g2.drawOval(xs[i] - rad, ys[i] - rad, diam, diam);
                }
            }
        }
        g2.setStroke(oldStroke);
    }

    // ---- Line image rendering (replaces ProfileLinePanel) -----------------

    private void saveLineImage(File f, int w, int h, boolean raster) throws IOException {
        if (raster) {
            BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = im.createGraphics();
            g2.setRenderingHints(new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            paintLines(g2, w, h);
            g2.dispose();
            ImageIO.write(im, "png", f);
        } else {
            svgSave(f, w, h, transparent, (g) -> paintLines(g, w, h));
        }
    }

    private void paintLines(Graphics g, int w, int h) {
        if (!transparent) { g.setColor(Color.white); g.fillRect(0, 0, w, h); }
        if (drawColorBar) drawSiteColorBar((Graphics2D) g, w);
        for (int i = 0; i < linePainters.size(); i++) {
            LineEntry e = linePainters.get(i);
            e.color = lineColor;
            paintLineEntry(g, e, 0, colorbarHeight + i * lineWeight, w,
                    colorbarHeight + i * lineWeight + lineWeight + 1);
        }
        g.setColor(Color.DARK_GRAY);
        if (!linePainters.isEmpty()) {
            g.drawLine(0, colorbarHeight - 1, w, colorbarHeight - 1);
            int lastLine = colorbarHeight + (linePainters.size() * lineWeight) + lineWeight + 1;
            g.drawLine(0, lastLine, w, lastLine);
        }
        if (drawBorder) { g.setColor(Color.black); g.drawRect(0, 0, w, h); }
    }

    private void paintLineEntry(Graphics g, LineEntry e, int x1, int y1, int x2, int y2) {
        int w = x2 - x1, h = y2 - y1;
        for (int i = 0; i < params.getNumBins(); i++) {
            int x = x1 + i * (w / params.getNumBins());
            double value = e.profile.value(i);
            double yf;
            if (colorQuantized) {
                yf = 0;
                for (int a = 0; a < colorQuantaLimits.length; a++)
                    if (value > colorQuantaLimits[a])
                        yf = (double) a / (double) (colorQuantaLimits.length - 1);
            } else {
                yf = fractionalOffset(value);
            }
            if (yf > 0) {
                g.setColor(fracColor(e.color, yf));
                g.fillRect(x, y1, w / params.getNumBins(), h);
            }
        }
    }

    private void drawSiteColorBar(Graphics2D g2d, int w) {
        int cWidth = w;
        int cHeight = colorbarHeight - 5;
        if (!colorQuantized) {
            GradientPaint gp = new GradientPaint(0, 0, Color.white, cWidth, 0, lineColor, false);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, cWidth, cHeight);
        } else {
            for (int i = 0; i < colorQuantaLimits.length; i++) {
                int off = i * (cWidth / colorQuantaLimits.length);
                g2d.setColor(fracColor(lineColor, (double) i / (double) (colorQuantaLimits.length - 1)));
                g2d.fillRect(off, 0, cWidth / colorQuantaLimits.length, cHeight);
            }
        }
        g2d.setPaint(Color.black);
        g2d.setColor(Color.black);
        if (scaleMin < 0) {
            int zeroOff = (int) ((double) cWidth * (0 - scaleMin) / (scaleMax - scaleMin));
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawLine(zeroOff, 0, zeroOff, cHeight);
        }
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRect(0, 0, cWidth, cHeight);
        g2d.setColor(Color.white);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics metrics = g2d.getFontMetrics();
        String maxVal = (scaleMax >= 1 || scaleMax == 0)
                ? String.format("%.0f", scaleMax) : String.format("%.2e", scaleMax);
        g2d.drawString(maxVal, cWidth - metrics.stringWidth(maxVal), cHeight - 2);
    }

    // ---- Multi-set image rendering (replaces MultiProfilePanel) -----------

    private void saveMultiImage(File f, int w, int h) throws IOException {
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = im.createGraphics();
        g2.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        paintMulti(g2, w, h);
        ImageIO.write(im, "png", f);
    }

    private void paintMulti(Graphics g, int w, int h) {
        int border = 20;
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);
        g2.fillRect(0, 0, w, h);

        double profileMax = 0, profileMin = Double.MAX_VALUE;
        int binPix = 0, profileLength = 0;
        BinningParameters bps = multiProfiles.get(0).getBinningParameters();
        boolean isStranded = false;

        for (Profile p : multiProfiles) {
            if (binPix < w / (p.length() + 1)) binPix = w / (p.length() + 1);
            if (profileLength < p.length()) profileLength = p.length();
            if (profileMax < p.max()) profileMax = p.max();
            if (profileMin > p.min()) profileMin = p.min();
            isStranded = isStranded || p.isStranded();
            setScale(profileMin, profileMax);
        }

        for (int p = 0; p < multiProfiles.size(); p++)
            drawProfileCurve(g2, multiProfiles.get(p),
                    MULTI_COLORS[p % MULTI_COLORS.length], profileStyle,
                    border, 0, w, h - border);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics metrics = g2.getFontMetrics();
        int maxLegendWidth = 0;
        for (Profile p : multiProfiles) {
            String label = String.format("%s: %d datapoints", p.getName(), p.getNumProfiles());
            maxLegendWidth = Math.max(maxLegendWidth, metrics.stringWidth(label));
        }
        for (int p = 0; p < multiProfiles.size(); p++) {
            g2.setColor(MULTI_COLORS[p % MULTI_COLORS.length]);
            String label = String.format("%s: %d datapoints",
                    multiProfiles.get(p).getName(), multiProfiles.get(p).getNumProfiles());
            g2.drawString(label, w - border - maxLegendWidth, 12 + 12 * p);
        }

        g2.setColor(Color.black);
        if (profileMax < 10 && profileMax > 1)
            g2.drawString(String.format("%.2f", getScaleMax()), border / 2, 12);
        else if (profileMax >= 1 || profileMax == 0)
            g2.drawString(String.format("%.0f", getScaleMax()), border / 2, 12);
        else
            g2.drawString(String.format("%.2e", getScaleMax()), border / 2, 12);
        if (profileMin == 0 || profileMin <= -1)
            g2.drawString(String.format("%.0f", getScaleMin()), border / 2, h - border - 1);
        else
            g2.drawString(String.format("%.2e", getScaleMin()), border / 2, h - border - 1);

        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(border, h - border, (binPix * profileLength) + border, h - border);
        String minLabel = String.format("%d", -1 * (bps.getWindowSize() / 2));
        String maxLabel = String.format("%d", bps.getWindowSize() / 2);
        g2.drawString(maxLabel, border + binPix * profileLength - metrics.stringWidth(maxLabel), h);
        g2.drawString(minLabel, border, h);

        g2.setColor(Color.black);
        int lineHeight = 20, lineWidthPx = 4;
        if (isStranded) {
            g2.setStroke(new BasicStroke((float) lineWidthPx));
            int[] a = new int[7], b = new int[7];
            arrangeArrow(a, b, lineHeight,
                    border + (binPix * (profileLength / 2)),
                    border + (binPix * (profileLength / 2)) + 150,
                    h - border);
            g2.drawPolyline(a, b, 7);
        } else {
            g2.fillRect(border + (binPix * (profileLength / 2)) - lineWidthPx / 2,
                    h - lineHeight - (border / 2), lineWidthPx, lineHeight);
        }
    }

    // ---- Shared helpers ---------------------------------------------------

    private static Color fracColor(Color col, double v) {
        double sVal = v > 1 ? 1 : (v < 0 ? 0 : v);
        int red   = (int) (col.getRed()   * sVal + Color.white.getRed()   * (1 - sVal));
        int green = (int) (col.getGreen() * sVal + Color.white.getGreen() * (1 - sVal));
        int blue  = (int) (col.getBlue()  * sVal + Color.white.getBlue()  * (1 - sVal));
        return new Color(red, green, blue, col.getAlpha());
    }

    private static void arrangeArrow(int[] a, int[] b, int height,
            int gx1, int gx2, int my) {
        double arrowHt = 0.1 * height, arrowWd = 2;
        int a1 = gx1;
        int a2 = (int) Math.round(gx1 + arrowWd * 6);
        int a3 = (int) Math.round(gx1 + arrowWd * 10);
        a[0] = a1; a[1] = a1; a[2] = a2; a[3] = a2; a[4] = a3; a[5] = a2; a[6] = a2;
        int b1 = my;
        int b2 = (int) Math.round(my - arrowHt * 13);
        int b3 = (int) Math.round(my - arrowHt * 10);
        int b4 = (int) Math.round(my - arrowHt * 16);
        b[0] = b1; b[1] = b2; b[2] = b2; b[3] = b3; b[4] = b2; b[5] = b4; b[6] = b2;
    }

    @FunctionalInterface
    private interface Painter { void paint(Graphics g); }

    private static void svgSave(File f, int w, int h, boolean transparent, Painter painter)
            throws IOException {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document doc = domImpl.createDocument(null, "svg", null);
        SVGGraphics2D svg = new SVGGraphics2D(doc);
        svg.setSVGCanvasSize(new Dimension(w, h));
        if (!transparent) { svg.setColor(Color.white); svg.fillRect(0, 0, w, h); }
        painter.paint(svg);
        try (FileOutputStream fos = new FileOutputStream(f);
             Writer out = new OutputStreamWriter(fos, "UTF-8")) {
            svg.stream(out, true);
            fos.flush();
        }
    }

    // ---- Line entry (replaces ProfileLinePaintable) -----------------------

    private static class LineEntry {
        final Profile profile;
        Color color = Color.blue;
        LineEntry(Profile p) { profile = p; }
    }
}
