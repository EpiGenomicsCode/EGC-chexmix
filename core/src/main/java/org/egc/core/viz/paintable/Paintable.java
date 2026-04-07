package org.egc.core.viz.paintable;

import java.awt.Graphics;

public interface Paintable { 
    public void paintItem(Graphics g, 
            int ulx, int uly, 
            int lrx, int lry);
    public void addPaintableChangedListener(PaintableChangedListener l);
    public void removePaintableChangedListener(PaintableChangedListener l);
}
