package com.biol498.rpkmvisualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import com.clcbio.api.base.util.NoRemovalIterator;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;

public class VisualizerPanel extends JComponent {
    private static final long serialVersionUID = -2830785835576165007L;
    private Sequence seq;
    private int length;
    private int maxRpkm;
    private int maxMagnitude;
    private static final int margin = 30;
    private int diameter;
    private double zoom;
    private List<RpkmRegion> rpkmRegions;
    private int maxLabelWidth;
    private int leftColumnHeight;
    private int rightColumnHeight;

    private boolean needResize;

    public VisualizerPanel(List<RpkmRegion> regions) {
        //seq = s;
        //this.setLayout(null);

        rpkmRegions = regions;

        diameter = 300;
        zoom = 1.5d;

        // These have to be set when Graphics is available for font measuring
        maxLabelWidth = -1;

        needResize = true;

        maxRpkm = 0;
        maxMagnitude = 50;
        length = 0;

        for(int i = 0; i < rpkmRegions.size(); i++) {
            if((int)rpkmRegions.get(i).getRpkm() > maxRpkm)
                maxRpkm = (int)rpkmRegions.get(i).getRpkm();
            if(rpkmRegions.get(i).getEnd() > length)
                length = rpkmRegions.get(i).getEnd();
        }

        this.setPreferredSize(new Dimension(getWholeWidth(),getWholeHeight()));
    }

    private Double rpkmToMagnitude(Double rpkm) {
        return rpkm / maxRpkm * 50;
    }

    private int getWholeWidth() {
        return (2 * margin) + getFigureWidth() + (2 * getTextColumnWidth()); // Add a margin between text columns and figure
    }

    private int getWholeHeight() {
        return (2 * margin) + Math.max(getFigureWidth(), Math.max(leftColumnHeight, rightColumnHeight)); // Add a margin between text columns and figure
    }

    private int getTextColumnWidth() {
        return maxLabelWidth; // Find max label width
    }

    private int getFigureWidth() {
        return (int)((diameter + maxMagnitude) * zoom);
    }

    private void recalculateSize(Graphics g) {
        FontMetrics fm = g.getFontMetrics();

        int countRightColumn = 0;
        for (int i = 0; i < rpkmRegions.size(); i++) {
            if ((rpkmRegions.get(i).getStart() + rpkmRegions.get(i).getEnd())/2 < length/2)
                countRightColumn++;
            if ((int)fm.getStringBounds(rpkmRegions.get(i).getName(), g).getWidth() > maxLabelWidth)
                maxLabelWidth = (int)fm.getStringBounds(rpkmRegions.get(i).getName(), g).getWidth();
        }
        int countLeftColumn = rpkmRegions.size() - countRightColumn;

        leftColumnHeight = (int)(countLeftColumn * fm.getStringBounds("A", g).getHeight());
        rightColumnHeight = (int)(countRightColumn * fm.getStringBounds("A", g).getHeight());

        this.setPreferredSize(new Dimension(getWholeWidth(),getWholeHeight()));
        this.revalidate();
        needResize = false;
    }

    public void paint(Graphics graphics) {
        super.paint(graphics);

        Graphics2D g = (Graphics2D)graphics;
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D bounds;

        if (needResize)
            recalculateSize(g);

        int zoomdiam = (int)(diameter * zoom);
        int centerx = getWholeWidth()/2;
        int centery = getWholeHeight()/2;
        int left = centerx - (zoomdiam/2);
        int top = centery - (zoomdiam/2);
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = g.getColor();

        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(c);
        g.drawOval(left, top, zoomdiam, zoomdiam);
        g.fillOval(left, top, zoomdiam, zoomdiam);

        double startDeg;
        double endDeg;
        double magnitude;
        for (int i = 0; i < rpkmRegions.size(); i++) {
            startDeg = 360 * rpkmRegions.get(i).getStart() / length;
            endDeg = 360 * rpkmRegions.get(i).getEnd() / length;

            magnitude = rpkmToMagnitude(rpkmRegions.get(i).getRpkm()) * zoom;

            g.fill(
                    new Arc2D.Double(
                            left - magnitude/2,
                            top  - magnitude/2,
                            zoomdiam + magnitude,
                            zoomdiam + magnitude,
                            90 - startDeg,
                            -(endDeg - startDeg),
                            Arc2D.PIE));
        }
        g.setColor(Color.white);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g.fillOval(left, top, zoomdiam, zoomdiam);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c);
        g.drawOval(left, top, zoomdiam, zoomdiam);
        
        int rightcolumn_left = margin + getTextColumnWidth() + getFigureWidth();
        int rightcolumn_top = (getWholeHeight() - rightColumnHeight)/2;
        int leftcolumn_top = (getWholeHeight() - leftColumnHeight)/2;
        int leftcolumn_right = margin + getTextColumnWidth();
        int pos = rightcolumn_top;
        int i = 0;
        for (; i < rpkmRegions.size(); i++) {
            if ((rpkmRegions.get(i).getStart() + rpkmRegions.get(i).getEnd())/2 > length/2)
                break;
            bounds = fm.getStringBounds(rpkmRegions.get(i).getName(), g);
            g.drawString(
                    rpkmRegions.get(i).getName(),
                    rightcolumn_left,
                    (int)(pos + bounds.getHeight()));
            pos = pos + (int)bounds.getHeight();
        }
        pos = leftcolumn_top;
        for (; i < rpkmRegions.size(); i++) {
            bounds = fm.getStringBounds(rpkmRegions.get(i).getName(), g);
            g.drawString(
                    rpkmRegions.get(i).getName(),
                    (int)(leftcolumn_right - bounds.getWidth()),
                    (int)(pos + bounds.getHeight()));
            pos = pos + (int)bounds.getHeight();
        }
    }

    public void setZoom(double z) {
        zoom = z;
        needResize = true;
        repaint();
    }
}
